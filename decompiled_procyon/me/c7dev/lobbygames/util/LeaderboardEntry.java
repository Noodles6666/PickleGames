// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeaderboardEntry
{
    private int score;
    private long expire;
    private String displayscore;
    private String displayname;
    private UUID uuid;
    
    public LeaderboardEntry(final UUID uuid, final String displayname, final int score, final String s, final long n) {
        this.uuid = uuid;
        this.score = score;
        this.expire = ((n > 0L) ? (System.currentTimeMillis() + n * 1000L) : -1L);
        this.displayscore = ((s == null) ? ("" + score) : s);
        this.displayname = displayname;
    }
    
    public LeaderboardEntry copy() {
        final LeaderboardEntry leaderboardEntry = new LeaderboardEntry(this.uuid, this.displayname, this.score, this.displayscore, -1L);
        leaderboardEntry.setRawExpiration(this.expire);
        return leaderboardEntry;
    }
    
    public UUID getUniqueId() {
        return this.uuid;
    }
    
    public int getScore() {
        return this.score;
    }
    
    public long getExpiration() {
        return this.expire;
    }
    
    public void setRawExpiration(final long expire) {
        this.expire = expire;
    }
    
    public void setExpiration(final long n) {
        this.expire = ((n > 0L) ? (System.currentTimeMillis() + n * 1000L) : -1L);
    }
    
    public boolean isExpired() {
        return this.expire > 0L && System.currentTimeMillis() > this.expire;
    }
    
    public String getDisplayScore() {
        return this.displayscore;
    }
    
    public String getDisplayName() {
        return this.displayname;
    }
    
    public void setDisplayScore(final String displayscore) {
        this.displayscore = displayscore;
    }
    
    public void setDisplayName(final String displayname) {
        this.displayname = displayname;
    }
    
    public void setScore(final int score) {
        this.score = score;
    }
    
    public Map<String, Object> serialize() {
        final HashMap hashMap = new HashMap();
        if (this.uuid != null) {
            hashMap.put("uuid", this.uuid.toString());
        }
        hashMap.put("display_name", this.displayname);
        hashMap.put("score", this.score);
        hashMap.put("expires", this.expire);
        if (this.displayscore != null && !this.displayscore.equals("" + this.score)) {
            hashMap.put("display_score", this.displayscore);
        }
        return hashMap;
    }
}
