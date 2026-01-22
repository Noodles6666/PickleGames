# Soccer游戏待修复清单

## 当前状态
- 编译时间: 2026-01-22 23:45:27
- 基础版本: Procyon反编译的干净版本
- Game.java: 已包含Folia修复（ConcurrentModificationException已修复）
- Soccer.java: 原始版本，需要添加Folia兼容性修改

## 需要的修改

### 1. 添加传送保护（最重要）
**问题**: 玩家传送时被误判为出界
**需要添加**:
- `private List<UUID> teleporting_players` 字段
- 在构造函数和onJoin中标记传送中的玩家
- 在onMove中忽略传送中的玩家

### 2. 修复倒计时不启动
**问题**: 两个玩家加入后倒计时不开始
**需要修改**:
- 在onJoin事件结束时调用倒计时检查

### 3. Folia实体任务包装
**需要包装的操作**:
- 构造函数中的 `preparePlayer()` 和 `giveItems()`
- onJoin中的 `preparePlayer()` 和 `giveItems()`
- assignTeam中的盔甲设置
- 游戏开始时的物品栏设置

### 4. 其他Folia修复
- SlimeSoccerBall.teleport() 改为 teleportAsync()
- 球物理任务使用 runLocationTaskTimer()

## 建议的修复顺序

1. **先测试当前版本** - 看看哪些功能正常，哪些有问题
2. **添加传送保护** - 这是最关键的修复
3. **修复倒计时** - 让游戏可以开始
4. **添加实体任务包装** - 确保Folia兼容性
5. **测试和调试** - 逐步验证每个修改

## 快速修复方案

如果你想快速测试，可以：
1. 重启服务器
2. 测试当前版本
3. 报告具体的问题
4. 我会针对性地修复最关键的问题

当前版本至少解决了：
- ✅ ConcurrentModificationException
- ✅ 可以编译和运行
- ✅ 其他游戏（Pool, Snake等）的Folia修复保留

还需要解决：
- ❌ Soccer传送问题
- ❌ Soccer倒计时问题
- ❌ Soccer的Folia兼容性
