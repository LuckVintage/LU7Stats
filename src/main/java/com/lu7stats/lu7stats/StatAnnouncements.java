package com.lu7stats.lu7stats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.json.JSONObject;
import org.json.JSONException;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class StatAnnouncements extends JavaPlugin {

	private FileConfiguration config;
	private Map<String, String> statMessages;
	private String messagePrefix;
	private String lastBroadcastedStat;
	private int randomStatInterval;
	boolean debugModeEnabled;
	private List<String> statistics;

	private void loadConfig() {
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Attempting to load configuration...");
		}
		saveDefaultConfig();
		config = getConfig();
		messagePrefix = config.getString("messagePrefix", "&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l]");
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Loaded messagePrefix: " + messagePrefix);
		}
		randomStatInterval = config.getInt("randomStatInterval", 15);
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Loaded randomStatInterval: " + randomStatInterval);
		}
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Finished loading configuration");
		}
	}

	private void loadCustomMessages() {
		try {
			// Load messages from messages.json
			byte[] jsonData = Files.readAllBytes(getDataFolder().toPath().resolve("messages.json"));
			String messagesJson = new String(jsonData, StandardCharsets.UTF_8);
			JSONObject messages = new JSONObject(messagesJson);

			// Initialise the statMessages map and the statistics list
			statMessages = new ConcurrentHashMap<>();
			statistics = new ArrayList<>();

			// Populate the statMessages map and the statistics list with custom messages
			for (String key : messages.keySet()) {
				statMessages.put(key, messages.getString(key));
				statistics.add(key);
			}

			getLogger().log(Level.INFO,
					"Custom messages loaded successfully! Loaded " + statistics.size() + " statistics.");
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Error reading messages.json", e);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Error processing messages.json", e);
		}
	}

	@Override
	public void onEnable() {
		loadConfig();

		debugModeEnabled = config.getBoolean("enableDebug", false);

		if (debugModeEnabled) {
			getLogger().log(Level.INFO,
					"DEBUG: Debug mode is enabled, LU7 Stats will log extra messages to the console. This should be disabled when in production.");
		}

		File messagesFile = new File(getDataFolder(), "messages.json");
		if (!messagesFile.exists()) {
			getLogger().log(Level.INFO, "Messages.json not found, generating default file...");
			saveResource("messages.json", false);
			getLogger().log(Level.INFO, "Default messages.json created successfully!");
		}

		loadCustomMessages();

		try {
			getCommand("broadcaststat").setExecutor(this);
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Loaded command 'broadcaststat' successfully!");
			}
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Error loading command: broadcaststat", e);
		}

		try {
			getCommand("lu7statsreload").setExecutor(this);
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Loaded command 'statsreload' successfully!");
			}
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Error loading command: statsreload", e);
		}

		try {
			getCommand("lu7statshealth").setExecutor(this);
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Loaded command 'statshealth' successfully!");
			}
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Error loading command: statshealth", e);
		}

		getLogger().log(Level.INFO, "LU7 Stats plugin has been enabled!");

		if (config.getBoolean("enablebStats", true)) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Initializing bStats...");
			}
			try {
				int pluginId = 20633;
				Metrics metrics = new Metrics(this, pluginId);
				getLogger().log(Level.INFO,
						"bStats metrics has been enabled. To opt-out, change 'enablebStats' to false in config.yml.");
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, "Error initializing bStats", e);
				if (debugModeEnabled) {
					getLogger().log(Level.INFO, "DEBUG: Skipping bStats initialization due to an error.");
				}
			}
		} else {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Skipping bStats initialization as per config.");
			}
		}

		getServer().getScheduler().runTaskLater(this, () -> {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Running dependency checks...");
			}
			checkDependencies();
		}, 600L);
	}

	private void scheduleStatBroadcast() {
		int intervalMinutes = config.getInt("randomStatInterval", 15);
		new BukkitRunnable() {
			@Override
			public void run() {
				sendRandomAnnouncement();
			}
		}.runTaskTimer(this, 0, 20 * 60 * intervalMinutes);
		getLogger().log(Level.INFO,
				"Stat broadcast task scheduled with an interval of " + intervalMinutes + " minutes.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equals("broadcaststat")) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: 'broadcaststat' command triggered by " + sender);
			}
			if (args.length == 0) {
				sendRandomAnnouncement();
				sender.sendMessage(colorize(
						"&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aBroadcast triggered manually with a random stat!"));
			} else if (args.length == 1) {
				sendManualAnnouncement(args[0], sender);
			} else {
				sender.sendMessage(colorize("&cUsage: /broadcaststat [stat]"));
			}
			return true;
		} else if (label.equals("lu7statsreload")) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: 'statsreload' command triggered by " + sender);
			}
			if (args.length == 0) {
				reloadFiles(sender);
			} else {
				sender.sendMessage(colorize("&cUsage: /lu7statsreload"));
			}
			return true;
		} else if (label.equals("lu7statshealth")) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: 'statshealth' command triggered by " + sender);
			}
			if (args.length == 0) {
				checkHealth(sender);
			} else {
				sender.sendMessage(colorize("&cUsage: /lu7statshealth"));
			}
			return true;
		}
		return false;
	}

	private void reloadFiles(CommandSender sender) {
		reloadConfig();
		debugModeEnabled = config.getBoolean("enableDebug", false);
		config = getConfig();
		messagePrefix = config.getString("messagePrefix", "&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l]");
		randomStatInterval = config.getInt("randomStatInterval", 15);
		sender.sendMessage(colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aConfig.yml reloaded successfully!"));
		try {
			statMessages.clear();
			loadCustomMessages();
			sender.sendMessage(colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aMessages.json reloaded successfully!"));
		} catch (Exception e) {
			sender.sendMessage(colorize(
					"&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &cError reloading messages.json. Check console for details."));
			getLogger().log(Level.SEVERE, "Error reloading messages.json", e);
		}
	}

	private void checkHealth(CommandSender sender) {
		List<String> errorMessages = new ArrayList<>();

		if (!isPluginEnabled("PlaceholderAPI")) {
			errorMessages.add("PlaceholderAPI is not installed or enabled!");
			getLogger().log(Level.SEVERE,
					"PlaceholderAPI is not installed or enabled! LU7 Stats will NOT function as expected.");
		} else {
			if (!isPlaceholderAPIExpansionInstalled("PlayerStats")) {
				errorMessages.add("PlayerStats PlaceholderAPI expansion is not installed or enabled!");
				getLogger().log(Level.SEVERE,
						"PlayerStats PlaceholderAPI expansion is not installed or enabled! LU7 Stats will NOT function as expected.");
			}
		}

		if (!isPluginEnabled("PlayerStats")) {
			errorMessages.add("PlayerStats is not installed or enabled!");
			getLogger().log(Level.SEVERE,
					"PlayerStats is not installed or enabled! LU7 Stats will NOT function as expected.");
		}

		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			errorMessages.add("config.yml not found!");
			getLogger().log(Level.SEVERE, "config.yml not found! LU7 Stats will NOT function as expected.");
		}

		File messagesFile = new File(getDataFolder(), "messages.json");
		if (!messagesFile.exists()) {
			getLogger().log(Level.WARNING, "messages.json not found!");
		}

		if (!isValidConfig()) {
			errorMessages.add("config.yml is invalid!");
			getLogger().log(Level.SEVERE, "config.yml is invalid! LU7 Stats will NOT function as expected.");
		}

		if (!isValidMessages()) {
			errorMessages.add("messages.json is invalid!");
			getLogger().log(Level.WARNING, "messages.json is invalid!");
		}

		for (String errorMessage : errorMessages) {
			sender.sendMessage(colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aHealth check: &c" + errorMessage));
		}

		if (errorMessages.isEmpty()) {
			sender.sendMessage(colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aHealth check: Plugin is healthy!"));
			getLogger().log(Level.INFO, "Healthcheck run successfully: Plugin is healthy!");
		}
	}

	private boolean isPlaceholderAPIExpansionInstalled(String expansionName) {
		if (isPluginEnabled("PlaceholderAPI")) {
			return PlaceholderAPI.isRegistered(expansionName);
		}
		return false;
	}

	private boolean isPluginEnabled(String pluginName) {
		return Bukkit.getPluginManager().getPlugin(pluginName) != null
				&& Bukkit.getPluginManager().isPluginEnabled(pluginName);
	}

	private void checkDependencies() {
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Checking dependencies...");
		}
		boolean placeholderAPIEnabled = isPluginEnabled("PlaceholderAPI");
		if (config.getBoolean("enableDebug", false)) {
			getLogger().log(Level.INFO, "DEBUG: PlaceholderAPI Enabled: " + placeholderAPIEnabled);
		}
		boolean playerStatsEnabled = isPlaceholderAPIExpansionInstalled("PlayerStats");
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: PlayerStats Expansion Enabled: " + playerStatsEnabled);
		}
		if (!placeholderAPIEnabled) {
			getLogger().log(Level.SEVERE,
					"PlaceholderAPI is not installed or enabled! LU7 Stats will NOT function as expected.");
			return;
		} else {
			if (!isPlaceholderAPIExpansionInstalled("PlayerStats")) {
				getLogger().log(Level.SEVERE,
						"PlayerStats PlaceholderAPI expansion is not installed or enabled! LU7 Stats will NOT function as expected.");
			}
		}

		boolean playerStatsPluginEnabled = isPluginEnabled("PlayerStats");
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: PlayerStats Plugin Enabled: " + playerStatsPluginEnabled);
		}
		if (!isPluginEnabled("PlayerStats")) {
			getLogger().log(Level.SEVERE,
					"PlayerStats is not installed or enabled! LU7 Stats will NOT function as expected.");
		}

		if (placeholderAPIEnabled && playerStatsEnabled) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Dependencies are satisfied. Scheduling stat broadcasts...");
			}
			scheduleStatBroadcast();
		} else {
			getLogger().log(Level.SEVERE, "Due to missing dependencies, automatic stat broadcasts have been disabled.");
		}
	}

	private boolean isValidConfig() {
		try {
			getConfig().load(new InputStreamReader(new FileInputStream(new File(getDataFolder(), "config.yml")),
					StandardCharsets.UTF_8));
			return true;
		} catch (IOException | InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Error loading config.yml", e);
			return false;
		}
	}

	private boolean isValidMessages() {
		try {
			JSONObject messages = new JSONObject(new String(
					Files.readAllBytes(new File(getDataFolder(), "messages.json").toPath()), StandardCharsets.UTF_8));
			return true;
		} catch (IOException | JSONException e) {
			getLogger().log(Level.SEVERE, "Error loading messages.json", e);
			return false;
		}
	}

	private void sendManualAnnouncement(String specifiedStat, CommandSender sender) {
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Attempting to process manual stat broadcast");
		}
		boolean validStat = statistics.contains(specifiedStat);

		if (validStat) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Selected statistic: " + specifiedStat);
			}
			sender.sendMessage(colorize(
					"&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aBroadcast triggered manually for stat: &e" + specifiedStat));
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Attempting to request stat values from PlaceHolderAPI...");
			}
			getServer().getScheduler().runTaskAsynchronously(this, () -> {
				String topPlayer1 = getTopPlayer1(specifiedStat);
				String number1 = getNumber1(specifiedStat);
				getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
					String topPlayer2 = getTopPlayer1(specifiedStat);
					String number2 = getNumber1(specifiedStat);
					if (debugModeEnabled) {
						getLogger().log(Level.INFO, "DEBUG: Retrieved topPlayer successfully: " + topPlayer2);
						getLogger().log(Level.INFO, "DEBUG: Retrieved number successfully: " + number2);
					}
					processAnnouncement(specifiedStat, topPlayer2, number2);
				}, 30L);
			});
		} else {
			sender.sendMessage(
					colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &cInvalid stat specified: " + specifiedStat));
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<>();
		if (args.length == 1) {
			for (String stat : statistics) {
				if (stat.toLowerCase().startsWith(args[0].toLowerCase())) {
					completions.add(stat);
				}
			}
		}
		return completions;
	}

	private void sendRandomAnnouncement() {
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Attempting to process random stat broadcast");
		}
		if (Bukkit.getOnlinePlayers().isEmpty()) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: There are no players online, skipping stat broadcast.");
			}
			return;
		}

		if (statistics.isEmpty()) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: No statistics found in messages.json, skipping broadcast.");
			}
			return;
		}

		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Selecting random statistic from list...");
		}
		Random random = new Random();
		String randomStat = statistics.get(random.nextInt(statistics.size()));

		if (lastBroadcastedStat != null && lastBroadcastedStat.equals(randomStat)) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO,
						"DEBUG: Chosen stat is the same as the last broadcasted stat, picking another...");
			}
			sendRandomAnnouncement();
			return;
		}
		getServer().getScheduler().runTaskAsynchronously(this, () -> {
			String topPlayer = getTopPlayer1(randomStat);
			String number = getNumber1(randomStat);

			if (topPlayer.contains("Processing") || number.contains("Processing")) {
				if (debugModeEnabled) {
					getLogger().log(Level.INFO, "DEBUG: PlaceholderAPI returned 'Processing'. Retrying in 1 second...");
				}
				getServer().getScheduler().runTaskLater(this, () -> sendRandomAnnouncement(), 20L);
				return;
			}

			if ("0".equals(number)) {
				if (debugModeEnabled) {
					getLogger().log(Level.INFO, "DEBUG: Chosen stat has no top player, picking another...");
				}
				sendRandomAnnouncement();
				return;
			}
			getServer().getScheduler().runTask(this, () -> {
				processAnnouncement(randomStat, topPlayer, number);
				lastBroadcastedStat = randomStat;
			});
		});
	}

	private String getNumber1(String stat) {
		String result = "Processing";
		int retries = 0;
		final int MAX_RETRIES = 5;
		while (result.contains("Processing") && retries < MAX_RETRIES) {
			result = PlaceholderAPI.setPlaceholders(null, "%playerstats_top:1," + stat + ",only:number%");
			if (result.contains("Processing")) {
				retries++;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		return result;
	}

	private String getTopPlayer1(String stat) {
		String result = "Processing";
		int retries = 0;
		final int MAX_RETRIES = 5;
		while (result.contains("Processing") && retries < MAX_RETRIES) {
			String placeholder = "%playerstats_top:1," + stat + ",only:player_name%";
			result = PlaceholderAPI.setPlaceholders(null, placeholder);
			if (result.contains("Processing")) {
				retries++;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		return result;
	}

	private void processAnnouncement(String randomStat, String topPlayer, String number) {
		String customMessage = statMessages.getOrDefault(randomStat,
				"&aThe top player for %stat% is: &c%topPlayer% with %number%");
		String prefixedMessage = messagePrefix + " " + customMessage.replace("%stat%", randomStat.replace(":", " "))
				.replace("%topPlayer%", topPlayer).replace("%number%", number);
		boolean messageSent = false;
		try {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.hasPermission("lu7stats.seebroadcasts")) {
					player.sendMessage(colorize(prefixedMessage));
					if (!messageSent && player.isOnline()) {
						messageSent = true;
						if (debugModeEnabled) {
							getLogger().log(Level.INFO,
									"DEBUG: Stat broadcast sent successfully: Message: " + prefixedMessage);
						}
					}
				}
			}
		} catch (Exception e) {
			messageSent = false;
			getLogger().log(Level.SEVERE,
					"Something went wrong while attempting to send the stat broadcast: " + e.getMessage());
		}
	}

	private String colorize(String message) {
		return message.replace("&", "\u00A7");
	}

	@Override
	public void onDisable() {
		statMessages.clear();
		getLogger().log(Level.INFO, "LU7 Stats plugin has been disabled!");
	}
}