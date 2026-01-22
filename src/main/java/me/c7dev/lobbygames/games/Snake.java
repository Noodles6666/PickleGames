// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.games;

import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import java.util.Iterator;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.util.SchedulerUtil;
import me.c7dev.lobbygames.util.GameTask;
import java.util.LinkedList;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.LobbyGames;
import me.c7dev.lobbygames.util.Direction;
import org.bukkit.Material;
import org.bukkit.Location;
import me.c7dev.lobbygames.util.CoordinatePair;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import me.c7dev.lobbygames.Game;

public class Snake extends Game implements Listener
{
    private Player p;
    private int fat;
    private List<CoordinatePair> pixels;
    private CoordinatePair head;
    private CoordinatePair headstart;
    private CoordinatePair apple;
    private boolean keylistening;
    private Location spawn;
    private String action_bar;
    private Material head_mat;
    private Material body_mat;
    private Direction direction;
    
    public Snake(final LobbyGames v1, final Arena v2, final Player v3) {
        super(v1, GameType.SNAKE, v2, v3);
        this.fat = 0;
        this.pixels = new LinkedList<CoordinatePair>();
        this.keylistening = true;
        if (!this.canStart() || v2.getGameType() != GameType.SNAKE) {
            return;
        }
        this.p = this.getPlayer1();
        this.headstart = new CoordinatePair(-1, 1);
        this.head = new CoordinatePair(this.headstart.getX(), this.headstart.getY());
        final Material whiteWool = GameUtils.whiteWool();
        this.head_mat = GameUtils.getMaterialByString(v1.getConfig().getString("snake.head-material"), whiteWool);
        this.body_mat = GameUtils.getMaterialByString(v1.getConfig().getString("snake.body-material"), whiteWool);
        if (v2.isVerticalLayout()) {
            if (this.head_mat.hasGravity()) {
                this.head_mat = whiteWool;
            }
            if (this.body_mat.hasGravity()) {
                this.body_mat = whiteWool;
            }
        }
        final Block relative = v2.getSpawn1().getBlock().getRelative(BlockFace.DOWN);
        if (relative.getType() == Material.AIR) {
            relative.setType(Material.BARRIER);
        }
        this.setActive(true);
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)v1);
        final String configString = this.getPlugin().getConfigString(this.p, "snake.start-msg", "§3§m----------------------------------------\n§b§lSnake: §bEat apples to grow larger, but don't run into the walls or yourself! Use the W, A, S, and D keys to move!\n§3§m----------------------------------------");
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
        // 在 Folia 上使用 teleportAsync
        if (SchedulerUtil.isFolia()) {
            this.p.teleportAsync(this.spawn);
        } else {
            this.p.teleport(this.spawn);
        }
        this.preparePlayer(this.p);
        this.start(this.p, v1);
    }
    
    private void start(final Player player, final LobbyGames lobbyGames) {
        this.reset(false);
        this.keylistening = true;
        
        // 使用区域任务来初始化游戏，确保方块操作在正确的线程中
        final Location arenaCenter = this.getArena().getCenterPixel();
        new GameTask() {
            public void run() {
                // 初始化蛇的身体
                for (int i = 2; i >= 0; --i) {
                    final CoordinatePair coordinatePair = new CoordinatePair(Snake.this.headstart.getX() - i, Snake.this.headstart.getY());
                    Snake.this.pixels.add(coordinatePair);
                    Snake.this.getPixel(coordinatePair).getBlock().setType((i == 0) ? Snake.this.head_mat : Snake.this.body_mat);
                }
                // 创建苹果
                Snake.this.createApple();
            }
        }.runLocationTask((Plugin)lobbyGames, arenaCenter);
        
        player.setWalkSpeed(0.15f);
        
        // 使用区域任务而不是全局任务，因为需要访问方块
        new GameTask() {
            public void run() {
                if (!Snake.this.isActive()) {
                    this.cancel();
                }
                Snake.this.shiftFrame();
            }
        }.runLocationTaskTimer((Plugin)lobbyGames, arenaCenter, 1L, 3L);
        
        this.action_bar = lobbyGames.getConfigString("snake.action-bar", "§aApples: §f%score%");
        this.updateActionBar();
    }
    
    public void updateActionBar() {
        if (this.canStart() && this.action_bar.length() > 0) {
            this.getPlugin().sendActionbar(this.p, this.action_bar.replaceAll("\\Q%score%\\E", "" + this.score).replaceAll("\\Q(s)\\E", (this.score == 1) ? "" : "s"), true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(final PlayerMoveEvent playerMoveEvent) {
        if (!this.canStart() || playerMoveEvent.getPlayer().getUniqueId() != this.p.getUniqueId() || !this.isActive()) {
            return;
        }
        final double n = 0.005;
        if (playerMoveEvent.getTo().getX() - playerMoveEvent.getFrom().getX() > n) {
            this.turn(Direction.RIGHT);
        }
        else if (playerMoveEvent.getTo().getX() - playerMoveEvent.getFrom().getX() < -n) {
            this.turn(Direction.LEFT);
        }
        else if (playerMoveEvent.getTo().getZ() - playerMoveEvent.getFrom().getZ() > n) {
            this.turn(Direction.DOWN);
        }
        else if (playerMoveEvent.getTo().getZ() - playerMoveEvent.getFrom().getZ() < -n) {
            this.turn(Direction.UP);
        }
        // 在 Folia 上使用 teleportAsync
        if (SchedulerUtil.isFolia()) {
            this.p.teleportAsync(this.spawn);
        } else {
            this.p.teleport(this.spawn);
        }
    }
    
    public void createApple() {
        final CoordinatePair randomPixel = super.randomPixel();
        for (final CoordinatePair coordinatePair : this.pixels) {
            if (coordinatePair.getX() == randomPixel.getX() && coordinatePair.getY() == randomPixel.getY()) {
                this.createApple();
                return;
            }
        }
        this.apple = randomPixel;
        GameUtils.setWool(this.getWorld().getBlockAt(this.getPixel(randomPixel)), (byte)14);
    }
    
    public void shiftFrame() {
        if (this.direction == null) {
            return;
        }
        int n = 0;
        int n2 = 0;
        switch (this.direction) {
            case RIGHT: {
                n = 1;
                break;
            }
            case DOWN: {
                n2 = -1;
                break;
            }
            case LEFT: {
                n = -1;
                break;
            }
            case UP: {
                n2 = 1;
                break;
            }
        }
        final Block block = this.getWorld().getBlockAt(this.getPixel(this.head.getX(), this.head.getY()));
        if (block.getType() == this.head_mat) {
            block.setType(this.body_mat);
        }
        this.head.setX(this.head.getX() + n);
        this.head.setY(this.head.getY() + n2);
        if (!this.arena.isInBounds(this.getPixel(this.head.getX(), this.head.getY()))) {
            this.die(true);
            return;
        }
        final Iterator<CoordinatePair> iterator = this.pixels.iterator();
        while (iterator.hasNext()) {
            if (this.head.equals(iterator.next())) {
                this.die(false);
                return;
            }
        }
        boolean b = false;
        if (this.head.getX() == this.apple.getX() && this.head.getY() == this.apple.getY()) {
            this.fat += 2;
            ++this.score;
            b = true;
            this.p.playSound(this.p.getLocation(), GameUtils.getOrbPickupSound(), 1.0f, 8.0f);
        }
        if (!this.isActive()) {
            return;
        }
        this.pixels.add(new CoordinatePair(this.head.getX(), this.head.getY()));
        final Block block2 = this.getWorld().getBlockAt(this.getPixel(this.head.getX(), this.head.getY()));
        if (block2.getType() != this.head_mat) {
            block2.setType(this.head_mat);
        }
        if (this.fat == 0) {
            final CoordinatePair coordinatePair = this.pixels.getFirst();
            final Block block3 = this.getWorld().getBlockAt(this.getPixel(coordinatePair.getX(), coordinatePair.getY()));
            if (block3.getType() == this.body_mat) {
                block3.setType(Material.AIR);
            }
            this.pixels.removeFirst();
        }
        else {
            --this.fat;
        }
        if (b) {
            this.createApple();
        }
        this.keylistening = true;
        this.updateActionBar();
    }
    
    public void turn(final Direction direction) {
        int value = direction.getValue();
        if (!this.getArena().isVerticalLayout()) {
            --value;
        }
        else if (this.getArena().getCoordinateRotation() % 2 == 1) {
            value -= 2;
        }
        final int n = (value + this.getArena().getCoordinateRotation()) % 4;
        if (this.direction == null && n == 2) {
            return;
        }
        switch (n % 4) {
            case 0: {
                this.moveRight();
                break;
            }
            case 1: {
                this.moveDown();
                break;
            }
            case 2: {
                this.moveLeft();
                break;
            }
            case 3: {
                this.moveUp();
                break;
            }
        }
    }
    
    public void moveRight() {
        if (this.keylistening && this.direction != Direction.RIGHT && this.direction != Direction.LEFT) {
            this.direction = Direction.RIGHT;
            this.keylistening = false;
        }
    }
    
    public void moveDown() {
        if (this.keylistening && this.direction != Direction.UP && this.direction != Direction.DOWN) {
            this.direction = Direction.DOWN;
            this.keylistening = false;
        }
    }
    
    public void moveLeft() {
        if (this.keylistening && this.direction != Direction.RIGHT && this.direction != Direction.LEFT) {
            this.direction = Direction.LEFT;
            this.keylistening = false;
        }
    }
    
    public void moveUp() {
        if (this.keylistening && this.direction != Direction.UP && this.direction != Direction.DOWN) {
            this.direction = Direction.UP;
            this.keylistening = false;
        }
    }
    
    public void die(final boolean b) {
        if (!this.isActive()) {
            return;
        }
        this.setActive(false);
        this.direction = null;
        Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(this.p, this, this.score));
        this.addScore(this.p, this.score);
        super.end();
        this.getPlugin().teleportToSpawn(this.p, this.getArena(), true);
        this.p.playSound(this.p.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
        final String configString = this.getPlugin().getConfigString("snake.end-msg", "\n§3§m----------------------------------------\n" + (b ? "§c§lYou hit a wall!" : "§c§lYou ran into yourself!") + "\n§bScore: §f" + this.score + " Apple" + ((this.score == 1) ? "" : "s") + "\n§3§m----------------------------------------");
        if (configString.length() > 0) {
            String s = configString.replaceAll("\\Q%score%\\E", "" + this.score).replaceAll("\\Q(s)\\E", (this.score == 1) ? "" : "s");
            if (s.contains("%reason%")) {
                s = s.replaceAll("%reason%", b ? this.getPlugin().getConfig().getString("snake.hit-wall-msg").replaceAll("&", "§") : this.getPlugin().getConfig().getString("snake.ran-into-self-msg").replaceAll("&", "§"));
            }
            this.p.sendMessage(this.getPlugin().sp(this.p, s));
        }
        this.p.setWalkSpeed(0.2f);
        this.reset(true);
    }
    
    @EventHandler
    public void onEnd(final GameEndEvent gameEndEvent) {
        if (gameEndEvent.getGame().getGameType() == this.getGameType() && gameEndEvent.getGame().getArena().getID() == this.getArena().getID()) {
            HandlerList.unregisterAll((Listener)this);
            this.reset(true);
        }
    }
    
    public void reset(final boolean b) {
        // 在 Folia 上，方块操作需要在区域线程中执行
        final Material fillMaterial = (LobbyGames.SERVER_VERSION <= 12) ? Material.AIR : Material.AIR;
        final Material baseMaterial = (LobbyGames.SERVER_VERSION <= 12) ? Material.valueOf("WOOL") : Material.BLACK_WOOL;
        final byte baseData = (LobbyGames.SERVER_VERSION <= 12) ? (byte)15 : (byte)0;
        
        if (SchedulerUtil.isFolia()) {
            // 使用区域调度器执行方块填充
            final Location centerLoc = this.getArena().getCenterPixel();
            Bukkit.getRegionScheduler().run(this.getPlugin(), centerLoc, task -> {
                GameUtils.fill(this.getArena(), fillMaterial, (byte)0, true, baseMaterial, baseData, false);
            });
        } else {
            GameUtils.fill(this.getArena(), fillMaterial, (byte)0, true, baseMaterial, baseData, false);
        }
        
        if (b) {
            this.setActive(false);
            // 使用区域任务而不是全局任务，因为需要设置方块
            final Location arenaCenter = this.getArena().getCenterPixel();
            new me.c7dev.lobbygames.util.GameTask() {
                public void run() {
                    for (int i = 0; i < 3; ++i) {
                        Snake.this.getWorld().getBlockAt(Snake.this.getPixel(Snake.this.headstart.getX() - i, Snake.this.headstart.getY())).setType((i == 0) ? Snake.this.head_mat : Snake.this.body_mat);
                    }
                }
            }.runLocationTaskLater(this.getPlugin(), arenaCenter, 20L);
        }
    }
}
