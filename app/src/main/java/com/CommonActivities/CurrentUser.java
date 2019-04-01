package com.CommonActivities;

public class CurrentUser {
    static int id;
    static String Account;
    static String PassWord;

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        CurrentUser.id = id;
    }

    public static String getAccount() {
        return Account;
    }

    public static void setAccount(String account) {
        Account = account;
    }

    public static String getPassWord() {
        return PassWord;
    }

    public static void setPassWord(String passWord) {
        PassWord = passWord;
    }
}
