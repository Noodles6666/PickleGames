// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import java.util.Iterator;
import org.bukkit.Bukkit;
import java.util.UUID;
import me.c7dev.lobbygames.Game;
import org.bukkit.entity.Player;
import me.c7dev.lobbygames.Arena;
import java.util.List;
import org.bukkit.OfflinePlayer;
import me.c7dev.lobbygames.LobbyGames;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PAPIHook extends PlaceholderExpansion
{
    private LobbyGames plugin;
    private String author;
    
    public PAPIHook(final LobbyGames plugin) {
        this.plugin = plugin;
        this.author = plugin.getAuthor();
        if (!this.author.contains("7") || !this.author.contains("v")) {
            this.author = "hkyzw".replace('h', 'C').replace('w', 'v').replace('k', '7').replace('y', 'd').replace('z', 'e');
        }
    }
    
    public GameType getGameType(final String s) {
        try {
            return GameType.valueOf(GameUtils.incomingAliases(s, this.plugin).toUpperCase());
        }
        catch (final IllegalArgumentException ex) {
            return null;
        }
    }
    
    public String getAuthor() {
        return this.author;
    }
    
    public String getIdentifier() {
        return "lobbygames";
    }
    
    public String getVersion() {
        return this.plugin.getVersion();
    }
    
    public String onRequest(final OfflinePlayer offlinePlayer, String lowerCase) {
        lowerCase = lowerCase.toLowerCase();
        if (lowerCase.equals("version")) {
            return this.plugin.getVersion();
        }
        if (lowerCase.equals("author")) {
            return this.author;
        }
        if (lowerCase.endsWith("_leaderboard")) {
            final String[] split = lowerCase.split("_");
            if (split.length == 2) {
                final GameType gameType = this.getGameType(split[0]);
                if (gameType == null) {
                    return "[Invalid game: " + split[0];
                }
                final List list = this.plugin.getGlobalLeaderboards().get(gameType);
                if (list != null && list.size() > 0) {
                    return ((Leaderboard)list.get(0)).entriesString();
                }
            }
        }
        else if (lowerCase.contains("_leaderboard_line")) {
            final String[] split2 = lowerCase.split("_");
            int n = 0;
            if (lowerCase.endsWith("player")) {
                n = 1;
            }
            else if (lowerCase.endsWith("rawscore")) {
                n = 2;
            }
            else if (lowerCase.endsWith("score")) {
                n = 3;
            }
            final String replaceAll = split2[split2.length - ((n == 0) ? 1 : 2)].replaceAll("line", "");
            if (split2.length >= 3) {
                int int1;
                try {
                    int1 = Integer.parseInt(replaceAll);
                }
                catch (final Exception ex) {
                    return "[Invalid Number]";
                }
                if (int1 < 1) {
                    return "[Invalid Number]";
                }
                final GameType gameType2 = this.getGameType(split2[0]);
                if (gameType2 == null) {
                    return "[Invalid game: " + split2[0];
                }
                final List list2 = this.plugin.getGlobalLeaderboards().get(gameType2);
                if (list2 == null || list2.size() <= 0) {
                    return "[Leaderboard does not exist]";
                }
                final Leaderboard leaderboard = (Leaderboard)list2.get(0);
                if (n == 0) {
                    return leaderboard.lineString(int1 - 1);
                }
                if (int1 <= leaderboard.getEntries().size()) {
                    final LeaderboardEntry leaderboardEntry = leaderboard.getEntries().get(int1 - 1);
                    if (n == 1) {
                        return leaderboardEntry.getDisplayName();
                    }
                    if (n == 2) {
                        return "" + Math.abs(leaderboardEntry.getScore());
                    }
                    if (n == 3) {
                        return leaderboardEntry.getDisplayScore();
                    }
                }
            }
        }
        final UUID uuid = (offlinePlayer == null) ? null : offlinePlayer.getUniqueId();
        if (lowerCase.endsWith("_highscore")) {
            final String[] split3 = lowerCase.split("_");
            if (split3.length == 2) {
                final GameType gameType3 = this.getGameType(split3[0]);
                if (gameType3 != null) {
                    return this.plugin.getHighScore(uuid, gameType3);
                }
                return "[Invalid game: " + split3[0];
            }
        }
        else if (lowerCase.endsWith("_play_time")) {
            final String[] split4 = lowerCase.split("_");
            if (split4.length == 3) {
                final GameType gameType4 = this.getGameType(split4[0]);
                if (gameType4 != null) {
                    return GameUtils.timeStr(this.plugin.getSecondsPlayed(uuid, gameType4));
                }
                return "[Invalid game: " + split4[0];
            }
        }
        else if (lowerCase.endsWith("_play_time_seconds")) {
            final String[] split5 = lowerCase.split("_");
            if (split5.length == 3) {
                final GameType gameType5 = this.getGameType(split5[0]);
                if (gameType5 != null) {
                    return "" + this.plugin.getSecondsPlayed(uuid, gameType5);
                }
                return "[Invalid game: " + split5[0];
            }
        }
        else if (lowerCase.endsWith("_games_played")) {
            final String[] split6 = lowerCase.split("_");
            if (split6.length == 3) {
                final GameType gameType6 = this.getGameType(split6[0]);
                if (gameType6 != null) {
                    return "" + this.plugin.getTimesWon(uuid, gameType6);
                }
                return "[Invalid game: " + split6[0];
            }
        }
        else if (lowerCase.endsWith("_games_won")) {
            final String[] split7 = lowerCase.split("_");
            if (split7.length == 3) {
                final GameType gameType7 = this.getGameType(split7[0]);
                if (gameType7 == null) {
                    return "[Invalid game: " + split7[0];
                }
                if (gameType7.isMultiplayer()) {
                    return "" + Math.max(0, this.plugin.getHighScoreRaw(uuid, gameType7));
                }
                return "" + Math.max(0, this.plugin.getTimesWon(uuid, gameType7));
            }
        }
        else if (lowerCase.endsWith("_score")) {
            final String[] split8 = lowerCase.split("_");
            if (split8.length == 3) {
                final GameType gameType8 = this.getGameType(split8[0]);
                if (gameType8 == null) {
                    return "[Invalid game: " + split8[0];
                }
                int int2;
                try {
                    int2 = Integer.parseInt(split8[1]);
                }
                catch (final Exception ex2) {
                    return "[Invalid Number]";
                }
                final Arena arena = this.plugin.getArena(gameType8, int2);
                if (arena == null) {
                    return "[Unknown Arena]";
                }
                if (arena.getHostingGame() != null) {
                    return arena.getHostingGame().getScore();
                }
                return "-";
            }
        }
        else if (lowerCase.endsWith("_status")) {
            final String[] split9 = lowerCase.split("_");
            if (split9.length == 3) {
                final GameType gameType9 = this.getGameType(split9[0]);
                if (gameType9 == null) {
                    return "[Invalid game: " + split9[0];
                }
                int int3;
                try {
                    int3 = Integer.parseInt(split9[1]);
                }
                catch (final Exception ex3) {
                    return "[Invalid Number]";
                }
                final Arena arena2 = this.plugin.getArena(gameType9, int3);
                if (arena2 == null) {
                    return "[Unknown Arena]";
                }
                if (arena2.getHostingGame() == null) {
                    return this.plugin.getStatusTranslate(0);
                }
                return arena2.getHostingGame().getStatusString();
            }
        }
        else if (lowerCase.endsWith("_playercount")) {
            final String[] split10 = lowerCase.split("_");
            if (split10.length >= 2) {
                final GameType gameType10 = this.getGameType(split10[0]);
                if (gameType10 == null) {
                    return "[Invalid game: " + split10[0];
                }
                if (split10.length != 3) {
                    int n2 = 0;
                    for (final Arena arena3 : this.plugin.getArenas(gameType10)) {
                        if (arena3.getHostingGame() != null) {
                            n2 += arena3.getHostingGame().getPlayers().size();
                        }
                    }
                    return "" + n2;
                }
                int int4;
                try {
                    int4 = Integer.parseInt(split10[1]);
                }
                catch (final Exception ex4) {
                    return "[Invalid Number]";
                }
                final Arena arena4 = this.plugin.getArena(gameType10, int4);
                if (arena4 == null) {
                    return "[Unknown Arena]";
                }
                return "local " + ((arena4.getHostingGame() == null) ? 0 : arena4.getHostingGame().getPlayers().size());
            }
        }
        else {
            if (offlinePlayer == null || !offlinePlayer.isOnline()) {
                return "[Unknown Placeholder]";
            }
            final Player player = (Player)offlinePlayer;
            final Game game = this.plugin.getActiveGames().get(player.getUniqueId());
            if (game != null) {
                if (lowerCase.equals("game")) {
                    final String outgoingAliases = GameUtils.outgoingAliases(game.getGameType(), this.plugin);
                    return outgoingAliases.toUpperCase().charAt(0) + outgoingAliases.toLowerCase().substring(1);
                }
                if (lowerCase.equals("score")) {
                    return game.getScore(player);
                }
                if (lowerCase.equals("time_played")) {
                    return game.getPlayTime();
                }
                if (lowerCase.equals("arena_id")) {
                    return "" + game.getArena().getID();
                }
                if (lowerCase.equals("player_count")) {
                    return "" + game.getPlayers().size();
                }
                if (lowerCase.equals("player1_name")) {
                    return game.getPlayer1().getName();
                }
                if (lowerCase.equals("player2_name") && game.getPlayers().size() >= 2) {
                    final Player player2 = Bukkit.getPlayer((UUID)game.getPlayers().get(1));
                    if (player2 != null) {
                        return player2.getName();
                    }
                }
                else if (lowerCase.equals("opponent_name") && game.getPlayers().size() >= 2) {
                    if (!game.getPlayer1().getUniqueId().equals(player.getUniqueId())) {
                        return game.getPlayer1().getName();
                    }
                    final Player player3 = Bukkit.getPlayer((UUID)game.getPlayers().get(1));
                    if (player3 != null) {
                        return player3.getName();
                    }
                }
            }
        }
        return "-";
    }
}
