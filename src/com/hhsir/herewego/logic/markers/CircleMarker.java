package com.hhsir.herewego.logic.markers;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.hhsir.herewego.logic.Cell;

public class CircleMarker extends BaseShapeMarker {

    public CircleMarker(Cell cell) {
        super(cell);
    }

    public void draw(Canvas c, float size, float x, float y, Paint paint) {
        super.draw(c, size, x, y, paint);
        c.drawCircle(x, y, size / 4, localPaint);
    }
}
