package com.lu7stats.lu7stats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;

public class StatAnnouncements extends JavaPlugin {

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
			"kill_entity:witch", "mine_block:deepslate_emerald_ore", "kill_entity:skeleton", "kill_entity:ghast" };
  @Override
  public void onEnable() {
    // Schedule the task to run every 15 minutes
    new BukkitRunnable() {
      @Override
      public void run() {
        sendRandomAnnouncement();
      }
    }.runTaskTimer(this, 0, 20 * 60 * 15); // 20 ticks per second, 60 seconds per minute, 15 minutes

    // Register the command
    getCommand("broadcaststat").setExecutor(this);

    // Log successful enable
    getLogger().log(Level.INFO, "LU7 Stats plugin has been enabled!");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (label.equals("broadcaststat")) {
      if (args.length == 0) {
        // Manually trigger the broadcast with a random stat
        sendRandomAnnouncement();
        sender.sendMessage(colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aBroadcast triggered manually with a random stat!"));
      } else if (args.length == 1) {
        // Manually trigger the broadcast with the specified stat
        sendManualAnnouncement(args[0], sender);
      } else {
        sender.sendMessage(colorize("&cUsage: /broadcaststat [stat]"));
      }
      return true;
    }
    return false;
  }

  private void sendManualAnnouncement(String specifiedStat, CommandSender sender) {
    // Check if the specified stat is valid
    boolean validStat = false;
    for (String stat: statistics) {
      if (stat.equalsIgnoreCase(specifiedStat)) {
        validStat = true;
        break;
      }
    }

    if (validStat) {
      // Use CompletableFuture for asynchronous processing
      CompletableFuture < String > topPlayerFuture1 = CompletableFuture.supplyAsync(() -> getTopPlayer(specifiedStat));
      CompletableFuture < String > numberFuture1 = CompletableFuture.supplyAsync(() -> getNumber(specifiedStat));

      // Introduce a small delay (adjust the time as needed)
      try {
        TimeUnit.MILLISECONDS.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      CompletableFuture < String > topPlayerFuture2 = CompletableFuture.supplyAsync(() -> getTopPlayer(specifiedStat));
      CompletableFuture < String > numberFuture2 = CompletableFuture.supplyAsync(() -> getNumber(specifiedStat));

      CompletableFuture < Void > combinedFuture = CompletableFuture.allOf(topPlayerFuture1, numberFuture1, topPlayerFuture2, numberFuture2);

      // Wait for both CompletableFuture to complete
      combinedFuture.thenAcceptAsync(ignored -> {
        // Inform the command sender first
        sender.sendMessage(colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &aBroadcast triggered manually for stat: &e" + specifiedStat));

        // Use the second set of results
        String topPlayer = topPlayerFuture2.join();
        String number = numberFuture2.join();
        processAnnouncement(specifiedStat, topPlayer, number);
      });
    } else {
      // If the specified stat is not found, send an error message
      sender.sendMessage(colorize("&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] &cInvalid stat specified: " + specifiedStat));
    }
  }

  @Override
  public List < String > onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    List < String > completions = new ArrayList < > ();

    if (args.length == 1) {
      // Tab completion for the first argument (stat)
      for (String stat: statistics) {
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
    CompletableFuture < String > topPlayerFuture1 = CompletableFuture.supplyAsync(() -> getTopPlayer(randomStat));
    CompletableFuture < String > numberFuture1 = CompletableFuture.supplyAsync(() -> getNumber(randomStat));

    CompletableFuture < Void > combinedFuture1 = CompletableFuture.allOf(topPlayerFuture1, numberFuture1);

    // Introduce a small delay (adjust the time as needed)
    try {
      TimeUnit.MILLISECONDS.sleep(50);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    CompletableFuture < String > topPlayerFuture2 = CompletableFuture.supplyAsync(() -> getTopPlayer(randomStat));
    CompletableFuture < String > numberFuture2 = CompletableFuture.supplyAsync(() -> getNumber(randomStat));

    CompletableFuture < Void > combinedFuture2 = CompletableFuture.allOf(topPlayerFuture2, numberFuture2);

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
    String customMessage = getMessageForStat(randomStat);

    // Broadcast the colored announcement to all online players with the permission
    for (Player player: Bukkit.getOnlinePlayers()) {
      if (player.hasPermission("lu7stats.seebroadcasts")) {
        String message = "&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] " + customMessage
          .replace("%stat%", randomStat.replace(":", " "))
          .replace("%topPlayer%", topPlayer)
          .replace("%number%", number); // Use the retrieved number directly
        player.sendMessage(colorize(message));
      }
    }
  }

  private String getNumber(String stat) {
    // Get the raw number from PlaceholderAPI (no parsing needed)
    return PlaceholderAPI.setPlaceholders(null, "%playerstats_top:1," + stat + ",only:number%");
  }

  private String getTopPlayer(String stat) {
    // Use PlaceholderAPI to get the top player for the specified statistic and sub-statistic
    String placeholder = "%playerstats_top:1," + stat + ",only:player_name%";
    return PlaceholderAPI.setPlaceholders(null, placeholder);
  }

  private String getMessageForStat(String stat) {
    // Map each statistic to its custom message
    Map < String, String > statMessages = new HashMap < > ();
    statMessages.put("break_item:stone_pickaxe", "&aThe player who has broken the most stone pickaxes is: &c%topPlayer% &awith &c%number% &ablocks!");
    statMessages.put("mine_block:stone", "&aThe player who has mined the most stone is: &c%topPlayer% &awith &c%number% &astone mined!");
    statMessages.put("animals_bred", "&aThe player who has bred the most animals is: &c%topPlayer% &awith &c%number% &aanimals bred!");
    statMessages.put("bell_ring", "&aThe player who has rung the most bells is: &c%topPlayer% &awith &c%number% &abells rung!");
    statMessages.put("boat_one_cm", "&aThe player who has traveled the most distance by boat is: &c%topPlayer% &awith &c%number% &ablocks!");
    statMessages.put("cake_slices_eaten", "&aThe player who has eaten the most cake slices is: &c%topPlayer% &awith &c%number% &aslices!");
    statMessages.put("cauldron_filled", "&aThe player who has filled the most cauldrons is: &c%topPlayer% &awith &c%number% &acauldrons!");
    statMessages.put("chest_opened", "&aThe player who has opened the most chests is: &c%topPlayer% &awith &c%number% &achests opened!");
    statMessages.put("climb_one_cm", "&aThe player who has climbed the most is: &c%topPlayer% &awith &c%number% &ablocks climbed!");
    statMessages.put("crouch_one_cm", "&aThe player who has crouched the most distance is: &c%topPlayer% &awith &c%number%&ablocks!");
    statMessages.put("damage_dealt", "&aThe player who has dealt the most damage is: &c%topPlayer% &awith &c%number% &adamage dealt!");
    statMessages.put("damage_taken", "&aThe player who has taken the most damage is: &c%topPlayer% &awith &c%number% &adamage taken!");
    statMessages.put("damage_blocked_by_shield", "&aThe player who has blocked the most damage with a shield is: &c%topPlayer% &awith &c%number% &adamage blocked!");
    statMessages.put("deaths", "&aThe player who has the most deaths is: &c%topPlayer% &awith &c%number% &adeaths!");
    statMessages.put("fall_one_cm", "&aThe player who has fallen the most blocks is: &c%topPlayer% &awho has fallen &c%number% &ablocks!");
    statMessages.put("flower_potted", "&aThe player who has potted the most flowers is: &c%topPlayer% &awith &c%number% &a!");
    statMessages.put("fish_caught", "&aThe player who has caught the most fish is: &c%topPlayer% &awith &c%number%&a!");
    statMessages.put("horse_one_cm", "&aThe player who has ridden the most distance on a horse is: &c%topPlayer% &awith &c%number%&a!");
    statMessages.put("item_enchanted", "&aThe player who has enchanted the most items is: &c%topPlayer% &awith &c%number%&a!");
    statMessages.put("jump", "&aThe player who has jumped the most is: &c%topPlayer% &awith &c%number%&a jumps!");
    statMessages.put("leave_game", "&aThe player who has left the game the most is: &c%topPlayer% &awith &c%number% &aleaves!");
    statMessages.put("minecart_one_cm", "&aThe player who has traveled the most distance by minecart is: &c%topPlayer% &awho has travelled &c%number% &ablocks!");
    statMessages.put("mob_kills", "&aThe player who has killed the most mobs is: &c%topPlayer% &awith &c%number% &amob kills!");
    statMessages.put("noteblock_played", "&aThe player who has played the most noteblocks is: &c%topPlayer% &awith &c%number% &anoteblocks played!");
    statMessages.put("noteblock_tuned", "&aThe player who has tuned the most noteblocks is: &c%topPlayer% &awith &c%number% &anoteblocks tuned!");
    statMessages.put("open_barrel", "&aThe player who has opened the most barrels is: &c%topPlayer% &awith &c%number% &a!");
    statMessages.put("pig_one_cm", "&aThe player who has traveled the most distance on a pig is: &c%topPlayer% &awith &c%number% &ablocks!");
    statMessages.put("play_one_minute", "&aThe player who has played the longest is: &c%topPlayer% &awith &c%number% &a!");
    statMessages.put("player_kills", "&aThe player who has the most player kills is: &c%topPlayer% &awith &c%number% &akills!");
    statMessages.put("sleep_in_bed", "&aThe player who has slept the most is: &c%topPlayer% &awith &c%number% &atimes slept!");
    statMessages.put("sneak_time", "&aThe player who has spent the most time sneaking is: &c%topPlayer% &awith &c%number%&a!");
    statMessages.put("sprint_one_cm", "&aThe player who has sprinted the most is: &c%topPlayer% &awith &c%number% &ablocks!");
    statMessages.put("swim_one_cm", "&aThe player who has swum the most is: &c%topPlayer% &awith &c%number% &ablocks!");
    statMessages.put("talked_to_villager", "&aThe player who has talked to the most villagers is: &c%topPlayer% &awith &c%number% &atimes!");
    statMessages.put("traded_with_villager", "&aThe player who has traded with the most villagers is: &c%topPlayer% &awith &c%number% &atimes!");
    statMessages.put("walk_on_water_one_cm", "&aThe player who has walked on water the most is: &c%topPlayer% &awith &c%number% &ablocks!");
    statMessages.put("walk_one_cm", "&aThe player who has walked the most is: &c%topPlayer% &awith &c%number% &ablocks!");
    statMessages.put("walk_under_water_one_cm", "&aThe player who has walked underwater the most is: &c%topPlayer% &awith &c%number% &ablocks!");
    statMessages.put("mine_block:oak_log", "&aThe player who has mined the most oak logs is: &c%topPlayer% &awith &c%number% &aoak logs!");
    statMessages.put("mine_block:spruce_log", "&aThe player who has mined the most spruce logs is: &c%topPlayer% &awith &c%number% &aspruce logs!");
    statMessages.put("mine_block:birch_log", "&aThe player who has mined the most birch logs is: &c%topPlayer% &awith &c%number% &abirch logs!");
    statMessages.put("mine_block:deepslate_diamond_ore", "&aThe player who has mined the most deepslate diamond ore is: &c%topPlayer% &awith &c%number% &adeepslate diamond ores!");
    statMessages.put("mine_block:deepslate", "&aThe player who has mined the most deepslate is: &c%topPlayer% &awith &c%number% &adeepslate!");
    statMessages.put("mine_block:grass_block", "&aThe player who has broken the most grass blocks is: &c%topPlayer% &awith &c%number% &agrass blocks!");
    statMessages.put("mine_block:wheat", "&aThe player who has harvested the most wheat is: &c%topPlayer% &awith &c%number% &awheat!");
    statMessages.put("mine_block:sugar_cane", "&aThe player who has harvested the most sugar cane is: &c%topPlayer% &awith &c%number% &asugar cane!");
    statMessages.put("mine_block:potatoes", "&aThe player who has harvested the most potatoes is: &c%topPlayer% &awith &c%number% &apotatoes!");
    statMessages.put("mine_block:acacia_log", "&aThe player who has mined the most acacia logs is: &c%topPlayer% &awith &c%number% &aacacia logs!");
    statMessages.put("mine_block:beetroots", "&aThe player who has harvested the most beetroots is: &c%topPlayer% &awith &c%number% &abeetroots!");
    statMessages.put("mine_block:bamboo", "&aThe player who has harvested the most bamboo is: &c%topPlayer% &awith &c%number% &abamboo!");
    statMessages.put("mine_block:sand", "&aThe player who has mined the most sand is: &c%topPlayer% &awith &c%number% &asand!");
    statMessages.put("mine_block:netherrack", "&aThe player who has mined the most netherrack is: &c%topPlayer% &awith &c%number% &anetherrack!");
    statMessages.put("mine_block:deepslate_iron_ore", "&aThe player who has mined the most deepslate iron ore is: &c%topPlayer% &awith &c%number% &adeepslate iron ores!");
    statMessages.put("mine_block:deepslate_redstone_ore", "&aThe player who has mined the most deepslate redstone ore is: &c%topPlayer% &awith &c%number% &adeepslate redstone ores!");
    statMessages.put("mine_block:deepslate_lapis_ore", "&aThe player who has mined the most deepslate lapis ore is: &c%topPlayer% &awith &c%number% &adeepslate lapis ores!");
    statMessages.put("mine_block:deepslate_coal_ore", "&aThe player who has mined the most deepslate coal ore is: &c%topPlayer% &awith &c%number% &adeepslate coal ores!");
    statMessages.put("mine_block:deepslate_gold_ore", "&aThe player who has mined the most deepslate gold ore is: &c%topPlayer% &awith &c%number% &adeepslate gold ores!");
    statMessages.put("mine_block:deepslate_copper_ore", "&aThe player who has mined the most deepslate copper ore is: &c%topPlayer% &awith &c%number% &adeepslate copper ores!");
    statMessages.put("mine_block:deepslate_iron_ore", "&aThe player who has mined the most deepslate iron ore is: &c%topPlayer% &awith &c%number% &adeepslate iron ores!");
    statMessages.put("kill_entity:zombie", "&aThe player who has killed the most zombies is: &c%topPlayer% &awith &c%number% &azombie kills!");
    statMessages.put("kill_entity:spider", "&aThe player who has killed the most spiders is: &c%topPlayer% &awith &c%number% &aspider kills!");
    statMessages.put("kill_entity:cave_spider", "&aThe player who has killed the most cave spiders is: &c%topPlayer% &awith &c%number% &acave spider kills!");
    statMessages.put("kill_entity:creeper", "&aThe player who has killed the most creepers is: &c%topPlayer% &awith &c%number% &acreeper kills!");
    statMessages.put("kill_entity:cow", "&aThe player who has killed the most cows is: &c%topPlayer% &awith &c%number% &acow kills!");
    statMessages.put("kill_entity:sheep", "&aThe player who has killed the most sheep is: &c%topPlayer% &awith &c%number% &asheep kills!");
    statMessages.put("kill_entity:enderman", "&aThe player who has killed the most endermen is: &c%topPlayer% &awith &c%number% &aendermen kills!");
    statMessages.put("kill_entity:guardian", "&aThe player who has killed the most guardians is: &c%topPlayer% &awith &c%number% &aguardian kills!");
    statMessages.put("kill_entity:iron_golem", "&aThe player who has killed the most iron golems is: &c%topPlayer% &awith &c%number% &airon golem kills!");
    statMessages.put("kill_entity:magma_cube", "&aThe player who has killed the most magma cubes is: &c%topPlayer% &awith &c%number% &amagma cube kills!");
    statMessages.put("kill_entity:villager", "&aThe player who has killed the most villagers is: &c%topPlayer% &awith &c%number% &avillager kills!");
    statMessages.put("kill_entity:vex", "&aThe player who has killed the most vexes is: &c%topPlayer% &awith &c%number% &avex kills!");
    statMessages.put("kill_entity:strider", "&aThe player who has killed the most striders is: &c%topPlayer% &awith &c%number% &astrider kills!");
    statMessages.put("kill_entity:squid", "&aThe player who has killed the most squids is: &c%topPlayer% &awith &c%number% &asquid kills!");
    statMessages.put("kill_entity:phantom", "&aThe player who has killed the most phantoms is: &c%topPlayer% &awith &c%number% &aphantom kills!");
    statMessages.put("kill_entity:silverfish", "&aThe player who has killed the most silverfish is: &c%topPlayer% &awith &c%number% &asilverfish kills!");
    statMessages.put("kill_entity:piglin_brute", "&aThe player who has killed the most piglin brutes is: &c%topPlayer% &awith &c%number% &apiglin brute kills!");
    statMessages.put("kill_entity:piglin", "&aThe player who has killed the most piglins is: &c%topPlayer% &awith &c%number% &apiglin kills!");
    statMessages.put("kill_entity:blaze", "&aThe player who has killed the most blazes is: &c%topPlayer% &awith &c%number% &ablaze kills!");
    statMessages.put("kill_entity:chicken", "&aThe player who has killed the most chickens is: &c%topPlayer% &awith &c%number% &achicken kills!");
    statMessages.put("kill_entity:pig", "&aThe player who has killed the most pigs is: &c%topPlayer% &awith &c%number% &apig kills!");
    statMessages.put("kill_entity:slime", "&aThe player who has killed the most slimes is: &c%topPlayer% &awith &c%number% &aslime kills!");
    statMessages.put("kill_entity:witch", "&aThe player who has killed the most witches is: &c%topPlayer% &awith &c%number% &awitch kills!");
    statMessages.put("mine_block:deepslate_emerald_ore", "&aThe player who has mined the most deepslate emerald ore is: &c%topPlayer% &awith &c%number% &adeepslate emerald ores!");
    statMessages.put("kill_entity:skeleton", "&aThe player who has killed the most skeletons is: &c%topPlayer% &awith &c%number% &askeleton kills!");
    statMessages.put("kill_entity:ghast", "&aThe player who has killed the most ghasts is: &c%topPlayer% &awith &c%number% &aghast kills!");

    // Get the custom message for the statistic
    return statMessages.getOrDefault(stat, "&aThe top player for %stat% is: &c%topPlayer%");
  }

  private String colorize(String message) {
    // Use Minecraft color codes to add color to the message
    return message.replace("&", "\u00A7");
  }
}