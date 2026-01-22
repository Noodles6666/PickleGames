// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashMap;
import java.util.Map;

public class PlayerStats
{
    private String display_score;
    private int score;
    private int games_played;
    private int seconds_played;
    
    public PlayerStats(final int score, final String display_score, final int games_played, final int seconds_played) {
        this.display_score = "";
        this.score = Integer.MIN_VALUE;
        this.display_score = display_score;
        this.score = score;
        this.games_played = games_played;
        this.seconds_played = seconds_played;
    }
    
    public int getGamesPlayed() {
        return this.games_played;
    }
    
    public int getScore() {
        return this.score;
    }
    
    public int getSecondsPlayed() {
        return this.seconds_played;
    }
    
    public String secondsPlayed() {
        return GameUtils.timeStr(this.seconds_played);
    }
    
    public String getDisplayScore() {
        return this.display_score;
    }
    
    public void setGamesPlayed(final int games_played) {
        this.games_played = games_played;
    }
    
    public void setScore(final int score) {
        this.score = score;
    }
    
    public void setSecondsPlayed(final int seconds_played) {
        this.seconds_played = seconds_played;
    }
    
    public void setDisplayScore(final String display_score) {
        this.display_score = display_score;
    }
    
    public Map<String, Object> serialize() {
        final HashMap hashMap = new HashMap();
        hashMap.put("score", this.score);
        hashMap.put("display-score", this.display_score);
        hashMap.put("games-played", this.games_played);
        hashMap.put("time-played", this.seconds_played);
        return hashMap;
    }
    
    public static HashMap<GameType, PlayerStats> deserialize(final FileConfiguration fileConfiguration, final UUID uuid) {
        final HashMap hashMap = new HashMap();
        GameType[] values;
        for (int length = (values = GameType.values()).length, i = 0; i < length; ++i) {
            final GameType key = values[i];
            final String s = uuid.toString() + "." + key.toString().toLowerCase();
            if (fileConfiguration.get(s) != null) {
                hashMap.put(key, new PlayerStats(fileConfiguration.getInt(s + ".score"), fileConfiguration.getString(s + ".display-score"), fileConfiguration.getInt(s + ".games-played"), fileConfiguration.getInt(s + ".time-played")));
            }
        }
        return hashMap;
    }
}
