package com.CommonActivities;

import android.net.Uri;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Socket_service {
    private static final String HOST = "192.168.200.128";
    private static final int PORT = 9999;
    private static Socket_service send = null;
    private Socket socket = null;
    private OutputStream out = null;
    private InputStream in = null;
    private FileInputStream fil = null;

    private Socket_service()
    {

    }

    public static Socket_service getSend()
    {
        if(send == null)
            send = new Socket_service();
        return send;
    }

    public void  Connect()
    {
        try{
            socket = new Socket(HOST,PORT);
            out = socket.getOutputStream();
            in = socket.getInputStream();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void Close()
    {
        try{
            out.close();
            in.close();
            socket.close();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void Send_Msg(String str)
    {
        try{
            out.write(str.getBytes());
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void Send_Img(Uri uri)
    {
        byte[] buf = new byte[1024];
        int len = 0;
        try{
            fil = new FileInputStream(uri.getPath());
            while((len = fil.read(buf)) != -1)
            {
                out.write(buf, 0, len);
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void Recv_Msg()
    {
        byte[] buf = new byte[1024];
        try{
            int len = in.read(buf);
            System.out.println(new String(buf, 0, len));
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
