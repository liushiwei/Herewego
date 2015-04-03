
package com.hhsir.herewego;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConsoleActivity extends Activity {
    private Telnet client = null;
    private Toast fastToast;
    private static int SERVERPORT = 23;
    private static String SERVER_IP = "192.168.0.105";
    private static TextView et;
    private static TextView server_message;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        fastToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        et = (TextView) findViewById(R.id.inputStreamTextView);
        et.setMovementMethod(new ScrollingMovementMethod());
    }
    
    private Handler mHandler  = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            addServerMessage(msg.obj.toString());
            super.handleMessage(msg);
        }
        
    };

    public void onClickConnect(View view) {
        EditText etIp = (EditText) findViewById(R.id.EditTextIp);
        server_message= (TextView) findViewById(R.id.server_message);

        if (!etIsEmpty(etIp)) {
            String tmp = etIp.getText().toString();

            if (tmp.contains(":")) {
                String[] address = tmp.split(":");
                SERVER_IP = address[0];
                SERVERPORT = Integer.parseInt(address[1]);
            }
            else {
                SERVER_IP = etIp.getText().toString();
            }

            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("last_server", tmp);
            editor.commit();

        }
        else
            toastFast("Enter a server IP");

        if (client != null && client.isConnected())
            toastFast("Already connected");
        else
            try {
                client = new Telnet(this, SERVER_IP, SERVERPORT);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        GetResponseAsync responsetask = new GetResponseAsync("", 1000);
        try {
            Log.e("Telnet", responsetask.execute().get());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return;
    }

    private boolean disconnect() {
        if (client.disconnect()) {
            return true;
        }
        return false;
    }

    public void onClickDisconnect(View view) {
        if (client != null && client.isConnected()) {
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

    public void onClickSend(View view) {

        if (client == null || !client.isConnected()) {
            toastFast("Not connected to a server");
            return;
        }

        EditText et = (EditText) findViewById(R.id.EditTextCommand);

        // client.sendCommand(et.getText().toString());
        GetResponseAsync responsetask = new GetResponseAsync(et.getText().toString(), 1000);
        try {
            Log.e("Telnet", responsetask.execute().get());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    void toastFast(String str) {

        fastToast.setText(str);
        fastToast.show();
    }

    private boolean etIsEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    private class GetResponseAsync extends AsyncTask<Void, Void, String> {
        final String cmd;
        final int timeout;
        GetResponseTask response;

        public GetResponseAsync(String cmd, int timeout) {
            this.cmd = cmd;
            this.timeout = timeout;
            response = client.getResponse(ConsoleActivity.this);
        }

        protected void onPreExecute() {
            response.execute(client, cmd);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;

            try {
                result = response.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException
                    | TimeoutException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            if (result == null) {
                return "";
            }
            mHandler.obtainMessage(0, result).sendToTarget();
            result = result.replace("\r\n", "");
            result = result.replace(" ", "");

            return result;
        }

    }

    public void setConsole(String str) {
        et.setText(str);
        return;
    }
    public void addServerMessage(String message) {
        server_message.append(message);
        //server_message.scrollTo(x, y);
    }
}
