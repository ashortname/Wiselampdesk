package com.example.xyx.wiselampdesk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import com.CommonActivities.*;

import java.io.File;

public class LoginActivity extends BaseActivity {

    private static final int CLOSE_DILOG = -2;
    private static final int  Link_Error = -1;
    private static final int  NO_ACCOUNT = 1;
    private static final int Pass_Error = 2;
    private static final int Login = 3;
    private static final String Host = "192.168.9.128";
    private static final int Port = 3306 ;

    private EditText edit_acc, edit_pas;

    //自动登录和记住密码的checkbox定义
    private CheckBox autologin;
    private CheckBox rememberpassword;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private boolean isRemenber, isAutoLogin;

//    //管理活动
    private LoginActivity loginCon = this;

    private handler myhand = new handler();
    User user = new User();
    User u = new User();

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        Intent intent = getIntent();

        pref= PreferenceManager.getDefaultSharedPreferences(this);
        /**
         * 自动登录和记住密码功能
         */
        rememberpassword = (CheckBox)findViewById(R.id.checkBox_password);
        autologin = (CheckBox)findViewById(R.id.checkBox_login);
        edit_acc = (EditText) findViewById(R.id.et_account);
        edit_pas = (EditText) findViewById(R.id.et_password);


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

        loginCon.addActivity();


        /**
         * 返回按钮点击事件
         */
        ImageView imageView=(ImageView)findViewById(R.id.back2);
        TextView textView=(TextView)findViewById(R.id.back3);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        /**
         * 点击按钮显示密码
         */
        ImageView see_picture=(ImageView)findViewById(R.id.iv_see_password);
        final EditText password = (EditText)findViewById(R.id.et_password);
        see_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });

        //连接检测
        sock_Thread myThread = new sock_Thread();
        new Thread(myThread).start();

        //创建文件夹
        File mfile = new File(Environment.getExternalStorageDirectory(), "myLight/");
        if(!mfile.exists())
        {
            mfile.mkdirs();
        }

        //事件绑定
        Button btn_zc = (Button)findViewById(R.id.btn_login_register);
        btn_zc.setOnClickListener(new Btn_regis());

        Button btn_lan = (Button)findViewById(R.id.btn_login);
        btn_lan.setOnClickListener(new Btn_lan());

        /****************
         * 自动登录
         */
        isRemenber= pref.getBoolean("remember_password",false);
        isAutoLogin = pref.getBoolean("autoLogin", false);
        if( intent.getStringExtra("Register_Clear") != null)
        {
            isAutoLogin = false;
            isRemenber = false;
        }
        if( intent.getStringExtra("logout") != null)
        {
            isAutoLogin = false;
        }
        if(isRemenber){
            //将账号和密码都设置到文本中
            String autoaccount = pref.getString("account","");
            String autopassword = pref.getString("password","");
//            user.setAccount(pref.getString("account","Account"));
//            user.setPassword(pref.getString("password","PassWord"));
            edit_acc.setText(autoaccount);
            edit_pas.setText(autopassword);
            rememberpassword.setChecked(true);
            if( isAutoLogin )
            {
                autologin.setChecked(true);
                new Btn_lan().onClick( btn_lan );
            }
        }
    }



    /**
     * 登录线程
     */
    class login_Thread implements Runnable
    {
        @Override
        public void run()
        {
            Message msg = new Message();
            DBService db = DBService.getDbService();
            u = db.getUserData(user);

            if(u.getAccount().equals("_PROTECTED_"))
            {
                msg.what = NO_ACCOUNT;
            }
            else if( !u.getPassword().equals(user.getPassword()))
            {
                msg.what = Pass_Error;
            }
            else{
                msg.what = Login;
                editor = pref.edit();
                if( rememberpassword.isChecked() ){
                    editor.putBoolean("remember_password",true);
                    editor.putString("account",u.getAccount());
                    editor.putString("password",u.getPassword());
                    if(autologin.isChecked())
                    {
                        editor.putBoolean("autoLogin", true);
                    }
                    else{
                        editor.putBoolean("autoLogin", false);
                    }
                }else {
                    editor.clear();
                }
                editor.apply();
            }

            myhand.sendEmptyMessage(CLOSE_DILOG);
            myhand.sendMessage(msg);
        }
    }

    /**
     * 检测线程
     */
    class sock_Thread implements Runnable
    {
        @Override
        public void run()
        {
            Message msg = new Message();
            if(!Test_url.isHostConnectable(Host,Port))
            {
                msg.what = Link_Error;
            }
            myhand.sendMessage(msg);
        }
    }

    /**
     * 注册
     */
    class Btn_regis implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            Intent intent = new Intent();
            intent.setAction("My_New_User_Regis");
            startActivity(intent);
        }
    }

    /**
     * 登录
     */
    class Btn_lan implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            user.setAccount(edit_acc.getText().toString());
            user.setPassword(edit_pas.getText().toString());
            //trim()去掉首尾空格
            if(edit_acc.getText().toString().trim().equals("") || edit_pas.getText().toString().trim().equals(""))
            {
                show_Toast("账号或密码不能为空！");
                edit_acc.getText().clear();
                edit_acc.clearFocus();
            }
            else{
                pd = ProgressDialog.show(LoginActivity.this, "登录", "正在登录……");
                login_Thread myThread = new login_Thread();
                new Thread(myThread).start();
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
                case CLOSE_DILOG:
                    pd.dismiss();// 关闭ProgressDialog
                    break;
                case Link_Error:
                    isAutoLogin = false;
                    show_Toast("无法连接至服务器！");
                    break;
                case NO_ACCOUNT:
                    show_Toast("账号不存在！");
                    break;
                case Pass_Error:
                    show_Toast("账号或密码错误！");
                    break;
                case Login:
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        try {
                            intent.putExtra("uri", u.getImg().toString());
                            Log.d("xyx", u.getImg().toString());
                        }catch (NullPointerException e)
                        {
                            show_Toast("用户数据异常！");
                        }finally {
                            CurrentUser.setAccount(u.getAccount());
                            CurrentUser.setId(u.getID());
                            CurrentUser.setPassWord(u.getPassword());
                            intent.putExtra("id", String.valueOf(u.getID()));
                            intent.putExtra("name", u.getAccount());
                            intent.putExtra("password", u.getPassword());
                            show_Toast("登陆成功");
                            startActivity(intent);
                            loginCon.removeActivity();
                        }
                    break;
                default:
                    break;
            }
        }
    }
}
