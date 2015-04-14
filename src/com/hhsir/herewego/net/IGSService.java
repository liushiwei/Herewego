
package com.hhsir.herewego.net;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.hhsir.herewego.igs.Game;
import com.hhsir.herewego.igs.Moves;
import com.hhsir.herewego.igs.Parser;
import com.hhsir.herewego.igs.Player;
import com.hhsir.herewego.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
    private Parser mParser;

    public static final String END_LINE = new String(new char[] {
            '\r', '\n', '1', ' ', '5', '\r', '\n'
    });

    @Override
    public void onCreate() {
        super.onCreate();
        fastToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        SharedPreferences sharedPref = this.getSharedPreferences("Service", Context.MODE_PRIVATE);
        SERVER_IP = sharedPref.getString("Address", SERVER_IP);
        SERVERPORT = sharedPref.getInt("Port", SERVERPORT);
        USER = sharedPref.getString("User", "");
        PWD = sharedPref.getString("Pwd", "");
        mParser = new Parser();
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
        if (isShowConsole && mListener != null) {
            mListener.serverMessage(message);
        }
    }

    @Override
    public void serviceConnected() {
        if (mListener != null) {
            mListener.serverConnect();
        }
        GetResponseAsync responsetask = new GetResponseAsync(client, "", 1000);
        try {
            String result = responsetask.execute().get();
            String results[] = result.split("\n");
            int length = results.length;
            if (results[length - 1].contains("Login")) {
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
        if (mListener != null) {
            mListener.serverMessage(message);
        }

    }

    private void login() {
        GetResponseAsync responsetask = new GetResponseAsync(client, USER, 1000);
        try {
            String result = responsetask.execute().get();
            String results[] = result.split("\n");
            int length = results.length;
            if (results[1].contains("1 1") || results[1].contains("Password")) {
                GetResponseAsync pwd = new GetResponseAsync(client, PWD, 1000);
                String pwd_result = pwd.execute().get();
                results = pwd_result.split("\n");
                length = results.length;
                if (results[length - 1].contains("1 0") || results[0].contains("Login")) {
                    mListener.invalidPassword();
                    client.disconnect();
                } else {
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

    public void login(String userName, String pwd) {
        USER = userName;
        PWD = pwd;
        if (client != null && client.isConnected())
            toastFast("Already connected");
        else
            try {
                client = new Telnet(this, SERVER_IP, SERVERPORT);
                if (!client.isConnected()) {
                    client.connectIGSServer();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    @Override
    public void serviceDisconnected() {
        if (isShowConsole && mListener != null) {
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
        if (client != null && client.isConnected()) {
            GetResponseAsync pwd = new GetResponseAsync(client, command.getName(), 1000);
            try {
                String result = pwd.execute().get();
                return result;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    public String sendCommand(String command) {
        if (client != null && client.isConnected()) {
            GetResponseAsync pwd = new GetResponseAsync(client, command, 1000);
            try {
                String result = pwd.execute().get();
                return result;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    public List<Game> getGames() {
        if (client != null && client.isConnected()) {
            GetResponseAsync pwd = new GetResponseAsync(client, Commands.GAMES.getName(), 1000);
            StringBuilder builder = new StringBuilder();
            try {
                String result = pwd.execute().get();
                if (result != null && result.length() > 0) {
                    builder.append(result);
                    String end = builder.substring(builder.length() - 4, builder.length());
                    while (!end.endsWith(END_LINE)) {
                        // TODO 添加另一种结束符的判断
                        GetResponseAsync get_result = new GetResponseAsync(client, "", 1000);
                        result = get_result.execute().get();
                        if (result != null && result.length() > 0) {
                            builder.append(result);
                            end = builder.substring(builder.length() - 7, builder.length());
                        } else {
                            return null;
                        }

                    }

                }
                result = builder.toString();
                String games[] = result.split("\r\n");
                mParser.parseGamesResult(games);
                return null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    

    public List<Player> getUsers() {
        if (client != null && client.isConnected()) {
            GetResponseAsync pwd = new GetResponseAsync(client, Commands.USER.getName(), 1000);
            StringBuilder builder = new StringBuilder();
            try {
                String result = pwd.execute().get();
                if (result != null && result.length() > 0) {
                    builder.append(result);
                    String end = builder.substring(builder.length() - 7, builder.length());
                    while (!end.endsWith(END_LINE)) {
                        // TODO 添加另一种结束符的判断
                        GetResponseAsync get_result = new GetResponseAsync(client, "", 1000);
                        result = get_result.execute().get();
                        if (result != null && result.length() > 0) {
                            builder.append(result);
                            end = builder.substring(builder.length() - 7, builder.length());
                        } else {
                            return null;
                        }

                    }

                }
                result = builder.toString();
                String users[] = result.split("\r\n");
                List<Player> users_list = mParser.parseUsersResult(users);
                Log.e(IGSService.class, "list size = "+users_list.size());
                return null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    private int user_name_l = 10;
    private int user_info_l = 14;
    private int user_lang_l = 7;

   
    
    public List<Moves> getMoves(int game_id){
        if (client != null && client.isConnected()) {
            GetResponseAsync pwd = new GetResponseAsync(client, Commands.MOVES.getName()+" "+game_id, 1000);
            StringBuilder builder = new StringBuilder();
            try {
                String result = pwd.execute().get();
                if (result != null && result.length() > 0) {
                    builder.append(result);
                    String end = builder.substring(builder.length() - 7, builder.length());
                    while (!end.endsWith(END_LINE)) {
                        // TODO 添加另一种结束符的判断
                        GetResponseAsync get_result = new GetResponseAsync(client, "", 1000);
                        result = get_result.execute().get();
                        if (result != null && result.length() > 0) {
                            builder.append(result);
                            end = builder.substring(builder.length() - 7, builder.length());
                        } else {
                            return null;
                        }

                    }

                }
                result = builder.toString();
                String moves[] = result.split("\r\n");
                List<Moves> moves_list = mParser.parseMoves(moves);
                Log.e(IGSService.class, "list size = "+moves_list.size());
                return null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

}
