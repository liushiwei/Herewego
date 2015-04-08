
package com.hhsir.herewego;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hhsir.herewego.net.Commands;
import com.hhsir.herewego.net.GetResponseAsync;
import com.hhsir.herewego.net.IGSServerListener;
import com.hhsir.herewego.net.IGSService;
import com.hhsir.herewego.net.IGSService.MyIBinder;
import com.hhsir.herewego.net.Telnet;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleActivity extends Activity implements IGSServerListener {
    private static final String TAG = "ConsoleActivity";
    private Toast fastToast;
    private static int SERVERPORT = 23;
    private static String SERVER_IP = "192.168.0.105";
    private static TextView et;
    private static EditText server_message;
    private IGSService mService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        fastToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        et = (TextView) findViewById(R.id.inputStreamTextView);
        et.setMovementMethod(new ScrollingMovementMethod());
        server_message= (EditText) findViewById(R.id.server_message);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/DroidSansMono.ttf");
        server_message.setTypeface(typeface);
        //startService(new Intent(this,IGSService.class));
        bindService(new Intent(this,IGSService.class), myLocalServiceConnection, Service.BIND_AUTO_CREATE);
        String reg_guest_user = "^\\s*guest[0-9]+.*";
        Pattern p = Pattern.compile(reg_guest_user);
        if(p.matches(reg_guest_user, "  guest790                  --        NR    0/   0  79  -   50s    Q- default")) {
            Log.e(TAG, "user is guest ");
        }
           
    }
    
    private Handler mHandler  = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    if(msg.obj!=null){
                        SpannableString s = new SpannableString(msg.obj.toString());
                        addServerMessage(s);
                    }
                    break;
                case 1:
                    et.setText(msg.obj.toString());
                    break;
                case 2:
                    fastToast.setText(msg.obj.toString());
                    fastToast.show();
                    break;
            }
            super.handleMessage(msg);
        }
        
    };

    public void onClickConnect(View view) {


        return;
    }

    private boolean disconnect() {
        if (mService.getTelnet().disconnect()) {
            return true;
        }
        return false;
    }

    public void onClickDisconnect(View view) {
        if (mService.getTelnet() != null && mService.getTelnet().isConnected()) {
            if (disconnect()) {
                toastFast("Disconnected from server");
            }
            else
                toastFast("Error disconnecting from server");
        }
        else {
            toastFast("Already disconnected");
        }
        return;
    }
    
    public void onClickLogin(View view) {
        if(mService!=null){
            EditText etusername = (EditText) findViewById(R.id.username);
            EditText etpassword = (EditText) findViewById(R.id.password);
            if (!etIsEmpty(etusername)&&!etIsEmpty(etpassword)) {
              mService.login(etusername.getText().toString(), etpassword.getText().toString());
            }
            
        }
    }

    public void onClickSend(View view) {
        EditText command =  (EditText) findViewById(R.id.EditTextCommand);
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                if(mService!=null){
                    mService.getUsers();
                }
            }
        }).start();
        

    }

    public void toastFast(String str) {
        mHandler.obtainMessage(2, str).sendToTarget();
    }

    private boolean etIsEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }


    public void setConsole(String str) {
        mHandler.obtainMessage(1, str).sendToTarget();
        return;
    }
    public void addServerMessage(SpannableString message) {
        server_message.append(message);
        //server_message.scrollTo(x, y);
    }

    @Override
    public void serverConnect() {
        mHandler.obtainMessage(1, "Service connected").sendToTarget();
        
    }

    @Override
    public void serverDisconnect() {
        mHandler.obtainMessage(1, "Service disconnected").sendToTarget();
        
    }

    @Override
    public void loginSuccess() {
        mHandler.obtainMessage(1, "Login success").sendToTarget();
        
    }

    @Override
    public void invalidPassword() {
        mHandler.obtainMessage(1, "Invalid Password").sendToTarget();
    }

    @Override
    public void serverMessage(String message) {
        mHandler.obtainMessage(0, message).sendToTarget();
    }
    
    private ServiceConnection myLocalServiceConnection = new ServiceConnection() {  
        public void onServiceConnected(android.content.ComponentName name,  
                android.os.IBinder service) {  
            MyIBinder myIBinder = (MyIBinder) service;  
            mService = (IGSService) myIBinder.getService();
            mService.setIGSListener(ConsoleActivity.this);
        };  
  
        public void onServiceDisconnected(android.content.ComponentName name) {  
          
        };  
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        onClickDisconnect(null);
        unbindService(myLocalServiceConnection);
    }  
    
    
    
}
