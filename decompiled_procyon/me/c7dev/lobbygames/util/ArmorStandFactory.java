// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import org.bukkit.entity.Entity;
import me.c7dev.lobbygames.LobbyGames;
import java.util.function.Consumer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;

public class ArmorStandFactory
{
    private Location loc;
    private String name;
    private boolean arms;
    private boolean small;
    private boolean visible;
    private ItemStack head_item;
    private ItemStack hand_item;
    private EulerAngle arm_angle;
    
    public ArmorStandFactory(final Location loc) {
        this.arms = false;
        this.small = false;
        this.visible = false;
        this.loc = loc;
    }
    
    public ArmorStandFactory setVisible(final boolean visible) {
        this.visible = visible;
        return this;
    }
    
    public ArmorStandFactory setName(final String name) {
        if (name.equals("empty")) {
            this.name = "§b";
        }
        else if (name != null && name.length() > 0) {
            this.name = name;
        }
        return this;
    }
    
    public ArmorStandFactory setArms(final boolean arms) {
        this.arms = arms;
        return this;
    }
    
    public ArmorStandFactory setSmall(final boolean small) {
        this.small = small;
        return this;
    }
    
    public ArmorStandFactory setHeadItem(final ItemStack head_item) {
        this.head_item = head_item;
        return this;
    }
    
    public ArmorStandFactory setHandItem(final ItemStack hand_item) {
        this.arms = true;
        this.hand_item = hand_item;
        return this;
    }
    
    public ArmorStandFactory setHandAngle(final EulerAngle arm_angle) {
        this.arms = true;
        this.arm_angle = arm_angle;
        return this;
    }
    
    public ArmorStand spawn() {
        final Consumer<ArmorStand> consumer = new Consumer<ArmorStand>() {
            @Override
            public void accept(final ArmorStand armorStand) {
                armorStand.setVisible(ArmorStandFactory.this.visible);
                armorStand.setGravity(false);
                armorStand.setAI(false);
                if (ArmorStandFactory.this.name != null && ArmorStandFactory.this.name.length() > 0) {
                    armorStand.setCustomName(ArmorStandFactory.this.name);
                    armorStand.setCustomNameVisible(true);
                }
                if (ArmorStandFactory.this.head_item != null) {
                    armorStand.setHelmet(ArmorStandFactory.this.head_item);
                }
                if (ArmorStandFactory.this.hand_item != null) {
                    armorStand.setItemInHand(ArmorStandFactory.this.hand_item);
                }
                if (ArmorStandFactory.this.arms) {
                    armorStand.setArms(true);
                }
                if (ArmorStandFactory.this.arm_angle != null) {
                    armorStand.setRightArmPose(ArmorStandFactory.this.arm_angle);
                }
                armorStand.setSmall(ArmorStandFactory.this.small);
            }
        };
        ArmorStand armorStand;
        if (LobbyGames.SERVER_VERSION <= 12) {
            armorStand = (ArmorStand)this.loc.getWorld().spawn(this.loc, (Class)ArmorStand.class);
            consumer.accept(armorStand);
        }
        else {
            armorStand = this.spawnT(this.loc, ArmorStand.class, consumer);
        }
        return armorStand;
    }
    
    private <T extends Entity> T spawnT(final Location location, final Class<T> clazz, final java.util.function.Consumer<T> consumer) {
        return location.getWorld().spawn(location, clazz, new org.bukkit.util.Consumer<T>() {
            public void accept(final T t) {
                consumer.accept(t);
            }
        });
    }
}
