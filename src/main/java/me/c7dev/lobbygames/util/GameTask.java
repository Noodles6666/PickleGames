package me.c7dev.lobbygames.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * 游戏任务包装类
 * 兼容 Folia 和 Bukkit 的可取消任务
 */
public abstract class GameTask implements Runnable {
    
    private Object task; // BukkitTask 或 Folia ScheduledTask
    private boolean cancelled = false;
    
    /**
     * 任务执行方法（子类实现）
     */
    public abstract void run();
    
    /**
     * 取消任务
     */
    public void cancel() {
        cancelled = true;
        if (task != null && !SchedulerUtil.isFolia()) {
            // 只在 Bukkit 上取消任务对象
            // Folia 任务通过 cancelled 标志自动停止
            if (task instanceof BukkitTask) {
                ((BukkitTask) task).cancel();
            }
        }
    }
    
    /**
     * 检查任务是否已取消
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * 运行同步任务
     */
    public GameTask runTask(Plugin plugin) {
        if (SchedulerUtil.isFolia()) {
            task = Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> {
                if (!cancelled) run();
            });
        } else {
            task = Bukkit.getScheduler().runTask(plugin, this);
        }
        return this;
    }
    
    /**
     * 运行延迟任务
     */
    public GameTask runTaskLater(Plugin plugin, long delay) {
        if (SchedulerUtil.isFolia()) {
            task = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> {
                if (!cancelled) run();
            }, delay);
        } else {
            task = Bukkit.getScheduler().runTaskLater(plugin, this, delay);
        }
        return this;
    }
    
    /**
     * 运行定时任务
     */
    public GameTask runTaskTimer(Plugin plugin, long delay, long period) {
        if (SchedulerUtil.isFolia()) {
            task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> {
                if (cancelled) {
                    scheduledTask.cancel();
                } else {
                    run();
                }
            }, delay, period);
        } else {
            task = Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period);
        }
        return this;
    }
    
    /**
     * 运行异步任务
     */
    public GameTask runTaskAsynchronously(Plugin plugin) {
        if (SchedulerUtil.isFolia()) {
            task = Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                if (!cancelled) run();
            });
        } else {
            task = Bukkit.getScheduler().runTaskAsynchronously(plugin, this);
        }
        return this;
    }
    
    /**
     * 运行延迟异步任务
     */
    public GameTask runTaskLaterAsynchronously(Plugin plugin, long delay) {
        if (SchedulerUtil.isFolia()) {
            task = Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> {
                if (!cancelled) run();
            }, delay * 50, java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, delay);
        }
        return this;
    }
    
    /**
     * 运行定时异步任务
     */
    public GameTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) {
        if (SchedulerUtil.isFolia()) {
            task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> {
                if (cancelled) {
                    scheduledTask.cancel();
                } else {
                    run();
                }
            }, delay * 50, period * 50, java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, delay, period);
        }
        return this;
    }
    
    /**
     * 在实体所在区域运行任务
     */
    public GameTask runEntityTask(Plugin plugin, Entity entity) {
        if (SchedulerUtil.isFolia()) {
            task = entity.getScheduler().run(plugin, scheduledTask -> {
                if (!cancelled) run();
            }, null);
        } else {
            task = Bukkit.getScheduler().runTask(plugin, this);
        }
        return this;
    }
    
    /**
     * 在实体所在区域运行延迟任务
     */
    public GameTask runEntityTaskLater(Plugin plugin, Entity entity, long delay) {
        if (SchedulerUtil.isFolia()) {
            task = entity.getScheduler().runDelayed(plugin, scheduledTask -> {
                if (!cancelled) run();
            }, null, delay);
        } else {
            task = Bukkit.getScheduler().runTaskLater(plugin, this, delay);
        }
        return this;
    }
    
    /**
     * 在实体所在区域运行定时任务
     */
    public GameTask runEntityTaskTimer(Plugin plugin, Entity entity, long delay, long period) {
        if (SchedulerUtil.isFolia()) {
            task = entity.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
                if (cancelled) {
                    scheduledTask.cancel();
                } else {
                    run();
                }
            }, null, delay, period);
        } else {
            task = Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period);
        }
        return this;
    }
    
    /**
     * 在指定位置所在区域运行任务
     */
    public GameTask runLocationTask(Plugin plugin, Location location) {
        if (SchedulerUtil.isFolia()) {
            task = Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> {
                if (!cancelled) run();
            });
        } else {
            task = Bukkit.getScheduler().runTask(plugin, this);
        }
        return this;
    }
    
    /**
     * 在指定位置所在区域运行延迟任务
     */
    public GameTask runLocationTaskLater(Plugin plugin, Location location, long delay) {
        if (SchedulerUtil.isFolia()) {
            task = Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> {
                if (!cancelled) run();
            }, delay);
        } else {
            task = Bukkit.getScheduler().runTaskLater(plugin, this, delay);
        }
        return this;
    }
    
    /**
     * 在指定位置所在区域运行定时任务
     */
    public GameTask runLocationTaskTimer(Plugin plugin, Location location, long delay, long period) {
        if (SchedulerUtil.isFolia()) {
            task = Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, scheduledTask -> {
                if (cancelled) {
                    scheduledTask.cancel();
                } else {
                    run();
                }
            }, delay, period);
        } else {
            task = Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period);
        }
        return this;
    }
}
