package com.example.xyx.wiselampdesk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.CommonActivities.BaseActivity;
import com.CommonActivities.CurrentUser;
import com.CommonActivities.DBService;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;

public class ControlActivity extends BaseActivity {
    private String TAG = "Control";

    private static final int ERROR = -1 ;
    private static final int MSG_CONNECT_SUCCESS = 0;
    private static final int MSG_CONNECT_FAILURE = 1;
    private static final int SAVE_CHANGES = 3;
    private static final String CMD_TOGGLE = "{\"id\":%id,\"method\":\"toggle\",\"params\":[]}\r\n" ;
    private static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n" ;
    private static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n" ;
    private static final String CMD_CT = "{\"id\":%id,\"method\":\"set_ct_abx\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_HSV = "{\"id\":%id,\"method\":\"set_hsv\",\"params\":[%value, 100, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS_SCENE = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_COLOR_SCENE = "{\"id\":%id,\"method\":\"set_scene\",\"params\":[\"cf\",1,0,\"100,1,%color,1\"]}\r\n";

    private int mCmdId;
    private Socket mSocket;
    private String mBulbIP;
    private int mBulbPort;
    private ProgressDialog mProgressDialog;
    private SeekBar mBrightness;
    private SeekBar mCT;
    private SeekBar mColor;
    private Button mBtnOn;
    private Button mBtnOff;
    private Button mBtnMusic;
    private BufferedOutputStream mBos;
    private BufferedReader mReader;
    private int Brightness, Color_Tem;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_CONNECT_FAILURE:
                    mProgressDialog.dismiss();
                    break;
                case MSG_CONNECT_SUCCESS:
                    mProgressDialog.dismiss();
                    break;
                case SAVE_CHANGES:
                    show_Toast("保存成功！");
                    break;
                case ERROR:
                    show_Toast("保存失败！");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Brightness = 80;
        Color_Tem = 0;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /**
         * 设置状态栏颜色
         */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        setContentView(R.layout.activity_control);

        //返回和提交设置按钮的设置
        ImageView imageView=(ImageView)findViewById(R.id.control_back);
        Button button=(Button)findViewById(R.id.control_btn);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ControlActivity.this,addDevice.class);
                startActivity(intent);
            }
        });

        /*********************
         * 保存设置的参数
         */
        //保存设置的参数
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
           public void run()
           {
               try {
                   DBService db = DBService.getDbService();
                   int result = db.Update_Set(Color_Tem, Brightness, CurrentUser.getId());
                   if(result > 0 )
                   {
                       mHandler.sendEmptyMessage(SAVE_CHANGES);
                   }
               }catch (SQLException e)
               {
                   mHandler.sendEmptyMessage(ERROR);
               }
           }
        }.start();
            }
        });

        mBulbIP = getIntent().getStringExtra("ip");
        mBulbPort = Integer.parseInt(getIntent().getStringExtra("port"));
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Connecting...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        mBrightness = (SeekBar) findViewById(R.id.brightness);
        mColor = (SeekBar) findViewById(R.id.color);
        mCT = (SeekBar) findViewById(R.id.ct);
        mCT.setMax(4800);
        mColor.setMax(360);
        mBrightness.setMax(100);

        mBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /********************************
             * 此处读取光照
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Brightness = seekBar.getProgress();
                write(parseBrightnessCmd(seekBar.getProgress()));
            }
        });
        mCT.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /*********************
             * 此处读取色温
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Color_Tem = seekBar.getProgress() + 1700;
                write(parseCTCmd(seekBar.getProgress() + 1700));
            }
        });

        mColor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /************************
             * 此处读取彩色
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                write(parseColorCmd(seekBar.getProgress()));
            }
        });

        /*************************
         * 台灯开关控制
         */
        mBtnOn = (Button) findViewById(R.id.btn_on);
        mBtnOff = (Button) findViewById(R.id.btn_off);
        mBtnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(parseSwitch(true));
            }
        });
        mBtnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(parseSwitch(false));
            }
        });
        connect();
    }

    private boolean cmd_run = true;
    private void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cmd_run = true;
                    mSocket = new Socket(mBulbIP, mBulbPort);
                    mSocket.setKeepAlive(true);
                    mBos= new BufferedOutputStream(mSocket.getOutputStream());
                    mHandler.sendEmptyMessage(MSG_CONNECT_SUCCESS);
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    while (cmd_run){
                        try {
                            String value = mReader.readLine();
                            Log.d(TAG, "value = "+value);
                        }catch (Exception e){

                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(MSG_CONNECT_FAILURE);
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            cmd_run = false;
            if (mSocket!=null)
                mSocket.close();
        }catch (Exception e){

        }

    }

    private String parseSwitch(boolean on){
        String cmd;
        if (on){
            cmd = CMD_ON.replace("%id", String.valueOf(++mCmdId));
        }else {
            cmd = CMD_OFF.replace("%id", String.valueOf(++mCmdId));
        }
        return cmd;
    }
    private String parseCTCmd(int ct){
        return CMD_CT.replace("%id",String.valueOf(++mCmdId)).replace("%value",String.valueOf(ct+1700));
    }
    private String parseColorCmd(int color){
        return CMD_HSV.replace("%id",String.valueOf(++mCmdId)).replace("%value",String.valueOf(color));
    }
    private String parseBrightnessCmd(int brightness){
        return CMD_BRIGHTNESS.replace("%id",String.valueOf(++mCmdId)).replace("%value",String.valueOf(brightness));
    }
    private void write(String cmd){
        if (mBos != null && mSocket.isConnected()){
            try {
                mBos.write(cmd.getBytes());
                mBos.flush();
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            Log.d(TAG,"mBos = null or mSocket is closed");
        }
    }
}
