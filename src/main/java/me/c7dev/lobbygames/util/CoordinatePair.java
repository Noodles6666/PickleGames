// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

public class CoordinatePair
{
    private int xn;
    private int yn;
    
    public CoordinatePair(final int xn, final int yn) {
        this.xn = xn;
        this.yn = yn;
    }
    
    public int getX() {
        return this.xn;
    }
    
    public int getY() {
        return this.yn;
    }
    
    public CoordinatePair setX(final int xn) {
        this.xn = xn;
        return this;
    }
    
    public CoordinatePair setY(final int yn) {
        this.yn = yn;
        return this;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof CoordinatePair)) {
            return false;
        }
        final CoordinatePair coordinatePair = (CoordinatePair)o;
        return coordinatePair.getX() == this.xn && coordinatePair.getY() == this.yn;
    }
    
    public CoordinatePair clone() {
        return new CoordinatePair(this.xn, this.yn);
    }
    
    public CoordinatePair add(final CoordinatePair coordinatePair) {
        this.xn += coordinatePair.getX();
        this.yn += coordinatePair.getY();
        return this;
    }
    
    public CoordinatePair multiply(final int n) {
        this.xn *= n;
        this.yn *= n;
        return this;
    }
    
    @Override
    public String toString() {
        return "(" + this.xn + ", " + this.yn;
    }
}
