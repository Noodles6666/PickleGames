// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.games;

import org.bukkit.event.HandlerList;
import me.c7dev.lobbygames.api.events.GameEndEvent;
import me.c7dev.lobbygames.api.events.PlayerQuitLobbyGameEvent;
import org.bukkit.event.Event;
import me.c7dev.lobbygames.api.events.GameWinEvent;
import me.c7dev.lobbygames.api.events.PlayerJoinLobbyGameEvent;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Particle;
import org.bukkit.Color;
import me.c7dev.lobbygames.util.ArmorStandFactory;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import me.c7dev.lobbygames.util.GameUtils;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import me.c7dev.lobbygames.util.GameType;
import me.c7dev.lobbygames.Arena;
import me.c7dev.lobbygames.LobbyGames;
import me.c7dev.lobbygames.util.CoordinatePair;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import me.c7dev.lobbygames.Game;

public class Connect4 extends Game implements Listener
{
    private Player p1;
    private Player p2;
    private boolean p1_turn;
    private boolean p1_red;
    private boolean placing;
    private String your_turn;
    private String opp_turn;
    private String red;
    private String yellow;
    private State[][] spaces;
    private int place_count;
    private int particle_count;
    private CoordinatePair particles_exclude;
    private double particle_offset_x;
    private double particle_offset_z;
    private double particle_square_radius;
    
    public Connect4(final LobbyGames v1, final Arena v2, final Player p3) {
        super(v1, GameType.CONNECT4, v2, p3);
        this.placing = false;
        this.place_count = 0;
        this.particle_square_radius = 0.125;
        if (!this.canStart() || v2.getGameType() != GameType.CONNECT4) {
            return;
        }
        if (!v2.isVerticalLayout()) {
            if (this.p1.hasPermission("lobbygames.admin")) {
                this.p1.sendMessage("§cThere was an error in starting this game, check the console.");
            }
            Bukkit.getLogger().severe("This Connect 4 game could not start because flat (horizontal) arenas are not supported!");
            return;
        }
        v1.setReturnLocation(p3);
        if (v2.getSpawn1() != null) {
            p3.teleport(v2.getSpawn1());
        }
        this.preparePlayer(p3);
        this.p1 = p3;
        this.p1_turn = this.random.nextBoolean();
        this.p1_red = this.random.nextBoolean();
        this.your_turn = v1.getConfigString("your-turn-msg", "§aYour Turn");
        this.opp_turn = v1.getConfigString("opponent-turn-msg", "§7Opponent's Turn");
        final String[] split = v1.getConfigString("connect4.translatable-words", "Red, Yellow").split(",");
        this.red = "§c§l" + split[0].trim();
        this.yellow = "§e§l" + ((split.length > 1) ? split[1].trim() : "Yellow");
        this.spaces = new State[v2.getWidth()][v2.getHeight()];
        this.particle_count = (v1.getConfig().getBoolean("connect4.increased-particles", false) ? 20 : 3);
        for (int i = 0; i < v2.getWidth(); ++i) {
            for (int j = 0; j < v2.getHeight(); ++j) {
                this.spaces[i][j] = State.EMPTY;
            }
        }
        if ((v2.getCoordinateRotation() & 0x1) == 0x0) {
            this.particle_offset_x = 1.0;
            this.particle_offset_z = 0.0;
        }
        else {
            this.particle_offset_x = 0.0;
            this.particle_offset_z = 1.0;
        }
        p3.getInventory().setItem(8, v1.getQuitItem());
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)v1);
        this.reset();
        this.startParticleTimer();
    }
    
    @EventHandler
    public void onEntityInteract(final PlayerInteractAtEntityEvent playerInteractAtEntityEvent) {
        if (this.containsPlayer(playerInteractAtEntityEvent.getPlayer())) {
            this.place(playerInteractAtEntityEvent.getPlayer(), null);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onArmorStand(final PlayerArmorStandManipulateEvent playerArmorStandManipulateEvent) {
        if (!this.canStart()) {
            return;
        }
        if (this.containsPlayer(playerArmorStandManipulateEvent.getPlayer())) {
            playerArmorStandManipulateEvent.setCancelled(true);
            this.place(playerArmorStandManipulateEvent.getPlayer(), null);
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
                if (playerInteractEvent.getPlayer().getUniqueId().equals(this.p1_turn ? this.p1.getUniqueId() : this.p2.getUniqueId())) {
                    playerInteractEvent.setCancelled(true);
                    this.place(playerInteractEvent.getPlayer(), (playerInteractEvent.getClickedBlock() == null) ? null : playerInteractEvent.getClickedBlock().getLocation());
                }
                else {
                    final String configString = this.plugin.getConfigString("not-your-turn-msg", "");
                    if (configString.length() > 0) {
                        this.getPlugin().sendActionbar(playerInteractEvent.getPlayer(), configString, false);
                    }
                }
            }
        }
    }
    
    public boolean checkWin(final int n, final int n2, final State state) {
        int n3 = 0;
        for (int i = Math.max(n2 - 3, 0); i < n2 + 4; ++i) {
            if (i < this.arena.getHeight()) {
                if (this.spaces[n][i] == state) {
                    if (++n3 >= 4) {
                        return true;
                    }
                }
                else {
                    n3 = 0;
                }
            }
        }
        int n4 = 0;
        for (int j = Math.max(n - 3, 0); j < n + 4; ++j) {
            if (j < this.arena.getWidth()) {
                if (this.spaces[j][n2] == state) {
                    if (++n4 >= 4) {
                        return true;
                    }
                }
                else {
                    n4 = 0;
                }
            }
        }
        int n5 = 0;
        for (int k = -3; k < 5; ++k) {
            final int n6 = n + k;
            final int n7 = n2 + k;
            if (n6 >= 0 && n7 >= 0 && n6 < this.arena.getWidth() && n7 < this.arena.getHeight()) {
                if (this.spaces[n6][n7] == state) {
                    if (++n5 >= 4) {
                        return true;
                    }
                }
                else {
                    n5 = 0;
                }
            }
        }
        int n8 = 0;
        for (int l = -3; l < 5; ++l) {
            final int n9 = n - l;
            final int n10 = n2 + l;
            if (n9 >= 0 && n10 >= 0 && n9 < this.arena.getWidth() && n10 < this.arena.getHeight()) {
                if (this.spaces[n9][n10] == state) {
                    if (++n8 >= 4) {
                        return true;
                    }
                }
                else {
                    n8 = 0;
                }
            }
        }
        return false;
    }
    
    public Location getLookingBlock(final Player player) {
        final Location add = player.getLocation().add(0.0, player.isSneaking() ? 1.25 : 1.5, 0.0);
        final Vector multiply = player.getLocation().getDirection().normalize().multiply(0.4);
        int n = 0;
        while (!this.arena.isInBounds(add)) {
            if (n >= 12) {
                return null;
            }
            ++n;
            add.add(multiply);
        }
        return add;
    }
    
    public void place(final Player player, Location lookingBlock) {
        if (this.placing || !this.isActive() || !player.getUniqueId().equals(this.p1_turn ? this.p1.getUniqueId() : this.p2.getUniqueId())) {
            return;
        }
        if (lookingBlock == null) {
            lookingBlock = this.getLookingBlock(player);
        }
        else if (!this.arena.isInBounds(lookingBlock)) {
            return;
        }
        if (lookingBlock == null) {
            return;
        }
        final CoordinatePair coords = this.getCoords(lookingBlock);
        int n = coords.getX() + this.arena.getWidth() / 2;
        if (this.arena.getWidth() % 2 == 0 && (this.arena.getCoordinateRotation() == 0 || this.arena.getCoordinateRotation() == 1)) {
            --n;
        }
        if (n < 0 || n >= this.spaces.length) {
            return;
        }
        int y = 0;
        for (int n2 = 0; n2 < this.arena.getHeight() && this.spaces[n][n2] != State.EMPTY; ++n2) {
            ++y;
        }
        if (y >= this.arena.getHeight()) {
            return;
        }
        coords.setY(y);
        final State state = (this.p1_red ? this.p1_turn : (!this.p1_turn)) ? State.RED : State.YELLOW;
        this.spaces[n][y] = state;
        this.particles_exclude = new CoordinatePair(n, y);
        this.placing = true;
        ++this.place_count;
        GameUtils.blockLocation(lookingBlock);
        lookingBlock.add(0.5, 0.0, 0.5);
        lookingBlock.add(this.arena.getGenericOffset().multiply(0.25));
        final Location clone = lookingBlock.clone();
        if (this.arena.isVerticalLayout()) {
            lookingBlock.setY(this.arena.getLocation2().getY());
            clone.setY(lookingBlock.getY() - this.arena.getHeight() + y);
            clone.add(0.0, -0.2, 0.0);
            lookingBlock.add(0.0, -0.25, 0.0);
        }
        final ArmorStand spawn = new ArmorStandFactory(lookingBlock).setHeadItem(GameUtils.createWool(1, (this.p1_red ? this.p1_turn : (!this.p1_turn)) ? 14 : 4, "Click", new String[0])).spawn();
        final int n3 = 6;
        final Location location = lookingBlock;
        Object o;
        if (LobbyGames.SERVER_VERSION > 12) {
            o = new Particle.DustOptions((this.p1_red ? this.p1_turn : (!this.p1_turn)) ? Color.RED : Color.fromRGB(255, 225, 0), 0.6f);
        }
        else {
            o = null;
        }
        final Location finalClone = clone;
        final Location finalLocation = location;
        new BukkitRunnable() {
            int ticks_done = 0;
            Vector incr = finalLocation.toVector().subtract(finalClone.toVector()).multiply(1.0 / n3);
            private final /* synthetic */ Vector val$particle_offset = Connect4.this.particleOffset(true);
            
            public void run() {
                if (this.ticks_done >= n3) {
                    Connect4.this.placing = false;
                    this.cancel();
                    Connect4.this.particles_exclude = null;
                    return;
                }
                if (o != null) {
                    spawn.getWorld().spawnParticle(Particle.REDSTONE, spawn.getLocation().add(this.val$particle_offset), 20, Connect4.this.particle_square_radius * Connect4.this.particle_offset_x, Connect4.this.particle_square_radius, Connect4.this.particle_square_radius * Connect4.this.particle_offset_z, (Object)o);
                }
                spawn.teleport(spawn.getLocation().add(this.incr));
                ++this.ticks_done;
            }
        }.runTaskTimer((Plugin)this.plugin, 5L, 1L);
        if (this.checkWin(n, y, state)) {
            this.win(this.p1_turn, true);
        }
        else if (this.place_count >= this.arena.getWidth() * this.arena.getHeight()) {
            this.draw();
        }
        this.p1_turn = !this.p1_turn;
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinLobbyGameEvent playerJoinLobbyGameEvent) {
        if (!this.canStart() || playerJoinLobbyGameEvent.getGame().getGameType() != GameType.CONNECT4 || playerJoinLobbyGameEvent.getGame().getArena().getID() != this.getArena().getID()) {
            return;
        }
        this.plugin.setReturnLocation(playerJoinLobbyGameEvent.getPlayer());
        if (this.getArena().getSpawn1() != null) {
            playerJoinLobbyGameEvent.getPlayer().teleport(this.getArena().getSpawn1());
            this.preparePlayer(playerJoinLobbyGameEvent.getPlayer());
        }
        if (this.getPlayers().size() == 2) {
            this.setActive(true);
            this.p2 = playerJoinLobbyGameEvent.getPlayer();
            this.p2.getInventory().setItem(8, this.plugin.getQuitItem());
            final String configString = this.getPlugin().getConfigString("connect4.start-msg", "§3§m----------------------------------------\n§b§lConnect 4: §bAdd tiles to the top of the board and try to connect 4 of your color in any row, column, or diagonal!\n§3§m----------------------------------------");
            if (configString.length() > 0) {
                this.p1.sendMessage(this.plugin.sp(this.p1, configString));
                this.p2.sendMessage(this.plugin.sp(this.p2, configString));
            }
            if ((!this.p1.getWorld().getName().equals(this.getArena().getWorld().getName()) || this.p1.getLocation().distance(this.getArena().getCenterPixel()) > 10.0) && this.getArena().getSpawn1() != null) {
                this.p1.teleport(this.getArena().getSpawn1());
            }
            this.reset();
            final String configString2 = this.plugin.getConfigString("connect4.side-msg", "\n§bYou are playing as %side%§b!");
            final String replaceAll = configString2.replaceAll("\\Q%side%\\E", this.red);
            final String replaceAll2 = configString2.replaceAll("\\Q%side%\\E", this.yellow);
            if (this.p1_red) {
                this.p1.sendMessage(this.plugin.sp(this.p1, replaceAll + " §f" + (this.p1_turn ? this.your_turn : this.opp_turn)));
                this.p1.getInventory().setItem(4, GameUtils.createWool(1, 14, replaceAll, new String[0]));
                this.p2.sendMessage(this.plugin.sp(this.p2, replaceAll2 + " §f" + (this.p1_turn ? this.opp_turn : this.your_turn)));
                this.p2.getInventory().setItem(4, GameUtils.createWool(1, 4, replaceAll2, new String[0]));
            }
            else {
                this.p1.sendMessage(this.plugin.sp(this.p1, replaceAll2 + " §f" + (this.p1_turn ? this.your_turn : this.opp_turn)));
                this.p1.getInventory().setItem(4, GameUtils.createWool(1, 4, replaceAll2, new String[0]));
                this.p2.sendMessage(this.plugin.sp(this.p2, replaceAll + " §f" + (this.p1_turn ? this.opp_turn : this.your_turn)));
                this.p2.getInventory().setItem(4, GameUtils.createWool(1, 14, replaceAll, new String[0]));
            }
            if (this.opp_turn.length() > 0 || this.your_turn.length() > 0) {
                new BukkitRunnable() {
                    public void run() {
                        if (!Connect4.this.canStart() || !Connect4.this.isActive()) {
                            this.cancel();
                            return;
                        }
                        Connect4.this.getPlugin().sendActionbar(Connect4.this.p1, Connect4.this.p1_turn ? Connect4.this.your_turn : Connect4.this.opp_turn, true);
                        Connect4.this.getPlugin().sendActionbar(Connect4.this.p2, Connect4.this.p1_turn ? Connect4.this.opp_turn : Connect4.this.your_turn, true);
                    }
                }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
            }
        }
    }
    
    private void startParticleTimer() {
        if (LobbyGames.SERVER_VERSION <= 12 || !this.canStart() || !this.plugin.getConfig().getBoolean("connect4.particles-enabled", true)) {
            return;
        }
        new BukkitRunnable() {
            private final /* synthetic */ Particle.DustOptions val$red = new Particle.DustOptions(Color.RED, 0.6f);
            private final /* synthetic */ Particle.DustOptions val$yellow = new Particle.DustOptions(Color.fromRGB(255, 225, 0), 0.6f);
            private final /* synthetic */ Vector val$center_offset = Connect4.this.particleOffset(false);
            
            public void run() {
                if (!Connect4.this.canStart()) {
                    this.cancel();
                    return;
                }
                for (int i = 0; i < Connect4.this.spaces.length; ++i) {
                    for (int j = 0; j < Connect4.this.spaces[0].length; ++j) {
                        if (Connect4.this.spaces[i][j] != State.EMPTY) {
                            if (Connect4.this.particles_exclude == null || Connect4.this.particles_exclude.getX() != i || Connect4.this.particles_exclude.getY() != j) {
                                final int n = ((Connect4.this.arena.getCoordinateRotation() & 0x1) == 0x0) ? (Connect4.this.arena.getWidth() - i - 1) : i;
                                final Particle.DustOptions dustOptions = (Connect4.this.spaces[i][j] == State.RED) ? this.val$red : this.val$yellow;
                                final Location add = Connect4.this.getPixel(n, j).add(this.val$center_offset);
                                add.getWorld().spawnParticle(Particle.REDSTONE, add, Connect4.this.particle_count, Connect4.this.particle_square_radius * Connect4.this.particle_offset_x, Connect4.this.particle_square_radius, Connect4.this.particle_square_radius * Connect4.this.particle_offset_z, (Object)dustOptions);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 4L);
    }
    
    private Vector particleOffset(final boolean b) {
        if (b) {
            return this.arena.getGenericOffset().multiply(0.3).add(new Vector(0, 2, 0));
        }
        final boolean b2 = this.arena.getCoordinateRotation() != 1 && this.arena.getCoordinateRotation() != 2;
        final Vector subtract = this.arena.getLocation1().toVector().subtract(this.arena.getCenterPixel().toVector());
        subtract.add(new Vector(0.5 * this.particle_offset_x, 0.5, 0.5 * this.particle_offset_z));
        subtract.add(this.arena.getGenericOffset().multiply(0.1 + (double)(b2 ? 1 : 0)));
        return subtract;
    }
    
    public void draw() {
        if (this.p2 != null) {
            final String configString = this.plugin.getConfigString("connect4.draw-msg", "&2&m----------------------------------------[newline]&a&lThis game is a draw![newline]&2&m----------------------------------------");
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
            final String replaceAll = this.plugin.getConfigString("connect4.win-msg", "&6&m----------------------------------------[newline]&e&l" + name + "&e won the Connect 4 game![newline]&6&m----------------------------------------").replaceAll("\\Q%winner%\\E", name).replaceAll("\\Q%player%\\E", name);
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
        if (gameEndEvent.getGame().getGameType() == this.getGameType() && gameEndEvent.getGame().getArena().getID() == this.arena.getID()) {
            HandlerList.unregisterAll((Listener)this);
            if (this.plugin.getConfig().getBoolean("connect4.reset-on-end")) {
                new BukkitRunnable() {
                    public void run() {
                        if (Connect4.this.getArena().getHostingGame() == null) {
                            Connect4.this.reset();
                        }
                    }
                }.runTaskLater((Plugin)this.plugin, 120L);
            }
        }
    }
    
    public void reset() {
        this.clearArmorStands();
    }
    
    enum State
    {
        EMPTY("EMPTY", 0), 
        RED("RED", 1), 
        YELLOW("YELLOW", 2);
        
        private State(final String name, final int ordinal) {
        }
    }
}
