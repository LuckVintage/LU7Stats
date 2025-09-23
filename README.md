# LU7Stats Minecraft Plugin:

LU7Stats is a Minecraft Plugin that utilizes PlaceholderAPI to broadcast top stat messages every 15 minutes with the help of PlayerStats. This plugin was primarily created for use on a private survival server. Pull requests are welcome.

## Installation:

- Download the latest .jar from [here](https://github.com/LuckVintage/LU7Stats/raw/main/target/lu7stats-1.2-SNAPSHOT.jar).
- Download PlaceholderAPI and PlayerStats. Drop all .jar files into your Minecraft server plugins folder.
- Start your server.
- Run this PlaceholderAPI command to download the PlayerStatsExpansion: ```/papi ecloud download PlayerStats```.
- Reload PlaceholderAPI with the command ```/papi reload``` (or just restart your server).

### Prerequisites:

- [PlaceholderAPI version 2.11.6](https://repo.extendedclip.com/content/repositories/placeholderapi/me/clip/placeholderapi/2.11.6/placeholderapi-2.11.6.jar)
- [PlayerStatsExpansion for PlaceholderAPI](https://github.com/Artemis-the-gr8/PlayerStatsExpansion)
- [PlayerStats version 2.5](https://github.com/itHotL/PlayerStats)

### Version Support:

LU7Stats has been tested and confirmed to work on Minecraft Java versions: 1.20 - 1.21.8.

## Commands and Permissions:


| Command | Description | Permission |
|---|---|---|
| `/broadcaststat` | Manually triggers the broadcast of a random stat message | `lu7stats.manualbroadcasts` |
| `/broadcaststat <stat>` | Manually triggers the broadcast of a specific stat message | `lu7stats.manualbroadcasts` |
| `/lu7statsreload` | Manually reload all plugin config files | `lu7stats.reload` |
| `/lu7statshealth` | Checks the plugin health | `lu7stats.healthcheck` |
| N/A | Permission to see stat broadcast messages - default permission | `lu7stats.seebroadcasts` |

## Statistics and Broadcast Customisation:

You can easily customize the broadcast messages for each statistic by modifying the messages.yml file. Inside this file, you have the flexibility to add or remove any statistics that are supported by PlayerStats. You can also format your messages with colour codes.

When creating your messages, use the following placeholders:

| Placeholder | Description |
|---|---|
| `%topPlayer%` | This will automatically be replaced with the player's name |
| `%number%` | This will be replaced with the value of the statistic |


For example, ```"mine_block:carrots": "&aThe player who has harvested the most carrots is: &c%topPlayer% &awith &c%number% &acarrots!"``` will result in the below stat broadcast:

![Screenshot of stat broadcast message](https://cdn.luckvintage.com/lu7stats.png)

<details>
<summary>Statistics Included By Default:</summary>

```markdown
- animals_bred
- bell_ring
- boat_one_cm
- break_item:bow
- break_item:crossbow
- break_item:diamond_axe
- break_item:diamond_hoe
- break_item:diamond_pickaxe
- break_item:diamond_shovel
- break_item:diamond_sword
- break_item:elytra
- break_item:fishing_rod
- break_item:flint_and_steel
- break_item:iron_axe
- break_item:iron_hoe
- break_item:iron_pickaxe
- break_item:iron_shovel
- break_item:iron_sword
- break_item:netherite_axe
- break_item:netherite_hoe
- break_item:netherite_pickaxe
- break_item:netherite_shovel
- break_item:netherite_sword
- break_item:shield
- break_item:stone_axe
- break_item:stone_hoe
- break_item:stone_pickaxe
- break_item:stone_shovel
- break_item:stone_sword
- break_item:trident
- break_item:wooden_axe
- break_item:wooden_hoe
- break_item:wooden_pickaxe
- break_item:wooden_shovel
- break_item:wooden_sword
- cake_slices_eaten
- cauldron_filled
- chest_opened
- climb_one_cm
- craft_item:acacia_planks
- craft_item:anvil
- craft_item:bamboo_planks
- craft_item:beacon
- craft_item:birch_planks
- craft_item:bone_meal
- craft_item:bread
- craft_item:brewing_stand
- craft_item:cherry_planks
- craft_item:chest
- craft_item:comparator
- craft_item:crafting_table
- craft_item:crimson_planks
- craft_item:dark_oak_planks
- craft_item:dispenser
- craft_item:enchanting_table
- craft_item:ender_chest
- craft_item:furnace
- craft_item:glass
- craft_item:glass_bottle
- craft_item:golden_apple
- craft_item:item_frame
- craft_item:jungle_planks
- craft_item:ladder
- craft_item:mangrove_planks
- craft_item:netherite_ingot
- craft_item:note_block
- craft_item:oak_planks
- craft_item:painting
- craft_item:repeater
- craft_item:shears
- craft_item:shield
- craft_item:shulker_box
- craft_item:spruce_planks
- craft_item:tinted_glass
- craft_item:tnt
- craft_item:warped_planks
- craft_item:white_bed
- craft_item:copper_ingot
- craft_item:cut_copper_stairs
- craft_item:cut_copper_slab
- craft_item:exposed_cut_copper_stairs
- craft_item:exposed_cut_copper_slab
- craft_item:weathered_cut_copper_stairs
- craft_item:weathered_cut_copper_slab
- craft_item:oxidized_cut_copper_stairs
- craft_item:oxidized_cut_copper_slab
- craft_item:waxed_cut_copper_stairs
- craft_item:waxed_cut_copper_slab
- craft_item:waxed_exposed_copper_stairs
- craft_item:waxed_exposed_copper_slab
- craft_item:waxed_weathered_copper_stairs
- craft_item:waxed_weathered_copper_slab
- craft_item:lightning_rod
- crouch_one_cm
- damage_blocked_by_shield
- damage_dealt
- damage_taken
- deaths
- fall_one_cm
- fish_caught
- flower_potted
- fly_one_cm
- horse_one_cm
- interact_with_blast_furnace
- interact_with_smoker
- item_enchanted
- jump
- kill_entity:blaze
- kill_entity:cave_spider
- kill_entity:chicken
- kill_entity:cow
- kill_entity:creeper
- kill_entity:elder_guardian
- kill_entity:ender_dragon
- kill_entity:enderman
- kill_entity:ghast
- kill_entity:guardian
- kill_entity:iron_golem
- kill_entity:magma_cube
- kill_entity:phantom
- kill_entity:pig
- kill_entity:piglin
- kill_entity:piglin_brute
- kill_entity:sheep
- kill_entity:silverfish
- kill_entity:skeleton
- kill_entity:slime
- kill_entity:spider
- kill_entity:squid
- kill_entity:strider
- kill_entity:vex
- kill_entity:villager
- kill_entity:witch
- kill_entity:wither_skeleton
- kill_entity:zombie
- leave_game
- mine_block:acacia_log
- mine_block:ancient_debris
- mine_block:andesite
- mine_block:bamboo
- mine_block:beetroots
- mine_block:birch_log
- mine_block:brown_mushroom
- mine_block:cactus
- mine_block:carrots
- mine_block:chorus_plant
- mine_block:clay
- mine_block:coal_ore
- mine_block:cobbled_deepslate
- mine_block:cobblestone
- mine_block:cocoa
- mine_block:dark_oak_log
- mine_block:deepslate
- mine_block:deepslate_coal_ore
- mine_block:deepslate_diamond_ore
- mine_block:deepslate_emerald_ore
- mine_block:deepslate_gold_ore
- mine_block:deepslate_iron_ore
- mine_block:deepslate_lapis_ore
- mine_block:deepslate_redstone_ore
- mine_block:diamond_ore
- mine_block:dirt
- mine_block:emerald_ore
- mine_block:gold_ore
- mine_block:granite
- mine_block:grass_block
- mine_block:iron_block
- mine_block:iron_ore
- mine_block:jungle_log
- mine_block:kelp_plant
- mine_block:lapis_ore
- mine_block:mangrove_log
- mine_block:melon
- mine_block:nether_wart
- mine_block:netherrack
- mine_block:oak_log
- mine_block:obsidian
- mine_block:potatoes
- mine_block:pumpkin
- mine_block:red_mushroom
- mine_block:redstone_ore
- mine_block:sand
- mine_block:sea_pickle
- mine_block:soul_sand
- mine_block:sponge
- mine_block:spruce_log
- mine_block:stone
- mine_block:sugar_cane
- mine_block:sweet_berry_bush
- mine_block:warped_fungus
- mine_block:warped_log
- mine_block:wheat
- mine_block:copper_ore
- mine_block:deepslate_copper_ore
- mine_block:raw_copper_block
- mine_block:copper_block
- mine_block:cut_copper
- mine_block:exposed_copper
- mine_block:weathered_copper
- mine_block:oxidized_copper
- mine_block:exposed_cut_copper
- mine_block:weathered_cut_copper
- mine_block:oxidized_cut_copper
- mine_block:waxed_copper_block
- mine_block:waxed_cut_copper
- mine_block:waxed_exposed_copper
- mine_block:waxed_weathered_copper
- mine_block:waxed_oxidized_copper
- mine_block:waxed_exposed_cut_copper
- mine_block:waxed_weathered_cut_copper
- mine_block:waxed_oxidized_cut_copper
- minecart_one_cm
- mob_kills
- noteblock_played
- noteblock_tuned
- open_barrel
- pig_one_cm
- play_one_minute
- player_kills
- sleep_in_bed
- sneak_time
- sprint_one_cm
- swim_one_cm
- talked_to_villager
- traded_with_villager
- walk_one_cm
- walk_under_water_one_cm

You can customise the prefix and how often statistics are broadcast by modifying the ```config.yml``` file.