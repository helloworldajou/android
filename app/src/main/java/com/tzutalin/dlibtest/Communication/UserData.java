package com.tzutalin.dlibtest.Communication;

/**
 * Created by alsrn on 2017-05-25.
 */

public class UserData extends Data {

    private UserData() {
        super("", "0", "0");
    }

    private static class Singleton {
        private static final UserData instance = new UserData();
    }

    public static UserData getInstance() {
        return Singleton.instance;
    }
}