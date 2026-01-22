// 
// Decompiled by Procyon v0.6.0
// 

package me.c7dev.lobbygames.util;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SudokuGenerator
{
    int[][] spaces;
    Random r;
    boolean finished;
    List<Integer> numbers;
    Integer[] numbers_a;
    
    public SudokuGenerator() {
        this.finished = false;
        this.numbers = new ArrayList<Integer>();
        this.numbers_a = new Integer[9];
        this.r = new Random();
        this.spaces = new int[9][9];
        for (int i = 0; i < 9; ++i) {
            this.numbers.add(i + 1);
            for (int j = 0; j < 9; ++j) {
                this.spaces[i][j] = 0;
            }
        }
    }
    
    public void loadSudoku(final int[][] spaces) {
        this.spaces = spaces;
        this.finished = true;
    }
    
    public int[][] generateSudoku() throws Exception {
        if (this.finished) {
            return this.spaces;
        }
        int n = 0;
        while (!this.generateSudokuIteration()) {
            if (n >= 10000) {
                throw new Exception("Could not generate a new Sudoku puzzle!");
            }
            ++n;
            for (int i = 0; i < 9; ++i) {
                for (int j = 0; j < 9; ++j) {
                    this.spaces[i][j] = 0;
                }
            }
        }
        return this.spaces;
    }
    
    public boolean generateSudokuIteration() {
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (!this.generateNumber(i, j)) {
                    return false;
                }
            }
        }
        return this.finished = true;
    }
    
    public boolean generateNumber(final int n, final int n2) {
        int intValue = 0;
        Collections.shuffle(this.numbers);
        for (int i = 0; i < 10; ++i) {
            if (i == 9) {
                return false;
            }
            intValue = this.numbers.get(i);
            if (this.unfilledInBox(n, n2, intValue) && this.unfilledInCross(n, n2, intValue)) {
                break;
            }
        }
        this.spaces[n2][n] = intValue;
        return true;
    }
    
    public boolean unfilledInCross(final int n, final int n2, final int n3) {
        for (int i = 0; i < 9; ++i) {
            if (this.spaces[n2][i] == n3) {
                return false;
            }
        }
        for (int j = 0; j < 9; ++j) {
            if (this.spaces[j][n] == n3) {
                return false;
            }
        }
        return true;
    }
    
    public boolean unfilledInBox(final int n, final int n2, final int n3) {
        final int n4 = n - n % 3;
        final int n5 = n2 - n2 % 3;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                if (this.spaces[i + n5][j + n4] == n3) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean gradeSudoku() {
        if (!this.finished) {
            return false;
        }
        for (int i = 0; i < 9; ++i) {
            if (!this.gradeCross(i, i)) {
                return false;
            }
        }
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 3; ++k) {
                if (!this.gradeBox(j * 3, k * 3)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean gradeCross(final int n, final int n2) {
        final ArrayList list = new ArrayList();
        for (int i = 0; i < 9; ++i) {
            final int n3 = this.spaces[n2][i];
            if (n3 == 0 || list.contains(n3)) {
                return false;
            }
            list.add(n3);
        }
        final ArrayList list2 = new ArrayList();
        for (int j = 0; j < 9; ++j) {
            final int n4 = this.spaces[j][n];
            if (n4 == 0 || list2.contains(n4)) {
                return false;
            }
            list2.add(n4);
        }
        return list.size() == 9 && list2.size() == 9;
    }
    
    public boolean gradeBox(final int n, final int n2) {
        final ArrayList list = new ArrayList();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                final int n3 = this.spaces[i + n2][j + n];
                if (n3 == 0 || list.contains(n3)) {
                    return false;
                }
                list.add(n3);
            }
        }
        return list.size() == 9;
    }
    
    public boolean isFinished() {
        return this.finished;
    }
    
    public int unfilledCount() {
        int n = 0;
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (this.spaces[i][j] == 0) {
                    ++n;
                }
            }
        }
        return n;
    }
}
