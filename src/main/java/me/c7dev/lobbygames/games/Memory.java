// 
// Memory Game for LobbyGames
// 

package me.c7dev.lobbygames.games;

import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.Game;
import me.c7dev.lobbygames.LobbyGames;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import me.c7dev.lobbygames.util.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Memory extends Game implements Listener {
    
    private Player p;
    private int[][] board;
    private boolean[][] revealed;
    private Location firstClick;
    private Location secondClick;
    private int pairsFound;
    private int totalPairs;
    private long startTime;
    private int moves;
    private boolean waitingForCheck;
    
    public Memory(final LobbyGames plugin, final Arena arena, final Player player) {
        super(plugin, GameType.MEMORY, arena, player);
        
        if (!this.canStart() || arena.getGameType() != GameType.MEMORY) {
            return;
        }
        
        this.p = this.getPlayer1();
        this.pairsFound = 0;
        this.moves = 0;
        this.waitingForCheck = false;
        this.startTime = System.currentTimeMillis();
        
        int width = arena.getWidth();
        int height = arena.getHeight();
        this.totalPairs = (width * height) / 2;
        
        this.board = new int[width][height];
        this.revealed = new boolean[width][height];
        
        this.generatePairs();
        
        this.setActive(true);
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
        
        final String startMsg = plugin.getConfigString("memory.start-msg", 
            "§3§m----------------------------------------\n" +
            "§b§l记忆翻牌：§b点击方块找到配对的颜色！\n" +
            "§7配对数：§f" + this.totalPairs + "\n" +
            "§3§m----------------------------------------")
            .replace("%pairs%", String.valueOf(this.totalPairs));
        if (startMsg.length() > 0) {
            p.sendMessage(startMsg);
        }
        
        this.initArena();
        
        if (arena.getSpawn1() != null) {
            plugin.setReturnLocation(p);
            p.teleport(arena.getSpawn1());
        }
    }
    
    private void generatePairs() {
        final List<Integer> pairs = new ArrayList<Integer>();
        for (int i = 0; i < this.totalPairs; i++) {
            pairs.add(i);
            pairs.add(i);
        }
        Collections.shuffle(pairs);
        
        int index = 0;
        for (int x = 0; x < this.board.length; x++) {
            for (int z = 0; z < this.board[0].length; z++) {
                this.board[x][z] = pairs.get(index++);
                this.revealed[x][z] = false;
            }
        }
    }
    
    private void initArena() {
        for (int x = 0; x < this.board.length; x++) {
            for (int z = 0; z < this.board[0].length; z++) {
                final Block block = this.getBlockAt(x, z);
                if (LobbyGames.SERVER_VERSION >= 13) {
                    block.setType(Material.WHITE_WOOL);
                } else {
                    block.setType(Material.valueOf("WOOL"));
                    block.getState().setRawData((byte)0);
                    block.getState().update();
                }
            }
        }
    }
    
    private Block getBlockAt(int x, int z) {
        final Location loc = this.getArena().getLocation1().clone();
        loc.add(x, 0, z);
        return loc.getBlock();
    }
    
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getPlayer().equals(this.p)) {
            return;
        }
        
        if (!this.isActive()) {
            return;
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        
        final Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }
        
        final int[] coords = this.getCoordinates(clicked.getLocation());
        if (coords == null) {
            return;
        }
        
        final int x = coords[0];
        final int z = coords[1];
        
        if (this.revealed[x][z]) {
            return;
        }
        
        if (this.waitingForCheck) {
            return;
        }
        
        event.setCancelled(true);
        
        if (this.firstClick == null) {
            this.firstClick = clicked.getLocation();
            this.revealBlock(x, z);
            return;
        }
        
        if (this.secondClick == null) {
            // 防止点击同一个方块两次
            if (clicked.getLocation().equals(this.firstClick)) {
                return;
            }
            
            this.secondClick = clicked.getLocation();
            this.revealBlock(x, z);
            this.moves++;
            this.waitingForCheck = true;
            
            // 立即检查配对，不延迟
            this.checkMatch();
        }
    }
    
    private void revealBlock(int x, int z) {
        final Block block = this.getBlockAt(x, z);
        final int pairId = this.board[x][z];
        
        if (LobbyGames.SERVER_VERSION >= 13) {
            final Material[] colors = {
                Material.RED_WOOL, Material.BLUE_WOOL, Material.GREEN_WOOL,
                Material.YELLOW_WOOL, Material.ORANGE_WOOL, Material.PURPLE_WOOL,
                Material.PINK_WOOL, Material.LIME_WOOL, Material.CYAN_WOOL,
                Material.LIGHT_BLUE_WOOL, Material.MAGENTA_WOOL, Material.BROWN_WOOL
            };
            block.setType(colors[pairId % colors.length]);
        } else {
            final byte[] colors = {14, 11, 13, 4, 1, 10, 6, 5, 9, 3, 2, 12};
            block.setType(Material.valueOf("WOOL"));
            block.getState().setRawData(colors[pairId % colors.length]);
            block.getState().update();
        }
    }
    
    private void checkMatch() {
        if (this.firstClick == null || this.secondClick == null) {
            this.resetClicks();
            return;
        }
        
        final int[] coords1 = this.getCoordinates(this.firstClick);
        final int[] coords2 = this.getCoordinates(this.secondClick);
        
        if (coords1 == null || coords2 == null) {
            this.resetClicks();
            return;
        }
        
        final int x1 = coords1[0], z1 = coords1[1];
        final int x2 = coords2[0], z2 = coords2[1];
        
        final int value1 = this.board[x1][z1];
        final int value2 = this.board[x2][z2];
        
        if (value1 == value2) {
            // 配对成功 - 标记为已翻开
            this.revealed[x1][z1] = true;
            this.revealed[x2][z2] = true;
            this.pairsFound++;
            
            final String successMsg = this.getPlugin().getConfigString("memory.pair-success-msg", 
                "§a§l配对成功！ §7(%found%/%total%)")
                .replace("%found%", String.valueOf(this.pairsFound))
                .replace("%total%", String.valueOf(this.totalPairs));
            p.sendMessage(successMsg);
            
            this.resetClicks();
            
            if (this.pairsFound >= this.totalPairs) {
                this.win();
            }
        } else {
            // 配对失败 - 延迟后重置方块为白色
            final String failMsg = this.getPlugin().getConfigString("memory.pair-fail-msg", "§c§l配对失败！");
            p.sendMessage(failMsg);
            
            new me.c7dev.lobbygames.util.GameTask() {
                public void run() {
                    if (!Memory.this.isActive()) {
                        return;
                    }
                    
                    // 只重置未被标记为已翻开的方块
                    if (!Memory.this.revealed[x1][z1]) {
                        final Block block1 = Memory.this.getBlockAt(x1, z1);
                        if (LobbyGames.SERVER_VERSION >= 13) {
                            block1.setType(Material.WHITE_WOOL);
                        } else {
                            block1.setType(Material.valueOf("WOOL"));
                            block1.getState().setRawData((byte)0);
                            block1.getState().update(true, false);
                        }
                    }
                    
                    if (!Memory.this.revealed[x2][z2]) {
                        final Block block2 = Memory.this.getBlockAt(x2, z2);
                        if (LobbyGames.SERVER_VERSION >= 13) {
                            block2.setType(Material.WHITE_WOOL);
                        } else {
                            block2.setType(Material.valueOf("WOOL"));
                            block2.getState().setRawData((byte)0);
                            block2.getState().update(true, false);
                        }
                    }
                    
                    Memory.this.resetClicks();
                }
            }.runTaskLater(this.getPlugin(), 20L);
        }
    }
    
    private void resetClicks() {
        this.firstClick = null;
        this.secondClick = null;
        this.waitingForCheck = false;
    }
    
    private int[] getCoordinates(Location loc) {
        final Location origin = this.getArena().getLocation1();
        
        final int x = loc.getBlockX() - origin.getBlockX();
        final int z = loc.getBlockZ() - origin.getBlockZ();
        
        if (x < 0 || x >= this.board.length || z < 0 || z >= this.board[0].length) {
            return null;
        }
        
        if (loc.getBlockY() != origin.getBlockY()) {
            return null;
        }
        
        return new int[]{x, z};
    }
    
    private void win() {
        final long timeTaken = (System.currentTimeMillis() - this.startTime) / 1000;
        
        final String winMsg = this.getPlugin().getConfigString("memory.win-msg",
            "§2§m----------------------------------------\n" +
            "§a§l恭喜完成！\n" +
            "§a时间：§f%time% 秒\n" +
            "§a移动次数：§f%moves%\n" +
            "§2§m----------------------------------------")
            .replace("%time%", String.valueOf(timeTaken))
            .replace("%moves%", String.valueOf(this.moves));
        
        if (winMsg.length() > 0) {
            p.sendMessage(winMsg);
        }
        
        final int score = Math.max(1000 - (int)timeTaken * 10 - this.moves * 5, 100);
        
        Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(this.p, this, score));
        this.end();
    }
    
    @Override
    public void end() {
        super.end();
        this.initArena();
    }
}
