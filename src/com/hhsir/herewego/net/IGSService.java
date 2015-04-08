
package com.hhsir.herewego.net;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.hhsir.herewego.igs.Game;
import com.hhsir.herewego.igs.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IGSService extends Service implements MessageHandler {
    public static final String TAG = "IGSService";
    private Telnet client = null;
    private static int SERVERPORT = 7777;
    private static String SERVER_IP = "igs.joyjoy.net";
    public static boolean isShowConsole = false;
    private static String USER;
    private static String PWD;
    private Toast fastToast;
    private final MyIBinder myIBinder = new MyIBinder();  
    private IGSServerListener mListener;
    
    public static final String END_LINE = new String(new char[]{'\r','\n','1',' ','5','\r','\n'});
    @Override
    public void onCreate() {
        super.onCreate();
        fastToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        SharedPreferences sharedPref = this.getSharedPreferences("Service", Context.MODE_PRIVATE);
        SERVER_IP = sharedPref.getString("Address", SERVER_IP);
        SERVERPORT = sharedPref.getInt("Port", SERVERPORT);
        USER = sharedPref.getString("User", "");
        PWD = sharedPref.getString("Pwd", "");
        
    }
    
    public class MyIBinder extends Binder {  
        public Service getService() {  
            return IGSService.this;  
        }  
    }  
  

    @Override
    public IBinder onBind(Intent intent) {
        return myIBinder;
    }

    public void toastFast(String str) {

        fastToast.setText(str);
        fastToast.show();
    }
    
    public void setIGSListener(IGSServerListener listener) {
        mListener = listener;
    }

    @Override
    public void setConsole(String message) {
        if(isShowConsole&&mListener!=null) {
            mListener.serverMessage(message);
        }
    }

    @Override
    public void serviceConnected() {
        if(mListener!=null) {
            mListener.serverConnect();
        }
        GetResponseAsync responsetask = new GetResponseAsync(client,"", 1000);
        try {
            String result = responsetask.execute().get();
            String results[] = result.split("\n");
            int length = results.length;
            if(results[length-1].contains("Login")) {
                login();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        
    }

    @Override
    public void setMessage(String message) {
        if(mListener!=null) {
            mListener.serverMessage(message);
        }
        
    }
    
    private void login() {
        GetResponseAsync responsetask = new GetResponseAsync(client,USER, 1000);
        try {
            String result = responsetask.execute().get();
            String results[] = result.split("\n");
            int length = results.length;
            if(results[1].contains("1 1")||results[1].contains("Password")) {
                GetResponseAsync pwd = new GetResponseAsync(client,PWD, 1000);
                String pwd_result = pwd.execute().get();
                results = pwd_result.split("\n");
                length = results.length;
                if(results[length-1].contains("1 0")||results[0].contains("Login")) {
                    mListener.invalidPassword();
                    client.disconnect();
                }else {
                    mListener.loginSuccess();
                    sendCommand(Commands.TOGGLE_CLIENT_TRUE);
                    sendCommand(Commands.TOGGLE_QUIET_TRUE);
                }
            }
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    
    public void login(String userName,String pwd) {
        USER = userName;
        PWD = pwd;
        if (client != null && client.isConnected())
            toastFast("Already connected");
        else
            try {
                client = new Telnet(this,SERVER_IP, SERVERPORT);
                if(!client.isConnected()) {
                    client.connectIGSServer();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    @Override
    public void serviceDisconnected() {
        if(isShowConsole&&mListener!=null) {
            mListener.serverDisconnect();
        }
        
    }
    
    public boolean disconnectServer() {
        if (client.disconnect()) {
            return true;
        }
        return false;
    }
    
    public Telnet getTelnet() {
        return client;
    }
    
    
    public String sendCommand(Commands command) {
        if(client!=null&&client.isConnected()) {
            GetResponseAsync pwd = new GetResponseAsync(client,command.getName(), 1000);
            try {
                String result = pwd.execute().get();
                return result;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }else {
            return null;
        }
    }
    
    public String sendCommand(String command) {
        if(client!=null&&client.isConnected()) {
            GetResponseAsync pwd = new GetResponseAsync(client,command, 1000);
            try {
                String result = pwd.execute().get();
                return result;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }else {
            return null;
        }
    }
    
    public List<Game> getGames(){
        if(client!=null&&client.isConnected()) {
            GetResponseAsync pwd = new GetResponseAsync(client,Commands.GAMES.getName(), 1000);
            StringBuilder builder = new StringBuilder();
            try {
                String result = pwd.execute().get();
                if(result!=null&&result.length()>0){
                    builder.append(result);
                    String end =builder.substring(builder.length()-4, builder.length());
                    while (!end.endsWith(END_LINE)) {
                        // TODO 添加另一种结束符的判断
                        GetResponseAsync get_result = new GetResponseAsync(client,"", 1000);
                        result = get_result.execute().get();
                        if(result!=null&&result.length()>0){
                            builder.append(result);
                            end =builder.substring(builder.length()-7, builder.length());
                        }else {
                            return null;
                        }
                        
                    }
                    
                }
                result = builder.toString();
                String games[] = result.split("\r\n");
                parseGamesResult(games);
                return null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }else {
            return null;
        }
    }
    
    private List<Game> parseGamesResult(String[] results){
        List<Game> games = new ArrayList<Game>();
        String reg_games_id = "\\s\\[\\s*([0-9]+)\\]";
        String reg_player_name = "(\\w+)";
        String reg_player_rank = "\\[\\s*([0-9]+[kd]\\*)\\]";
        String reg_game_info = "\\(\\s*([0-9]+)\\s*([0-9]+)\\s*([0-9]+)\\s*([0-9]+\\.*[0-9]*)\\s*([0-9]+)\\s*([ICPS])\\)";
        String reg_game_observing = "\\(\\s*([0-9]+)\\)";
        Pattern p = Pattern.compile("[7]"+reg_games_id+"\\s*"+reg_player_name+"\\s*"+reg_player_rank+"\\s*vs\\.\\s*"+reg_player_name+"\\s*"+reg_player_rank+"\\s*"+reg_game_info+"\\s*"+reg_game_observing);
        for(String game :results) {
            //Log.e(TAG, "gameid = "+game.substring(game.indexOf('['), game.indexOf(']')));
            //String other = game.substring(game.indexOf(']'), game.length());
            //Log.e(TAG, "gameid = "+game.substring(game.indexOf('['), game.indexOf(']')));
            Matcher m = p.matcher(game);  
            if(m.matches()) {
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
//            Log.e(TAG, "list size = "+games.size());
        }
        return games;
    }
    
    public List<Player> getUsers(){
        if(client!=null&&client.isConnected()) {
            GetResponseAsync pwd = new GetResponseAsync(client,Commands.USER.getName(), 1000);
            StringBuilder builder = new StringBuilder();
            try {
                String result = pwd.execute().get();
                if(result!=null&&result.length()>0){
                    builder.append(result);
                    String end =builder.substring(builder.length()-4, builder.length());
                    while (!end.endsWith(END_LINE)) {
                        // TODO 添加另一种结束符的判断
                        GetResponseAsync get_result = new GetResponseAsync(client,"", 1000);
                        result = get_result.execute().get();
                        if(result!=null&&result.length()>0){
                            builder.append(result);
                            end =builder.substring(builder.length()-7, builder.length());
                        }else {
                            return null;
                        }
                        
                    }
                    
                }
                result = builder.toString();
                String users[] = result.split("\r\n");
                parseUsersResult(users);
                return null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }else {
            return null;
        }
    }
    
    private List<Player> parseUsersResult(String[] results){
        List<Player> users = new ArrayList<Player>();
        String reg_guest_user = "^42\\s*guest[0-9]+.*";
        String reg_user_name = "(\\w+)";
        Pattern p = Pattern.compile(reg_guest_user);
        
        for(String user :results) {
           if(p.matches(reg_guest_user, user)) {
               Log.e(TAG, user);
           }else {
               
           }
        }
        return users;
    }
    

}
