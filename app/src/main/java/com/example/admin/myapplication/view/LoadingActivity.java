package com.example.admin.myapplication.view;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.executor.FifoPriorityThreadPoolExecutor;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.vo.Lock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class LoadingActivity extends AppCompatActivity
{


    private ArrayList<Lock> lockManager;
    private static final String TAG = "Loading";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);


        ImageView gif = (ImageView)findViewById(R.id.gif_image);
        //GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(gif);
        GlideBuilder glideBuilder = new GlideBuilder(getApplicationContext());
        glideBuilder.setDiskCacheService(new FifoPriorityThreadPoolExecutor(4));
        glideBuilder.setResizeService(new FifoPriorityThreadPoolExecutor(4));

        Glide.with(this)
                .load(R.drawable.fluid_loader)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(gif);

        /*
        *자물쇠 데이터 가져오기
        */
        lockManager = new ArrayList<Lock>();
        try
        {
                Log.i(TAG, "get lock-info" );
                BufferedReader br = new BufferedReader(new FileReader(getFilesDir() + "lock_info.txt"));
                Lock lock;
                StringTokenizer token;
                String temp;

                Log.i(TAG, "read lock-info" );
                while((temp = br.readLine()) != null)
                {
                    lock = new Lock();
                    /*데이터 파싱*/
                    token = new StringTokenizer(temp);
                    //lock 정보 가져오기
                    lockManager.add(lock);
                }
                Log.i(TAG, "close lock-info" );
                br.close();

        }
        catch (FileNotFoundException e)
        {
            Log.i(TAG, "lock-info,txt not found");
            try
            {
                BufferedWriter bw = new BufferedWriter(new FileWriter(getFilesDir() + "lock_info.txt", false));
                bw.close();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }

            e.printStackTrace();
        }
        catch (IOException e)
        {
            Log.i(TAG, "something wrong" );
            e.printStackTrace();
        }

        // 스마트폰에서 블루투스 기능 지원하는지 확인

        if(BluetoothAdapter.getDefaultAdapter() == null)
        {
            Toast.makeText(getApplicationContext(), "블루투스 연결을 지원하지 않는 디바이스입니다!", Toast.LENGTH_LONG).show();
            finish();
        }
        //등록된 디바이스 test할때는 != 0으로
        if(lockManager.size() != 0)
        {
            /*
             *등록된 자물쇠가 없는경우
             */
            Intent intent = new Intent(getApplicationContext(), AddLockActivity.class);
            intent.putParcelableArrayListExtra("lock_manager", lockManager);
            startActivity(intent);
            finish();
        }
        else
        {
            /* n
             * 등록된 자물쇠가 존재하는 경우
             */

            //lockManager.get(0);//첫번째 자물쇠 디바이스의 맥 어드레스 가져오기
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            intent.putParcelableArrayListExtra("lock_manager", lockManager);
            startActivity(intent);
            finish();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
