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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.admin.myapplication.BluetoothConstants;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.controller.BluetoothService;
import com.example.admin.myapplication.vo.Lock;

import java.util.ArrayList;

public class AddLockActivity extends AppCompatActivity {

    private static final String TAG = "addDevice";
    private static final boolean D=true;
    private ArrayList<Lock> lockManager;
    private LinearLayout linearLayout;
    private BluetoothService btService;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if(msg.what == BluetoothConstants.REQUEST_ENABLE_BT)
            {
                if(D) Log.i(TAG,"MESSAGE_STATE_CHANGE"+msg.arg1);
                switch (msg.arg1)
                {
                    case BluetoothConstants.STATE_CONNECTED:
                        Toast.makeText(getApplicationContext(),"블루투스연결성공",Toast.LENGTH_SHORT).show();
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

        Log.d(TAG, "onActivityResult" + resultCode);
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
                    String address = data.getStringExtra("device_address");
                    Lock lock= new Lock();
                    lock.setMacAddr(address);
                    lockManager.add(lock);
                    //BluetoothDevice device = btService.getDeviceInfo(address);

                    //btService.connect(device);
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
        if(btService == null)
        {
            btService = new BluetoothService(this, handler);
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
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putParcelableArrayListExtra("lock_manager", lockManager);
        startActivity(intent);
        finish();
    }
}
