// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.games;

import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import me.c7dev.lobbygames.api.events.PlayerQuitLobbyGameEvent;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import me.c7dev.lobbygames.util.GameTask;
import me.c7dev.lobbygames.util.SchedulerUtil;
import me.c7dev.lobbygames.api.events.PlayerJoinLobbyGameEvent;
import me.c7dev.lobbygames.util.CoordinatePair;
import org.bukkit.Location;
import me.c7dev.lobbygames.util.ArmorStandFactory;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.util.Vector;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import me.c7dev.lobbygames.util.GameUtils;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.LobbyGames;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import me.c7dev.lobbygames.Game;

public class TicTacToe extends Game implements Listener
{
    private Player p1;
    private Player p2;
    private boolean p1_x;
    private boolean p1_turn;
    private State[][] spaces;
    private int move_count;
    private String your_turn;
    private String opp_turn;
    
    public TicTacToe(final LobbyGames v1, final Arena v2, final Player returnLocation) {
        super(v1, GameType.TICTACTOE, v2, returnLocation);
        this.spaces = new State[3][3];
        this.move_count = 0;
        if (!this.canStart() || v2.getGameType() != GameType.TICTACTOE) {
            return;
        }
        this.p1 = returnLocation;
        this.p1_x = this.random.nextBoolean();
        this.p1_turn = this.random.nextBoolean();
        this.your_turn = v1.getConfigString("your-turn-msg", "§aYour Turn");
        this.opp_turn = v1.getConfigString("opponent-turn-msg", "§7Opponent's Turn");
        for (int i = 0; i < 3; ++i) {
            this.spaces[i][0] = State.EMPTY;
            this.spaces[i][1] = State.EMPTY;
            this.spaces[i][2] = State.EMPTY;
        }
        if (v2.getSpawn1() != null) {
            v1.setReturnLocation(returnLocation);
            returnLocation.teleportAsync(v2.getSpawn1());
        }
        this.preparePlayer(returnLocation);
        returnLocation.getInventory().setItem(3, GameUtils.createWool(1, 14, v1.getConfigString("tictactoe.join-side-title", "§bSwitch to §c§lX§b's").replaceAll("\\Q%side%\\E", "§c§lX"), new String[0]));
        returnLocation.getInventory().setItem(5, GameUtils.createWool(1, 0, v1.getConfigString("tictactoe.join-side-title", "§bSwitch to §f§lO§b's").replaceAll("\\Q%side%\\E", "§f§lO"), new String[0]));
        returnLocation.getInventory().setItem(8, v1.getQuitItem());
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)v1);
        this.reset();
    }
    
    public void checkWin(final int n, final int n2, final State state) {
        if (state == State.EMPTY) {
            return;
        }
        final Vector vector = new Vector(0.5, 0.5, 0.5);
        final int n3 = 1;
        for (int n4 = 0; n4 < 3 && this.spaces[n][n4] == state; ++n4) {
            if (n4 == 2) {
                this.win(this.spaces[n][n4] == State.X == this.p1_x, true);
                if (LobbyGames.SERVER_VERSION > 8) {
                    GameUtils.repeatParticleLine(this.getPixel((n - 1) * n3, -2).add(vector), this.getPixel((n - 1) * n3, 2).add(vector), this.plugin, 10);
                }
                return;
            }
        }
        for (int n5 = 0; n5 < 3 && this.spaces[n5][n2] == state; ++n5) {
            if (n5 == 2) {
                this.win(this.spaces[n5][n2] == State.X == this.p1_x, true);
                if (LobbyGames.SERVER_VERSION > 8) {
                    GameUtils.repeatParticleLine(this.getPixel(-2, (n2 - 1) * n3).add(vector), this.getPixel(2, (n2 - 1) * n3).add(vector), this.plugin, 10);
                }
                return;
            }
        }
        if (n == n2) {
            for (int i = 0; i < 3; ++i) {
                if (this.spaces[i][i] != state) {
                    break;
                }
                if (i == 2) {
                    this.win(this.spaces[i][i] == State.X == this.p1_x, true);
                    if (LobbyGames.SERVER_VERSION > 8) {
                        GameUtils.repeatParticleLine(this.getPixel(-2, -2).add(vector), this.getPixel(2, 2).add(vector), this.plugin, 10);
                    }
                    return;
                }
            }
        }
        if (n + n2 == 2) {
            for (int j = 0; j < 3; ++j) {
                if (this.spaces[2 - j][j] != state) {
                    break;
                }
                if (j == 2) {
                    this.win(this.spaces[2 - j][j] == State.X == this.p1_x, true);
                    if (LobbyGames.SERVER_VERSION > 8) {
                        GameUtils.repeatParticleLine(this.getPixel(2, -2).add(vector), this.getPixel(-2, 2).add(vector), this.plugin, 10);
                    }
                    return;
                }
            }
        }
        if (this.move_count >= 9) {
            this.draw();
        }
    }
    
    @EventHandler
    public void onEntityInteract(final PlayerInteractAtEntityEvent playerInteractAtEntityEvent) {
        if (this.containsPlayer(playerInteractAtEntityEvent.getPlayer()) && playerInteractAtEntityEvent.getPlayer().getUniqueId().equals((this.p1_turn || this.p2 == null) ? this.p1.getUniqueId() : this.p2.getUniqueId())) {
            this.place(playerInteractAtEntityEvent.getRightClicked().getLocation().add(playerInteractAtEntityEvent.getClickedPosition()).add(this.getArena().getGenericOffset().multiply(-1)).getBlock());
        }
    }
    
    @EventHandler
    public void onInteract(final PlayerInteractEvent playerInteractEvent) {
        if (!this.canStart()) {
            return;
        }
        if (this.containsPlayer(playerInteractEvent.getPlayer())) {
            final ItemStack handItem = GameUtils.getHandItem(playerInteractEvent.getPlayer());
            if (handItem != null && handItem.getType() == this.plugin.getQuitItem().getType()) {
                this.quitConfirmation(playerInteractEvent.getPlayer());
                return;
            }
            if (this.isActive()) {
                if (playerInteractEvent.getAction() == Action.LEFT_CLICK_BLOCK || playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (playerInteractEvent.getPlayer().getUniqueId().equals((this.p1_turn || this.p2 == null) ? this.p1.getUniqueId() : this.p2.getUniqueId())) {
                        playerInteractEvent.setCancelled(true);
                        this.place(playerInteractEvent.getClickedBlock());
                    }
                    else {
                        final String configString = this.plugin.getConfigString("not-your-turn-msg", "");
                        if (configString.length() > 0) {
                            this.getPlugin().sendActionbar(playerInteractEvent.getPlayer(), configString, false);
                        }
                    }
                }
            }
            else if (handItem != null && handItem.getType().toString().contains("WOOL")) {
                this.p1_x = (GameUtils.getData(handItem) != 0);
                playerInteractEvent.getPlayer().sendMessage(this.plugin.getConfigString(playerInteractEvent.getPlayer(), "tictactoe.now-playing-as", "§bYou will play as %side%§b's!").replaceAll("\\Q%side%\\E", this.p1_x ? "§c§lX" : "§f§lO"));
            }
        }
    }
    
    public void place(final Block block) {
        final Location add = block.getLocation().add(this.getArena().getGenericOffset());
        if (this.getArena().isInBounds(add)) {
            final CoordinatePair coords = this.getCoords(add);
            final int n = coords.getX() + 1;
            final int n2 = coords.getY() + 1;
            if (n < 0 || n2 < 0 || n > 2 || n2 > 2) {
                return;
            }
            if (this.spaces[n][n2] == State.EMPTY) {
                this.p1_turn = !this.p1_turn;
                final boolean b = (!this.p1_turn && this.p1_x) || (this.p1_turn && !this.p1_x);
                this.spaces[n][n2] = (b ? State.X : State.O);
                if (LobbyGames.SERVER_VERSION >= 13) {
                    block.setType(Material.valueOf((b ? "RED" : "WHITE") + "_CONCRETE"));
                }
                else {
                    GameUtils.setWool(block, b ? 14 : 0);
                }
                new ArmorStandFactory(add.clone().add(this.getArena().getArmorStandOffset()).subtract(this.getArena().getGenericOffset())).setName(b ? "§c§lX" : "§f§lO").spawn();
                ++this.move_count;
                this.checkWin(n, n2, b ? State.X : State.O);
            }
        }
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinLobbyGameEvent playerJoinLobbyGameEvent) {
        if (!this.canStart() || playerJoinLobbyGameEvent.getGame().getGameType() != GameType.TICTACTOE || playerJoinLobbyGameEvent.getGame().getArena().getID() != this.getArena().getID()) {
            return;
        }
        if (this.getArena().getSpawn1() != null) {
            this.plugin.setReturnLocation(playerJoinLobbyGameEvent.getPlayer());
            playerJoinLobbyGameEvent.getPlayer().teleportAsync(this.getArena().getSpawn1());
        }
        this.preparePlayer(playerJoinLobbyGameEvent.getPlayer());
        if (this.getPlayers().size() == 2) {
            this.setActive(true);
            this.p2 = playerJoinLobbyGameEvent.getPlayer();
            this.p2.getInventory().setItem(8, this.plugin.getQuitItem());
            final String configString = this.getPlugin().getConfigString("tictactoe.start-msg", "§3§m----------------------------------------\n§b§lTic Tac Toe: §bGet three X's or three O's in a row to win!\n§3§m----------------------------------------");
            if (configString.length() > 0) {
                this.p1.sendMessage(this.plugin.sp(this.p1, configString));
                this.p2.sendMessage(this.plugin.sp(this.p2, configString));
            }
            this.reset();
            final String configString2 = this.plugin.getConfigString("tictactoe.side-msg", "\n§bYou are playing as %side%§b's!");
            final String replaceAll = configString2.replaceAll("\\Q%side%\\E", "§c§lX");
            final String replaceAll2 = configString2.replaceAll("\\Q%side%\\E", "§f§lO");
            this.p1.sendMessage(this.plugin.sp(this.p1, (this.p1_x ? replaceAll : replaceAll2) + " §f" + (this.p1_turn ? this.your_turn : this.opp_turn)));
            this.p2.sendMessage(this.plugin.sp(this.p2, (this.p1_x ? replaceAll2 : replaceAll) + " §f" + (this.p1_turn ? this.opp_turn : this.your_turn)));
            final ItemStack itemStack = new ItemStack(Material.AIR);
            this.p1.getInventory().setItem(3, itemStack);
            this.p1.getInventory().setItem(5, itemStack);
            this.p2.getInventory().setItem(3, itemStack);
            this.p2.getInventory().setItem(5, itemStack);
            if ((!this.p1.getWorld().getName().equals(this.getArena().getWorld().getName()) || this.p1.getLocation().distance(this.getArena().getCenterPixel()) > 10.0) && this.getArena().getSpawn1() != null) {
                this.p1.teleportAsync(this.getArena().getSpawn1());
            }
            if (this.opp_turn.length() > 0 || this.your_turn.length() > 0) {
                new GameTask() {
                    @Override
                    public void run() {
                        if (!TicTacToe.this.canStart() || !TicTacToe.this.isActive()) {
                            this.cancel();
                            return;
                        }
                        TicTacToe.this.getPlugin().sendActionbar(TicTacToe.this.p1, TicTacToe.this.p1_turn ? TicTacToe.this.your_turn : TicTacToe.this.opp_turn, true);
                        TicTacToe.this.getPlugin().sendActionbar(TicTacToe.this.p2, TicTacToe.this.p1_turn ? TicTacToe.this.opp_turn : TicTacToe.this.your_turn, true);
                    }
                }.runTaskTimer(this.plugin, 1L, 20L);
            }
        }
    }
    
    public void draw() {
        if (this.p2 != null) {
            final String configString = this.plugin.getConfigString("tictactoe.draw-msg", "&2&m----------------------------------------[newline]&a&lThis game is a draw![newline]&2&m----------------------------------------");
            this.p1.sendMessage(this.plugin.sp(this.p1, configString));
            this.p2.sendMessage(this.plugin.sp(this.p2, configString));
            Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(this.p1, this, 0.5));
            Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(this.p1, this, 0.5));
            this.p1.playSound(this.p1.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
            this.p2.playSound(this.p2.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
        }
        this.end();
    }
    
    public void win(final boolean b, final boolean b2) {
        if (this.p2 != null) {
            final Player player = b ? this.p1 : this.p2;
            final String name = player.getName();
            final String replaceAll = this.plugin.getConfigString("tictactoe.win-msg", "&6&m----------------------------------------[newline]&e&l" + name + "&e won the Tic Tac Toe game![newline]&6&m----------------------------------------").replaceAll("\\Q%winner%\\E", name).replaceAll("\\Q%player%\\E", name);
            this.p1.sendMessage(this.plugin.sp(this.p1, replaceAll));
            this.p2.sendMessage(this.plugin.sp(this.p2, replaceAll));
            this.setIsWinner(player.getUniqueId(), true);
            this.setIsWinner((b ? this.p2 : this.p1).getUniqueId(), false);
            if (b2) {
                this.addScore(player, 1);
            }
            Bukkit.getPluginManager().callEvent((Event)new GameWinEvent(b ? this.p1 : this.p2, this, 1.0));
            this.p1.playSound(this.p1.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
            this.p2.playSound(this.p2.getLocation(), GameUtils.fireworkBlastSound(), 1.0f, 1.0f);
        }
        this.end();
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitLobbyGameEvent playerQuitLobbyGameEvent) {
        if (!this.canStart()) {
            return;
        }
        if (playerQuitLobbyGameEvent.getPlayer().getUniqueId().equals(this.p1.getUniqueId())) {
            this.win(false, false);
        }
        else if (this.p2 != null && playerQuitLobbyGameEvent.getPlayer().getUniqueId().equals(this.p2.getUniqueId())) {
            this.win(true, false);
        }
    }
    
    @EventHandler
    public void onEnd(final GameEndEvent gameEndEvent) {
        if (gameEndEvent.getGame().getGameType() == this.getGameType() && gameEndEvent.getGame().getArena().getID() == super.getArena().getID()) {
            HandlerList.unregisterAll((Listener)this);
            
            SchedulerUtil.runLocationTaskLater(this.plugin, this.getArena().getCenterPixel(), () -> {
                if (TicTacToe.this.getArena().getHostingGame() == null) {
                    TicTacToe.this.reset();
                }
            }, 100L);
        }
    }
    
    public void reset() {
        GameUtils.fill(this.getArena(), Material.AIR, (byte)0, (LobbyGames.SERVER_VERSION <= 12) ? Material.valueOf("WOOD") : Material.DARK_OAK_PLANKS, (byte)0);
        this.clearArmorStands();
    }
    
    enum State
    {
        EMPTY("EMPTY", 0), 
        X("X", 1), 
        O("O", 2);
        
        private State(final String name, final int ordinal) {
        }
    }
}
