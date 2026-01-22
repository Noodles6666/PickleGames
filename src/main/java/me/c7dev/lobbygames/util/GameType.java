// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

public enum GameType
{
    SNAKE("SNAKE", 0, false, false, true, false), 
    MINESWEEPER("MINESWEEPER", 1, false, true, false, false), 
    SPLEEF("SPLEEF", 2, true, true, false, true), 
    CLICKER("CLICKER", 3, false, true, false, false), 
    SOCCER("SOCCER", 4, true, true, false, true), 
    SUDOKU("SUDOKU", 5, false, true, true, false), 
    T048("T048", 6, false, false, false, false), 
    TICTACTOE("TICTACTOE", 7, true, true, true, false), 
    POOL("POOL", 8, true, true, false, false), 
    CONNECT4("CONNECT4", 9, true, false, true, false),
    MEMORY("MEMORY", 10, false, true, false, false),
    GOMOKU("GOMOKU", 11, true, true, true, false);
    
    private final boolean mp;
    private final boolean direct_mapping;
    private final boolean vertical_supported;
    private final boolean disable_fly;
    
    private GameType(final String name, final int ordinal, final boolean mp, final boolean direct_mapping, final boolean vertical_supported, final boolean disable_fly) {
        this.mp = mp;
        this.direct_mapping = direct_mapping;
        this.vertical_supported = vertical_supported;
        this.disable_fly = disable_fly;
    }
    
    public boolean isMultiplayer() {
        return this.mp;
    }
    
    public boolean isDirectBlockMapping() {
        return this.direct_mapping;
    }
    
    public boolean canSupportVerticalArena() {
        return this.vertical_supported;
    }
    
    public boolean isFlyDisabled() {
        return this.disable_fly;
    }
    
    @Deprecated
    public boolean usesLeaderboard() {
        return true;
    }
}
