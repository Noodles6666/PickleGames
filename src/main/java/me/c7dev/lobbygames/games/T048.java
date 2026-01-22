// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.games;

import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import me.c7dev.lobbygames.api.events.PlayerQuitLobbyGameEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import me.c7dev.lobbygames.util.CoordinatePair;
import java.util.Random;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.inventory.ItemStack;
import me.c7dev.lobbygames.util.Direction;
import org.bukkit.Location;
import me.c7dev.lobbygames.util.T048Tile;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import me.c7dev.lobbygames.Game;

public class T048 extends Game implements Listener
{
    private Player p;
    private T048Tile[][] spaces;
    private Location spawn;
    private int width;
    private int height;
    private int w2;
    private int h2;
    private Direction future_move;
    private Direction moving_dir;
    private boolean made_2048;
    private long lastMove;
    private ItemStack[] tileblocks;
    
    public T048(final LobbyGames v1, final Arena v2, final Player v3) {
        super(v1, GameType.T048, v2, v3);
        this.made_2048 = false;
        this.lastMove = 0L;
        this.tileblocks = new ItemStack[] { new ItemStack(GameUtils.whiteWool(), 1), new ItemStack(Material.CLAY, 1), GameUtils.createWool(1, 1, "orange", new String[0]), new ItemStack((LobbyGames.SERVER_VERSION > 12) ? Material.RED_SAND : Material.valueOf("RED_SANDSTONE"), 1), (LobbyGames.SERVER_VERSION > 12) ? new ItemStack(Material.ACACIA_PLANKS, 1) : new ItemStack(Material.valueOf("WOOD"), 1, (short)4), GameUtils.createWool(1, 14, "red", new String[0]), new ItemStack(Material.SAND, 1), (LobbyGames.SERVER_VERSION > 12) ? new ItemStack(Material.BIRCH_PLANKS, 1) : new ItemStack(Material.valueOf("WOOD"), 1, (short)2), new ItemStack((LobbyGames.SERVER_VERSION > 12) ? Material.OAK_PLANKS : Material.valueOf("WOOD"), 1), GameUtils.createWool(1, 4, "yellow", new String[0]), new ItemStack(Material.GOLD_BLOCK, 1), new ItemStack(Material.NETHERRACK, 1) };
        if (!this.canStart() || v2.getGameType() != GameType.T048) {
            return;
        }
        this.p = this.getPlayer1();
        final Block relative = v2.getSpawn1().getBlock().getRelative(BlockFace.DOWN);
        if (relative.getType() == Material.AIR) {
            relative.setType(Material.BARRIER);
        }
        this.setActive(true);
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)v1);
        final String configString = this.getPlugin().getConfigString(this.p, "2048.start-msg", "§3§m----------------------------------------\n§b§l2048: §bUse the W, A, S, and D keys to merge tiles and get to the 2048 tile without filling the board!\n§3§m----------------------------------------");
        if (configString.length() > 0) {
            this.p.sendMessage(configString);
        }
        final float[] array = { -90.0f, -180.0f, 90.0f, 0.0f };
        final float[] array2 = { -180.0f, -90.0f, 0.0f, 90.0f };
        float yaw;
        if (this.getArena().isVerticalLayout()) {
            yaw = array2[this.getArena().getCoordinateRotation()];
        }
        else {
            yaw = array[this.getArena().getCoordinateRotation()];
        }
        (this.spawn = this.getArena().getSpawn1().clone()).setYaw(yaw);
        v1.setReturnLocation(this.p);
        this.p.teleportAsync(this.spawn);
        this.preparePlayer(this.p);
        this.width = v2.getWidth();
        this.height = v2.getHeight();
        if ((v2.getCoordinateRotation() & 0x1) == 0x0) {
            final int width = this.width;
            this.width = this.height;
            this.height = width;
        }
        this.w2 = (int)Math.ceil(this.width / 2.0);
        if (!v2.isInBounds(this.getPixel(-this.w2, 0))) {
            --this.w2;
        }
        this.h2 = (int)Math.ceil(this.height / 2.0);
        if (!v2.isInBounds(this.getPixel(0, -this.h2))) {
            --this.h2;
        }
        this.start(this.p, v1);
    }
    
    public boolean isFilled() {
        for (int i = 0; i < this.width; ++i) {
            for (int j = 0; j < this.height; ++j) {
                if (this.spaces[i][j] == null) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public CoordinatePair randomCoords(final Random random) {
        final CoordinatePair coordinatePair = new CoordinatePair(random.nextInt(this.width), random.nextInt(this.height));
        if (!this.getArena().isInBounds(this.getPixel(coordinatePair.getX() - this.w2, coordinatePair.getY() - this.h2)) || this.spaces[coordinatePair.getX()][coordinatePair.getY()] != null) {
            return this.randomCoords(random);
        }
        return coordinatePair;
    }
    
    public void start(final Player player, final LobbyGames lobbyGames) {
        this.reset();
        this.spaces = new T048Tile[this.width][this.height];
        player.getInventory().setItem(8, lobbyGames.getQuitItem());
        this.spawnTile();
        this.spawnTile();
        final String configString = lobbyGames.getConfigString("2048.action-bar", "§aScore: §f%score%");
        if (configString.length() > 0) {
            new BukkitRunnable() {
                public void run() {
                    if (!T048.this.canStart() || !T048.this.isActive()) {
                        this.cancel();
                        return;
                    }
                    T048.this.getPlugin().sendActionbar(player, configString.replaceAll("\\Q%score%\\E", "" + T048.this.score), true);
                }
            }.runTaskTimer((Plugin)lobbyGames, 1L, 20L);
        }
    }
    
    public boolean checkStuck() {
        for (int i = 0; i < this.width; ++i) {
            for (int j = 0; j < this.height; ++j) {
                final T048Tile t048Tile = this.spaces[i][j];
                if (t048Tile == null) {
                    return false;
                }
                if (this.isInBounds(i + 1, j)) {
                    final T048Tile t048Tile2 = this.spaces[i + 1][j];
                    if (t048Tile2 == null || t048Tile2.getNum() == t048Tile.getNum()) {
                        return false;
                    }
                }
                if (this.isInBounds(i - 1, j)) {
                    final T048Tile t048Tile3 = this.spaces[i - 1][j];
                    if (t048Tile3 == null || t048Tile3.getNum() == t048Tile.getNum()) {
                        return false;
                    }
                }
                if (this.isInBounds(i, j + 1)) {
                    final T048Tile t048Tile4 = this.spaces[i][j + 1];
                    if (t048Tile4 == null || t048Tile4.getNum() == t048Tile.getNum()) {
                        return false;
                    }
                }
                if (this.isInBounds(i, j - 1)) {
                    final T048Tile t048Tile5 = this.spaces[i][j - 1];
                    if (t048Tile5 == null || t048Tile5.getNum() == t048Tile.getNum()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public void spawnTile() {
        if (!this.canStart()) {
            return;
        }
        if (this.isFilled()) {
            this.die();
            return;
        }
        final Random random = new Random();
        final int nextInt = random.nextInt(7);
        final CoordinatePair randomCoords = this.randomCoords(random);
        this.spaces[randomCoords.getX()][randomCoords.getY()] = new T048Tile((nextInt == 1) ? 1 : 0, new CoordinatePair(randomCoords.getX() - this.w2, randomCoords.getY() - this.h2), this, this.tileblocks);
        if (this.checkStuck()) {
            this.die();
        }
    }
    
    public void moveTile(final T048Tile t048Tile, final int n, final int n2, final T048Tile t048Tile2) {
        final CoordinatePair coords = t048Tile.getCoords();
        this.spaces[coords.getX() + this.w2][coords.getY() + this.h2] = null;
        if (t048Tile2 == null) {
            coords.setX(coords.getX() + n);
            coords.setY(coords.getY() + n2);
            this.spaces[coords.getX() + this.w2][coords.getY() + this.h2] = t048Tile;
        }
        t048Tile.teleport(this.getPixel(coords), t048Tile2);
    }
    
    public void move(final Direction future_move) {
        if (future_move == null) {
            return;
        }
        int value = future_move.getValue();
        if (this.moving_dir != null) {
            if (this.moving_dir != future_move) {
                this.future_move = future_move;
            }
            return;
        }
        this.moving_dir = null;
        this.future_move = null;
        final int n = (--value + this.getArena().getCoordinateRotation()) % 4;
        final CoordinatePair coordinatePair = (n % 2 == 0) ? new CoordinatePair(0, 1) : new CoordinatePair(1, 0);
        CoordinatePair coordinatePair2 = new CoordinatePair(0, 0);
        CoordinatePair coordinatePair3 = null;
        switch (n % 4) {
            case 0: {
                coordinatePair3 = new CoordinatePair(1, 0);
                break;
            }
            case 1: {
                coordinatePair3 = new CoordinatePair(0, -1);
                coordinatePair2 = new CoordinatePair(0, this.height - 1);
                break;
            }
            case 2: {
                coordinatePair3 = new CoordinatePair(-1, 0);
                coordinatePair2 = new CoordinatePair(this.width - 1, 0);
                break;
            }
            default: {
                coordinatePair3 = new CoordinatePair(0, 1);
                break;
            }
        }
        if (this.executeMove(coordinatePair3, coordinatePair2.clone(), coordinatePair, true)) {
            new me.c7dev.lobbygames.util.GameTask() {
                public void run() {
                    T048.this.spawnTile();
                    new me.c7dev.lobbygames.util.GameTask() {
                        public void run() {
                            T048.this.moving_dir = null;
                            T048.this.move(T048.this.future_move);
                        }
                    }.runTaskLater(T048.this.getPlugin(), 3L);
                }
            }.runTaskLater(this.getPlugin(), 4L);
        }
        else {
            new me.c7dev.lobbygames.util.GameTask() {
                public void run() {
                    T048.this.moving_dir = null;
                    T048.this.move(T048.this.future_move);
                }
            }.runTaskLater(this.getPlugin(), 3L);
        }
    }
    
    private boolean executeMove(final CoordinatePair coordinatePair, final CoordinatePair coordinatePair2, final CoordinatePair coordinatePair3, final boolean b) {
        boolean b2 = false;
        while (this.isInBounds(coordinatePair2)) {
            CoordinatePair clone;
            CoordinatePair add;
            for (clone = coordinatePair2.clone(); this.isInBounds(clone); clone = add) {
                add = clone.clone().add(coordinatePair);
                final T048Tile t048Tile = this.spaces[clone.getX()][clone.getY()];
                if (t048Tile != null && this.isInBounds(add)) {
                    final T048Tile t048Tile2 = this.spaces[add.getX()][add.getY()];
                    if (t048Tile2 == null) {
                        b2 = true;
                        this.moveTile(t048Tile, coordinatePair.getX(), coordinatePair.getY(), null);
                    }
                    else if (t048Tile2.getNum() == t048Tile.getNum()) {
                        final CoordinatePair add2 = add.clone().add(coordinatePair);
                        boolean b3 = false;
                        if (this.isInBounds(add2)) {
                            final T048Tile t048Tile3 = this.spaces[add2.getX()][add2.getY()];
                            if (t048Tile3 != null && t048Tile3.getNum() == t048Tile.getNum()) {
                                b3 = true;
                                final CoordinatePair add3 = add2.clone().add(coordinatePair);
                                if (this.isInBounds(add3)) {
                                    final T048Tile t048Tile4 = this.spaces[add3.getX()][add3.getY()];
                                    if (t048Tile4 != null && t048Tile4.getNum() == t048Tile.getNum()) {
                                        b3 = false;
                                    }
                                }
                            }
                        }
                        if (!b3) {
                            b2 = true;
                            this.score += (int)Math.pow(2.0, t048Tile.getNum() + 2);
                            this.moveTile(t048Tile, coordinatePair.getX(), coordinatePair.getY(), t048Tile2);
                            this.spaces[clone.getX()][clone.getY()] = null;
                            if (t048Tile.getNum() == 9 && !this.made_2048) {
                                this.getPlayer1().playSound(this.getPlayer1().getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
                                this.made_2048 = true;
                                this.setIsWinner(this.p.getUniqueId(), true);
                            }
                        }
                    }
                }
            }
            if (b2) {
                final CoordinatePair multiply = coordinatePair.clone().multiply(-1);
                clone.add(multiply);
                int n = 0;
                while (this.isInBounds(clone)) {
                    final T048Tile t048Tile5 = this.spaces[clone.getX()][clone.getY()];
                    if (t048Tile5 == null) {
                        ++n;
                    }
                    else if (n > 0) {
                        this.moveTile(t048Tile5, n * coordinatePair.getX(), n * coordinatePair.getY(), null);
                    }
                    clone.add(multiply);
                }
            }
            coordinatePair2.add(coordinatePair3);
        }
        return b2;
    }
    
    public void die() {
        final String replaceAll = this.getPlugin().getConfigString(this.p, "2048.end-msg", "§3§m----------------------------------------\n§b§lRan out of moves! §bScore:§f %score% Points\n§3§m----------------------------------------").replaceAll("\\Q%score%\\E", "" + this.score);
        if (replaceAll.length() > 0) {
            this.p.sendMessage(replaceAll);
        }
        this.getPlugin().teleportToSpawn(this.p, this.getArena(), true);
        Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(this.p, this, this.score));
        this.end();
    }
    
    public void reset() {
        this.clearArmorStands();
    }
    
    public boolean isInBounds(final CoordinatePair coordinatePair) {
        return this.isInBounds(coordinatePair.getX(), coordinatePair.getY());
    }
    
    public boolean isInBounds(final int n, final int n2) {
        return n >= 0 && n2 >= 0 && n < this.width && n2 < this.height;
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent playerInteractEvent) {
        if (this.canStart() && playerInteractEvent.getPlayer().getUniqueId().equals(this.p.getUniqueId())) {
            final ItemStack handItem = GameUtils.getHandItem(this.p);
            if (handItem != null && handItem.getType() == this.getPlugin().getQuitItem().getType()) {
                this.quitConfirmation(playerInteractEvent.getPlayer());
            }
        }
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitLobbyGameEvent playerQuitLobbyGameEvent) {
        if (playerQuitLobbyGameEvent.getPlayer().getUniqueId().equals(this.getPlayer1().getUniqueId()) && this.canStart()) {
            this.end();
            Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(this.p, this, this.score));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(final PlayerMoveEvent playerMoveEvent) {
        if (!this.canStart() || !playerMoveEvent.getPlayer().getUniqueId().equals(this.p.getUniqueId()) || !this.isActive() || System.currentTimeMillis() - this.lastMove < 30L) {
            return;
        }
        this.lastMove = System.currentTimeMillis();
        this.p.teleportAsync(this.spawn);
        final double n = 0.002;
        if (playerMoveEvent.getTo().getX() - playerMoveEvent.getFrom().getX() > n) {
            this.move(Direction.RIGHT);
        }
        else if (playerMoveEvent.getTo().getX() - playerMoveEvent.getFrom().getX() < -n) {
            this.move(Direction.LEFT);
        }
        else if (playerMoveEvent.getTo().getZ() - playerMoveEvent.getFrom().getZ() > n) {
            this.move(Direction.DOWN);
        }
        else if (playerMoveEvent.getTo().getZ() - playerMoveEvent.getFrom().getZ() < -n) {
            this.move(Direction.UP);
        }
    }
    
    @EventHandler
    public void onManipulate(final PlayerArmorStandManipulateEvent playerArmorStandManipulateEvent) {
        if (!this.canStart()) {
            return;
        }
        if (!this.getArena().isInBoundsXZ(playerArmorStandManipulateEvent.getRightClicked().getLocation())) {
            return;
        }
        if (Math.abs(playerArmorStandManipulateEvent.getRightClicked().getLocation().getY() - this.getArena().getCenterPixel().getBlockY()) <= 2.5) {
            playerArmorStandManipulateEvent.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEnd(final GameEndEvent gameEndEvent) {
        if (gameEndEvent.getGame().getGameType() == this.getGameType() && gameEndEvent.getGame().getArena().getID() == this.getArena().getID()) {
            this.addScore(this.p, this.score);
            if (this.getPlugin().getConfig().getBoolean("2048.reset-on-end")) {
                new me.c7dev.lobbygames.util.GameTask() {
                    public void run() {
                        if (T048.this.getArena().getHostingGame() == null) {
                            T048.this.reset();
                        }
                    }
                }.runLocationTaskLater((Plugin)this.getPlugin(), T048.this.getArena().getCenterPixel(), 120L);
            }
            HandlerList.unregisterAll((Listener)this);
        }
    }
}
