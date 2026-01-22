// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util.soccer;

import org.bukkit.entity.Player;
import java.util.function.Consumer;
import org.bukkit.World;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Slime;

public class SlimeSoccerBall implements SoccerBallEntity
{
    private Slime slime;
    private int size;
    
    public SlimeSoccerBall() {
        this.size = 3;
    }
    
    @Override
    public void spawn(final Location location) {
        this.remove();
        (this.slime = (Slime)location.getWorld().spawn(location, (Class)Slime.class)).setSize(this.size);
        this.slime.setAI(false);
    }
    
    @Override
    public void teleport(final Location location) {
        this.slime.teleportAsync(location);
    }
    
    @Override
    public void remove() {
        if (this.slime != null) {
            this.slime.remove();
        }
    }
    
    @Override
    public boolean isValid() {
        return this.slime != null && this.slime.isValid();
    }
    
    @Override
    public boolean containsUUID(final UUID obj) {
        return this.slime.getUniqueId().equals(obj);
    }
    
    @Override
    public Location getLocation() {
        return this.slime.getLocation();
    }
    
    @Override
    public World getWorld() {
        return this.slime.getWorld();
    }
    
    @Override
    public void setSize(final int n) {
        this.size = n;
        if (this.slime != null) {
            this.slime.setSize(n);
        }
    }
    
    @Override
    public void setClickConsumer(final Consumer<Player> consumer) {
    }
}
