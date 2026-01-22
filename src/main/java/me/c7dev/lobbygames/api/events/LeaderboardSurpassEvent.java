// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.api.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.util.Leaderboard;
import java.util.UUID;
import org.bukkit.event.Event;

public class LeaderboardSurpassEvent extends Event
{
    private UUID u;
    private UUID surpassed_uuid;
    private int new_rank;
    private int surpassed_old_rank;
    private Leaderboard lb;
    private static final HandlerList handlers;
    
    static {
        handlers = new HandlerList();
    }
    
    public LeaderboardSurpassEvent(final Leaderboard lb, final int surpassed_old_rank, final int new_rank, final UUID surpassed_uuid, final UUID u) {
        this.lb = lb;
        this.new_rank = new_rank;
        this.surpassed_uuid = surpassed_uuid;
        this.u = u;
        this.surpassed_old_rank = surpassed_old_rank;
    }
    
    public Leaderboard getLeaderboard() {
        return this.lb;
    }
    
    public int getPlayerNewRank() {
        return this.new_rank + 1;
    }
    
    public Integer getSurpassedPlayerOldRank() {
        return this.surpassed_old_rank + 1;
    }
    
    public UUID getPlayerUUID() {
        return this.u;
    }
    
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(this.u);
    }
    
    public UUID getSurpassedPlayerUUID() {
        return this.surpassed_uuid;
    }
    
    public OfflinePlayer getSurpassedPlayer() {
        return (this.surpassed_uuid == null) ? null : Bukkit.getOfflinePlayer(this.surpassed_uuid);
    }
    
    public HandlerList getHandlers() {
        return LeaderboardSurpassEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return LeaderboardSurpassEvent.handlers;
    }
}
