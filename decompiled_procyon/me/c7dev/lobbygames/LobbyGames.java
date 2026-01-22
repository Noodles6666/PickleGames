// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames;

import java.net.URLConnection;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.function.Consumer;
import me.c7dev.lobbygames.util.LeaderboardEntry;
import java.io.IOException;
import me.c7dev.lobbygames.games.Connect4;
import me.c7dev.lobbygames.games.Pool;
import me.c7dev.lobbygames.games.TicTacToe;
import me.c7dev.lobbygames.games.T048;
import me.c7dev.lobbygames.games.Sudoku;
import me.c7dev.lobbygames.games.Clicker;
import me.c7dev.lobbygames.games.Spleef;
import me.c7dev.lobbygames.games.Minesweeper;
import me.c7dev.lobbygames.games.Snake;
import me.c7dev.lobbygames.games.Memory;
import me.c7dev.lobbygames.games.Gomoku;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.Iterator;
import me.c7dev.lobbygames.games.Soccer;
import java.util.Map;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import me.c7dev.lobbygames.commands.ConsoleJoinCommand;
import me.c7dev.lobbygames.commands.JoinCommand;
import me.c7dev.lobbygames.commands.LobbyGamesCommand;
import me.c7dev.lobbygames.util.GameUtils;
import java.util.ArrayList;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.inventory.ItemStack;
import me.c7dev.lobbygames.util.DatabaseConnection;
import me.c7dev.lobbygames.commands.GameCreateInstance;
import me.c7dev.lobbygames.util.PAPIHook;
import org.bukkit.Location;
import me.c7dev.lobbygames.util.Leaderboard;
import me.c7dev.lobbygames.util.PlayerStats;
import me.c7dev.lobbygames.util.GameReward;
import java.util.List;
import me.c7dev.lobbygames.util.GameType;
import java.util.UUID;
import java.util.HashMap;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyGames extends JavaPlugin
{
    private HashMap<UUID, Game> active;
    private HashMap<GameType, List<Arena>> arenas;
    private HashMap<GameType, List<GameReward>> rewards;
    private HashMap<UUID, Long> join_cooldown;
    private HashMap<UUID, HashMap<GameType, PlayerStats>> highscore_cache;
    private HashMap<GameType, List<Leaderboard>> global_leaderboard;
    private HashMap<String, GameType> game_alias;
    private HashMap<GameType, String> outgoing_alias;
    private HashMap<UUID, Long> proximity_delay;
    private HashMap<UUID, Location> return_loc;
    private boolean interworld;
    private long leaderboard_expiry;
    private boolean saving_enabled;
    private boolean papi;
    private boolean save_highscores;
    private boolean pool_proximity;
    private boolean soccer_proximity;
    private boolean spleef_proximity;
    private boolean using_mysql;
    private boolean using_return_loc;
    private boolean disable_holograms;
    private PAPIHook papi_hook;
    private List<String> blocked_commands;
    private int command_whitelist_mode;
    private HashMap<UUID, GameCreateInstance> editing;
    private DatabaseConnection db;
    private ItemStack quit_item;
    private String[] status_words;
    private Economy eco;
    public static int SERVER_VERSION;
    public final String pluginData = "User: %%__USER__%%, Resource: %%__RESOURCE__%%, Nonce: %%__NONCE__%%, Verity token: %%__VERIFY_TOKEN__%%, isPolymart: %%__POLYMART__%%, Timestamp: %%__TIMESTAMP__%%";
    
    static {
        LobbyGames.SERVER_VERSION = 12;
    }
    
    public LobbyGames() {
        this.active = new HashMap<UUID, Game>();
        this.arenas = new HashMap<GameType, List<Arena>>();
        this.rewards = new HashMap<GameType, List<GameReward>>();
        this.join_cooldown = new HashMap<UUID, Long>();
        this.highscore_cache = new HashMap<UUID, HashMap<GameType, PlayerStats>>();
        this.global_leaderboard = new HashMap<GameType, List<Leaderboard>>();
        this.game_alias = new HashMap<String, GameType>();
        this.outgoing_alias = new HashMap<GameType, String>();
        this.proximity_delay = new HashMap<UUID, Long>();
        this.return_loc = new HashMap<UUID, Location>();
        this.interworld = true;
        this.leaderboard_expiry = 86400L;
        this.saving_enabled = false;
        this.papi = false;
        this.save_highscores = true;
        this.pool_proximity = false;
        this.soccer_proximity = false;
        this.spleef_proximity = true;
        this.using_mysql = false;
        this.using_return_loc = false;
        this.disable_holograms = false;
        this.blocked_commands = new ArrayList<String>();
        this.command_whitelist_mode = 0;
        this.editing = new HashMap<UUID, GameCreateInstance>();
        this.status_words = new String[] { "Open", "Waiting", "In Use" };
    }
    
    public String getVersion() {
        return this.getDescription().getVersion();
    }
    
    public void onEnable() {
        // 自动使用中文配置文件
        this.loadChineseConfig();
        
        GameType[] gameTypes = GameType.values();
        for (int i = 0; i < gameTypes.length; ++i) {
            final GameType gameType = gameTypes[i];
            this.arenas.put(gameType, new ArrayList<Arena>());
        }
        LobbyGames.SERVER_VERSION = GameUtils.getVersionInt();
        this.loadConfigSettings();
        new LobbyGamesCommand(this);
        new JoinCommand(this);
        new ConsoleJoinCommand(this);
        new EventListeners(this);
        new BukkitRunnable() {
            int count = 0;
            
            public void run() {
                try {
                    Bukkit.getConsoleSender().sendMessage("[LobbyGames] Loaded " + LobbyGames.this.loadArenas() + " arenas!");
                    this.cancel();
                }
                catch (final Exception ex) {
                    ex.printStackTrace();
                    ++this.count;
                    if (this.count >= 3) {
                        Bukkit.getLogger().severe("Could not load LobbyGames arenas! Please report this as a bug on the discord server.");
                        this.cancel();
                    }
                    else {
                        Bukkit.getConsoleSender().sendMessage("[LobbyGames] Failed to load arenas, trying again in 10 seconds...");
                    }
                }
            }
        }.runTaskTimer((Plugin)this, 0L, 200L);
        new BukkitRunnable() {
            public void run() {
                LobbyGames.this.reloadLeaderboards();
            }
        }.runTaskLater((Plugin)this, 6000L);
        this.papi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (this.papi) {
            this.save_highscores = true;
            (this.papi_hook = new PAPIHook(this)).register();
        }
        this.using_mysql = this.getConfig().getBoolean("mysql.enabled");
        if (this.using_mysql) {
            this.db = new DatabaseConnection(this);
            this.testMySQL();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            final Object v5 = this.getServer().getServicesManager().getRegistration((Class)Economy.class);
            if (v5 != null) {
                this.eco = (Economy)((RegisteredServiceProvider)v5).getProvider();
            }
        }
    }
    
    public void onDisable() {
        if (this.active != null) {
            for (final Map.Entry entry : this.active.entrySet()) {
                final Player player = Bukkit.getPlayer((UUID)entry.getKey());
                if (player == null) {
                    continue;
                }
                ((Game)entry.getValue()).returnPlayerInv(player);
            }
        }
        GameType[] values;
        for (int length = (values = GameType.values()).length, i = 0; i < length; ++i) {
            final GameType gameType = values[i];
            if (this.arenas.containsKey(gameType)) {
                for (final Arena arena : this.arenas.get(gameType)) {
                    if (arena.getLeaderboard() != null) {
                        arena.getLeaderboard().remove();
                    }
                    if (gameType == GameType.SOCCER && arena.getHostingGame() != null) {
                        final Soccer soccer = (Soccer)arena.getHostingGame();
                        if (soccer.getBall() != null) {
                            soccer.getBall().remove();
                        }
                        soccer.removeAllSpectators();
                    }
                }
            }
        }
        if (this.global_leaderboard != null) {
            for (Map.Entry<GameType, List<Leaderboard>> entry : this.global_leaderboard.entrySet()) {
                for (Leaderboard leaderboard : entry.getValue()) {
                    leaderboard.remove();
                }
            }
        }
        if (this.papi) {
            try {
                this.papi_hook.unregister();
            }
            catch (final Exception ex) {}
        }
        this.saveArenas();
        if (this.db != null) {
            this.db.close();
        }
    }
    
    public void loadConfigSettings() {
        this.leaderboard_expiry = this.getConfig().getLong("leaderboard-entry-expiration") * 86400L;
        if (this.leaderboard_expiry < 0L) {
            this.leaderboard_expiry = -1L;
        }
        this.interworld = this.getConfig().getBoolean("interworld-teleportation-enabled");
        this.command_whitelist_mode = this.getConfig().getInt("command-block-mode");
        this.blocked_commands = this.getConfig().getStringList("command-block-list");
        this.pool_proximity = this.getConfig().getBoolean("pool.proximity-joining");
        this.soccer_proximity = this.getConfig().getBoolean("soccer.proximity-joining");
        this.spleef_proximity = this.getConfig().getBoolean("spleef.proximity-joining", true);
        this.using_return_loc = !this.getConfig().getBoolean("return-to-game-spawn", true);
        this.disable_holograms = this.getConfig().getBoolean("disable-holograms", false);
        this.game_alias.clear();
        this.outgoing_alias.clear();
        GameType[] values;
        for (int length = (values = GameType.values()).length, i = 0; i < length; ++i) {
            final GameType gameType = values[i];
            final String configString = this.getConfigString(GameUtils.getConfigName(gameType) + ".game-alias", "");
            if (configString != null && configString.length() > 0) {
                this.game_alias.put(configString.toLowerCase(), gameType);
                this.outgoing_alias.put(gameType, configString.toLowerCase());
            }
        }
        final String[] split = this.getConfigString("status-translate").split(",");
        for (int j = 0; j < split.length; ++j) {
            if (this.status_words.length > j && split[j].length() > 0) {
                this.status_words[j] = split[j].trim();
            }
        }
        final String upperCase = this.getConfigString("quit-item-material", "ARROW").toUpperCase();
        Material material = Material.ARROW;
        try {
            material = Material.valueOf(upperCase);
        }
        catch (final Exception ex) {
            Bukkit.getLogger().warning("Invalid material '" + upperCase + "' in lobbygames quit item!");
        }
        this.quit_item = GameUtils.createItem(material, 1, (byte)0, this.getConfigString("quit-item-title", "§c§lQuit"), new String[0]);
        this.loadRewards();
    }
    
    private void loadRewards() {
        try {
            File file = new File(this.getDataFolder().getAbsolutePath() + "/rewards.yml");
            if (!file.exists()) {
                this.saveResource("rewards.yml", false);
                file = new File(this.getDataFolder().getAbsolutePath() + "/rewards.yml");
            }
            YamlConfiguration loadConfiguration;
            try {
                loadConfiguration = YamlConfiguration.loadConfiguration(file);
            }
            catch (final Exception ex) {
                ex.printStackTrace();
                Bukkit.getLogger().severe("Could not load LobbyGames rewards.yml, incorrect YAML format.");
                return;
            }
            this.rewards.clear();
            GameType[] values;
            for (int length = (values = GameType.values()).length, i = 0; i < length; ++i) {
                final GameType key = values[i];
                final String s = (key == GameType.T048) ? "2048" : key.toString().toLowerCase();
                final ConfigurationSection configurationSection = ((FileConfiguration)loadConfiguration).getConfigurationSection(s);
                if (configurationSection != null) {
                    final ArrayList value = new ArrayList();
                    final Iterator iterator = configurationSection.getKeys(false).iterator();
                    while (iterator.hasNext()) {
                        final GameReward gameReward = new GameReward(this, key, (FileConfiguration)loadConfiguration, this.getConfig(), s + "." + (String)iterator.next());
                        if (gameReward.isEnabled()) {
                            value.add(gameReward);
                        }
                    }
                    this.rewards.put(key, value);
                }
            }
        }
        catch (final Exception ex2) {
            ex2.printStackTrace();
            Bukkit.getLogger().severe("Could not load LobbyGames rewards.yml");
        }
    }
    
    public GameReward[] getGameRewards(final GameType gameType) {
        if (this.rewards.containsKey(gameType)) {
            final List list = this.rewards.get(gameType);
            return (GameReward[])list.toArray(new GameReward[list.size()]);
        }
        return new GameReward[0];
    }
    
    public void reloadLeaderboards() {
        GameType[] values;
        for (int length = (values = GameType.values()).length, i = 0; i < length; ++i) {
            final GameType gameType = values[i];
            for (final Arena arena : this.getArenas(gameType)) {
                if (arena.getLeaderboard() != null) {
                    arena.getLeaderboard().reloadFromConfig();
                }
            }
            if (this.getGlobalLeaderboards().containsKey(gameType)) {
                final Iterator iterator2 = this.getGlobalLeaderboards().get(gameType).iterator();
                while (iterator2.hasNext()) {
                    ((Leaderboard)iterator2.next()).reloadFromConfig();
                }
            }
        }
    }
    
    public String sp(final Player player, final String s) {
        return this.papi ? PlaceholderAPI.setPlaceholders(player, s) : s;
    }
    
    public static LobbyGames getInstance() {
        return (LobbyGames)getPlugin((Class)LobbyGames.class);
    }
    
    public HashMap<UUID, Game> getActiveGames() {
        return this.active;
    }
    
    public HashMap<UUID, Long> getJoinCooldown() {
        return this.join_cooldown;
    }
    
    public HashMap<String, GameType> getGameAlias() {
        return this.game_alias;
    }
    
    public String getOutgoingGameAlias(final GameType key) {
        final String s = this.outgoing_alias.get(key);
        return (s == null) ? key.toString().toLowerCase().replaceAll("t048", "2048").replaceAll("connect4", "Connect 4").replaceAll("tictactoe", "Tic Tac Toe") : s;
    }
    
    public HashMap<UUID, Long> getProximityDelay() {
        return this.proximity_delay;
    }
    
    public List<Arena> getArenas(final GameType key) {
        return this.arenas.get(key);
    }
    
    public long getLeaderboardExpiry() {
        return this.leaderboard_expiry;
    }
    
    public boolean isPAPI() {
        return this.papi;
    }
    
    public int getCommandBlockMode() {
        return this.command_whitelist_mode;
    }
    
    public List<String> getBlockedCommands() {
        return this.blocked_commands;
    }
    
    public boolean isHologramsDisabled() {
        return this.disable_holograms;
    }
    
    public boolean poolProximityJoining() {
        return this.pool_proximity;
    }
    
    public boolean soccerProximityJoining() {
        return this.soccer_proximity;
    }
    
    public boolean spleefProximityJoining() {
        return this.spleef_proximity;
    }
    
    public String getStatusTranslate(final int a) {
        return this.status_words[Math.max(Math.min(a, 2), 0)];
    }
    
    public HashMap<UUID, GameCreateInstance> getEditingMap() {
        return this.editing;
    }
    
    public ItemStack getQuitItem() {
        return this.quit_item;
    }
    
    public String getAuthor() {
        return "ytrew".replace('y', 'C').replace('w', 'v').replace('t', '7').replace('r', 'd');
    }
    
    public boolean isPlayerInActiveGame(final UUID uuid) {
        return this.active.containsKey(uuid) && this.active.get(uuid).isActive();
    }
    
    public boolean usingReturnLoc() {
        return this.using_return_loc;
    }
    
    public Location getReturnLocation(final UUID key) {
        return this.return_loc.get(key);
    }
    
    public void setReturnLocation(final Player player) {
        final Location location = player.getLocation();
        if (!player.getWorld().getName().equals(location.getWorld().getName()) || !this.using_return_loc) {
            return;
        }
        this.return_loc.put(player.getUniqueId(), location);
    }
    
    public void removeReturnLocation(final UUID key) {
        this.return_loc.remove(key);
    }
    
    public void teleportToSpawn(final Player player, final Arena arena) {
        this.teleportToSpawn(player, arena, false);
    }
    
    public void teleportToSpawn(final Player player, final Arena arena, final boolean b) {
        final Location location = this.return_loc.get(player.getUniqueId());
        if (location != null && this.using_return_loc) {
            if (GameUtils.dist(location, player.getLocation()) > 2 || !location.getWorld().getName().equals(player.getLocation().getWorld().getName())) {
                player.teleport(location);
            }
        }
        else if (!b) {
            player.teleport(arena.getSpawn1());
        }
        this.removeReturnLocation(player.getUniqueId());
    }
    
    public void reload() {
        this.highscore_cache.clear();
        this.join_cooldown.clear();
        this.reloadConfig();
        this.using_mysql = this.getConfig().getBoolean("mysql.enabled");
        if (this.db != null) {
            this.db.close();
            this.db = null;
        }
        if (this.using_mysql) {
            this.db = new DatabaseConnection(this);
            this.testMySQL();
        }
        this.loadConfigSettings();
        if (this.papi_hook != null) {
            try {
                this.papi_hook.unregister();
            }
            catch (final Exception ex) {}
        }
        this.papi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (this.papi) {
            this.save_highscores = true;
            (this.papi_hook = new PAPIHook(this)).register();
        }
    }
    
    public boolean isArenaAvailable(final GameType key, final int n) {
        return this.isArenaAvailable(this.arenas.get(key).get(n));
    }
    
    public boolean isArenaAvailable(final Arena arena) {
        return arena.getHostingGame() == null;
    }
    
    public Arena getArena(final GameType key, final int n) {
        final List list = this.arenas.get(key);
        if (list == null) {
            return null;
        }
        if (n <= 0 || n > list.size()) {
            return null;
        }
        if (((Arena)list.get(n - 1)).getID() == n) {
            return (Arena)list.get(n - 1);
        }
        return null;
    }
    
    public HashMap<GameType, List<Leaderboard>> getGlobalLeaderboards() {
        return this.global_leaderboard;
    }
    
    public void quitPlayer(final Player player) {
        this.quitPlayer(player.getUniqueId());
    }
    
    public void quitPlayer(final UUID key) {
        final Player player = Bukkit.getPlayer(key);
        if (this.active.containsKey(key) && player != null) {
            this.active.get(key).removePlayer(player);
        }
        this.join_cooldown.remove(key);
        this.highscore_cache.remove(key);
        this.active.remove(key);
        this.proximity_delay.remove(key);
        this.return_loc.remove(key);
    }
    
    public Game getGame(final Player player) {
        return this.getGame(player.getUniqueId());
    }
    
    public Game getGame(final UUID key) {
        return this.active.get(key);
    }
    
    public Game joinPlayer(final Player player, final GameType gameType) {
        return this.joinPlayer(player, gameType, -1);
    }
    
    public boolean usingMySQL() {
        return this.using_mysql;
    }
    
    public void setUsingMySQL(final boolean using_mysql) {
        this.using_mysql = using_mysql;
        if (this.using_mysql) {
            this.db = new DatabaseConnection(this);
            this.testMySQL();
        }
    }
    
    public void testMySQL() {
        new BukkitRunnable() {
            public void run() {
                LobbyGames.this.db.testConnection();
            }
        }.runTaskAsynchronously((Plugin)this);
    }
    
    public boolean takeJoinCost(final Player player, final GameType gameType) {
        if (this.eco == null) {
            return true;
        }
        final double abs = Math.abs(this.getConfig().getDouble(GameUtils.getConfigName(gameType) + ".wager-cost", 0.0));
        if (abs > 0.0) {
            if (this.eco.getBalance((OfflinePlayer)player) < abs) {
                player.sendMessage(this.getConfigString("not-enough-balance", "§cThis game costs $" + abs + " to play!").replaceAll("\\Q%cost%\\E", "" + abs));
                return false;
            }
            this.eco.withdrawPlayer((OfflinePlayer)player, abs);
        }
        return true;
    }
    
    public Game joinPlayer(final Player player, final GameType gameType, final int n) {
        return this.joinPlayer(player, gameType, n, true);
    }
    
    public Game joinPlayer(final Player player, final GameType gameType, final int n, final boolean b) {
        if (this.getActiveGames().get(player.getUniqueId()) != null) {
            player.sendMessage(this.getConfigString(player, "error-already-in-game", "§4Error: §cYou are already in a game, use /lg quit"));
            return null;
        }
        if (n <= -1) {
            if (gameType.isMultiplayer()) {
                for (final Arena arena : this.getArenas(gameType)) {
                    if (arena.getHostingGame() != null && !arena.getHostingGame().isActive()) {
                        return this.joinPlayer(player, arena, b);
                    }
                }
            }
            for (final Arena arena2 : this.getArenas(gameType)) {
                if (this.isArenaAvailable(arena2)) {
                    return this.joinPlayer(player, arena2, b);
                }
            }
            player.sendMessage(this.getConfigString(player, "error-no-arenas-available", "§4Error: §cThere are no available arenas for this game!"));
            return null;
        }
        final Arena arena3 = this.getArena(gameType, n);
        if (arena3 == null) {
            player.sendMessage(this.getConfigString("error-arena-not-exist", "§4Error: §cThis arena does not exist!"));
            return null;
        }
        return this.joinPlayer(player, arena3, b);
    }
    
    public Game joinPlayer(final Player player, final Arena arena) {
        return this.joinPlayer(player, arena, true);
    }
    
    public Game joinPlayer(final Player player, final Arena arena, final boolean b) {
        if (arena == null) {
            player.sendMessage(this.getConfigString("error-arena-not-exist", "§4Error: §cThis arena does not exist!"));
            return null;
        }
        if (this.join_cooldown.containsKey(player.getUniqueId())) {
            final long longValue = this.join_cooldown.get(player.getUniqueId());
            if (System.currentTimeMillis() < longValue) {
                final int n = (int)((longValue - System.currentTimeMillis()) / 1000L) + 1;
                player.sendMessage(this.getConfigString(player, "cooldown-msg", "§cYou must wait " + n + " second(s) to do this!").replaceAll("\\Q%seconds%\\E", "" + n).replaceAll("\\Q(s)\\E", (n == 1) ? "" : "s"));
                return null;
            }
            this.join_cooldown.remove(player.getUniqueId());
        }
        if (arena.getGameType().isMultiplayer()) {
            if (arena.getHostingGame() != null && arena.getHostingGame().isActive()) {
                player.sendMessage(this.getConfigString(player, "error-arena-in-use", "§4Error: §cThis arena is already in use!"));
                return null;
            }
        }
        else if (!this.isArenaAvailable(arena)) {
            player.sendMessage(this.getConfigString(player, "error-arena-in-use", "§4Error: §cThis arena is already in use!"));
            return null;
        }
        if (b && !player.hasPermission("lobbygames.admin") && !this.interworld && !arena.getLocation1().getWorld().getName().equals(player.getLocation().getWorld().getName())) {
            player.sendMessage(this.getConfigString("error-wrong-world", "§4Error: §cYou cannot access this arena from this world!"));
            return null;
        }
        if (!this.takeJoinCost(player, arena.getGameType())) {
            return null;
        }
        if (arena.getHostingGame() != null) {
            arena.getHostingGame().appendPlayer(player);
        }
        else {
            switch (arena.getGameType()) {
                case SNAKE: {
                    return new Snake(this, arena, player);
                }
                case MINESWEEPER: {
                    return new Minesweeper(this, arena, player);
                }
                case SPLEEF: {
                    return new Spleef(this, arena, player, true);
                }
                case CLICKER: {
                    return new Clicker(this, arena, player);
                }
                case SOCCER: {
                    return new Soccer(this, arena, player);
                }
                case SUDOKU: {
                    return new Sudoku(this, arena, player);
                }
                case T048: {
                    return new T048(this, arena, player);
                }
                case TICTACTOE: {
                    return new TicTacToe(this, arena, player);
                }
                case POOL: {
                    return new Pool(this, arena, player);
                }
                case CONNECT4: {
                    return new Connect4(this, arena, player);
                }
                case MEMORY: {
                    return new Memory(this, arena, player);
                }
                case GOMOKU: {
                    return new Gomoku(this, arena, player);
                }
            }
        }
        return null;
    }
    
    public boolean saveArena(final Arena arena) {
        if (!arena.isValidConfiguration()) {
            return false;
        }
        if (this.arenas.get(arena.getGameType()).size() >= arena.getID()) {
            Bukkit.getConsoleSender().sendMessage("§cCould not create a new arena, invalid ID!");
            return false;
        }
        this.arenas.get(arena.getGameType()).add(arena);
        GameUtils.initArena(arena, this);
        this.saving_enabled = true;
        this.saveArenas();
        return true;
    }
    
    public boolean deleteArena(final Arena arena) {
        final List<Arena> list = this.arenas.get(arena.getGameType());
        if (list == null) {
            return false;
        }
        if (list.get(arena.getID() - 1).getID() == arena.getID()) {
            list.remove(arena.getID() - 1);
            if (arena.getGameType() != GameType.SOCCER) {
                if (arena.getGameType() == GameType.CLICKER) {
                    GameUtils.fill(arena.getLocation1().add(-1.0, -1.0, -1.0), arena.getLocation2().add(1.0, 3.0, 1.0), false, arena.getCoordinateRotation(), Material.AIR, (byte)0, true, null, (byte)0, false);
                }
                else if (arena.getGameType() == GameType.POOL) {
                    if (arena.getWidth() > arena.getHeight()) {
                        GameUtils.fill(arena.getLocation1().add(1.0, 0.0, 1.0), arena.getLocation2().add(-1.0, 2.0, -1.0), false, arena.getCoordinateRotation(), Material.AIR, (byte)0, true, null, (byte)0, false);
                    }
                    else {
                        GameUtils.fill(arena.getLocation1().add(1.0, 0.0, -1.0), arena.getLocation2().add(-1.0, 2.0, 1.0), false, arena.getCoordinateRotation(), Material.AIR, (byte)0, true, null, (byte)0, false);
                    }
                }
                else {
                    GameUtils.fill(arena, Material.AIR, (byte)0, Material.AIR, (byte)0);
                }
                arena.clearArmorStands();
            }
            for (final Arena arena2 : list) {
                if (arena2.getID() > arena.getID()) {
                    arena2.decrementID();
                }
            }
            this.saveArenas();
            if (arena.getLeaderboard() != null) {
                arena.getLeaderboard().remove();
            }
            return true;
        }
        return false;
    }
    
    private int loadArenas() {
        final File file = new File(this.getDataFolder().getAbsolutePath() + "/arenas.yml");
        try {
            if (file.createNewFile()) {
                return 0;
            }
        }
        catch (final IOException ex) {
            ex.printStackTrace();
            Bukkit.getLogger().warning("Failed to save a new arenas.yml file!");
            return 0;
        }
        GameType[] values;
        for (int length = (values = GameType.values()).length, i = 0; i < length; ++i) {
            this.arenas.put(values[i], new ArrayList<Arena>());
        }
        this.global_leaderboard.clear();
        this.active.clear();
        int n = 0;
        boolean b = true;
        try {
            final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file);
            GameType[] values2;
            for (int length2 = (values2 = GameType.values()).length, j = 0; j < length2; ++j) {
                final GameType gameType = values2[j];
                final ArrayList value = new ArrayList();
                final String lowerCase = gameType.toString().toLowerCase();
                for (int n2 = 1; ((FileConfiguration)loadConfiguration).get(lowerCase + ".arena-" + n2) != null; ++n2) {
                    final Location deserializeLocation = GameUtils.deserializeLocation((FileConfiguration)loadConfiguration, lowerCase + ".arena-" + n2 + ".loc1");
                    final Location deserializeLocation2 = GameUtils.deserializeLocation((FileConfiguration)loadConfiguration, lowerCase + ".arena-" + n2 + ".loc2");
                    final Location deserializeLocation3 = GameUtils.deserializeLocation((FileConfiguration)loadConfiguration, lowerCase + ".arena-" + n2 + ".spawn1");
                    final Location deserializeLocation4 = GameUtils.deserializeLocation((FileConfiguration)loadConfiguration, lowerCase + ".arena-" + n2 + ".leaderboard.location");
                    final Location deserializeLocation5 = GameUtils.deserializeLocation((FileConfiguration)loadConfiguration, lowerCase + ".arena-" + n2 + ".special1");
                    final Location deserializeLocation6 = GameUtils.deserializeLocation((FileConfiguration)loadConfiguration, lowerCase + ".arena-" + n2 + ".special2");
                    if (deserializeLocation == null || deserializeLocation.getWorld() == null || deserializeLocation2 == null || deserializeLocation2.getWorld() == null) {
                        Bukkit.getLogger().warning("Could not load arena in '" + ((FileConfiguration)loadConfiguration).getString(lowerCase + ".arena-" + n2 + ".loc1.world") + "' because world does not exist!");
                    }
                    else {
                        Leaderboard leaderboard = null;
                        if (deserializeLocation4 != null && deserializeLocation4.getWorld() != null) {
                            leaderboard = new Leaderboard(this, gameType, deserializeLocation4);
                            final ArrayList entries = new ArrayList();
                            for (int n3 = 1; ((FileConfiguration)loadConfiguration).get(lowerCase + ".arena-" + n2 + ".leaderboard.entry-" + n3) != null; ++n3) {
                                final LeaderboardEntry deserializeEntry = GameUtils.deserializeEntry((FileConfiguration)loadConfiguration, lowerCase + ".arena-" + n2 + ".leaderboard.entry-" + n3, this.leaderboard_expiry);
                                if (!deserializeEntry.isExpired()) {
                                    entries.add(deserializeEntry);
                                }
                            }
                            leaderboard.setEntries(entries);
                        }
                        value.add(new Arena(n2, gameType, deserializeLocation, deserializeLocation2, deserializeLocation3, leaderboard, deserializeLocation5, deserializeLocation6));
                        ++n;
                    }
                }
                this.arenas.put(gameType, value);
                if (((FileConfiguration)loadConfiguration).get(lowerCase + ".leaderboard") != null) {
                    final ArrayList list = new ArrayList();
                    final ArrayList<Leaderboard> value2 = new ArrayList<Leaderboard>();
                    for (int n4 = 1; ((FileConfiguration)loadConfiguration).get(lowerCase + ".leaderboard.locations.loc-" + n4) != null; ++n4) {
                        final Location deserializeLocation7 = GameUtils.deserializeLocation((FileConfiguration)loadConfiguration, lowerCase + ".leaderboard.locations.loc-" + n4);
                        if (deserializeLocation7 != null && deserializeLocation7.getWorld() != null) {
                            value2.add(new Leaderboard(this, gameType, deserializeLocation7));
                        }
                        else {
                            Bukkit.getLogger().warning("Could not load a leaderboard because the world no longer exists!");
                        }
                    }
                    final ArrayList<LeaderboardEntry> entryList = new ArrayList<LeaderboardEntry>();
                    for (int n5 = 1; ((FileConfiguration)loadConfiguration).get(lowerCase + ".leaderboard.entries.entry-" + n5) != null; ++n5) {
                        final LeaderboardEntry deserializeEntry2 = GameUtils.deserializeEntry((FileConfiguration)loadConfiguration, lowerCase + ".leaderboard.entries.entry-" + n5, this.leaderboard_expiry);
                        if (!deserializeEntry2.isExpired()) {
                            entryList.add(deserializeEntry2);
                        }
                    }
                    for (final Leaderboard leaderboard2 : value2) {
                        final ArrayList<LeaderboardEntry> entries2 = new ArrayList<LeaderboardEntry>();
                        for (int k = 0; k < entryList.size(); ++k) {
                            entries2.add(entryList.get(k).copy());
                        }
                        leaderboard2.setEntries(entries2);
                    }
                    this.global_leaderboard.put(gameType, value2);
                }
            }
            if (n > 0) {
                this.saving_enabled = true;
            }
        }
        catch (final Exception ex2) {
            ex2.printStackTrace();
            b = false;
        }
        if (n == 0 && b) {
            Bukkit.getLogger().warning("[LobbyGames] If you are using the leaked version, your server may not be secure. You may ignore this warning if you downloaded from spigot.");
        }
        return n;
    }
    
    public void saveArenas() {
        if (!this.saving_enabled) {
            return;
        }
        final File file = new File(this.getDataFolder().getAbsolutePath() + "/arenas.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (final IOException ex) {
                ex.printStackTrace();
                Bukkit.getLogger().severe("Could not save arenas.yml! LobbyGames will lose arena data.");
                return;
            }
        }
        final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<GameType, List<Arena>> entry : this.arenas.entrySet()) {
            final String lowerCase = entry.getKey().toString().toLowerCase();
            int n = 1;
            ((FileConfiguration)loadConfiguration).set(lowerCase, (Object)null);
            for (Arena arena : entry.getValue()) {
                ((FileConfiguration)loadConfiguration).set(lowerCase + ".arena-" + n + ".loc1", (Object)arena.getLocation1().serialize());
                ((FileConfiguration)loadConfiguration).set(lowerCase + ".arena-" + n + ".loc2", (Object)arena.getLocation2().serialize());
                if (arena.getSpawn1() != null) {
                    ((FileConfiguration)loadConfiguration).set(lowerCase + ".arena-" + n + ".spawn1", (Object)arena.getSpawn1().serialize());
                }
                if (arena.getSpecialLoc1() != null) {
                    ((FileConfiguration)loadConfiguration).set(lowerCase + ".arena-" + n + ".special1", (Object)arena.getSpecialLoc1().serialize());
                }
                if (arena.getSpecialLoc2() != null) {
                    ((FileConfiguration)loadConfiguration).set(lowerCase + ".arena-" + n + ".special2", (Object)arena.getSpecialLoc2().serialize());
                }
                if (arena.getLeaderboardLocation() != null) {
                    ((FileConfiguration)loadConfiguration).set(lowerCase + ".arena-" + n + ".leaderboard.location", (Object)arena.getLeaderboardLocation().serialize());
                    final String s = lowerCase + ".arena-" + n + ".leaderboard";
                    int n2 = arena.getLeaderboard().getEntries().size();
                    if (!((GameType)entry.getKey()).isMultiplayer()) {
                        n2 = Math.min(arena.getLeaderboard().getEntries().size(), arena.getLeaderboard().getSize());
                    }
                    for (int i = 0; i < n2; ++i) {
                        ((FileConfiguration)loadConfiguration).set(s + ".entry-" + (i + 1), (Object)arena.getLeaderboard().getEntries().get(i).serialize());
                    }
                }
                ++n;
            }
        }
        for (Map.Entry entry2 : this.global_leaderboard.entrySet()) {
            final String lowerCase2 = ((GameType)entry2.getKey()).toString().toLowerCase();
            ((FileConfiguration)loadConfiguration).set(lowerCase2 + ".leaderboard", (Object)null);
            if (((List)entry2.getValue()).size() > 0) {
                int n3 = 1;
                final Iterator iterator4 = ((List)entry2.getValue()).iterator();
                while (iterator4.hasNext()) {
                    ((FileConfiguration)loadConfiguration).set(lowerCase2 + ".leaderboard.locations.loc-" + n3, (Object)((Leaderboard)iterator4.next()).getLocation().serialize());
                    ++n3;
                }
                final List<LeaderboardEntry> entries = ((List<Leaderboard>)entry2.getValue()).get(0).getEntries();
                int n4 = 1;
                final Iterator<LeaderboardEntry> iterator5 = entries.iterator();
                while (iterator5.hasNext()) {
                    ((FileConfiguration)loadConfiguration).set(lowerCase2 + ".leaderboard.entries.entry-" + n4, (Object)iterator5.next().serialize());
                    ++n4;
                }
            }
        }
        try {
            ((FileConfiguration)loadConfiguration).save(file);
        }
        catch (final IOException ex2) {
            ex2.printStackTrace();
            Bukkit.getLogger().severe("Could not save arenas.yml! LobbyGames will lose arena data.");
        }
    }
    
    public void setHighScore(final Player player, final GameType gameType, final PlayerStats playerStats) {
        this.setHighScore(player, gameType, playerStats.getScore(), playerStats.getDisplayScore(), playerStats.getSecondsPlayed(), true);
    }
    
    public void setHighScore(final Player player, final GameType gameType, final PlayerStats playerStats, final boolean b) {
        this.setHighScore(player, gameType, playerStats.getScore(), playerStats.getDisplayScore(), playerStats.getSecondsPlayed(), b);
    }
    
    public void setHighScore(final Player player, final GameType gameType, final int n, final String s, final int n2) {
        this.setHighScore(player, gameType, n, s, n2, true);
    }
    
    public void setHighScore(final Player player, final GameType gameType, final int n, final String s, final int n2, final boolean b) {
        if (!this.save_highscores) {
            return;
        }
        final String s2 = (s == null) ? ("" + n) : s;
        final HashMap<GameType, PlayerStats> hashMap = this.highscore_cache.get(player.getUniqueId());
        final Consumer<HashMap<GameType, PlayerStats>> consumer = hashMap2 -> {
            if (hashMap2 == null) {
                return;
            }
            else {
                PlayerStats value = hashMap2.get(gameType);
                if (value == null) {
                    value = new PlayerStats(Integer.MIN_VALUE, "0", 1, n2);
                }
                else {
                    value.setGamesPlayed(value.getGamesPlayed() + 1);
                    value.setSecondsPlayed(value.getSecondsPlayed() + n2);
                }
                if (n > value.getScore()) {
                    value.setScore(n);
                    value.setDisplayScore(s2);
                }
                HashMap<GameType, PlayerStats> value2 = hashMap2;
                if (value2 == null) {
                    value2 = new HashMap<GameType, PlayerStats>();
                }
                value2.put(gameType, value);
                this.highscore_cache.put(player.getUniqueId(), value2);
                if (this.using_mysql) {
                    final String s3 = ("" + value.getScore()).equals(value.getDisplayScore()) ? null : value.getDisplayScore();
                    final int[] array = { value.getScore(), value.getSecondsPlayed(), value.getGamesPlayed() };
                    final String s4 = "INSERT INTO lobbygames_stats (uuid, game, display_score, score, seconds_played, games_played) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE display_score = ?, score = ?, seconds_played = ?, games_played = ?";
                    this.db.execute(s4, new String[] { player.getUniqueId().toString(), gameType.toString(), s3 }, array, b);
                }
                else {
                    final Player finalPlayer = player;
                    final GameType finalGameType = gameType;
                    final PlayerStats finalValue = value;
                    new BukkitRunnable() {
                        private final /* synthetic */ Player val$p = finalPlayer;
                        private final /* synthetic */ GameType val$gt = finalGameType;
                        private final /* synthetic */ PlayerStats val$fstats = finalValue;
                        
                        public void run() {
                            final File file = new File(LobbyGames.this.getDataFolder().getAbsolutePath() + "/player_stats.yml");
                            if (!file.exists()) {
                                try {
                                    file.createNewFile();
                                }
                                catch (final IOException ex) {
                                    ex.printStackTrace();
                                    Bukkit.getLogger().severe("Could not save player_stats.yml!");
                                    return;
                                }
                            }
                            final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file);
                            ((FileConfiguration)loadConfiguration).set(this.val$p.getUniqueId().toString() + "." + this.val$gt.toString().toLowerCase(), (Object)this.val$fstats.serialize());
                            try {
                                ((FileConfiguration)loadConfiguration).save(file);
                            }
                            catch (final IOException ex2) {
                                ex2.printStackTrace();
                                Bukkit.getLogger().severe("Could not save player_stats.yml!");
                            }
                        }
                    }.runTaskAsynchronously((Plugin)this);
                }
                return;
            }
        };
        if (hashMap != null) {
            consumer.accept(hashMap);
        }
        else {
            this.getHighScoreMap(player.getUniqueId(), consumer);
        }
    }
    
    public HashMap<GameType, PlayerStats> getHighScoreMap(final UUID uuid) {
        return this.getHighScoreMap(uuid, null);
    }
    
    public HashMap<GameType, PlayerStats> getHighScoreMap(final UUID key, final Consumer<HashMap<GameType, PlayerStats>> consumer) {
        if (!this.save_highscores) {
            if (consumer != null) {
                consumer.accept(null);
            }
            return null;
        }
        final HashMap hashMap = this.highscore_cache.get(key);
        if (hashMap != null) {
            if (consumer != null) {
                consumer.accept(hashMap);
            }
            return hashMap;
        }
        if (this.using_mysql) {
            final String[] array = { key.toString() };
            final String s = "SELECT * FROM lobbygames_stats WHERE uuid = ?";
            if (consumer == null) {
                return this.handleDatabaseHighScoreMapResults(key, this.db.query(s, array, null, false, null), null);
            }
            this.db.query(s, array, null, false, set -> this.handleDatabaseHighScoreMapResults(key, set, consumer));
        }
        else {
            if (consumer == null) {
                return this.getDiskHighScoreMapResults(key);
            }
            new BukkitRunnable() {
                public void run() {
                    consumer.accept(LobbyGames.this.getDiskHighScoreMapResults(key));
                }
            }.runTaskAsynchronously((Plugin)this);
        }
        return null;
    }
    
    private HashMap<GameType, PlayerStats> handleDatabaseHighScoreMapResults(final UUID key, final ResultSet set, final Consumer<HashMap<GameType, PlayerStats>> consumer) {
        if (set == null) {
            if (consumer != null) {
                consumer.accept(null);
            }
            return null;
        }
        try {
            final HashMap value = new HashMap();
            while (set.next()) {
                final int int1 = set.getInt("highscore");
                final GameType value2 = GameType.valueOf(set.getString("game_type"));
                String string = set.getString("display_score");
                if (string == null) {
                    string = "" + int1;
                }
                value.put(value2, new PlayerStats(int1, string, set.getInt("games_played"), set.getInt("time_played")));
            }
            set.close();
            this.highscore_cache.put(key, value);
            if (consumer != null) {
                consumer.accept(value);
            }
            return value;
        }
        catch (final Exception ex) {
            ex.printStackTrace();
            try {
                set.close();
            }
            catch (final SQLException ex2) {
                ex2.printStackTrace();
            }
            if (consumer != null) {
                consumer.accept(null);
            }
            return null;
        }
    }
    
    private HashMap<GameType, PlayerStats> getDiskHighScoreMapResults(final UUID key) {
        final File file = new File(this.getDataFolder().getAbsolutePath() + "/player_stats.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (final IOException ex) {
                ex.printStackTrace();
                Bukkit.getLogger().severe("Could not save player_stats.yml!");
                return null;
            }
        }
        final HashMap<GameType, PlayerStats> deserialize = PlayerStats.deserialize((FileConfiguration)YamlConfiguration.loadConfiguration(file), key);
        this.highscore_cache.put(key, deserialize);
        return deserialize;
    }
    
    public PlayerStats getStats(final UUID uuid, final GameType gameType, final Consumer<PlayerStats> consumer) {
        if (consumer == null) {
            final HashMap<GameType, PlayerStats> highScoreMap = this.getHighScoreMap(uuid, null);
            if (highScoreMap != null && highScoreMap.containsKey(gameType)) {
                return (PlayerStats)highScoreMap.get(gameType);
            }
        }
        else {
            this.getHighScoreMap(uuid, hashMap -> {
                if (hashMap != null && hashMap.containsKey(gameType)) {
                    consumer.accept(hashMap.get(gameType));
                }
                else {
                    consumer.accept(null);
                }
                return;
            });
        }
        return null;
    }
    
    public String getHighScore(final UUID uuid, final GameType gameType) {
        if (!this.save_highscores) {
            return "0";
        }
        final PlayerStats stats = this.getStats(uuid, gameType, null);
        if (stats == null) {
            return "-";
        }
        return stats.getDisplayScore();
    }
    
    public void getHighScoreAsync(final UUID uuid, final GameType gameType, final Consumer<String> consumer) {
        if (!this.save_highscores) {
            consumer.accept("0");
        }
        else {
            this.getStats(uuid, gameType, playerStats -> {
                if (playerStats == null) {
                    consumer.accept("-");
                }
                else {
                    consumer.accept(playerStats.getDisplayScore());
                }
            });
        }
    }
    
    public int getHighScoreRaw(final UUID uuid, final GameType gameType) {
        if (!this.save_highscores) {
            return Integer.MIN_VALUE;
        }
        final PlayerStats stats = this.getStats(uuid, gameType, null);
        if (stats == null) {
            return Integer.MIN_VALUE;
        }
        return stats.getScore();
    }
    
    public void getHighScoreRawAsync(final UUID uuid, final GameType gameType, final Consumer<Integer> consumer) {
        if (!this.save_highscores) {
            consumer.accept(Integer.MIN_VALUE);
        }
        else {
            this.getStats(uuid, gameType, playerStats -> {
                if (playerStats == null) {
                    consumer.accept(Integer.MIN_VALUE);
                }
                else {
                    consumer.accept(playerStats.getScore());
                }
            });
        }
    }
    
    public int getSecondsPlayed(final UUID uuid, final GameType gameType) {
        if (!this.save_highscores) {
            return 0;
        }
        final PlayerStats stats = this.getStats(uuid, gameType, null);
        if (stats == null) {
            return 0;
        }
        return stats.getSecondsPlayed();
    }
    
    public void getSecondsPlayedAsync(final UUID uuid, final GameType gameType, final Consumer<Integer> consumer) {
        if (!this.save_highscores) {
            consumer.accept(0);
        }
        else {
            this.getStats(uuid, gameType, playerStats -> {
                if (playerStats == null) {
                    consumer.accept(0);
                }
                else {
                    consumer.accept(playerStats.getSecondsPlayed());
                }
            });
        }
    }
    
    public int getTimesWon(final UUID uuid, final GameType gameType) {
        if (!this.save_highscores) {
            return 0;
        }
        final PlayerStats stats = this.getStats(uuid, gameType, null);
        if (stats == null) {
            return 0;
        }
        return stats.getGamesPlayed();
    }
    
    public void getTimesWonAsync(final UUID uuid, final GameType gameType, final Consumer<Integer> consumer) {
        if (!this.save_highscores) {
            consumer.accept(0);
        }
        else {
            this.getStats(uuid, gameType, playerStats -> {
                if (playerStats == null) {
                    consumer.accept(0);
                }
                else {
                    consumer.accept(playerStats.getGamesPlayed());
                }
            });
        }
    }
    
    public void sendActionbar(final Player player, String sp, final boolean b) {
        if (player == null || sp.length() == 0) {
            return;
        }
        sp = this.sp(player, sp);
        if (LobbyGames.SERVER_VERSION < 12) {
            if (!b) {
                player.sendMessage(sp);
            }
        }
        else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent)new TextComponent(sp));
        }
    }
    
    public String getConfigString(final Player player, final String s, final String s2) {
        final String configString = this.getConfigString(s);
        return this.sp(player, (configString == null) ? s2.replaceAll("&", "§").replaceAll("\\Q[newline]\\E", "\n") : configString);
    }
    
    public String getConfigString(final String s, final String s2) {
        final String configString = this.getConfigString(s);
        return (configString == null) ? s2.replaceAll("&", "§").replaceAll("\\Q[newline]\\E", "\n") : configString;
    }
    
    /**
     * 自动加载中文配置文件
     * 直接使用 config_zh_CN.yml 作为主配置文件
     */
    private void loadChineseConfig() {
        File dataFolder = this.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File configFile = new File(dataFolder, "config.yml");
        
        // 直接从资源中保存中文配置为 config.yml
        if (!configFile.exists()) {
            this.saveResource("config_zh_CN.yml", false);
            File chineseConfigFile = new File(dataFolder, "config_zh_CN.yml");
            
            try {
                YamlConfiguration chineseConfig = YamlConfiguration.loadConfiguration(chineseConfigFile);
                chineseConfig.save(configFile);
                Bukkit.getLogger().info("[\u6ce1\u83dc\u6e38\u620f] \u5df2\u751f\u6210\u4e2d\u6587\u914d\u7f6e\u6587\u4ef6 config.yml");
                
                // 删除临时的 config_zh_CN.yml
                chineseConfigFile.delete();
            } catch (Exception e) {
                Bukkit.getLogger().warning("[\u6ce1\u83dc\u6e38\u620f] \u65e0\u6cd5\u751f\u6210\u914d\u7f6e\u6587\u4ef6: " + e.getMessage());
                this.saveDefaultConfig();
            }
        }
        
        // 重新加载配置
        this.reloadConfig();
    }
    
    public String getConfigString(final String s) {
        final String string = this.getConfig().getString(s);
        if (string != null) {
            return string.replace('&', '§').replaceAll("\\Q[newline]\\E", "\n").replaceAll("\\n", "\n");
        }
        if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("no")) {
            return this.getConfigString(s.toLowerCase() + "-text");
        }
        Bukkit.getLogger().warning("Could not get value from config: '" + s);
        return "§c[Missing value: " + s;
    }
}
