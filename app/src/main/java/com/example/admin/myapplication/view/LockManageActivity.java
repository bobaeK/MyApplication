package com.example.admin.myapplication.view;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.R;

public class LockManageActivity extends AppCompatActivity {
    final static String TAG = "LockManageActivity";
    private LockManageAdapter adapter;
    private ViewPager viewPager;
    private Button selectBtn;

    private TextView lockNameTv;
    private TextView lockSerialTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_manage);

        selectBtn = (Button)findViewById(R.id.select_btn);
        lockNameTv = (TextView)findViewById(R.id.lock_name);
        lockSerialTv = (TextView)findViewById(R.id.lock_serial_num);


        viewPager = (ViewPager)findViewById(R.id.view);
        adapter = new LockManageAdapter(this, 3);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i(TAG, "onPageScrolled");
                Log.i(TAG, "position : "+position);
                Log.i(TAG, "positionOffset : "+positionOffset);
                Log.i(TAG, "positionOffsetPixels : "+positionOffsetPixels);

                if(positionOffset == 0 && positionOffsetPixels == 0)
                    lockNameTv.setText("lock"+(position + 1));

            }
            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected");
                Log.i(TAG, "position : "+position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.i(TAG, "onPageScrollStateChanged");
                Log.i(TAG, "state : "+state);
            }
        });
    }
    public void do_select(View view){
        int test = viewPager.getCurrentItem();
        Toast.makeText(getApplicationContext(), String.valueOf(test + 1), Toast.LENGTH_SHORT).show();
    }
}
