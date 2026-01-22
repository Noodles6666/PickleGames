// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

public enum Direction
{
    DOWN("DOWN", 0, 1), 
    LEFT("LEFT", 1, 2), 
    UP("UP", 2, 3), 
    RIGHT("RIGHT", 3, 4);
    
    private int value;
    
    private Direction(final String name, final int ordinal, final int value) {
        this.value = value;
    }
    
    public int getValue() {
        return this.value;
    }
}
