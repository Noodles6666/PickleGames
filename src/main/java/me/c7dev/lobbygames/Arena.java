// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames;

import org.bukkit.World;
import java.util.Iterator;
import java.util.Collection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;
import me.c7dev.lobbygames.util.Leaderboard;
import me.c7dev.lobbygames.util.GameType;
import org.bukkit.Location;

public class Arena
{
    private int id;
    private int width;
    private int height;
    private int rotation;
    private Location center;
    private Location s1;
    private Location l1;
    private Location l2;
    private Location spec1;
    private Location spec2;
    private boolean vertical;
    private boolean z_plane;
    private boolean valid_configuration;
    private boolean proximity_joining;
    private double proximity_range;
    private Game hosting;
    private GameType gt;
    private Leaderboard board;
    private Vector as_offset;
    private Vector generic_offset;
    
    public static boolean isValidCoords(final Location location, final Location location2) {
        if (location == null || location2 == null) {
            Bukkit.getLogger().warning("A coordinate was null when creating a LobbyGames arena!");
            return false;
        }
        int n = 0;
        if (location.getBlockX() != location2.getBlockX()) {
            ++n;
        }
        if (location.getBlockY() != location2.getBlockY()) {
            ++n;
        }
        if (location.getBlockZ() != location2.getBlockZ()) {
            ++n;
        }
        return n == 2 && location.getWorld().getName() == location2.getWorld().getName() && location2.getBlockY() >= location.getBlockY();
    }
    
    public Arena(final int n, final GameType gameType, final Location location, final Location location2, final Location location3, final Leaderboard leaderboard, final Location spec1, final Location spec2) {
        this(n, gameType, location, location2, location3, leaderboard);
        this.spec1 = spec1;
        this.spec2 = spec2;
    }
    
    public Arena(final int id, final GameType gt, final Location l1, final Location l2, final Location s1, final Leaderboard board) {
        this.rotation = 0;
        this.spec1 = null;
        this.spec2 = null;
        this.valid_configuration = false;
        this.proximity_joining = false;
        this.proximity_range = 5.0;
        this.hosting = null;
        this.board = null;
        this.id = id;
        this.s1 = s1;
        GameUtils.blockLocation(l1);
        GameUtils.blockLocation(l2);
        this.l1 = l1;
        this.l2 = l2;
        this.gt = gt;
        this.board = board;
        this.vertical = (l1.getY() != l2.getY());
        this.z_plane = (l1.getX() == l2.getX());
        this.width = (int)(this.z_plane ? Math.abs(l2.getZ() - l1.getZ()) : Math.abs(l2.getX() - l1.getX())) + 1;
        this.height = (int)(this.vertical ? Math.abs(l2.getY() - l1.getY()) : Math.abs(l2.getZ() - l1.getZ())) + 1;
        this.center = new Location(l1.getWorld(), (double)((l1.getBlockX() + l2.getBlockX()) / 2), (double)(this.vertical ? ((l1.getBlockY() + l2.getBlockY()) / 2) : l1.getBlockY()), (double)((l1.getBlockZ() + l2.getBlockZ()) / 2));
        if (!isValidCoords(l1, l2) && gt != GameType.CLICKER) {
            return;
        }
        this.generic_offset = new Vector(0, 1, 0);
        this.as_offset = new Vector(0.5, -0.75, 0.5);
        if (!this.vertical) {
            if (l2.getX() > l1.getX()) {
                this.rotation = ((l2.getZ() <= l1.getZ()) ? 1 : 0);
            }
            else {
                this.rotation = ((l2.getZ() > l1.getZ()) ? 3 : 2);
            }
        }
        else {
            if (this.z_plane) {
                this.rotation = ((l2.getZ() > l1.getZ()) ? 1 : 3);
            }
            else {
                this.rotation = ((l2.getX() > l1.getX()) ? 0 : 2);
            }
            switch (this.rotation) {
                case 1: {
                    this.generic_offset = new Vector(-1, 0, 0);
                    this.as_offset = new Vector(-0.3, -1.75, 0.5);
                    break;
                }
                case 2: {
                    this.generic_offset = new Vector(0, 0, -1);
                    this.as_offset = new Vector(0.5, -1.75, -0.3);
                    break;
                }
                case 3: {
                    this.generic_offset = new Vector(1, 0, 0);
                    this.as_offset = new Vector(1.2, -1.75, 0.5);
                    break;
                }
                default: {
                    this.generic_offset = new Vector(0, 0, 1);
                    this.as_offset = new Vector(0.5, -1.75, 1.2);
                    break;
                }
            }
        }
        this.valid_configuration = true;
    }
    
    public boolean isInBoundsXZ(final Location location) {
        if (this.l1.getX() < this.l2.getX()) {
            if (location.getBlockX() < this.l1.getX() || location.getBlockX() > this.l2.getX()) {
                return false;
            }
        }
        else if (location.getBlockX() > this.l1.getX() || location.getBlockX() < this.l2.getX()) {
            return false;
        }
        if (this.l1.getZ() < this.l2.getZ()) {
            if (location.getBlockZ() < this.l1.getZ() || location.getBlockZ() > this.l2.getZ()) {
                return false;
            }
        }
        else if (location.getBlockZ() > this.l1.getZ() || location.getBlockZ() < this.l2.getZ()) {
            return false;
        }
        return true;
    }
    
    public static boolean isInBoundsXZ(final Location location, final Location location2, final Location location3) {
        if (location2.getX() < location3.getX()) {
            if (location.getBlockX() < location2.getX() || location.getBlockX() > location3.getX()) {
                return false;
            }
        }
        else if (location.getBlockX() > location2.getX() || location.getBlockX() < location3.getX()) {
            return false;
        }
        if (location2.getZ() < location3.getZ()) {
            if (location.getBlockZ() < location2.getZ() || location.getBlockZ() > location3.getZ()) {
                return false;
            }
        }
        else if (location.getBlockZ() > location2.getZ() || location.getBlockZ() < location3.getZ()) {
            return false;
        }
        return true;
    }
    
    public boolean isInBounds(final Location location) {
        if (location.getBlockY() < this.l1.getY() || location.getBlockY() > this.l2.getY()) {
            return false;
        }
        if (this.l1.getX() < this.l2.getX()) {
            if (location.getBlockX() < this.l1.getX() || location.getBlockX() > this.l2.getX()) {
                return false;
            }
        }
        else if (location.getBlockX() > this.l1.getX() || location.getBlockX() < this.l2.getX()) {
            return false;
        }
        if (this.l1.getZ() < this.l2.getZ()) {
            if (location.getBlockZ() < this.l1.getZ() || location.getBlockZ() > this.l2.getZ()) {
                return false;
            }
        }
        else if (location.getBlockZ() > this.l1.getZ() || location.getBlockZ() < this.l2.getZ()) {
            return false;
        }
        return true;
    }
    
    public Location getTrueCenter() {
        int n;
        int n2;
        if (this.l1.getX() < this.l2.getX()) {
            n = this.l1.getBlockX();
            n2 = this.l2.getBlockX();
        }
        else {
            n = this.l2.getBlockX();
            n2 = this.l1.getBlockX();
        }
        int n3;
        int n4;
        if (this.l1.getY() < this.l2.getY()) {
            n3 = this.l1.getBlockY();
            n4 = this.l2.getBlockY();
        }
        else {
            n3 = this.l2.getBlockY();
            n4 = this.l1.getBlockY();
        }
        int n5;
        int n6;
        if (this.l1.getZ() < this.l2.getZ()) {
            n5 = this.l1.getBlockZ();
            n6 = this.l2.getBlockZ();
        }
        else {
            n5 = this.l2.getBlockZ();
            n6 = this.l1.getBlockZ();
        }
        ++n2;
        ++n4;
        ++n6;
        return new Location(this.l1.getWorld(), (n + n2) / 2.0, (n3 + n4) / 2.0, (n5 + n6) / 2.0);
    }
    
    public void clearArmorStands() {
        double n;
        double n2;
        if (this.gt == GameType.POOL) {
            n = 1.0;
            n2 = 1.0;
        }
        else {
            n = (((this.width & 0x1) == 0x1) ? 0.5 : 0.0);
            n2 = (((this.height & 0x1) == 0x1) ? 0.5 : 0.0);
        }
        final Location trueCenter = this.getTrueCenter();
        
        // 在 Folia 上，需要在区域线程中获取实体
        if (SchedulerUtil.isFolia()) {
            // 使用区域调度器在正确的线程中执行
            Bukkit.getRegionScheduler().run(LobbyGames.getInstance(), trueCenter, task -> {
                Collection<Entity> collection;
                if (this.isVerticalLayout()) {
                    if (this.generic_offset == null) {
                        return;
                    }
                    trueCenter.add(this.generic_offset);
                    if (this.rotation == 0 || this.rotation == 2) {
                        collection = this.l1.getWorld().getNearbyEntities(trueCenter, (int)(this.width / 2.0) + n, (int)(this.height / 2.0) + n2, 2.0);
                    }
                    else {
                        collection = this.l1.getWorld().getNearbyEntities(trueCenter, 2.0, (int)(this.height / 2.0) + n2, (int)(this.width / 2.0) + n);
                    }
                }
                else {
                    collection = this.l1.getWorld().getNearbyEntities(trueCenter, (int)(this.width / 2.0) + n, 1.0, (int)(this.height / 2.0) + n2);
                }
                for (final Entity entity : collection) {
                    if (entity instanceof ArmorStand) {
                        entity.remove();
                    }
                }
            });
        } else {
            // 非 Folia 服务器，直接执行
            Collection<Entity> collection;
            if (this.isVerticalLayout()) {
                if (this.generic_offset == null) {
                    return;
                }
                trueCenter.add(this.generic_offset);
                if (this.rotation == 0 || this.rotation == 2) {
                    collection = this.l1.getWorld().getNearbyEntities(trueCenter, (int)(this.width / 2.0) + n, (int)(this.height / 2.0) + n2, 2.0);
                }
                else {
                    collection = this.l1.getWorld().getNearbyEntities(trueCenter, 2.0, (int)(this.height / 2.0) + n2, (int)(this.width / 2.0) + n);
                }
            }
            else {
                collection = this.l1.getWorld().getNearbyEntities(trueCenter, (int)(this.width / 2.0) + n, 1.0, (int)(this.height / 2.0) + n2);
            }
            for (final Entity entity : collection) {
                if (entity instanceof ArmorStand) {
                    entity.remove();
                }
            }
        }
    }
    
    public int getID() {
        return this.id;
    }
    
    @Deprecated
    public void decrementID() {
        --this.id;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public Game getHostingGame() {
        return this.hosting;
    }
    
    public boolean isHostingGame() {
        return this.hosting != null;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof Arena && ((Arena)o).getID() == this.id;
    }
    
    public boolean isVerticalLayout() {
        return this.vertical;
    }
    
    public boolean isVerticalZAxis() {
        return this.z_plane;
    }
    
    public int getCoordinateRotation() {
        return this.rotation;
    }
    
    public World getWorld() {
        return this.l1.getWorld();
    }
    
    public Location getCenterPixel() {
        return this.center.clone();
    }
    
    public Vector getArmorStandOffset() {
        return this.as_offset.clone();
    }
    
    public Vector getGenericOffset() {
        return this.generic_offset.clone();
    }
    
    public Location getLocation1() {
        return this.l1.clone();
    }
    
    public Location getLocation2() {
        return this.l2.clone();
    }
    
    public Location getSpawn1() {
        return this.s1;
    }
    
    public Location getSpecialLoc1() {
        return this.spec1;
    }
    
    public Location getSpecialLoc2() {
        return this.spec2;
    }
    
    public Location getLeaderboardLocation() {
        return (this.board == null) ? null : this.board.getLocation();
    }
    
    public Leaderboard getLeaderboard() {
        return this.board;
    }
    
    public boolean isValidConfiguration() {
        return this.valid_configuration;
    }
    
    public GameType getGameType() {
        return this.gt;
    }
    
    public boolean isProximityJoiningEnabled() {
        return this.proximity_joining;
    }
    
    public void setProximityJoining(final boolean proximity_joining) {
        this.proximity_joining = proximity_joining;
    }
    
    public double getProximityRange() {
        return this.proximity_range;
    }
    
    public void setProximityRange(final double proximity_range) {
        this.proximity_range = Math.max(1.0, Math.min(20.0, proximity_range));
    }
    
    public void setHostingGame(final Game hosting) {
        this.hosting = hosting;
    }
}
