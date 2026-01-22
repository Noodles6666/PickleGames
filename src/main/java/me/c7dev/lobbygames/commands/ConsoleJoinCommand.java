// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.commands;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.util.GameType;
import java.util.ArrayList;
import java.util.List;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;

public class ConsoleJoinCommand implements CommandExecutor, TabCompleter
{
    private LobbyGames plugin;
    private String noperm;
    private List<String> tabgamelist;
    
    public ConsoleJoinCommand(final LobbyGames plugin) {
        this.tabgamelist = new ArrayList<String>();
        this.plugin = plugin;
        this.noperm = plugin.getConfigString("no-permission", "§cYou don't have permission!");
        plugin.getCommand("pgjoinplayer").setExecutor((CommandExecutor)this);
        GameType[] values;
        for (int length = (values = GameType.values()).length, i = 0; i < length; ++i) {
            this.tabgamelist.add(GameUtils.getConfigName(values[i]).toLowerCase());
        }
    }
    
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] array) {
        if (commandSender.hasPermission("lobbygames.forcejoin") || commandSender.hasPermission("lobbygames.admin")) {
            if (array.length >= 2) {
                final Player player = Bukkit.getPlayer(array[0]);
                if (player == null) {
                    commandSender.sendMessage(this.plugin.getConfigString("console-error-player-offline", "§4错误：§c%player% 不在线！").replace("%player%", array[0]));
                    return true;
                }
                final GameType value = GameType.valueOf(GameUtils.incomingAliases(array[1], this.plugin).toUpperCase());
                if (value == null) {
                    commandSender.sendMessage(this.plugin.getConfigString("cmd-error-invalid-game-type", "§4错误：§c'%game%' 不是有效的游戏类型！").replace("%game%", array[1]));
                    return true;
                }
                int int1 = -1;
                if (array.length >= 3 && !array[2].equalsIgnoreCase("tac")) {
                    try {
                        int1 = Integer.parseInt(array[2]);
                    }
                    catch (final Exception ex) {
                        commandSender.sendMessage(this.plugin.getConfigString("cmd-error-not-number", "§4错误：§c'%value%' 不是数字！").replace("%value%", array[2]));
                        return true;
                    }
                }
                this.plugin.joinPlayer(player, value, int1, false);
            }
            else {
                commandSender.sendMessage(this.plugin.getConfigString("console-usage-forcejoin", "§4用法：§c/%command% <玩家> <游戏> [竞技场ID]").replace("%command%", s.toLowerCase()));
            }
        }
        else {
            commandSender.sendMessage(this.noperm);
        }
        return true;
    }
    
    public List<String> onTabComplete(final CommandSender commandSender, final Command command, final String s, final String[] array) {
        if (array.length == 2) {
            return this.tabgamelist;
        }
        return new ArrayList<String>();
    }
}
