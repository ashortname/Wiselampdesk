package com.example.jqs.wiselampdesk;

import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.CommonActivities.BaseActivity;
import com.CommonActivities.DBService;
import com.CommonActivities.myApplication;

import java.sql.SQLException;

public class change_password extends BaseActivity {

    //定义控件
    private EditText passold;
    private EditText passnew;
    private EditText confirm_password;
    private Button confirm_btn;
    private ImageView imageView;
    private TextView textView;
    private String USERID,USERPASS;

    private change_password changeCon = this;

    /*******************************/
    private handler myhandler = new handler();
    private static final int PASSWORD_CHANGE_OK = 1;
    private static final int PASSWORD_CHANGE_FAIL = -1;
    private static final int ERROR = -2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_password);
        Intent intent = getIntent();
        USERID = intent.getStringExtra("userID");
        USERPASS = intent.getStringExtra("OldPass");

        changeCon.addActivity();

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


        //初始化控件
        passold = (EditText)findViewById(R.id.change_password_old);
        passnew=(EditText)findViewById(R.id.change_password_new);
        confirm_password=(EditText)findViewById(R.id.change_password_confirm);
        confirm_btn=(Button)findViewById(R.id.btn_change_password);
        imageView=(ImageView)findViewById(R.id.change_back);
        textView=(TextView) findViewById(R.id.change_back1);

        /**
         * 点击确认修改返回登录界面
         */
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldpass = passold.getText().toString().trim();
                String newpass = passnew.getText().toString().trim();
                String repass = confirm_password.getText().toString().trim();

                if(oldpass.equals(""))
                {
                    show_Toast("请输入原始密码！");
                    passold.clearFocus();
                }
                else if(newpass.equals(""))
                {
                    show_Toast("请输入新密码！");
                    passnew.getText().clear();
                    confirm_password.getText().clear();
                    passnew.clearFocus();
                }else if(repass.equals(""))
                {
                    show_Toast("请确认新密码！");
                    passnew.getText().clear();
                    confirm_password.getText().clear();
                    confirm_password.clearFocus();
                }else{
                        if(!USERPASS.equals(oldpass))
                        {
                            show_Toast("原始密码错误！");
                            passold.getText().clear();
                            passold.clearFocus();
                        }
                        else{
                            if(!oldpass.equals(newpass)) {
                                if (!newpass.equals(repass)) {
                                    show_Toast("新密码不一致！");
                                    passnew.getText().clear();
                                    confirm_password.getText().clear();
                                    passnew.clearFocus();
                                } else {
                                    PassWord_Change myThread = new PassWord_Change();
                                    new Thread(myThread).start();
                                }
                            }else{
                                show_Toast("当前密码一致！");
                            }
                        }
                }
            }
        });

        /**
         * 点击返回返回主界面
         */
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(change_password.this,MainActivity.class);
                startActivity(intent);
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(change_password.this,MainActivity.class);
                startActivity(intent);
            }
        });


    }

    /**
     * 密码修改线程
     */
    class PassWord_Change implements Runnable
    {
        @Override
        public void run()
        {
            try {
                DBService db = DBService.getDbService();
                int id;
                id = Integer.parseInt(USERID);
                int result = db.updatePass(id, passnew.getText().toString().trim());
                if(result > 0 )
                {
                    myhandler.sendEmptyMessage(PASSWORD_CHANGE_OK);
                }
                else
                    myhandler.sendEmptyMessage(PASSWORD_CHANGE_FAIL);
            }catch (SQLException e)
            {
                myhandler.sendEmptyMessage(ERROR);
            }
        }
    }

    /**
     * 处理器
     */
    public class handler extends Handler {

        handler(){

        }

        handler(Looper looper)
        {
            super(looper);
        }

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case PASSWORD_CHANGE_OK :
                    show_Toast("修改成功！请重新登录！");
                    Intent intent=new Intent(change_password.this,LoginActivity.class);

//                    changeCon.removeActivity();
                    myApplication.removeALLActivity_();
                    startActivity(intent);
                    break;
                case PASSWORD_CHANGE_FAIL :
                    show_Toast("修改失败！");
                    passnew.getText().clear();
                    passold.getText().clear();
                    confirm_password.getText().clear();
                    passold.clearFocus();
                    break;
                case ERROR :
                    show_Toast("未知错误！");
                    break;
                default:
                    break;
            }
        }
    }
}
