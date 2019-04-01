package com.example.xyx.wiselampdesk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.CommonActivities.BaseActivity;
import com.CommonActivities.DBService;
import com.CommonActivities.User;
import com.CommonActivities.myApplication;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends BaseActivity {

    private static final int CLOSE_DILOG = -1;
    private static final int INSERT_OK = 1;
    private static final int INSERT_FAIL = 2;
    private static final int USER_EXIST = 3;
    private static final int TAKE_PHOTO = 4;
    private static final int CROP_PHOTO = 5;

    private Uri imageUri,imageUri1;
    private ImageView pic,back_image;
    private EditText account, pass, repass;
    private File outputImage,outputImage1;

    private TextView back_text,pht;
    private Button register_button;

    private ProgressDialog pd;

    //管理活动
    private RegisterActivity regidCon = this;

    private User u = new User();
    private handler myhand = new handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        setContentView(R.layout.activity_register);
        Fresco.initialize(this);

        regidCon.addActivity();
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


        /**
         * 初始化控件
         */
        back_image=(ImageView)findViewById(R.id.back);
        back_text=(TextView)findViewById(R.id.back1);

        account=(EditText)findViewById(R.id.register_account);
        pass=(EditText)findViewById(R.id.register_password);
        repass=(EditText)findViewById(R.id.register_confirmpassword);

        //注册
        register_button=(Button)findViewById(R.id.btn_register);
        register_button.setOnClickListener(new Btn_Ensure());

        pic=(ImageView)findViewById(R.id.img_upload_img);
        pht = (TextView)findViewById(R.id.tx_upload_img);
        pht.setOnClickListener(new Btn_Take());

        /**
         * 点击返回返回到登陆界面
         */
        back_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                intent.putExtra("logout", "logout");
                startActivity(intent);
            }
        });
        back_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                intent.putExtra("logout", "logout");
                startActivity(intent);
            }
        });
    }


    /**********************************************************
     * 2018.07.04
     *******************************************************/

    /**
     * 处理器
     */
    public class handler extends Handler {

        public handler(){

        }

        public handler(Looper looper)
        {
            super(looper);
        }

        public void handleMessage(Message msg)
        {
            switch(msg.what){

                case CLOSE_DILOG:
                    pd.dismiss();// 关闭ProgressDialog
                    break;
                case INSERT_OK:
                    show_Toast("注册成功，请登陆。");
                    Intent inten = new Intent(RegisterActivity.this, LoginActivity.class);
                    inten.putExtra("Register_Clear", "clear");
                    startActivity(inten);
                    regidCon.removeActivity();
                    break;
                case INSERT_FAIL:
                    show_Toast("注册失败！");
                    break;
                case USER_EXIST:
                    show_Toast("用户名已存在！");
                    break;
                default:
                    show_Toast("什么鬼！");
                    break;
            }
        }
    }


    /**
     * 注册线程
     */
    class MyThread implements Runnable
    {
        @Override
        public void run()
        {
            DBService db = DBService.getDbService();
            Message message = new Message();
            if( db.getUserData(u).getAccount().equals("_PROTECTED_")) {
                if (db.insertUserData(u) > 0)
                {
                    message.what = INSERT_OK;
                }
                else
                    message.what = INSERT_FAIL;
            }
            else message.what = USER_EXIST;

            myhand.sendEmptyMessage(CLOSE_DILOG);
            myhand.sendMessage(message);
        }
    }


    /**
     * 拍照按钮监听器
     */
    class Btn_Take implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            outputImage = new File(Environment.getExternalStorageDirectory(),"myLight/light.jpg");
            outputImage1 = new File(Environment.getExternalStorageDirectory(),"myLight/light0.jpg");
            try{
                if (outputImage.exists() || outputImage1.exists())
                {
                    outputImage.delete();
                    outputImage1.delete();
                }
                outputImage.createNewFile();
                outputImage1.createNewFile();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
            imageUri = Uri.fromFile(outputImage);
            imageUri1 = Uri.fromFile(outputImage1);
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, TAKE_PHOTO);
        }
    }


    /**
     * 照片处理结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");
//                    intent.putExtra("scale", "true");
                    intent.putExtra("crop", "true");
                    // 裁剪框的比例，1：1
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    // 裁剪后输出图片的尺寸大小
                    intent.putExtra("outputX", 800);
                    intent.putExtra("outputY", 1000);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri1);
                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK){
                    try{
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri1));
                        pic.setImageBitmap(bitmap);
                        u.setImg(imageUri1);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }


    /**
     * 注册按钮
     */
    class Btn_Ensure implements View.OnClickListener
    {

        @Override
        public void onClick(View view)
        {
            u.setAccount(account.getText().toString());
            u.setPassword(pass.getText().toString());
            String re = repass.getText().toString();

            //trim()消除什么影响来着？
            if( account.getText().toString().trim().equals("") ||  pass.getText().toString().trim().equals(""))
            {
                show_Toast("账号或密码不能为空！");
            }

            else if( u.getPassword().equals(re))
            {

                pd = ProgressDialog.show(RegisterActivity.this, "注册", "注册中……");
                MyThread myThread = new MyThread();
                new Thread(myThread).start();
            }
            else {
                show_Toast("两次输入密码不一致！");
                pass.getText().clear();
                repass.getText().clear();
                pass.clearFocus();
            }
        }
    }

}
