# Folia 兼容性分析报告

## 📋 概述

**结论**：❌ **当前版本不兼容 Folia 1.21.1**

这个插件是基于传统的 Spigot/Paper API 开发的，使用了大量 Folia 不支持的全局调度器 API。

---

## 🔍 Folia 简介

### 什么是 Folia？

Folia 是 Paper 的一个实验性分支，专为**多线程区域化**设计：

- **区域化世界**：将世界分成多个独立区域，每个区域在不同线程上运行
- **更高性能**：可以利用多核 CPU，提升大型服务器性能
- **API 变化**：需要使用新的调度器 API，不再支持全局调度器

### Folia 的主要变化

1. **移除全局调度器**：`Bukkit.getScheduler()` 不再可用
2. **区域化调度**：必须使用实体/位置相关的调度器
3. **线程安全**：需要确保代码在正确的区域线程上执行

---

## ❌ 不兼容的原因

### 1. 大量使用 BukkitRunnable

插件中有 **100+ 处**使用了 `BukkitRunnable`，这在 Folia 中需要替换为区域化调度器。

**示例位置**：
- `LobbyGames.java` - 主循环任务
- `TicTacToe.java` - 回合提示任务
- `Gomoku.java` - 回合提示任务
- `Soccer.java` - 倒计时任务
- `Pool.java` - 粒子效果任务
- `Spleef.java` - 方块融化任务
- `Snake.java` - 游戏循环任务
- `Minesweeper.java` - 计时器任务
- 等等...

**传统 API（不兼容 Folia）**：
```java
new BukkitRunnable() {
    public void run() {
        // 游戏逻辑
    }
}.runTaskTimer(plugin, 0L, 20L);
```

**Folia 需要的 API**：
```java
// 实体调度器
entity.getScheduler().runAtFixedRate(plugin, task -> {
    // 游戏逻辑
}, null, 1L, 20L);

// 位置调度器
Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, task -> {
    // 游戏逻辑
}, 1L, 20L);
```

### 2. 使用全局调度器

**示例**：`Minesweeper.java`
```java
Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, new Runnable() {
    @Override
    public void run() {
        // 延迟任务
    }
});
```

在 Folia 中，这会直接抛出异常。

### 3. 跨区域操作

插件中的某些功能可能涉及跨区域操作：
- 玩家传送到竞技场
- 多个玩家同时参与游戏
- 全局排行榜更新

这些在 Folia 中需要特殊处理，确保在正确的区域线程上执行。

---

## 🔧 兼容 Folia 需要的修改

### 1. 替换所有 BukkitRunnable

**工作量**：⭐⭐⭐⭐⭐ 非常大（100+ 处）

需要将所有 `BukkitRunnable` 替换为 Folia 的区域化调度器：

```java
// 之前
new BukkitRunnable() {
    public void run() {
        player.sendMessage("消息");
    }
}.runTaskLater(plugin, 20L);

// 之后（Folia）
player.getScheduler().runDelayed(plugin, task -> {
    player.sendMessage("消息");
}, null, 20L);
```

### 2. 添加 Folia 检测和兼容层

**工作量**：⭐⭐⭐ 中等

创建一个调度器包装类，自动检测是否运行在 Folia 上：

```java
public class SchedulerUtil {
    private static boolean isFolia;
    
    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }
    }
    
    public static void runTaskLater(Plugin plugin, Runnable task, long delay) {
        if (isFolia) {
            // 使用 Folia API
        } else {
            // 使用传统 Bukkit API
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
}
```

### 3. 处理跨区域操作

**工作量**：⭐⭐⭐⭐ 较大

确保所有涉及多个玩家或位置的操作都在正确的区域线程上执行。

### 4. 更新 pom.xml

添加 Folia API 依赖：

```xml
<dependency>
    <groupId>dev.folia</groupId>
    <artifactId>folia-api</artifactId>
    <version>1.21.1-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

---

## 📊 兼容性对比

| 服务器类型 | 兼容性 | 说明 |
|-----------|--------|------|
| Spigot | ✅ 完全兼容 | 原生支持 |
| Paper | ✅ 完全兼容 | Paper 是 Spigot 的增强版 |
| Purpur | ✅ 完全兼容 | Purpur 基于 Paper |
| **Folia** | ❌ **不兼容** | 需要大量修改 |

---

## 🎯 推荐方案

### 方案 1：使用 Paper 而不是 Folia（推荐）

**优点**：
- ✅ 无需修改代码
- ✅ 立即可用
- ✅ 稳定可靠

**缺点**：
- ❌ 无法利用 Folia 的多线程优势

**适用场景**：
- 中小型服务器（< 200 玩家）
- 不需要极致性能优化
- 希望稳定运行

### 方案 2：等待官方 Folia 支持

**优点**：
- ✅ 官方支持，质量有保证
- ✅ 持续维护

**缺点**：
- ❌ 需要等待开发者更新
- ❌ 时间不确定

**适用场景**：
- 不急于使用 Folia
- 愿意等待官方更新

### 方案 3：自行修改以支持 Folia（不推荐）

**优点**：
- ✅ 可以立即使用 Folia
- ✅ 可以定制功能

**缺点**：
- ❌ 工作量巨大（预计 40+ 小时）
- ❌ 需要深入了解 Folia API
- ❌ 维护成本高
- ❌ 可能引入新 Bug

**工作量估算**：
1. 替换所有 BukkitRunnable：20 小时
2. 创建兼容层：8 小时
3. 处理跨区域操作：10 小时
4. 测试和调试：12 小时
5. **总计**：约 50 小时

---

## 🔍 Folia 适用场景

Folia 并不适合所有服务器，以下情况才建议使用：

### ✅ 适合使用 Folia

- 大型服务器（500+ 玩家）
- 多个独立世界/区域
- 需要极致性能优化
- 有专业技术团队维护

### ❌ 不适合使用 Folia

- 中小型服务器（< 200 玩家）
- 使用大量传统插件
- 没有技术团队支持
- 追求稳定性而非性能

---

## 📝 技术细节

### Folia 调度器 API 对比

| 功能 | Bukkit/Paper | Folia |
|------|-------------|-------|
| 延迟任务 | `Bukkit.getScheduler().runTaskLater()` | `entity.getScheduler().runDelayed()` |
| 定时任务 | `Bukkit.getScheduler().runTaskTimer()` | `entity.getScheduler().runAtFixedRate()` |
| 异步任务 | `Bukkit.getScheduler().runTaskAsynchronously()` | `Bukkit.getAsyncScheduler().runNow()` |
| 全局任务 | `Bukkit.getScheduler().runTask()` | ❌ 不支持，必须指定区域 |

### 示例：游戏循环改造

**之前（Bukkit）**：
```java
new BukkitRunnable() {
    public void run() {
        if (!isActive()) {
            this.cancel();
            return;
        }
        updateGame();
    }
}.runTaskTimer(plugin, 0L, 20L);
```

**之后（Folia 兼容）**：
```java
if (isFolia()) {
    // Folia: 使用玩家调度器
    player.getScheduler().runAtFixedRate(plugin, task -> {
        if (!isActive()) {
            task.cancel();
            return;
        }
        updateGame();
    }, null, 1L, 20L);
} else {
    // Bukkit/Paper: 使用传统调度器
    new BukkitRunnable() {
        public void run() {
            if (!isActive()) {
                this.cancel();
                return;
            }
            updateGame();
        }
    }.runTaskTimer(plugin, 0L, 20L);
}
```

---

## 🎯 总结

### 当前状态
- ❌ **不兼容 Folia 1.21.1**
- ✅ **完全兼容 Spigot/Paper/Purpur**

### 建议
1. **如果你的服务器 < 200 玩家**：使用 Paper，无需 Folia
2. **如果你需要 Folia**：等待官方支持或联系开发者
3. **如果你有技术团队**：可以考虑自行修改（工作量约 50 小时）

### Folia 现状
- Folia 仍处于**实验阶段**
- 大多数插件尚未支持 Folia
- 生态系统不够成熟
- 建议等待 Folia 稳定后再考虑迁移

---

## 📚 参考资源

- [Folia 官方文档](https://docs.papermc.io/folia)
- [Folia GitHub](https://github.com/PaperMC/Folia)
- [Folia 插件开发指南](https://docs.papermc.io/folia/reference/region-logic)
- [Paper vs Folia 对比](https://docs.papermc.io/folia/reference/overview)

---

**报告生成时间**：2026-01-20  
**分析版本**：PickleGames v1.0  
**Folia 版本**：1.21.1
