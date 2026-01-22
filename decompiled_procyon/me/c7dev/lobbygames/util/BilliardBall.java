// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import java.util.UUID;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;
import java.util.ArrayList;
import org.bukkit.Location;
import java.util.List;
import org.bukkit.util.Vector;
import me.c7dev.lobbygames.games.Pool;
import org.bukkit.entity.ArmorStand;

public class BilliardBall
{
    private int number;
    private ArmorStand as;
    private boolean sunk;
    private Pool game;
    private double v_progress;
    private Vector v;
    private List<Integer> collided;
    private List<Integer> collided_lastframe;
    private Location v_prog_loc;
    public Vector as_offset;
    public static double BALL_RADIUS;
    public static double BALL_DIAMETER;
    public static double VMAX;
    public static double VMIN;
    public static double FRICTION;
    public static double BOUNCE;
    public static double COLLIDE;
    
    static {
        BilliardBall.BALL_RADIUS = 0.0945;
        BilliardBall.BALL_DIAMETER = 0.189;
        BilliardBall.VMAX = 0.45;
        BilliardBall.VMIN = 0.0075;
        BilliardBall.FRICTION = 0.97;
        BilliardBall.BOUNCE = -0.72;
        BilliardBall.COLLIDE = 0.88;
    }
    
    public BilliardBall(final int number, final Location location, final Pool game) {
        this.sunk = false;
        this.v_progress = 0.0;
        this.v = new Vector(0, 0, 0);
        this.collided = new ArrayList<Integer>();
        this.collided_lastframe = new ArrayList<Integer>();
        this.v_prog_loc = null;
        this.as_offset = new Vector(0.333333, 0.66, -0.125);
        final String s = game.getWords()[0];
        this.number = number;
        location.setYaw(0.0f);
        location.setPitch(0.0f);
        this.game = game;
        if (game.isLegacyMode()) {
            this.as_offset.setY(this.as_offset.getY() - 0.125);
        }
        final ArmorStandFactory setSmall = new ArmorStandFactory(location.clone().add(this.as_offset)).setArms(true).setSmall(true);
        if (game.isLegacyMode()) {
            setSmall.setHandAngle(new EulerAngle(Math.toRadians(-41.0), Math.toRadians(40.0), Math.toRadians(-18.0)));
        }
        else {
            setSmall.setHandAngle(new EulerAngle(Math.toRadians(-15.0), 0.7853981633974483, Math.toRadians(1.0)));
        }
        this.as = setSmall.spawn();
        final int[] array = { 4, 11, 14, 10, 1, 5, 12, 15 };
        ItemStack itemStack = null;
        if (number == 0) {
            itemStack = new ItemStack(Material.QUARTZ_BLOCK);
        }
        else if (game.isSameColor()) {
            itemStack = GameUtils.createWool(1, (number == 8) ? 15 : ((number < 8) ? game.getSolidsSC() : game.getStripesSC()), "§f§l" + number + " " + s, new String[0]);
        }
        else if (number <= 8) {
            itemStack = GameUtils.createWool(1, array[number - 1], "§f§l" + number + " " + s, new String[0]);
        }
        else if (number <= 15) {
            itemStack = GameUtils.createClay(1, array[number - 9], "§f§l" + number + " " + s, new String[0]);
        }
        if (itemStack == null) {
            this.sunk = true;
            return;
        }
        if (LobbyGames.SERVER_VERSION < 12) {
            this.as.getEquipment().setItemInHand(itemStack);
        }
        else {
            this.as.getEquipment().setItemInMainHand(itemStack);
        }
    }
    
    public boolean tick() {
        final double length = this.v.length();
        if (length == 0.0) {
            return false;
        }
        if (length < BilliardBall.VMIN) {
            this.v = new Vector(0, 0, 0);
            this.game.checkInHole(this);
            return false;
        }
        if (length > BilliardBall.VMAX) {
            this.v.normalize().multiply(BilliardBall.VMAX);
        }
        if (this.v_progress >= length || length < BilliardBall.BALL_DIAMETER) {
            this.as.teleport(this.as.getLocation().add(this.v));
            this.v.multiply(BilliardBall.FRICTION);
            return false;
        }
        this.v_progress += BilliardBall.BALL_RADIUS;
        this.v_prog_loc = this.as.getLocation().add(this.v.clone().normalize().multiply(this.v_progress));
        return true;
    }
    
    public List<Integer> getCollided() {
        return this.collided;
    }
    
    public void shiftCollisionList() {
        this.v_progress = 0.0;
        this.v_prog_loc = null;
        this.collided_lastframe.clear();
        for (int i = 0; i < this.collided.size(); ++i) {
            this.collided_lastframe.add(this.collided.get(i));
        }
        this.collided.clear();
    }
    
    public boolean collide(final BilliardBall billiardBall) {
        if (this.number == billiardBall.getNumber() || Math.abs(this.fastLocation().getX() - billiardBall.fastLocation().getX()) > BilliardBall.BALL_DIAMETER || Math.abs(this.fastLocation().getZ() - billiardBall.fastLocation().getZ()) > BilliardBall.BALL_DIAMETER || this.fastLocation().distance(billiardBall.fastLocation()) > BilliardBall.BALL_DIAMETER || this.sunk || billiardBall.isSunk() || this.collided.contains(billiardBall.getNumber()) || this.collided_lastframe.contains(billiardBall.getNumber())) {
            return false;
        }
        if (this.v_progress > 0.0) {
            this.as.teleport(this.v_prog_loc);
        }
        final Vector normalize = billiardBall.getLocation().toVector().subtract(this.getLocation().toVector()).normalize();
        final double n = normalize.angle(this.v);
        final double n2 = normalize.clone().multiply(-1).angle(billiardBall.getVelocity());
        if (Double.isNaN(n)) {
            return false;
        }
        final double abs = Math.abs(BilliardBall.COLLIDE * (this.getVelocity().length() + billiardBall.getVelocity().length()) * Math.cos(Math.min(Math.abs(n), Double.isNaN(n2) ? 7.0 : Math.abs(n2))));
        billiardBall.setVelocity(billiardBall.getVelocity().add(normalize.clone().multiply(abs)));
        this.setVelocity(this.getVelocity().subtract(normalize.multiply(abs)));
        this.collided.add(billiardBall.getNumber());
        billiardBall.getCollided().add(this.number);
        return abs > 0.001;
    }
    
    public void sink() {
        if (this.sunk) {
            return;
        }
        this.sunk = true;
        this.v = new Vector(0, 0, 0);
        this.as.remove();
    }
    
    public int getNumber() {
        return this.number;
    }
    
    public boolean isSunk() {
        return this.sunk;
    }
    
    public UUID getUniqueId() {
        return this.as.getUniqueId();
    }
    
    public Location getLocation() {
        return this.as.getLocation().subtract(this.as_offset);
    }
    
    public Location fastLocation() {
        return (this.v_prog_loc == null) ? this.as.getLocation() : this.v_prog_loc;
    }
    
    public void setLocation(final Location location) {
        this.as.teleport(location.add(this.as_offset));
    }
    
    public void teleport(final Location location) {
        location.setYaw(0.0f);
        location.setPitch(0.0f);
        this.as.teleport(location.add(this.as_offset));
    }
    
    public Vector getVelocity() {
        return this.v;
    }
    
    public void setVelocity(final Vector vector) {
        this.v = vector.setY(0);
    }
}
