# Folia 迁移快速参考指南

## 🚀 快速替换模式

### 1. 简单延迟任务

#### ❌ 之前（Bukkit）
```java
Bukkit.getScheduler().runTaskLater(plugin, () -> {
    player.sendMessage("消息");
}, 20L);
```

#### ✅ 之后（Folia 兼容）
```java
SchedulerUtil.runTaskLater(plugin, () -> {
    player.sendMessage("消息");
}, 20L);
```

---

### 2. BukkitRunnable 定时任务

#### ❌ 之前（Bukkit）
```java
new BukkitRunnable() {
    @Override
    public void run() {
        if (!isActive()) {
            this.cancel();
            return;
        }
        updateGame();
    }
}.runTaskTimer(plugin, 0L, 20L);
```

#### ✅ 之后（Folia 兼容）
```java
new GameTask() {
    @Override
    public void run() {
        if (!isActive()) {
            this.cancel();
            return;
        }
        updateGame();
    }
}.runTaskTimer(plugin, 0L, 20L);
```

---

### 3. 玩家相关任务

#### ❌ 之前（Bukkit）
```java
Bukkit.getScheduler().runTask(plugin, () -> {
    player.teleport(location);
    player.sendMessage("传送成功");
});
```

#### ✅ 之后（Folia 兼容）
```java
SchedulerUtil.runEntityTask(plugin, player, () -> {
    player.teleport(location);
    player.sendMessage("传送成功");
});
```

---

### 4. 位置/方块相关任务

#### ❌ 之前（Bukkit）
```java
Bukkit.getScheduler().runTaskLater(plugin, () -> {
    location.getBlock().setType(Material.AIR);
}, 20L);
```

#### ✅ 之后（Folia 兼容）
```java
SchedulerUtil.runLocationTaskLater(plugin, location, () -> {
    location.getBlock().setType(Material.AIR);
}, 20L);
```

---

### 5. 异步任务

#### ❌ 之前（Bukkit）
```java
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    // 数据库操作
    database.save(data);
});
```

#### ✅ 之后（Folia 兼容）
```java
SchedulerUtil.runTaskAsynchronously(plugin, () -> {
    // 数据库操作
    database.save(data);
});
```

---

### 6. 保存任务引用以便取消

#### ❌ 之前（Bukkit）
```java
BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    updateGame();
}, 0L, 20L);

// 取消任务
task.cancel();
```

#### ✅ 之后（Folia 兼容）
```java
GameTask task = new GameTask() {
    @Override
    public void run() {
        updateGame();
    }
}.runTaskTimer(plugin, 0L, 20L);

// 取消任务
task.cancel();
```

---

## 📋 调度器选择决策树

```
需要运行任务？
│
├─ 涉及玩家操作？
│  ├─ 是 → 使用 SchedulerUtil.runEntityTask*()
│  │      例如：传送、发送消息、修改背包
│  │
│  └─ 否 → 继续判断
│
├─ 涉及方块/位置操作？
│  ├─ 是 → 使用 SchedulerUtil.runLocationTask*()
│  │      例如：放置方块、生成粒子、播放音效
│  │
│  └─ 否 → 继续判断
│
├─ 需要异步执行？
│  ├─ 是 → 使用 SchedulerUtil.runTaskAsynchronously()
│  │      例如：数据库操作、文件读写
│  │
│  └─ 否 → 使用 SchedulerUtil.runTask*()
│           例如：全局逻辑、配置重载
```

---

## 🎯 常见场景示例

### 场景 1：游戏倒计时

```java
// 游戏开始倒计时
GameTask countdownTask = new GameTask() {
    int countdown = 5;
    
    @Override
    public void run() {
        if (countdown <= 0) {
            startGame();
            this.cancel();
            return;
        }
        
        for (Player p : players) {
            p.sendMessage("游戏将在 " + countdown + " 秒后开始");
        }
        countdown--;
    }
}.runTaskTimer(plugin, 0L, 20L);
```

### 场景 2：玩家传送

```java
// 传送玩家到竞技场
SchedulerUtil.runEntityTask(plugin, player, () -> {
    player.teleport(arena.getSpawn());
    player.sendMessage("已传送到竞技场");
});
```

### 场景 3：方块动画

```java
// 方块逐渐消失动画
for (int i = 0; i < blocks.size(); i++) {
    final Location loc = blocks.get(i);
    final int delay = i * 2L; // 每个方块延迟 2 tick
    
    SchedulerUtil.runLocationTaskLater(plugin, loc, () -> {
        loc.getBlock().setType(Material.AIR);
    }, delay);
}
```

### 场景 4：数据库保存

```java
// 异步保存玩家数据
SchedulerUtil.runTaskAsynchronously(plugin, () -> {
    database.savePlayerStats(player.getUniqueId(), stats);
});
```

### 场景 5：游戏循环

```java
// 游戏主循环
GameTask gameLoop = new GameTask() {
    @Override
    public void run() {
        if (!isActive()) {
            this.cancel();
            return;
        }
        
        // 更新游戏状态
        updateGameState();
        
        // 检查胜利条件
        if (checkWinCondition()) {
            endGame();
            this.cancel();
        }
    }
}.runTaskTimer(plugin, 0L, 1L); // 每 tick 执行一次
```

### 场景 6：粒子效果

```java
// 在位置播放粒子效果
SchedulerUtil.runLocationTask(plugin, location, () -> {
    location.getWorld().spawnParticle(
        Particle.FLAME, 
        location, 
        10, 
        0.5, 0.5, 0.5, 
        0.1
    );
});
```

---

## ⚠️ 常见错误

### 错误 1：在 Folia 上使用全局调度器处理玩家操作

```java
// ❌ 错误：可能在错误的线程上执行
Bukkit.getScheduler().runTask(plugin, () -> {
    player.teleport(location); // 可能失败
});

// ✅ 正确：使用实体调度器
SchedulerUtil.runEntityTask(plugin, player, () -> {
    player.teleport(location); // 保证在正确的线程
});
```

### 错误 2：忘记取消任务

```java
// ❌ 错误：任务无法取消
Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    if (!isActive()) {
        // 无法取消任务！
        return;
    }
    updateGame();
}, 0L, 20L);

// ✅ 正确：使用 GameTask
GameTask task = new GameTask() {
    @Override
    public void run() {
        if (!isActive()) {
            this.cancel(); // 可以取消
            return;
        }
        updateGame();
    }
}.runTaskTimer(plugin, 0L, 20L);
```

### 错误 3：在异步任务中操作游戏对象

```java
// ❌ 错误：在异步线程中操作方块
SchedulerUtil.runTaskAsynchronously(plugin, () -> {
    location.getBlock().setType(Material.AIR); // 线程不安全！
});

// ✅ 正确：使用同步任务
SchedulerUtil.runLocationTask(plugin, location, () -> {
    location.getBlock().setType(Material.AIR); // 线程安全
});
```

---

## 📝 迁移检查清单

### 代码审查
- [ ] 搜索所有 `BukkitRunnable` 并替换为 `GameTask`
- [ ] 搜索所有 `Bukkit.getScheduler()` 并替换为 `SchedulerUtil`
- [ ] 检查所有玩家操作是否使用 `runEntityTask*`
- [ ] 检查所有方块操作是否使用 `runLocationTask*`
- [ ] 检查所有数据库操作是否使用异步任务

### 功能测试
- [ ] 在 Paper 服务器上测试所有功能
- [ ] 在 Folia 服务器上测试所有功能
- [ ] 测试任务取消功能
- [ ] 测试跨区域功能
- [ ] 测试性能表现

---

## 🔧 工具方法

### 检测服务器类型

```java
if (SchedulerUtil.isFolia()) {
    // Folia 特定逻辑
} else {
    // Bukkit/Paper 逻辑
}
```

### 批量取消任务

```java
// 保存所有任务
List<GameTask> tasks = new ArrayList<>();

// 创建任务时添加到列表
GameTask task = new GameTask() {
    @Override
    public void run() {
        // ...
    }
}.runTaskTimer(plugin, 0L, 20L);
tasks.add(task);

// 游戏结束时取消所有任务
for (GameTask t : tasks) {
    t.cancel();
}
tasks.clear();
```

---

## 📚 相关文档

- [Folia 兼容性分析报告](./Folia兼容性分析报告.md)
- [Folia 兼容性开发指南](./Folia兼容性开发指南.md)
- [Folia 兼容性开发进度](./Folia兼容性开发进度.md)

---

**最后更新**：2026-01-22  
**版本**：1.0
