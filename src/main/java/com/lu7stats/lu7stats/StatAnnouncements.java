package com.lu7stats.lu7stats;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;
import org.json.JSONObject;

public class StatAnnouncements extends JavaPlugin implements TabExecutor {
    private static final String DEFAULT_MESSAGE = "&aThe player with the highest %stat% is: &c%topPlayer% &awith &c%number%!";
    private static final String DEFAULT_PREFIX = "&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l]";
    private static final String DEFAULT_STAT_PERMISSION = "lu7stats.seebroadcasts";

    private FileConfiguration config;
    private final Map<String, String> statMessages = new LinkedHashMap<>();
    private final List<StatDefinition> statistics = new ArrayList<>();
    private String defaultMessage = DEFAULT_MESSAGE;
    private String messagePrefix = DEFAULT_PREFIX;
    private String lastBroadcastedStat;
    private int randomStatInterval = 15;
    private boolean debugModeEnabled;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        debugModeEnabled = config.getBoolean("enableDebug", false);
        loadConfigValues();

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Could not create plugin data folder!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!loadAndPopulateMessages()) {
            getLogger().severe("Unable to load messages.json. LU7 Stats will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerCommands();

        if (config.getBoolean("enablebStats", true)) {
            try {
                int pluginId = 20633;
                new Metrics(this, pluginId);
                getLogger().info("bStats metrics has been enabled. To opt-out, set 'enablebStats' to false.");
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Error initializing bStats.", e);
            }
        }

        getLogger().info("Discovered " + statistics.size() + " usable Minecraft statistics.");
        getLogger().info("LU7 Stats plugin has been enabled!");
        getServer().getScheduler().runTaskLater(this, this::scheduleStatBroadcast, 100L);
    }

    @Override
    public void onDisable() {
        statMessages.clear();
        statistics.clear();
        getLogger().info("LU7 Stats plugin has been disabled!");
    }

    private void loadConfigValues() {
        messagePrefix = config.getString("messagePrefix", DEFAULT_PREFIX);
        randomStatInterval = config.getInt("randomStatInterval", 15);

        if (randomStatInterval <= 0) {
            getLogger().warning("randomStatInterval must be greater than 0. Automatic broadcasts will be disabled.");
        }

        if (debugModeEnabled) {
            getLogger().info("DEBUG: messagePrefix = " + messagePrefix);
            getLogger().info("DEBUG: randomStatInterval = " + randomStatInterval);
        }
    }

    private boolean loadAndPopulateMessages() {
        File messagesFile = new File(getDataFolder(), "messages.json");

        if (!messagesFile.exists()) {
            try {
                JSONObject initial = new JSONObject();
                initial.put("_default", DEFAULT_MESSAGE);
                Files.writeString(messagesFile.toPath(), initial.toString(2), StandardCharsets.UTF_8);
                getLogger().info("Created default messages.json.");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Unable to create messages.json.", e);
                return false;
            }
        }

        JSONObject messages;
        try {
            String json = Files.readString(messagesFile.toPath(), StandardCharsets.UTF_8);
            messages = new JSONObject(json);
        } catch (IOException | JSONException e) {
            getLogger().log(Level.SEVERE, "Unable to read messages.json.", e);
            return false;
        }

        defaultMessage = messages.optString("_default", DEFAULT_MESSAGE);
        if (defaultMessage.isBlank()) {
            defaultMessage = DEFAULT_MESSAGE;
        }

        List<StatDefinition> discoveredStatistics = discoverStatistics();
        Map<String, String> existingMessages = new LinkedHashMap<>();

        for (String key : messages.keySet()) {
            if (key.equals("_default")) {
                continue;
            }
            Object value = messages.opt(key);
            if (value instanceof String string) {
                existingMessages.put(key, string);
            }
        }

        Map<String, String> outputMessages = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        outputMessages.putAll(existingMessages);

        int added = 0;
        for (StatDefinition stat : discoveredStatistics) {
            if (!outputMessages.containsKey(stat.key())) {
                outputMessages.put(stat.key(), defaultMessage);
                added++;
            }
        }

        JSONObject output = new JSONObject(new LinkedHashMap<>());
        output.put("_default", defaultMessage);

        for (Map.Entry<String, String> entry : outputMessages.entrySet()) {
            output.put(entry.getKey(), entry.getValue());
        }

        try {
            Files.writeString(messagesFile.toPath(), output.toString(2), StandardCharsets.UTF_8);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Unable to write populated messages.json.", e);
            return false;
        }

        statMessages.clear();
        for (Map.Entry<String, String> entry : outputMessages.entrySet()) {
            if (isKnownStatisticKey(entry.getKey())) {
                statMessages.put(entry.getKey(), entry.getValue());
            }
        }

        statistics.clear();
        statistics.addAll(discoveredStatistics);

        if (added > 0) {
            getLogger().info("Automatically added " + added + " new statistics to messages.json.");
        }

        if (debugModeEnabled) {
            getLogger().info("DEBUG: Loaded " + statMessages.size() + " statistic message overrides.");
        }

        return true;
    }

    private List<StatDefinition> discoverStatistics() {
        List<StatDefinition> discovered = new ArrayList<>();

        for (Statistic statistic : Registry.STATISTIC) {
            if (statistic == null) {
                continue;
            }

            switch (statistic.getType()) {
                case UNTYPED -> discovered.add(new StatDefinition(statistic, null, null, statistic.getKey().getKey()));
                case BLOCK -> {
                    for (Material material : Material.values()) {
                        if (!material.isBlock()) {
                            continue;
                        }
                        String key = buildKey(statistic, material);
                        discovered.add(new StatDefinition(statistic, material, null, key));
                    }
                }
                case ITEM -> {
                    for (Material material : Material.values()) {
                        if (!material.isItem()) {
                            continue;
                        }
                        String key = buildKey(statistic, material);
                        discovered.add(new StatDefinition(statistic, material, null, key));
                    }
                }
                case ENTITY -> {
                    for (EntityType entityType : EntityType.values()) {
                        if (!isValidEntityStatistic(statistic, entityType)) {
                            continue;
                        }
                        String key = buildKey(statistic, entityType);
                        discovered.add(new StatDefinition(statistic, null, entityType, key));
                    }
                }
            }
        }

        Map<String, StatDefinition> unique = new LinkedHashMap<>();
        for (StatDefinition definition : discovered) {
            unique.putIfAbsent(definition.key(), definition);
        }

        List<StatDefinition> result = new ArrayList<>(unique.values());
        result.sort(Comparator.comparing(StatDefinition::key, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private boolean isValidEntityStatistic(Statistic statistic, EntityType entityType) {
        try {
            org.bukkit.scoreboard.Criteria.statistic(statistic, entityType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String buildKey(Statistic statistic, Material material) {
        return statistic.getKey().getKey() + ":" + material.getKey().getKey();
    }

    private String buildKey(Statistic statistic, EntityType entityType) {
        return statistic.getKey().getKey() + ":" + entityType.getKey().getKey();
    }

    private boolean isKnownStatisticKey(String key) {
        for (StatDefinition statistic : statistics) {
            if (statistic.key().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private void registerCommands() {
        registerCommand("broadcaststat");
        registerCommand("lu7statsreload");
        registerCommand("lu7statshealth");
    }

    private void registerCommand(String commandName) {
        if (getCommand(commandName) == null) {
            getLogger().warning("Command '" + commandName + "' is missing from plugin.yml.");
            return;
        }
        getCommand(commandName).setExecutor(this);
        getCommand(commandName).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "broadcaststat" -> {
                if (args.length == 0) {
                    sendRandomAnnouncement();
                    sender.sendMessage(colorize(messagePrefix + " " + "&aBroadcast triggered manually with a random stat!"));
                    return true;
                }
                if (args.length == 1) {
                    sendManualAnnouncement(args[0], sender);
                    return true;
                }
                sender.sendMessage(colorize("&cUsage: /broadcaststat [stat]"));
                return true;
            }
            case "lu7statsreload" -> {
                if (args.length != 0) {
                    sender.sendMessage(colorize("&cUsage: /lu7statsreload"));
                    return true;
                }
                reloadFiles(sender);
                return true;
            }
            case "lu7statshealth" -> {
                if (args.length != 0) {
                    sender.sendMessage(colorize("&cUsage: /lu7statshealth"));
                    return true;
                }
                checkHealth(sender);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!command.getName().equalsIgnoreCase("broadcaststat")) {
            return completions;
        }

        if (args.length != 1) {
            return completions;
        }

        String partial = args[0].toLowerCase();

        for (StatDefinition statistic : statistics) {
            if (statistic.key().toLowerCase().startsWith(partial)) {
                completions.add(statistic.key());
            }
        }

        return completions;
    }

    private void reloadFiles(CommandSender sender) {
        reloadConfig();
        config = getConfig();
        debugModeEnabled = config.getBoolean("enableDebug", false);
        loadConfigValues();

        sender.sendMessage(colorize(messagePrefix + " " + "&aConfig.yml reloaded successfully!"));

        boolean success = loadAndPopulateMessages();

        if (success) {
            sender.sendMessage(colorize(messagePrefix + " " + "&aMessages.json reloaded and synchronised with Paper's statistic registry!"));
        } else {
            sender.sendMessage(colorize(messagePrefix + " " + "&cError reloading messages.json. Check console."));
        }
    }

    private void scheduleStatBroadcast() {
        if (randomStatInterval <= 0) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                sendRandomAnnouncement();
            }
        }.runTaskTimer(this, 0L, 20L * 60L * randomStatInterval);

        getLogger().info("Stat broadcast task scheduled with an interval of " + randomStatInterval + " minutes.");
    }

    private void sendRandomAnnouncement() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            if (debugModeEnabled) {
                getLogger().info("DEBUG: No players online; skipping stat broadcast.");
            }
            return;
        }

        if (statistics.isEmpty()) {
            getLogger().warning("No statistics are available.");
            return;
        }

        StatDefinition selected = chooseRandomStatistic();

        if (selected == null) {
            return;
        }

        if (debugModeEnabled) {
            getLogger().info("DEBUG: Selected random statistic: " + selected.key());
        }

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            LeaderboardEntry result = findTopPlayer(selected);

            if (result == null || result.value() <= 0) {
                if (debugModeEnabled) {
                    getLogger().info("DEBUG: " + selected.key() + " has no non-zero leaderboard value.");
                }
                getServer().getScheduler().runTask(this, this::sendRandomAnnouncement);
                return;
            }

            getServer().getScheduler().runTask(this, () -> {
                processAnnouncement(selected, result);
                lastBroadcastedStat = selected.key();
            });
        });
    }

    private StatDefinition chooseRandomStatistic() {
        if (statistics.isEmpty()) {
            return null;
        }

        Random random = new Random();

        if (statistics.size() == 1) {
            return statistics.get(0);
        }

        for (int attempt = 0; attempt < 20; attempt++) {
            StatDefinition candidate = statistics.get(random.nextInt(statistics.size()));
            if (!candidate.key().equalsIgnoreCase(lastBroadcastedStat)) {
                return candidate;
            }
        }

        for (StatDefinition candidate : statistics) {
            if (!candidate.key().equalsIgnoreCase(lastBroadcastedStat)) {
                return candidate;
            }
        }

        return statistics.get(0);
    }

    private void sendManualAnnouncement(String specifiedStat, CommandSender sender) {
        StatDefinition definition = findStatistic(specifiedStat);

        if (definition == null) {
            sender.sendMessage(colorize(messagePrefix + " " + "&cInvalid statistic: " + specifiedStat));
            return;
        }

        sender.sendMessage(colorize(messagePrefix + " " + "&aBroadcast triggered manually for stat: &e" + definition.key()));

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            LeaderboardEntry result = findTopPlayer(definition);

            if (result == null || result.value() <= 0) {
                getServer().getScheduler().runTask(this, () -> sender.sendMessage(colorize(messagePrefix + " " + "&cNo player has a non-zero value for this statistic.")));
                return;
            }

            getServer().getScheduler().runTask(this, () -> {
                processAnnouncement(definition, result);
                lastBroadcastedStat = definition.key();
            });
        });
    }

    private LeaderboardEntry findTopPlayer(StatDefinition definition) {
        OfflinePlayer topPlayer = null;
        int highestValue = 0;
        OfflinePlayer[] players = Bukkit.getOfflinePlayers();

        for (OfflinePlayer player : players) {
            int value;
            try {
                value = readStatistic(player, definition);
            } catch (Exception e) {
                if (debugModeEnabled) {
                    getLogger().log(Level.WARNING, "DEBUG: Could not read statistic " + definition.key() + " for player " + player.getUniqueId(), e);
                }
                continue;
            }

            if (value > highestValue) {
                highestValue = value;
                topPlayer = player;
            }
        }

        if (topPlayer == null || highestValue <= 0) {
            return null;
        }

        String playerName = topPlayer.getName();

        if (playerName == null || playerName.isBlank()) {
            playerName = topPlayer.getUniqueId().toString();
        }

        return new LeaderboardEntry(playerName, highestValue);
    }

    private int readStatistic(OfflinePlayer player, StatDefinition definition) {
        Statistic statistic = definition.statistic();

        return switch (statistic.getType()) {
            case UNTYPED -> player.getStatistic(statistic);
            case BLOCK, ITEM -> {
                Material material = definition.material();
                if (material == null) {
                    yield 0;
                }
                yield player.getStatistic(statistic, material);
            }
            case ENTITY -> {
                EntityType entityType = definition.entityType();
                if (entityType == null) {
                    yield 0;
                }
                yield player.getStatistic(statistic, entityType);
            }
        };
    }

    private StatDefinition findStatistic(String key) {
        if (key == null) {
            return null;
        }

        for (StatDefinition statistic : statistics) {
            if (statistic.key().equalsIgnoreCase(key)) {
                return statistic;
            }
        }

        return null;
    }

    private void processAnnouncement(StatDefinition definition, LeaderboardEntry result) {
        String customMessage = statMessages.get(definition.key());

        if (customMessage == null || customMessage.isBlank()) {
            customMessage = defaultMessage;
        }

        String displayStat = getDisplayName(definition);
        String finalMessage = messagePrefix + " " + customMessage.replace("%stat%", displayStat)
                .replace("%topPlayer%", result.playerName())
                .replace("%number%", formatStatisticValue(definition, result.value()));

        sendToPermittedPlayers(finalMessage);
    }

    private String getDisplayName(StatDefinition definition) {
        String base = humanize(definition.statistic().getKey().getKey());

        if (definition.material() != null) {
            return base + ": " + humanize(definition.material().getKey().getKey());
        }

        if (definition.entityType() != null) {
            return base + ": " + humanize(definition.entityType().getKey().getKey());
        }

        return base;
    }

    private String formatStatisticValue(StatDefinition definition, int value) {
        String stat = definition.statistic().getKey().getKey();

        return switch (stat) {
            // Stored as ticks (20 ticks = 1 second)
            case "play_one_minute", "time_since_death", "time_since_rest" -> formatTicks(value);

            // Stored as centimetres
            case "walk_one_cm", "crouch_one_cm", "sprint_one_cm", "swim_one_cm", "fall_one_cm", "climb_one_cm", "fly_one_cm" -> formatDistance(value);

            default -> String.format("%,d", value);
        };
    }

    private String formatTicks(int ticks) {
        long seconds = ticks / 20L;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        List<String> parts = new ArrayList<>();

        if (days > 0) {
            parts.add(days + " day" + (days == 1 ? "" : "s"));
        }
        if (hours > 0) {
            parts.add(hours + " hour" + (hours == 1 ? "" : "s"));
        }
        if (minutes > 0) {
            parts.add(minutes + " minute" + (minutes == 1 ? "" : "s"));
        }
        if (seconds > 0 && parts.size() < 2) {
            parts.add(seconds + " second" + (seconds == 1 ? "" : "s"));
        }
        if (parts.isEmpty()) {
            return "0 seconds";
        }

        return String.join(", ", parts);
    }

    private String formatDistance(int centimetres) {
        double metres = centimetres / 100.0;

        if (metres >= 1000) {
            return String.format("%.2f km", metres / 1000);
        }

        return String.format("%.0f metres", metres);
    }

    private String humanize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String[] words = value.replace('-', '_').split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }

    private void sendToPermittedPlayers(String message) {
        boolean messageSent = false;

        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission(DEFAULT_STAT_PERMISSION)) {
                    continue;
                }
                player.sendMessage(colorize(message));
                messageSent = true;
            }

            if (debugModeEnabled) {
                getLogger().info("DEBUG: Stat broadcast sent: " + message);
            }

            if (!messageSent && debugModeEnabled) {
                getLogger().info("DEBUG: No online players have " + DEFAULT_STAT_PERMISSION);
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Something went wrong while sending the stat broadcast.", e);
        }
    }

    private void checkHealth(CommandSender sender) {
        List<String> errors = new ArrayList<>();
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            errors.add("config.yml not found!");
        }

        File messagesFile = new File(getDataFolder(), "messages.json");

        if (!messagesFile.exists()) {
            errors.add("messages.json not found!");
        }

        if (!isValidConfig()) {
            errors.add("config.yml is invalid!");
        }

        if (!isValidMessages()) {
            errors.add("messages.json is invalid!");
        }

        if (statistics.isEmpty()) {
            errors.add("No Paper statistics were discovered!");
        }

        if (errors.isEmpty()) {
            sender.sendMessage(colorize(messagePrefix + " " + "&aHealth check: Plugin is healthy!"));
            getLogger().info("Healthcheck run successfully: Plugin is healthy!");
            return;
        }

        for (String error : errors) {
            sender.sendMessage(colorize(messagePrefix + " " + "&cHealth check: " + error));
            getLogger().warning("Health check: " + error);
        }
    }

    private boolean isValidConfig() {
        try {
            getConfig().load(new InputStreamReader(new FileInputStream(new File(getDataFolder(), "config.yml")), StandardCharsets.UTF_8));
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "Error loading config.yml.", e);
            return false;
        }
    }

    private boolean isValidMessages() {
        File file = new File(getDataFolder(), "messages.json");

        if (!file.exists()) {
            return false;
        }

        try {
            new JSONObject(Files.readString(file.toPath(), StandardCharsets.UTF_8));
            return true;
        } catch (IOException | JSONException e) {
            getLogger().log(Level.SEVERE, "Error loading messages.json.", e);
            return false;
        }
    }

    private String colorize(String message) {
        if (message == null) {
            return "";
        }
        return message.replace("&", "\u00A7");
    }

    private record StatDefinition(Statistic statistic, Material material, EntityType entityType, String key) {}

    private record LeaderboardEntry(String playerName, int value) {}
}