// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;
import org.bukkit.Location;
import me.c7dev.lobbygames.games.T048;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.ArmorStand;

public class T048Tile
{
    private int num;
    private int teleport_order;
    private ArmorStand as;
    private ItemStack[] tileblocks;
    private LobbyGames plugin;
    private CoordinatePair coords;
    public static final double Y_ADD = -1.75;
    public static final double Y_INC_MAX = 0.39;
    private double y_base;
    
    public T048Tile(final int a, final CoordinatePair coords, final T048 t048, final ItemStack[] tileblocks) {
        this.teleport_order = 0;
        final Location pixel = t048.getPixel(coords);
        this.num = a;
        this.tileblocks = tileblocks;
        this.plugin = t048.getPlugin();
        this.coords = coords;
        this.y_base = pixel.getY();
        pixel.add(0.5, -1.75 + 0.39 * (Math.min(a, tileblocks.length - 1) / (tileblocks.length - 1.0)), 0.5);
        this.as = new ArmorStandFactory(pixel).setName("§f§l" + (int)Math.pow(2.0, a + 1)).setHeadItem(tileblocks[Math.min(a, tileblocks.length - 1)]).spawn();
    }
    
    public Location getLocation() {
        return this.as.getLocation();
    }
    
    public double getLocY() {
        return this.y_base - 1.75 + 0.39 * (Math.min(this.num, this.tileblocks.length - 1) / (this.tileblocks.length - 1.0));
    }
    
    public void incr() {
        ++this.num;
        this.as.setCustomName("§f§l" + (int)Math.pow(2.0, this.num + 1));
        this.as.setHelmet(this.tileblocks[Math.min(this.num, this.tileblocks.length - 1)]);
        final Location location = this.as.getLocation();
        location.setY(this.getLocY());
        this.as.teleport(location);
    }
    
    public int getNum() {
        return this.num;
    }
    
    public void remove() {
        this.as.remove();
    }
    
    public CoordinatePair getCoords() {
        return this.coords;
    }
    
    public void setCoords(final CoordinatePair coords) {
        this.coords = coords;
    }
    
    public UUID getUniqueId() {
        if (this.as == null) {
            return null;
        }
        return this.as.getUniqueId();
    }
    
    public void teleport(final Location location, final T048Tile t048Tile) {
        final int n = 2;
        location.add(0.5, 0.0, 0.5);
        location.setY(this.getLocY());
        final Vector multiply = location.toVector().subtract(this.as.getLocation().toVector()).multiply(1.0 / n);
        ++this.teleport_order;
        new BukkitRunnable() {
            int ticks_done = 0;
            final int telo = T048Tile.this.teleport_order;
            
            public void run() {
                if (this.ticks_done >= n || T048Tile.this.teleport_order != this.telo) {
                    if (t048Tile != null) {
                        T048Tile.this.as.remove();
                        t048Tile.incr();
                    }
                    this.cancel();
                    return;
                }
                T048Tile.this.as.teleport(T048Tile.this.as.getLocation().add(multiply));
                ++this.ticks_done;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }
}
