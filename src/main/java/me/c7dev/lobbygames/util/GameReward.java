// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.command.CommandSender;
import java.util.UUID;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.NamespacedKey;
import me.c7dev.lobbygames.Game;
import org.bukkit.entity.Player;
import java.util.Iterator;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import org.bukkit.configuration.file.FileConfiguration;
import me.c7dev.lobbygames.LobbyGames;
import java.util.List;

public class GameReward
{
    private boolean oneTime;
    private boolean enabled;
    private boolean perPlayer;
    private boolean runOnQuit;
    private List<String> cmds;
    private Boolean forWinner;
    private LobbyGames plugin;
    private int minScore;
    private int maxScore;
    private String name;
    private String msg;
    private GameType gt;
    
    public GameReward(final LobbyGames plugin, final GameType gt, final FileConfiguration fileConfiguration, final FileConfiguration fileConfiguration2, final String name) {
        this.oneTime = false;
        this.enabled = true;
        this.cmds = new ArrayList<String>();
        this.plugin = plugin;
        this.gt = gt;
        this.name = name;
        if (!name.startsWith((gt == GameType.T048) ? "2048" : gt.toString().toLowerCase())) {
            Bukkit.getLogger().severe("Can not load reward '" + name);
            this.enabled = false;
            return;
        }
        if (!fileConfiguration.getBoolean(name + ".enabled", true)) {
            this.enabled = false;
            return;
        }
        this.minScore = fileConfiguration.getInt(name + ".min-score", -1);
        this.maxScore = fileConfiguration.getInt(name + ".max-score", -1);
        this.perPlayer = fileConfiguration.getBoolean(name + ".per-player", true);
        this.oneTime = fileConfiguration.getBoolean(name + ".one-time");
        this.runOnQuit = fileConfiguration.getBoolean(name + ".run-on-quit", false);
        this.addCommands(fileConfiguration, name + ".console-commands", this.cmds);
        this.addCommands(fileConfiguration, name + ".commands", this.cmds);
        this.msg = fileConfiguration.getString(name + ".msg");
        if (this.msg == null) {
            this.msg = fileConfiguration.getString(name + ".message");
        }
        if (this.msg != null) {
            this.msg = this.msg.replace('&', '§').replaceAll("\\Q[newline]\\E", "\n").replaceAll("\\n", "\n");
        }
        if (fileConfiguration.get(name + ".for-winning-team") != null || fileConfiguration.get(name + ".for-winning-player") != null) {
            this.forWinner = (fileConfiguration.getBoolean(name + ".for-winning-team") || fileConfiguration.getBoolean(name + ".for-winning-player"));
        }
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public boolean isPerPlayer() {
        return this.perPlayer;
    }
    
    private void addCommands(final FileConfiguration fileConfiguration, final String s, final List<String> list) {
        final List stringList = fileConfiguration.getStringList(s);
        if (stringList == null) {
            return;
        }
        final Iterator iterator = stringList.iterator();
        while (iterator.hasNext()) {
            String[] split;
            for (int length = (split = ((String)iterator.next()).split("\n")).length, i = 0; i < length; ++i) {
                list.add(split[i]);
            }
        }
    }
    
    public void reward(final Player player, final Game game) {
        this.reward(player, true, game);
    }
    
    public void reward(final Player player, final boolean b, final Game game) {
        if (!this.enabled) {
            return;
        }
        if (this.forWinner != null && this.forWinner != b) {
            return;
        }
        if (!this.runOnQuit && !game.wasActive()) {
            return;
        }
        final int abs = Math.abs(game.getScoreInt());
        final String score = game.getScore();
        if (this.minScore >= 0 && abs < this.minScore) {
            return;
        }
        if (this.maxScore >= 0 && abs > this.maxScore) {
            return;
        }
        if (this.oneTime && LobbyGames.SERVER_VERSION >= 19 && player != null) {
            final NamespacedKey namespacedKey = new NamespacedKey((Plugin)this.plugin, "lg-received-" + this.gt.toString() + "-" + this.name);
            final PersistentDataContainer persistentDataContainer = player.getPersistentDataContainer();
            final Boolean b2 = (Boolean)persistentDataContainer.get(namespacedKey, PersistentDataType.BOOLEAN);
            if (b2 != null && b2) {
                return;
            }
            persistentDataContainer.set(namespacedKey, PersistentDataType.BOOLEAN, Boolean.TRUE);
        }
        final String name = game.getWinner().getName();
        final ArrayList elements = new ArrayList();
        final ArrayList elements2 = new ArrayList();
        for (final UUID uuid : game.getPlayers()) {
            (game.isWinner(uuid) ? elements : elements2).add(Bukkit.getOfflinePlayer(uuid).getName());
        }
        if (game instanceof final Spectatable spectatable) {
            for (final UUID uuid2 : spectatable.getSpectators()) {
                if (!game.hasWinnerEntrySet(uuid2)) {
                    continue;
                }
                (game.isWinner(uuid2) ? elements : elements2).add(Bukkit.getOfflinePlayer(uuid2).getName());
            }
        }
        final String join = String.join(", ", elements);
        final String join2 = String.join(", ", elements2);
        final String s = (String)((elements2.size() > 0) ? elements2.get(0) : "-");
        if (this.msg != null && player != null) {
            player.sendMessage(this.msg.replaceAll("\\Q%player%\\E", player.getName()).replaceAll("\\Q%score%\\E", score).replaceAll("\\Q%score_raw%\\E", "" + abs).replaceAll("\\Q%winner%\\E", name).replaceAll("\\Q%winners%\\E", join).replaceAll("\\Q%loser%\\E", s).replaceAll("\\Q%losers%\\E", join2));
        }
        final ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
        for (String s2 : this.cmds) {
            String s3 = s2.replaceAll("\\Q%score%\\E", score).replaceAll("\\Q%score_raw%\\E", "" + abs).replaceAll("\\Q%arena_id%\\E", "" + game.getArena().getID()).replaceAll("\\Q%winner%\\E", name).replaceAll("\\Q%winners%\\E", join).replaceAll("\\Q%loser%\\E", s).replaceAll("\\Q%losers%\\E", join2);
            if (player != null) {
                s3 = s3.replaceAll("\\Q%player%\\E", player.getName()).replaceAll("\\Q%uuid%\\E", player.getUniqueId().toString());
            }
            if (s2.startsWith("/")) {
                s2.substring(1);
            }
            Bukkit.dispatchCommand((CommandSender)consoleSender, this.plugin.sp(player, s3).trim());
        }
    }
}
