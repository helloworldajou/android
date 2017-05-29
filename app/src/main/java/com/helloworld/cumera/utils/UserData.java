package com.helloworld.cumera.utils;


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