package com.tzutalin.dlibtest.Communication;

/**
 * Created by alsrn on 2017-05-17.
 */

public class Data extends Value {

    String username;

    public Data(String username, String eyes, String chin){
        super(eyes, chin);
        this.username = username;

    }

    public String getUsername() {

        if(username == "")
            return "kimsup10";

        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}