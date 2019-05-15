package br.com.ufal.easy.model.diff;

import java.util.ArrayList;
import java.util.List;

/**
 * Federal University of Alagoas - 2018
 *
 */
public class Hunk {
    public static final int MODIFICATION = 1;
    public static final int ADDITION = 2;
    public static final int DELETION = 3;

    private int leftFrom = 0;
    private int leftTo = 0;

    private int rightFrom = 0;
    private int rightTo = 0;

    private int kind;

    public Hunk() {
    }

    public int getLeftFrom() {
        return leftFrom;
    }

    public void setLeftFrom(int leftFrom) {
        this.leftFrom = leftFrom;
    }

    public int getLeftTo() {
        return leftTo;
    }

    public void setLeftTo(int leftTo) {
        this.leftTo = leftTo;
    }

    public int getRightFrom() {
        return rightFrom;
    }

    public void setRightFrom(int rightFrom) {
        this.rightFrom = rightFrom;
    }

    public int getRightTo() {
        return rightTo;
    }

    public void setRightTo(int rightTo) {
        this.rightTo = rightTo;
    }

    public int getKind() { return kind; }

    public void setKind(int kind) { this.kind = kind; }

    public boolean isChange() {
        if(kind == MODIFICATION) return true;
        return false;
    }

    public boolean isDeletion() {
        if(kind == DELETION) return true;
        return false;
    }

    public boolean isAddition() {
        if(kind == ADDITION) return true;
        return false;
    }

    @Override
    public String toString() {
        return "Hunk{" + "leftFrom=" + leftFrom + ", leftTo=" + leftTo + ", rightFrom=" + rightFrom + ", rightTo=" + rightTo + ", kind=" + kind + '}';
    }
}