// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.games;

import java.lang.invoke.CallSite;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.StringConcatFactory;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import me.c7dev.lobbygames.api.events.PlayerQuitLobbyGameEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import java.util.Map;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import me.c7dev.lobbygames.util.GameUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import java.util.ArrayList;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.LobbyGames;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import me.c7dev.lobbygames.util.CoordinatePair;
import java.util.List;
import me.c7dev.lobbygames.util.ClickBlock;
import java.util.UUID;
import java.util.HashMap;
import org.bukkit.event.Listener;
import me.c7dev.lobbygames.Game;

public class Clicker extends Game implements Listener
{
    private HashMap<UUID, ClickBlock> blocks;
    private List<CoordinatePair> usedcoords;
    private int spawned;
    private int bpg;
    private Player p;
    private String score_format;
    private Location loc;
    
    public CoordinatePair getRandomOffset(final Random random) {
        final CoordinatePair coordinatePair = new CoordinatePair(random.nextInt(3) - 1, random.nextInt(3) - 1);
        if (coordinatePair.getX() == 0 && coordinatePair.getY() == 0) {
            return this.getRandomOffset(random);
        }
        if (this.usedcoords.size() < 8) {
            final Iterator<CoordinatePair> iterator = this.usedcoords.iterator();
            while (iterator.hasNext()) {
                if (coordinatePair.equals(iterator.next())) {
                    return this.getRandomOffset(random);
                }
            }
        }
        return coordinatePair;
    }
    
    public Clicker(final LobbyGames v1, final Arena v2, final Player returnLocation) {
        super(v1, GameType.CLICKER, v2, returnLocation);
        this.blocks = new HashMap<UUID, ClickBlock>();
        this.usedcoords = new ArrayList<CoordinatePair>();
        this.spawned = 0;
        this.bpg = 40;
        if (!this.canStart() || v2.getGameType() != GameType.CLICKER) {
            return;
        }
        this.p = returnLocation;
        this.loc = v2.getLocation1().add(0.5, 0.0, 0.5);
        v1.setReturnLocation(returnLocation);
        returnLocation.teleport(this.loc);
        this.preparePlayer(returnLocation);
        this.setActive(true);
        for (final Entity entity : this.loc.getWorld().getNearbyEntities(this.loc, 3.0, 3.0, 3.0)) {
            if (entity instanceof ArmorStand) {
                entity.remove();
            }
        }
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)v1);
        final int int1 = v1.getConfig().getInt("clicker.blocks-per-game");
        this.bpg = ((int1 == 0) ? 40 : int1);
        final String configString = v1.getConfigString(returnLocation, "clicker.start-msg", "");
        if (configString.length() > 0) {
            returnLocation.sendMessage(configString);
        }
        this.score_format = v1.getConfigString("clicker.score-format", "§2Score: §f%score% &6(%remaining% Remaining)");
        returnLocation.getInventory().setItem(8, v1.getQuitItem());
        new BukkitRunnable() {
            Random r = new Random();
            
            public void run() {
                if (Clicker.this.canStart() && Clicker.this.spawned < Clicker.this.bpg) {
                    final CoordinatePair randomOffset = Clicker.this.getRandomOffset(this.r);
                    final ClickBlock value = new ClickBlock(Clicker.this.loc.clone().add((double)randomOffset.getX(), -1.2, (double)randomOffset.getY()), v1);
                    Clicker.this.blocks.put(value.getArmorStand().getUniqueId(), value);
                    Clicker.this.usedcoords.add(randomOffset);
                    final Clicker this$0 = Clicker.this;
                    ++this$0.spawned;
                    value.setRemoveRunnable(new BukkitRunnable() {
                        public void run() {
                            Clicker.this.blocks.remove(value.getArmorStand().getUniqueId());
                            Clicker.this.usedcoords.remove(randomOffset);
                            if (!Clicker.this.isActive() && Clicker.this.blocks.size() == 0) {
                                Clicker.this.die();
                            }
                        }
                    });
                    if (Clicker.this.score_format.length() > 0) {
                        Clicker.this.getPlugin().sendActionbar(returnLocation, v1.sp(returnLocation, Clicker.this.actionBar()), true);
                    }
                }
                else {
                    this.cancel();
                    Clicker.this.setActive(false);
                }
            }
        }.runTaskTimer((Plugin)v1, 0L, 17L);
    }
    
    public void click(final ArmorStand armorStand) {
        final ClickBlock clickBlock = this.blocks.get(armorStand.getUniqueId());
        if (clickBlock == null || clickBlock.isClicked()) {
            return;
        }
        clickBlock.click();
        if (clickBlock.isGreen()) {
            ++this.score;
            this.p.playSound(this.p.getLocation(), GameUtils.getOrbPickupSound(), 1.0f, 8.0f);
        }
        else {
            this.score -= 3;
            this.p.playSound(this.p.getLocation(), GameUtils.getSound(11, "GLASS", "BLOCK_GLASS_BREAK"), 1.0f, 1.0f);
        }
        if (this.score_format.length() > 0) {
            this.getPlugin().sendActionbar(this.p, this.getPlugin().sp(this.p, this.actionBar()), true);
        }
    }
    
    private String actionBar() {
        final int n = this.getDuration() / 60;
        final String s = "" + this.getDuration() % 60;
        return this.score_format.replaceAll("\\Q%score%\\E", "" + this.score).replaceAll("\\Q%time%\\E", n + ":" + ((s.length() == 1) ? /* invokedynamic(!) */ProcyonInvokeDynamicHelper_2.invoke(s) : s)).replaceAll("\\Q%remaining%\\E", "" + (this.bpg - this.spawned));
    }
    
    @EventHandler
    public void onClick(final PlayerArmorStandManipulateEvent playerArmorStandManipulateEvent) {
        if (!this.canStart()) {
            return;
        }
        final boolean containsKey = this.blocks.containsKey(playerArmorStandManipulateEvent.getRightClicked().getUniqueId());
        final boolean equals = playerArmorStandManipulateEvent.getPlayer().getUniqueId().equals(this.p.getUniqueId());
        if (containsKey || equals) {
            playerArmorStandManipulateEvent.setCancelled(true);
        }
        if (containsKey && equals) {
            this.click(playerArmorStandManipulateEvent.getRightClicked());
        }
    }
    
    @EventHandler
    public void onLeftClick(final EntityDamageByEntityEvent entityDamageByEntityEvent) {
        if (!this.canStart()) {
            return;
        }
        if (entityDamageByEntityEvent.getDamager() instanceof Player && entityDamageByEntityEvent.getDamager().getUniqueId().equals(this.p.getUniqueId()) && entityDamageByEntityEvent.getEntity() instanceof ArmorStand && this.blocks.containsKey(entityDamageByEntityEvent.getEntity().getUniqueId())) {
            entityDamageByEntityEvent.setCancelled(true);
            this.click((ArmorStand)entityDamageByEntityEvent.getEntity());
        }
    }
    
    @EventHandler
    public void onInteractBlock(final PlayerInteractEvent playerInteractEvent) {
        if (!this.canStart()) {
            return;
        }
        if (playerInteractEvent.getPlayer().getUniqueId().equals(this.p.getUniqueId())) {
            final ItemStack handItem = GameUtils.getHandItem(playerInteractEvent.getPlayer());
            if (handItem != null && handItem.getType() == this.getPlugin().getQuitItem().getType() && this.getDuration() >= 1) {
                this.removePlayer(playerInteractEvent.getPlayer());
                playerInteractEvent.setCancelled(true);
            }
            else if ((playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK || playerInteractEvent.getAction() == Action.LEFT_CLICK_BLOCK) && playerInteractEvent.getBlockFace() == BlockFace.UP) {
                final Block clickedBlock = playerInteractEvent.getClickedBlock();
                if (clickedBlock == null) {
                    return;
                }
                final int a = this.loc.getBlockX() - clickedBlock.getX();
                final int a2 = this.loc.getBlockZ() - clickedBlock.getZ();
                if (Math.abs(a) <= 1 && Math.abs(a2) <= 1) {
                    final Iterator<Map.Entry<UUID, ClickBlock>> iterator = this.blocks.entrySet().iterator();
                    while (iterator.hasNext()) {
                        final ArmorStand armorStand = iterator.next().getValue().getArmorStand();
                        if (armorStand.getLocation().getBlockX() == clickedBlock.getX() && armorStand.getLocation().getBlockZ() == clickedBlock.getZ()) {
                            this.click(armorStand);
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onBreakBlock(final BlockBreakEvent blockBreakEvent) {
        if (!this.canStart()) {
            return;
        }
        if (blockBreakEvent.getPlayer().getUniqueId().equals(this.p.getUniqueId())) {
            blockBreakEvent.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBuild(final BlockPlaceEvent blockPlaceEvent) {
        if (!this.canStart()) {
            return;
        }
        if (blockPlaceEvent.getPlayer().getUniqueId().equals(this.p.getUniqueId())) {
            blockPlaceEvent.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onKick(final PlayerQuitLobbyGameEvent playerQuitLobbyGameEvent) {
        if (playerQuitLobbyGameEvent.getPlayer().getUniqueId().equals(this.p.getUniqueId()) && this.p.isOnline()) {
            this.getPlugin().teleportToSpawn(playerQuitLobbyGameEvent.getPlayer(), this.getArena());
            if (this.canStart()) {
                this.end();
            }
        }
    }
    
    public void die() {
        if (!this.canStart()) {
            return;
        }
        final String replaceAll = this.getPlugin().getConfigString(this.p, "clicker.win-msg", "\n§2§m----------------------------------------\n§a§lScore: §f" + this.score + "\n§2§m----------------------------------------").replaceAll("\\Q%score%\\E", "" + this.score);
        if (replaceAll.length() > 0) {
            this.p.sendMessage(replaceAll);
        }
        this.getPlugin().teleportToSpawn(this.p, this.getArena());
        this.addScore(this.p, this.score);
        Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(this.p, this, this.score));
        HandlerList.unregisterAll((Listener)this);
        this.end();
    }
    
    @EventHandler
    public void onEnd(final GameEndEvent gameEndEvent) {
        if (gameEndEvent.getGame().getGameType() == this.getGameType() && gameEndEvent.getGame().getArena().getID() == super.getArena().getID()) {
            HandlerList.unregisterAll((Listener)this);
        }
    }
    
    // This helper class was generated by Procyon to approximate the behavior of an
    // 'invokedynamic' instruction that it doesn't know how to interpret.
    private static final class ProcyonInvokeDynamicHelper_2
    {
        private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
        private static MethodHandle handle;
        private static volatile int fence;
        
        private static MethodHandle handle() {
            final MethodHandle handle = ProcyonInvokeDynamicHelper_2.handle;
            if (handle != null)
                return handle;
            return ProcyonInvokeDynamicHelper_2.ensureHandle();
        }
        
        private static MethodHandle ensureHandle() {
            ProcyonInvokeDynamicHelper_2.fence = 0;
            MethodHandle handle = ProcyonInvokeDynamicHelper_2.handle;
            if (handle == null) {
                MethodHandles.Lookup lookup = ProcyonInvokeDynamicHelper_2.LOOKUP;
                try {
                    handle = ((CallSite)StringConcatFactory.makeConcatWithConstants(lookup, "makeConcatWithConstants", MethodType.methodType(String.class, String.class), "0\u0001")).dynamicInvoker();
                }
                catch (Throwable t) {
                    throw new UndeclaredThrowableException(t);
                }
                ProcyonInvokeDynamicHelper_2.fence = 1;
                ProcyonInvokeDynamicHelper_2.handle = handle;
                ProcyonInvokeDynamicHelper_2.fence = 0;
            }
            return handle;
        }
        
        private static String invoke(String p0) {
            try {
                return (String) ProcyonInvokeDynamicHelper_2.handle().invokeExact(p0);
            }
            catch (Throwable t) {
                throw new UndeclaredThrowableException(t);
            }
        }
    }
}
