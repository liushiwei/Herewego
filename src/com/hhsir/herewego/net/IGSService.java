
package com.hhsir.herewego.net;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class IGSService extends Service implements MessageHandler {
    private Telnet client = null;
    private static int SERVERPORT = 7777;
    private static String SERVER_IP = "igs.joyjoy.net";
    public static boolean isShowConsole = false;
    private static String USER;
    private static String PWD;
    private Toast fastToast;
    private final MyIBinder myIBinder = new MyIBinder();  
    private IGSServerListener mListener;
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }else {
            return null;
        }
    }
    
    
    

}
