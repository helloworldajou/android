package com.tzutalin.dlibtest.Communication;

/**
 * Created by alsrn on 2017-05-25.
 */

public class Value {

    String eyes;
    String chin;

    public Value(String eyes, String chin){
        this.eyes = eyes;
        this.chin = chin;
    }

    public String getEyes() {
        return eyes;
    }

    public void setEyes(String eyes) {
        this.eyes = eyes;
    }

    public String getChin() {
        return chin;
    }

    public void setChin(String chin) {
        this.chin = chin;
    }
}
