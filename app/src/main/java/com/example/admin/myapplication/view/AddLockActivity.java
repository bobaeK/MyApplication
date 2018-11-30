package com.example.admin.myapplication.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.BluetoothConstants;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.BluetoothService;
import com.example.admin.myapplication.vo.Lock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class AddLockActivity extends AppCompatActivity {
    private static final String D_NAME = "DOBELOCK2";

    private static final String TAG = "addLockActivity";
    private static final boolean D=true;
    private ArrayList<Lock> lockManager;
    protected LinearLayout linearLayout;
    private BluetoothService btService;


    private EditText lockName;
    private EditText serialNum;
    //private TextView test;


    private String address = null;
    private String name = null;
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if(msg.what == BluetoothConstants.MESSAGE_STATE_CHANGE)
            {
                if(D) Log.i(TAG,"MESSAGE_STATE_CHANGE"+msg.arg1);
                switch (msg.arg1)
                {
                    case BluetoothConstants.STATE_CONNECTED:
                        Toast.makeText(getApplicationContext(),"블루투스연결성공",Toast.LENGTH_SHORT).show();

                        if(D_NAME.equals(name)) {
                            lockName.setText(name);
                            serialNum.setText(address);
                            serialNum.setEnabled(false);

                            //test.setText("등록완료!!");
                        }else{
                            lockName.setText("");
                            serialNum.setText("");
                            serialNum.setEnabled(true);

                            //test.setText("디바이스를 등록해주세요!");
                            Toast.makeText(getApplicationContext(), "잘못된 기기 등록입니다!!", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case BluetoothConstants.STATE_FAIL:
                        Toast.makeText(getApplicationContext(),"블루투스연결실패",Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        }
    };
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult " + requestCode);
        switch(requestCode)
        {
            //블루투스 On을 거부한 경우 처리하기(앱 종료)
            case BluetoothConstants.REQUEST_ENABLE_BT:
                //When the request to enable Bluetooth returns
                if(resultCode == Activity.RESULT_OK)
                {
                    //확인을 눌렀을 때
                    Log.d(TAG, "Bluetooth is enable");

                    Intent intent = new Intent(getApplicationContext(), DeviceListActivity.class);
                    startActivityForResult(intent, BluetoothConstants.REQUEST_CONNECT_DEVICE);

                }
                else
                {
                    Log.d(TAG, "Bluetooth is not enable");
                    Toast.makeText(getApplicationContext(), "블루투스 연결이 필요합니다!", Toast.LENGTH_LONG).show();
                    finish();
                }
            case BluetoothConstants.REQUEST_CONNECT_DEVICE:
                if(resultCode==Activity.RESULT_OK)
                {
                    address = data.getStringExtra("device_address");
                    name = data.getStringExtra("device_name");
                    btService.connect(btService.getDeviceInfo(address));

                    Log.i(TAG, address + ", " + name);
                }
                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);

        linearLayout = (LinearLayout)findViewById(R.id.linearlayout);
        lockManager = getIntent().getParcelableArrayListExtra("lock_manager");

        lockName = (EditText)findViewById(R.id.lock_name);
        serialNum = (EditText)findViewById(R.id.serial_num);
        //test = (TextView)findViewById(R.id.test);


        if(btService == null)
            btService = new BluetoothService(this, handler);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(btService != null)
            btService.stop();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
    void scanDeviceOnClickListener(View view){
        /*
         * 등록된 자물쇠가 존재하는 경우
         */
        if (btService.getBtAdapter().isEnabled()) {
            // 블루투스 기기의 사용가능여부가 true 일때
            Log.d(TAG, "Bluetooth Enable Now");
            Intent intent = new Intent(getApplicationContext(), DeviceListActivity.class);
            startActivityForResult(intent, BluetoothConstants.REQUEST_CONNECT_DEVICE);
        }
        else
        {
            Log.d(TAG, "Bluetooth Enable Request");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothConstants.REQUEST_ENABLE_BT);
        }
    }
    void registerOnClickListener(View view){
        name = lockName.getText().toString();
        address = serialNum.getText().toString();

        if(serialNum.isEnabled()){
            Toast.makeText(getApplicationContext(),"먼저 디바이스와 연결해주세요!!", Toast.LENGTH_SHORT).show();
            return;
        }else{
            Lock lock = new Lock();
            lock.setMacAddr(address);
            lock.setName(name);
            lockManager.add(lock);

            //파일에 저장
            try
            {
                BufferedWriter bw = new BufferedWriter(new FileWriter(getFilesDir() + "lock_info.txt"));

                for(int i = 0; i < lockManager.size(); ++i) {
                    Log.i(TAG, "get lock-info");
                    lock = lockManager.get(i);
                    bw.append(lock.getName());
                    bw.append('\n');
                    bw.append(String.valueOf(lock.getOrder()));
                    bw.append('\n');
                    bw.append(lock.getMacAddr());
                    bw.append('\n');
                    bw.append(String.valueOf(lock.getBattery()));
                    bw.append('\n');
                    bw.append(String.valueOf(lock.getState()));
                    bw.append('\n');
                    Log.d(TAG, "write lock-info");
                }
                bw.append("%%end%%");
                bw.close();
            }
            catch (FileNotFoundException e)
            {
                Log.d(TAG, "lock-info.txt not found");
                e.printStackTrace();
            }
            catch (IOException e)
            {
                Log.i(TAG, "something wrong" );
                e.printStackTrace();
            }
        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putParcelableArrayListExtra("lock_manager", lockManager);
        startActivity(intent);
        finish();
    }

    void cancelOnClickListener(View view){
        if(lockManager.size() == 0){

        }else{

        }
        finish();
    }
}