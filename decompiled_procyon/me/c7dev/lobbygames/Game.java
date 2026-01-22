// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames;

import me.c7dev.lobbygames.api.events.GameEndEvent;
import org.bukkit.OfflinePlayer;
import me.c7dev.lobbygames.util.GameReward;
import org.bukkit.command.CommandSender;
import me.c7dev.lobbygames.api.events.PlayerQuitLobbyGameEvent;
import org.bukkit.Location;
import me.c7dev.lobbygames.util.CoordinatePair;
import me.c7dev.lobbygames.util.Leaderboard;
import org.bukkit.plugin.Plugin;
import java.util.Iterator;
import me.c7dev.lobbygames.util.GameUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.World;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.PlayerJoinLobbyGameEvent;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import java.util.Random;
import me.c7dev.lobbygames.util.PlayerStats;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import me.c7dev.lobbygames.util.GameType;

public class Game
{
    private GameType gt;
    private List<UUID> players;
    private List<UUID> no_endtp;
    private HashMap<UUID, ItemStack[]> inventories;
    private HashMap<UUID, ItemStack[]> quit_confirmation;
    private HashMap<UUID, GameMode> p_gm;
    private boolean active;
    private boolean can_start;
    private boolean was_active;
    protected boolean defaultIsWinner;
    protected LobbyGames plugin;
    protected Arena arena;
    protected int score;
    private long starttime;
    private HashMap<UUID, PlayerStats> temporary_stats;
    private HashMap<UUID, Boolean> prev_fly;
    private HashMap<UUID, Boolean> is_winner;
    private HashMap<UUID, Long> last_move;
    protected Random random;
    
    public Game(final LobbyGames v1, final GameType v2, final Arena v3, final Player v4) {
        this.players = new ArrayList<UUID>();
        this.no_endtp = new ArrayList<UUID>();
        this.inventories = new HashMap<UUID, ItemStack[]>();
        this.quit_confirmation = new HashMap<UUID, ItemStack[]>();
        this.p_gm = new HashMap<UUID, GameMode>();
        this.active = false;
        this.can_start = false;
        this.was_active = false;
        this.defaultIsWinner = false;
        this.score = 0;
        this.starttime = System.currentTimeMillis() / 1000L;
        this.temporary_stats = new HashMap<UUID, PlayerStats>();
        this.prev_fly = new HashMap<UUID, Boolean>();
        this.is_winner = new HashMap<UUID, Boolean>();
        this.last_move = new HashMap<UUID, Long>();
        if (v4 == null || !v4.isOnline()) {
            throw new IllegalArgumentException("Could not start a game because a player (" + v4.getName() + ") is offline!");
        }
        (this.players = new ArrayList<UUID>()).add(v4.getUniqueId());
        this.gt = v2;
        this.plugin = v1;
        this.random = new Random();
        v1.getClass();
        if (!"User: 640139, Resource: 109780, Nonce: 1648310078, Verity token: %%__VERIFY_TOKEN__%%, isPolymart: %%__POLYMART__%%, Timestamp: %%__TIMESTAMP__%%".startsWith("User: ")) {
            return;
        }
        if (v3 == null || v3.getHostingGame() != null) {
            Bukkit.getLogger().warning("Could not start a new game because the arena doesn't exist or is already in use!");
            return;
        }
        if (v3.getGameType() != this.gt) {
            Bukkit.getLogger().warning("Could not start a game because the arena types do not match!");
            return;
        }
        if (v3.getLeaderboard() != null) {
            v3.getLeaderboard().updateDisplay();
        }
        this.can_start = true;
        (this.arena = v3).setHostingGame(this);
        final Object v5 = v1.getActiveGames().get(v4.getUniqueId());
        if (v5 != null) {
            ((Game)v5).removePlayer(v4);
        }
        v1.getActiveGames().put(v4.getUniqueId(), this);
        Bukkit.getPluginManager().callEvent((Event)new PlayerJoinLobbyGameEvent(v4, this));
        this.afkTimer();
    }
    
    public GameType getGameType() {
        return this.gt;
    }
    
    public Player getPlayer1() {
        return (this.players.size() > 0) ? Bukkit.getPlayer((UUID)this.players.get(0)) : null;
    }
    
    public List<UUID> getPlayers() {
        return this.players;
    }
    
    public boolean isActive() {
        return this.active;
    }
    
    public boolean wasActive() {
        return this.was_active;
    }
    
    public boolean canStart() {
        return this.can_start;
    }
    
    public LobbyGames getPlugin() {
        return this.plugin;
    }
    
    public World getWorld() {
        return this.arena.getCenterPixel().getWorld();
    }
    
    public void setActive(final boolean active) {
        this.active = active;
        if (this.active) {
            this.was_active = true;
            this.starttime = System.currentTimeMillis() / 1000L;
        }
    }
    
    public Arena getArena() {
        return this.arena;
    }
    
    public String getPlayTime() {
        int n = 0;
        if (this.was_active) {
            n = (int)(System.currentTimeMillis() / 1000L - this.starttime);
        }
        final int n2 = n / 60;
        String s = "" + n % 60;
        if (n2 > 0) {
            s = n2 + "m, " + s;
        }
        return s;
    }
    
    private void afkTimer() {
        new BukkitRunnable() {
            private final /* synthetic */ double val$max_seconds = Game.this.plugin.getConfig().getDouble("afk-kick-seconds", 90.0);
            
            public void run() {
                if (!Game.this.can_start) {
                    this.cancel();
                    return;
                }
                if (this.val$max_seconds <= 0.0) {
                    return;
                }
                final long currentTimeMillis = System.currentTimeMillis();
                for (final UUID key : Game.this.players) {
                    double n;
                    if (Game.this.last_move.containsKey(key)) {
                        n = Game.this.last_move.get(key);
                    }
                    else {
                        Game.this.last_move.put(key, currentTimeMillis);
                        n = (double)currentTimeMillis;
                    }
                    if ((currentTimeMillis - n) / 1000.0 > this.val$max_seconds) {
                        final Player player = Bukkit.getPlayer(key);
                        if (player == null) {
                            continue;
                        }
                        Game.this.removePlayer(player);
                        final String replaceAll = Game.this.plugin.getConfigString("afk-kick-msg").replaceAll("\\Q%game%\\E", GameUtils.outgoingAliases(Game.this.gt, Game.this.plugin));
                        if (replaceAll.length() <= 0) {
                            continue;
                        }
                        player.sendMessage(replaceAll);
                    }
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 60L);
    }
    
    public void refreshAFKTimer(final Player player) {
        this.refreshAFKTimer(player.getUniqueId());
    }
    
    public void refreshAFKTimer(final UUID key) {
        this.last_move.put(key, System.currentTimeMillis());
    }
    
    public boolean containsPlayer(final Player player) {
        return this.arena.getWorld().getName().toString().equals(player.getWorld().getName().toString()) && this.players.contains(player.getUniqueId());
    }
    
    public void quitConfirmation(final Player player) {
        if (!this.inventories.containsKey(player.getUniqueId()) || System.currentTimeMillis() / 1000L - this.starttime < 1L) {
            return;
        }
        this.quit_confirmation.put(player.getUniqueId(), player.getInventory().getContents());
        for (int i = 0; i < 36; ++i) {
            player.getInventory().clear(i);
        }
        player.getInventory().setItem(3, GameUtils.createWool(1, 5, this.plugin.getConfigString("yes-text", "§a§lYes"), new String[0]));
        player.getInventory().setItem(5, GameUtils.createWool(1, 14, this.plugin.getConfigString("no-text", "§c§lNo"), new String[0]));
    }
    
    public void removeQuitConfirmation(final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            this.removeQuitConfirmation(player);
        }
    }
    
    public void removeQuitConfirmation(final Player player) {
        final ItemStack[] contents = this.quit_confirmation.get(player.getUniqueId());
        if (contents == null) {
            return;
        }
        this.quit_confirmation.remove(player.getUniqueId());
        player.getInventory().setContents(contents);
        if (LobbyGames.SERVER_VERSION <= 12) {
            player.updateInventory();
        }
    }
    
    public boolean isInQuitConfirmation(final UUID key) {
        return this.quit_confirmation.containsKey(key);
    }
    
    public void addScore(final Player player, final int n) {
        this.addScore(player, n, null);
    }
    
    public void addScore(final Player player, final int n, final String s) {
        if (this.gt.isMultiplayer()) {
            this.plugin.getHighScoreRawAsync(player.getUniqueId(), this.gt, n2 -> {
                int currentScore = (n2 != null) ? n2.intValue() : 0;
                if (currentScore == Integer.MIN_VALUE) {
                    currentScore = 0;
                }
                this.handleAddScore(player, currentScore + 1, s);
            });
        }
        else {
            this.handleAddScore(player, n, s);
        }
    }
    
    private void handleAddScore(final Player player, final int n, final String s) {
        final String s2 = this.plugin.getConfig().getBoolean("use-display-names") ? player.getDisplayName() : player.getName();
        if (this.arena.getLeaderboard() != null) {
            this.arena.getLeaderboard().addScore(player.getUniqueId(), s2, n, s, this.gt.isMultiplayer());
        }
        final List list = this.plugin.getGlobalLeaderboards().get(this.gt);
        if (list != null) {
            final Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                ((Leaderboard)iterator.next()).addScore(player.getUniqueId(), s2, n, s);
            }
        }
        this.temporary_stats.put(player.getUniqueId(), new PlayerStats(n, s, 0, (int)(System.currentTimeMillis() / 1000L - this.starttime)));
    }
    
    public boolean isInvPrepared(final Player player) {
        return this.inventories.containsKey(player.getUniqueId());
    }
    
    public void preparePlayer(final Player player, final GameMode gameMode) {
        if (this.inventories.containsKey(player.getUniqueId())) {
            return;
        }
        this.inventories.put(player.getUniqueId(), player.getInventory().getContents());
        player.getInventory().clear();
        this.p_gm.put(player.getUniqueId(), player.getGameMode());
        if (this.gt.isFlyDisabled()) {
            this.prev_fly.put(player.getUniqueId(), player.getAllowFlight());
            player.setAllowFlight(false);
        }
        if (gameMode != null) {
            player.setGameMode(gameMode);
        }
    }
    
    public void preparePlayer(final Player player) {
        if (this.inventories.containsKey(player.getUniqueId())) {
            return;
        }
        this.inventories.put(player.getUniqueId(), player.getInventory().getContents());
        player.getInventory().clear();
        this.p_gm.put(player.getUniqueId(), player.getGameMode());
        if (this.gt.isFlyDisabled()) {
            this.prev_fly.put(player.getUniqueId(), player.getAllowFlight());
            player.setAllowFlight(false);
        }
    }
    
    public void returnPlayerInv(final Player player) {
        if (player.isOnline()) {
            if (this.inventories.containsKey(player.getUniqueId())) {
                player.getInventory().setContents((ItemStack[])this.inventories.get(player.getUniqueId()));
                this.inventories.remove(player.getUniqueId());
                if (LobbyGames.SERVER_VERSION <= 12) {
                    player.updateInventory();
                }
            }
            if (this.p_gm.containsKey(player.getUniqueId())) {
                player.setGameMode((GameMode)this.p_gm.get(player.getUniqueId()));
            }
            if (this.prev_fly.containsKey(player.getUniqueId())) {
                player.setAllowFlight((boolean)this.prev_fly.get(player.getUniqueId()));
                this.prev_fly.remove(player.getUniqueId());
            }
        }
    }
    
    public CoordinatePair randomPixel() {
        final CoordinatePair v1 = new CoordinatePair(this.random.nextInt(this.arena.getWidth()) - this.arena.getWidth() / 2, this.random.nextInt(this.arena.getHeight()) - this.arena.getHeight() / 2);
        if (!this.arena.isInBounds(this.getPixel(v1))) {
            return this.randomPixel();
        }
        return v1;
    }
    
    public Location getPixel(final CoordinatePair coordinatePair) {
        return this.getPixel(coordinatePair.getX(), coordinatePair.getY());
    }
    
    public void restart() {
    }
    
    public String getScore(final Player player) {
        return this.getScore();
    }
    
    public String getScore() {
        return "" + this.score;
    }
    
    public int getScoreInt() {
        return this.score;
    }
    
    public int getScoreInt(final Player player) {
        return this.score;
    }
    
    public CoordinatePair getCoords(final Location location) {
        final Location centerPixel = this.arena.getCenterPixel();
        if (!this.arena.isVerticalLayout()) {
            return new CoordinatePair(location.getBlockX() - centerPixel.getBlockX(), location.getBlockZ() - centerPixel.getBlockZ());
        }
        switch (this.arena.getCoordinateRotation()) {
            case 1: {
                return new CoordinatePair(location.getBlockZ() - centerPixel.getBlockZ(), location.getBlockY() - centerPixel.getBlockY());
            }
            case 2: {
                return new CoordinatePair(location.getBlockX() - centerPixel.getBlockX(), location.getBlockY() - centerPixel.getBlockY());
            }
            case 3: {
                return new CoordinatePair(centerPixel.getBlockZ() - location.getBlockZ(), location.getBlockY() - centerPixel.getBlockY());
            }
            default: {
                return new CoordinatePair(centerPixel.getBlockX() - location.getBlockX(), location.getBlockY() - centerPixel.getBlockY());
            }
        }
    }
    
    public Location getPixel(final int n, final int n2) {
        final Location centerPixel = this.arena.getCenterPixel();
        if (this.arena.isVerticalLayout()) {
            if (this.gt.isDirectBlockMapping()) {
                switch (this.arena.getCoordinateRotation()) {
                    case 1: {
                        return centerPixel.add(0.0, (double)n2, (double)n);
                    }
                    case 2: {
                        return centerPixel.add((double)n, (double)n2, 0.0);
                    }
                    case 3: {
                        return centerPixel.add(0.0, (double)n2, (double)(-n));
                    }
                    default: {
                        return centerPixel.add((double)(-n), (double)n2, 0.0);
                    }
                }
            }
            else {
                switch (this.arena.getCoordinateRotation()) {
                    case 1: {
                        return centerPixel.add(0.0, (double)n2, (double)n);
                    }
                    case 2: {
                        return centerPixel.add((double)(-n), (double)n2, 0.0);
                    }
                    case 3: {
                        return centerPixel.add(0.0, (double)n2, (double)(-n));
                    }
                    default: {
                        return centerPixel.add((double)n, (double)n2, 0.0);
                    }
                }
            }
        }
        else {
            if (this.gt.isDirectBlockMapping()) {
                return centerPixel.add((double)n, 0.0, (double)n2);
            }
            switch (this.arena.getCoordinateRotation()) {
                case 1: {
                    return centerPixel.add((double)n, 0.0, (double)(-n2));
                }
                case 2: {
                    return centerPixel.add((double)(-n2), 0.0, (double)(-n));
                }
                case 3: {
                    return centerPixel.add((double)(-n), 0.0, (double)n2);
                }
                default: {
                    return centerPixel.add((double)n2, 0.0, (double)n);
                }
            }
        }
    }
    
    public void appendPlayer(final Player player) {
        if (this.gt.isMultiplayer()) {
            final Game game = this.plugin.getActiveGames().get(player.getUniqueId());
            if (game != null) {
                game.removePlayer(player);
            }
            this.plugin.getActiveGames().put(player.getUniqueId(), this);
            this.players.add(player.getUniqueId());
            Bukkit.getPluginManager().callEvent((Event)new PlayerJoinLobbyGameEvent(player, this));
        }
    }
    
    public void removePlayer(final Player player) {
        if (!this.players.contains(player.getUniqueId())) {
            return;
        }
        if (this.arena.getSpawn1() != null && !this.no_endtp.contains(player.getUniqueId())) {
            this.plugin.teleportToSpawn(player, this.arena);
        }
        this.plugin.removeReturnLocation(player.getUniqueId());
        if (this.players.size() <= 1 || (this.gt.isMultiplayer() && this.players.size() <= (this.active ? 2 : 1))) {
            this.end();
        }
        else {
            this.players.remove(player.getUniqueId());
            this.plugin.getActiveGames().remove(player.getUniqueId());
            this.returnPlayerInv(player);
            final long long1 = this.plugin.getConfig().getLong("cooldown-seconds");
            if (long1 > 0L) {
                this.plugin.getJoinCooldown().put(player.getUniqueId(), System.currentTimeMillis() + 1000L * long1);
            }
            Bukkit.getPluginManager().callEvent((Event)new PlayerQuitLobbyGameEvent(player, this));
            if (this.was_active && !this.temporary_stats.containsKey(player.getUniqueId())) {
                this.temporary_stats.put(player.getUniqueId(), new PlayerStats(Integer.MIN_VALUE, "0", 0, (int)(System.currentTimeMillis() / 1000L - this.starttime)));
            }
            this.sendRewards(player);
        }
    }
    
    public void noEndTeleportation(final UUID uuid) {
        this.no_endtp.add(uuid);
    }
    
    public String getStatusString() {
        if (this.players.size() == 0) {
            return this.plugin.getStatusTranslate(0);
        }
        return this.plugin.getStatusTranslate(this.active ? 2 : 1);
    }
    
    public void clearArmorStands() {
        this.arena.clearArmorStands();
    }
    
    public int getDuration() {
        if (!this.was_active) {
            return 0;
        }
        return (int)(System.currentTimeMillis() / 1000L - this.starttime);
    }
    
    public void setIsWinner(final UUID key, final boolean b) {
        if (key != null) {
            this.is_winner.put(key, b);
        }
    }
    
    public boolean hasWinnerEntrySet(final UUID key) {
        return this.is_winner.containsKey(key);
    }
    
    protected void sendRewards() {
        final String s = GameUtils.getConfigName(this.gt) + ".console-command-on-end";
        if (this.plugin.getConfig().getString(s) != null) {
            final String configString = this.plugin.getConfigString(s);
            final String name = this.getWinner().getName();
            if (configString != null && configString.length() > 0) {
                String[] split;
                for (int length = (split = configString.split("\\Q[newline]\\E|\n")).length, i = 0; i < length; ++i) {
                    String substring = split[i];
                    if (substring.startsWith("/")) {
                        substring = substring.substring(1);
                    }
                    final String replaceAll = this.plugin.sp(this.getPlayer1(), substring.trim()).replaceAll("\\Q%score%\\E", this.getScore()).replaceAll("\\Q%score_raw%\\E", "" + this.getScoreInt()).replaceAll("\\Q%winner%\\E", name).replaceAll("\\Q%player%\\E", this.getPlayer1().getName());
                    if (replaceAll.length() != 0) {
                        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), replaceAll);
                    }
                }
            }
        }
        final GameReward[] gameRewards = this.plugin.getGameRewards(this.gt);
        if (gameRewards.length == 0) {
            return;
        }
        final Iterator<UUID> iterator = this.players.iterator();
        while (iterator.hasNext()) {
            final Player player = Bukkit.getPlayer((UUID)iterator.next());
            if (player == null) {
                continue;
            }
            this.sendRewards(player);
        }
        GameReward[] array;
        for (int length2 = (array = gameRewards).length, j = 0; j < length2; ++j) {
            final GameReward gameReward = array[j];
            if (!gameReward.isPerPlayer()) {
                gameReward.reward(null, true, this);
            }
        }
    }
    
    private void sendRewards(final Player player) {
        GameReward[] gameRewards;
        for (int length = (gameRewards = this.plugin.getGameRewards(this.gt)).length, i = 0; i < length; ++i) {
            final GameReward gameReward = gameRewards[i];
            if (gameReward.isPerPlayer()) {
                gameReward.reward(player, this.is_winner.getOrDefault(player.getUniqueId(), this.defaultIsWinner), this);
            }
        }
    }
    
    public OfflinePlayer getWinner() {
        if (this.is_winner.size() == 0) {
            return (OfflinePlayer)this.getPlayer1();
        }
        for (final UUID key : this.players) {
            if (this.is_winner.getOrDefault(key, false)) {
                return Bukkit.getOfflinePlayer(key);
            }
        }
        return (OfflinePlayer)this.getPlayer1();
    }
    
    public boolean isWinner(final UUID key) {
        return this.is_winner.getOrDefault(key, this.defaultIsWinner);
    }
    
    public void end() {
        if (!this.can_start) {
            return;
        }
        new BukkitRunnable() {
            public void run() {
                if (Game.this.was_active) {
                    final int n = (int)(System.currentTimeMillis() / 1000L - Game.this.starttime);
                    for (int i = 0; i < Game.this.players.size(); ++i) {
                        final Player player = Bukkit.getPlayer((UUID)Game.this.players.get(i));
                        if (player != null) {
                            PlayerStats playerStats = Game.this.temporary_stats.get(player.getUniqueId());
                            if (playerStats == null) {
                                playerStats = new PlayerStats(Integer.MIN_VALUE, "0", 0, n);
                            }
                            Game.this.plugin.setHighScore(player, Game.this.gt, playerStats, i >= Game.this.players.size() - 1);
                        }
                    }
                }
            }
        }.runTaskLaterAsynchronously((Plugin)this.plugin, 10L);
        this.active = false;
        this.can_start = false;
        this.arena.setHostingGame(null);
        Bukkit.getPluginManager().callEvent((Event)new GameEndEvent(this));
        final long long1 = this.plugin.getConfig().getLong("cooldown-seconds");
        final long l = System.currentTimeMillis() + 1000L * long1;
        for (final UUID uuid : this.players) {
            final Player player = Bukkit.getPlayer(uuid);
            this.plugin.getActiveGames().remove(uuid);
            if (long1 > 0L) {
                this.plugin.getJoinCooldown().put(uuid, l);
            }
            if (player != null) {
                this.returnPlayerInv(player);
                Bukkit.getPluginManager().callEvent((Event)new PlayerQuitLobbyGameEvent(player, this));
            }
        }
        this.sendRewards();
        if (this.arena.getLeaderboard() != null) {
            this.plugin.saveArenas();
        }
    }
}
