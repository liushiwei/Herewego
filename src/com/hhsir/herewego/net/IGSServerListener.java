package com.hhsir.herewego.net;

public interface IGSServerListener {
    
    public void serverConnect();
    public void serverDisconnect();
    public void loginSuccess();
    public void invalidPassword();
    public void serverMessage(String message);

}
