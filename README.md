<div align='center'>

<img src=https://cdn.luckvintage.com/LU7Logo.jpg alt="logo" width=200 height=200 />

<h1>LU7Stats Minecraft Plugin</h1>
<p>LU7Stats is a Minecraft Plugin that broadcasts top stat messages in chat every 15 minutes. This plugin was primarily created for use on a private survival server. Pull requests are welcome. </p>

</div>

## Installation:

- Download the latest .jar from [here](https://github.com/LuckVintage/LU7Stats/raw/main/target/lu7stats-2.0-SNAPSHOT.jar).
- Place the downloaded .jar file into your Minecraft server plugins folder.
- Start your server.


### Version Support:

LU7Stats has been tested and confirmed to work on Paper 26.2. We will always support the latest version of Minecraft.

## Commands and Permissions:


| Command | Description | Permission |
|---|---|---|
| `/broadcaststat` | Manually triggers the broadcast of a random stat message | `lu7stats.manualbroadcasts` |
| `/broadcaststat <stat>` | Manually triggers the broadcast of a specific stat message | `lu7stats.manualbroadcasts` |
| `/lu7statsreload` | Manually reload all plugin config files | `lu7stats.reload` |
| `/lu7statshealth` | Checks the plugin health | `lu7stats.healthcheck` |
| N/A | Permission to see stat broadcast messages - default permission | `lu7stats.seebroadcasts` |

## Statistics and Broadcast Customisation:

You can easily customize the broadcast messages for each statistic by modifying the messages.yml file. You can also format your messages with colour codes. You can customise the prefix and how often statistics are broadcast by modifying the ```config.yml``` file.

When creating your messages, use the following placeholders:

| Placeholder | Description |
|---|---|
| `%topPlayer%` | This will automatically be replaced with the player's name |
| `%number%` | This will be replaced with the value of the statistic |


For example, ```"mine_block:carrots": "&aThe player who has harvested the most carrots is: &c%topPlayer% &awith &c%number% &acarrots!"``` will result in the below stat broadcast:

![Screenshot of stat broadcast message](https://cdn.luckvintage.com/lu7stats.png)