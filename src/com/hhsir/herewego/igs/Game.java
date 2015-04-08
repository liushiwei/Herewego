
package com.hhsir.herewego.igs;

public class Game {

    private int game_id;
    private Player black;
    private Player white;
    private int boardSize;

    public int getGame_id() {
        return game_id;
    }

    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }

    public Player getBlack() {
        return black;
    }

    public void setBlack(Player black) {
        this.black = black;
    }

    public Player getWhite() {
        return white;
    }

    public void setWhite(Player white) {
        this.white = white;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    public int getHandicapAmount() {
        return handicapAmount;
    }

    public void setHandicapAmount(int handicapAmount) {
        this.handicapAmount = handicapAmount;
    }

    public float getKomiValue() {
        return komiValue;
    }

    public void setKomiValue(float komiValue) {
        this.komiValue = komiValue;
    }

    public int getByoYomi() {
        return byoYomi;
    }

    public void setByoYomi(int byoYomi) {
        this.byoYomi = byoYomi;
    }

    public String getFR() {
        return FR;
    }

    public void setFR(String fR) {
        FR = fR;
    }

    public int getObserving() {
        return observing;
    }

    public void setObserving(int observing) {
        this.observing = observing;
    }

    private int moves;
    private int handicapAmount;
    private float komiValue;
    private int byoYomi;
    private String FR;
    private int observing;

}
