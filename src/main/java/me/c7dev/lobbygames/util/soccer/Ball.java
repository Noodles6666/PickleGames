// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util.soccer;

import org.bukkit.Effect;
import org.bukkit.World;
import java.util.UUID;
import me.c7dev.lobbygames.games.Soccer;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Ball
{
    private Vector v;
    private int size;
    private double vmax;
    private SoccerBallEntity entity;
    public static final double BOUNCE = -0.8;
    
    public Ball(final Location location, final Soccer soccer) {
        this.v = new Vector(0.0, -0.1, 0.0);
        this.size = soccer.getBallSize();
        this.vmax = soccer.getBallSpeed();
        (this.entity = soccer.getBallEntity()).setSize(this.size);
        this.entity.spawn(location);
    }
    
    public void remove() {
        this.entity.remove();
    }
    
    public void reload(final Location location) {
        if (this.entity.isValid()) {
            return;
        }
        this.entity.spawn(location);
    }
    
    public boolean containsUUID(final UUID uuid) {
        return this.entity.containsUUID(uuid);
    }
    
    public Location getLocation() {
        return this.entity.getLocation().add(0.0, -2.0, 0.0);
    }
    
    public World getWorld() {
        return this.entity.getWorld();
    }
    
    public Vector getVelocity() {
        return this.v;
    }
    
    public void tick(final double n) {
        final double length = this.v.length();
        if (length == 0.0) {
            return;
        }
        if (length > this.vmax) {
            this.v = this.v.normalize().multiply(this.vmax);
        }
        this.entity.teleport(this.entity.getLocation().clone().add(this.v));
        this.v.add(new Vector(0.0, -0.04, 0.0));
        if (this.v.getY() < 0.0 && this.entity.getLocation().getY() <= n + 0.25) {
            this.v.setY(-this.v.getY()).multiply(Math.abs(-0.8));
            if (this.v.length() <= 0.25) {
                this.v = new Vector(0, 0, 0);
                return;
            }
        }
        if (this.v.length() > this.vmax / 2.0) {
            this.getWorld().playEffect(this.getLocation().add(0.7, 1.7, 0.7), Effect.SMOKE, 0);
        }
    }
    
    public void setVelocity(final Vector v, final boolean b) {
        if (b) {
            this.v.add(v.normalize().setY(0.7).multiply(this.vmax / 2.1));
        }
        else {
            this.v = v;
        }
    }
    
    public void addGravity() {
        this.v.add(new Vector(0.0, -0.1, 0.0));
    }
}
