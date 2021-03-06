
package com.hhsir.herewego.net;

import android.app.Activity;
import android.os.AsyncTask;

import com.hhsir.herewego.util.Log;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Locale;

public class Telnet {
    private TelnetClient client = null;
    private MessageHandler handler;
    private OutputStream outstream;
    // private ReaderThreadTask readerThread;
    private final String SERVER_IP;
    private final int SERVERPORT;
    private static Telnet singleTelnet;

    public Telnet(MessageHandler handler, String ip, int port) throws IOException {
        this.handler = handler;
        SERVER_IP = ip;
        SERVERPORT = port;
        client = new TelnetClient();
    }

    public void setMessageHandler(MessageHandler handler) {
        this.handler = handler;
    }

    public static Telnet getTelnet(String ip, int port) throws IOException {
        if (singleTelnet == null) {
            return singleTelnet = new Telnet(null, ip, port);
        } else {
            return singleTelnet;
        }
    }

    // TELNET
    public void connectIGSServer() {
//        ConnectTask connection = new ConnectTask();
//        connection.execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connectToServer();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void connectToServer() throws IOException {
        if (client.isConnected()) {
            disconnect();
            client = null;
        }
        if (client == null)
            client = new TelnetClient();

        if (SERVER_IP == null) {
            handler.toastFast("Enter the server address");
            return;
        }

        try {
            client.connect(SERVER_IP, SERVERPORT);

            handler.toastFast(String.format(Locale.ENGLISH, "Connected to %s,%d", SERVER_IP,
                    SERVERPORT));
            handler.setConsole(String.format("Connected to %s:%d\n", SERVER_IP, SERVERPORT));
            handler.serviceConnected();
            return;
        } catch (SocketException ex) {
            handler.toastFast("Connection error...");
            throw new SocketException("Connection error...");
        } catch (IOException ex) {
            handler.toastFast("Connection error...");
            Log.e(Telnet.class, ex.toString());
            throw new IOException("Connection error..."); // try next port
        }

    }

    // TELNET HELPERS
    public boolean sendCommand(String cmd) {
        if (client == null || !client.isConnected()) {
            handler.toastFast("Not connected to a server");
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(cmd);
        stringBuilder.append("\r\n");

        byte[] cmdbyte = stringBuilder.toString().getBytes();

        outstream = client.getOutputStream();
        Log.i(Telnet.class, (new String(cmdbyte, 0, cmdbyte.length)));

        try {
            outstream.write(cmdbyte, 0, cmdbyte.length);
            outstream.flush();
            return true;
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }

    }

    public ExpectResponseTask expectResponse(Activity activity) {
        return new ExpectResponseTask(activity);
    }

    public GetResponseTask getResponse() {
        return new GetResponseTask();

    }

    public BufferedInputStream getStream() {
        return (BufferedInputStream) client.getInputStream();
    }

    public Charset getCharset() {
        return client.getCharset();
    }

    public boolean isConnected() {
        if (client != null)
            return client.isConnected();
        else
            return false;
    }

    // exits telnet session and cleans up the telnet console
    public boolean disconnect() {

        try {
            if (outstream != null)
                outstream.close();
            if (client != null && client.isConnected())
                client.disconnect();
            client = null;
            outstream = null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        handler.serviceDisconnected();
        return true;

    }

    // THREADS
    private class ConnectTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                connectToServer();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public MessageHandler getMessageHandler() {
        return handler;
    }

}
