package com.lu7creative.lu7creative;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class StatAnnouncements extends JavaPlugin {

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
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("broadcaststat")) {
            // Manually trigger the broadcast when the command is executed
            sendRandomAnnouncement();
            sender.sendMessage("Broadcast triggered manually.");
            return true;
        }
        return false;
    }

    private void sendRandomAnnouncement() {
        // Check if there are players online
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            return;
        }

        // Specify the statistics and sub-statistics you want to announce
        String[] statistics = {
                "break_item:stone_pickaxe", "mine_block:stone", "animals_bred", "bell_ring",
                "boat_one_cm", "cake_slices_eaten", "cauldron_filled", "chest_opened", "climb_one_cm",
                "crouch_one_cm", "damage_dealt", "damage_taken", "damage_blocked_by_shield",
                "deaths", "fall_one_cm", "flower_potted", "fish_caught",
                "horse_one_cm", "item_enchanted", "jump", "leave_game", "minecart_one_cm",
                "mob_kills", "noteblock_played", "noteblock_tuned", "open_barrel", "pig_one_cm",
                "play_one_minute", "player_kills", "sleep_in_bed", "sneak_time", "sprint_one_cm",
                "swim_one_cm", "talked_to_villager", "traded_with_villager",
                "walk_on_water_one_cm", "walk_one_cm", "walk_under_water_one_cm",
                "mine_block:oak_log", "mine_block:spruce_log", "mine_block:birch_log", "mine_block:deepslate_diamond_ore",
                "mine_block:deepslate", "mine_block:grass_block", "mine_block:wheat", "mine_block:sugar_cane", "mine_block:potatoes",
                "mine_block:acacia_log", "mine_block:beetroots", "mine_block:bamboo", "mine_block:sand"
            };

        // Randomly pick one statistic to announce
        Random random = new Random();
        String randomStat = statistics[random.nextInt(statistics.length)];

        // Use CompletableFuture for asynchronous processing
        CompletableFuture<String> topPlayerFuture1 = CompletableFuture.supplyAsync(() -> getTopPlayer(randomStat));
        CompletableFuture<String> numberFuture1 = CompletableFuture.supplyAsync(() -> getNumber(randomStat));

        CompletableFuture<Void> combinedFuture1 = CompletableFuture.allOf(topPlayerFuture1, numberFuture1);

        // Introduce a small delay (adjust the time as needed)
        try {
            TimeUnit.MILLISECONDS.sleep(100);
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
        String customMessage = getMessageForStat(randomStat);

        // Broadcast the colored announcement to all online players
        String message = "&9&l[&6&lL&a&lU&e&l7&c&l Stats&9&l] " + customMessage
                .replace("%stat%", randomStat.replace(":", " "))
                .replace("%topPlayer%", topPlayer)
                .replace("%number%", number); // Use the retrieved number directly

        Bukkit.broadcastMessage(colorize(message));
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
        Map<String, String> statMessages = new HashMap<>();
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
        statMessages.put("horse_one_cm", "&aThe player who has ridden the most distance on a horse is: &c%topPlayer% &awith &c%number% &a!");
        statMessages.put("item_enchanted", "&aThe player who has enchanted the most items is: &c%topPlayer% &awith &c%number%&a!");
        statMessages.put("jump", "&aThe player who has jumped the most is: &c%topPlayer% &awith &c%number%&ajumps!");
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

        // Get the custom message for the statistic
        return statMessages.getOrDefault(stat, "&aThe top player for %stat% is: &c%topPlayer%");
    }

    private String colorize(String message) {
        // Use Minecraft color codes to add color to the message
        return message.replace("&", "\u00A7");
    }
}

