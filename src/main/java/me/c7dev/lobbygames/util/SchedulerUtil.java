package me.c7dev.lobbygames.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Folia 兼容调度器工具类
 * 自动检测服务器类型并使用相应的调度器 API
 */
public class SchedulerUtil {
    
    private static Boolean isFolia = null;
    
    /**
     * 检测是否运行在 Folia 服务器上
     */
    public static boolean isFolia() {
        if (isFolia == null) {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                isFolia = true;
            } catch (ClassNotFoundException e) {
                isFolia = false;
            }
        }
        return isFolia;
    }
    
    /**
     * 运行同步任务
     */
    public static void runTask(Plugin plugin, Runnable task) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * 运行延迟任务
     */
    public static void runTaskLater(Plugin plugin, Runnable task, long delay) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * 运行定时任务（全局）
     */
    public static void runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }
    
    /**
     * 运行异步任务
     */
    public static void runTaskAsynchronously(Plugin plugin, Runnable task) {
        if (isFolia()) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
    
    /**
     * 运行延迟异步任务
     */
    public static void runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay) {
        if (isFolia()) {
            Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), 
                delay * 50, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
        }
    }
    
    /**
     * 运行定时异步任务
     */
    public static void runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period) {
        if (isFolia()) {
            Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), 
                delay * 50, period * 50, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
        }
    }
    
    /**
     * 在实体所在区域运行任务
     */
    public static void runEntityTask(Plugin plugin, Entity entity, Runnable task) {
        if (isFolia()) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * 在实体所在区域运行延迟任务
     */
    public static void runEntityTaskLater(Plugin plugin, Entity entity, Runnable task, long delay) {
        if (isFolia()) {
            entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * 在实体所在区域运行定时任务
     */
    public static void runEntityTaskTimer(Plugin plugin, Entity entity, Runnable task, long delay, long period) {
        if (isFolia()) {
            entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), null, delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }
    
    /**
     * 在指定位置所在区域运行任务
     */
    public static void runLocationTask(Plugin plugin, Location location, Runnable task) {
        if (isFolia()) {
            Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * 在指定位置所在区域运行延迟任务
     */
    public static void runLocationTaskLater(Plugin plugin, Location location, Runnable task, long delay) {
        if (isFolia()) {
            Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * 在指定位置所在区域运行定时任务
     */
    public static void runLocationTaskTimer(Plugin plugin, Location location, Runnable task, long delay, long period) {
        if (isFolia()) {
            Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, scheduledTask -> task.run(), delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }
    
    /**
     * 取消所有任务
     */
    public static void cancelTasks(Plugin plugin) {
        if (!isFolia()) {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
        // Folia 中任务通过返回的 ScheduledTask 对象取消
    }
    
    /**
     * 安全传送实体（兼容 Folia）
     * 在 Folia 上使用 teleportAsync，在 Bukkit 上使用同步 teleport
     */
    public static void teleport(Entity entity, Location location) {
        if (isFolia()) {
            entity.teleportAsync(location);
        } else {
            entity.teleport(location);
        }
    }
}
