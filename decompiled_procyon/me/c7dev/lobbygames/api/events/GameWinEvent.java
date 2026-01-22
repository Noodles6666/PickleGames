// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.api.events;

import java.util.Iterator;
import org.bukkit.Bukkit;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import me.c7dev.lobbygames.util.GameType;
import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class GameWinEvent extends Event
{
    double score;
    Player p;
    Game g;
    private static final HandlerList handlers;
    
    static {
        handlers = new HandlerList();
    }
    
    public GameWinEvent(final Player p3, final Game g, final double score) {
        this.g = g;
        this.p = p3;
        this.score = score;
    }
    
    public double getScore() {
        return this.score;
    }
    
    public Player getPlayer() {
        return this.p;
    }
    
    public Game getGame() {
        return this.g;
    }
    
    public GameType getGameType() {
        return this.g.getGameType();
    }
    
    public int getDuration() {
        return this.g.getDuration();
    }
    
    public List<Player> getPlayers() {
        final ArrayList list = new ArrayList();
        final Iterator<UUID> iterator = this.g.getPlayers().iterator();
        while (iterator.hasNext()) {
            final Player player = Bukkit.getPlayer((UUID)iterator.next());
            if (player != null) {
                list.add(player);
            }
        }
        return list;
    }
    
    public HandlerList getHandlers() {
        return GameWinEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return GameWinEvent.handlers;
    }
}
