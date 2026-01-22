// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util.soccer;

import org.bukkit.entity.Player;
import java.util.function.Consumer;
import org.bukkit.World;
import java.util.UUID;
import org.bukkit.Location;

public interface SoccerBallEntity
{
    void setSize(final int p0);
    
    void spawn(final Location p0);
    
    void teleport(final Location p0);
    
    void remove();
    
    boolean isValid();
    
    boolean containsUUID(final UUID p0);
    
    Location getLocation();
    
    World getWorld();
    
    void setClickConsumer(final Consumer<Player> p0);
}
