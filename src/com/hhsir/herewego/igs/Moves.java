package com.hhsir.herewego.igs;

import android.graphics.Point;

public class Moves {
    private boolean black;
    private Point points[];
    private int step;
    public boolean isBlack() {
        return black;
    }
    public void setBlack(boolean black) {
        this.black = black;
    }
    public Point[] getPoints() {
        return points;
    }
    public void setPoints(Point[] points) {
        this.points = points;
    }
    public int getStep() {
        return step;
    }
    public void setStep(int step) {
        this.step = step;
    }
    
    
}
