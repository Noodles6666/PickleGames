# Soccer游戏修复快速参考

## 状态
✅ **已完成并编译成功** (2026-01-22 23:53)

## 快速测试步骤

```bash
# 1. 重启服务器（必须！）
stop
start

# 2. 创建竞技场
/pg create soccer 2

# 3. 设置竞技场
# 按提示设置两个角点和球门位置

# 4. 保存
/pg save

# 5. 第一个玩家加入
/pg join soccer 2

# 6. 第二个玩家加入
/pg join soccer 2

# 预期：倒计时自动启动！
```

## 主要修改

| 修改项 | 说明 |
|--------|------|
| BukkitRunnable → GameTask | 5处替换 |
| 传送 | 使用teleportAsync() |
| 传送保护 | 添加teleporting_players列表 |
| Y坐标检查 | -0.0 → -11.0 |
| 实体任务包装 | 所有玩家操作 |
| 初始延迟 | 0L → 1L |

## 解决的问题

1. ✅ 玩家加入后立即被踢出
2. ✅ 两个玩家加入后倒计时不启动
3. ✅ ConcurrentModificationException

## 文件位置

- 源码：`src/main/java/me/c7dev/lobbygames/games/Soccer.java`
- JAR：`target/PickleGames-1.0.jar`
- 部署：`C:\Users\Administrator\Desktop\测试端\plugins\PickleGames-1.0.jar`

## 关键代码片段

### 传送保护
```java
this.teleporting_players.add(player.getUniqueId());
player.teleportAsync(location).thenAccept(result -> {
    new GameTask() {
        public void run() {
            Soccer.this.teleporting_players.remove(player.getUniqueId());
        }
    }.runTaskLater((Plugin)plugin, 20L);
});
```

### 实体任务包装
```java
new GameTask() {
    public void run() {
        player.getInventory().setHelmet(...);
        // 其他物品栏操作
    }
}.runEntityTask((Plugin)plugin, player);
```

### Y坐标检查
```java
final double n2 = player.getLocation().getY() - arena.getCenterPixel().getY();
if (n2 > 11.0 || n2 < -11.0) {
    // 玩家超出范围
}
```

## 测试检查清单

- [ ] 服务器已完全重启
- [ ] 竞技场已创建并保存
- [ ] 第一个玩家能成功加入
- [ ] 第一个玩家不会被踢出
- [ ] 第二个玩家能成功加入
- [ ] 倒计时自动启动
- [ ] 游戏正常开始
- [ ] 队伍分配正常
- [ ] 球物理正常
- [ ] 进球检测正常

## 如果出现问题

1. 检查服务器日志
2. 确认是否完全重启（不是reload）
3. 确认Folia版本正确
4. 检查竞技场配置
5. 查看详细报告：`Soccer游戏Folia兼容性完整修复报告.md`

## 相关文档

- 📄 完整修复报告
- 📄 测试指南
- 📄 迁移完成总结

---

**记住：每次修改后必须完全重启服务器！**
