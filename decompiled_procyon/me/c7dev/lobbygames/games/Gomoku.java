// 
// Gomoku (Five in a Row) Game for PickleGames
// 

package me.c7dev.lobbygames.games;

import me.c7dev.lobbygames.Game;
import me.c7dev.lobbygames.LobbyGames;
import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.util.CoordinatePair;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import me.c7dev.lobbygames.api.events.PlayerJoinLobbyGameEvent;
import me.c7dev.lobbygames.api.events.PlayerQuitLobbyGameEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Gomoku extends Game implements Listener {
    
    private Player p1;
    private Player p2;
    private boolean p1_black;
    private boolean p1_turn;
    private State[][] board;
    private int board_size;
    private int move_count;
    private String your_turn;
    private String opp_turn;
    private Material black_material;
    private Material white_material;
    
    public Gomoku(final LobbyGames plugin, final Arena arena, final Player player) {
        super(plugin, GameType.GOMOKU, arena, player);
        this.move_count = 0;
        
        if (!this.canStart() || arena.getGameType() != GameType.GOMOKU) {
            return;
        }
        
        this.p1 = player;
        this.p1_black = this.random.nextBoolean();
        this.p1_turn = this.random.nextBoolean();
        
        this.your_turn = plugin.getConfigString("your-turn-msg", "§a你的回合");
        this.opp_turn = plugin.getConfigString("opponent-turn-msg", "§7对手的回合");
        
        this.black_material = Material.valueOf(plugin.getConfig().getString("gomoku.black-material", "BLACK_CONCRETE"));
        this.white_material = Material.valueOf(plugin.getConfig().getString("gomoku.white-material", "WHITE_CONCRETE"));
        
        this.board_size = Math.min(arena.getWidth(), arena.getHeight());
        this.board = new State[this.board_size][this.board_size];
        
        for (int i = 0; i < this.board_size; i++) {
            for (int j = 0; j < this.board_size; j++) {
                this.board[i][j] = State.EMPTY;
            }
        }
        
        if (arena.getSpawn1() != null) {
            plugin.setReturnLocation(player);
            player.teleport(arena.getSpawn1());
        }
        
        this.preparePlayer(player);
        player.getInventory().setItem(8, plugin.getQuitItem());
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.reset();
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (!this.canStart()) {
            return;
        }
        
        if (!this.containsPlayer(event.getPlayer())) {
            return;
        }
        
        final ItemStack handItem = GameUtils.getHandItem(event.getPlayer());
        if (handItem != null && handItem.getType() == this.plugin.getQuitItem().getType()) {
            this.quitConfirmation(event.getPlayer());
            return;
        }
        
        if (!this.isActive()) {
            return;
        }
        
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (!event.getPlayer().getUniqueId().equals((this.p1_turn || this.p2 == null) ? this.p1.getUniqueId() : this.p2.getUniqueId())) {
            final String notYourTurn = this.plugin.getConfigString("not-your-turn-msg", "");
            if (notYourTurn.length() > 0) {
                this.getPlugin().sendActionbar(event.getPlayer(), notYourTurn, false);
            }
            return;
        }
        
        event.setCancelled(true);
        this.place(event.getClickedBlock());
    }
    
    public void place(final Block block) {
        final Location loc = block.getLocation().add(this.getArena().getGenericOffset());
        
        if (!this.getArena().isInBounds(loc)) {
            return;
        }
        
        final CoordinatePair coords = this.getCoords(loc);
        final int x = coords.getX() + (this.board_size / 2);
        final int y = coords.getY() + (this.board_size / 2);
        
        if (x < 0 || y < 0 || x >= this.board_size || y >= this.board_size) {
            return;
        }
        
        if (this.board[x][y] != State.EMPTY) {
            return;
        }
        
        this.p1_turn = !this.p1_turn;
        final boolean isBlack = (!this.p1_turn && this.p1_black) || (this.p1_turn && !this.p1_black);
        this.board[x][y] = isBlack ? State.BLACK : State.WHITE;
        
        block.setType(isBlack ? this.black_material : this.white_material);
        
        ++this.move_count;
        
        if (this.checkWin(x, y, isBlack ? State.BLACK : State.WHITE)) {
            this.win(!this.p1_turn, true);
        } else if (this.move_count >= this.board_size * this.board_size) {
            this.draw();
        }
    }
    
    private boolean checkWin(final int x, final int y, final State stone) {
        if (stone == State.EMPTY) {
            return false;
        }
        
        return this.checkDirection(x, y, stone, 1, 0) ||
               this.checkDirection(x, y, stone, 0, 1) ||
               this.checkDirection(x, y, stone, 1, 1) ||
               this.checkDirection(x, y, stone, 1, -1);
    }
    
    private boolean checkDirection(final int x, final int y, final State stone, final int dx, final int dy) {
        int count = 1;
        count += this.countStones(x, y, stone, dx, dy);
        count += this.countStones(x, y, stone, -dx, -dy);
        return count >= 5;
    }
    
    private int countStones(final int x, final int y, final State stone, final int dx, final int dy) {
        int count = 0;
        int nx = x + dx;
        int ny = y + dy;
        
        while (nx >= 0 && nx < this.board_size && ny >= 0 && ny < this.board_size) {
            if (this.board[nx][ny] == stone) {
                count++;
                nx += dx;
                ny += dy;
            } else {
                break;
            }
        }
        
        return count;
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinLobbyGameEvent event) {
        if (!this.canStart() || event.getGame().getGameType() != GameType.GOMOKU || 
            event.getGame().getArena().getID() != this.getArena().getID()) {
            return;
        }
        
        if (this.getArena().getSpawn1() != null) {
            this.plugin.setReturnLocation(event.getPlayer());
            event.getPlayer().teleport(this.getArena().getSpawn1());
        }
        
        this.preparePlayer(event.getPlayer());
        
        if (this.getPlayers().size() == 2) {
            this.setActive(true);
            this.p2 = event.getPlayer();
            this.p2.getInventory().setItem(8, this.plugin.getQuitItem());
            
            final String startMsg = this.getPlugin().getConfigString("gomoku.start-msg", 
                "§3§m----------------------------------------\n" +
                "§b§l五子棋：§b在棋盘上连成五个同色棋子即可获胜！\n" +
                "§3§m----------------------------------------");
            
            if (startMsg.length() > 0) {
                this.p1.sendMessage(this.plugin.sp(this.p1, startMsg));
                this.p2.sendMessage(this.plugin.sp(this.p2, startMsg));
            }
            
            this.reset();
            
            final String sideMsg = this.plugin.getConfigString("gomoku.side-msg", "\n§b你正在使用 %side%§b！");
            final String blackSide = sideMsg.replaceAll("\\Q%side%\\E", "§8§l黑子");
            final String whiteSide = sideMsg.replaceAll("\\Q%side%\\E", "§f§l白子");
            
            this.p1.sendMessage(this.plugin.sp(this.p1, (this.p1_black ? blackSide : whiteSide) + 
                " §f" + (this.p1_turn ? this.your_turn : this.opp_turn)));
            this.p2.sendMessage(this.plugin.sp(this.p2, (this.p1_black ? whiteSide : blackSide) + 
                " §f" + (this.p1_turn ? this.opp_turn : this.your_turn)));
            
            if ((!this.p1.getWorld().getName().equals(this.getArena().getWorld().getName()) || 
                 this.p1.getLocation().distance(this.getArena().getCenterPixel()) > 10.0) && 
                this.getArena().getSpawn1() != null) {
                this.p1.teleport(this.getArena().getSpawn1());
            }
            
            if (this.opp_turn.length() > 0 || this.your_turn.length() > 0) {
                new BukkitRunnable() {
                    public void run() {
                        if (!Gomoku.this.canStart() || !Gomoku.this.isActive()) {
                            this.cancel();
                            return;
                        }
                        Gomoku.this.getPlugin().sendActionbar(Gomoku.this.p1, 
                            Gomoku.this.p1_turn ? Gomoku.this.your_turn : Gomoku.this.opp_turn, true);
                        Gomoku.this.getPlugin().sendActionbar(Gomoku.this.p2, 
                            Gomoku.this.p1_turn ? Gomoku.this.opp_turn : Gomoku.this.your_turn, true);
                    }
                }.runTaskTimer(this.plugin, 0L, 20L);
            }
        }
    }
    
    public void draw() {
        if (this.p2 != null) {
            final String drawMsg = this.plugin.getConfigString("gomoku.draw-msg", 
                "&2&m----------------------------------------[newline]" +
                "&a&l这局游戏平局！[newline]" +
                "&2&m----------------------------------------");
            
            this.p1.sendMessage(this.plugin.sp(this.p1, drawMsg));
            this.p2.sendMessage(this.plugin.sp(this.p2, drawMsg));
            
            Bukkit.getPluginManager().callEvent(new GameWinEvent(this.p1, this, 0.5));
            Bukkit.getPluginManager().callEvent(new GameWinEvent(this.p2, this, 0.5));
            
            this.p1.playSound(this.p1.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
            this.p2.playSound(this.p2.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
        }
        
        this.end();
    }
    
    public void win(final boolean p1Wins, final boolean addScore) {
        if (this.p2 != null) {
            final Player winner = p1Wins ? this.p1 : this.p2;
            final String winnerName = winner.getName();
            final String winMsg = this.plugin.getConfigString("gomoku.win-msg", 
                "&6&m----------------------------------------[newline]" +
                "&e&l%winner% &e赢得了五子棋游戏！[newline]" +
                "&6&m----------------------------------------")
                .replaceAll("\\Q%winner%\\E", winnerName)
                .replaceAll("\\Q%player%\\E", winnerName);
            
            this.p1.sendMessage(this.plugin.sp(this.p1, winMsg));
            this.p2.sendMessage(this.plugin.sp(this.p2, winMsg));
            
            this.setIsWinner(winner.getUniqueId(), true);
            this.setIsWinner((p1Wins ? this.p2 : this.p1).getUniqueId(), false);
            
            if (addScore) {
                this.addScore(winner, 1);
            }
            
            Bukkit.getPluginManager().callEvent(new GameWinEvent(winner, this, 1.0));
            
            this.p1.playSound(this.p1.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
            this.p2.playSound(this.p2.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
        }
        
        this.end();
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitLobbyGameEvent event) {
        if (!this.canStart()) {
            return;
        }
        
        if (event.getPlayer().getUniqueId().equals(this.p1.getUniqueId())) {
            this.win(false, false);
        } else if (this.p2 != null && event.getPlayer().getUniqueId().equals(this.p2.getUniqueId())) {
            this.win(true, false);
        }
    }
    
    @EventHandler
    public void onEnd(final GameEndEvent event) {
        if (event.getGame().getGameType() == this.getGameType() && 
            event.getGame().getArena().getID() == this.getArena().getID()) {
            HandlerList.unregisterAll(this);
            
            new BukkitRunnable() {
                public void run() {
                    if (Gomoku.this.getArena().getHostingGame() == null) {
                        Gomoku.this.reset();
                    }
                }
            }.runTaskLater(this.plugin, 100L);
        }
    }
    
    public void reset() {
        GameUtils.fill(this.getArena(), Material.AIR, (byte)0, 
            (LobbyGames.SERVER_VERSION <= 12) ? Material.valueOf("WOOD") : Material.OAK_PLANKS, (byte)0);
        this.clearArmorStands();
        
        for (int i = 0; i < this.board_size; i++) {
            for (int j = 0; j < this.board_size; j++) {
                this.board[i][j] = State.EMPTY;
            }
        }
        
        this.move_count = 0;
    }
    
    enum State {
        EMPTY,
        BLACK,
        WHITE
    }
}
