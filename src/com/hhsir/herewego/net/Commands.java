package com.hhsir.herewego.net;

public enum Commands {
    TOGGLE_CLIENT_TRUE("toggle client true",1),TOGGLE_CLIENT_FALSE("toggle client false",2),TOGGLE_QUIET_TRUE("toggle quiet true",3),TOGGLE_QUIET_FALSE("toggle quiet false",3),GAMES("games",4);
       
       private  String name;  
       private   int  index;  
       private  Commands(String name,  int  index) {  
           this .name = name;  
           this .index = index;  
       }
       public String getName() {
           return name;
       }
       public int getIndex() {
           return index;
       }
}
