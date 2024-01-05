package com.lu7stats.lu7stats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONObject;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.io.IOException;
import java.io.File;

public class StatAnnouncements extends JavaPlugin {

	private FileConfiguration config;
	private Map<String, String> statMessages;
	private String messagePrefix;
	private int randomStatInterval; // Variable to store the interval

    // Helper method to load the config
    private void loadConfig() {
        saveDefaultConfig();
        config = getConfig();
        messagePrefix = config.getString("messagePrefix", "&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l]"); // Message prefix
        randomStatInterval = config.getInt("randomStatinterval", 15); // Load the interval
    }

	private void loadCustomMessages() {
		try {
			// Load messages from messages.json
			byte[] jsonData = Files.readAllBytes(getDataFolder().toPath().resolve("messages.json"));
			String messagesJson = new String(jsonData);
			JSONObject messages = new JSONObject(messagesJson);

			// Initialize the statMessages map
			statMessages = new HashMap<>();

			// Populate the statMessages map with custom messages
			for (String stat : statistics) {
				if (messages.has(stat)) {
					statMessages.put(stat, messages.getString(stat));
				}
			}
			getLogger().log(Level.INFO, "Custom messages loaded successfully!");
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Error reading messages.json", e);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Error processing messages.json", e);
		}
	}

	private String[] statistics = { "break_item:stone_pickaxe", "mine_block:stone", "animals_bred", "bell_ring",
			"boat_one_cm", "cake_slices_eaten", "cauldron_filled", "chest_opened", "climb_one_cm", "crouch_one_cm",
			"damage_dealt", "damage_taken", "damage_blocked_by_shield", "deaths", "fall_one_cm", "flower_potted",
			"fish_caught", "horse_one_cm", "item_enchanted", "jump", "leave_game", "minecart_one_cm", "mob_kills",
			"noteblock_played", "noteblock_tuned", "open_barrel", "pig_one_cm", "play_one_minute", "player_kills",
			"sleep_in_bed", "sneak_time", "sprint_one_cm", "swim_one_cm", "talked_to_villager", "traded_with_villager",
			"walk_on_water_one_cm", "walk_one_cm", "walk_under_water_one_cm", "mine_block:oak_log",
			"mine_block:spruce_log", "mine_block:birch_log", "mine_block:deepslate_diamond_ore", "mine_block:deepslate",
			"mine_block:grass_block", "mine_block:wheat", "mine_block:sugar_cane", "mine_block:potatoes",
			"mine_block:acacia_log", "mine_block:beetroots", "mine_block:bamboo", "mine_block:sand",
			"mine_block:netherrack", "mine_block:deepslate_iron_ore", "mine_block:deepslate_redstone_ore",
			"mine_block:deepslate_lapis_ore", "mine_block:deepslate_coal_ore", "mine_block:deepslate_gold_ore",
			"mine_block:deepslate_copper_ore", "mine_block:deepslate_iron_ore", "kill_entity:zombie",
			"kill_entity:spider", "kill_entity:cave_spider", "kill_entity:creeper", "kill_entity:cow",
			"kill_entity:sheep", "kill_entity:enderman", "kill_entity:guardian", "kill_entity:iron_golem",
			"kill_entity:magma_cube", "kill_entity:phantom", "kill_entity:villager", "kill_entity:vex",
			"kill_entity:strider", "kill_entity:squid", "kill_entity:silverfish", "kill_entity:piglin_brute",
			"kill_entity:piglin", "kill_entity:blaze", "kill_entity:chicken", "kill_entity:pig", "kill_entity:slime",
			"kill_entity:witch", "mine_block:deepslate_emerald_ore", "kill_entity:skeleton", "kill_entity:ghast",
			"mine_block:clay" };

	@Override
	public void onEnable() {
	    loadConfig(); // Load the config

	    // Check if messages.json already exists before saving
	    File messagesFile = new File(getDataFolder(), "messages.json");
	    if (!messagesFile.exists()) {
	        getLogger().log(Level.INFO, "Messages.json not found, generating default file...");
	        saveResource("messages.json", false);
	        getLogger().log(Level.INFO, "Default messages.json created successfully!");
	    }

	    // Load custom messages
	    loadCustomMessages();

	    // Register the manual broadcast command
	    getCommand("broadcaststat").setExecutor(this);

	    // Register the command for reloading config and messages
	    getCommand("lu7statsreload").setExecutor(this);

	    // Schedule the initial task
	    scheduleRandomAnnouncementTask();

	    // Log successful enable
	    getLogger().log(Level.INFO, "LU7 Stats plugin has been enabled!");

	    // Start bStats
	    if (config.getBoolean("enablebStats", true)) {
	        int pluginId = 20633;
	        Metrics metrics = new Metrics(this, pluginId);
	        getLogger().log(Level.INFO,
	                "bStats metrics has been enabled. To opt-out, change 'enablebStats' to false in config.yml.");
	    }
	}

	
	private int announcementTaskId = -1;

	private void scheduleRandomAnnouncementTask() {
	    // Cancel the existing task if it exists
	    if (announcementTaskId != -1) {
	        Bukkit.getScheduler().cancelTask(announcementTaskId);
	    }

	    // Schedule a new task
	    announcementTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
	        // Use the class field for interval directly
	        sendRandomAnnouncement();
	    }, 0, 20 * 60 * randomStatInterval); // 20 ticks per second, 60 seconds per minute
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equals("broadcaststat")) {
			if (args.length == 0) {
				// Manually trigger the broadcast with a random stat
				sendRandomAnnouncement();
				sender.sendMessage(colorize(
						"&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aBroadcast triggered manually with a random stat!"));
			} else if (args.length == 1) {
				// Manually trigger the broadcast with the specified stat
				sendManualAnnouncement(args[0], sender);
			} else {
				sender.sendMessage(colorize("&cUsage: /broadcaststat [stat]"));
			}
			return true;
		} else if (label.equals("lu7statsreload")) {
			if (args.length == 0) {
				reloadFiles(sender);
			} else {
				sender.sendMessage(colorize("&cUsage: /lu7statsreload"));
			}
			return true;
		}
		return false;
	}

    // Command to reload files
    private void reloadFiles(CommandSender sender) {
        // Reload config.yml
        reloadConfig();
        config = getConfig();
        messagePrefix = config.getString("messagePrefix", "&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l]"); // Reload message prefix
        randomStatInterval = config.getInt("randomStatinterval", 15); // Update the stored interval
        sender.sendMessage(colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aConfig.yml reloaded successfully!"));

        // Reload messages.json
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
	private void sendManualAnnouncement(String specifiedStat, CommandSender sender) {
		// Check if the specified stat is valid
		boolean validStat = false;
		for (String stat : statistics) {
			if (stat.equalsIgnoreCase(specifiedStat)) {
				validStat = true;
				break;
			}
		}

		if (validStat) {
			// Use CompletableFuture for asynchronous processing
			CompletableFuture<String> topPlayerFuture1 = CompletableFuture
					.supplyAsync(() -> getTopPlayer(specifiedStat));
			CompletableFuture<String> numberFuture1 = CompletableFuture.supplyAsync(() -> getNumber(specifiedStat));

			// Introduce a small delay (adjust the time as needed)
			try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			CompletableFuture<String> topPlayerFuture2 = CompletableFuture
					.supplyAsync(() -> getTopPlayer(specifiedStat));
			CompletableFuture<String> numberFuture2 = CompletableFuture.supplyAsync(() -> getNumber(specifiedStat));

			CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(topPlayerFuture1, numberFuture1,
					topPlayerFuture2, numberFuture2);

			// Wait for both CompletableFuture to complete
			combinedFuture.thenAcceptAsync(ignored -> {
				// Inform the command sender first
				sender.sendMessage(
						colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aBroadcast triggered manually for stat: &e"
								+ specifiedStat));

				// Use the second set of results
				String topPlayer = topPlayerFuture2.join();
				String number = numberFuture2.join();
				processAnnouncement(specifiedStat, topPlayer, number);
			});
		} else {
			// If the specified stat is not found, send an error message
			sender.sendMessage(
					colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &cInvalid stat specified: " + specifiedStat));
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completions = new ArrayList<>();

		if (args.length == 1) {
			// Tab completion for the first argument (stat)
			for (String stat : statistics) {
				if (stat.toLowerCase().startsWith(args[0].toLowerCase())) {
					completions.add(stat);
				}
			}
		}

		return completions;
	}

	private void sendRandomAnnouncement() {
		// Check if there are players online
		if (Bukkit.getOnlinePlayers().isEmpty()) {
			return;
		}

		// Randomly pick one statistic to announce
		Random random = new Random();
		String randomStat = statistics[random.nextInt(statistics.length)];

		// Use CompletableFuture for asynchronous processing
		CompletableFuture<String> topPlayerFuture1 = CompletableFuture.supplyAsync(() -> getTopPlayer(randomStat));
		CompletableFuture<String> numberFuture1 = CompletableFuture.supplyAsync(() -> getNumber(randomStat));

		CompletableFuture<Void> combinedFuture1 = CompletableFuture.allOf(topPlayerFuture1, numberFuture1);

		// Introduce a small delay (adjust the time as needed)
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		CompletableFuture<String> topPlayerFuture2 = CompletableFuture.supplyAsync(() -> getTopPlayer(randomStat));
		CompletableFuture<String> numberFuture2 = CompletableFuture.supplyAsync(() -> getNumber(randomStat));

		CompletableFuture<Void> combinedFuture2 = CompletableFuture.allOf(topPlayerFuture2, numberFuture2);

		// Wait for both CompletableFuture to complete
		combinedFuture1.thenAcceptAsync(ignored -> {
			String topPlayer = topPlayerFuture1.join();
			String number = numberFuture1.join();
			// Do not send the message here
		});

		combinedFuture2.thenAcceptAsync(ignored -> {
			String topPlayer = topPlayerFuture2.join();
			String number = numberFuture2.join();
			processAnnouncement(randomStat, topPlayer, number);
		});
	}

    private void processAnnouncement(String randomStat, String topPlayer, String number) {
        // Get the custom message for the statistic
        String customMessage = statMessages.getOrDefault(randomStat, "&aThe top player for %stat% is: &c%topPlayer% with %number%");
        
        // Add the message prefix to the beginning of the message
        String prefixedMessage = messagePrefix + " " + customMessage.replace("%stat%", randomStat.replace(":", " "))
                .replace("%topPlayer%", topPlayer).replace("%number%", number);

        // Broadcast the coloured announcement to all online players with the permission
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("lu7stats.seebroadcasts")) {
                player.sendMessage(colorize(prefixedMessage));
            }
        }
    }

	private String getNumber(String stat) {
		// Get the raw number from PlaceholderAPI (no parsing needed)
		return PlaceholderAPI.setPlaceholders(null, "%playerstats_top:1," + stat + ",only:number%");
	}

	private String getTopPlayer(String stat) {
		// Use PlaceholderAPI to get the top player for the specified statistic and
		// sub-statistic
		String placeholder = "%playerstats_top:1," + stat + ",only:player_name%";
		return PlaceholderAPI.setPlaceholders(null, placeholder);
	}

	private String colorize(String message) {
		// Use Minecraft colour codes to add colour to the message
		return message.replace("&", "\u00A7");
	}

	@Override
	public void onDisable() {
		statMessages.clear();
		getLogger().log(Level.INFO, "LU7 Stats plugin has been disabled!");
	}
}