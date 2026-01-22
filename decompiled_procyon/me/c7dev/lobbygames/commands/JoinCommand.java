// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.commands;

import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.util.GameUtils;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.command.CommandExecutor;

public class JoinCommand implements CommandExecutor
{
    private LobbyGames plugin;
    
    public JoinCommand(final LobbyGames plugin) {
        this.plugin = plugin;
        plugin.getCommand("pgjoin").setExecutor((CommandExecutor)this);
    }
    
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] array) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(this.plugin.getConfigString("cmd-error-must-be-player", "§c你必须是玩家才能执行此操作！"));
            return true;
        }
        final Player player = (Player)commandSender;
        if (!this.plugin.getConfig().getBoolean("alias-commands-enabled", true)) {
            if (player.hasPermission("lobbygames.admin")) {
                player.sendMessage(this.plugin.getConfigString("cmd-error-alias-disabled", "§4错误：§cLobbyGames 别名命令在配置中已禁用！(alias-commands-enabled)"));
            }
            return true;
        }
        if (s.equalsIgnoreCase("lgjoin")) {
            player.sendMessage(this.plugin.getConfigString("cmd-error-invalid-game-type", "§4错误：§c'%game%' 不是有效的游戏类型！").replace("%game%", s));
            return true;
        }
        final GameType value = GameType.valueOf(GameUtils.incomingAliases(s, this.plugin).toUpperCase().replaceAll("LOBBYGAMES:", ""));
        if (value == null) {
            player.sendMessage(this.plugin.getConfigString("cmd-error-invalid-game-type", "§4错误：§c'%game%' 不是有效的游戏类型！").replace("%game%", s));
            return true;
        }
        int int1 = -1;
        if (array.length >= 1 && !array[0].equalsIgnoreCase("tac")) {
            try {
                int1 = Integer.parseInt(array[0]);
            }
            catch (final Exception ex) {
                player.sendMessage(this.plugin.getConfigString("cmd-error-not-number", "§4错误：§c'%value%' 不是数字！").replace("%value%", array[0]));
                return true;
            }
        }
        this.plugin.joinPlayer(player, value, int1);
        return true;
    }
}
