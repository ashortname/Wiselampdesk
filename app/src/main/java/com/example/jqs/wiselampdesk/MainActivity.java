package com.example.jqs.wiselampdesk;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import com.CommonActivities.BaseActivity;
import com.CommonActivities.User;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.fragment.DeviceFragment;
import com.fragment.HomeFragment;
import com.fragment.SceneFragment;

import java.io.FileNotFoundException;

public class MainActivity extends BaseActivity {

/*******************************************/
    //底部菜单栏控件定义
    private BottomNavigationBar mBottomNavigationBar;
    private SceneFragment sceneFragment;
    private HomeFragment homeFragment;
    private DeviceFragment deviceFragment;

    //侧滑菜单栏定义
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menu,img;
    private TextView textid,textname;
    private TextView textView;

    private MainActivity mainCon = this;

    //用户数据
    User user = new User();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        setContentView(R.layout.activity_main);

        mainCon.addActivity();

       textView=(TextView)findViewById(R.id.text_change);

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


        //获取传过来的用户名、ID和头像
        Intent intent = getIntent();
        user.setAccount(intent.getStringExtra("name"));
        user.setID(Integer.parseInt(intent.getStringExtra("id")));
        user.setPassword(intent.getStringExtra("password"));
        String name = "Name："+ user.getAccount();
        String id = "ID："+ user.getID();
        Uri uri = null;
        try {
            uri = Uri.parse((String) intent.getStringExtra("uri"));
        }catch (Exception e)
        {

        }

        drawerLayout = (DrawerLayout) findViewById(R.id.activity_na);
        navigationView = (NavigationView) findViewById(R.id.nav);
        menu= (ImageView) findViewById(R.id.main_menu);
        View headerView = navigationView.getHeaderView(0);//获取头布局

        /*******
         * 侧滑菜单获取头布局的控件并修改
         */
        img = (ImageView) headerView.findViewById(R.id.person);
        textid = (TextView) headerView.findViewById(R.id.head_id);
        textname = (TextView) headerView.findViewById(R.id.head_name);
        textid.setText(id);
        textname.setText(name);
        if(uri != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                img.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (Exception e)
            {

            }
        }else{

        }


        menu.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               switch (view.getId()){
                   case R.id.main_menu://点击菜单，跳出侧滑菜单
                       if (drawerLayout.isDrawerOpen(navigationView)){
                           drawerLayout.closeDrawer(navigationView);
                       }else{
                           drawerLayout.openDrawer(navigationView);
                       }
                       break;
               }
           }
       });

        /**
         * 侧滑菜单栏item点击事件
         */
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.change_password:
                        Intent intent=new Intent(MainActivity.this,change_password.class);
                        intent.putExtra("userID", String.valueOf(user.getID()));
                        intent.putExtra("OldPass", user.getPassword());
                        startActivity(intent);
                        break;
                    case R.id.exit_login:
                        Intent intent1=new Intent(MainActivity.this,LoginActivity.class);
                        intent1.putExtra("logout", "logout");
                        startActivity(intent1);
                        mainCon.removeActivity();
                        break;
                }
                return true;
            }
        });

        /**
         * 为底部导航栏添加项目
         */
        mBottomNavigationBar = (BottomNavigationBar)findViewById(R.id.bottom_navigation_bar);
        mBottomNavigationBar
                .addItem(new BottomNavigationItem(R.mipmap.ic_scene, "情景"))
                .addItem(new BottomNavigationItem(R.mipmap.ic_home,"房间"))
                .addItem(new BottomNavigationItem(R.mipmap.ic_device, "设备"))
                .setFirstSelectedPosition(0)//设置默认选择item
                .initialise();//初始化


        mBottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();

                switch (position) {
                    case 0:
                        if (sceneFragment== null) {
                            sceneFragment = SceneFragment.newInstance(getString(R.string.scene),getString(R.string.btn_scene));
                        }
                        textView.setText("情景");
                        beginTransaction.replace(R.id.sub_content, sceneFragment);
                        break;
                    case 1:
                        if (homeFragment == null) {
                            homeFragment = HomeFragment.newInstance(getString(R.string.home),getString(R.string.btn_home));
                        }
                        textView.setText("房间");
                        beginTransaction.replace(R.id.sub_content,homeFragment);
                        break;
                    case 2:
                        if (deviceFragment == null) {
                            deviceFragment = DeviceFragment.newInstance(getString(R.string.device),getString(R.string.btn_device));
                        }
                        textView.setText("设备");
                        beginTransaction.replace(R.id.sub_content, deviceFragment );
                        break;
                }
                beginTransaction.commit();

            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });
        setDefaultFragment();


    }

    /**
     * set the default fragment
     */
    private void setDefaultFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        SceneFragment msceneFragment = sceneFragment.newInstance(getString(R.string.scene),getString(R.string.btn_scene));
        textView.setText("情景");
        transaction.replace(R.id.sub_content, msceneFragment).commit();

    }
}
