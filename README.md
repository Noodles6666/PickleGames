# 🥒 PickleGames (泡菜游戏)

一个功能丰富的 Minecraft 大厅小游戏插件，包含 12 种精彩游戏！

## 🎮 游戏列表

1. **台球 (Pool)** - 经典 8 球台球游戏
2. **足球 (Soccer)** - 团队足球比赛
3. **贪吃蛇 (Snake)** - 经典贪吃蛇游戏
4. **扫雷 (Minesweeper)** - 经典扫雷游戏
5. **四子棋 (Connect 4)** - 垂直四子连珠游戏
6. **井字棋 (Tic Tac Toe)** - 经典井字棋游戏
7. **数独 (Sudoku)** - 经典数独谜题
8. **2048** - 合并方块达到 2048
9. **雪地大战 (Spleef)** - 破坏雪块，最后站立者获胜
10. **点击游戏 (Clicker)** - 快速点击方块游戏
11. **记忆翻牌 (Memory)** - 找到配对的颜色方块
12. **五子棋 (Gomoku)** - 连成五个同色棋子即可获胜

## ✨ 特性

- 🎨 **完整中文汉化** - 所有消息和界面都已汉化
- 🎯 **GUI 管理界面** - 可视化创建和管理竞技场
- 🏆 **排行榜系统** - 支持全局和本地排行榜
- 💰 **经济系统集成** - 支持 Vault 经济系统
- 📊 **PlaceholderAPI 支持** - 可在其他插件中显示游戏数据
- 🗄️ **MySQL 支持** - 可选的 MySQL 数据库存储
- 🎪 **靠近自动加入** - 玩家靠近竞技场自动加入游戏
- 🎮 **多竞技场支持** - 每种游戏可创建多个竞技场

## 📋 要求

- **Minecraft 版本**: 1.16 - 1.21.1
- **服务器类型**: Spigot / Paper / Purpur
- **Java 版本**: 16+
- **可选依赖**:
  - Vault (经济系统)
  - PlaceholderAPI (变量支持)

## 🚀 安装

1. 下载最新版本的 `PickleGames-1.0.jar`
2. 将 jar 文件放入服务器的 `plugins` 文件夹
3. 重启服务器
4. 编辑 `plugins/LobbyGames/config_zh_CN.yml` 配置文件
5. 使用 `/pg reload` 重新加载配置

## 🎯 快速开始

### 创建竞技场

1. 使用 `/pg gui` 打开管理界面
2. 点击"创建新竞技场"
3. 选择游戏类型
4. 按照提示设置竞技场坐标
5. 使用 `/pg save` 保存竞技场

### 加入游戏

- **命令加入**: `/pg join <游戏> [竞技场ID]`
- **靠近加入**: 走近启用了靠近自动加入的竞技场
- **告示牌加入**: 创建 `[JOIN]` 告示牌

## 📖 命令列表

| 命令 | 权限 | 说明 |
|------|------|------|
| `/pg gui` | `picklegames.admin` | 打开管理界面 |
| `/pg join <游戏> [ID]` | 无 | 加入游戏 |
| `/pg quit` | 无 | 退出当前游戏 |
| `/pg list [游戏]` | `picklegames.command` | 查看竞技场列表 |
| `/pg create <游戏>` | `picklegames.admin` | 创建竞技场 |
| `/pg delete <游戏> <ID>` | `picklegames.admin` | 删除竞技场 |
| `/pg tp <游戏> <ID>` | `picklegames.command` | 传送到竞技场 |
| `/pg kick <玩家>` | `picklegames.kickplayer` | 踢出玩家 |
| `/pg reload` | `picklegames.admin` | 重新加载配置 |

## 🔧 配置

主配置文件位于 `plugins/LobbyGames/config_zh_CN.yml`

### 基础配置

```yaml
# 排行榜大小
leaderboard-size: 5

# 冷却时间（秒）
cooldown-seconds: 3

# AFK 踢出时间（秒，-1 禁用）
afk-kick-seconds: 90

# 游戏结束后返回出生点
return-to-game-spawn: true
```

### 游戏配置

每个游戏都有独立的配置部分，例如：

```yaml
gomoku:
  game-alias: "五子棋"
  wager-cost: 0
  black-material: "BLACK_CONCRETE"
  white-material: "WHITE_CONCRETE"
  start-msg: |
    §3§m----------------------------------------
    §b§l五子棋：§b在棋盘上连成五个同色棋子即可获胜！
    §3§m----------------------------------------
```

## 🎨 GUI 管理界面

使用 `/pg gui` 打开可视化管理界面：

- **创建新竞技场** - 选择游戏类型并创建竞技场
- **管理现有竞技场** - 查看、编辑、删除竞技场
- **排行榜管理** - 管理全局和本地排行榜

### 竞技场详情菜单

- **靠近自动加入开关** - 切换是否允许玩家靠近自动加入
- **靠近范围调整** - 设置自动加入的触发范围（1-20 格）
- **传送到竞技场** - 快速传送到竞技场位置
- **删除竞技场** - 删除当前竞技场

## 🏆 排行榜

### 全局排行榜

显示所有竞技场的最高分：

```
/pg leaderboard create <游戏>
```

### 本地排行榜

显示特定竞技场的最高分：

```
/pg set leaderboard
```

## 🔌 PlaceholderAPI 变量

```
%lobbygames_<游戏>_score% - 玩家在该游戏的最高分
%lobbygames_<游戏>_rank% - 玩家在该游戏的排名
%lobbygames_total_wins% - 玩家总获胜次数
```

## 🛠️ 构建

### 要求

- Maven 3.6+
- Java 16+

### 构建命令

```bash
mvn clean package
```

构建后的 jar 文件位于 `target/PickleGames-1.0.jar`

## 📝 更新日志

### v1.0 (2026-01-20)

- ✅ 完整中文汉化
- ✅ 添加五子棋游戏
- ✅ 添加记忆翻牌游戏
- ✅ GUI 管理界面
- ✅ 靠近自动加入功能
- ✅ 靠近范围调整功能
- ✅ 品牌重塑为"泡菜游戏"
- ✅ 修复多个 Bug

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目基于原 LobbyGames 插件修改和汉化。

## 🔗 相关链接

- 原版插件: [LobbyGames by c7dev](https://github.com/c7dev/LobbyGames)
- Discord: [加入我们的 Discord](https://discord.gg/yx9hUByTzq)

## ⚠️ 注意事项

### Folia 兼容性

**当前版本不兼容 Folia**。如果你需要在 Folia 服务器上运行，请使用 Paper 服务器。

Folia 兼容版本正在开发中，敬请期待！

### 已知问题

- 某些游戏在垂直竞技场上可能有显示问题
- MySQL 连接需要正确配置才能使用

## 💡 提示

- 使用 `/pg gui` 是最简单的管理方式
- 建议先在测试服务器上创建竞技场
- 定期备份配置文件和数据库
- 查看 Wiki 获取详细的游戏创建指南

## 📞 支持

如果遇到问题：

1. 查看配置文件是否正确
2. 检查服务器日志中的错误信息
3. 在 GitHub 上提交 Issue
4. 加入 Discord 寻求帮助

---

**感谢使用泡菜游戏！祝你游戏愉快！** 🥒🎮
