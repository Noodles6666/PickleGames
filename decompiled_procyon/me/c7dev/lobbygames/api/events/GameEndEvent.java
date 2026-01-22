// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.api.events;

import java.util.Iterator;
import org.bukkit.Bukkit;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import me.c7dev.lobbygames.util.GameType;
import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.Game;
import org.bukkit.event.Event;

public class GameEndEvent extends Event
{
    private Game g;
    private static final HandlerList handlers;
    
    static {
        handlers = new HandlerList();
    }
    
    public GameEndEvent(final Game g) {
        this.g = g;
    }
    
    public Game getGame() {
        return this.g;
    }
    
    public GameType getGameType() {
        return this.g.getGameType();
    }
    
    public Player getPlayer() {
        return this.g.getPlayer1();
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
        return GameEndEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return GameEndEvent.handlers;
    }
}
