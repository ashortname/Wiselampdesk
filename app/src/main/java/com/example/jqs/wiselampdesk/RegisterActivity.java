package com.example.jqs.wiselampdesk;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.*;
import android.widget.*;

import com.CommonActivities.BaseActivity;
import com.CommonActivities.DBService;
import com.CommonActivities.User;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.*;


public class RegisterActivity extends BaseActivity {

    private static final int CLOSE_DILOG = -1;
    private static final int INSERT_OK = 1;
    private static final int INSERT_FAIL = 2;
    private static final int USER_EXIST = 3;
    private static final int TAKE_PHOTO = 4;
    private static final int CROP_PHOTO = 5;
    /*********************
     * 权限申请代码
     */
    public static final int RC_CHOOSE_PHOTO = 2;
    //拍照权限申请
    public static final int RC_TAKE_PHOTO = 1;

    //*******************popwindow
    private PopupWindow popwin;
    private View popView;

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
        //pht.setOnClickListener(new Btn_Take());
        pht.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                //创建弹出式菜单对象（最低版本11）
//                final PopupMenu popup = new PopupMenu(RegisterActivity.this, v);//第二个参数是绑定的那个view
//                //获取菜单填充器
//                MenuInflater inflater = popup.getMenuInflater();
//                //填充菜单
//                inflater.inflate(R.menu.pop_reg, popup.getMenu());
//                //绑定菜单项的点击事件
//                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    public boolean onMenuItemClick(MenuItem item) {
//
//                        switch(item.getItemId())
//                        {
//                            case R.id.takephoto:
//                                if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
//                                    //未授权，申请授权(从相册选择图片需要读取存储卡的权限)
//                                    ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_TAKE_PHOTO);
//                                }else {
//                                    TakePhoto();
//                                }
//                                popup.dismiss();
//                                break;
//                            case R.id.choosephoto:
//                                if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                                    //未授权，申请授权(从相册选择图片需要读取存储卡的权限)
//                                    ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RC_CHOOSE_PHOTO);
//                                } else {
//                                    //已授权，获取照片
//                                    choosePhoto();
//                                }
//                                popup.dismiss();
//                                break;
//                            case R.id.exitReg:
//                            default:
//                                popup.dismiss();
//                                break;
//                        }
//                        return true;
//                    }
//                });
//                //显示(这一行代码不要忘记了)
//                popup.show();

                //*******************************
                if(popwin == null ){
                    //变暗
                    lightoff();

                    popView = View.inflate(RegisterActivity.this, R.layout.popwin_reg, null);
                    popwin = new PopupWindow(popView, WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.WRAP_CONTENT);

                    popwin.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            lighton();
                            popwin = null;
                        }
                    });

                    // 设置点击popupwindow外屏幕其它地方消失
                    popwin.setOutsideTouchable(true);

                    //拍照
                    popView.findViewById(R.id.text_takephoto).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                                //未授权，申请授权(从相册选择图片需要读取存储卡的权限)
                                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_TAKE_PHOTO);
                            }else {
                                TakePhoto();
                            }

                            popwin.dismiss();
                        }
                    });

                    //相册
                    popView.findViewById(R.id.text_choosephoto).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                //未授权，申请授权(从相册选择图片需要读取存储卡的权限)
                                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RC_CHOOSE_PHOTO);
                            } else {
                                //已授权，获取照片
                                choosePhoto();
                            }

                            popwin.dismiss();
                        }
                    });
                }

                // 在点击之后设置popupwindow的销毁
                if (popwin.isShowing()) {
                    popwin.dismiss();
                }

                //底部显示
                popwin.showAtLocation(RegisterActivity.this.findViewById(R.id.back), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });



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


    /**
     * 设置手机屏幕亮度变暗
     */
    private void lightoff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().setAttributes(lp);
    }

    /**
     * 设置手机屏幕亮度显示正常
     */
    private void lighton() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1f;
        getWindow().setAttributes(lp);
    }


    /**********************************************************
     * 2019.05.22
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

    /*****************************
     * 拍照
     */
    private void TakePhoto()
    {
        //初始化存储的file对象
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

    /**********************************
     * 从相册选取照片
     */
     private void choosePhoto()
     {
         Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
         intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
         startActivityForResult(intentToPickPic, RC_CHOOSE_PHOTO);
     }

    /**
     权限申请结果回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_TAKE_PHOTO:   //拍照权限申请返回
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    TakePhoto();
                }
                break;
            case RC_CHOOSE_PHOTO:   //相册选择照片权限申请返回
                choosePhoto();
                break;
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
            case RC_CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    imageUri1 = data.getData();
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
