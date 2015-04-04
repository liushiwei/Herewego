package com.hhsir.herewego.net;

public interface MessageHandler {
    
    public void toastFast(String message);
    public void setConsole(String message);
    
    public void setMessage(String message);
    public void serviceConnected();
    public void serviceDisconnected();
}
