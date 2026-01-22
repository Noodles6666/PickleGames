// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import java.util.Iterator;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.UUID;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.sql.DriverManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import me.c7dev.lobbygames.LobbyGames;
import java.sql.Connection;

public class DatabaseConnection
{
    public static final String INIT_SQL = "CREATE TABLE IF NOT EXISTS lobbygames_stats (uuid VARCHAR(63) NOT NULL,game_type ENUM(\"SNAKE\", \"MINESWEEPER\", \"SPLEEF\", \"CLICKER\", \"SOCCER\", \"SUDOKU\", \"T048\", \"TICTACTOE\", \"POOL\", \"CONNECT4\") NOT NULL,highscore INT,time_played INT DEFAULT 0 NOT NULL,games_played INT DEFAULT 0 NOT NULL,display_score VARCHAR(63),PRIMARY KEY (uuid, game_type),INDEX idx_uuid (uuid));";
    private Connection con;
    private String url;
    private String username;
    private String pw;
    boolean has_init;
    private long last_use;
    private LobbyGames plugin;
    
    public DatabaseConnection(final LobbyGames plugin) {
        this.has_init = false;
        this.last_use = System.currentTimeMillis();
        if (!plugin.usingMySQL()) {
            return;
        }
        this.plugin = plugin;
        final ArrayList elements = new ArrayList();
        if (System.getenv("LOBBYGAMES_MYSQL_HOST") == null) {
            this.url = plugin.getConfig().getString("mysql.host");
        }
        else {
            this.url = System.getenv("LOBBYGAMES_MYSQL_HOST");
            elements.add("host");
        }
        if (System.getenv("LOBBYGAMES_MYSQL_USERNAME") == null) {
            this.username = plugin.getConfig().getString("mysql.username");
        }
        else {
            this.username = System.getenv("LOBBYGAMES_MYSQL_USERNAME");
            elements.add("username");
        }
        if (System.getenv("LOBBYGAMES_MYSQL_PASSWORD") == null) {
            this.pw = plugin.getConfig().getString("mysql.password");
        }
        else {
            this.pw = System.getenv("LOBBYGAMES_MYSQL_PASSWORD");
            elements.add("password");
        }
        String s;
        if (System.getenv("LOBBYGAMES_MYSQL_NAME") == null) {
            s = plugin.getConfig().getString("mysql.name");
        }
        else {
            s = System.getenv("LOBBYGAMES_MYSQL_NAME");
            elements.add("name");
        }
        if (this.url == null || this.username == null || this.pw == null) {
            Bukkit.getLogger().warning("Config values for mysql.url, mysql.username, or mysql.password is null, skipping database hook");
            return;
        }
        if (elements.size() > 0) {
            Bukkit.getLogger().info("Configured LobbyGames MySQL " + String.join(", ", elements) + " from environment instead of config");
        }
        this.url = "jdbc:mysql://" + this.url.replaceFirst("^https?:\\/+", "").split("\\/")[0] + "/" + s + "?characterEncoding=latin1&autoReconnect=true";
        
        new GameTask() {
            @Override
            public void run() {
                try {
                    if (DatabaseConnection.this.con == null) {
                        return;
                    }
                    if (System.currentTimeMillis() - DatabaseConnection.this.last_use >= 10000L && !DatabaseConnection.this.con.isClosed()) {
                        DatabaseConnection.this.close();
                    }
                }
                catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 1L, 20L);
    }
    
    private DatabaseConnection connect() {
        this.last_use = System.currentTimeMillis();
        try {
            if (this.con != null && !this.con.isClosed()) {
                return this;
            }
            this.con = DriverManager.getConnection(this.url, this.username, this.pw);
        }
        catch (final Exception ex) {
            ex.printStackTrace();
            Bukkit.getLogger().severe("LobbyGames could not connect to MySQL! Using player_stats.yml for future updates.");
            this.plugin.setUsingMySQL(false);
            return this;
        }
        if (!this.has_init) {
            this.has_init = true;
            this.execute("CREATE TABLE IF NOT EXISTS lobbygames_stats (uuid VARCHAR(63) NOT NULL,game_type ENUM(\"SNAKE\", \"MINESWEEPER\", \"SPLEEF\", \"CLICKER\", \"SOCCER\", \"SUDOKU\", \"T048\", \"TICTACTOE\", \"POOL\", \"CONNECT4\") NOT NULL,highscore INT,time_played INT DEFAULT 0 NOT NULL,games_played INT DEFAULT 0 NOT NULL,display_score VARCHAR(63),PRIMARY KEY (uuid, game_type),INDEX idx_uuid (uuid));", null, null, false);
            this.importPlayerStats();
        }
        return this;
    }
    
    public void testConnection() {
        this.connect();
        this.close();
    }
    
    public void query(final String s, final String[] array, final Consumer<ResultSet> consumer) {
        this.query(s, null, null, true, consumer);
    }
    
    public void query(final String s, final Consumer<ResultSet> consumer) {
        this.query(s, null, null, true, consumer);
    }
    
    public void query(final String s, final String[] array, final int[] array2, final Consumer<ResultSet> consumer) {
        this.query(s, array, array2, true, consumer);
    }
    
    public ResultSet query(final String s, final String[] array, final int[] array2, final boolean b, final Consumer<ResultSet> consumer) {
        this.connect();
        if (this.con == null) {
            if (consumer != null) {
                consumer.accept(null);
            }
            return null;
        }
        this.last_use = System.currentTimeMillis();
        if (consumer == null) {
            final ResultSet executeQuery = this.executeQuery(s, array, array2, null);
            if (b) {
                this.close();
            }
            return executeQuery;
        }
        
        SchedulerUtil.runTaskAsynchronously(this.plugin, () -> {
            DatabaseConnection.this.executeQuery(s, array, array2, consumer);
            if (b) {
                DatabaseConnection.this.close();
            }
        });
        return null;
    }
    
    private ResultSet executeQuery(final String s, final String[] array, final int[] array2, final Consumer<ResultSet> consumer) {
        ResultSet executeQuery = null;
        try {
            final PreparedStatement prepareStatement = this.con.prepareStatement(s);
            this.setArguments(prepareStatement, array, array2);
            executeQuery = prepareStatement.executeQuery();
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }
        if (consumer != null) {
            consumer.accept(executeQuery);
        }
        return executeQuery;
    }
    
    public void execute(final String s, final String[] array, final int[] array2) {
        this.execute(s, array, array2, true);
    }
    
    public void execute(final String s, final String[] array, final int[] array2, final boolean b) {
        SchedulerUtil.runTaskAsynchronously(this.plugin, () -> {
            DatabaseConnection.this.executeSync(s, array, array2, b);
        });
    }
    
    private void executeSync(final String s, final String[] array, final int[] array2, final boolean b) {
        this.connect();
        if (this.con == null) {
            return;
        }
        this.last_use = System.currentTimeMillis();
        try {
            final PreparedStatement prepareStatement = this.con.prepareStatement(s);
            this.setArguments(prepareStatement, array, array2);
            prepareStatement.execute();
            if (b) {
                this.close();
            }
        }
        catch (final Exception ex) {
            ex.printStackTrace();
            Bukkit.getLogger().severe("LobbyGames could not perform a write to MySQL! Data may be out of sync.");
        }
    }
    
    public void setArguments(final PreparedStatement preparedStatement, final String[] array, final int[] array2) throws SQLException {
        int i = 0;
        if (array != null) {
            while (i < array.length) {
                preparedStatement.setString(i + 1, array[i]);
                ++i;
            }
        }
        if (array2 != null) {
            for (int j = 0; j < array2.length; ++j) {
                if (array2[j] == Integer.MIN_VALUE) {
                    preparedStatement.setNull(i + j + 1, 4);
                }
                else {
                    preparedStatement.setInt(i + j + 1, array2[j]);
                }
            }
        }
    }
    
    public void importPlayerStats() {
        SchedulerUtil.runTaskAsynchronously(this.plugin, () -> {
            try {
                final File file = new File(DatabaseConnection.this.plugin.getDataFolder().getAbsolutePath() + "/player_stats.yml");
                if (!file.exists()) {
                    return;
                }
                final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file);
                if (((FileConfiguration)loadConfiguration).getBoolean("exported_db")) {
                    return;
                }
                String s = "INSERT INTO lobbygames_stats (uuid, game_type, highscore, time_played, games_played, display_score) VALUES ";
                final HashMap<UUID, HashMap<GameType, PlayerStats>> hashMap = new HashMap<UUID, HashMap<GameType, PlayerStats>>();
                final Iterator<String> iterator = ((FileConfiguration)loadConfiguration).getKeys(false).iterator();
                while (iterator.hasNext()) {
                    final UUID fromString = UUID.fromString(iterator.next());
                    final HashMap<GameType, PlayerStats> deserialize = PlayerStats.deserialize((FileConfiguration)loadConfiguration, fromString);
                    hashMap.put(fromString, deserialize);
                    for (int i = 0; i < deserialize.size(); ++i) {
                        s += "(?, ?, ?, ?, ?, ?), ";
                    }
                }
                final String s2 = s.replaceAll(", $", "") + " ON DUPLICATE KEY UPDATE uuid=uuid";
                DatabaseConnection.this.connect();
                if (DatabaseConnection.this.con == null) {
                    return;
                }
                final PreparedStatement prepareStatement = DatabaseConnection.this.con.prepareStatement(s2);
                int n = 1;
                for (Map.Entry<UUID, HashMap<GameType, PlayerStats>> entry : hashMap.entrySet()) {
                    final UUID uuid = entry.getKey();
                    for (Map.Entry<GameType, PlayerStats> entry2 : entry.getValue().entrySet()) {
                        final GameType gameType = entry2.getKey();
                        final PlayerStats playerStats = entry2.getValue();
                        final String s3 = ("" + playerStats.getScore()).equals(playerStats.getDisplayScore()) ? null : playerStats.getDisplayScore();
                        prepareStatement.setString(n, uuid.toString());
                        prepareStatement.setString(n + 1, gameType.toString());
                        if (playerStats.getScore() == Integer.MIN_VALUE) {
                            prepareStatement.setNull(n + 2, 4);
                        }
                        else {
                            prepareStatement.setInt(n + 2, playerStats.getScore());
                        }
                        prepareStatement.setInt(n + 3, playerStats.getSecondsPlayed());
                        prepareStatement.setInt(n + 4, playerStats.getGamesPlayed());
                        prepareStatement.setString(n + 5, s3);
                        n += 6;
                    }
                }
                prepareStatement.execute();
                Bukkit.getLogger().info("Imported stats for " + hashMap.size() + " player(s) into MySQL!");
                ((FileConfiguration)loadConfiguration).set("exported_db", (Object)true);
                ((FileConfiguration)loadConfiguration).save(file);
            }
            catch (final Exception ex) {
                ex.printStackTrace();
                Bukkit.getLogger().severe("Could not load player_stats.yml into MySQL!");
            }
        });
    }
    
    public void close() {
        if (this.con == null) {
            return;
        }
        try {
            this.con.close();
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }
        this.con = null;
    }
}
