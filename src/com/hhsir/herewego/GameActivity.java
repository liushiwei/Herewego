
package com.hhsir.herewego;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.hhsir.herewego.board.GoBoardViewHD;
import com.hhsir.herewego.board.GoSoundManager;
import com.hhsir.herewego.logic.Cell;
import com.hhsir.herewego.logic.GoGame;
import com.hhsir.herewego.util.Log;

public class GameActivity extends Activity implements OnTouchListener, OnKeyListener,
        GoGame.GoGameChangeListener {

    public GoSoundManager sound_man;

    @InjectView(R.id.go_board)
    GoBoardViewHD go_board = null;

    @InjectView(R.id.zoom_board)
    GoBoardViewHD zoom_board = null;

    @InjectView(R.id.game_extra_container)
    View gameExtrasContainer;

    private int last_processed_move_change_num = 0;

    private InteractionScope interaction_scope;
    private Toast info_toast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        ButterKnife.inject(this);

        interaction_scope = App.getInteractionScope();
        if (getGame() == null) { // cannot do anything without a game
            Log.w(GameActivity.class, "finish()ing " + this + " cuz getGame()==null");
            finish();
            return;
        }
        if (sound_man == null) {
            sound_man = new GoSoundManager(this);
        }
        setupBoard();
        createInfoToast();

        game2ui();
    }

    @SuppressLint("ShowToast")
    // this is correct - we do not want to show the toast at this stage
    private void createInfoToast() {
        info_toast = Toast.makeText(this.getBaseContext(), "", Toast.LENGTH_LONG);
    }

    @Override
    protected void onResume() {
        super.onResume();

        go_board.setFocusableInTouchMode(true);
        go_board.setFocusable(true);
        go_board.requestFocus();

        if (getGame() == null) {
            Log.w(GameActivity.class,
                    "we do not have a game in onStart of a GoGame activity - thats crazy!");
        } else {
            getGame().addGoGameChangeListener(this);
        }
    }

    public GoGame getGame() {
        return App.getGame();
    }

    /**
     * find the go board widget and set up some properties
     */
    private void setupBoard() {
        go_board.setOnTouchListener(this);
        go_board.setOnKeyListener(this);
        go_board.move_stone_mode = false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        eventForZoomBoard(event);
        doTouch(event);
        return true;
    }

    public void game2ui() {
        go_board.postInvalidate();
        refreshZoomFragment();
    }

    public void refreshZoomFragment() {
        zoom_board.postInvalidate();
    }

    protected void eventForZoomBoard(MotionEvent event) {
        App.getInteractionScope().setTouchPosition(
                getBoard().pixel2cell(event.getX(), event.getY()));

        if (event.getAction() == MotionEvent.ACTION_UP) {
            gameExtrasContainer.setVisibility(View.VISIBLE);
            zoom_board.setVisibility(View.GONE);
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            gameExtrasContainer.setVisibility(View.GONE);
            zoom_board.setVisibility(View.VISIBLE);
        }
        refreshZoomFragment();
    }

    public GoBoardViewHD getBoard() {
        if (go_board == null) {
            setupBoard();
        }
        return go_board;
    }

    public void doTouch(MotionEvent event) {

        // calculate position on the field by position on the touchscreen

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                interaction_scope.setTouchPosition(getBoard()
                        .pixel2cell(event.getX(), event.getY()));
                break;

            case MotionEvent.ACTION_OUTSIDE:
                interaction_scope.setTouchPosition(null);
                break;

            case MotionEvent.ACTION_UP:

                if (go_board.move_stone_mode) {
                    // TODO check if this is an illegal move ( e.g. in variants
                    // )

                    if (getGame().getVisualBoard().isCellFree(interaction_scope.getTouchCell())) {
                        getGame().getActMove().setCell(interaction_scope.getTouchCell());
                        getGame().getActMove().setDidCaptures(true); // TODO
                                                                     // check if
                                                                     // we harm
                                                                     // sth with
                                                                     // that
                        getGame().refreshBoards();
                    }
                    go_board.move_stone_mode = false; // moving of stone done
                } else if ((getGame().getActMove().isOnCell(interaction_scope.getTouchCell()))) {
                    initializeStoneMove();
                } else {
                    doMoveWithUIFeedback(interaction_scope.getTouchCell());
                }

                interaction_scope.setTouchPosition(null);
                break;
        }

        getGame().notifyGameChange();
    }

    @Override
    public void onGoGameChange() {
        Log.i(GameActivity.class, "onGoGameChange in GoActivity");
        if (getGame().getActMove().getMovePos() > last_processed_move_change_num) {
            if (getGame().isBlackToMove()) {
                sound_man.playSound(GoSoundManager.SOUND_PLACE1);
            } else {
                sound_man.playSound(GoSoundManager.SOUND_PLACE2);
            }
        }
        last_processed_move_change_num = getGame().getActMove().getMovePos();

        game2ui();
    }

    protected byte doMoveWithUIFeedback(Cell cell) {
        if (cell == null) {
            return GoGame.MOVE_INVALID_NOT_ON_BOARD;
        }

        final byte res = getGame().do_move(cell);

        switch (res) {
            case GoGame.MOVE_INVALID_IS_KO:
            case GoGame.MOVE_INVALID_CELL_NO_LIBERTIES:
                showInfoToast(getToastForResult(res));
        }

        return res;
    }

    private int getToastForResult(byte res) {
        switch (res) {
            case GoGame.MOVE_INVALID_IS_KO:
                return R.string.invalid_move_ko;

            case GoGame.MOVE_INVALID_CELL_NO_LIBERTIES:
                return R.string.invalid_move_no_liberties;
        }

        throw (new RuntimeException("Illegal game result " + res));
    }

    /**
     * show a the info toast with a specified text from a resource ID
     */
    protected void showInfoToast(@StringRes int resId) {
        info_toast.setText(resId);
        info_toast.show();
    }

    public void initializeStoneMove() {

        if (getGame().getGoMover().isPlayingInThisGame()) { // don't allow with
                                                            // a mover
            return;
        }

        if (go_board.move_stone_mode) { // already in the mode
            return; // -> do nothing
        }

        go_board.move_stone_mode = true;

        // // TODO check if we only want this in certain modes
        // if (GoPrefs.isAnnounceMoveActive()) {
        //
        // new
        // AlertDialog.Builder(this).setMessage(R.string.hint_stone_move).setPositiveButton(R.string.ok,
        //
        // new DialogInterface.OnClickListener() {
        // public void onClick(DialogInterface dialog, int whichButton) {
        // GoPrefs.setAnnounceMoveActive(false);
        // }
        // }).show();
        // }
    }

}
