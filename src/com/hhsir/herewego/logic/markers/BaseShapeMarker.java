package com.hhsir.herewego.logic.markers;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.hhsir.herewego.logic.Cell;

public class BaseShapeMarker extends GoMarker {

    Paint localPaint;

    public BaseShapeMarker(Cell cell) {
        super(cell);
    }

    public void draw(Canvas c, float size, float x, float y, Paint paint) {
        if (localPaint == null) {
            localPaint = new Paint(paint);
            localPaint.setStyle(Paint.Style.STROKE);
            localPaint.setStrokeWidth(3);
            localPaint.setAntiAlias(true);
        }
    }
}
