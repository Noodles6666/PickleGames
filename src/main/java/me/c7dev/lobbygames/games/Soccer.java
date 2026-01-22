// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.games;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntitySpawnEvent;
import me.c7dev.lobbygames.api.events.PlayerQuitLobbyGameEvent;
import org.bukkit.event.EventHandler;
import java.util.Collections;
import org.bukkit.util.Vector;
import me.c7dev.lobbygames.api.events.PlayerJoinLobbyGameEvent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import org.bukkit.Color;
import java.util.Set;
import me.c7dev.lobbygames.api.events.SpectatorQuitLobbyGameEvent;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.SpectatorJoinLobbyGameEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.GameMode;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.util.GameTask;
import java.util.function.Consumer;
import me.c7dev.lobbygames.util.soccer.SlimeSoccerBall;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;
import org.bukkit.Difficulty;
import java.util.ArrayList;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.LobbyGames;
import me.c7dev.lobbygames.Arena;
import org.bukkit.entity.Player;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import me.c7dev.lobbygames.util.soccer.SoccerBallEntity;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import me.c7dev.lobbygames.util.soccer.Ball;
import me.c7dev.lobbygames.util.Spectatable;
import org.bukkit.event.Listener;
import me.c7dev.lobbygames.Game;

public class Soccer extends Game implements Listener, Spectatable
{
    private int ascore;
    private int bscore;
    private int goal_threshold;
    private int max_time;
    private int ball_size;
    private Ball ball;
    private List<UUID> a;
    private List<UUID> b;
    private HashMap<UUID, Long> boost_delay;
    private HashMap<UUID, Long> click_delay;
    private HashMap<UUID, Boolean> spectators;
    private String cooldown_msg;
    private long double_jump_delay;
    private double ball_speed;
    private SoccerBallEntity ball_entity;
    private Location net1a;
    private Location net2a;
    private Location net1b;
    private Location net2b;
    private boolean zplane;
    private boolean goal_registered;
    private boolean start_countdown;
    private boolean boost_enabled;
    private boolean spectators_enabled;
    private List<UUID> teleporting_players;  // Folia: 正在传送中的玩家，暂时忽略onMove检查
    
    public void broadcast(final String s) {
        if (s.length() == 0) {
            return;
        }
        final Iterator<UUID> iterator = this.getAllPlayers().iterator();
        while (iterator.hasNext()) {
            final Player player = Bukkit.getPlayer((UUID)iterator.next());
            if (player != null) {
                player.sendMessage(this.getPlugin().sp(player, s));
            }
        }
    }
    
    public boolean checkNet(final Location location) {
        if (location.getBlockY() > Math.max(this.net1a.getBlockY(), this.net1b.getBlockY())) {
            return false;
        }
        if (Arena.isInBoundsXZ(location, this.net1a, this.net1b)) {
            this.goal(true);
            return true;
        }
        if (Arena.isInBoundsXZ(location, this.net2a, this.net2b)) {
            this.goal(false);
            return true;
        }
        return false;
    }
    
    public Soccer(final LobbyGames lobbyGames, final Arena arena, final Player player) {
        this(lobbyGames, arena, player, null);
    }
    
    public Soccer(final LobbyGames v1, final Arena v2, final Player player, final SoccerBallEntity soccerBallEntity) {
        super(v1, GameType.SOCCER, v2, player);
        this.ascore = 0;
        this.bscore = 0;
        this.goal_threshold = 15;
        this.max_time = -1;
        this.ball_size = 3;
        this.a = new ArrayList<UUID>();
        this.b = new ArrayList<UUID>();
        this.boost_delay = new HashMap<UUID, Long>();
        this.click_delay = new HashMap<UUID, Long>();
        this.spectators = new HashMap<UUID, Boolean>();
        this.teleporting_players = new ArrayList<UUID>();  // Folia: 初始化传送列表
        this.goal_registered = false;
        this.start_countdown = false;
        this.boost_enabled = false;
        this.spectators_enabled = true;
        if (!this.canStart() || v2.getGameType() != GameType.SOCCER) {
            return;
        }
        if (v2.getSpecialLoc1() == null || v2.getSpecialLoc2() == null) {
            if (player.hasPermission("lobbygames.admin")) {
                player.sendMessage("§cThere was an error in starting this game, check the console.");
            }
            Bukkit.getLogger().severe("This soccer game can not start because a soccer net location is undefined!");
            return;
        }
        if (v2.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            if (player.hasPermission("lobbygames.admin")) {
                player.sendMessage("§cThere was an error in starting this game, check the console.");
            }
            Bukkit.getLogger().severe("This soccer game can not start because the world is set to Peaceful difficulty! The ball (a Slime entity) can not spawn.");
            return;
        }
        for (final Entity entity : v2.getWorld().getEntitiesByClass(Slime.class)) {
            if (v2.isInBoundsXZ(entity.getLocation()) && entity.getLocation().getY() >= v2.getLocation1().getBlockY() && entity instanceof Slime) {
                entity.remove();
            }
        }
        this.net1a = v2.getSpecialLoc1().clone();
        this.net1b = v2.getSpecialLoc2().clone();
        if (v2.isInBoundsXZ(this.net1a) || v2.isInBoundsXZ(this.net1b)) {
            if (player.hasPermission("lobbygames.admin")) {
                player.sendMessage("§cThere was an error in starting this game, check the console.");
            }
            Bukkit.getLogger().severe("This soccer game can not start because a soccer net location is inside the arena! (Must define a cube outside)");
            return;
        }
        final int min = Math.min(v2.getLocation1().getBlockX(), v2.getLocation2().getBlockX());
        final int max = Math.max(v2.getLocation1().getBlockX(), v2.getLocation2().getBlockX());
        final int min2 = Math.min(v2.getLocation1().getBlockZ(), v2.getLocation2().getBlockZ());
        final int max2 = Math.max(v2.getLocation1().getBlockZ(), v2.getLocation2().getBlockZ());
        this.zplane = (min <= this.net1a.getBlockX() && this.net1a.getBlockX() <= max);
        this.double_jump_delay = this.getPlugin().getConfig().getLong("soccer.boost-jump-cooldown");
        this.goal_threshold = v1.getConfig().getInt("soccer.point-win-threshold");
        this.max_time = v1.getConfig().getInt("soccer.game-max-time");
        this.boost_enabled = v1.getConfig().getBoolean("soccer.boost-jump-enabled");
        this.cooldown_msg = v1.getConfigString("cooldown-msg", "§cYou must wait %seconds% second(s) to do this!");
        this.spectators_enabled = v1.getConfig().getBoolean("soccer.spectators-enabled", true);
        this.ball_size = v1.getConfig().getInt("soccer.ball-size", 3);
        this.ball_speed = (v1.getConfig().getDouble("soccer.ball-speed", 2.0) + 4.0) / 10.0;
        if (this.ball_size < 1) {
            this.ball_size = 2;
        }
        else if (this.ball_size > 12) {
            this.ball_size = 12;
        }
        if (this.ball_speed < 0.5) {
            this.ball_speed = 0.5;
        }
        else if (this.ball_speed > 1.0) {
            this.ball_speed = 1.0;
        }
        if (this.zplane) {
            final int min3 = Math.min(Math.abs(this.net1a.getBlockZ() - max2), Math.abs(this.net1a.getBlockZ() - min2));
            if (Math.abs(this.net1b.getBlockX() - this.net1a.getBlockX()) + 1 > v2.getHeight()) {
                if (player.hasPermission("lobbygames.admin")) {
                    player.sendMessage("§cThere was an error in starting this game, check the console.");
                }
                Bukkit.getLogger().severe("This soccer game can not start because the 2 soccer net locations encapsulate the arena! They must create a cuboid outside of the arena's bounds.");
                return;
            }
            final int n = (this.net1a.getZ() < min2) ? 1 : -1;
            (this.net2b = this.net1a.clone().add(0.0, 0.0, (double)(n * (v2.getHeight() + 2 * min3 - 1)))).setY(this.net1b.getY());
            (this.net2a = this.net1b.clone().add(0.0, 0.0, (double)(n * (v2.getHeight() + 1)))).setY(this.net1a.getY());
        }
        else {
            final int min4 = Math.min(Math.abs(this.net1a.getBlockX() - max), Math.abs(this.net1a.getBlockX() - min));
            if (Math.abs(this.net1b.getBlockZ() - this.net1a.getBlockZ()) + 1 > v2.getWidth()) {
                if (player.hasPermission("lobbygames.admin")) {
                    player.sendMessage("§cThere was an error in starting this game, check the console.");
                }
                Bukkit.getLogger().severe("This soccer game can not start because the 2 soccer net locations encapsulate the arena! They must create a cuboid outside of the arena's bounds.");
                return;
            }
            final int n2 = (this.net1a.getX() < min) ? 1 : -1;
            (this.net2b = this.net1a.clone().add((double)(n2 * (v2.getWidth() + 2 * min4 - 1)), 0.0, 0.0)).setY(this.net1b.getY());
            (this.net2a = this.net1b.clone().add((double)(n2 * (v2.getWidth() + 1)), 0.0, 0.0)).setY(this.net1a.getY());
        }
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)v1);
        if (!v2.isInBoundsXZ(player.getLocation())) {
            v1.setReturnLocation(player);
            // Folia: 标记玩家正在传送，使用异步传送
            this.teleporting_players.add(player.getUniqueId());
            player.teleportAsync(v2.getCenterPixel()).thenAccept(result -> {
                // 传送完成后，延迟1秒再从列表中移除
                new GameTask() {
                    public void run() {
                        Soccer.this.teleporting_players.remove(player.getUniqueId());
                    }
                }.runTaskLater((Plugin)v1, 20L);
            });
        }
        // Folia: 玩家操作必须在实体任务中执行
        new GameTask() {
            public void run() {
                Soccer.this.preparePlayer(player);
                Soccer.this.giveItems(player);
                if (Soccer.this.boost_enabled) {
                    player.setAllowFlight(true);
                }
            }
        }.runEntityTask((Plugin)v1, player);
        if (soccerBallEntity == null) {
            this.ball_entity = new SlimeSoccerBall();
        }
        else {
            this.setSoccerBallEntity(soccerBallEntity);
        }
        this.ball_entity.setClickConsumer(new Consumer<Player>() {
            @Override
            public void accept(final Player player) {
                Soccer.this.sendPlayerClickBall(player);
            }
        });
        this.ball = new Ball(v2.getCenterPixel().clone().add(0.0, 1.0, 0.0), this);
        if (this.max_time > 0 && this.max_time < 60) {
            this.max_time = 60;
        }
        // Folia: 使用GameTask替代BukkitRunnable
        new GameTask() {
            public void run() {
                if (!Soccer.this.canStart()) {
                    this.cancel();
                }
                Soccer.this.updateActionBar();
                if (Soccer.this.max_time > 0 && Soccer.this.getDuration() > Soccer.this.max_time && Soccer.this.ascore != Soccer.this.bscore) {
                    Soccer.this.win(Soccer.this.ascore > Soccer.this.bscore);
                }
            }
        }.runTaskTimer((Plugin)v1, 1L, 20L);
        // Folia: 球物理任务使用区域任务
        final Location arenaCenter = v2.getCenterPixel();
        new GameTask() {
            public void run() {
                if (!Soccer.this.canStart()) {
                    this.cancel();
                    return;
                }
                if (Soccer.this.ball == null) {
                    return;
                }
                Soccer.this.ball.tick(v2.getLocation1().getY());
                final Location location = Soccer.this.ball.getLocation();
                final Location location2 = v2.getLocation1();
                final Location location3 = v2.getLocation2();
                if (location2.getX() < location3.getX()) {
                    if (((location.getBlockX() < location2.getX() && Soccer.this.ball.getVelocity().getX() < 0.0) || (location.getBlockX() > location3.getX() && Soccer.this.ball.getVelocity().getX() > 0.0)) && (Soccer.this.zplane || !Soccer.this.checkNet(location))) {
                        Soccer.this.ball.setVelocity(Soccer.this.ball.getVelocity().setX(-0.8 * Soccer.this.ball.getVelocity().getX()), false);
                    }
                }
                else if (((location.getBlockX() > location2.getX() && Soccer.this.ball.getVelocity().getX() > 0.0) || (location.getBlockX() < location3.getX() && Soccer.this.ball.getVelocity().getX() < 0.0)) && (Soccer.this.zplane || !Soccer.this.checkNet(location))) {
                    Soccer.this.ball.setVelocity(Soccer.this.ball.getVelocity().setX(-0.8 * Soccer.this.ball.getVelocity().getX()), false);
                }
                if (location2.getZ() < location3.getZ()) {
                    if (((location.getBlockZ() < location2.getZ() && Soccer.this.ball.getVelocity().getZ() < 0.0) || (location.getBlockZ() > location3.getZ() && Soccer.this.ball.getVelocity().getZ() > 0.0)) && (!Soccer.this.zplane || !Soccer.this.checkNet(Soccer.this.ball.getLocation()))) {
                        Soccer.this.ball.setVelocity(Soccer.this.ball.getVelocity().setZ(-0.8 * Soccer.this.ball.getVelocity().getZ()), false);
                    }
                }
                else if (((location.getBlockZ() > location2.getZ() && Soccer.this.ball.getVelocity().getZ() > 0.0) || (location.getBlockZ() < location3.getZ() && Soccer.this.ball.getVelocity().getZ() < 0.0)) && (!Soccer.this.zplane || !Soccer.this.checkNet(Soccer.this.ball.getLocation()))) {
                    Soccer.this.ball.setVelocity(Soccer.this.ball.getVelocity().setZ(-0.8 * Soccer.this.ball.getVelocity().getZ()), false);
                }
            }
        }.runLocationTaskTimer((Plugin)v1, arenaCenter, 1L, 1L);
    }
    
    public List<UUID> getAllPlayers() {
        final ArrayList list = new ArrayList();
        final Iterator<UUID> iterator = super.getPlayers().iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        final Iterator<UUID> iterator2 = this.spectators.keySet().iterator();
        while (iterator2.hasNext()) {
            list.add(iterator2.next());
        }
        return list;
    }
    
    public int getBallSize() {
        return this.ball_size;
    }
    
    public double getBallSpeed() {
        return this.ball_speed;
    }
    
    @Override
    public String getScore() {
        if (!this.isActive()) {
            return "-";
        }
        return this.getPlugin().getConfigString("soccer.score-format", "§9§l" + this.ascore + "§7§l - §c§l" + this.bscore).replaceAll("\\Q%team1_score%\\E", "" + this.ascore).replaceAll("\\Q%team2_score%\\E", "" + this.bscore);
    }
    
    @Override
    public int getScoreInt(final Player player) {
        if (!this.containsPlayer(player)) {
            return 0;
        }
        return this.a.contains(player.getUniqueId()) ? this.ascore : this.bscore;
    }
    
    @Override
    public String getScore(final Player player) {
        return "" + this.getScoreInt(player);
    }
    
    public SoccerBallEntity getBallEntity() {
        return this.ball_entity;
    }
    
    public void setSoccerBallEntity(final SoccerBallEntity ball_entity) {
        if (ball_entity == null) {
            throw new IllegalArgumentException("Soccer ball entity interface can not be null!");
        }
        Location location = null;
        if (this.ball_entity != null) {
            location = this.ball_entity.getLocation();
            this.ball_entity.remove();
        }
        this.ball_entity = ball_entity;
        if (location != null) {
            this.ball_entity.spawn(location);
        }
    }
    
    public String getWinCountdown() {
        String s = "-";
        if (this.max_time > 0) {
            int n = this.max_time - this.getDuration();
            if (n < 0) {
                n = 0;
            }
            s = n / 60 + ":" + GameUtils.formatTime(n % 60);
        }
        return s;
    }
    
    public void updateActionBar() {
        if (this.goal_registered) {
            return;
        }
        final String replaceAll = (this.isActive() ? this.getPlugin().getConfigString("soccer.action-bar", "%score%") : this.getPlugin().getConfigString("waiting-players", "§7Waiting for more players to join...")).replaceAll("\\Q%score%\\E", this.getScore()).replaceAll("\\Q%player_count%\\E", "" + this.getPlayers().size()).replaceAll("\\Q%timer%\\E", this.getWinCountdown());
        final Iterator<UUID> iterator = this.getAllPlayers().iterator();
        while (iterator.hasNext()) {
            this.getPlugin().sendActionbar(Bukkit.getPlayer((UUID)iterator.next()), replaceAll, true);
        }
    }
    
    public void appendSpectator(final Player player) {
        if (!this.spectators_enabled || this.containsPlayer(player) || this.spectators.containsKey(player.getUniqueId())) {
            return;
        }
        this.spectators.put(player.getUniqueId(), player.getAllowFlight());
        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        }
        player.setAllowFlight(true);
        Bukkit.getPluginManager().callEvent((Event)new SpectatorJoinLobbyGameEvent(player, this));
    }
    
    public void removeSpectator(final Player player) {
        if (!this.spectators.containsKey(player.getUniqueId()) || this.containsPlayer(player)) {
            return;
        }
        player.setAllowFlight((boolean)this.spectators.get(player.getUniqueId()));
        this.spectators.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        Bukkit.getPluginManager().callEvent((Event)new SpectatorQuitLobbyGameEvent(player, this));
    }
    
    public Set<UUID> getSpectators() {
        return this.spectators.keySet();
    }
    
    public void goal(final boolean b) {
        if (this.goal_registered) {
            return;
        }
        if (b) {
            if (this.isActive()) {
                ++this.ascore;
            }
            GameUtils.spawnFirework(this.ball.getLocation().add(0.0, 3.0, 0.0), Color.BLUE);
            if (this.ascore >= this.goal_threshold && this.max_time <= 0) {
                this.win(true);
                return;
            }
            final String replaceAll = this.getPlugin().getConfigString("soccer.team1-score-msg", "§9§lBLUE team scoared a goal! %score%").replaceAll("\\Q%score%\\E", this.getScore());
            final Iterator<UUID> iterator = this.getAllPlayers().iterator();
            while (iterator.hasNext()) {
                final Player player = Bukkit.getPlayer((UUID)iterator.next());
                if (player != null) {
                    player.sendMessage(this.getPlugin().sp(player, replaceAll));
                    player.playSound(player.getLocation(), GameUtils.getOrbPickupSound(), 1.0f, 8.0f);
                }
            }
        }
        else {
            if (this.isActive()) {
                ++this.bscore;
            }
            GameUtils.spawnFirework(this.ball.getLocation().add(0.0, 3.0, 0.0), Color.RED);
            if (this.bscore >= this.goal_threshold && this.max_time <= 0) {
                this.win(false);
                return;
            }
            final String replaceAll2 = this.getPlugin().getConfigString("soccer.team2-score-msg", "§c§lRED team scored a goal! %score").replaceAll("\\Q%score%\\E", this.getScore());
            final Iterator<UUID> iterator2 = this.getAllPlayers().iterator();
            while (iterator2.hasNext()) {
                final Player player2 = Bukkit.getPlayer((UUID)iterator2.next());
                if (player2 != null) {
                    player2.sendMessage(this.getPlugin().sp(player2, replaceAll2));
                    player2.playSound(player2.getLocation(), GameUtils.getOrbPickupSound(), 1.0f, 8.0f);
                }
            }
        }
        this.goal_registered = true;
        // Folia: 使用GameTask替代BukkitRunnable
        new GameTask() {
            private final /* synthetic */ int val$respawn_seconds = Soccer.this.plugin.getConfig().getInt("soccer.ball-respawn-delay");
            
            public void run() {
                // Folia: 球实体移除必须在区域任务中执行
                if (Soccer.this.ball != null) {
                    final Location ballLoc = Soccer.this.ball.getLocation();
                    final Ball ballToRemove = Soccer.this.ball;
                    Soccer.this.ball = null;
                    new GameTask() {
                        public void run() {
                            ballToRemove.remove();
                        }
                    }.runLocationTask((Plugin)Soccer.this.getPlugin(), ballLoc);
                }
                
                if (this.val$respawn_seconds == 0) {
                    Soccer.this.goal_registered = false;
                    // Folia: 球创建需要在区域任务中执行（因为Ball构造函数会调用setSize等实体操作）
                    final Location spawnLoc = Soccer.this.getArena().getCenterPixel().clone().add(0.0, 10.0, 0.0);
                    new GameTask() {
                        public void run() {
                            Soccer.this.ball = new Ball(spawnLoc, Soccer.this);
                        }
                    }.runLocationTask((Plugin)Soccer.this.getPlugin(), spawnLoc);
                    return;
                }
                final int respawnSeconds = this.val$respawn_seconds;
                // Folia: 使用GameTask替代BukkitRunnable
                new GameTask() {
                    int seconds = respawnSeconds;
                    String respawn_str = Soccer.this.plugin.getConfigString("soccer.ball-respawn-msg", "§eThe ball will respawn in §c%seconds% §esecond(s)!");
                    
                    public void run() {
                        if (this.seconds > 0) {
                            final Iterator<UUID> iterator = Soccer.this.getAllPlayers().iterator();
                            while (iterator.hasNext()) {
                                Soccer.this.getPlugin().sendActionbar(Bukkit.getPlayer((UUID)iterator.next()), this.respawn_str.replaceAll("\\Q%seconds%\\E", "" + this.seconds).replaceAll("\\Q(s)\\E", (this.seconds == 1) ? "" : "s").replaceAll("\\Q%timer%\\E", Soccer.this.getWinCountdown()), false);
                            }
                            --this.seconds;
                        }
                        else {
                            this.cancel();
                            Soccer.this.goal_registered = false;
                            // Folia: 球创建需要在区域任务中执行
                            final Location spawnLoc = Soccer.this.getArena().getCenterPixel().clone().add(0.0, 10.0, 0.0);
                            new GameTask() {
                                public void run() {
                                    Soccer.this.ball = new Ball(spawnLoc, Soccer.this);
                                }
                            }.runLocationTask((Plugin)Soccer.this.getPlugin(), spawnLoc);
                            Soccer.this.updateActionBar();
                        }
                    }
                }.runTaskTimer((Plugin)Soccer.this.getPlugin(), 1L, 20L);
            }
        }.runTaskLater((Plugin)super.getPlugin(), 7L);
    }
    
    public void win(final boolean b) {
        if (!this.canStart()) {
            return;
        }
        this.setActive(false);
        this.broadcast(b ? this.getPlugin().getConfigString("soccer.team1-win-msg", "§9§lBLUE team wins the game!") : this.getPlugin().getConfigString("soccer.team2-win-msg", "§c§lRED team wins the game!"));
        final ArrayList elements = new ArrayList();
        final ArrayList elements2 = new ArrayList();
        for (final UUID uuid : this.getPlayers()) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if ((b ? this.a : this.b).contains(uuid)) {
                    Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(player, this, b ? this.ascore : this.bscore));
                    elements.add(player.getName());
                    this.addScore(player, 1);
                    this.setIsWinner(uuid, true);
                }
                else {
                    elements2.add(player.getName());
                    this.setIsWinner(uuid, false);
                }
                if (this.getArena().getSpawn1() != null) {
                    // Folia: 使用teleportAsync而不是teleport
                    player.teleportAsync(this.getArena().getSpawn1());
                }
            }
        }
        final String join = String.join(", ", elements);
        final String join2 = String.join(", ", elements2);
        final String string = this.getPlugin().getConfig().getString("soccer.console-command-on-end-per-player");
        if (string != null) {
            String[] split;
            for (int length = (split = string.split("\\Q[newline]\\E|\n")).length, i = 0; i < length; ++i) {
                String s = split[i].replaceAll("\\Q%score%\\E", this.getScore()).replaceAll("\\Q%winning_player_list%\\E", join).replaceAll("\\Q%losing_player_list%\\E", join2).replace('&', '§').trim();
                if (s.startsWith("\\")) {
                    s = s.substring(1);
                }
                if (s.length() != 0) {
                    for (final UUID uuid2 : this.getPlayers()) {
                        if ((b ? this.a : this.b).contains(uuid2)) {
                            final Player player2 = Bukkit.getPlayer(uuid2);
                            if (player2 == null) {
                                continue;
                            }
                            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), this.getPlugin().sp(player2, s.replaceAll("\\Q%player%\\E|\\Q%winner%\\E", player2.getName())));
                        }
                    }
                }
            }
        }
        this.end();
    }
    
    public void randomTeam(final Player player) {
        this.assignTeam(player, (this.a.size() == this.b.size()) ? this.random.nextBoolean() : (this.a.size() < this.b.size()));
    }
    
    public void assignTeam(final Player player, final boolean b) {
        if (!this.containsPlayer(player)) {
            return;
        }
        if ((b && this.a.contains(player.getUniqueId())) || (!b && this.b.contains(player.getUniqueId()))) {
            return;
        }
        if (b) {
            this.a.add(player.getUniqueId());
            this.b.remove(player.getUniqueId());
            player.sendMessage(this.getPlugin().getConfigString("soccer.team1-wool-title", "§bYou are on the §b§lBLUE§b team!"));
        }
        else {
            this.b.add(player.getUniqueId());
            this.a.remove(player.getUniqueId());
            player.sendMessage(this.getPlugin().getConfigString("soccer.team2-wool-title", "§cYou are on the §c§lRED§c team!"));
        }
        final Color color = b ? Color.fromRGB(75, 75, 255) : Color.fromRGB(255, 75, 75);
        // Folia: 物品栏操作必须在实体任务中执行
        new GameTask() {
            public void run() {
                player.getInventory().setHelmet(GameUtils.createArmor(Material.LEATHER_HELMET, color));
                player.getInventory().setChestplate(GameUtils.createArmor(Material.LEATHER_CHESTPLATE, color));
                player.getInventory().setLeggings(GameUtils.createArmor(Material.LEATHER_LEGGINGS, color));
                player.getInventory().setBoots(GameUtils.createArmor(Material.LEATHER_BOOTS, color));
            }
        }.runEntityTask((Plugin)this.getPlugin(), player);
    }
    
    public void giveItems(final Player player) {
        GameUtils.clearInv(player);
        player.getInventory().setItem(3, GameUtils.createWool(1, 3, this.getPlugin().getConfigString("soccer.team1-join-title", "&bJoin blue team &7(Right-click)"), new String[0]));
        player.getInventory().setItem(5, GameUtils.createWool(1, 14, this.getPlugin().getConfigString("soccer.team2-join-title", "&cJoin red team &7(Right-click)"), new String[0]));
        player.getInventory().setItem(8, this.getPlugin().getQuitItem());
    }
    
    public void removeAllSpectators() {
        final Location spawn1 = this.getArena().getSpawn1();
        UUID[] array;
        for (int length = (array = this.spectators.keySet().toArray(new UUID[this.spectators.size()])).length, i = 0; i < length; ++i) {
            final Player player = Bukkit.getPlayer(array[i]);
            if (player != null) {
                this.removeSpectator(player);
                player.teleport(spawn1);
            }
        }
    }
    
    @EventHandler
    private void onJoin(final PlayerJoinLobbyGameEvent playerJoinLobbyGameEvent) {
        if (!this.canStart() || this.isActive() || playerJoinLobbyGameEvent.getGame().getGameType() != GameType.SOCCER || playerJoinLobbyGameEvent.getGame().getArena().getID() != this.getArena().getID()) {
            return;
        }
        final Player joiningPlayer = playerJoinLobbyGameEvent.getPlayer();
        
        if (!this.getArena().isInBoundsXZ(joiningPlayer.getLocation())) {
            // Folia: 标记玩家正在传送
            this.teleporting_players.add(joiningPlayer.getUniqueId());
            joiningPlayer.teleportAsync(this.getArena().getCenterPixel()).thenAccept(result -> {
                // 传送完成后，延迟1秒再从列表中移除
                new GameTask() {
                    public void run() {
                        Soccer.this.teleporting_players.remove(joiningPlayer.getUniqueId());
                    }
                }.runTaskLater((Plugin)Soccer.this.getPlugin(), 20L);
            });
        }
        
        // Folia: 玩家操作必须在实体线程中执行
        new GameTask() {
            public void run() {
                Soccer.this.removeSpectator(joiningPlayer);
                Soccer.this.preparePlayer(joiningPlayer);
                Soccer.this.giveItems(joiningPlayer);
            }
        }.runEntityTask((Plugin)this.getPlugin(), joiningPlayer);
        
        int int1 = this.getPlugin().getConfig().getInt("soccer.player-join-threshold");
        if (int1 < 2) {
            int1 = 2;
        }
        final int n = int1;
        
        if (!this.start_countdown && this.getPlayers().size() >= n) {
            this.start_countdown = true;
            // Folia: 使用GameTask替代BukkitRunnable
            new GameTask() {
                int c = 0;
                private final /* synthetic */ int val$seconds = Soccer.this.plugin.getConfig().getInt("soccer.countdown-seconds");
                private final /* synthetic */ String val$format = Soccer.this.plugin.getConfigString("countdown-format", "&eThe game will start in &c%seconds%&e seconds!");
                private final /* synthetic */ int val$required_players = n;  // 保存所需玩家数
                
                public void run() {
                    if (!Soccer.this.canStart()) {
                        this.cancel();
                        return;
                    }
                    final int remainingSeconds = this.val$seconds - this.c;
                    
                    if (remainingSeconds == 0) {
                        this.cancel();
                        Soccer.this.setActive(true);
                        Soccer.this.ball.reload(Soccer.this.ball.getLocation());
                        Soccer.this.ball.setVelocity(new Vector(0.0, 0.5, 0.0), false);
                        final String configString = Soccer.this.getPlugin().getConfigString("soccer.start-msg", "§3§m----------------------------------------\n§b§lSoccer: §bPunch the ball into your team's net to win points!\n§3§m----------------------------------------");
                        final String configString2 = Soccer.this.getPlugin().getConfigString("soccer.boost-jump-title", "§a§lBoost Jump");
                        for (final UUID uuid : Soccer.this.getPlayers()) {
                            if (Soccer.this.isInQuitConfirmation(uuid)) {
                                Soccer.this.removeQuitConfirmation(uuid);
                            }
                            if (!Soccer.this.a.contains(uuid) && !Soccer.this.b.contains(uuid)) {
                                final Player player = Bukkit.getPlayer(uuid);
                                if (player == null) {
                                    continue;
                                }
                                Soccer.this.randomTeam(player);
                            }
                        }
                        final List<UUID> players = Soccer.this.getPlayers();
                        Collections.shuffle(players);
                        
                        // Folia: 物品栏操作需要在实体任务中执行
                        for (final UUID uuid : players) {
                            final Player player2 = Bukkit.getPlayer(uuid);
                            if (player2 != null) {
                                new GameTask() {
                                    public void run() {
                                        player2.sendMessage(Soccer.this.getPlugin().sp(player2, configString));
                                        GameUtils.clearInv(player2);
                                        if (Math.abs(Soccer.this.a.size() - Soccer.this.b.size()) > 1) {
                                            Soccer.this.randomTeam(player2);
                                        }
                                        if (Soccer.this.a.contains(player2.getUniqueId())) {
                                            player2.getInventory().setItem(4, GameUtils.createWool(1, 3, Soccer.this.getPlugin().getConfigString("soccer.team1-wool-title", "§bYou are on the §b§lBLUE§b team!"), new String[0]));
                                        }
                                        else {
                                            player2.getInventory().setItem(4, GameUtils.createWool(1, 14, Soccer.this.getPlugin().getConfigString("soccer.team2-wool-title", "§cYou are on the §c§lRED§c team!"), new String[0]));
                                        }
                                        if (Soccer.this.boost_enabled) {
                                            player2.getInventory().setItem(0, GameUtils.createItem(Material.FEATHER, 1, (byte)0, configString2, new String[0]));
                                        }
                                    }
                                }.runEntityTask((Plugin)Soccer.this.getPlugin(), player2);
                            }
                        }
                    }
                    else if (Soccer.this.getPlayers().size() < this.val$required_players) {
                        this.cancel();
                        Soccer.this.broadcast(Soccer.this.getPlugin().getConfigString("waiting-players", "§cWaiting for more players to join..."));
                        Soccer.this.start_countdown = false;
                    }
                    else if (this.c == 0 || remainingSeconds % 5 == 0) {
                        Soccer.this.broadcast(this.val$format.replaceAll("\\Q%seconds%\\E", "" + remainingSeconds));
                    }
                    ++this.c;
                }
            }.runTaskTimer((Plugin)this.getPlugin(), 1L, 20L);
        }
    }
    
    private void doubleJump(final Player player) {
        if (!this.boost_enabled) {
            return;
        }
        if (!this.boost_delay.containsKey(player.getUniqueId()) || this.boost_delay.get(player.getUniqueId()) < System.currentTimeMillis()) {
            this.boost_delay.put(player.getUniqueId(), System.currentTimeMillis() + 1000L * this.double_jump_delay);
            player.setVelocity(new Vector(player.getVelocity().normalize().getX() * 1.8, 0.8, player.getVelocity().normalize().getZ() * 1.8));
        }
        else {
            int n = (int)(this.boost_delay.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
            ++n;
            this.getPlugin().sendActionbar(player, this.cooldown_msg.replaceAll("\\Q%seconds%\\E", "" + n).replaceAll("\\Q(s)\\E", (n == 1) ? "" : "s"), false);
        }
    }
    
    private boolean ballContainsUUID(final UUID uuid) {
        return this.ball != null && this.ball.containsUUID(uuid);
    }
    
    @EventHandler
    private void onQuit(final PlayerQuitLobbyGameEvent playerQuitLobbyGameEvent) {
        if (!this.canStart()) {
            return;
        }
        this.a.remove(playerQuitLobbyGameEvent.getPlayer().getUniqueId());
        this.b.remove(playerQuitLobbyGameEvent.getPlayer().getUniqueId());
        if (this.isActive()) {
            if (this.a.size() == 0) {
                this.win(false);
            }
            else if (this.b.size() == 0) {
                this.win(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    private void onSpawn(final EntitySpawnEvent entitySpawnEvent) {
        if (entitySpawnEvent.getEntity() instanceof Slime && ((this.ball == null && this.getArena().isInBoundsXZ(entitySpawnEvent.getEntity().getLocation())) || this.ballContainsUUID(entitySpawnEvent.getEntity().getUniqueId()))) {
            entitySpawnEvent.setCancelled(false);
        }
    }
    
    @EventHandler
    private void onDeath(final EntityDeathEvent entityDeathEvent) {
        if (!this.canStart() || !this.ballContainsUUID(entityDeathEvent.getEntity().getUniqueId())) {
            return;
        }
        entityDeathEvent.setDroppedExp(0);
        this.ball.reload(entityDeathEvent.getEntity().getLocation());
    }
    
    @EventHandler
    private void onMove(final PlayerMoveEvent playerMoveEvent) {
        if (!this.canStart()) {
            return;
        }
        final Player player = playerMoveEvent.getPlayer();
        
        // Folia: 如果玩家正在传送中，暂时忽略移动检查
        if (this.teleporting_players.contains(player.getUniqueId())) {
            return;
        }
        
        boolean inBoundsXZ = this.getArena().isInBoundsXZ(player.getLocation()) || 
                             Arena.isInBoundsXZ(player.getLocation(), this.net1a, this.net1b) || 
                             Arena.isInBoundsXZ(player.getLocation(), this.net2a, this.net2b);
        boolean sameWorld = player.getWorld().getName().equals(this.getArena().getCenterPixel().getWorld().getName());
        int n = (inBoundsXZ && sameWorld) ? 1 : 0;
        
        if (n != 0) {
            final double n2 = player.getLocation().getY() - this.getArena().getCenterPixel().getY();
            // Folia: 允许玩家在竞技场中心上下各11格范围内
            if (n2 > 11.0 || n2 < -11.0) {
                Bukkit.getLogger().info("[Soccer Debug] Player " + player.getName() + " Y offset out of range: " + n2);
                n = 0;
            }
        }
        
        if (this.containsPlayer(player)) {
            if (n == 0) {
                if (this.isActive()) {
                    // 游戏进行中，玩家出界算输
                    if (this.a.contains(player.getUniqueId())) {
                        if (this.a.size() == 1) {
                            this.win(false);
                            return;
                        }
                    }
                    else if (this.b.size() == 1) {
                        this.win(true);
                        return;
                    }
                    this.noEndTeleportation(player.getUniqueId());
                    this.removePlayer(player);
                } else {
                    // 游戏未开始（倒计时期间），将玩家传送回中心
                    this.teleporting_players.add(player.getUniqueId());
                    player.teleportAsync(this.getArena().getCenterPixel()).thenAccept(result -> {
                        new GameTask() {
                            public void run() {
                                Soccer.this.teleporting_players.remove(player.getUniqueId());
                            }
                        }.runTaskLater((Plugin)this.getPlugin(), 20L);
                    });
                }
            }
        }
        else if (n != 0) {
            if (!this.isActive()) {
                if (this.getPlugin().soccerProximityJoining()) {
                    final double n3 = playerMoveEvent.getPlayer().getLocation().getY() - this.getArena().getLocation1().getY();
                    if (n3 >= 0.0 && n3 <= 5.0) {
                        this.appendPlayer(playerMoveEvent.getPlayer());
                    }
                }
                else {
                    this.removeSpectator(playerMoveEvent.getPlayer());
                }
            }
            else {
                this.appendSpectator(playerMoveEvent.getPlayer());
            }
        }
        else if (this.spectators.containsKey(playerMoveEvent.getPlayer().getUniqueId())) {
            this.removeSpectator(playerMoveEvent.getPlayer());
        }
    }
    
    @EventHandler
    private void onInteract(final PlayerInteractEvent playerInteractEvent) {
        if (!this.canStart()) {
            return;
        }
        if (this.containsPlayer(playerInteractEvent.getPlayer())) {
            final ItemStack handItem = GameUtils.getHandItem(playerInteractEvent.getPlayer());
            if (handItem != null) {
                if (handItem.getType() == this.getPlugin().getQuitItem().getType()) {
                    if (this.isActive()) {
                        this.quitConfirmation(playerInteractEvent.getPlayer());
                    }
                    else {
                        this.removePlayer(playerInteractEvent.getPlayer());
                    }
                }
                else if (handItem.getType() == Material.FEATHER && playerInteractEvent.getAction() != Action.LEFT_CLICK_AIR && playerInteractEvent.getAction() != Action.LEFT_CLICK_BLOCK) {
                    this.doubleJump(playerInteractEvent.getPlayer());
                }
                else if (handItem.getType().toString().endsWith("WOOL") && !this.isInQuitConfirmation(playerInteractEvent.getPlayer().getUniqueId())) {
                    if (this.isActive()) {
                        return;
                    }
                    if (GameUtils.getData(handItem) == 14) {
                        this.assignTeam(playerInteractEvent.getPlayer(), false);
                    }
                    else if (GameUtils.getData(handItem) == 3) {
                        this.assignTeam(playerInteractEvent.getPlayer(), true);
                    }
                }
            }
        }
    }
    
    @EventHandler
    private void onEnd(final GameEndEvent gameEndEvent) {
        if (gameEndEvent.getGame().getGameType() == GameType.SOCCER && gameEndEvent.getGame().getArena().getID() == this.getArena().getID()) {
            this.setActive(false);
            // Folia: 实体移除必须在实体所在区域的线程中执行
            if (this.ball != null && this.ball.getLocation() != null) {
                final Location ballLoc = this.ball.getLocation();
                new GameTask() {
                    public void run() {
                        Soccer.this.ball.remove();
                    }
                }.runLocationTask((Plugin)this.getPlugin(), ballLoc);
            }
            HandlerList.unregisterAll((Listener)this);
            this.removeAllSpectators();
        }
    }
    
    @EventHandler
    private void onDamage(final EntityDamageEvent entityDamageEvent) {
        if (!this.canStart()) {
            return;
        }
        if (entityDamageEvent.getEntity() instanceof Slime && this.ballContainsUUID(entityDamageEvent.getEntity().getUniqueId())) {
            entityDamageEvent.setCancelled(true);
        }
    }
    
    @EventHandler
    private void onDoubleJump(final PlayerToggleFlightEvent playerToggleFlightEvent) {
        if (!this.boost_enabled || !this.canStart() || !this.containsPlayer(playerToggleFlightEvent.getPlayer())) {
            return;
        }
        playerToggleFlightEvent.setCancelled(true);
        final Player player = playerToggleFlightEvent.getPlayer();
        // Folia: 物品栏操作必须在实体任务中执行
        new GameTask() {
            public void run() {
                player.setAllowFlight(false);
            }
        }.runEntityTask((Plugin)this.getPlugin(), player);
        // Folia: 使用GameTask替代BukkitRunnable
        new GameTask() {
            public void run() {
                if (Soccer.this.canStart() && Soccer.this.containsPlayer(player)) {
                    player.setAllowFlight(true);
                }
            }
        }.runEntityTaskLater((Plugin)this.getPlugin(), player, 20L * this.double_jump_delay - 1L);
        this.doubleJump(player);
    }
    
    @EventHandler
    private void onClick(final EntityDamageByEntityEvent entityDamageByEntityEvent) {
        if (!this.canStart()) {
            return;
        }
        if (this.ballContainsUUID(entityDamageByEntityEvent.getEntity().getUniqueId()) && this.getPlayers().contains(entityDamageByEntityEvent.getDamager().getUniqueId())) {
            entityDamageByEntityEvent.setCancelled(true);
            this.sendPlayerClickBall((Player)entityDamageByEntityEvent.getDamager());
        }
    }
    
    public void sendPlayerClickBall(final Player player) {
        final UUID uniqueId = player.getUniqueId();
        if (!this.getPlayers().contains(uniqueId)) {
            return;
        }
        if (System.currentTimeMillis() - this.click_delay.getOrDefault(uniqueId, 0L) < 80L) {
            return;
        }
        this.click_delay.put(uniqueId, System.currentTimeMillis());
        if (this.isActive()) {
            this.ball.setVelocity(player.getLocation().getDirection(), true);
        }
        else {
            this.ball.setVelocity(new Vector(0.0, 0.5, 0.0), true);
        }
    }
    
    public Ball getBall() {
        return this.ball;
    }
}
