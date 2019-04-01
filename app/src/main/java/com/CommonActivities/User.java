package com.CommonActivities;

import android.net.Uri;

import java.io.InputStream;

public class User {
    int ID,Color_temp,Bright;
    String Account,Password;
    Uri img;
    InputStream in = null;

    public User(){
            setBright(100);
            setColor(100);
            setAccount("_PROTECTED_");
    }

    public User(int id,String account, int color_temp, int bright,String pass)
    {
        setID(id);
        setPassword(pass);
        setColor(color_temp);
        setBright(bright);
        setAccount(account);
    }

    public void setID(int id)
    {
        ID = id;
    }

    public void setPassword(String pass)
    {
        Password = pass;
    }

    public void setBright(int bright)
    {
        Bright = bright;
    }

    public void setColor(int color_temp)
    {
        Color_temp = color_temp;
    }

    public void setAccount(String account) {
        Account = account;
    }

    public void setImg(Uri uri)
    {
        img = uri;
    }

    public void setIn(InputStream in)
    {
        this.in = in;
    }

    public int getID() {
        return ID;
    }

    public int getBright() {
        return Bright;
    }

    public int getColor_temp() {
        return Color_temp;
    }

    public String getAccount() {
        return Account;
    }

    public String getPassword() {
        return Password;
    }

    public Uri getImg()
    {
        return img;
    }

    public InputStream getIn()
    {
        return in;
    }

}
