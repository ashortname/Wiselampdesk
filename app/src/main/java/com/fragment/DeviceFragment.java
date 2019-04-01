package com.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyx.wiselampdesk.R;
import com.example.xyx.wiselampdesk.addDevice;

/**
 * Created by lenovo on 2018/7/5.
 */

public class DeviceFragment extends Fragment {

    public static  DeviceFragment newInstance(String s1,String s2){
        DeviceFragment deviceFragment = new  DeviceFragment();
        Bundle bundle = new Bundle();
        bundle.putString("xyx1",s1);
        bundle.putString("xyx2",s2);
        deviceFragment.setArguments(bundle);
        return deviceFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_content, container, false);
        Bundle bundle = getArguments();
        String s1 = bundle.getString("xyx1");
        String s2=bundle.getString("xyx2");
        TextView textView = (TextView) view.findViewById(R.id.fragment_text_view);
        Button button=(Button)view.findViewById(R.id.fragment_btn);
        ImageView imageView=(ImageView)view.findViewById(R.id.fragment_image);
        textView.setText(s1);
        button.setText(s2);
        imageView.setImageResource(R.mipmap.device_lamp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),addDevice.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
