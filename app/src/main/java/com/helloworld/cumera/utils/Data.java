package com.helloworld.cumera.utils;

public class Data extends Value {

    String username;

    public Data(String username, String eyes, String chin){
        super(eyes, chin);
        this.username = username;

    }

    public String getUsername() {

        if(username == "")
            return "unknown";

        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}