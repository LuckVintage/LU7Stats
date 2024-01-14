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
import java.nio.charset.StandardCharsets;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class StatAnnouncements extends JavaPlugin {

	private FileConfiguration config;
	private Map<String, String> statMessages;
	private String messagePrefix;
	private String lastBroadcastedStat;
	private int randomStatInterval; // Variable to store the interval
	// Check if debug mode is enabled in the config
	boolean debugModeEnabled;

	// Helper method to load the config
	private void loadConfig() {
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Attempting to load configuration...");
		}
		saveDefaultConfig();
		config = getConfig();
		messagePrefix = config.getString("messagePrefix", "&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l]"); // Message prefix
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Loaded messagePrefix: " + messagePrefix);
		}
		randomStatInterval = config.getInt("randomStatInterval", 15); // Load the interval
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
			"mine_block:spruce_log", "mine_block:birch_log", "mine_block:jungle_log", "mine_block:dark_oak_log",
			"mine_block:mangrove_log", "mine_block:cherry_blossom_log", "mine_block:warped_log",
			"mine_block:deepslate_diamond_ore", "mine_block:deepslate", "mine_block:grass_block", "mine_block:wheat",
			"mine_block:sugar_cane", "mine_block:potatoes", "mine_block:acacia_log", "mine_block:beetroots",
			"mine_block:bamboo", "mine_block:sand", "mine_block:netherrack", "mine_block:deepslate_iron_ore",
			"mine_block:deepslate_redstone_ore", "mine_block:deepslate_lapis_ore", "mine_block:deepslate_coal_ore",
			"mine_block:deepslate_gold_ore", "mine_block:deepslate_copper_ore", "mine_block:deepslate_iron_ore",
			"kill_entity:zombie", "kill_entity:spider", "kill_entity:cave_spider", "kill_entity:creeper",
			"kill_entity:cow", "kill_entity:sheep", "kill_entity:enderman", "kill_entity:guardian",
			"kill_entity:iron_golem", "kill_entity:magma_cube", "kill_entity:phantom", "kill_entity:villager",
			"kill_entity:vex", "kill_entity:strider", "kill_entity:squid", "kill_entity:silverfish",
			"kill_entity:piglin_brute", "kill_entity:piglin", "kill_entity:blaze", "kill_entity:chicken",
			"kill_entity:pig", "kill_entity:slime", "kill_entity:witch", "mine_block:deepslate_emerald_ore",
			"kill_entity:skeleton", "kill_entity:ghast", "mine_block:clay", "break_item:stone_sword",
			"break_item:stone_axe", "break_item:stone_pickaxe", "break_item:stone_hoe", "break_item:stone_shovel",
			"break_item:wooden_axe", "break_item:wooden_sword", "break_item:wooden_pickaxe", "break_item:wooden_hoe",
			"break_item:wooden_shovel", "break_item:iron_axe", "break_item:iron_sword", "break_item:iron_pickaxe",
			"break_item:iron_hoe", "break_item:iron_shovel", "break_item:diamond_axe", "break_item:diamond_sword",
			"break_item:diamond_pickaxe", "break_item:diamond_hoe", "break_item:diamond_shovel",
			"break_item:netherite_axe", "break_item:netherite_sword", "break_item:netherite_pickaxe",
			"break_item:netherite_hoe", "break_item:netherite_shovel", "break_item:trident", "break_item:bow",
			"break_item:crossbow", "break_item:elytra", "break_item:fishing_rod", "break_item:elytra",
			"break_item:flint_and_steel", "break_item:shield", "mine_block:ancient_debris" };

	@Override
	public void onEnable() {

		loadConfig(); // Load the config

		debugModeEnabled = config.getBoolean("enableDebug", false);

		if (debugModeEnabled) {
			getLogger().log(Level.INFO,
					"DEBUG: Debug mode is enabled, LU7 Stats will log extra messages to the console. This should be disabled when in production.");
		}

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
		try {
			getCommand("broadcaststat").setExecutor(this);
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Loaded command 'broadcaststat' successfully!");
			}
		} catch (Exception e) {
			// Log an error message if an exception occurs
			getLogger().log(Level.SEVERE, "Error loading command: broadcaststat", e);
		}

		// Register the command for reloading config and messages
		try {
			getCommand("lu7statsreload").setExecutor(this);
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Loaded command 'statsreload' successfully!");
			}
		} catch (Exception e) {
			// Log an error message if an exception occurs
			getLogger().log(Level.SEVERE, "Error loading command: statsreload", e);
		}

		// Register the health check command
		try {
			getCommand("lu7statshealth").setExecutor(this);
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Loaded command 'statshealth' successfully!");
			}
		} catch (Exception e) {
			// Log an error message if an exception occurs
			getLogger().log(Level.SEVERE, "Error loading command: statshealth", e);
		}

		// Log successful enable
		getLogger().log(Level.INFO, "LU7 Stats plugin has been enabled!");

		// Check if bStats is enabled in the config
		if (config.getBoolean("enablebStats", true)) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Initializing bStats...");
			}
			// If bStats is enabled in config, attempt to initialise it
			try {
				int pluginId = 20633;
				Metrics metrics = new Metrics(this, pluginId);
				getLogger().log(Level.INFO,
						"DEBUG: bStats metrics has been enabled. To opt-out, change 'enablebStats' to false in config.yml.");
			} catch (Exception e) {
				// Log an error message if an exception occurs during bStats initialization
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

		// Schedule a delayed task to perform dependency checks after a 30-second delay
		getServer().getScheduler().runTaskLater(this, () -> {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Running dependency checks...");
			}
			checkDependencies(); // Run checkDependencies method
		}, 600L); // 600L represents a delay of 600 ticks (30 seconds * 20 ticks/second)

	}

	private void scheduleStatBroadcast() {
		// Schedule the task to run based on the configured interval
		int intervalMinutes = config.getInt("randomStatInterval", 15);

		new BukkitRunnable() {
			@Override
			public void run() {
				sendRandomAnnouncement();
			}
		}.runTaskTimer(this, 0, 20 * 60 * intervalMinutes); // 20 ticks per second, 60 seconds per minute

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
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: 'statsreload' command triggered by " + sender);
			}
			if (args.length == 0) {
				reloadFiles(sender);
			} else {
				sender.sendMessage(colorize("&cUsage: /lu7statsreload"));
			}
			return true;
		} else if (label.equals("lu7statshealth")) { // Adding the new healthcheck command
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

	// Command to reload files
	private void reloadFiles(CommandSender sender) {
		// Reload config.yml
		reloadConfig();
		debugModeEnabled = config.getBoolean("enableDebug", false);
		config = getConfig();
		messagePrefix = config.getString("messagePrefix", "&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l]"); // Reload message
																									// prefix
		randomStatInterval = config.getInt("randomStatInterval", 15); // Update the stored interval
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

	private void checkHealth(CommandSender sender) {
		List<String> errorMessages = new ArrayList<>();

		// Check if PlaceholderAPI is present and enabled
		if (!isPluginEnabled("PlaceholderAPI")) {
			errorMessages.add("PlaceholderAPI is not installed or enabled!");
			getLogger().log(Level.SEVERE,
					"PlaceholderAPI is not installed or enabled! LU7 Stats will NOT function as expected.");
		} else {
			// Check if the PlayerStats PlaceholderAPI expansion is registered
			if (!isPlaceholderAPIExpansionInstalled("PlayerStats")) {
				errorMessages.add("PlayerStats PlaceholderAPI expansion is not installed or enabled!");
				getLogger().log(Level.SEVERE,
						"PlayerStats PlaceholderAPI expansion is not installed or enabled! LU7 Stats will NOT function as expected.");
			}
		}

		// Check if PlayerStats is present and enabled
		if (!isPluginEnabled("PlayerStats")) {
			errorMessages.add("PlayerStats is not installed or enabled!");
			getLogger().log(Level.SEVERE,
					"PlayerStats is not installed or enabled! LU7 Stats will NOT function as expected.");
		}

		// Check if config.yml exists
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			errorMessages.add("config.yml not found!");
			getLogger().log(Level.SEVERE, "config.yml not found! LU7 Stats will NOT function as expected.");
		}

		// Check if messages.json exists
		File messagesFile = new File(getDataFolder(), "messages.json");
		if (!messagesFile.exists()) {
			getLogger().log(Level.WARNING, "messages.json not found!");
		}

		// Validate the content of config.yml
		if (!isValidConfig()) {
			errorMessages.add("config.yml is invalid!");
			getLogger().log(Level.SEVERE, "config.yml is invalid! LU7 Stats will NOT function as expected.");
		}

		// Validate the content of messages.json
		if (!isValidMessages()) {
			errorMessages.add("messages.json is invalid!");
			getLogger().log(Level.WARNING, "messages.json is invalid!");
		}

		// Display all error messages, if any
		for (String errorMessage : errorMessages) {
			sender.sendMessage(colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aHealth check: &c" + errorMessage));
		}

		// If there are no errors, send success message
		if (errorMessages.isEmpty()) {
			sender.sendMessage(colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aHealth check: Plugin is healthy!"));
			getLogger().log(Level.INFO, "Healthcheck run successfully: Plugin is healthy!");
		}
	}

	private boolean isPlaceholderAPIExpansionInstalled(String expansionName) {
		// Check if PlaceholderAPI is present and enabled
		if (isPluginEnabled("PlaceholderAPI")) {
			// Check if the PlaceholderAPI expansion is registered
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
		// Check if PlaceholderAPI is present and enabled
		boolean placeholderAPIEnabled = isPluginEnabled("PlaceholderAPI");
		if (config.getBoolean("enableDebug", false)) {
			getLogger().log(Level.INFO, "DEBUG: PlaceholderAPI Enabled: " + placeholderAPIEnabled);
		}

		// Check if the PlayerStats PlaceholderAPI expansion is registered
		boolean playerStatsEnabled = isPlaceholderAPIExpansionInstalled("PlayerStats");
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: PlayerStats Expansion Enabled: " + playerStatsEnabled);
		}

		// Check if PlaceholderAPI is present and enabled
		if (!placeholderAPIEnabled) {
			getLogger().log(Level.SEVERE,
					"PlaceholderAPI is not installed or enabled! LU7 Stats will NOT function as expected.");
			return; // Early return if PlaceholderAPI is not present
		} else {
			// Check if the PlayerStats PlaceholderAPI expansion is registered
			if (!isPlaceholderAPIExpansionInstalled("PlayerStats")) {
				getLogger().log(Level.SEVERE,
						"PlayerStats PlaceholderAPI expansion is not installed or enabled! LU7 Stats will NOT function as expected.");
			}
		}

		// Check if PlayerStats is present and enabled
		boolean playerStatsPluginEnabled = isPluginEnabled("PlayerStats");
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: PlayerStats Plugin Enabled: " + playerStatsPluginEnabled);
		}
		if (!isPluginEnabled("PlayerStats")) {
			getLogger().log(Level.SEVERE,
					"PlayerStats is not installed or enabled! LU7 Stats will NOT function as expected.");
		}

		// Run scheduleStatBroadcast only if all plugins are enabled
		if (placeholderAPIEnabled && playerStatsEnabled) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Dependencies are satisfied. Scheduling stat broadcasts...");
			}
			scheduleStatBroadcast();
		} else {
			// Log the severe missing dependencies error
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
			// Add more specific validation if needed
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
		boolean validStat = false;
		for (String stat : statistics) {
			if (stat.equalsIgnoreCase(specifiedStat)) {
				validStat = true;
				break;
			}
		}

		if (validStat) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Selected statistic: " + specifiedStat);
			}
			// Inform the command sender first
			sender.sendMessage(colorize(
					"&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aBroadcast triggered manually for stat: &e" + specifiedStat));

			// Use BukkitScheduler to introduce a delay and run tasks asynchronously
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: Attempting to request stat values from PlaceHolderAPI...");
			}
			getServer().getScheduler().runTaskAsynchronously(this, () -> {
				// Fetch the first set of results
				String topPlayer1 = getTopPlayer(specifiedStat);
				String number1 = getNumber(specifiedStat);

				// Introduce a delay (adjust ticks as needed, 20 ticks = 1 second)
				getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
					// Fetch the second set of results
					String topPlayer2 = getTopPlayer(specifiedStat);
					String number2 = getNumber(specifiedStat);
					if (debugModeEnabled) {
						getLogger().log(Level.INFO, "DEBUG: Retrieved topPlayer successfully: " + topPlayer2);
						getLogger().log(Level.INFO, "DEBUG: Retrieved number successfully: " + number2);
					}

					// Process and send the announcement with the obtained values
					processAnnouncement(specifiedStat, topPlayer2, number2);
				}, 20L); // 20 ticks delay
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

		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Attempting to process random stat broadcast");
		}

		// Check if there are players online
		if (Bukkit.getOnlinePlayers().isEmpty()) {
			if (debugModeEnabled) {
				getLogger().log(Level.INFO, "DEBUG: There are no players online, skipping stat broadcast.");
			}
			return;
		}

		// Randomly pick one statistic to announce
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Selecting random statistic from array...");
		}
		Random random = new Random();
		String randomStat = statistics[random.nextInt(statistics.length)];
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Selected random statistic: " + randomStat);
		}

		// Use BukkitScheduler to introduce a delay and run tasks asynchronously
		if (debugModeEnabled) {
			getLogger().log(Level.INFO, "DEBUG: Attempting to request stat values from PlaceHolderAPI...");
		}
		getServer().getScheduler().runTaskAsynchronously(this, () -> {
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
				// Introduce a delay before sending the message
				getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
					String topPlayer = topPlayerFuture2.join();
					String number = numberFuture2.join();
					if (debugModeEnabled) {
						getLogger().log(Level.INFO, "DEBUG: Retrieved topPlayer successfully: " + topPlayer);
						getLogger().log(Level.INFO, "DEBUG: Retrieved number successfully: " + number);
					}

					// Check if the number value is '0' and pick another random stat if true
					if ("0".equals(number)) {
						if (debugModeEnabled) {
							getLogger().log(Level.INFO, "DEBUG: Chosen stat has no top player, picking another...");
						}
						sendRandomAnnouncement();
						return;
					}

					// Check if the last broadcasted stat is the same as the current random stat
					if (lastBroadcastedStat != null && lastBroadcastedStat.equals(randomStat)) {
						if (debugModeEnabled) {
							getLogger().log(Level.INFO,
									"DEBUG: Chosen stat is the same as the last broadcasted stat, picking another...");
						}
						sendRandomAnnouncement();
						return;
					}

					// Process and send the announcement with the obtained values
					processAnnouncement(randomStat, topPlayer, number);
					// Save the last broadcasted stat
					lastBroadcastedStat = randomStat;
				}, 20L); // 20 ticks delay (adjust as needed)
			});
		});
	}

	private void processAnnouncement(String randomStat, String topPlayer, String number) {
		// Get the custom message for the statistic
		String customMessage = statMessages.getOrDefault(randomStat,
				"&aThe top player for %stat% is: &c%topPlayer% with %number%");

		// Add the message prefix to the beginning of the message
		String prefixedMessage = messagePrefix + " " + customMessage.replace("%stat%", randomStat.replace(":", " "))
				.replace("%topPlayer%", topPlayer).replace("%number%", number);

		// Variable to track if the message has been sent successfully to at least one
		// player
		boolean messageSent = false;

		// Broadcast the coloured announcement to all online players with the permission
		try {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.hasPermission("lu7stats.seebroadcasts")) {
					player.sendMessage(colorize(prefixedMessage));
					if (!messageSent && player.isOnline()) {
						// Log the debug message if broadcast was sent successfully
						messageSent = true;
						if (debugModeEnabled) {
							getLogger().log(Level.INFO,
									"DEBUG: Stat broadcast sent successfully: Message: " + prefixedMessage);
						}
					}
				}
			}
		} catch (Exception e) {
			// If an exception occurs, log the error message in the debug log
			messageSent = false; // Set messageSent to false in case of an error
			getLogger().log(Level.SEVERE,
					"Something went wrong while attempting to send the stat broadcast: " + e.getMessage());
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