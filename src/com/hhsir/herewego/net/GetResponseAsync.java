package com.hhsir.herewego.net;

import android.os.AsyncTask;

import com.hhsir.herewego.ConsoleActivity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GetResponseAsync extends AsyncTask<Void, Void, String> {
    final String cmd;
    final int timeout;
    GetResponseTask response;
    final Telnet client;
    public GetResponseAsync(Telnet client,String cmd, int timeout) {
        this.cmd = cmd;
        this.client = client;
        this.timeout = timeout;
        response = client.getResponse();
    }

    protected void onPreExecute() {
        response.execute(client, cmd);
    }

    @Override
    protected String doInBackground(Void... params) {
        String result;

        try {
            result = response.get(timeout, TimeUnit.MILLISECONDS);
            client.getMessageHandler().setMessage(result);
        } catch (InterruptedException | ExecutionException
                | TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        if (result == null) {
            return "";
        }
//        mHandler.obtainMessage(0, result).sendToTarget();
//        result = result.replace("\r\n", "");
//        result = result.replace(" ", "");

        return result;
    }

}
