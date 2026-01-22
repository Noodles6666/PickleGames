// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.games;

import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import org.bukkit.Particle;
import org.bukkit.event.EventPriority;
import org.bukkit.block.Block;
import java.util.Set;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.DyeColor;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import me.c7dev.lobbygames.util.ArmorStandFactory;
import org.bukkit.Sound;
import java.util.Iterator;
import org.bukkit.scheduler.BukkitRunnable;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.util.GameTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import me.c7dev.lobbygames.util.GameType;
import org.bukkit.entity.Player;
import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.entity.ArmorStand;
import java.util.HashMap;
import me.c7dev.lobbygames.util.CoordinatePair;
import java.util.List;
import org.bukkit.event.Listener;
import me.c7dev.lobbygames.Game;

public class Minesweeper extends Game implements Listener
{
    private List<CoordinatePair> mines;
    private List<CoordinatePair> opened;
    private List<CoordinatePair> flagged;
    private HashMap<CoordinatePair, ArmorStand[]> flagged_armorstand;
    private boolean can_fly;
    private boolean legacy_mode;
    private boolean flagdecor;
    private int starting_mines;
    private long clickdelay;
    LobbyGames plugin;
    
    public Minesweeper(final LobbyGames lobbyGames, final Arena v3, final Player v4) {
        super(lobbyGames, GameType.MINESWEEPER, v3, v4);
        this.mines = new ArrayList<CoordinatePair>();
        this.opened = new ArrayList<CoordinatePair>();
        this.flagged = new ArrayList<CoordinatePair>();
        this.flagged_armorstand = new HashMap<CoordinatePair, ArmorStand[]>();
        this.can_fly = false;
        this.legacy_mode = false;
        this.flagdecor = true;
        this.clickdelay = System.currentTimeMillis();
        if (!super.canStart() || v3.getGameType() != GameType.MINESWEEPER) {
            return;
        }
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)lobbyGames);
        this.plugin = lobbyGames;
        double double1 = lobbyGames.getConfig().getDouble("minesweeper.flag-landmine-distribution");
        if (double1 != 0.0) {
            if (double1 < 0.1) {
                double1 = 0.1;
            }
            if (double1 > 0.3) {
                double1 = 0.3;
            }
        }
        else {
            double1 = 0.16666666666666666;
        }
        this.starting_mines = (int)(super.getArena().getWidth() * super.getArena().getHeight() * double1);
        this.legacy_mode = (LobbyGames.SERVER_VERSION == 8 && lobbyGames.getConfig().getBoolean("legacy-mode"));
        final String configString = lobbyGames.getConfigString(v4, "minesweeper.start-msg", "§3§m----------------------------------------\n§b§lMinesweeper: §bThere are " + this.starting_mines + " landmines randomly spread through the grid! Right-click to open a cell, and use the flag tool to mark a landmine. The numbers represent how many mines a cell is touching!\n§3§m----------------------------------------");
        if (configString.length() > 0) {
            this.getPlayer1().sendMessage(configString.replaceAll("\\Q%starting_mines%\\E", "" + this.starting_mines));
        }
        this.flagdecor = lobbyGames.getConfig().getBoolean("minesweeper.flag-armor-stands-enabled");
        this.start();
    }
    
    public void start() {
        this.setActive(true);
        this.reset();
        this.can_fly = this.getPlayer1().getAllowFlight();
        if (this.plugin.getConfig().getBoolean("minesweeper.player-can-fly")) {
            this.getPlayer1().setAllowFlight(true);
        }
        if (super.getArena().getSpawn1() != null) {
            this.plugin.setReturnLocation(this.getPlayer1());
            this.getPlayer1().teleportAsync(super.getArena().getSpawn1());
        }
        this.preparePlayer(this.getPlayer1());
        this.getPlayer1().getInventory().setItem(0, GameUtils.createItem(Material.BLAZE_ROD, 1, (byte)0, this.getPlugin().getConfigString("minesweeper.wand-title", "§c§lSet Flag"), new String[0]));
        this.getPlayer1().getInventory().setItem(8, this.plugin.getQuitItem());
        new GameTask() {
            public void run() {
                if (!Minesweeper.this.isActive()) {
                    this.cancel();
                    return;
                }
                Minesweeper.this.updateActionBar();
                final Minesweeper this$0 = Minesweeper.this;
                Minesweeper.access$1(this$0, this$0.score + 1);
            }
        }.runTaskTimer((Plugin)this.plugin, 1L, 20L);
    }
    
    public CoordinatePair createMine(final CoordinatePair coordinatePair) {
        final CoordinatePair randomPixel = super.randomPixel();
        for (final CoordinatePair coordinatePair2 : this.mines) {
            if (coordinatePair2.getX() == randomPixel.getX() && coordinatePair2.getY() == randomPixel.getY()) {
                return this.createMine(coordinatePair);
            }
        }
        if (Math.abs(coordinatePair.getX() - randomPixel.getX()) <= 1 && Math.abs(coordinatePair.getY() - randomPixel.getY()) <= 1) {
            return this.createMine(coordinatePair);
        }
        return randomPixel;
    }
    
    public byte getByte(final int n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 11;
        }
        if (n == 2) {
            return 13;
        }
        if (n == 3) {
            return 14;
        }
        if (n == 4) {
            return 10;
        }
        if (n == 5) {
            return 12;
        }
        if (n >= 6) {
            return 15;
        }
        return 0;
    }
    
    public void addFlag(final CoordinatePair key) {
        if (this.flagged.size() >= this.starting_mines + 10 || this.mines.size() == 0) {
            return;
        }
        final Location pixel = this.getPixel(key);
        if (!this.getArena().isInBounds(pixel)) {
            return;
        }
        this.flagged.add(key);
        pixel.getBlock().setType(Material.valueOf((LobbyGames.SERVER_VERSION <= 12) ? "STEP" : "SMOOTH_STONE_SLAB"));
        pixel.add(0.0, -1.0, 0.0);
        this.checkWin();
        this.updateActionBar();
        if (LobbyGames.SERVER_VERSION > 8) {
            this.getPlayer1().playSound(pixel, Sound.BLOCK_GRASS_PLACE, 1.0f, 1.0f);
        }
        if (!this.flagdecor) {
            return;
        }
        final ArmorStand spawn = new ArmorStandFactory(pixel.clone().add(0.85, 0.6, 1.0)).setHandAngle(new EulerAngle(Math.toRadians(81.0), 0.0, 0.0)).setHandItem(new ItemStack(Material.STICK)).spawn();
        final Location clone = pixel.clone();
        clone.setYaw(90.0f);
        if (!this.legacy_mode) {
            clone.add(0.85, 1.51, 0.717);
        }
        else {
            clone.add(1.02, 1.28, (clone.getZ() > 0.0) ? 0.737 : 0.717);
        }
        final ItemStack handItem = new ItemStack(Material.valueOf((LobbyGames.SERVER_VERSION <= 12) ? "BANNER" : "RED_BANNER"), 1);
        final BannerMeta itemMeta = (BannerMeta)handItem.getItemMeta();
        if (LobbyGames.SERVER_VERSION <= 12) {
            // For legacy versions, set the banner color
            try {
                itemMeta.getClass().getMethod("setBaseColor", DyeColor.class).invoke(itemMeta, DyeColor.RED);
            } catch (Exception e) {
                // Ignore if method doesn't exist
            }
        }
        handItem.setItemMeta((ItemMeta)itemMeta);
        final ArmorStandFactory setArms = new ArmorStandFactory(clone).setHandItem(handItem).setSmall(true).setArms(true);
        if (!this.legacy_mode) {
            setArms.setHandAngle(new EulerAngle(0.0, 0.0, 0.0));
        }
        else {
            setArms.setHandAngle(new EulerAngle(Math.toRadians(340.0), 0.0, 0.0));
        }
        this.flagged_armorstand.put(key, new ArmorStand[] { spawn, setArms.spawn() });
    }
    
    public void removeFlag(final CoordinatePair coordinatePair) {
        this.getPixel(coordinatePair.getX(), coordinatePair.getY()).getBlock().setType(Material.QUARTZ_BLOCK);
        CoordinatePair[] array;
        for (int length = (array = this.flagged.toArray(new CoordinatePair[this.flagged.size()])).length, i = 0; i < length; ++i) {
            final CoordinatePair coordinatePair2 = array[i];
            if (coordinatePair2.getX() == coordinatePair.getX() && coordinatePair2.getY() == coordinatePair.getY()) {
                this.flagged.remove(coordinatePair2);
            }
        }
        this.updateActionBar();
        if (this.flagdecor) {
            for (final Map.Entry entry : this.flagged_armorstand.entrySet()) {
                if (((CoordinatePair)entry.getKey()).getX() == coordinatePair.getX() && ((CoordinatePair)entry.getKey()).getY() == coordinatePair.getY()) {
                    ArmorStand[] array2;
                    for (int length2 = (array2 = (ArmorStand[])entry.getValue()).length, j = 0; j < length2; ++j) {
                        array2[j].remove();
                    }
                    this.flagged_armorstand.remove(entry.getKey());
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onInteractBlock(final PlayerInteractEvent playerInteractEvent) {
        if (!this.canStart()) {
            return;
        }
        if (!playerInteractEvent.getPlayer().getUniqueId().equals(this.getPlayer1().getUniqueId()) || !this.isActive()) {
            return;
        }
        final ItemStack handItem = GameUtils.getHandItem(playerInteractEvent.getPlayer());
        if (handItem != null && handItem.getType() == this.plugin.getQuitItem().getType()) {
            this.quitConfirmation(playerInteractEvent.getPlayer());
            return;
        }
        if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK || playerInteractEvent.getAction() == Action.LEFT_CLICK_BLOCK) {
            this.interactBlock(playerInteractEvent.getPlayer(), playerInteractEvent.getClickedBlock());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onArmorStand(final PlayerArmorStandManipulateEvent playerArmorStandManipulateEvent) {
        if (!this.canStart()) {
            return;
        }
        if (playerArmorStandManipulateEvent.getPlayer().getUniqueId().equals(this.getPlayer1().getUniqueId())) {
            playerArmorStandManipulateEvent.setCancelled(true);
            final ItemStack handItem = GameUtils.getHandItem(playerArmorStandManipulateEvent.getPlayer());
            if (handItem != null && handItem.getType() == this.plugin.getQuitItem().getType()) {
                this.quitConfirmation(playerArmorStandManipulateEvent.getPlayer());
                return;
            }
            for (final Block block : playerArmorStandManipulateEvent.getPlayer().getLineOfSight(null, 4)) {
                if (block.getType() == Material.QUARTZ_BLOCK) {
                    this.interactBlock(playerArmorStandManipulateEvent.getPlayer(), block);
                    return;
                }
                if (block.getType() == Material.valueOf((LobbyGames.SERVER_VERSION <= 12) ? "STEP" : "SMOOTH_STONE_SLAB") && handItem != null && handItem.getType() == Material.BLAZE_ROD) {
                    this.interactBlock(playerArmorStandManipulateEvent.getPlayer(), block);
                }
            }
        }
    }
    
    public void updateActionBar() {
        if (this.getPlayer1() == null || !this.getPlayer1().isOnline()) {
            return;
        }
        String s = "";
        if (this.score / 60 > 0) {
            s = this.score / 60 + "m ";
        }
        final String replacement = s + this.score % 60;
        int n = this.starting_mines - this.flagged.size();
        if (n < 0) {
            n = 0;
        }
        final String replaceAll = this.plugin.getConfigString("minesweeper.action-bar", "§4§l" + n + "§c mines remaining! §c§l" + replacement).replaceAll("\\Q%remaining_mines%\\E", "" + n).replaceAll("\\Q%time%\\E", replacement);
        if (replaceAll.length() == 0) {
            return;
        }
        this.getPlugin().sendActionbar(this.getPlayer1(), replaceAll, true);
    }
    
    public void interactBlock(final Player player, final Block block) {
        if (this.clickdelay > System.currentTimeMillis()) {
            return;
        }
        this.clickdelay = System.currentTimeMillis() + 100L;
        final Location location = block.getLocation();
        final Location pixel = this.getPixel(0, 0);
        if (super.getArena().isInBounds(location)) {
            final CoordinatePair coordinatePair = new CoordinatePair(location.getBlockX() - pixel.getBlockX(), location.getBlockZ() - pixel.getBlockZ());
            final ItemStack handItem = GameUtils.getHandItem(player);
            if (handItem != null && handItem.getType() == Material.BLAZE_ROD) {
                if (block.getType() == Material.QUARTZ_BLOCK) {
                    this.addFlag(coordinatePair);
                }
                else {
                    this.removeFlag(coordinatePair);
                }
            }
            else if (block.getType() == Material.QUARTZ_BLOCK && this.open(coordinatePair, true)) {
                this.die();
                if (LobbyGames.SERVER_VERSION >= 12) {
                    this.getPlayer1().spawnParticle(Particle.EXPLOSION, this.getPixel(coordinatePair.getX(), coordinatePair.getY()).add(0.0, 3.0, 0.0), 10);
                }
            }
        }
    }
    
    public int getN(final CoordinatePair coordinatePair) {
        int n = 0;
        for (final CoordinatePair coordinatePair2 : this.mines) {
            final int abs = Math.abs(coordinatePair2.getX() - coordinatePair.getX());
            final int abs2 = Math.abs(coordinatePair2.getY() - coordinatePair.getY());
            if (abs <= 1 && abs2 <= 1 && (coordinatePair2.getX() != coordinatePair.getX() || coordinatePair2.getY() != coordinatePair.getY())) {
                ++n;
            }
        }
        return n;
    }
    
    public List<CoordinatePair> getNeighbors(final CoordinatePair coordinatePair) {
        final ArrayList list = new ArrayList();
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                if (i != 0 || j != 0) {
                    final CoordinatePair coordinatePair2 = new CoordinatePair(coordinatePair.getX() + i, coordinatePair.getY() + j);
                    if (super.getArena().isInBounds(this.getPixel(coordinatePair2))) {
                        list.add(coordinatePair2);
                    }
                }
            }
        }
        return list;
    }
    
    public String getArmorName(final int n) {
        if (n >= 6) {
            return "§f§l" + n;
        }
        switch (n) {
            case 1: {
                return "§1§l1";
            }
            case 2: {
                return "§2§l2";
            }
            case 3: {
                return "§c§l3";
            }
            case 4: {
                return "§d§l4";
            }
            case 5: {
                return "§6§l5";
            }
            default: {
                return "";
            }
        }
    }
    
    public boolean open(final CoordinatePair coordinatePair, final boolean b) {
        if (!this.isActive()) {
            return false;
        }
        if (this.mines.size() == 0) {
            for (int i = 0; i < this.starting_mines; ++i) {
                this.mines.add(this.createMine(coordinatePair));
            }
        }
        for (final CoordinatePair coordinatePair2 : this.opened) {
            if (coordinatePair2.getX() == coordinatePair.getX() && coordinatePair2.getY() == coordinatePair.getY()) {
                return false;
            }
        }
        for (final CoordinatePair coordinatePair3 : this.mines) {
            if (coordinatePair3.getX() == coordinatePair.getX() && coordinatePair3.getY() == coordinatePair.getY()) {
                return true;
            }
        }
        final int n = this.getN(coordinatePair);
        for (final CoordinatePair coordinatePair4 : this.flagged) {
            if (coordinatePair.getX() == coordinatePair4.getX() && coordinatePair.getY() == coordinatePair4.getY()) {
                if (n == 0) {
                    final Iterator<CoordinatePair> iterator4 = this.getNeighbors(coordinatePair).iterator();
                    while (iterator4.hasNext()) {
                        this.open(iterator4.next(), false);
                    }
                }
                return false;
            }
        }
        final Location pixel = this.getPixel(coordinatePair.getX(), coordinatePair.getY());
        GameUtils.setWool(pixel.clone().add(0.0, -1.0, 0.0).getBlock(), this.getByte(n));
        pixel.getBlock().setType(Material.AIR);
        this.opened.add(coordinatePair);
        if (n == 0) {
            final Iterator<CoordinatePair> iterator5 = this.getNeighbors(coordinatePair).iterator();
            while (iterator5.hasNext()) {
                this.open(iterator5.next(), false);
            }
        }
        else {
            new ArmorStandFactory(pixel.add(0.5, -1.75, 0.5)).setName(this.getArmorName(n)).spawn();
        }
        if (b) {
            this.checkWin();
        }
        return false;
    }
    
    @Override
    public String getScore() {
        return this.getPlayTime();
    }
    
    @Override
    public int getScoreInt() {
        return this.getDuration();
    }
    
    public void die() {
        if (!this.isActive()) {
            return;
        }
        this.setActive(false);
        final Player player1 = this.getPlayer1();
        player1.playSound(player1.getLocation(), GameUtils.getSound(8, "EXPLODE", "ENTITY_GENERIC_EXPLODE"), 1.0f, 1.0f);
        if (this.getArena().getSpawn1() != null) {
            this.plugin.teleportToSpawn(player1, this.getArena());
        }
        final int n = this.score / 60;
        final int n2 = this.score % 60;
        final int n3 = this.starting_mines - this.flagged.size();
        String s = n2 + " Second" + ((n2 == 1) ? "" : "s");
        if (n > 0) {
            s = n + " Minute" + ((n == 1) ? "" : "s") + ", " + s;
        }
        final String configString = this.plugin.getConfigString("minesweeper.end-msg");
        String replaceAll;
        if (configString == null) {
            replaceAll = "\n§3§m----------------------------------------\n§c§lBOOM! You clicked a landmine!\n§bScore: §f" + s + " (" + n3 + " Mine" + ((n3 == 1) ? "" : "s") + " Remaining)\n§3§m----------------------------------------";
        }
        else {
            replaceAll = configString.replaceAll("\\Q%minutes%\\E", "" + n).replaceAll("\\Q%seconds%\\E", "" + n2).replaceAll("\\Q%remaining_mines%\\E", "" + n3);
        }
        if (replaceAll.length() > 0) {
            player1.sendMessage(this.plugin.sp(player1, replaceAll));
        }
        for (final CoordinatePair coordinatePair : this.mines) {
            final Location pixel = this.getPixel(coordinatePair.getX(), coordinatePair.getY());
            pixel.getBlock().setType(Material.REDSTONE_BLOCK);
            pixel.add(0.0, 1.0, 0.0);
            if (!pixel.getBlock().getType().equals((Object)Material.valueOf((LobbyGames.SERVER_VERSION <= 12) ? "STEP" : "SMOOTH_STONE_SLAB"))) {
                pixel.getBlock().setType(Material.AIR);
            }
        }
        for (final CoordinatePair coordinatePair2 : this.flagged) {
            final Location pixel2 = this.getPixel(coordinatePair2.getX(), coordinatePair2.getY());
            if (pixel2.getBlock().getType() != Material.REDSTONE_BLOCK) {
                pixel2.add(0.0, 1.0, 0.0).getBlock().setType(Material.AIR);
            }
        }
        this.end();
    }
    
    public void checkWin() {
        final int n = this.getArena().getWidth() / 2 + 1;
        final int n2 = this.getArena().getHeight() / 2 + 1;
        if (this.opened.size() >= this.getArena().getWidth() * this.getArena().getHeight() - this.starting_mines) {
            for (int i = -n; i < n; ++i) {
                for (int j = -n2; j < n2; ++j) {
                    if (this.getPixel(i, j).getBlock().getType() == Material.QUARTZ_BLOCK) {
                        this.addFlag(new CoordinatePair(i, j));
                    }
                }
            }
            this.win();
        }
    }
    
    public void win() {
        if (!this.isActive()) {
            return;
        }
        this.setActive(false);
        final Player player1 = this.getPlayer1();
        final int n = this.score / 60;
        final int n2 = this.score % 60;
        if (this.getArena().getSpawn1() != null) {
            this.plugin.teleportToSpawn(player1, this.getArena());
        }
        final String replaceAll = this.plugin.getConfigString("minesweeper.win-msg", "\n§2§m----------------------------------------\n§a§lYou win!\n§aScore: §f" + n + " Minute" + ((n == 1) ? "" : "s") + ", " + n2 + " Second" + ((n2 == 1) ? "" : "s") + "\n§2§m----------------------------------------").replaceAll("\\Q%minutes%\\E", "" + n).replaceAll("\\Q%seconds%\\E", "" + n2).replaceAll("\\Q%remaining_mines%\\E", "0");
        if (replaceAll.length() > 0) {
            player1.sendMessage(this.plugin.sp(player1, replaceAll));
        }
        Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(player1, this, this.score));
        this.end();
        player1.playSound(player1.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
        this.addScore(player1, -this.score, n + "m " + n2);
    }
    
    @EventHandler
    public void onEnd(final GameEndEvent gameEndEvent) {
        if (gameEndEvent.getGame().getGameType() == this.getGameType() && gameEndEvent.getGame().getArena().getID() == super.getArena().getID()) {
            if (this.getPlayer1().getAllowFlight()) {
                this.getPlayer1().setAllowFlight(this.can_fly);
            }
            if (this.plugin.getConfig().getBoolean("minesweeper.reset-on-end")) {
                this.reset();
            }
            HandlerList.unregisterAll((Listener)this);
        }
    }
    
    public void reset() {
        if (LobbyGames.SERVER_VERSION <= 12) {
            GameUtils.fill(this.getArena(), Material.QUARTZ_BLOCK, (byte)0, Material.valueOf("WOOL"), (byte)8);
        }
        else {
            GameUtils.fill(this.getArena(), Material.QUARTZ_BLOCK, (byte)0, Material.LIGHT_GRAY_WOOL, (byte)0);
        }
        this.clearArmorStands();
    }
    
    static /* synthetic */ void access$1(final Minesweeper minesweeper, final int score) {
        minesweeper.score = score;
    }
}
