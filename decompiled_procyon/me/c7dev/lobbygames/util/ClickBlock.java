// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.util.Random;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.ArmorStand;

public class ClickBlock
{
    private boolean green;
    private boolean clicked;
    private ArmorStand as;
    private BukkitRunnable onRemove;
    
    public ClickBlock(final Location location, final LobbyGames lobbyGames) {
        this.green = true;
        this.clicked = false;
        this.as = null;
        this.onRemove = null;
        this.green = (new Random().nextDouble() < 0.6);
        this.as = new ArmorStandFactory(location).setName(this.green ? lobbyGames.getConfigString("clicker.green-blocks", "§aClick!") : lobbyGames.getConfigString("clicker.red-blocks", "§cDon't Click!")).setHeadItem((LobbyGames.SERVER_VERSION <= 12) ? new ItemStack(Material.valueOf("WOOL"), 1, (short)(this.green ? 5 : 14)) : new ItemStack(this.green ? Material.LIME_WOOL : Material.RED_WOOL, 1)).spawn();
        int n = (int)(lobbyGames.getConfig().getDouble("clicker.seconds-in-air") * 10.0);
        if (n < 8) {
            n = 8;
        }
        if (n > 45) {
            n = 45;
        }
        final int n2 = n;
        new BukkitRunnable() {
            int i = 0;
            private final /* synthetic */ double val$delta = 1.0 / n2;
            
            public void run() {
                if (this.i < n2) {
                    ClickBlock.this.as.teleport(ClickBlock.this.as.getLocation().add(0.0, this.val$delta, 0.0));
                    ++this.i;
                }
                else if (this.i < n2 << 1) {
                    ClickBlock.this.as.teleport(ClickBlock.this.as.getLocation().add(0.0, -this.val$delta, 0.0));
                    ++this.i;
                }
                else {
                    ClickBlock.this.as.remove();
                    this.cancel();
                    if (ClickBlock.this.onRemove != null) {
                        ClickBlock.this.onRemove.runTask((Plugin)lobbyGames);
                    }
                }
            }
        }.runTaskTimer((Plugin)lobbyGames, 0L, 1L);
    }
    
    public boolean isGreen() {
        return this.green;
    }
    
    public boolean isClicked() {
        return this.clicked;
    }
    
    public ArmorStand getArmorStand() {
        return this.as;
    }
    
    public void setRemoveRunnable(final BukkitRunnable onRemove) {
        this.onRemove = onRemove;
    }
    
    public void click() {
        if (this.clicked) {
            return;
        }
        this.clicked = true;
        this.as.setCustomName(this.green ? "§a§l+1" : "§c§l-3");
        this.as.setCustomNameVisible(true);
        this.as.setHelmet((LobbyGames.SERVER_VERSION <= 12) ? new ItemStack(Material.valueOf("WOOL"), 1, (short)8) : new ItemStack(Material.LIGHT_GRAY_WOOL, 1));
    }
}
