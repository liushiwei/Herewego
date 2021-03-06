package com.hhsir.herewego.igs;

import android.graphics.Point;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    
    private static final String TAG = "Parser";

    public List<Game> parseGamesResult(String[] results) {
        List<Game> games = new ArrayList<Game>();
        String reg_games_id = "\\s\\[\\s*([0-9]+)\\]";
        String reg_player_name = "(\\w+)";
        String reg_player_rank = "\\[\\s*([0-9]+[kd]\\*)\\]";
        String reg_game_info = "\\(\\s*([0-9]+)\\s*([0-9]+)\\s*([0-9]+)\\s*([-]?[0-9]+\\.*[0-9]*)\\s*([0-9]+)\\s*([ICPS])\\)";
        String reg_game_observing = "\\(\\s*([0-9]+)\\)";
        Pattern p = Pattern.compile("[7]" + reg_games_id + "\\s*" + reg_player_name + "\\s*"
                + reg_player_rank + "\\s*vs\\.\\s*" + reg_player_name + "\\s*" + reg_player_rank
                + "\\s*" + reg_game_info + "\\s*" + reg_game_observing);
        for (String game : results) {
            // Log.e(TAG, "gameid = "+game.substring(game.indexOf('['),
            // game.indexOf(']')));
            // String other = game.substring(game.indexOf(']'), game.length());
            // Log.e(TAG, "gameid = "+game.substring(game.indexOf('['),
            // game.indexOf(']')));
            Matcher m = p.matcher(game);
            if (m.matches()) {
                Game g = new Game();
                g.setGame_id(Integer.valueOf(m.group(1)));
                Player white = new Player();
                white.setName(m.group(2));
                white.setRank(m.group(3));
                Player black = new Player();
                black.setName(m.group(4));
                black.setRank(m.group(5));
                g.setBlack(black);
                g.setWhite(white);
                g.setMoves(Integer.valueOf(m.group(6)));
                g.setBoardSize(Integer.valueOf(m.group(7)));
                g.setHandicapAmount(Integer.valueOf(m.group(8)));
                g.setKomiValue(Float.valueOf(m.group(9)));
                g.setByoYomi(Integer.valueOf(m.group(10)));
                g.setFR(m.group(11));
                g.setObserving(Integer.valueOf(m.group(12)));
                games.add(g);
            }
            // Log.e(TAG, "list size = "+games.size());
        }
        return games;
    }
    
    public List<Player> parseUsersResult(String[] results) {
        List<Player> users = new ArrayList<Player>();

        String reg_guest_user = "^42\\s*guest[0-9]+.*";
        String reg_player_rank = "((NR)|([0-9]+[kd]\\*?))";
        String reg_player_wl = "([0-9]+\\/\\s*[0-9]+)";
        String reg_player_obs_pl = "(-|[0-9]+)";
        String reg_player_Idle = "([0-9]+[smh])";
        String reg_player_flag = "([QX-][-S!])";
        String reg_player_language = "(\\w+)";
        String reg_user = reg_player_rank + "\\s+" + reg_player_wl + "\\s+"
                + reg_player_obs_pl + "\\s+" + reg_player_obs_pl + "\\s+" + reg_player_Idle
                + "\\s+" + reg_player_flag + "\\s+" + reg_player_language + "\\s*";
        Pattern p = Pattern.compile(reg_guest_user);
        for (int i=1;i<results.length-2;i++) {
            String user = results[i];
            if (p.matches(reg_guest_user, user)) {
                Log.e(TAG, user);
                // TODO 解析游客
            } else {
                Player player = new Player();
                try {
                    byte[] player_byte = user.getBytes("Shift_JIS");
                    player.setName(new String(player_byte, 2, 10));
                    player.setInfo(new String(player_byte, 15, 14));
                    player.setCountry(new String(player_byte, 24, 8));
                    String new_user = new String(player_byte, 40, player_byte.length - 40);
                    Pattern p_user = Pattern.compile(reg_user);
                    Matcher matcher_user = p_user.matcher(new_user);
                    if (matcher_user.matches()) {
                        player.setRank(matcher_user.group(1));
                        player.setWl(matcher_user.group(4));
                        if (matcher_user.group(5).equals("-"))
                            player.setObs(0);
                        else {
                            player.setObs(Integer.valueOf(matcher_user.group(5)));
                        }
                        if (matcher_user.group(6).equals("-"))
                            player.setPl(0);
                        else {
                            player.setPl(Integer.valueOf(matcher_user.group(6)));
                        }
                        player.setIdle(matcher_user.group(7));
                        player.setFlag(matcher_user.group(8));
                        player.setLanguage(matcher_user.group(9));
                    }
                    users.add(player);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

        }
        return users;
    }
    
    public List<Moves> parseMoves(String[] moves){
        String reg_move = "^15\\s*([0-9]+)\\(([B|W])\\)\\:\\s(.*)";
        Pattern p = Pattern.compile(reg_move);
        List<Moves> list_moves = new ArrayList<Moves>();
        for(int i=0;i<moves.length-1;i++) {
            Matcher m = p.matcher(moves[i]);
            if(m.matches()) {
                Moves move =new Moves();
                move.setStep(Integer.valueOf(m.group(1)));
                move.setBlack(m.group(2).equals("B"));
                String[] points_s = m.group(3).split(" ");
                Point[] points = new Point[points_s.length];
                for(int j=0;j<points_s.length;j++) {
                    Point t = new Point(points_s[j].charAt(0)-34,Integer.valueOf(points_s[j].substring(1, points_s[j].length())));
                    points[j] = t;
                }
                move.setPoints(points);
                list_moves.add(move);
            }
        }
        return list_moves;
    }

}
