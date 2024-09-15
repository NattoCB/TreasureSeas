
![banner](https://github.com/i493052739/TreasureSeas/blob/main/src/main/resources/treasure_seas.png)

> 开始一段冒险之旅，在这里钓鱼不再仅仅是放线。
>
> 在这宝藏之海中，每次捕获都是和深海水生生物的一场战斗，
>
> 钓到你的战利品，出售它们来赚取宝石；升级你的钓竿，发现稀有的鱼种和宝藏。
>
> 所向睥睨，逐步成为终极垂钓者。

---
模组仍然在早期开发中，版本支持有限，未来会新增更多玩法、支持更多版本。

如若你在游戏中遇到问题、或有任何新的建议，欢迎来到上方 github issue 面板内提出～

## 模组简介：

> 在尽可能兼容同类钓鱼模组的基础上，重构了钓鱼系统。
>
> 钓鱼内容高度可配置，让服主及整合包作者可以尽可能地发挥想象力

现在你可以通过为你的钓竿增加`【鱼之战斗I-V】`附魔，从而可以触发新版钓鱼，和新版战利品规则



> 新版钓鱼奖励可分为如下几类：
>
> 【鱼】【垃圾】【宝藏】【终极宝藏】
>
> 根据【鱼之战斗】附魔等级，Loot 奖励类别的概率也有不同

主世界奖励类型百分比分布图

| 等级 | 宝藏 | 垃圾 | 鱼   | 终极宝藏 |
| ---- | ---- | ---- | ---- | -------- |
| 1    | 5.0  | 10.0 | 84.8 | 0.2      |
| 2    | 5.0  | 8.0  | 86.6 | 0.4      |
| 3    | 5.0  | 6.0  | 88.2 | 0.8      |
| 4    | 5.0  | 4.0  | 89.9 | 1.2      |
| 5    | 5.0  | 2.0  | 90.5 | 2.5      |

地狱奖励类型百分比分布图（需要搭配模组：炽海生机）

| 等级 | 宝藏 | 垃圾 | 鱼   | 终极宝藏 |
| ---- | ---- | ---- | ---- | -------- |
| 1    | 5.0  | 65.0 | 29.8 | 0.2      |
| 2    | 6.0  | 57.0 | 36.2 | 0.8      |
| 3    | 8.0  | 48.0 | 42.2 | 1.8      |
| 4    | 9.0  | 42.0 | 46.8 | 2.2      |
| 5    | 9.0  | 33.0 | 53.0 | 5.0      |


## 品质
> 而每条鱼在不同【世界】【天气】【时间】下均存在如下可能的稀有度：
> * ORDINARY（普通）0-50%
> * UNCOMMON（少见）50-60%
> * RARE（稀有）60-75%
> * SUPERIOR（优质）75-90%
> * EXCEPTIONAL（卓越）90-95%
> * LEGEND（传说）95-97.5%
> * MYTHIC（史诗）97.5-99%
> * DIVINE（神圣）99%+
>
越稀有的品质，鱼的体长越大，垂钓难度越高（你可通过升级钓竿【鱼之战斗】附魔等级来与之抗衡）

在 雷雨天、夜晚、乘船 钓到大鱼、稀有鱼的概率均会略微增加 ~~（风浪越大，鱼越贵~~

此外，不同的稀有度会给钓手带来不同的【经验倍数】【售价倍数】（绿宝石）以及【成就】

在极小的几率（1/1800）下，你可能遇到【闪光】鱼，它是普通同品质鱼类的 5 倍价格。

鱼类出售的方式也变得更加方便：你仅需要8个任意木板（围成箱子形状），1个任意原版鱼，即可得到【商用鱼篓】方块。你可以在这里对鱼和宝藏进行交易，来赚取绿宝石。

## 成就
包含一系列成就可获取：
1. 共同钓鱼
2. 海盗之宝（等价于128绿宝石）
3. 力量果实（永久增加0.5伤害，最多使用8次）
4. 生机果实(永久增加1心，最多使用3次)
5. 闪光鱼
6. 各长度的鱼
7. 各品质的鱼

## 钓竿成长

在附魔【鱼之战斗】后，钓竿会开始记录 钓鱼次数，并会自动补全耐久

对应钓鱼次数满足后，你可以用下一本【鱼之战斗】附魔书来对钓竿在【铁砧】进行升级：

* 等级1需求:  **0** 只
* 等级2需求:  **25** 只
* 等级3需求:  **85** 只
* 等级4需求:  **175** 只
* 等级5需求:  **335** 只

如果附魔书等级大于当前钓竿等级，如用【V】给【I】级钓竿附魔，那么仅可以得到【II】等级钓竿。

## 推荐整合搭配

* 炽海生机 Nether Depths Upgrade https://www.mcmod.cn/class/7717.html
* 水产业2 Aquaculture 2 https://www.mcmod.cn/class/281.html
* 盗贼之海 Fish of Thieves https://www.mcmod.cn/class/11434.html
* 独特鱼类 Unusual Fish Mod https://www.mcmod.cn/class/8753.html
* 蟹农乐事 Crabber's Delight https://www.mcmod.cn/class/11415.html
* 旅行背包 Traveler's Backpack https://www.mcmod.cn/class/1732.html

在搭配上述模组后，配置默认启用 69 种鱼、13 种宝藏、27 种垃圾、7 个终极宝藏

推荐使用旅行背包来装鱼，从而更方便地搬运你的战利品

当然，你也可以原生使用本模组，或剔除一些你不需要、无法兼容的模组，你依然可以默认使用到一部分本模组的原生配置

或者，你也可以动手，自己修改、书写配置（见下方教程）来自行搭配你想添加的鱼、宝藏，来进一步提高冒险的乐趣。


## 关于配置：

### 客户端配置

```properties
# 是否启用钓鱼 HUD 窗体（默认屏幕居中的偏右侧）
hud.fishing_info.enable=true
# 是否启用钓鱼 HUD 窗体自定义位置（true 代表按照下方的 x y 值显示 HUD）
hud.fishing_info.custom_position.enable=false
hud.fishing_info.custom_position.x=0
hud.fishing_info.custom_position.y=20
# 是否启用调试模式（可在 /.minecraft/logs/latest.log 和 debug.log 查看详细日志，你可以用来排查你自己客制化的鱼类配置信息是否正确路由）
log.debug_mode.enable=false
```

### 通用配置
```toml
[Fishes] # 一共四类 Fishes、Junks、Treasures、UltimateTreasures

[Fishes.minecraft_salmon] # 自定义名称
lowestLootableEnchantmentLevel = 1 # 最低【鱼之战斗附魔等级可遇到】
ticksToWin = 200 # 战斗胜利的最短时长 ticks
speedModifier = 1.5 # 调试中的参数，暂留 1.0~2.0 之间
flatSegmentRandomRangeMin = 1.0 # 战斗途中鱼类维持平稳状态的最小可能时间（秒）
flatSegmentRandomRangeMax = 3.5 # 战斗途中鱼类维持平稳状态的最大可能时间（秒）
fluxSegmentRandomRangeMin = 2.0 # 战斗途中鱼类维持波动状态的最小可能时间（秒）
fluxSegmentRandomRangeMax = 4.5 # 战斗途中鱼类维持波动状态的最大可能时间（秒）
allowedTime = "night" # 鱼可能出现的时间段，可用值： MORNING, AFTERNOON, EVENING, NIGHT, ALL 不区分大小写
allowedWeather = "rain" # 鱼可能出现的天气，可用值： CLEAR, RAIN, STORM, ALL 不区分大小写
possibleBiomes = ["minecraft:river", "minecraft:swamp"] # 鱼可能出现的生物群系，支持模组群系，如若留空[]则代表在该世界所有群系可遭遇
possibleWorlds = ["overworld"] # 鱼可能出现的世界，如 the_nether, the_end，或模组世界，如若留空[]则代表所有世界
modNamespace = "minecraft" # 鱼物品的模组id
fishItemName = "pufferfish" # 鱼物品的物品id
minLength = 10.0 # 鱼可能的最小长度 cm
maxLength = 200.0 # 鱼可能的最大长度 cm
mostCommonLength = 40.0 # 鱼最常见的长度 cm
lengthDispersion = 8.0 # 鱼长度概率分布的离散程度，一般 1 - 20
minAppearDepth = 10 # 鱼可能出现的最小水深位置 m
maxAppearDepth = 25 # 鱼可能出现的最大水深位置 m
sampleWeight = 5 # 鱼在同位置内的种群丰度（用于和同位置内的其他鱼类进行权重抽样）
caveOnly = false # 是否仅在洞穴出现
basePrice = 4 # 售卖绿宝石的基础价值

# 模组鱼案例，地狱岩浆鱼
[Fishes.netherdepthsupgrade_obsidianfish]
lowestLootableEnchantmentLevel = 2
ticksToWin = 340
speedModifier = 2.1
flatSegmentRandomRangeMin = 0.8
flatSegmentRandomRangeMax = 2.0
fluxSegmentRandomRangeMin = 1.5
fluxSegmentRandomRangeMax = 5.5
allowedTime = "all"
allowedWeather = "all"
possibleBiomes = ["minecraft:crimson_forest", "minecraft:basalt_deltas"]
possibleWorlds = ["the_nether"]
modNamespace = "netherdepthsupgrade"
fishItemName = "obsidianfish"
minLength = 15.0
maxLength = 120.0
mostCommonLength = 30.0
lengthDispersion = 10.0
minAppearDepth = 0
maxAppearDepth = 100
sampleWeight = 4
caveOnly = false
basePrice = 12

# 宝藏案例
[Treasures]

[Treasures.minecraft_bow]
lowestLootableEnchantmentLevel = 1
ticksToWin = 250
speedModifier = 1.3
flatSegmentRandomRangeMin = 0.8
flatSegmentRandomRangeMax = 3.0
fluxSegmentRandomRangeMin = 3.5
fluxSegmentRandomRangeMax = 7.5
allowedTime = "all"
allowedWeather = "all"
possibleBiomes = []
possibleWorlds = ["overworld"]
modNamespace = "minecraft"
fishItemName = "bow"
minLength = 0.0
maxLength = 0.0
mostCommonLength = 0.0
lengthDispersion = 0.0
minAppearDepth = 0
maxAppearDepth = 100
sampleWeight = 4
caveOnly = false
basePrice = 0

# 垃圾案例

[Junks]

[Junks.minecraft_lily_pad]
lowestLootableEnchantmentLevel = 1
ticksToWin = 150
speedModifier = 1.0
flatSegmentRandomRangeMin = 2.8
flatSegmentRandomRangeMax = 4.5
fluxSegmentRandomRangeMin = 5.0
fluxSegmentRandomRangeMax = 5.5
allowedTime = "all"
allowedWeather = "all"
possibleBiomes = []
possibleWorlds = ["overworld"]
modNamespace = "minecraft"
fishItemName = "lily_pad"
minLength = 0.0
maxLength = 0.0
mostCommonLength = 0.0
lengthDispersion = 0.0
minAppearDepth = 0
maxAppearDepth = 100
sampleWeight = 10
caveOnly = false
basePrice = 0

# 终极宝藏案例

[UltimateTreasures]

[UltimateTreasures.treasure_seas_pirate_treasure]
lowestLootableEnchantmentLevel = 4
ticksToWin = 250
speedModifier = 1.3
flatSegmentRandomRangeMin = 0.8
flatSegmentRandomRangeMax = 5.0
fluxSegmentRandomRangeMin = 1.5
fluxSegmentRandomRangeMax = 3.5
allowedTime = "all"
allowedWeather = "all"
possibleBiomes = []
possibleWorlds = []
modNamespace = "treasure_seas"
fishItemName = "pirate_treasure"
minLength = 0.0
maxLength = 0.0
mostCommonLength = 0.0
lengthDispersion = 0.0
minAppearDepth = 30
maxAppearDepth = 100
sampleWeight = 1
caveOnly = false
basePrice = 128
```

## 调试命令

```yaml
# 重载所有配置（包含客户端HUD界面配置，和鱼类配置池）
/treasureseas reload

# 在日志界面打印当前整合包内所有已注册的群系名（供你选择给指定的群系增加鱼类配置）
/treasureseas log_biomes

# 在日志界面打印当前整合包内所有已注册的世界名（供你选择给指定的世界增加鱼类配置）
/treasureseas log_world_paths
```

---
![banner](https://github.com/i493052739/TreasureSeas/blob/main/src/main/resources/treasure_seas.png)

> Embark on an adventurous journey where fishing is more than just casting a line.
In Treasure Seas‘ enchantment, every catch is a battle as you engage in thrilling duels
with the aquatic creatures of the deep.  Once you've reeled in your prized catches,
sell them to earn emeralds and upgrade your gear.  Master your fishing rod, discover rare treasures,
and become the ultimate angler in this dynamic, treasure-filled world.


The mod is still in early development, and more gameplay features and version support will be added in the future. If you have any great ideas or encounter any issues, please create a ticket on the official GitHub Issues page [[Link](https://github.com/i493052739/TreasureSeas/issues)] and let me know.

Many thanks~


## Mod Overview:

The fishing system has been restructured to be as compatible as possible with similar fishing mods.

The fishing content is highly configurable, allowing server owners and modpack creators to use their imagination to the fullest.

Now you can trigger the new fishing system and the new loot rules by adding the `【Fish Fighter I-V】` enchantment to your fishing rod.

The new fishing rewards are categorized into the following types:

* 【Fish】【Junk】【Treasure】【Ultimate Treasure】

Depending on the level of the `Fish Fighter` enchantment, the probability of loot categories also varies.

### Overworld Reward Distribution:

| Level | Treasure | Junk  | Fish  | Ultimate Treasure |
| ----- | -------- | ----- | ----- | ----------------- |
| 1     | 5.0%     | 10.0% | 84.8% | 0.2%              |
| 2     | 5.0%     | 8.0%  | 86.6% | 0.4%              |
| 3     | 5.0%     | 6.0%  | 88.2% | 0.8%              |
| 4     | 5.0%     | 4.0%  | 89.9% | 1.2%              |
| 5     | 5.0%     | 2.0%  | 90.5% | 2.5%              |

### Nether Reward Distribution (Requires the mod: Nether Depths Upgrade):

| Level | Treasure | Junk  | Fish  | Ultimate Treasure |
| ----- | -------- | ----- | ----- | ----------------- |
| 1     | 5.0%     | 65.0% | 29.8% | 0.2%              |
| 2     | 6.0%     | 57.0% | 36.2% | 0.8%              |
| 3     | 8.0%     | 48.0% | 42.2% | 1.8%              |
| 4     | 9.0%     | 42.0% | 46.8% | 2.2%              |
| 5     | 9.0%     | 33.0% | 53.0% | 5.0%              |

Each fish has different rarity levels depending on the **Weather**, **Time**, and **Luck**:

- **ORDINARY** 0-50%
- **UNCOMMON** 50-60%
- **RARE** 60-75%
- **SUPERIOR** 75-90%
- **EXCEPTIONAL** 90-99%
- **LEGEND** 99-99.9%
- **MYTHIC** 99.9-99.99%
- **DIVINE** 99.99%+

Additionally, different rarities provide the angler with varying **experience multipliers**, **selling price multipliers** (emeralds), and **achievements**.

On a very small chance (1/1800), you might encounter a **Shiny** fish, which is worth 5 times the price of a regular fish of the same quality.

The process of selling fish has also become more convenient: you only need 8 of any type of wood planks (arranged in a chest shape) and 1 of any vanilla fish to craft a **Commercial Fish Basket** block. You can use this to trade fish and treasures for emeralds.

> Tip: During thunderstorms/rain/snow, at the evening/night, or while fishing on a boat, the chances of catching big fish and rare fish may slightly increase.

### Achievements:

1. **Co-Fishing** (fishing together with your friend)
2. **Pirate's Treasure** (Equivalent to 128 Emeralds)
3. **Fruit of Power** (Permanently increases damage by 0.5, up to 8 times)
4. **Fruit of Vitality** (Permanently increases 1 heart, up to 3 times)
5. **Shiny Fish**
6. **Various Fish Lengths**
7. **Various Fish Qualities**

## Fishing Rod Progression

After applying the `Fish Fighter` enchantment, the fishing rod will start tracking the number of fish caught and will automatically restore its durability.

Once the required number of catches is met, you can use the next level of the `Fish Fighter` enchantment book to upgrade the fishing rod at an **anvil**:

* **Requirement for Level 1:**  0 catches
* **Requirement for Level 2:**  25 catches
* **Requirement for Level 3:**  85 catches
* **Requirement for Level 4:**  175 catches
* **Requirement for Level 5:**  335 catches

If the enchantment book level is higher than the current fishing rod level (e.g., using a `V` level book on a `I` level fishing rod), you will only upgrade the rod to **Level II**.


## Recommended Mod Combinations

- **Nether Depths Upgrade**: [https://modrinth.com/mod/nether-depths-upgrade](https://modrinth.com/mod/nether-depths-upgrade)
- **Aquaculture 2**: [https://modrinth.com/mod/aquaculture](https://modrinth.com/mod/aquaculture)
- **Fish of Thieves**: [https://modrinth.com/mod/fish-of-thieves](https://modrinth.com/mod/fish-of-thieves)
- **Unusual Fish Mod**: [https://modrinth.com/mod/unusual-fish-mod](https://modrinth.com/mod/unusual-fish-mod)
- **Crabber's Delight**: [https://modrinth.com/mod/crabbers-delight](https://modrinth.com/mod/crabbers-delight)
- **Traveler's Backpack**: [https://modrinth.com/mod/travelersbackpack](https://modrinth.com/mod/travelersbackpack)

When paired with the above mods, 69 types of fish, 13 types of treasures, 27 types of junk, and 7 ultimate treasures are enabled by default.

Of course, you can use this mod natively, or remove some incompatible or unnecessary mods, and still access part of the default configuration.

Alternatively, you can get hands-on and write your own configuration (see tutorial below) to further enhance the adventure fun!

## Config Example
### Client-side config
`treasureseas-client.properties`
```properties
# Whether to enable the Fishing HUD window (default is slightly to the right of the center of the screen)
hud.fishing_info.enable=true
# Whether to enable custom position for the Fishing HUD window (true means the HUD will be displayed according to the x and y values below)
hud.fishing_info.custom_position.enable=false
hud.fishing_info.custom_position.x=0
hud.fishing_info.custom_position.y=20
# Whether to enable debug mode (detailed logs can be found in /.minecraft/logs/latest.log and debug.log, which you can use to check if your custom fish configuration is correctly routed)
log.debug_mode.enable=false
```

### Common config 
`treasureseas-common.tmol`
```toml
[Fishes] # There are four categories: Fishes, Junks, Treasures, UltimateTreasures

[Fishes.minecraft_salmon] # Custom name
lowestLootableEnchantmentLevel = 1 # Minimum 'Fish Fighter' enchantment level required to encounter
ticksToWin = 200 # Minimum duration in ticks to win the battle
speedModifier = 1.5 # currently under debugging&dev, keep between 1.0 and 2.0
flatSegmentRandomRangeMin = 1.0 # Minimum possible time (in seconds) for fish to maintain a stable state during the battle
flatSegmentRandomRangeMax = 3.5 # Maximum possible time (in seconds) for fish to maintain a stable state during the battle
fluxSegmentRandomRangeMin = 2.0 # Minimum possible time (in seconds) for fish to maintain a fluctuating state during the battle
fluxSegmentRandomRangeMax = 4.5 # Maximum possible time (in seconds) for fish to maintain a fluctuating state during the battle
allowedTime = "night" # Time of day when the fish can appear; possible values: MORNING, AFTERNOON, EVENING, NIGHT, ALL (case-insensitive)
allowedWeather = "rain" # Weather conditions when the fish can appear; possible values: CLEAR, RAIN, STORM, ALL (case-insensitive)
possibleBiomes = ["minecraft:river", "minecraft:swamp"] # Biomes where the fish can appear; supports modded biomes. If left empty [], the fish can be encountered in all biomes of the current world
possibleWorlds = ["overworld"] # Worlds where the fish can appear, e.g., the_nether, the_end, or modded worlds. If left empty [], the fish can be encountered in all worlds
modNamespace = "minecraft" # Mod ID of the fish item
fishItemName = "pufferfish" # Item ID of the fish item
minLength = 10.0 # Minimum possible length of the fish in cm
maxLength = 200.0 # Maximum possible length of the fish in cm
mostCommonLength = 40.0 # Most common length of the fish in cm
lengthDispersion = 8.0 # Dispersion of length probability distribution; generally between 1 - 20
minAppearDepth = 10 # Minimum depth (in meters) where the fish can appear
maxAppearDepth = 25 # Maximum depth (in meters) where the fish can appear
sampleWeight = 5 # Abundance of the fish population at the same location (used for weighted sampling against other fish species in the same area)
caveOnly = false # Whether the fish only appears in caves
basePrice = 4 # Base value in emeralds when sold

```
more examples please see the content above

## Debugging commands

```yaml
# Reload all configurations (including client HUD interface settings and fish configuration pools)
/treasureseas reload

# Print all registered biomes' names in this modpack to the console / log file 
# (for you to choose which biomes to add fish configurations)
/treasureseas log_biomes

# Print all registered world names in this modpack to the console / log file 
# (for you to choose which worlds to add fish configurations)
/treasureseas log_world_paths
```