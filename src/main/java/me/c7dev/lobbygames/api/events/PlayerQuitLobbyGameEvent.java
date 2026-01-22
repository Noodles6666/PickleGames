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
import org.bukkit.entity.Player;
import me.c7dev.lobbygames.Game;
import org.bukkit.event.Event;

public class PlayerQuitLobbyGameEvent extends Event
{
    Game g;
    Player p;
    private static final HandlerList handlers;
    
    static {
        handlers = new HandlerList();
    }
    
    public PlayerQuitLobbyGameEvent(final Player p2, final Game g) {
        this.g = g;
        this.p = p2;
    }
    
    public Game getGame() {
        return this.g;
    }
    
    public GameType getGameType() {
        return this.g.getGameType();
    }
    
    public Player getPlayer() {
        return this.p;
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
        return PlayerQuitLobbyGameEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PlayerQuitLobbyGameEvent.handlers;
    }
}
