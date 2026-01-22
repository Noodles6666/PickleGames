// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.commands;

import java.lang.invoke.CallSite;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.StringConcatFactory;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles;
import org.bukkit.OfflinePlayer;
import org.bukkit.Location;
import java.util.Iterator;
import me.c7dev.lobbygames.util.LeaderboardEntry;
import me.c7dev.lobbygames.util.Leaderboard;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import me.c7dev.lobbygames.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.command.PluginCommand;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.util.GameType;
import java.util.ArrayList;
import java.util.List;
import me.c7dev.lobbygames.Arena;
import java.util.UUID;
import java.util.HashMap;
import me.c7dev.lobbygames.LobbyGames;
import me.c7dev.lobbygames.gui.AdminGUI;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;

public class LobbyGamesCommand implements CommandExecutor, TabCompleter
{
    private LobbyGames plugin;
    private String noperm;
    private HashMap<UUID, Arena> delconfirm;
    private List<String> tabgamelist;
    private AdminGUI adminGUI;
    
    public LobbyGamesCommand(final LobbyGames plugin) {
        this.delconfirm = new HashMap<UUID, Arena>();
        this.tabgamelist = new ArrayList<String>();
        this.plugin = plugin;
        this.noperm = this.plugin.getConfigString("no-permission", "§cYou don't have permission!");
        this.adminGUI = new AdminGUI(plugin);
        final PluginCommand command = this.plugin.getCommand("picklegames");
        command.setExecutor((CommandExecutor)this);
        command.setTabCompleter((TabCompleter)this);
        GameType[] values;
        for (int length = (values = GameType.values()).length, i = 0; i < length; ++i) {
            this.tabgamelist.add(GameUtils.getConfigName(values[i]).toLowerCase());
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
    
    public Arena getArena(final String[] array, final Player player) {
        return this.getArena(array[1], (array.length >= 3) ? array[2] : null, player);
    }
    
    public Arena getArena(final String s, final String s2, final Player player) {
        final GameType gameType = this.getGameType(s);
        if (gameType == null) {
            player.sendMessage(this.plugin.getConfigString("cmd-error-invalid-game-type", "§4错误：§c'%game%' 不是有效的游戏类型！").replace("%game%", s));
            return null;
        }
        int int1 = 1;
        if (s2 != null) {
            try {
                int1 = Integer.parseInt(s2);
            }
            catch (final Exception ex) {
                player.sendMessage(this.plugin.getConfigString("cmd-error-not-number", "§4错误：§c'%value%' 不是数字！").replace("%value%", s2));
                return null;
            }
        }
        final Arena arena = this.plugin.getArena(gameType, int1);
        if (arena == null) {
            player.sendMessage(this.plugin.getConfigString("cmd-error-arena-not-found", "§4错误：§c没有 ID 为 %id% 的 %game% 竞技场！").replace("%game%", GameUtils.outgoingAliases(gameType, this.plugin)).replace("%id%", String.valueOf(int1)));
            return null;
        }
        return arena;
    }
    
    private boolean isJoinCmdEnabled() {
        return this.plugin.getConfig().getBoolean("lg-join-command-enabled", true);
    }
    
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] array) {
        if (array.length >= 1 && array[0].equalsIgnoreCase("reload")) {
            if (commandSender.hasPermission("lobbygames.admin")) {
                this.plugin.reload();
                this.plugin.reloadLeaderboards();
                commandSender.sendMessage(this.plugin.getConfigString("cmd-reload-success", "§bLobbyGames 已重新加载！"));
            }
            else {
                commandSender.sendMessage(this.noperm);
            }
            return true;
        }
        if (!(commandSender instanceof Player)) {
            return true;
        }
        final Player player = (Player)commandSender;
        final String sp = this.plugin.sp(player, this.noperm);
        if (array.length == 0) {
            player.sendMessage(this.plugin.getConfigString("cmd-version", "§b正在使用 LobbyGames v%version%").replace("%version%", this.plugin.getVersion()));
            if (player.hasPermission("lobbygames.command")) {
                player.sendMessage(this.plugin.getConfigString("cmd-help-hint", "§a使用 §b/lg help§a 查看帮助！"));
            }
            return true;
        }
        if (array[0].equalsIgnoreCase("help") || array[0].equalsIgnoreCase("?")) {
            if (player.hasPermission("lobbygames.command") || player.hasPermission("lobbygames.admin") || player.hasPermission("lobbygames.kickplayer")) {
                player.sendMessage(this.plugin.getConfigString("cmd-help-title", "\n§3§lLobbyGames 命令："));
                player.sendMessage(this.plugin.getConfigString("cmd-help-join", "§b/lg join <游戏> [竞技场ID]"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-restart", "§b/lg restart"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-quit", "§b/lg quit"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-list", "§b/lg list [游戏] §7- lobbygames.command"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-tp", "§b/lg tp <游戏> <竞技场ID> §7- lobbygames.command"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-kick", "§b/lg kick <玩家> §7- lobbygames.kickplayer"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-clearstands", "§b/lg clearstands <游戏> <竞技场ID> §7- lobbygames.admin"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-create", "§b/lg create <游戏> §7- lobbygames.admin"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-delete", "§b/lg delete <游戏> <竞技场ID> §7- lobbygames.admin"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-leaderboard", "§b/lg leaderboard <create;clear;delete> <游戏> §7- lobbygames.admin"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-reload", "§b/lg reload §7- lobbygames.admin"));
                player.sendMessage(this.plugin.getConfigString("cmd-help-gui", "§b/lg gui §7- lobbygames.admin §6§l[新功能]"));
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("gui") || array[0].equalsIgnoreCase("menu")) {
            if (player.hasPermission("lobbygames.admin")) {
                this.adminGUI.openMainMenu(player);
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("join")) {
            if (!this.isJoinCmdEnabled()) {
                if (player.hasPermission("lobbygames.admin")) {
                    player.sendMessage(this.plugin.getConfigString("cmd-error-join-disabled", "§4错误：§c此命令在配置中已禁用 (lg-join-cmd-enabled)"));
                }
                return true;
            }
            if (array.length < 2) {
                player.sendMessage(this.plugin.getConfigString("cmd-usage-join", "§4用法：§c/lg join <游戏> [竞技场ID]"));
                return true;
            }
            if (this.plugin.getActiveGames().get(player.getUniqueId()) != null) {
                player.sendMessage(this.plugin.getConfigString(player, "error-already-in-game", "§4错误：§c你已经在游戏中了，使用 /lg quit 退出"));
                return true;
            }
            if (array.length >= 2) {
                final GameType gameType = this.getGameType(array[1]);
                if (gameType == null) {
                    player.sendMessage(this.plugin.getConfigString("cmd-error-invalid-game-type", "§4错误：§c'%game%' 不是有效的游戏类型！").replace("%game%", array[1]));
                    return true;
                }
                int int1 = -1;
                if (array.length >= 3 && !array[2].equalsIgnoreCase("tac")) {
                    try {
                        int1 = Integer.parseInt(array[2]);
                    }
                    catch (final Exception ex) {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-not-number", "§4错误：§c'%value%' 不是数字！").replace("%value%", array[2]));
                        return true;
                    }
                }
                this.plugin.joinPlayer(player, gameType, int1);
            }
            else {
                commandSender.sendMessage(this.plugin.getConfigString("cmd-usage-join", "§4用法：§c/lg join <游戏> [竞技场ID]"));
            }
        }
        else if (array[0].equalsIgnoreCase("reset") || array[0].equalsIgnoreCase("restart")) {
            final Game game = this.plugin.getActiveGames().get(player.getUniqueId());
            if (game == null) {
                player.sendMessage(this.plugin.getConfigString(player, "error-must-be-in-game", "§4Error: §cYou must be in a game to do this!"));
                return true;
            }
            game.restart();
        }
        else if (array[0].equalsIgnoreCase("create")) {
            if (player.hasPermission("lobbygames.admin")) {
                if (array.length >= 2) {
                    final GameType gameType2 = this.getGameType(array[1]);
                    if (gameType2 != null) {
                        // 强制清理旧的编辑实例
                        final GameCreateInstance oldInstance = this.plugin.getEditingMap().get(player.getUniqueId());
                        if (oldInstance != null) {
                            player.sendMessage(this.plugin.getConfigString("cmd-deleted-unfinished", "§7已删除未完成的竞技场（类型：%type%）").replace("%type%", oldInstance.getGameType().toString()));
                            oldInstance.quit();
                            this.plugin.getEditingMap().remove(player.getUniqueId());
                        }
                        // 创建新的编辑实例
                        final GameCreateInstance newInstance = new GameCreateInstance(player, gameType2, this.plugin);
                        this.plugin.getEditingMap().put(player.getUniqueId(), newInstance);
                    }
                    else {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-invalid-game-type", "§4错误：§c'%game%' 不是有效的游戏类型！").replace("%game%", array[1]));
                    }
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-usage-create", "§4用法：§c/lg create <游戏类型>"));
                }
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("delete")) {
            if (player.hasPermission("lobbygames.admin")) {
                if (array.length >= 3) {
                    final Arena arena = this.getArena(array, player);
                    if (arena == null) {
                        return true;
                    }
                    if (arena.getHostingGame() != null) {
                        player.sendMessage(this.plugin.getConfigString("cmd-warning-arena-active", "§6警告：§e此竞技场当前有正在进行的游戏。"));
                    }
                    if (arena.getID() < this.plugin.getArenas(arena.getGameType()).size()) {
                        player.sendMessage(this.plugin.getConfigString("cmd-warning-id-shift", "§6警告：§e所有 ID 大于 %id% 的 %game% 竞技场将向下移动 1 位！").replace("%id%", String.valueOf(arena.getID())).replace("%game%", GameUtils.outgoingAliases(arena.getGameType(), this.plugin)));
                    }
                    player.sendMessage(this.plugin.getConfigString("cmd-delete-confirm", "§c你确定要删除此竞技场吗？输入 /lg confirm"));
                    this.delconfirm.put(player.getUniqueId(), arena);
                    new BukkitRunnable() {
                        public void run() {
                            if (LobbyGamesCommand.this.delconfirm.containsKey(player.getUniqueId())) {
                                LobbyGamesCommand.this.delconfirm.remove(player.getUniqueId());
                            }
                        }
                    }.runTaskLater((Plugin)this.plugin, 900L);
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-usage-delete", "§4用法：§c/lg delete <游戏> <竞技场ID>"));
                }
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("clearstands")) {
            if (player.hasPermission("lobbygames.admin")) {
                if (array.length >= 3) {
                    final Arena arena2 = this.getArena(array, player);
                    if (arena2 == null) {
                        return true;
                    }
                    if (arena2.getHostingGame() == null) {
                        arena2.clearArmorStands();
                        player.sendMessage(this.plugin.getConfigString("cmd-clearstands-success", "§a已清除此竞技场的盔甲架"));
                    }
                    else {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-arena-in-progress", "§4错误：§c此竞技场有正在进行的游戏！"));
                    }
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-usage-clearboard", "§e用法：§7/pg clearstands <游戏> <竞技场ID> &8(清除盔甲架)"));
                }
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("confirm")) {
            if (player.hasPermission("lobbygames.admin")) {
                final Arena arena3 = this.delconfirm.get(player.getUniqueId());
                if (arena3 != null) {
                    this.delconfirm.remove(player.getUniqueId());
                    if (arena3.getHostingGame() != null) {
                        arena3.getHostingGame().end();
                    }
                    if (this.plugin.deleteArena(arena3)) {
                        player.sendMessage(this.plugin.getConfigString("cmd-delete-success", "§b竞技场已删除。注意：所有 ID 更大的 %game% 竞技场已向下移动 1 位。").replace("%game%", GameUtils.outgoingAliases(arena3.getGameType(), this.plugin)));
                    }
                    else {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-delete-failed", "§4错误：§c无法删除此竞技场！"));
                    }
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-error-confirm-expired", "§4错误：§c此确认已过期！"));
                }
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("set")) {
            if (player.hasPermission("lobbygames.admin")) {
                final GameCreateInstance gameCreateInstance = this.plugin.getEditingMap().get(player.getUniqueId());
                if (gameCreateInstance != null) {
                    if (array.length == 1) {
                        gameCreateInstance.setLocation(player.getLocation());
                    }
                    else if (array[1].equalsIgnoreCase("scoreboard") || array[1].equalsIgnoreCase("leaderboard")) {
                        if (gameCreateInstance.setScoreboardLocation(player.getLocation())) {
                            player.sendMessage(this.plugin.getConfigString("cmd-set-location-success", "§a成功设置 §b%type%§a 位置！").replace("%type%", array[1].toLowerCase()));
                        }
                    }
                    else {
                        player.sendMessage(this.plugin.getConfigString("cmd-usage-set", "§4用法：§c\"/lg set\" 或 \"/lg set leaderboard\""));
                    }
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-error-not-editing", "§4错误：§c你必须处于编辑模式才能执行此操作！使用 §n/lg create"));
                }
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("finalize") || array[0].equalsIgnoreCase("save") || array[0].equalsIgnoreCase("finish")) {
            if (player.hasPermission("lobbygames.admin")) {
                final GameCreateInstance gameCreateInstance2 = this.plugin.getEditingMap().get(player.getUniqueId());
                if (gameCreateInstance2 != null) {
                    final int save = gameCreateInstance2.save();
                    if (save >= 0) {
                        String idSuffix = (save > 1) ? (" " + save) : "";
                        player.sendMessage(this.plugin.getConfigString("cmd-save-success", "§2成功！§a使用 §b/lg join %game%%id%§a 测试此竞技场").replace("%game%", GameUtils.getConfigName(gameCreateInstance2.getGameType())).replace("%id%", idSuffix));
                        this.plugin.getEditingMap().remove(player.getUniqueId());
                    }
                    else if (save == -1) {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-coords-invalid", "§4错误：§c坐标无效（设置阶段未捕获的错误）"));
                    }
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-error-not-editing", "§4错误：§c你必须处于编辑模式才能执行此操作！使用 §n/lg create§c 或 §n/lg edit"));
                }
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("quit") || array[0].equalsIgnoreCase("exit") || array[0].equalsIgnoreCase("leave")) {
            final GameCreateInstance gameCreateInstance3 = this.plugin.getEditingMap().get(player.getUniqueId());
            if (gameCreateInstance3 != null) {
                player.sendMessage(this.plugin.getConfigString("cmd-quit-editing", "§7已放弃未完成的竞技场数据，退出编辑模式。"));
                gameCreateInstance3.quit();
                this.plugin.getEditingMap().remove(player.getUniqueId());
            }
            else {
                final Game game2 = this.plugin.getActiveGames().get(player.getUniqueId());
                if (game2 != null) {
                    game2.removePlayer(player);
                }
            }
        }
        else if (array[0].equalsIgnoreCase("kick")) {
            if (player.hasPermission("lobbygames.kickplayer")) {
                if (array.length >= 2) {
                    final Player player2 = Bukkit.getPlayer(array[1]);
                    if (player2 != null) {
                        final Game game3 = this.plugin.getActiveGames().get(player2.getUniqueId());
                        if (game3 == null) {
                            player.sendMessage(this.plugin.getConfigString("cmd-error-player-not-in-game", "§4错误：§c%player% 不在游戏中！").replace("%player%", player2.getName()));
                            return true;
                        }
                        player2.sendMessage(this.plugin.getConfigString("cmd-kick-player", "§7你被 %player% 踢出了游戏").replace("%player%", player.getName()));
                        game3.removePlayer(player2);
                        player.sendMessage(this.plugin.getConfigString("cmd-kick-success", "§a成功将 %player% 踢出游戏！").replace("%player%", player2.getName()));
                    }
                    else {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-player-offline", "§4错误：§c此玩家不在线！"));
                    }
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-usage-kick", "§4用法：§c/lg kick <玩家>"));
                }
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("list")) {
            if (player.hasPermission("lobbygames.command")) {
                if (array.length == 1) {
                    player.sendMessage(this.plugin.getConfigString("cmd-list-summary", "§aLobbyGames 竞技场摘要："));
                    GameType[] values;
                    for (int length = (values = GameType.values()).length, i = 0; i < length; ++i) {
                        final GameType gameType3 = values[i];
                        final String outgoingAliases = GameUtils.outgoingAliases(gameType3, this.plugin);
                        final int size = this.plugin.getArenas(gameType3).size();
                        String gameName = outgoingAliases.toUpperCase().charAt(0) + outgoingAliases.substring(1);
                        player.sendMessage(this.plugin.getConfigString("cmd-list-game-count", "§b%game%：§3%count% 个竞技场").replace("%game%", gameName).replace("%count%", String.valueOf(size)));
                    }
                }
                else {
                    final GameType gameType4 = this.getGameType(array[1]);
                    if (gameType4 != null) {
                        player.sendMessage(this.plugin.getConfigString("cmd-list-game-arenas", "§aLobbyGames %game% 竞技场：").replace("%game%", GameUtils.outgoingAliases(gameType4, this.plugin)));
                        final List<Arena> arenas = this.plugin.getArenas(gameType4);
                        int n = 1;
                        for (Arena arena4 : arenas) {
                            final Location spawn1 = arena4.getSpawn1();
                            String s2 = "";
                            if (spawn1 != null) {
                                s2 = ", §3(" + spawn1.getBlockX() + ", " + spawn1.getBlockY() + ", " + spawn1.getBlockZ();
                            }
                            player.sendMessage(this.plugin.getConfigString("cmd-list-arena-info", "§a#%id%：§b'§o%world%§b'%coords%").replace("%id%", String.valueOf(n)).replace("%world%", arena4.getLocation1().getWorld().getName()).replace("%coords%", s2));
                            ++n;
                        }
                    }
                    else {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-unknown-game", "§4错误：§c未知的游戏 '%game%'").replace("%game%", array[1]));
                    }
                }
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("tp") || array[0].equalsIgnoreCase("teleport")) {
            if (player.hasPermission("lobbygames.command")) {
                if (array.length >= 2) {
                    final Arena arena5 = this.getArena(array, player);
                    if (arena5 == null) {
                        return true;
                    }
                    if (arena5.getSpawn1() != null) {
                        player.teleport(arena5.getSpawn1());
                    }
                    else {
                        player.teleport(arena5.getLocation1());
                    }
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-usage-tp", "§4用法：§c/lg tp <游戏> <竞技场ID>"));
                }
            }
            else {
                player.sendMessage(sp);
            }
        }
        else if (array[0].equalsIgnoreCase("leaderboard") || array[0].equalsIgnoreCase("board") || array[0].equalsIgnoreCase("lb") || array[0].equalsIgnoreCase("scoreboard")) {
            if (!player.hasPermission("lobbygames.admin")) {
                player.sendMessage(sp);
                return true;
            }
            if (array.length == 1) {
                player.sendMessage(this.plugin.getConfigString("cmd-lb-title", "§3§l/lg leaderboard 子命令："));
                player.sendMessage(this.plugin.getConfigString("cmd-lb-create-cmd", "§b/lg leaderboard create <游戏>"));
                player.sendMessage(this.plugin.getConfigString("cmd-lb-clearplayer-cmd", "§b/lg leaderboard clearplayer <玩家> <游戏>"));
                player.sendMessage(this.plugin.getConfigString("cmd-lb-clear-cmd", "§b/lg leaderboard clear <游戏>"));
                player.sendMessage(this.plugin.getConfigString("cmd-lb-delete-cmd", "§b/lg leaderboard delete <游戏> [id]"));
                player.sendMessage(this.plugin.getConfigString("cmd-lb-note", "§7此命令仅适用于全局排行榜，不适用于竞技场特定的排行榜。"));
                return true;
            }
            if (array[1].equalsIgnoreCase("create")) {
                if (array.length >= 3) {
                    final GameType gameType5 = this.getGameType(array[2]);
                    if (gameType5 == null) {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-invalid-game-type", "§4错误：§c'%game%' 不是有效的游戏类型！").replace("%game%", array[2]));
                        return true;
                    }
                    final String outgoingAliases2 = GameUtils.outgoingAliases(gameType5, this.plugin);
                    final Location location = player.getLocation();
                    location.setYaw(0.0f);
                    location.setPitch(0.0f);
                    final Leaderboard leaderboard = new Leaderboard(this.plugin, gameType5, location);
                    final HashMap<GameType, List<Leaderboard>> globalLeaderboards = this.plugin.getGlobalLeaderboards();
                    if (!globalLeaderboards.containsKey(gameType5)) {
                        final ArrayList value = new ArrayList();
                        value.add(leaderboard);
                        globalLeaderboards.put(gameType5, value);
                    }
                    else {
                        if (((List)globalLeaderboards.get(gameType5)).size() > 0) {
                            final Iterator iterator2 = globalLeaderboards.get(gameType5).iterator();
                            while (iterator2.hasNext()) {
                                if (((Leaderboard)iterator2.next()).getLocation().equals((Object)location)) {
                                    player.sendMessage(this.plugin.getConfigString("cmd-error-leaderboard-exists", "§4错误：§c此位置已经有一个排行榜！"));
                                    return true;
                                }
                            }
                            leaderboard.setEntries(((List<Leaderboard>)globalLeaderboards.get(gameType5)).get(0).getEntries());
                        }
                        ((List<Leaderboard>)globalLeaderboards.get(gameType5)).add(leaderboard);
                    }
                    leaderboard.updateDisplay();
                    this.plugin.saveArenas();
                    String gameName = outgoingAliases2.toString().toUpperCase().charAt(0) + outgoingAliases2.toString().substring(1);
                    player.sendMessage(this.plugin.getConfigString("cmd-lb-create-success", "§a成功为 %game% 添加了全局排行榜").replace("%game%", gameName));
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-usage-lb-create", "§4用法：§c/lg leaderboard create <游戏>"));
                }
            }
            else if (array[1].equalsIgnoreCase("clearplayer")) {
                if (array.length >= 4) {
                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(array[2]);
                    if (offlinePlayer != null) {
                        final GameType gameType6 = this.getGameType(array[3]);
                        if (gameType6 != null) {
                            final List<Leaderboard> list = this.plugin.getGlobalLeaderboards().get(gameType6);
                            if (list != null) {
                                for (Leaderboard leaderboard2 : list) {
                                    boolean b = false;
                                    LeaderboardEntry[] array2;
                                    for (int length2 = (array2 = leaderboard2.getEntries().toArray(new LeaderboardEntry[leaderboard2.getEntries().size()])).length, j = 0; j < length2; ++j) {
                                        final LeaderboardEntry leaderboardEntry = array2[j];
                                        final String lowerCase = leaderboardEntry.getDisplayName().replaceAll("§.", "").trim().toLowerCase();
                                        if (leaderboardEntry.getUniqueId() == null || offlinePlayer.getUniqueId().equals(leaderboardEntry.getUniqueId())) {
                                            if (lowerCase.equalsIgnoreCase(offlinePlayer.getName()) || lowerCase.endsWith(" " + offlinePlayer.getName().toLowerCase())) {
                                                leaderboard2.getEntries().remove(leaderboardEntry);
                                                b = true;
                                            }
                                        }
                                    }
                                    if (b) {
                                        leaderboard2.updateDisplay();
                                    }
                                }
                            }
                            player.sendMessage(this.plugin.getConfigString("cmd-lb-clearplayer-success", "§a已从 %game% 排行榜中移除 %player%！").replace("%player%", offlinePlayer.getName()).replace("%game%", GameUtils.outgoingAliases(gameType6, this.plugin)));
                        }
                        else {
                            player.sendMessage(this.plugin.getConfigString("cmd-error-invalid-game-type", "§4错误：§c'%game%' 不是有效的游戏类型！").replace("%game%", array[3]));
                        }
                    }
                    else {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-player-not-found", "§4错误：§c找不到玩家 '%player%'！").replace("%player%", array[2]));
                    }
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-usage-lb-clearplayer", "§4用法：§c/lg leaderboard clearplayer <玩家> <游戏>"));
                }
            }
            else if (array[1].equalsIgnoreCase("clear") || array[1].equalsIgnoreCase("clearboard")) {
                if (array.length >= 3) {
                    if (array.length >= 4) {
                        final Arena arena6 = this.getArena(array[2], array[3], player);
                        if (arena6 == null) {
                            return true;
                        }
                        if (arena6.getLeaderboard() == null) {
                            player.sendMessage(this.plugin.getConfigString("cmd-error-no-local-leaderboard", "§4错误：§c此竞技场没有本地排行榜！"));
                        }
                        else {
                            arena6.getLeaderboard().setEntries(new ArrayList<LeaderboardEntry>());
                            player.sendMessage(this.plugin.getConfigString("cmd-lb-clear-local-success", "§a成功清除此竞技场的本地排行榜！"));
                        }
                    }
                    else {
                        final GameType gameType7 = this.getGameType(array[2]);
                        if (gameType7 == null) {
                            player.sendMessage(this.plugin.getConfigString("cmd-error-invalid-game-type", "§4错误：§c'%game%' 不是有效的游戏类型！").replace("%game%", array[2]));
                            return true;
                        }
                        final List list2 = this.plugin.getGlobalLeaderboards().get(gameType7);
                        if (list2 != null) {
                            final Iterator iterator4 = list2.iterator();
                            while (iterator4.hasNext()) {
                                ((Leaderboard)iterator4.next()).setEntries(new ArrayList<LeaderboardEntry>());
                            }
                        }
                        final String outgoingAliases3 = GameUtils.outgoingAliases(gameType7, this.plugin);
                        String gameName = outgoingAliases3.toString().toUpperCase().charAt(0) + outgoingAliases3.toString().substring(1);
                        player.sendMessage(this.plugin.getConfigString("cmd-lb-clear-global-success", "§a成功清除 %game% 的全局排行榜").replace("%game%", gameName));
                    }
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-usage-lb-clear", "§4用法：§c/lg leaderboard clear <游戏> [id]"));
                }
            }
            else if (array[1].equalsIgnoreCase("delete")) {
                if (array.length >= 3) {
                    final GameType gameType8 = this.getGameType(array[2]);
                    if (gameType8 == null) {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-invalid-game-type", "§4错误：§c'%game%' 不是有效的游戏类型！").replace("%game%", array[2]));
                        return true;
                    }
                    int int2 = 1;
                    if (array.length >= 4) {
                        try {
                            int2 = Integer.parseInt(array[3]);
                        }
                        catch (final Exception ex2) {
                            player.sendMessage(this.plugin.getConfigString("cmd-error-not-number", "§4错误：§c'%value%' 不是数字！").replace("%value%", array[3]));
                            return true;
                        }
                    }
                    final List list3 = this.plugin.getGlobalLeaderboards().get(gameType8);
                    if (list3 == null) {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-no-global-leaderboards", "§4错误：§c此游戏类型没有全局排行榜！"));
                        return true;
                    }
                    if (list3.size() < int2 || int2 <= 0) {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-leaderboard-not-exist", "§4错误：§c全局排行榜 #%id% 不存在于此游戏类型！").replace("%id%", String.valueOf(int2)));
                        return true;
                    }
                    ((Leaderboard)list3.get(int2 - 1)).remove();
                    list3.remove(int2 - 1);
                    this.plugin.saveArenas();
                    player.sendMessage(this.plugin.getConfigString("cmd-lb-delete-global-success", "§a成功删除 %game% 的全局排行榜").replace("%game%", GameUtils.outgoingAliases(gameType8, this.plugin)));
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-usage-lb-delete", "§4用法：§c/lg leaderboard delete <游戏> [id]"));
                }
            }
            else {
                player.sendMessage(this.plugin.getConfigString("cmd-error-unknown-subcommand", "§c未知的子命令。"));
            }
        }
        else if (array[0].equalsIgnoreCase("clearboard") || array[0].equalsIgnoreCase("clearleaderboard") || array[0].equalsIgnoreCase("clearscoreboard")) {
            if (player.hasPermission("lobbygames.admin")) {
                if (array.length >= 3) {
                    final Arena arena7 = this.getArena(array, player);
                    if (arena7 == null) {
                        return true;
                    }
                    if (arena7.getLeaderboard() != null) {
                        arena7.getLeaderboard().setEntries(new ArrayList<LeaderboardEntry>());
                        player.sendMessage(this.plugin.getConfigString("cmd-lb-delete-success", "§a成功删除此竞技场的本地排行榜！"));
                    }
                    else {
                        player.sendMessage(this.plugin.getConfigString("cmd-error-no-local-leaderboard", "§4错误：§c此竞技场没有本地排行榜！"));
                    }
                }
                else {
                    player.sendMessage(this.plugin.getConfigString("cmd-usage-clearboard", "§4用法：§c/lg clearboard <游戏> <竞技场ID> §7(重置排行榜)"));
                }
            }
            else {
                player.sendMessage(sp);
            }
        }
        else {
            player.sendMessage(this.plugin.getConfigString("cmd-error-unknown-command", "§c未知的命令。"));
        }
        return true;
    }
    
    public List<String> onTabComplete(final CommandSender commandSender, final Command command, final String s, final String[] array) {
        final ArrayList list = new ArrayList();
        if (commandSender.hasPermission("lobbygames.command")) {
            if (array.length == 1) {
                final String[] array2 = { "join", "quit", "list", "set", "restart", "tp" };
                for (int i = 0; i < array2.length; ++i) {
                    list.add(array2[i]);
                }
                if (commandSender.hasPermission("lobbygames.kickplayer")) {
                    list.add("kick");
                }
                if (commandSender.hasPermission("lobbygames.admin")) {
                    list.add("reload");
                    list.add("clearstands");
                    list.add("create");
                    list.add("leaderboard");
                    list.add("delete");
                }
            }
            else if (array.length == 2) {
                Label_0402: {
                    Label_0363: {
                        final String lowerCase;
                        switch (lowerCase = array[0].toLowerCase()) {
                            case "leaderboard": {
                                break Label_0363;
                            }
                            case "teleport": {
                                break;
                            }
                            case "create": {
                                break;
                            }
                            case "delete": {
                                break;
                            }
                            case "lb": {
                                break Label_0363;
                            }
                            case "tp": {
                                break;
                            }
                            case "list": {
                                break;
                            }
                            default:
                                break Label_0402;
                        }
                        return this.tabgamelist;
                    }
                    list.add("create");
                    list.add("clear");
                    list.add("clearplayer");
                    list.add("delete");
                    return list;
                }
                if (array[0].equalsIgnoreCase("join") && this.isJoinCmdEnabled()) {
                    return this.tabgamelist;
                }
            }
            else if (array.length == 3) {
                if ((array[0].equalsIgnoreCase("leaderboard") || array[0].equalsIgnoreCase("lb")) && !array[1].equalsIgnoreCase("clearplayer")) {
                    return this.tabgamelist;
                }
                if (array[0].equalsIgnoreCase("delete")) {
                    final GameType gameType = this.getGameType(array[1]);
                    if (gameType != null) {
                        for (int size = this.plugin.getArenas(gameType).size(), j = 1; j <= size; ++j) {
                            list.add("" + j);
                        }
                    }
                }
            }
            else if (array.length == 4) {
                if ((array[0].equalsIgnoreCase("leaderboard") || array[0].equalsIgnoreCase("lb")) && array[1].equalsIgnoreCase("clearplayer")) {
                    return this.tabgamelist;
                }
            }
            else if (array.length == 1) {
                final String[] array3 = { "join", "quit", "restart" };
                for (int k = 0; k < array3.length; ++k) {
                    list.add(array3[k]);
                }
            }
            else if (array.length == 2) {
                return this.tabgamelist;
            }
        }
        return list;
    }
    
    // This helper class was generated by Procyon to approximate the behavior of an
    // 'invokedynamic' instruction that it doesn't know how to interpret.
    private static final class ProcyonInvokeDynamicHelper_1
    {
        private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
        private static MethodHandle handle;
        private static volatile int fence;
        
        private static MethodHandle handle() {
            final MethodHandle handle = ProcyonInvokeDynamicHelper_1.handle;
            if (handle != null)
                return handle;
            return ProcyonInvokeDynamicHelper_1.ensureHandle();
        }
        
        private static MethodHandle ensureHandle() {
            ProcyonInvokeDynamicHelper_1.fence = 0;
            MethodHandle handle = ProcyonInvokeDynamicHelper_1.handle;
            if (handle == null) {
                MethodHandles.Lookup lookup = ProcyonInvokeDynamicHelper_1.LOOKUP;
                try {
                    handle = ((CallSite)StringConcatFactory.makeConcatWithConstants(lookup, "makeConcatWithConstants", MethodType.methodType(String.class, int.class), " \u0001")).dynamicInvoker();
                }
                catch (Throwable t) {
                    throw new UndeclaredThrowableException(t);
                }
                ProcyonInvokeDynamicHelper_1.fence = 1;
                ProcyonInvokeDynamicHelper_1.handle = handle;
                ProcyonInvokeDynamicHelper_1.fence = 0;
            }
            return handle;
        }
        
        private static String invoke(int p0) {
            try {
                return (String) ProcyonInvokeDynamicHelper_1.handle().invokeExact(p0);
            }
            catch (Throwable t) {
                throw new UndeclaredThrowableException(t);
            }
        }
    }
}
