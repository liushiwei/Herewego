package com.hhsir.herewego.logic.cell_gatherer;

import com.hhsir.herewego.logic.BoardCell;

public class MustBeConnectedCellGatherer extends CellGatherer {

    public MustBeConnectedCellGatherer(BoardCell root) {
        super(root);
    }

    @Override
    protected void pushWithCheck(BoardCell cell) {
        final boolean unProcessed = processed.add(cell);

        if (cell.isInGroupWith(root)) {
            add(cell);
            if (unProcessed) {
                pushSurroundingWithCheck(cell);
            }
        }
    }
}
