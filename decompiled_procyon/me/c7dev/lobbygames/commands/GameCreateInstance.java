// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.commands;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import me.c7dev.lobbygames.util.Leaderboard;
import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import me.c7dev.lobbygames.util.GameType;

public class GameCreateInstance
{
    private GameType gt;
    private Player player;
    private int instr;
    private Location l1;
    private Location l2;
    private Location s1;
    private Location scoreloc;
    private Location special1;
    private Location special2;
    private LobbyGames plugin;
    
    public GameCreateInstance(final Player player, final GameType gt, final LobbyGames plugin) {
        this.instr = 0;
        if (!player.hasPermission("lobbygames.admin")) {
            return;
        }
        this.plugin = plugin;
        String locationName = (gt == GameType.CLICKER) ? "中心点" : "左下角";
        (this.player = player).sendMessage(this.plugin.getConfigString("create-editing-mode", "\n§b你现在处于编辑模式！使用 §n/lg quit§b 退出。"));
        this.player.sendMessage(this.plugin.getConfigString("create-step1", "§2步骤 1：§a前往竞技场的 §b%location%§a 并运行 §b/lg set").replace("%location%", locationName));
        this.gt = gt;
    }
    
    public static Location blockLocation(final Location location) {
        return GameUtils.blockLocation(location);
    }
    
    public static int getMinLength(final Location location, final Location location2) {
        if (location.getBlockY() == location2.getBlockY()) {
            return Math.min(Math.abs(location2.getBlockX() - location.getBlockX()), Math.abs(location2.getBlockZ() - location.getBlockZ()));
        }
        if (location.getBlockX() == location2.getBlockX()) {
            return Math.min(Math.abs(location2.getBlockY() - location.getBlockY()), Math.abs(location2.getBlockZ() - location.getBlockZ()));
        }
        return Math.min(Math.abs(location2.getBlockX() - location.getBlockX()), Math.abs(location2.getBlockY() - location.getBlockY()));
    }
    
    public static int getMaxLength(final Location location, final Location location2) {
        if (location.getBlockY() == location2.getBlockY()) {
            return Math.max(Math.abs(location2.getBlockX() - location.getBlockX()), Math.abs(location2.getBlockZ() - location.getBlockZ()));
        }
        if (location.getX() == location2.getX()) {
            return Math.max(Math.abs(location2.getBlockY() - location.getBlockY()), Math.abs(location2.getBlockZ() - location.getBlockZ()));
        }
        return Math.max(Math.abs(location2.getBlockX() - location.getBlockX()), Math.abs(location2.getBlockY() - location.getBlockY()));
    }
    
    public boolean isValid() {
        return this.player != null && this.player.isOnline() && this.gt != null && Arena.isValidCoords(this.l1, this.l2) && this.l1 != null && (this.l2 != null || this.gt == GameType.CLICKER) && (this.gt == GameType.MINESWEEPER || this.gt == GameType.SOCCER || this.s1 != null) && (this.gt != GameType.SPLEEF || !Arena.isInBoundsXZ(this.s1, this.l1, this.l2)) && (this.gt != GameType.CLICKER || this.l1.getBlockX() != this.s1.getBlockX() || this.l1.getBlockZ() != this.s1.getBlockZ());
    }
    
    public Location getLocation1() {
        return this.l1;
    }
    
    public Location getLocation2() {
        return this.l2;
    }
    
    public Location getSpawn1() {
        return this.s1;
    }
    
    public Location getScoreboardLocation() {
        return this.scoreloc;
    }
    
    public GameType getGameType() {
        return this.gt;
    }
    
    public int save() {
        if (this.gt == GameType.CLICKER) {
            this.l1.setYaw(0.0f);
            this.l1.setPitch(0.0f);
            this.l2 = this.l1;
        }
        if (this.l1 == null || this.l2 == null || this.s1 == null) {
            this.player.sendMessage(this.plugin.getConfigString("create-error-points-not-set", "§4错误：§c并非所有点都已设置！"));
            return -2;
        }
        if (!this.isValid() && this.gt != GameType.CLICKER) {
            return -1;
        }
        if (this.gt == GameType.SPLEEF) {
            this.l1.setY(this.l1.getY() - 1.0);
            this.l2.setY(this.l2.getY() - 1.0);
        }
        final int n = this.plugin.getArenas(this.gt).size() + 1;
        if (this.plugin.saveArena(new Arena(n, this.gt, this.l1, this.l2, this.s1, (this.scoreloc == null) ? null : new Leaderboard(this.plugin, this.gt, this.scoreloc), this.special1, this.special2))) {
            return n;
        }
        return -1;
    }
    
    public void quit() {
        this.instr = -1;
    }
    
    public void playParticles() {
        if (LobbyGames.SERVER_VERSION < 12 || this.l1 == null) {
            return;
        }
        final Location clone = this.l1.clone();
        clone.setX((double)clone.getBlockX());
        clone.setY((double)clone.getBlockY());
        clone.setZ((double)clone.getBlockZ());
        new BukkitRunnable() {
            public void run() {
                if (!GameCreateInstance.this.player.isOnline() || GameCreateInstance.this.instr == -1 || GameCreateInstance.this.l1 == null) {
                    this.cancel();
                    return;
                }
                final Location location = (GameCreateInstance.this.l2 == null) ? ((GameCreateInstance.this.gt == GameType.CLICKER) ? GameCreateInstance.this.l1.clone() : GameCreateInstance.this.player.getLocation()) : GameCreateInstance.this.l2.clone();
                location.setX((double)location.getBlockX());
                location.setY((double)location.getBlockY());
                location.setZ((double)location.getBlockZ());
                final Location clone = GameCreateInstance.this.l1.clone();
                if (Math.abs(clone.getBlockX() - location.getBlockX()) <= 44 && Math.abs(clone.getBlockZ() - location.getBlockZ()) <= 44) {
                    if (GameCreateInstance.this.l1.getBlockY() == location.getBlockY()) {
                        if (location.getZ() <= clone.getZ()) {
                            clone.setZ(clone.getZ() + 1.0);
                        }
                        else {
                            location.setZ(location.getZ() + 1.0);
                        }
                        if (location.getX() < clone.getX()) {
                            clone.setX(clone.getX() + 1.0);
                        }
                        else {
                            location.setX(location.getX() + 1.0);
                        }
                        GameUtils.particleLine(clone, new Location(clone.getWorld(), location.getX(), clone.getY(), clone.getZ()));
                        GameUtils.particleLine(clone, new Location(clone.getWorld(), clone.getX(), clone.getY(), location.getZ()));
                        GameUtils.particleLine(location, new Location(clone.getWorld(), clone.getX(), location.getY(), location.getZ()));
                        GameUtils.particleLine(location, new Location(clone.getWorld(), location.getX(), location.getY(), clone.getZ()));
                    }
                    else if (GameCreateInstance.this.l1.getBlockX() == location.getBlockX()) {
                        location.setY(location.getY() + 1.0);
                        if (location.getZ() <= clone.getZ()) {
                            clone.setZ(clone.getZ() + 1.0);
                        }
                        else {
                            location.setZ(location.getZ() + 1.0);
                        }
                        GameUtils.particleLine(clone, new Location(clone.getWorld(), clone.getX(), location.getY(), clone.getZ()));
                        GameUtils.particleLine(clone, new Location(clone.getWorld(), clone.getX(), clone.getY(), location.getZ()));
                        GameUtils.particleLine(location, new Location(clone.getWorld(), location.getX(), clone.getY(), location.getZ()));
                        GameUtils.particleLine(location, new Location(clone.getWorld(), location.getX(), location.getY(), clone.getZ()));
                    }
                    else if (GameCreateInstance.this.l1.getBlockZ() == location.getBlockZ()) {
                        location.setY(location.getY() + 1.0);
                        if (location.getX() < clone.getX()) {
                            clone.setX(clone.getX() + 1.0);
                        }
                        else {
                            location.setX(location.getX() + 1.0);
                        }
                        GameUtils.particleLine(clone, new Location(clone.getWorld(), location.getX(), clone.getY(), clone.getZ()));
                        GameUtils.particleLine(clone, new Location(clone.getWorld(), clone.getX(), location.getY(), clone.getZ()));
                        GameUtils.particleLine(location, new Location(clone.getWorld(), clone.getX(), location.getY(), location.getZ()));
                        GameUtils.particleLine(location, new Location(clone.getWorld(), location.getX(), clone.getY(), location.getZ()));
                    }
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 5L);
    }
    
    public void setLocation(final Location specialLoc2) {
        if (!this.player.hasPermission("lobbygames.admin")) {
            return;
        }
        switch (this.instr) {
            case 0: {
                if (this.setLocation1(specialLoc2)) {
                    this.player.sendMessage("\n§7已将左下角设置为 " + this.l1.getBlockX() + ", " + this.l1.getBlockY() + ", " + this.l1.getBlockZ());
                    if (this.gt == GameType.CLICKER) {
                        this.instr += 2;
                        this.player.sendMessage(this.plugin.getConfigString("create-step2-spawn", "§2步骤 2：§a前往竞技场的 §b出生点§a 并运行 §b/lg set"));
                    }
                    else {
                        ++this.instr;
                        this.player.sendMessage(this.plugin.getConfigString("create-step2-topright", "§2步骤 2：§a前往竞技场的 §b右上角§a 并运行 §b/lg set"));
                    }
                    this.playParticles();
                    break;
                }
                break;
            }
            case 1: {
                if (this.setLocation2(specialLoc2)) {
                    this.player.sendMessage("\n§7已将右上角设置为 " + this.l2.getBlockX() + ", " + this.l2.getBlockY() + ", " + this.l2.getBlockZ());
                    this.player.sendMessage(this.plugin.getConfigString("create-step3-spawn", "§2步骤 3：§a前往竞技场的 §b出生点§a 并运行 §b/lg set"));
                    ++this.instr;
                    break;
                }
                break;
            }
            case 2: {
                if (!this.setSpawn1(specialLoc2)) {
                    break;
                }
                if (this.gt == GameType.SOCCER) {
                    this.player.sendMessage(this.plugin.getConfigString("create-step4-soccer-net", "§2步骤 4：§a前往 §b红色足球网§a 的 §b右上角§a 并运行 §b/lg set"));
                    ++this.instr;
                    break;
                }
                this.player.sendMessage("\n§7已将出生点设置为 " + this.s1.getBlockX() + ", " + this.s1.getBlockY() + ", " + this.s1.getBlockZ() + " (偏航角: " + Math.round(this.s1.getYaw()) + ", 俯仰角: " + Math.round(this.s1.getPitch()) + ")");
                if (!this.gt.isMultiplayer()) {
                    this.player.sendMessage(this.plugin.getConfigString("create-locations-set-leaderboard", "§2位置已设置！§a要为此竞技场添加本地排行榜，使用 §b/lg set leaderboard"));
                }
                this.player.sendMessage(this.plugin.getConfigString("create-important-save", "§c§l重要：§a不要忘记使用 §b/lg save§a 保存此竞技场！"));
                this.instr = -1;
                break;
            }
            case 3: {
                if (this.setSpecialLoc1(specialLoc2)) {
                    this.player.sendMessage(this.plugin.getConfigString("create-step5-net-corner", "§2步骤 5：§a前往同一球网的 §b左下角§a 并运行 §b/lg set"));
                    ++this.instr;
                    break;
                }
                break;
            }
            case 4: {
                if (this.setSpecialLoc2(specialLoc2)) {
                    String leaderboardMsg = (this.gt != GameType.SOCCER) ? this.plugin.getConfigString("create-add-leaderboard-hint", "§a要为此竞技场添加本地排行榜，使用 §b/lg set leaderboard") : "";
                    this.player.sendMessage(this.plugin.getConfigString("create-locations-set", "§2位置已设置！") + " " + leaderboardMsg);
                    this.player.sendMessage(this.plugin.getConfigString("create-important-save", "§c§l重要：§a不要忘记使用 §b/lg save§a 保存此竞技场！"));
                    this.instr = -1;
                    break;
                }
                break;
            }
            default: {
                this.player.sendMessage(this.plugin.getConfigString("create-all-set-save", "§a所有位置已设置，确保使用 §b/lg save§a 保存新竞技场"));
                break;
            }
        }
    }
    
    public boolean checkCoords(final Location location, final Location location2) {
        if (location2 == null) {
            return true;
        }
        if (!Arena.isValidCoords(location2, location)) {
            this.player.sendMessage(this.plugin.getConfigString("create-error-invalid-coords", "§4错误：§c此坐标无效。坐标必须构成同一世界中平面正方形的 2 个角。"));
            return false;
        }
        if (!this.gt.canSupportVerticalArena() && location.getBlockY() != location2.getBlockY()) {
            this.player.sendMessage(this.plugin.getConfigString("create-error-must-be-flat", "§4错误：§c此游戏类型要求竞技场必须在地面上平铺。"));
            return false;
        }
        if (this.gt == GameType.SUDOKU) {
            if (getMinLength(location, location2) != getMaxLength(location, location2) || getMinLength(location, location2) != 8) {
                this.player.sendMessage(this.plugin.getConfigString("create-error-sudoku-size", "§4错误：§c数独必须是 9x9 竞技场"));
                return false;
            }
        }
        else if (this.gt == GameType.TICTACTOE) {
            if (getMinLength(location, location2) != getMaxLength(location, location2) || getMinLength(location, location2) != 2) {
                this.player.sendMessage(this.plugin.getConfigString("create-error-tictactoe-size", "§4错误：§c井字棋必须是 3x3 竞技场"));
                return false;
            }
        }
        else if (this.gt == GameType.POOL) {
            final int maxLength = getMaxLength(location, location2);
            if (getMinLength(location, location2) != 2 || maxLength < 3 || maxLength > 4) {
                this.player.sendMessage(this.plugin.getConfigString("create-error-pool-size", "§4错误：§c台球必须是 3x4 或 3x5 大小！"));
                return false;
            }
        }
        else if (this.gt == GameType.CONNECT4) {
            if (location2.getBlockY() == location.getBlockY()) {
                this.player.sendMessage(this.plugin.getConfigString("create-error-must-be-vertical", "§4错误：§c此游戏必须是垂直的（在墙上）！"));
                return false;
            }
            final int maxLength2 = getMaxLength(location, location2);
            final int minLength = getMinLength(location, location2);
            if (maxLength2 != 6 || minLength != 5) {
                if (minLength < 5) {
                    this.player.sendMessage(this.plugin.getConfigString("create-error-min-size", "§4错误：§c最小宽度和高度为 6 格！（推荐大小为 7x6）"));
                    return false;
                }
                if (maxLength2 > 49) {
                    this.player.sendMessage(this.plugin.getConfigString("create-error-max-size", "§4错误：§c最大宽度和高度为 50 格！（推荐大小为 7x6）"));
                    return false;
                }
            }
        }
        else {
            int minSize = (this.gt == GameType.T048) ? 3 : 6;
            int minBlocks = (this.gt == GameType.T048) ? 4 : 7;
            if (getMinLength(location2, location) < minSize) {
                this.player.sendMessage(this.plugin.getConfigString("create-error-too-small", "§4错误：§c此游戏的宽度和高度必须至少为 %size% 格。").replace("%size%", String.valueOf(minBlocks)));
                return false;
            }
            int maxSize = (this.gt == GameType.SPLEEF) ? 59 : 49;
            String maxBlocks = (this.gt == GameType.SPLEEF) ? "60" : "50";
            if (getMaxLength(location2, location) > maxSize && this.gt != GameType.SOCCER) {
                this.player.sendMessage(this.plugin.getConfigString("create-error-too-large", "§4错误：§c竞技场宽度不能大于 %size% 格！").replace("%size%", maxBlocks));
                return false;
            }
        }
        return true;
    }
    
    public boolean setLocation1(final Location l1) {
        if (!this.checkCoords(l1, this.l2)) {
            return false;
        }
        blockLocation(l1);
        this.l1 = l1;
        return true;
    }
    
    public boolean setLocation2(final Location location) {
        if (this.gt == GameType.CLICKER) {
            this.player.sendMessage(this.plugin.getConfigString("create-note-no-second-location", "§6注意：§e此游戏不需要第二个位置！"));
            return false;
        }
        if (!this.checkCoords(location, this.l1)) {
            return false;
        }
        this.l2 = blockLocation(location);
        return true;
    }
    
    public boolean setSpecialLoc1(final Location location) {
        if (this.gt == GameType.SOCCER) {
            if (this.l1 != null && this.l2 != null) {
                location.add(0.0, 1.0, 0.0);
                if (location.getBlockY() - this.l1.getBlockY() >= 3) {
                    if (!Arena.isInBoundsXZ(location, this.l1, this.l2)) {
                        this.special1 = blockLocation(location);
                        return true;
                    }
                    this.player.sendMessage(this.plugin.getConfigString("create-error-net-outside", "§4错误：§c足球网的角必须在竞技场选区之外！"));
                }
                else {
                    this.player.sendMessage(this.plugin.getConfigString("create-error-net-height", "§4错误：§c球网必须至少 3 格高！（此位置应该离地面有一定高度）"));
                }
            }
            else {
                this.player.sendMessage(this.plugin.getConfigString("create-error-corners-first", "§4错误：§c必须先定义竞技场的角！"));
            }
            return false;
        }
        this.player.sendMessage(this.plugin.getConfigString("create-error-no-special-location", "§4错误：§c此游戏类型不使用此位置。"));
        return true;
    }
    
    public boolean setSpecialLoc2(final Location location) {
        if (this.gt == GameType.SOCCER) {
            if (this.l1 != null && this.l2 != null) {
                if (location.getBlockY() == this.l1.getBlockY()) {
                    if (!Arena.isInBoundsXZ(location, this.l1, this.l2)) {
                        if (!this.neighborsArena(location) && this.special1 != null && !this.neighborsArena(this.special1)) {
                            this.player.sendMessage(this.plugin.getConfigString("create-error-net-neighbor", "§4错误：§c球网位置之一必须与竞技场相邻！"));
                            return false;
                        }
                        this.special2 = blockLocation(location);
                        return true;
                    }
                    else {
                        this.player.sendMessage(this.plugin.getConfigString("create-error-net-outside", "§4错误：§c足球网的角必须在竞技场选区之外！"));
                    }
                }
                else {
                    this.player.sendMessage(this.plugin.getConfigString("create-error-same-y-level", "§4错误：§c此位置必须与竞技场地面处于相同的 Y 值！"));
                }
            }
            else {
                this.player.sendMessage(this.plugin.getConfigString("create-error-corners-first", "§4错误：§c必须先定义竞技场的角！"));
            }
            return false;
        }
        this.player.sendMessage(this.plugin.getConfigString("create-error-no-special-location", "§4错误：§c此游戏类型不使用此位置。"));
        return true;
    }
    
    private boolean neighborsArena(final Location location) {
        return Math.abs(location.getBlockX() - this.l1.getBlockX()) == 1 || Math.abs(location.getBlockZ() - this.l1.getBlockZ()) == 1 || Math.abs(location.getBlockX() - this.l2.getBlockX()) == 1 || Math.abs(location.getBlockZ() - this.l2.getBlockZ()) == 1;
    }
    
    public boolean setSpawn1(final Location s1) {
        if (this.gt == GameType.SPLEEF || this.gt == GameType.SOCCER) {
            if (this.l1 == null || this.l2 == null) {
                this.player.sendMessage(this.plugin.getConfigString("create-error-corners-before-spawn", "§4错误：§c必须先设置竞技场的角才能设置出生点！"));
                return false;
            }
            if (Arena.isInBoundsXZ(s1, this.l1, this.l2)) {
                this.player.sendMessage(this.plugin.getConfigString("create-error-spawn-inside", "§4错误：§c出生点不能在竞技场内部！（这是被淘汰玩家传送的位置。）"));
                return false;
            }
        }
        else if (this.gt == GameType.CLICKER) {
            if (this.l1 == null) {
                this.player.sendMessage(this.plugin.getConfigString("create-error-center-before-spawn", "§4错误：§c必须先设置竞技场中心点才能设置点击游戏出生点！"));
                return false;
            }
            if (s1.getBlockX() == this.l1.getBlockX() && s1.getBlockZ() == this.l1.getBlockZ()) {
                this.player.sendMessage(this.plugin.getConfigString("create-error-spawn-same-as-center", "§4错误：§c出生点不能与中心点相同！"));
                return false;
            }
        }
        else if (this.gt == GameType.T048 || this.gt == GameType.SNAKE) {
            if (this.gt == GameType.SNAKE) {
                s1.setX(s1.getBlockX() + 0.5);
                s1.setZ(s1.getBlockZ() + 0.5);
            }
            if (this.l1.getBlockY() == this.l2.getBlockY()) {
                int n = 0;
                if (this.l2.getX() > this.l1.getX()) {
                    if (this.l2.getZ() > this.l1.getZ()) {
                        if (s1.getYaw() < -150.0f || s1.getYaw() > -30.0f) {
                            n = 1;
                        }
                    }
                    else if (s1.getYaw() > 0.0f) {
                        n = ((s1.getYaw() < 120.0f) ? 1 : 0);
                    }
                    else {
                        n = ((s1.getYaw() > -120.0f) ? 1 : 0);
                    }
                }
                else if (this.l2.getZ() > this.l1.getZ()) {
                    if (s1.getYaw() < -60.0f || s1.getYaw() > 60.0f) {
                        n = 1;
                    }
                }
                else if (s1.getYaw() < 30.0f || s1.getYaw() > 150.0f) {
                    n = 1;
                }
                if (n != 0) {
                    this.player.sendMessage(this.plugin.getConfigString("create-error-location1-view", "§4错误：§c位置 1 必须是相对于玩家视角的§o左下角§c位置"));
                    return false;
                }
            }
        }
        this.s1 = s1;
        return true;
    }
    
    public boolean setScoreboardLocation(final Location scoreloc) {
        if (this.gt.isMultiplayer()) {
            this.player.sendMessage(this.plugin.getConfigString("create-error-no-local-leaderboard-multiplayer", "§4错误：§c多人游戏不支持本地排行榜。"));
            return false;
        }
        this.scoreloc = scoreloc;
        return true;
    }
}
