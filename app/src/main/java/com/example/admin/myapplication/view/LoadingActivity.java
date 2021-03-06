package com.example.admin.myapplication.view;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.widget.Toast;

import com.example.admin.myapplication.BluetoothConstants;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.BluetoothService;
import com.example.admin.myapplication.vo.Lock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ObjectInputStream ois = null;
                lockManager = new ArrayList<Lock>();
                try
                {
                    Log.i(TAG, "get lock-info" );
                    ois = new ObjectInputStream(new FileInputStream(getFilesDir() + "lock_info.ser"));
                    Lock lock;
                    StringTokenizer token;
                    String temp;

                    Log.d(TAG, "read lock-info" );
                    while(true)
                    {
                        lock = (Lock)ois.readObject();
                        /*데이터 파싱*/
                        lockManager.add(lock);
                    }
                    //Log.i(TAG, "close lock-info" );
                    //ois.close();

                }
                catch (FileNotFoundException e)
                {
                    Log.d(TAG, "lock-info.ser not found");
                    try
                    {
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getFilesDir() + "lock_info.ser", false));
                        oos.close();
                    }
                    catch (IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                    e.printStackTrace();
                } catch(EOFException e){
                    if(ois != null) {
                        try {
                            ois.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                } catch(IOException e){
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                // 스마트폰에서 블루투스 기능 지원하는지 확인
                if(BluetoothAdapter.getDefaultAdapter() == null)
                {
                    Toast.makeText(getApplicationContext(), "블루투스 연결을 지원하지 않는 디바이스입니다!", Toast.LENGTH_LONG).show();
                    finish();
                }
                Intent intent = null;
                //등록된 디바이스 test할때는 != 0으로
                if(lockManager.size() != 0)
                {
                    /*
                     *등록된 자물쇠가 없는경우
                     */
                    intent = new Intent(getApplicationContext(), AddLockActivity.class);
                    intent.putParcelableArrayListExtra("lock_manager", lockManager);
                }
                else
                {
                    /* n
                     * 등록된 자물쇠가 존재하는 경우
                     */
                    //test용
                    Lock lock = new Lock();
                    lock.setMacAddr("98:D3:63:00:01:44");
                    lock.setState(1);
                    lock.setBattery(100);
                    lock.setOrder(1);
                    lockManager.add(lock);

                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putParcelableArrayListExtra("lock_manager", lockManager);

                }

                startActivity(intent);
                finish();
            }
        }, 2000);

    }
}
