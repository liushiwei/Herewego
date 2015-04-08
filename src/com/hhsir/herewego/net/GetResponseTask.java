
package com.hhsir.herewego.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.R.integer;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class GetResponseTask extends AsyncTask<Object, Void, String> {

    public GetResponseTask() {
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            Telnet client = (Telnet) params[0];
            BufferedInputStream instr = client.getStream();
            String cmd = (String) params[1];
            if (cmd != null && cmd.length() > 0)
                client.sendCommand(cmd);
            publishProgress();
            try {
                // Need to implement listener ASAP
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            int len = instr.available();
            int sleep_time = 0;
            while (len == 0) {
                try {
                    // Need to implement listener ASAP
                    Log.e("Telnet", "sleep = " + 300);
                    sleep_time+=300;
                    Thread.sleep(300);
                    if(sleep_time>2500){
                        return null;
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                len = instr.available();
            }
            Log.e("Telnet", "len = " + len);
            if (len > 0) {
                byte[] buff = new byte[len];
                int total = 0;
                do {
                    total += instr.read(buff, total, len);
                } while (total != len);

                Log.e("Telnet", "ret_read = " + total);
                Log.e("Telnet", "getCharset = " + client.getCharset());
                String res = new String(buff, 0, total,"Shift_JIS");
                Log.i("readline", res);
                return res;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

}
