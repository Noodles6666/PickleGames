// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import java.lang.invoke.CallSite;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.StringConcatFactory;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.LeaderboardSurpassEvent;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.util.Iterator;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import java.util.List;

public class Leaderboard
{
    private List<LeaderboardEntry> board;
    private int size;
    private List<ArmorStand> as;
    private Location loc;
    private LobbyGames plugin;
    private String title;
    private String format;
    private String empty;
    private long expiry;
    private GameType gt;
    private boolean merge;
    
    public Leaderboard(final LobbyGames plugin, final GameType gt, final Location loc) {
        this.board = new ArrayList<LeaderboardEntry>();
        this.as = new ArrayList<ArmorStand>();
        this.merge = false;
        int int1 = plugin.getConfig().getInt("leaderboard-size");
        if (int1 < 1) {
            int1 = 1;
        }
        else if (int1 > 10) {
            int1 = 10;
        }
        this.size = int1;
        this.plugin = plugin;
        this.loc = loc;
        this.gt = gt;
        this.expiry = plugin.getLeaderboardExpiry();
        this.loadStrings();
    }
    
    public void loadStrings() {
        final String outgoingAliases = GameUtils.outgoingAliases(this.gt, this.plugin);
        this.title = this.plugin.getConfigString("leaderboard-title", "§2§l%game% Leaderboard:").replaceAll("\\Q%%__LICENSE__%%\\E", outgoingAliases.toLowerCase().toString().charAt(0) + outgoingAliases.toString().substring(1)).replaceAll("\\Q%game%\\E", outgoingAliases.toUpperCase().toString().charAt(0) + outgoingAliases.toString().substring(1));
        this.format = this.plugin.getConfigString("leaderboard-format", "§b%ranking%: §a%name% §6%score%");
        this.empty = this.format.replaceAll("\\Q%name%\\E", "§7-").replaceAll("\\Q%player%\\E", this.format.contains("-") ? "" : "§7-").replaceAll("\\Q%score%\\E", "").trim();
        this.merge = (this.gt.isMultiplayer() || this.plugin.getConfig().getBoolean("merge-leaderboard-scores"));
    }
    
    public List<LeaderboardEntry> getEntries() {
        return this.board;
    }
    
    public int getSize() {
        return this.size;
    }
    
    public GameType getGameType() {
        return this.gt;
    }
    
    public LobbyGames getPlugin() {
        return this.plugin;
    }
    
    private void updateDisplayNames() {
        final boolean boolean1 = this.plugin.getConfig().getBoolean("use-display-names");
        for (final LeaderboardEntry leaderboardEntry : this.board) {
            if (leaderboardEntry.getUniqueId() == null) {
                continue;
            }
            if (boolean1) {
                final Player player = Bukkit.getPlayer(leaderboardEntry.getUniqueId());
                if (player == null || player.getDisplayName().equals(leaderboardEntry.getDisplayName())) {
                    continue;
                }
                leaderboardEntry.setDisplayName(player.getDisplayName());
            }
            else {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(leaderboardEntry.getUniqueId());
                if (offlinePlayer == null || offlinePlayer.getName() == null || offlinePlayer.getName().equals(leaderboardEntry.getDisplayName())) {
                    continue;
                }
                leaderboardEntry.setDisplayName(offlinePlayer.getName());
            }
        }
    }
    
    public void setEntries(final List<LeaderboardEntry> list) {
        this.board = ((list == null) ? new ArrayList<LeaderboardEntry>() : list);
        this.updateDisplay();
    }
    
    public void addScore(final Player player, final int n) {
        this.addScore(player, n, null);
    }
    
    public void addScore(final Player player, final int n, final String s) {
        this.addScore(player.getUniqueId(), this.plugin.getConfig().getBoolean("use-display-names") ? player.getDisplayName() : player.getName(), n, s);
    }
    
    public void addScore(final UUID uuid, final String s, final int n, final String s2) {
        this.addScore(uuid, s, n, s2, false);
    }
    
    public void addScore(final UUID uuid, final String anObject, final int score, final String s, final boolean b) {
        LeaderboardEntry leaderboardEntry = null;
        final HashMap hashMap = new HashMap();
        final HashMap hashMap2 = new HashMap();
        for (int n = 0; n < this.board.size() && this.board.get(n).getUniqueId() != null; ++n) {
            hashMap.put(this.board.get(n).getUniqueId(), n);
            hashMap2.put(n, this.board.get(n).getUniqueId());
        }
        if (this.merge || b) {
            for (LeaderboardEntry leaderboardEntry2 : this.board) {
                if (uuid.equals(leaderboardEntry2.getUniqueId()) || (leaderboardEntry2.getUniqueId() == null && leaderboardEntry2.getDisplayName().equals(anObject))) {
                    if (score > leaderboardEntry2.getScore() || this.gt.isMultiplayer()) {
                        if (this.gt.isMultiplayer()) {
                            leaderboardEntry2.setScore(leaderboardEntry2.getScore() + 1);
                        }
                        else {
                            leaderboardEntry2.setScore(score);
                        }
                        leaderboardEntry2.setDisplayScore((s == null) ? ("" + leaderboardEntry2.getScore()) : s);
                        leaderboardEntry2.setExpiration(this.expiry);
                        leaderboardEntry = leaderboardEntry2;
                        this.resortScores();
                        break;
                    }
                    return;
                }
            }
        }
        if (leaderboardEntry == null) {
            final LeaderboardEntry leaderboardEntry3 = new LeaderboardEntry(uuid, anObject, score, s, this.expiry);
            final ArrayList board = new ArrayList();
            for (final LeaderboardEntry leaderboardEntry4 : this.board) {
                if (leaderboardEntry == null && score > leaderboardEntry4.getScore()) {
                    board.add(leaderboardEntry3);
                    leaderboardEntry = leaderboardEntry3;
                }
                if (!leaderboardEntry4.isExpired()) {
                    board.add(leaderboardEntry4);
                }
            }
            if (leaderboardEntry == null) {
                board.add(leaderboardEntry3);
                leaderboardEntry = leaderboardEntry3;
            }
            this.board = board;
        }
        this.updateDisplay();
        int n2 = 0;
        for (int i = 0; i < this.board.size(); ++i) {
            final LeaderboardEntry leaderboardEntry5 = this.board.get(i);
            if (leaderboardEntry5.getUniqueId() != null) {
                if (!leaderboardEntry5.getUniqueId().equals(leaderboardEntry.getUniqueId())) {
                    if (leaderboardEntry.getScore() > leaderboardEntry5.getScore()) {
                        if (i > (int)hashMap.get(leaderboardEntry5.getUniqueId())) {
                            final UUID uniqueId = leaderboardEntry5.getUniqueId();
                            Bukkit.getPluginManager().callEvent((Event)new LeaderboardSurpassEvent(this, (int)hashMap.get(uniqueId), n2, uniqueId, leaderboardEntry.getUniqueId()));
                        }
                    }
                    else {
                        n2 = i;
                    }
                }
            }
        }
    }
    
    public void resortScores() {
        final ArrayList board = new ArrayList();
        while (this.board.size() > 0) {
            LeaderboardEntry leaderboardEntry = null;
            for (int i = 0; i < this.board.size(); ++i) {
                final LeaderboardEntry leaderboardEntry2 = this.board.get(i);
                if (leaderboardEntry == null || this.board.get(i).getScore() > leaderboardEntry.getScore()) {
                    leaderboardEntry = leaderboardEntry2;
                }
            }
            this.board.remove(leaderboardEntry);
            board.add(leaderboardEntry);
        }
        this.board = board;
    }
    
    public void reloadFromConfig() {
        int int1 = this.plugin.getConfig().getInt("leaderboard-size");
        if (int1 < 2) {
            int1 = 2;
        }
        else if (int1 > 20) {
            int1 = 20;
        }
        this.size = int1;
        this.loadStrings();
        this.updateDisplayNames();
        this.updateDisplay();
    }
    
    public String entriesString() {
        String title = this.title;
        for (int i = 1; i < Math.min(this.size, this.board.size()) + 1; ++i) {
            final LeaderboardEntry leaderboardEntry = this.board.get(i - 1);
            title = title + "\n" + this.format.replaceAll("\\Q%ranking%\\E", /* invokedynamic(!) */ProcyonInvokeDynamicHelper_3.invoke(i)).replaceAll("\\Q%player%\\E", leaderboardEntry.getDisplayName()).replaceAll("\\Q%score%\\E", leaderboardEntry.getDisplayScore()).replaceAll("\\Q%name%\\E", leaderboardEntry.getDisplayName());
        }
        return title;
    }
    
    public String lineString(final int n) {
        if (this.board.size() - 1 < n || n < 0) {
            return this.empty.replaceAll("\\Q%ranking%\\E", "").replaceAll(":", "").trim();
        }
        final LeaderboardEntry leaderboardEntry = this.board.get(n);
        return this.format.replaceAll("\\Q%ranking%\\E", "#" + (n + 1)).replaceAll("\\Q%player%\\E", leaderboardEntry.getDisplayName()).replaceAll("\\Q%score%\\E", leaderboardEntry.getDisplayScore()).replaceAll("\\Q%name%\\E", leaderboardEntry.getDisplayName()).trim();
    }
    
    public void updateDisplay() {
        new BukkitRunnable() {
            public void run() {
                Leaderboard.this.remove();
                if (Leaderboard.this.plugin.isHologramsDisabled() || !Leaderboard.this.loc.getChunk().isLoaded() || Leaderboard.this.loc.getWorld().getPlayers().size() == 0) {
                    return;
                }
                if (Leaderboard.this.as.size() < Leaderboard.this.size + 1) {
                    final Iterator<ArmorStand> iterator = Leaderboard.this.as.iterator();
                    while (iterator.hasNext()) {
                        iterator.next().remove();
                    }
                    Leaderboard.this.as.clear();
                    final double n = 0.26;
                    final Location add = Leaderboard.this.loc.clone().add(0.0, (Leaderboard.this.size + 1) * n - 1.5, 0.0);
                    for (int i = 0; i < Leaderboard.this.size + 1; ++i) {
                        Leaderboard.this.as.add(new ArmorStandFactory(add).setName(Leaderboard.this.empty.replaceAll("\\Q%ranking%\\E", "#" + i)).spawn());
                        add.add(0.0, -n, 0.0);
                    }
                }
                Leaderboard.this.as.get(0).setCustomName(Leaderboard.this.title);
                for (int j = 1; j <= Math.min(Leaderboard.this.size, Leaderboard.this.board.size()); ++j) {
                    final LeaderboardEntry leaderboardEntry = Leaderboard.this.board.get(j - 1);
                    Leaderboard.this.as.get(j).setCustomName(Leaderboard.this.format.replaceAll("\\Q%ranking%\\E", "#" + j).replaceAll("\\Q%player%\\E", leaderboardEntry.getDisplayName()).replaceAll("\\Q%score%\\E", leaderboardEntry.getDisplayScore()).replaceAll("\\Q%name%\\E", leaderboardEntry.getDisplayName()));
                }
            }
        }.runTaskLater((Plugin)this.plugin, 1L);
    }
    
    public void remove() {
        final Iterator<ArmorStand> iterator = this.as.iterator();
        while (iterator.hasNext()) {
            iterator.next().remove();
        }
        this.as.clear();
        if (this.loc != null) {
            for (final Entity entity : this.loc.getWorld().getNearbyEntities(this.loc, 0.25, 5.0, 0.25)) {
                if (entity instanceof ArmorStand) {
                    entity.remove();
                }
            }
        }
    }
    
    public Location getLocation() {
        return this.loc;
    }
    
    // This helper class was generated by Procyon to approximate the behavior of an
    // 'invokedynamic' instruction that it doesn't know how to interpret.
    private static final class ProcyonInvokeDynamicHelper_3
    {
        private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
        private static MethodHandle handle;
        private static volatile int fence;
        
        private static MethodHandle handle() {
            final MethodHandle handle = ProcyonInvokeDynamicHelper_3.handle;
            if (handle != null)
                return handle;
            return ProcyonInvokeDynamicHelper_3.ensureHandle();
        }
        
        private static MethodHandle ensureHandle() {
            ProcyonInvokeDynamicHelper_3.fence = 0;
            MethodHandle handle = ProcyonInvokeDynamicHelper_3.handle;
            if (handle == null) {
                MethodHandles.Lookup lookup = ProcyonInvokeDynamicHelper_3.LOOKUP;
                try {
                    handle = ((CallSite)StringConcatFactory.makeConcatWithConstants(lookup, "makeConcatWithConstants", MethodType.methodType(String.class, int.class), "#\u0001")).dynamicInvoker();
                }
                catch (Throwable t) {
                    throw new UndeclaredThrowableException(t);
                }
                ProcyonInvokeDynamicHelper_3.fence = 1;
                ProcyonInvokeDynamicHelper_3.handle = handle;
                ProcyonInvokeDynamicHelper_3.fence = 0;
            }
            return handle;
        }
        
        private static String invoke(int p0) {
            try {
                return (String) ProcyonInvokeDynamicHelper_3.handle().invokeExact(p0);
            }
            catch (Throwable t) {
                throw new UndeclaredThrowableException(t);
            }
        }
    }
}
