// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.games;

import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.Iterator;
import me.c7dev.lobbygames.util.ArmorStandFactory;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.GameMode;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.LobbyGames;
import java.util.Random;
import me.c7dev.lobbygames.util.SudokuGenerator;
import me.c7dev.lobbygames.util.GameUtils;
import org.bukkit.Material;
import me.c7dev.lobbygames.util.CoordinatePair;
import java.util.List;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import me.c7dev.lobbygames.Game;

public class Sudoku extends Game implements Listener
{
    private Player p;
    private int[][] spaces;
    private int[][] spaces_init;
    private ArmorStand[][] labels;
    private List<CoordinatePair> shown_spaces;
    private double starttime;
    private Material mat;
    private final GameUtils.ColorName[] colors;
    private final String[] color_str;
    private String action_bar;
    private SudokuGenerator grader;
    private boolean can_fly;
    private boolean restart_conf;
    
    public CoordinatePair randomSpace(final Random random) {
        final CoordinatePair coordinatePair = new CoordinatePair(random.nextInt(9), random.nextInt(9));
        if (this.spaces[coordinatePair.getX()][coordinatePair.getY()] == 0) {
            return coordinatePair;
        }
        return this.randomSpace(random);
    }
    
    public Sudoku(final LobbyGames v1, final Arena v2, final Player returnLocation) {
        super(v1, GameType.SUDOKU, v2, returnLocation);
        this.spaces = new int[9][9];
        this.labels = new ArmorStand[9][9];
        this.shown_spaces = new ArrayList<CoordinatePair>();
        this.colors = new GameUtils.ColorName[] { GameUtils.ColorName.PURPLE, GameUtils.ColorName.BLUE, GameUtils.ColorName.LIGHT_BLUE, GameUtils.ColorName.GREEN, GameUtils.ColorName.LIME, GameUtils.ColorName.YELLOW, GameUtils.ColorName.ORANGE, GameUtils.ColorName.RED, GameUtils.ColorName.MAGENTA };
        this.color_str = new String[] { "§5", "§1", "§b", "§2", "§a", "§e", "§6", "§c", "§d" };
        this.can_fly = false;
        this.restart_conf = false;
        if (!this.canStart() || v2.getGameType() != GameType.SUDOKU) {
            return;
        }
        this.p = returnLocation;
        this.setActive(true);
        this.starttime = (double)(System.currentTimeMillis() / 1000L);
        this.mat = ((LobbyGames.SERVER_VERSION >= 13) ? Material.WHITE_CONCRETE : Material.valueOf("WOOL"));
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)v1);
        final String configString = this.getPlugin().getConfigString(returnLocation, "sudoku.start-msg", "§3§m----------------------------------------\n§b§lSudoku: §bSet the numbers in the puzzle so every row, column, and 3x3 box has one of each digit from 1 to 9!\n§3§m----------------------------------------");
        if (configString.length() > 0) {
            returnLocation.sendMessage(configString);
        }
        this.action_bar = v1.getConfigString("sudoku.action-bar", "§aTime: §f%time%");
        if (v2.getSpawn1() != null) {
            v1.setReturnLocation(returnLocation);
            returnLocation.teleport(v2.getSpawn1());
        }
        this.preparePlayer(returnLocation, null);
        this.setActive(true);
        this.giveItems();
        if (v1.getConfig().getBoolean("sudoku.particles-enabled") && LobbyGames.SERVER_VERSION >= 12) {
            final boolean b = v2.isVerticalLayout() && (v2.getCoordinateRotation() == 0 || v2.getCoordinateRotation() == 3);
            final Vector vector = new Vector((int)(b ? 1 : 0), (int)(v2.isVerticalLayout() ? 0 : 1), (int)(b ? 1 : 0));
            new BukkitRunnable() {
                private final /* synthetic */ Location val$a1 = Sudoku.this.getPixel(-1, 5).add(vector);
                private final /* synthetic */ Location val$a2 = Sudoku.this.getPixel(-1, -4).add(vector);
                private final /* synthetic */ Location val$b1 = Sudoku.this.getPixel(2, 5).add(vector);
                private final /* synthetic */ Location val$b2 = Sudoku.this.getPixel(2, -4).add(vector);
                private final /* synthetic */ Location val$c1 = Sudoku.this.getPixel(5, 2).add(vector);
                private final /* synthetic */ Location val$c2 = Sudoku.this.getPixel(-4, 2).add(vector);
                private final /* synthetic */ Location val$d1 = Sudoku.this.getPixel(5, -1).add(vector);
                private final /* synthetic */ Location val$d2 = Sudoku.this.getPixel(-4, -1).add(vector);
                
                public void run() {
                    if (!Sudoku.this.canStart() || !Sudoku.this.isActive()) {
                        this.cancel();
                    }
                    GameUtils.particleLine(this.val$a1, this.val$a2, 9.0);
                    GameUtils.particleLine(this.val$b1, this.val$b2, 9.0);
                    GameUtils.particleLine(this.val$c1, this.val$c2, 9.0);
                    GameUtils.particleLine(this.val$d1, this.val$d2, 9.0);
                }
            }.runTaskTimer((Plugin)v1, 0L, 5L);
        }
        if (this.action_bar.length() > 0) {
            new BukkitRunnable() {
                public void run() {
                    if (!Sudoku.this.isActive() || !Sudoku.this.canStart()) {
                        this.cancel();
                        return;
                    }
                    final int n = (int)(System.currentTimeMillis() / 1000L - Sudoku.this.starttime);
                    final String replacement = "" + ((n >= 60) ? (n / 60 + "m ") : "") + n % 60;
                    if (Sudoku.this.grader == null || !Sudoku.this.grader.isFinished() || Sudoku.this.grader.unfilledCount() != 0) {
                        Sudoku.this.getPlugin().sendActionbar(returnLocation, Sudoku.this.action_bar.replaceAll("\\Q%time%\\E", replacement), true);
                    }
                }
            }.runTaskTimer((Plugin)v1, 0L, 20L);
        }
        this.start();
    }
    
    public void giveItems() {
        if (LobbyGames.SERVER_VERSION > 12) {
            for (int i = 0; i < 9; ++i) {
                this.p.getInventory().setItem(i, GameUtils.createItem(Material.valueOf(this.colors[i].toString() + "_CONCRETE"), 1, (byte)0, this.color_str[i] + "§l" + (i + 1), new String[0]));
            }
        }
        else {
            for (int j = 0; j < 9; ++j) {
                this.p.getInventory().setItem(j, GameUtils.createItem(this.mat, 1, this.colors[j], this.color_str[j] + "§l" + (j + 1), new String[0]));
            }
        }
    }
    
    public void start() {
        this.reset();
        this.setActive(true);
        this.can_fly = this.p.getAllowFlight();
        if (this.plugin.getConfig().getBoolean("sudoku.player-can-fly")) {
            this.p.setAllowFlight(true);
        }
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.spaces[i][j] = 0;
            }
        }
        (this.grader = new SudokuGenerator()).loadSudoku(this.spaces);
        int int1 = this.plugin.getConfig().getInt("sudoku.prefilled-count");
        if (int1 < 3) {
            int1 = 3;
        }
        else if (int1 > 70) {
            int1 = 70;
        }
        try {
            this.spaces_init = new SudokuGenerator().generateSudoku();
        }
        catch (final Exception ex) {
            this.end();
            Bukkit.getLogger().severe("Could not create a new Sudoku puzzle!");
            return;
        }
        final Random random = new Random();
        for (int k = 0; k < int1; ++k) {
            final CoordinatePair randomSpace = this.randomSpace(random);
            this.setSpace(randomSpace, this.spaces_init[randomSpace.getX()][randomSpace.getY()], true);
            this.shown_spaces.add(randomSpace);
        }
    }
    
    public void setSpace(final CoordinatePair coordinatePair, final int n, final boolean b) {
        this.setSpace(coordinatePair.getX(), coordinatePair.getY(), n, b);
    }
    
    public void setSpace(final int n, final int n2, int n3, final boolean b) {
        if (n3 == 0) {
            return;
        }
        if (n > 8 || n2 > 8 || n3 > 9) {
            return;
        }
        for (final CoordinatePair coordinatePair : this.shown_spaces) {
            if (coordinatePair.getX() == n && coordinatePair.getY() == n2) {
                return;
            }
        }
        if (this.spaces[n][n2] == n3) {
            n3 = 0;
        }
        this.spaces[n][n2] = n3;
        final Location pixel = this.getPixel(n - 4, n2 - 4);
        if (LobbyGames.SERVER_VERSION >= 13) {
            pixel.getBlock().setType(Material.valueOf(((n3 == 0) ? GameUtils.ColorName.WHITE : this.colors[n3 - 1]).toString() + "_CONCRETE"));
        }
        else {
            GameUtils.setWool(pixel.getBlock(), (byte)((n3 == 0) ? 0 : this.colors[n3 - 1].getMagicNumber()));
        }
        if (this.labels[n][n2] != null) {
            this.labels[n][n2].remove();
            this.labels[n][n2] = null;
        }
        if (n3 > 0) {
            this.labels[n][n2] = new ArmorStandFactory(pixel.clone().add(this.getArena().getArmorStandOffset())).setName((b ? "§f§l" : this.color_str[n3 - 1]) + n3).spawn();
        }
        if (this.grader.gradeSudoku()) {
            this.win();
        }
        else if (this.grader.unfilledCount() == 0) {
            final String configString = this.plugin.getConfigString("sudoku.invalid-solution", "§cInvalid Solution!");
            if (configString.length() > 0) {
                this.getPlugin().sendActionbar(this.p, configString, false);
            }
        }
    }
    
    public void placeBlock(final Location location, final int n) {
        if (!this.getArena().isInBounds(location)) {
            return;
        }
        final CoordinatePair coords = this.getCoords(location);
        this.setSpace(coords.getX() + 4, coords.getY() + 4, n, false);
    }
    
    @EventHandler
    public void onPlace(final PlayerInteractEvent playerInteractEvent) {
        if (!this.canStart() || !this.isActive() || !playerInteractEvent.getPlayer().getUniqueId().equals(this.p.getUniqueId())) {
            return;
        }
        if (!this.restart_conf && playerInteractEvent.getAction() != Action.LEFT_CLICK_BLOCK && playerInteractEvent.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        playerInteractEvent.setCancelled(true);
        final ItemStack itemStack = (LobbyGames.SERVER_VERSION <= 12) ? playerInteractEvent.getPlayer().getItemInHand() : playerInteractEvent.getPlayer().getInventory().getItemInMainHand();
        if (itemStack != null) {
            if (LobbyGames.SERVER_VERSION <= 12) {
                if (itemStack.getType() != this.mat) {
                    return;
                }
            }
            else if (!itemStack.getType().toString().endsWith("CONCRETE") && !this.restart_conf) {
                return;
            }
            if (this.restart_conf) {
                if (GameUtils.getData(itemStack) == 5) {
                    this.doRestart();
                }
                this.restart_conf = false;
                this.giveItems();
                return;
            }
            if (playerInteractEvent.getClickedBlock().getType().toString().endsWith("CONCRETE") || playerInteractEvent.getClickedBlock().getType() == this.mat) {
                for (int i = 0; i < 9; ++i) {
                    if (this.colors[i].getMagicNumber() == GameUtils.getData(itemStack)) {
                        this.placeBlock(playerInteractEvent.getClickedBlock().getLocation(), i + 1);
                        break;
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityInteract(final PlayerInteractAtEntityEvent playerInteractAtEntityEvent) {
        if (this.restart_conf) {
            return;
        }
        if (!this.isActive() || !this.canStart() || !playerInteractAtEntityEvent.getPlayer().getUniqueId().equals(this.p.getUniqueId())) {
            return;
        }
        final ItemStack handItem = GameUtils.getHandItem(playerInteractAtEntityEvent.getPlayer());
        final Block block = playerInteractAtEntityEvent.getRightClicked().getLocation().add(playerInteractAtEntityEvent.getClickedPosition()).add(this.getArena().getGenericOffset().multiply(-1)).getBlock();
        if (handItem != null) {
            if (LobbyGames.SERVER_VERSION <= 12) {
                if (handItem.getType() != this.mat) {
                    return;
                }
            }
            else if (!handItem.getType().toString().endsWith("CONCRETE")) {
                return;
            }
            if (block.getType().toString().endsWith("CONCRETE") || block.getType() == this.mat) {
                for (int i = 0; i < 9; ++i) {
                    if (this.colors[i].getMagicNumber() == GameUtils.getData(handItem)) {
                        this.placeBlock(block.getLocation(), i + 1);
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    public void restart() {
        if (this.restart_conf) {
            return;
        }
        this.restart_conf = true;
        this.p.getInventory().clear();
        this.p.getInventory().setItem(3, GameUtils.createWool(1, 5, this.plugin.getConfigString("yes-text", "§a§lYes"), new String[0]));
        this.p.getInventory().setItem(5, GameUtils.createWool(1, 14, this.plugin.getConfigString("no-text", "§c§lNo"), new String[0]));
    }
    
    public void doRestart() {
        this.restart_conf = false;
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.setSpace(i, j, this.spaces[i][j] = this.spaces_init[i][j], true);
            }
        }
    }
    
    public void win() {
        final int n = (int)(System.currentTimeMillis() / 1000L - this.starttime);
        final String replacement = n / 60 + "m " + n % 60;
        final String replaceAll = this.plugin.getConfigString("sudoku.win-msg", "§2§m----------------------------------------\n§a§lYou finished the sudoku! §aTime: " + replacement + "\n§2§m----------------------------------------").replaceAll("\\Q%time%\\E", replacement);
        if (replaceAll.length() > 0) {
            this.p.sendMessage(this.plugin.sp(this.p, replaceAll));
        }
        this.addScore(this.p, -n, replacement);
        Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(this.p, this, n));
        this.end();
        this.p.playSound(this.p.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
    }
    
    @Override
    public String getScore() {
        return this.getPlayTime();
    }
    
    @Override
    public int getScoreInt() {
        return this.getDuration();
    }
    
    public void reset() {
        GameUtils.fill(this.getArena(), this.mat, (byte)0, null, (byte)0);
        this.clearArmorStands();
    }
    
    @EventHandler
    public void onEnd(final GameEndEvent gameEndEvent) {
        if (gameEndEvent.getGame().getGameType() == this.getGameType() && gameEndEvent.getGame().getArena().getID() == this.getArena().getID()) {
            this.p.setAllowFlight(this.can_fly);
            if (this.plugin.getConfig().getBoolean("sudoku.reset-on-end")) {
                this.reset();
            }
            HandlerList.unregisterAll((Listener)this);
        }
    }
}
