package com.hhsir.herewego.logic.markers;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.hhsir.herewego.logic.Cell;

public class SquareMarker extends BaseShapeMarker {

    public SquareMarker(Cell cell) {
        super(cell);
    }

    @Override
    public void draw(Canvas c, float size, float x, float y, Paint paint) {
        super.draw(c, size, x, y, paint);
        c.drawRect(x - size / 4, y - size / 4, x + size / 4, y + size / 4, localPaint);
    }
}
