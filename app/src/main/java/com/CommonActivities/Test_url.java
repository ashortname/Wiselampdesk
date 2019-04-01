package com.CommonActivities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Test_url {

    public static boolean isHostConnectable(String host, int port) {
        Socket s = new Socket();
        try {
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            s.connect(socketAddress, 1000);   //设置超时时间1s
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try{
            s.close();
        }catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
