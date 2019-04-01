package com.CommonActivities;

import android.net.Uri;
import android.os.Environment;

import java.io.*;

public class ImageUtil {

    public static Uri uri = null;

    // 读取本地图片获取输入流
    public static FileInputStream readImage(Uri path) throws IOException {
        return new FileInputStream(path.getPath());
    }

    // 读取表中图片获取输出流
    public static Uri readBin2Image(InputStream in, String name) {

//        File file = new File(Environment.getExternalStorageDirectory(), "myLigh/"+name+".jpg");
        File file = new File(Environment.getExternalStorageDirectory(), "myLight/"+name+"/");
        if(!file.exists())
        {
            file.mkdirs();
        }

        File myfile = new File(Environment.getExternalStorageDirectory(), "myLight/"+name+"/"+name+".jpg");

        try {
            if (myfile.exists()) {
                myfile.delete();
            }
            myfile.createNewFile();
        }catch (IOException e)
        {
            e.printStackTrace();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myfile);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            uri = Uri.fromFile(myfile);
        }catch (IOException e){
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return uri;
    }
}