// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.games;

import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import me.c7dev.lobbygames.api.events.PlayerQuitLobbyGameEvent;
import me.c7dev.lobbygames.util.ArmorStandFactory;
import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import me.c7dev.lobbygames.util.CoordinatePair;
import org.bukkit.util.Vector;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.EventHandler;
import me.c7dev.lobbygames.api.events.PlayerJoinLobbyGameEvent;
import java.util.Iterator;
import org.bukkit.Sound;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;
import me.c7dev.lobbygames.util.GameTask;
import me.c7dev.lobbygames.util.SchedulerUtil;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import me.c7dev.lobbygames.util.GameUtils;
import org.bukkit.Material;
import java.util.ArrayList;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.entity.ArmorStand;
import me.c7dev.lobbygames.util.BilliardBall;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import me.c7dev.lobbygames.Game;

public class Pool extends Game implements Listener
{
    private Player p1;
    private Player p2;
    private Random random;
    private boolean p1_turn;
    private boolean in_play;
    private boolean sides_assigned;
    private boolean p1_solid;
    private boolean cueball_inhand;
    private boolean first_draw;
    private boolean collision;
    private boolean xplane;
    private boolean mw4;
    private boolean same_color;
    private boolean practice;
    private boolean can_practice;
    private boolean started_instance;
    private boolean ball_moved;
    private boolean legacy_mode;
    private boolean pocket_use_euclidean;
    private String your_turn;
    private String opp_turn;
    private String prac_mode_str;
    private String exit_prac_mode;
    private String[] words;
    private String[] power_words;
    private List<Integer> sunk_balls;
    private HashMap<UUID, Long> timeout;
    private int solids_sc;
    private int stripes_sc;
    private byte p1_power;
    private byte p2_power;
    private byte rail_hits;
    private byte[] power_bytes;
    private double pocket_radius;
    private ItemStack cue;
    private ItemStack book;
    private Location[] holes;
    private BilliardBall[] balls;
    private ArmorStand[] holes_as;
    public static final int BALL = 0;
    public static final int CUE_BALL = 1;
    public static final int POCKETED = 2;
    public static final int WOOL = 3;
    public static final int TERRACOTTA = 4;
    
    public Pool(final LobbyGames v1, final Arena v2, final Player player) {
        super(v1, GameType.POOL, v2, player);
        this.random = new Random();
        this.p1_turn = true;
        this.in_play = false;
        this.sides_assigned = false;
        this.p1_solid = false;
        this.cueball_inhand = false;
        this.first_draw = true;
        this.collision = false;
        this.same_color = false;
        this.practice = false;
        this.can_practice = true;
        this.started_instance = false;
        this.ball_moved = false;
        this.legacy_mode = false;
        this.pocket_use_euclidean = false;
        this.words = new String[] { "Ball", "Cue Ball", "Pocketed", "Wool", "Terracotta" };
        this.power_words = new String[] { "§aLow Power", "§eMedium Power", "§6High Power", "§cHighest Power" };
        this.sunk_balls = new ArrayList<Integer>();
        this.timeout = new HashMap<UUID, Long>();
        this.solids_sc = 5;
        this.stripes_sc = 1;
        this.p1_power = 2;
        this.p2_power = 2;
        this.rail_hits = 0;
        this.power_bytes = new byte[] { 5, 4, 1, 14 };
        this.holes_as = new ArmorStand[6];
        if (!this.canStart() || v2.getGameType() != GameType.POOL) {
            return;
        }
        this.p1 = player;
        this.p1_turn = this.random.nextBoolean();
        this.your_turn = v1.getConfigString("your-turn-msg", "§aYour Turn");
        this.opp_turn = v1.getConfigString("opponent-turn-msg", "§7Opponent's Turn");
        this.cue = GameUtils.createItem(v1.getConfig().getBoolean("pool.cue-blaze-rod") ? Material.BLAZE_ROD : Material.STICK, 1, (byte)0, v1.getConfigString("pool.cue-item-title", "§3Cue §7(Click the white cue ball)"), new String[0]);
        this.book = GameUtils.createItem(Material.BOOK, 1, (byte)0, v1.getConfigString("pool.open-gui-item-title", "§bOpen Pool Menu"), new String[0]);
        this.same_color = v1.getConfig().getBoolean("pool.same-color");
        this.solids_sc = GameUtils.getConfigColor(v1.getConfig(), "pool.solids-color");
        this.stripes_sc = GameUtils.getConfigColor(v1.getConfig(), "pool.stripes-color");
        this.can_practice = v1.getConfig().getBoolean("pool.practice-mode-enabled");
        this.prac_mode_str = v1.getConfigString(player, "pool.practice-mode-msg", "§cYou are in Practice Mode!");
        this.exit_prac_mode = v1.getConfigString(player, "pool.exit-practice-mode", "§cExit Practice Mode");
        this.legacy_mode = (LobbyGames.SERVER_VERSION == 8 && v1.getConfig().getBoolean("legacy-mode"));
        this.pocket_radius = Math.min(Math.max(v1.getConfig().getDouble("pool.pocket-radius", 0.35), 0.1), 0.6);
        this.pocket_use_euclidean = v1.getConfig().getBoolean("pool.circular-pockets", false);
        if (this.same_color) {
            if (this.stripes_sc > 14) {
                this.stripes_sc = 14;
            }
            if (this.stripes_sc < 1) {
                this.stripes_sc = 1;
            }
            if (this.solids_sc > 14) {
                this.solids_sc = 14;
            }
            if (this.solids_sc < 1) {
                this.solids_sc = 1;
            }
            if (this.solids_sc == this.stripes_sc) {
                Bukkit.getLogger().warning("Solids and Stripes cannot be the same color!");
                this.same_color = false;
            }
        }
        this.xplane = (v2.getWidth() > v2.getHeight());
        this.mw4 = (Math.max(v2.getHeight(), v2.getWidth()) == 4);
        final String[] split = v1.getConfigString("pool.translatable-words", "Ball, Cue Ball, Pocketed, Wool, Terracotta").split(",");
        for (int i = 0; i < this.words.length; ++i) {
            if (split.length > i && split[i].length() > 0) {
                this.words[i] = split[i].trim();
            }
        }
        final String[] split2 = v1.getConfigString("pool.translatable-power-words", "§aLow Power, §eMedium Power, §6High Power, §cHighest Power").split(",");
        for (int j = 0; j < this.power_words.length; ++j) {
            if (split2.length > j && split2[j].length() > 0) {
                this.power_words[j] = split2[j].trim();
            }
        }
        this.giveItems(player);
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)v1);
        this.reset();
        this.startParticleLoop();
        if (this.can_practice) {
            new GameTask() {
                public void run() {
                    if (Pool.this.p2 != null || !Pool.this.canStart()) {
                        this.cancel();
                        return;
                    }
                    if (Pool.this.practice) {
                        v1.sendActionbar(Pool.this.p1, Pool.this.prac_mode_str, true);
                    }
                }
            }.runTaskTimer((Plugin)v1, 1L, 20L);
        }
    }
    
    public boolean isSameColor() {
        return this.same_color;
    }
    
    public int getSolidsSC() {
        return this.solids_sc;
    }
    
    public int getStripesSC() {
        return this.stripes_sc;
    }
    
    public String[] getWords() {
        return this.words;
    }
    
    public boolean isLegacyMode() {
        return this.legacy_mode;
    }
    
    public void giveItems(final Player returnLocation) {
        if (this.getArena().getSpawn1() != null && (!returnLocation.getWorld().getName().equals(this.getArena().getSpawn1().getWorld().getName()) || returnLocation.getLocation().distance(this.getArena().getSpawn1()) >= 10.0)) {
            this.plugin.setReturnLocation(returnLocation);
            returnLocation.teleportAsync(this.getArena().getSpawn1());
        }
        this.preparePlayer(returnLocation, GameMode.ADVENTURE);
        returnLocation.getInventory().setItem(0, this.cue);
        returnLocation.getInventory().setItem(7, this.book);
        returnLocation.getInventory().setItem(8, this.plugin.getQuitItem());
        final byte b = this.p1.getUniqueId().equals(returnLocation.getUniqueId()) ? this.p1_power : this.p2_power;
        returnLocation.getInventory().setItem(1, GameUtils.createGlass(1, this.power_bytes[b], this.power_words[b], new String[0]));
    }
    
    public void startPractice() {
        if (!this.canStart() || !this.can_practice) {
            return;
        }
        this.practice = true;
        this.plugin.sendActionbar(this.p1, this.prac_mode_str, false);
        this.in_play = false;
        this.p1.getInventory().setItem(6, GameUtils.createItem(Material.FEATHER, 1, (byte)0, this.exit_prac_mode, new String[0]));
        this.start();
    }
    
    public void quitPractice() {
        this.practice = false;
        this.p1.getInventory().setItem(2, new ItemStack(Material.AIR));
        this.p1.getInventory().setItem(6, new ItemStack(Material.AIR));
        this.reset();
    }
    
    public boolean tickBall(final BilliardBall billiardBall, final Location location, final Location location2) {
        boolean tick = false;
        if (billiardBall != null && (billiardBall.getVelocity().getX() != 0.0 || billiardBall.getVelocity().getZ() != 0.0)) {
            this.ball_moved = true;
            tick = billiardBall.tick();
            final Location location3 = billiardBall.getLocation();
            if (!billiardBall.isSunk()) {
                if (location.getX() < location2.getX()) {
                    if (((billiardBall.getVelocity().getX() < 0.0 && location3.getX() - BilliardBall.BALL_RADIUS < location.getX()) || (billiardBall.getVelocity().getX() > 0.0 && location3.getX() - 1.0 + BilliardBall.BALL_RADIUS > location2.getX())) && !this.checkInHole(billiardBall)) {
                        billiardBall.setVelocity(billiardBall.getVelocity().setX(BilliardBall.BOUNCE * billiardBall.getVelocity().getX()));
                        if (billiardBall.getNumber() > 0) {
                            ++this.rail_hits;
                        }
                    }
                }
                else if (((billiardBall.getVelocity().getX() > 0.0 && location3.getX() + BilliardBall.BALL_RADIUS > location.getX() + 1.0) || (billiardBall.getVelocity().getX() < 0.0 && location3.getX() - BilliardBall.BALL_RADIUS < location2.getX())) && !this.checkInHole(billiardBall)) {
                    billiardBall.setVelocity(billiardBall.getVelocity().setX(BilliardBall.BOUNCE * billiardBall.getVelocity().getX()));
                    if (billiardBall.getNumber() > 0) {
                        ++this.rail_hits;
                    }
                }
                if (location.getZ() < location2.getZ()) {
                    if (((billiardBall.getVelocity().getZ() < 0.0 && location3.getZ() - BilliardBall.BALL_RADIUS < location.getZ()) || (billiardBall.getVelocity().getZ() > 0.0 && location3.getZ() + BilliardBall.BALL_RADIUS > location2.getZ() + 1.0)) && !this.checkInHole(billiardBall)) {
                        billiardBall.setVelocity(billiardBall.getVelocity().setZ(BilliardBall.BOUNCE * billiardBall.getVelocity().getZ()));
                        if (billiardBall.getNumber() > 0) {
                            ++this.rail_hits;
                        }
                    }
                }
                else if (((billiardBall.getVelocity().getZ() > 0.0 && location3.getZ() + BilliardBall.BALL_RADIUS > location.getZ() + 1.0) || (billiardBall.getVelocity().getZ() < 0.0 && location3.getZ() - BilliardBall.BALL_RADIUS < location2.getZ())) && !this.checkInHole(billiardBall)) {
                    billiardBall.setVelocity(billiardBall.getVelocity().setZ(BilliardBall.BOUNCE * billiardBall.getVelocity().getZ()));
                    if (billiardBall.getNumber() > 0) {
                        ++this.rail_hits;
                    }
                }
            }
            for (int i = 0; i < 16; ++i) {
                final BilliardBall billiardBall2 = this.balls[i];
                if (billiardBall2 != null && i != billiardBall.getNumber() && billiardBall.collide(billiardBall2)) {
                    this.collision = true;
                    tick = false;
                    final Sound sound = (LobbyGames.SERVER_VERSION >= 12) ? Sound.BLOCK_STONE_HIT : Sound.valueOf("DIG_STONE");
                    this.p1.playSound(billiardBall.getLocation(), sound, 0.15f, 100.0f);
                    if (this.p2 != null) {
                        this.p2.playSound(billiardBall.getLocation(), sound, 0.15f, 100.0f);
                    }
                }
            }
        }
        return tick;
    }
    
    public void start() {
        if (this.p2 == null && !this.practice) {
            return;
        }
        if (this.practice && !this.can_practice) {
            this.practice = false;
            return;
        }
        if (this.practice) {
            if (this.p2 != null) {
                this.quitPractice();
                this.setActive(true);
            }
        }
        else {
            this.setActive(true);
            this.removeQuitConfirmation(this.p1);
            this.removeQuitConfirmation(this.p2);
        }
        
        // 确保球已经被创建 - 如果 balls 数组为 null 或第一个球不存在，调用 reset()
        if (this.balls == null || this.balls[0] == null) {
            this.plugin.getLogger().info("[Pool Debug] Balls not initialized in start(), calling reset()");
            this.reset();
        }
        
        if (!this.practice) {
            final String configString = this.getPlugin().getConfigString("pool.start-msg", "§3§m----------------------------------------\n§b§lPool: §bHit the (white) cue ball to pocket other balls! Don't hit the (black) 8-ball until all of your designated balls have been pocketed.\n§3§m----------------------------------------");
            if (configString.length() > 0) {
                this.p1.sendMessage(this.plugin.sp(this.p1, configString));
                this.p2.sendMessage(this.plugin.sp(this.p2, configString));
            }
            if (LobbyGames.SERVER_VERSION < 12) {
                ((this.p1_turn || this.p2 == null) ? this.p1 : this.p2).sendMessage(this.your_turn);
                if (this.p2 != null) {
                    (this.p1_turn ? this.p2 : this.p1).sendMessage(this.opp_turn);
                }
            }
            new GameTask() {
                public void run() {
                    if (!Pool.this.canStart()) {
                        this.cancel();
                        return;
                    }
                    if (Pool.this.p1 != null && Pool.this.p1.isOnline()) {
                        Pool.this.getPlugin().sendActionbar(Pool.this.p1, Pool.this.p1_turn ? Pool.this.your_turn : Pool.this.opp_turn, true);
                    }
                    if (Pool.this.p2 != null && Pool.this.p2.isOnline()) {
                        Pool.this.getPlugin().sendActionbar(Pool.this.p2, Pool.this.p1_turn ? Pool.this.opp_turn : Pool.this.your_turn, true);
                    }
                }
            }.runTaskTimer((Plugin)this.plugin, 1L, 20L);
        }
        this.in_play = false;
        if (!this.started_instance) {
            this.started_instance = true;
            // 使用区域任务而不是全局任务，因为需要创建和操作实体（盔甲架、球）
            final Location arenaCenter = this.getArena().getCenterPixel();
            new GameTask() {
                public void run() {
                    if (!Pool.this.canStart()) {
                        this.cancel();
                        return;
                    }
                    if (!Pool.this.in_play) {
                        return;
                    }
                    Pool.this.ball_moved = false;
                    final Location location1 = Pool.this.getArena().getLocation1();
                    final Location location2 = Pool.this.getArena().getLocation2();
                    final ArrayList<Integer> list = new ArrayList<Integer>();
                    final ArrayList<Integer> list2 = new ArrayList<Integer>();
                    for (int i = 0; i < 16; ++i) {
                        if (Pool.this.tickBall(Pool.this.balls[i], location1, location2)) {
                            list.add(i);
                        }
                    }
                    while (list.size() > 0) {
                        list2.clear();
                        for (final Integer intValue : list) {
                            if (Pool.this.tickBall(Pool.this.balls[intValue], location1, location2)) {
                                list2.add(intValue);
                            }
                        }
                        list.clear();
                        final Iterator<Integer> iterator2 = list2.iterator();
                        while (iterator2.hasNext()) {
                            list.add(iterator2.next());
                        }
                    }
                    for (int j = 0; j < 16; ++j) {
                        final BilliardBall billiardBall = Pool.this.balls[j];
                        if (billiardBall != null && !billiardBall.isSunk()) {
                            billiardBall.shiftCollisionList();
                        }
                    }
                    if (!Pool.this.ball_moved) {
                        Pool.this.endTurn();
                    }
                }
            }.runLocationTaskTimer((Plugin)this.plugin, arenaCenter, 1L, 1L);
        }
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinLobbyGameEvent playerJoinLobbyGameEvent) {
        if (!this.canStart() || playerJoinLobbyGameEvent.getGame().getGameType() != GameType.POOL || playerJoinLobbyGameEvent.getGame().getArena().getID() != this.getArena().getID() || this.isActive()) {
            return;
        }
        if (this.getPlayers().size() == 2) {
            this.p2 = playerJoinLobbyGameEvent.getPlayer();
            this.giveItems(playerJoinLobbyGameEvent.getPlayer());
            this.start();
        }
    }
    
    public void endTurn() {
        this.in_play = false;
        boolean b = true;
        if ((!this.collision || this.sunk_balls.contains(0)) && (!this.first_draw || this.rail_hits >= 3)) {
            this.cueball_inhand = true;
            if (this.balls[0] != null) {
                this.balls[0].sink();
            }
            this.balls[0] = null;
        }
        if (this.first_draw) {
            b = false;
            if (this.sunk_balls.contains(8)) {
                final String configString = this.plugin.getConfigString("pool.pocketed-8ball", "§cThe 8-ball was pocketed!");
                this.p1.sendMessage(this.plugin.sp(this.p1, configString));
                if (this.p2 != null) {
                    this.p2.sendMessage(this.plugin.sp(this.p2, configString));
                }
                this.reset();
                this.first_draw = true;
                this.collision = false;
            }
            else if (this.rail_hits < 4) {
                this.reset();
                this.first_draw = true;
                this.collision = false;
                this.cueball_inhand = false;
                final String configString2 = this.plugin.getConfigString("pool.redo-break", "§bThis break was not strong enough, try again!");
                if (configString2 != null && configString2.length() > 0) {
                    final Player player = (this.p2 == null || this.p1_turn) ? this.p1 : this.p2;
                    player.sendMessage(this.plugin.sp(player, configString2));
                }
            }
            else if (this.sunk_balls.size() == 0) {
                b = true;
            }
        }
        if (this.sunk_balls.size() > 0) {
            String s = "";
            String s2 = "";
            int n = 0;
            int n2 = 0;
            int n3 = 0;
            for (int i = 0; i < this.sunk_balls.size(); ++i) {
                final int intValue = this.sunk_balls.get(i);
                if (intValue > 0) {
                    ++n3;
                    if (intValue < 8) {
                        ++n;
                        s = s + "#" + intValue + ", ";
                    }
                    else if (intValue > 8) {
                        ++n2;
                        s2 = s2 + "#" + intValue + ", ";
                    }
                }
            }
            final boolean contains = this.sunk_balls.contains(8);
            if (!contains && (n > 0 || n2 > 0)) {
                String replaceAll;
                if (s.length() > 0) {
                    replaceAll = (s + "abc").replaceAll("\\Q, abc\\E", "");
                }
                else {
                    replaceAll = "-";
                }
                String replaceAll2;
                if (s2.length() > 0) {
                    replaceAll2 = (s2 + "abc").replaceAll("\\Q, abc\\E", "");
                }
                else {
                    replaceAll2 = "-";
                }
                final String configString3 = this.plugin.getConfigString("pool.pocketed-balls", "§3%name% pocketed %count% ball(s):\n§b  Wool: §f%wool_pocketed%\n§b  Terracotta: §f%terracotta_pocketed%");
                if (configString3.length() > 0 && !this.practice) {
                    final String replaceAll3 = configString3.replaceAll("\\Q%wool_pocketed%\\E|\\Q%solids_pocketed%\\E", replaceAll).replaceAll("\\Q%terracotta_pocketed%\\E|\\Q%stripes_pocketed%\\E", replaceAll2).replaceAll("\\Q%combined_list%\\E", replaceAll + ((replaceAll.length() > 0) ? ", " : "") + replaceAll2).replaceAll("\\Q%name%\\E", (this.p1_turn || this.p2 == null) ? this.p1.getName() : this.p2.getName()).replaceAll("\\Q%player%\\E", (this.p1_turn || this.p2 == null) ? this.p1.getName() : this.p2.getName()).replaceAll("\\Q%count%\\E", "" + n3).replaceAll("\\Q(s)\\E", (n3 == 1) ? "" : "s").replaceAll("\\Q%wool_count%\\E", "" + n).replaceAll("\\Q%terracotta_count%\\E", "" + n2);
                    this.p1.sendMessage(this.plugin.sp(this.p1, replaceAll3));
                    if (this.p2 != null) {
                        this.p2.sendMessage(this.plugin.sp(this.p2, replaceAll3));
                    }
                }
            }
            if (!this.first_draw) {
                if (contains) {
                    this.cueball_inhand = false;
                    int n4 = 7;
                    int n5 = 7;
                    for (int j = 1; j < 16; ++j) {
                        if (this.balls[j] == null || this.balls[j].isSunk()) {
                            if (j < 8) {
                                --n4;
                            }
                            else if (j > 8) {
                                --n5;
                            }
                        }
                    }
                    final String configString4 = this.plugin.getConfigString((this.p1_turn || this.p2 == null) ? this.p1 : this.p2, (n4 == 0 && n5 == 0) ? "pool.pocketed-all-balls" : "pool.pocketed-8ball", "§cThe 8-ball was pocketed!");
                    this.p1.sendMessage(configString4);
                    if (this.p2 != null) {
                        this.p2.sendMessage(configString4);
                    }
                    final boolean contains2 = this.sunk_balls.contains(0);
                    if (this.practice) {
                        this.win(true, false);
                        return;
                    }
                    if (this.sides_assigned) {
                        if (n4 > 0 && n5 > 0) {
                            this.win(!this.p1_turn, true);
                        }
                        else if (this.p1_turn) {
                            if ((this.p1_solid && n4 == 0) || (!this.p1_solid && n5 == 0)) {
                                this.win(!contains2, true);
                            }
                            else {
                                this.win(false, true);
                            }
                        }
                        else if ((this.p1_solid && n5 == 0) || (!this.p1_solid && n4 == 0)) {
                            this.win(contains2, true);
                        }
                        else {
                            this.win(true, true);
                        }
                    }
                    else if (n4 == 0 && n5 == 0) {
                        this.win(this.p1_turn, true);
                    }
                    else {
                        this.win(!this.p1_turn, true);
                    }
                    return;
                }
                else if (!this.sides_assigned) {
                    b = false;
                    if ((n == 0 || n2 == 0) && !this.sunk_balls.contains(0) && !this.practice) {
                        this.p1_solid = ((this.p1_turn && n2 == 0) || (!this.p1_turn && n == 0));
                        this.sides_assigned = true;
                        final String configString5 = this.plugin.getConfigString("pool.side-designation", "\n§bYou need to pocket the §b§l%side%§b balls!");
                        this.p1.sendMessage(this.plugin.sp(this.p1, configString5.replaceAll("\\Q%side%\\E", this.p1_solid ? this.words[3] : this.words[4])));
                        this.p1.getInventory().setItem(1, this.p1_solid ? GameUtils.createWool(1, this.power_bytes[this.p1_power], this.power_words[this.p1_power], new String[0]) : GameUtils.createClay(1, this.power_bytes[this.p1_power], this.power_words[this.p1_power], new String[0]));
                        if (this.p2 != null) {
                            this.p2.sendMessage(this.plugin.sp(this.p2, configString5.replaceAll("\\Q%side%\\E", this.p1_solid ? this.words[4] : this.words[3])));
                            this.p2.getInventory().setItem(1, this.p1_solid ? GameUtils.createClay(1, this.power_bytes[this.p2_power], this.power_words[this.p2_power], new String[0]) : GameUtils.createWool(1, this.power_bytes[this.p2_power], this.power_words[this.p2_power], new String[0]));
                        }
                    }
                }
                else if (this.p1_turn) {
                    if ((this.p1_solid && n > 0) || (!this.p1_solid && n2 > 0)) {
                        b = false;
                    }
                }
                else if ((this.p1_solid && n2 > 0) || (!this.p1_solid && n > 0)) {
                    b = false;
                }
            }
        }
        if (!this.practice && (b || this.cueball_inhand)) {
            this.p1_turn = !this.p1_turn;
            if (LobbyGames.SERVER_VERSION < 12) {
                final Player player2 = (this.p1_turn || this.p2 == null) ? this.p1 : this.p2;
                player2.sendMessage(this.plugin.sp(player2, this.your_turn));
            }
            final Sound sound = (LobbyGames.SERVER_VERSION >= 12) ? Sound.BLOCK_COMPARATOR_CLICK : Sound.valueOf("CLICK");
            this.p1.playSound(this.p1.getLocation(), sound, 1.0f, 100.0f);
            if (this.p2 != null) {
                this.p2.playSound(this.p2.getLocation(), sound, 1.0f, 100.0f);
            }
        }
        if (this.cueball_inhand) {
            ((this.p1_turn || this.p2 == null) ? this.p1 : this.p2).getInventory().setItem(2, GameUtils.createItem(Material.QUARTZ_BLOCK, 1, (byte)0, this.plugin.getConfigString("pool.cueball-title", "§f§lCue Ball §7(Place anywhere on table)"), new String[0]));
            final String replaceAll4 = this.plugin.getConfigString("pool.cueball-inhand", "§b%player% has the cue ball in hand!").replaceAll("\\Q%player%\\E", (this.p1_turn || this.p2 == null) ? this.p1.getName() : this.p2.getName());
            if (replaceAll4.length() > 0 && !this.practice) {
                this.p1.sendMessage(this.plugin.sp(this.p1, replaceAll4));
                if (this.p2 != null) {
                    this.p2.sendMessage(this.plugin.sp(this.p2, replaceAll4));
                }
            }
        }
        this.sunk_balls.clear();
        if (this.collision) {
            this.first_draw = false;
        }
        this.collision = false;
        this.rail_hits = 0;
    }
    
    public void openGUI(final Player player) {
        if (!this.canStart()) {
            return;
        }
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 27, this.plugin.getConfigString("pool.gui-title", "Pool Ball Status"));
        final int[] array = { 4, 11, 14, 10, 1, 5, 12, 15 };
        final int sides_assigned = this.sides_assigned ? 1 : 0;
        if (this.same_color) {
            int n = 0;
            int n2 = 0;
            for (int i = 1; i < this.balls.length; ++i) {
                if (this.balls[i] == null || this.balls[i].isSunk()) {
                    if (i < 8) {
                        ++n;
                    }
                    else if (i > 8) {
                        ++n2;
                    }
                }
            }
            for (int j = 0; j < 7; ++j) {
                inventory.setItem(j + 1 + sides_assigned, (j < n) ? GameUtils.createWool(1, 8, "§f§l" + (j + 1) + " " + this.words[0] + " (" + this.words[3], "§7" + this.words[2]) : GameUtils.createWool(1, this.solids_sc, "§f§l" + (j + 1) + " " + this.words[0] + " (" + this.words[3], new String[0]));
                inventory.setItem(j + 10 + sides_assigned, (j < n2) ? GameUtils.createWool(1, 8, "§f§l" + (j + 9) + " " + this.words[0] + " (" + this.words[4], "§7" + this.words[2]) : GameUtils.createWool(1, this.stripes_sc, "§f§l" + (j + 9) + " " + this.words[0] + " (" + this.words[4], new String[0]));
            }
        }
        else {
            for (int k = 0; k < 7; ++k) {
                inventory.setItem(k + 1 + sides_assigned, (this.balls[k + 1] == null || this.balls[k + 1].isSunk()) ? GameUtils.createWool(1, 8, "§f§l" + (k + 1) + " " + this.words[0] + " (" + this.words[3], "§7" + this.words[2]) : GameUtils.createWool(1, array[k], "§f§l" + (k + 1) + " " + this.words[0] + " (" + this.words[3], new String[0]));
                inventory.setItem(k + 10 + sides_assigned, (this.balls[k + 9] == null || this.balls[k + 9].isSunk()) ? GameUtils.createWool(1, 8, "§f§l" + (k + 9) + " " + this.words[0] + " (" + this.words[4], "§7" + this.words[2]) : GameUtils.createClay(1, array[k], "§f§l" + (k + 9) + " " + this.words[0] + " (" + this.words[4], new String[0]));
            }
        }
        inventory.setItem(21 + sides_assigned, GameUtils.createItem(Material.QUARTZ_BLOCK, 1, (byte)0, "§f§l" + this.words[1], this.plugin.getConfigString("pool.cueball-description", "§7Hit this with the cue").split("(\\n,\\Q[newline]\\E)")));
        inventory.setItem(23 + sides_assigned, GameUtils.createWool(1, 15, "§f§l8 " + this.words[0], this.plugin.getConfigString("pool.8ball-description", "§7Pocket this §conly§7 after pocketing\n§7all of your other designated balls!").split("\n")));
        if (this.sides_assigned) {
            final ItemStack itemStack = (LobbyGames.SERVER_VERSION > 12) ? new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1) : new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short)7);
            final ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§b ");
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(1, itemStack);
            inventory.setItem(10, itemStack);
            inventory.setItem(19, itemStack);
            final ItemStack itemStack2 = (LobbyGames.SERVER_VERSION > 12) ? new ItemStack(Material.PLAYER_HEAD, 1) : new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short)3);
            final ItemStack itemStack3 = (LobbyGames.SERVER_VERSION > 12) ? new ItemStack(Material.PLAYER_HEAD, 1) : new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short)3);
            final SkullMeta itemMeta2 = (SkullMeta)itemStack2.getItemMeta();
            final SkullMeta itemMeta3 = (SkullMeta)itemStack2.getItemMeta();
            itemMeta2.setOwner((this.p1_solid || this.p2 == null) ? this.p1.getName() : this.p2.getName());
            itemMeta2.setDisplayName("§f§l" + ((this.p1_solid || this.p2 == null) ? this.p1.getName() : this.p2.getName()));
            itemMeta3.setOwner((!this.p1_solid || this.p2 == null) ? this.p1.getName() : this.p2.getName());
            itemMeta3.setDisplayName("§f§l" + ((!this.p1_solid || this.p2 == null) ? this.p1.getName() : this.p2.getName()));
            itemStack2.setItemMeta((ItemMeta)itemMeta2);
            itemStack3.setItemMeta((ItemMeta)itemMeta3);
            inventory.setItem(0, itemStack2);
            inventory.setItem(9, itemStack3);
        }
        player.openInventory(inventory);
    }
    
    public boolean checkInHole(final BilliardBall billiardBall) {
        final Location location = billiardBall.getLocation();
        Location[] holes;
        for (int length = (holes = this.holes).length, i = 0; i < length; ++i) {
            final Location location2 = holes[i];
            if (Math.abs(location.getX() - location2.getX()) <= this.pocket_radius && Math.abs(location.getZ() - location2.getZ()) <= this.pocket_radius) {
                if (!this.pocket_use_euclidean || Math.sqrt(Math.pow(location.getX() - location2.getX(), 2.0) + Math.pow(location.getZ() - location2.getZ(), 2.0)) <= this.pocket_radius) {
                    billiardBall.sink();
                    this.balls[billiardBall.getNumber()] = null;
                    this.sunk_balls.add(billiardBall.getNumber());
                    final Sound sound = (LobbyGames.SERVER_VERSION >= 12) ? Sound.BLOCK_STONE_BREAK : Sound.valueOf("DIG_STONE");
                    this.p1.playSound(billiardBall.getLocation(), sound, 0.5f, 10.0f);
                    if (this.p2 != null) {
                        this.p2.playSound(billiardBall.getLocation(), sound, 0.5f, 10.0f);
                    }
                    final String configString = this.plugin.getConfigString("pool.gui-title", "Pool Ball Status");
                    final Iterator<UUID> iterator = this.getPlayers().iterator();
                    while (iterator.hasNext()) {
                        final Player player = Bukkit.getPlayer((UUID)iterator.next());
                        if (player == null) {
                            continue;
                        }
                        final InventoryView openInventory = player.getOpenInventory();
                        if (openInventory == null || !openInventory.getTitle().equals(configString)) {
                            continue;
                        }
                        this.openGUI(player);
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public Location getPixel(final int n, final int n2) {
        if (this.xplane) {
            return super.getPixel(n, n2).add((double)((this.mw4 && this.getArena().getCoordinateRotation() <= 1) ? -1 : 0), 0.0, 0.0);
        }
        return super.getPixel(n2, n).add(0.0, 0.0, (double)((this.mw4 && this.getArena().getCoordinateRotation() >= 2) ? 1 : 0));
    }
    
    @Override
    public int getScoreInt(final Player player) {
        if (!this.isActive()) {
            return 0;
        }
        int n = 7;
        int n2 = 7;
        for (int i = 1; i < this.balls.length; ++i) {
            if (this.balls[i] == null || this.balls[i].isSunk()) {
                if (i < 8) {
                    ++n;
                }
                else if (i > 8) {
                    ++n2;
                }
            }
        }
        if (player.getUniqueId().equals((this.p1_solid || this.p2 == null) ? this.p1.getUniqueId() : this.p2.getUniqueId())) {
            return n;
        }
        return n2;
    }
    
    @Override
    public String getScore(final Player player) {
        return "" + this.getScoreInt(player);
    }
    
    private boolean checkTimeout(final UUID uuid) {
        if (System.currentTimeMillis() - this.timeout.getOrDefault(uuid, 0L) <= 100L) {
            return true;
        }
        this.timeout.put(uuid, System.currentTimeMillis());
        return false;
    }
    
    public void interact(final Player player, final Cancellable cancellable) {
        if (!this.containsPlayer(player)) {
            return;
        }
        cancellable.setCancelled(true);
        if (this.checkTimeout(player.getUniqueId())) {
            return;
        }
        final ItemStack handItem = GameUtils.getHandItem(player);
        if (handItem == null && !this.first_draw) {
            return;
        }
        if ((this.first_draw || this.cueball_inhand) && (handItem == null || handItem.getType() == Material.AIR || handItem.getType() == Material.QUARTZ_BLOCK)) {
            if (this.isActive() && !player.getUniqueId().equals(this.p1_turn ? this.p1.getUniqueId() : this.p2.getUniqueId())) {
                return;
            }
            if (this.in_play) {
                return;
            }
            final Location add = player.getLocation().add(0.0, player.isSneaking() ? 1.25 : 1.5, 0.0);
            final Vector multiply = player.getLocation().getDirection().normalize().multiply(0.25);
            int n = 0;
            while (add.getY() > this.getArena().getLocation1().getBlockY() + 1 && n < 20) {
                ++n;
                add.add(multiply);
            }
            if (this.getArena().isInBoundsXZ(add)) {
                if (this.first_draw) {
                    final CoordinatePair coords = this.getCoords(add);
                    final int coordinateRotation = this.getArena().getCoordinateRotation();
                    int n2 = ((coordinateRotation >= 2) ? 1 : -1) * ((this.mw4 && ((coordinateRotation <= 1 && this.xplane) || (coordinateRotation >= 2 && !this.xplane))) ? 2 : 1);
                    if (this.mw4 && this.xplane) {
                        ++n2;
                    }
                    final int n3 = this.xplane ? coords.getX() : coords.getY();
                    if (coordinateRotation <= 1) {
                        if (n3 > n2) {
                            return;
                        }
                    }
                    else if (n3 < n2) {
                        return;
                    }
                }
                add.setY(this.getArena().getLocation1().getY());
                if (this.balls[0] != null) {
                    this.balls[0].teleport(add);
                }
                else {
                    this.balls[0] = new BilliardBall(0, add, this);
                }
                player.getInventory().setItem(2, new ItemStack(Material.AIR));
            }
        }
        else if (handItem.getType() == this.plugin.getQuitItem().getType()) {
            this.quitConfirmation(player);
        }
        else if (handItem.getType() == Material.FEATHER && this.practice) {
            this.quitPractice();
        }
        else if (handItem.getType() == Material.BOOK) {
            this.openGUI(player);
        }
        else if ((handItem.getType() == Material.STICK || handItem.getType() == Material.BLAZE_ROD) && this.balls[0] != null && !this.in_play) {
            if (this.p2 == null) {
                if (!this.practice) {
                    if (!this.can_practice) {
                        return;
                    }
                    this.startPractice();
                }
            }
            else if (!player.getUniqueId().equals(this.p1_turn ? this.p1.getUniqueId() : this.p2.getUniqueId())) {
                if (this.isActive()) {
                    final String configString = this.plugin.getConfigString("not-your-turn-msg");
                    if (configString.length() > 0) {
                        this.getPlugin().sendActionbar(player, configString, false);
                    }
                }
                return;
            }
            if (this.balls[0].getLocation().toVector().subtract(player.getLocation().add(0.0, player.isSneaking() ? 0.25 : 0.5, 0.0).toVector()).angle(player.getLocation().getDirection().normalize()) <= 0.15 && player.getLocation().distance(this.getArena().getCenterPixel()) <= 5.0) {
                this.in_play = true;
                this.sunk_balls.clear();
                this.collision = false;
                this.rail_hits = 0;
                this.cueball_inhand = false;
                double n4 = BilliardBall.VMAX - 0.02;
                if (!this.first_draw) {
                    switch ((this.p1_turn || this.practice) ? this.p1_power : this.p2_power) {
                        case 0: {
                            n4 = 0.115;
                            break;
                        }
                        case 1: {
                            n4 = 0.22;
                            break;
                        }
                        case 2: {
                            n4 = 0.35;
                            break;
                        }
                    }
                }
                this.balls[0].setVelocity(player.getLocation().getDirection().setY(0).normalize().multiply(n4));
                final Sound sound = (LobbyGames.SERVER_VERSION >= 12) ? Sound.BLOCK_STONE_HIT : Sound.valueOf("DIG_STONE");
                this.p1.playSound(this.balls[0].getLocation(), sound, 0.15f, 100.0f);
                if (this.p2 != null) {
                    this.p2.playSound(this.balls[0].getLocation(), sound, 0.15f, 100.0f);
                }
            }
        }
        else if (handItem.getType().toString().endsWith("STAINED_GLASS") || handItem.getType().toString().endsWith("WOOL") || handItem.getType().toString().endsWith("TERRACOTTA") || handItem.getType().toString().endsWith("STAINED_CLAY")) {
            if (this.isInQuitConfirmation(player.getUniqueId())) {
                return;
            }
            final boolean equals = this.p1.getUniqueId().equals(player.getUniqueId());
            int n5;
            if (equals) {
                n5 = (this.p1_power + 1) % 4;
                this.p1_power = (byte)n5;
            }
            else {
                n5 = (this.p2_power + 1) % 4;
                this.p2_power = (byte)n5;
            }
            if (!this.sides_assigned) {
                player.getInventory().setItem(1, GameUtils.createGlass(1, this.power_bytes[n5], this.power_words[n5], new String[0]));
            }
            else if ((equals && this.p1_solid) || (!equals && !this.p1_solid)) {
                player.getInventory().setItem(1, GameUtils.createWool(1, this.power_bytes[n5], this.power_words[n5], new String[0]));
            }
            else {
                player.getInventory().setItem(1, GameUtils.createClay(1, this.power_bytes[n5], this.power_words[n5], new String[0]));
            }
            if (LobbyGames.SERVER_VERSION > 12) {
                player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.25f, 1.0f);
            }
        }
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent playerInteractEvent) {
        this.interact(playerInteractEvent.getPlayer(), (Cancellable)playerInteractEvent);
    }
    
    @EventHandler
    public void onInteract2(final PlayerInteractEntityEvent playerInteractEntityEvent) {
        this.interact(playerInteractEntityEvent.getPlayer(), (Cancellable)playerInteractEntityEvent);
    }
    
    @EventHandler
    public void onInteract3(final PlayerInteractAtEntityEvent playerInteractAtEntityEvent) {
        this.interact(playerInteractAtEntityEvent.getPlayer(), (Cancellable)playerInteractAtEntityEvent);
    }
    
    @EventHandler
    public void onInteract4(final EntityDamageByEntityEvent entityDamageByEntityEvent) {
        if (entityDamageByEntityEvent.getDamager() instanceof Player && entityDamageByEntityEvent.getEntity() instanceof ArmorStand) {
            this.interact((Player)entityDamageByEntityEvent.getDamager(), (Cancellable)entityDamageByEntityEvent);
        }
    }
    
    @EventHandler
    public void onInteract5(final PlayerAnimationEvent playerAnimationEvent) {
        if (playerAnimationEvent.getAnimationType() == PlayerAnimationType.ARM_SWING || LobbyGames.SERVER_VERSION >= 13) {
            this.interact(playerAnimationEvent.getPlayer(), (Cancellable)playerAnimationEvent);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorStandManipulate(final PlayerArmorStandManipulateEvent playerArmorStandManipulateEvent) {
        if (!this.canStart()) {
            return;
        }
        BilliardBall[] balls;
        for (int length = (balls = this.balls).length, i = 0; i < length; ++i) {
            final BilliardBall billiardBall = balls[i];
            if (billiardBall != null && billiardBall.getUniqueId().equals(playerArmorStandManipulateEvent.getRightClicked().getUniqueId())) {
                playerArmorStandManipulateEvent.setCancelled(true);
                break;
            }
        }
        ArmorStand[] holes_as;
        for (int length2 = (holes_as = this.holes_as).length, j = 0; j < length2; ++j) {
            final ArmorStand armorStand = holes_as[j];
            if (armorStand != null && armorStand.getUniqueId().equals(playerArmorStandManipulateEvent.getRightClicked().getUniqueId())) {
                playerArmorStandManipulateEvent.setCancelled(true);
                break;
            }
        }
        this.interact(playerArmorStandManipulateEvent.getPlayer(), (Cancellable)playerArmorStandManipulateEvent);
    }
    
    @EventHandler
    public void onMove(final PlayerMoveEvent playerMoveEvent) {
        if (!this.canStart()) {
            return;
        }
        if (this.p2 == null && this.plugin.poolProximityJoining()) {
            final double distSquareXZ = GameUtils.distSquareXZ(this.getArena().getCenterPixel(), playerMoveEvent.getPlayer().getLocation());
            if (playerMoveEvent.getPlayer().getUniqueId().equals(this.p1.getUniqueId())) {
                if (distSquareXZ >= 7.0) {
                    this.end(false);
                }
            }
            else if (distSquareXZ <= 5.2) {
                this.appendPlayer(playerMoveEvent.getPlayer());
            }
        }
    }
    
    public void win(final boolean b, final boolean b2) {
        if (this.practice) {
            this.quitPractice();
            return;
        }
        if (this.p2 != null) {
            final Player player = b ? this.p1 : this.p2;
            final String replaceAll = this.plugin.sp(b ? this.p1 : this.p2, this.plugin.getConfigString("pool.win-msg", "&6&m----------------------------------------[newline]&e&l" + player.getName() + "&e won the 8-Ball game![newline]&6&m----------------------------------------").replaceAll("\\Q%winner%\\E", player.getName()).replaceAll("\\Q%player%\\E", player.getName())).replaceAll("\\Q%loser%\\E", (b ? this.p2 : this.p1).getName());
            this.p1.sendMessage(replaceAll);
            this.p2.sendMessage(replaceAll);
            this.setIsWinner(player.getUniqueId(), true);
            this.setIsWinner((b ? this.p2 : this.p1).getUniqueId(), false);
            if (b2) {
                this.addScore(player, 1);
            }
            Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(b ? this.p1 : this.p2, this, 1.0));
            this.p1.playSound(this.p1.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
            this.p2.playSound(this.p2.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
        }
        this.end(true);
    }
    
    private void startParticleLoop() {
        if (this.plugin.getConfig().getBoolean("pool.particles-enabled")) {
            final Location[] array = new Location[this.holes.length];
            for (int i = 0; i < this.holes.length; ++i) {
                array[i] = this.holes[i].clone().add(0.0, 0.7, 0.0);
            }
            if (LobbyGames.SERVER_VERSION > 12) {
                new GameTask() {
                    private final /* synthetic */ Particle.DustOptions val$dsop = new Particle.DustOptions(Color.fromRGB(20, 20, 20), 1.0f);
                    
                    public void run() {
                        if (!Pool.this.canStart()) {
                            this.cancel();
                        }
                        Location[] val$plocs;
                        for (int length = (val$plocs = array).length, i = 0; i < length; ++i) {
                            final Location location = val$plocs[i];
                            location.getWorld().spawnParticle(Particle.DUST, location, 1, (Object)this.val$dsop);
                        }
                    }
                }.runTaskTimer((Plugin)this.plugin, 1L, 2L);
            }
            else {
                new GameTask() {
                    public void run() {
                        if (!Pool.this.canStart()) {
                            this.cancel();
                        }
                        Location[] val$plocs;
                        for (int length = (val$plocs = array).length, i = 0; i < length; ++i) {
                            final Location location = val$plocs[i];
                            location.getWorld().spawnParticle(Particle.FALLING_DUST, location, 1);
                        }
                    }
                }.runTaskTimer((Plugin)this.plugin, 1L, 2L);
            }
        }
    }
    
    public int randomBallNumber(final List<Integer> list) {
        final int n = this.random.nextInt(14) + 2;
        if (list.contains(n)) {
            return this.randomBallNumber(list);
        }
        list.add(n);
        return n;
    }
    
    public void reset() {
        // 在Folia上，如果可能不在区域线程中，延迟1 tick执行以确保在区域线程中
        // 这个检查通过尝试访问方块来判断
        if (SchedulerUtil.isFolia()) {
            try {
                // 尝试访问方块，如果抛出异常说明不在区域线程中
                this.getArena().getCenterPixel().getBlock().getType();
            } catch (Exception e) {
                // 不在区域线程中，延迟执行
                new GameTask() {
                    public void run() {
                        Pool.this.resetInternal();
                    }
                }.runLocationTask((Plugin)this.plugin, this.getArena().getCenterPixel());
                return;
            }
        }
        
        // 在正确的线程中，直接执行
        this.resetInternal();
    }
    
    private void resetInternal() {
        this.rail_hits = 0;
        this.first_draw = true;
        this.in_play = false;
        // 在 Folia 上，不能同步检查 chunk 是否加载
        // 如果 chunk 未加载，游戏任务不会运行，所以这个检查是安全的
        if (!SchedulerUtil.isFolia()) {
            if (!this.getArena().getCenterPixel().getChunk().isLoaded()) {
                return;
            }
        }
        
        // 先手动清除旧球（如果存在）
        if (this.balls != null) {
            for (int i = 0; i < this.balls.length; i++) {
                if (this.balls[i] != null) {
                    // 直接删除盔甲架，不使用异步
                    if (this.balls[i].getArmorStand() != null && !this.balls[i].getArmorStand().isDead()) {
                        this.balls[i].getArmorStand().remove();
                    }
                    this.balls[i] = null;
                }
            }
        }
        
        // 清除旧的洞口盔甲架
        if (this.holes_as != null) {
            for (int i = 0; i < this.holes_as.length; i++) {
                if (this.holes_as[i] != null && !this.holes_as[i].isDead()) {
                    this.holes_as[i].remove();
                    this.holes_as[i] = null;
                }
            }
        }
        
        // 填充台球桌方块
        // 注意：reset() 方法本身已经在区域任务中被调用，所以这里可以直接执行方块操作
        if (LobbyGames.SERVER_VERSION <= 12) {
            GameUtils.fill(this.getArena(), Material.valueOf("WOOL"), (byte)13, null, (byte)0, false);
        } else {
            GameUtils.fill(this.getArena(), Material.GREEN_WOOL, (byte)0, null, (byte)0, false);
        }
        
        final Arena arena = this.getArena();
        this.balls = new BilliardBall[16];
        this.holes_as = new ArmorStand[6];  // 初始化 holes_as 数组
        final Vector vector = new Vector(0.0, 0.32, 0.0);
        this.holes = new Location[6];
        final Location location1 = arena.getLocation1();
        final Location location2 = arena.getLocation2();
        switch (arena.getCoordinateRotation()) {
            case 1: {
                location1.add(0.0, 0.0, 1.0);
                location2.add(1.0, 0.0, 0.0);
                break;
            }
            case 2: {
                location1.add(1.0, 0.0, 1.0);
                break;
            }
            case 3: {
                location1.add(1.0, 0.0, 0.0);
                location2.add(0.0, 0.0, 1.0);
                break;
            }
            default: {
                location2.add(1.0, 0.0, 1.0);
                break;
            }
        }
        this.holes[0] = location1;
        this.holes[1] = location2;
        this.holes[2] = new Location(location1.getWorld(), location1.getX(), location1.getY(), location2.getZ());
        this.holes[3] = new Location(location1.getWorld(), location2.getX(), location1.getY(), location1.getZ());
        if (this.xplane) {
            this.holes[4] = new Location(location1.getWorld(), (location1.getX() + location2.getX()) / 2.0, location1.getY(), location1.getZ());
            this.holes[5] = new Location(location1.getWorld(), (location1.getX() + location2.getX()) / 2.0, location1.getY(), location2.getZ());
        }
        else {
            this.holes[4] = new Location(location1.getWorld(), location1.getX(), location1.getY(), (location1.getZ() + location2.getZ()) / 2.0);
            this.holes[5] = new Location(location1.getWorld(), location2.getX(), location1.getY(), (location1.getZ() + location2.getZ()) / 2.0);
        }
        final boolean boolean1 = this.plugin.getConfig().getBoolean("pool.carpets-enabled", true);
        for (int i = 0; i < 6; ++i) {
            if (this.holes[i] != null) {
                final ArmorStandFactory setSmall = new ArmorStandFactory(this.holes[i].add(vector)).setSmall(true);
                if (boolean1) {
                    setSmall.setHeadItem((LobbyGames.SERVER_VERSION <= 12) ? new ItemStack(Material.valueOf("CARPET"), 1, (short)15) : new ItemStack(Material.BLACK_CARPET, 1));
                }
                this.holes_as[i] = setSmall.spawn();
            }
        }
        final int n = (arena.getCoordinateRotation() == 0 || arena.getCoordinateRotation() == 1) ? 1 : -1;
        final int n2 = (arena.getCoordinateRotation() != 0 && arena.getCoordinateRotation() != 1) ? 1 : 0;
        final boolean b = Math.max(arena.getHeight(), arena.getWidth()) == 5;
        final Location pixel = this.getPixel(n2 - n, 0);
        final Location pixel2 = this.getPixel(n + n2, 0);
        
        // 只支持大桌（5格长）
        if (this.xplane) {
            // 从配置文件读取位置偏移，允许管理员调整
            // 默认值：白球 -0.3，彩球 -0.3
            double defaultCueBall = -0.3;
            double defaultColorBall = -0.3;
            double cueBallOffset = this.plugin.getConfig().getDouble("pool.cue-ball-offset", -1);
            double colorBallOffset = this.plugin.getConfig().getDouble("pool.color-ball-offset", -1);
            
            // -1 表示使用默认值
            if (cueBallOffset == -1) cueBallOffset = defaultCueBall;
            if (colorBallOffset == -1) colorBallOffset = defaultColorBall;
            
            pixel.add(n * cueBallOffset, 0.0, 0.5);
            pixel2.add(n * colorBallOffset, 0.0, 0.5);
        }
        else {
            // 从配置文件读取位置偏移，允许管理员调整
            // 默认值：白球 -0.3，彩球 -0.3
            double defaultCueBall = -0.3;
            double defaultColorBall = -0.3;
            double cueBallOffset = this.plugin.getConfig().getDouble("pool.cue-ball-offset", -1);
            double colorBallOffset = this.plugin.getConfig().getDouble("pool.color-ball-offset", -1);
            
            // -1 表示使用默认值
            if (cueBallOffset == -1) cueBallOffset = defaultCueBall;
            if (colorBallOffset == -1) colorBallOffset = defaultColorBall;
            
            pixel.add(0.5, 0.0, n * cueBallOffset);
            pixel2.add(0.5, 0.0, n * colorBallOffset);
        }
        this.balls[0] = new BilliardBall(0, pixel, this);
        Location location3 = pixel2.clone();
        final double n3 = BilliardBall.BALL_DIAMETER + 0.027;
        final ArrayList list = new ArrayList();
        int[] array;
        for (int length = (array = new int[] { 8, 7, 14 }).length, j = 0; j < length; ++j) {
            list.add(array[j]);
        }
        int n4 = 1;
        for (int k = 1; k < 6; ++k) {
            for (int l = 0; l < k; ++l) {
                int randomBallNumber = 0;
                switch (n4) {
                    case 1: {
                        randomBallNumber = 1;
                        break;
                    }
                    case 5: {
                        randomBallNumber = 8;
                        break;
                    }
                    case 11: {
                        randomBallNumber = 7;
                        break;
                    }
                    case 15: {
                        randomBallNumber = 14;
                        break;
                    }
                    default: {
                        randomBallNumber = this.randomBallNumber(list);
                        break;
                    }
                }
                this.balls[randomBallNumber] = new BilliardBall(randomBallNumber, location3, this);
                if (this.xplane) {
                    location3.add(0.0, 0.0, -n3);
                }
                else {
                    location3.add(n3, 0.0, 0.0);
                }
                ++n4;
            }
            if (this.xplane) {
                pixel2.add(n * n3, 0.0, n3 / 2.0);
            }
            else {
                pixel2.add(-n3 / 2.0, 0.0, n * n3);
            }
            location3 = pixel2.clone();
        }
    }
    
    @Override
    public void end() {
        this.plugin.getLogger().info("[Pool Debug] end() no-param called, delegating to end(true)");
        this.end(true);
    }
    
    public void end(final boolean b) {
        // 添加调试日志
        this.plugin.getLogger().info("[Pool Debug] end() called, b=" + b);
        
        if (b) {
            this.plugin.getProximityDelay().put(this.p1.getUniqueId(), System.currentTimeMillis() + 7000L);
            if (this.p2 != null) {
                this.plugin.getProximityDelay().put(this.p2.getUniqueId(), System.currentTimeMillis() + 7000L);
            }
        }
        
        // 在 Folia 上，实体操作必须在区域线程中执行
        final Location center = this.getArena().getCenterPixel();
        this.plugin.getLogger().info("[Pool Debug] isFolia=" + SchedulerUtil.isFolia() + ", center=" + (center != null));
        
        if (SchedulerUtil.isFolia() && center != null) {
            // 使用区域任务来清理实体，清理完成后再调用 super.end()
            this.plugin.getLogger().info("[Pool Debug] Scheduling region task for cleanup");
            Bukkit.getRegionScheduler().run(this.plugin, center, task -> {
                Pool.this.plugin.getLogger().info("[Pool Debug] Region task executing, calling cleanupEntities()");
                Pool.this.cleanupEntities();
                // 清理完成后，使用全局任务调用 super.end()
                Bukkit.getGlobalRegionScheduler().run(Pool.this.plugin, globalTask -> {
                    Pool.this.plugin.getLogger().info("[Pool Debug] Calling super.end()");
                    Pool.super.end();
                });
            });
        } else {
            // 非 Folia 环境，直接清理
            this.plugin.getLogger().info("[Pool Debug] Non-Folia, calling cleanupEntities() directly");
            this.cleanupEntities();
            super.end();
        }
    }
    
    private void cleanupEntities() {
        this.plugin.getLogger().info("[Pool Debug] cleanupEntities() called");
        
        // 清除所有球和盔甲架
        if (this.balls != null) {
            this.plugin.getLogger().info("[Pool Debug] balls array length: " + this.balls.length);
            for (int i = 0; i < this.balls.length; i++) {
                if (this.balls[i] != null) {
                    final ArmorStand armorStand = this.balls[i].getArmorStand();
                    if (armorStand != null && !armorStand.isDead()) {
                        this.plugin.getLogger().info("[Pool Debug] Removing ball " + i + " armor stand");
                        armorStand.remove();
                    }
                    this.balls[i] = null;
                }
            }
        } else {
            this.plugin.getLogger().warning("[Pool Debug] balls array is NULL!");
        }
        
        // 清除洞口盔甲架
        if (this.holes_as != null) {
            this.plugin.getLogger().info("[Pool Debug] holes_as array length: " + this.holes_as.length);
            for (int i = 0; i < this.holes_as.length; i++) {
                if (this.holes_as[i] != null && !this.holes_as[i].isDead()) {
                    this.plugin.getLogger().info("[Pool Debug] Removing hole " + i + " armor stand");
                    this.holes_as[i].remove();
                    this.holes_as[i] = null;
                }
            }
        }
        
        // 额外保险：清除竞技场区域内的所有盔甲架
        final Location center = this.getArena().getCenterPixel();
        if (center != null && center.getWorld() != null) {
            final double radius = Math.max(this.getArena().getWidth(), this.getArena().getHeight()) + 2;
            final Location loc1 = this.getArena().getLocation1();
            final Location loc2 = this.getArena().getLocation2();
            final double minX = Math.min(loc1.getX(), loc2.getX());
            final double maxX = Math.max(loc1.getX(), loc2.getX()) + 1;
            final double minY = Math.min(loc1.getY(), loc2.getY());
            final double maxY = Math.max(loc1.getY(), loc2.getY()) + 3;
            final double minZ = Math.min(loc1.getZ(), loc2.getZ());
            final double maxZ = Math.max(loc1.getZ(), loc2.getZ()) + 1;
            
            this.plugin.getLogger().info("[Pool Debug] Scanning for armor stands in arena bounds");
            int removedCount = 0;
            
            for (org.bukkit.entity.Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (entity instanceof ArmorStand) {
                    final ArmorStand as = (ArmorStand) entity;
                    final Location loc = as.getLocation();
                    
                    if (loc.getX() >= minX && loc.getX() <= maxX &&
                        loc.getY() >= minY && loc.getY() <= maxY &&
                        loc.getZ() >= minZ && loc.getZ() <= maxZ) {
                        as.remove();
                        removedCount++;
                    }
                }
            }
            
            this.plugin.getLogger().info("[Pool Debug] Removed " + removedCount + " armor stands from arena");
        }
    }
    
    public int sinkedCount() {
        int n = 0;
        for (int i = 1; i < this.balls.length; ++i) {
            if (i != 8 && (this.balls[i] == null || this.balls[i].isSunk())) {
                ++n;
            }
        }
        return n;
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitLobbyGameEvent playerQuitLobbyGameEvent) {
        if (!this.canStart()) {
            return;
        }
        if (playerQuitLobbyGameEvent.getPlayer().getUniqueId().equals(this.p1.getUniqueId())) {
            this.win(false, this.sinkedCount() > 4);
        }
        else if (this.p2 != null && playerQuitLobbyGameEvent.getPlayer().getUniqueId().equals(this.p2.getUniqueId())) {
            this.win(true, this.sinkedCount() > 4);
        }
    }
    
    @EventHandler
    public void onEnd(final GameEndEvent gameEndEvent) {
        if (gameEndEvent.getGame().getGameType() == this.getGameType() && gameEndEvent.getGame().getArena().getID() == super.getArena().getID()) {
            this.plugin.getLogger().info("[Pool Debug] onEnd event triggered, unregistering listeners");
            HandlerList.unregisterAll((Listener)this);
            // 不要自动重置球台，让下一个游戏开始时再重置
            // 这样可以避免在没有人玩的时候生成球
        }
    }
}
