package com.example.admin.myapplication.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.BluetoothConstants;
import com.example.admin.myapplication.controller.BluetoothService;
import com.example.admin.myapplication.controller.OpenWeatherAPITask;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.vo.Lock;
import com.example.admin.myapplication.vo.Weather;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "Main";

    private ImageView unlock;
    private ImageView lock;
    private Button refresh;

    private BluetoothService btService;
    private StringBuffer outStringBuffer;

    private ArrayList<Parcelable> lockManager;
    private int sendingState;
    private StringBuilder stringBuilder = new StringBuilder("");
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch(msg.what)
            {
                case BluetoothConstants.REQUEST_ENABLE_BT:
                    Log.i(TAG,"MESSAGE_STATE_CHANGE"+msg.arg1);
                    switch (msg.arg1)
                    {
                        case BluetoothConstants.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(),"블루투스연결성공",Toast.LENGTH_SHORT).show();
                            //int state = ((Lock)lockManager.get(0)).getState();
                            int state = 0;
                            btService.write("3".getBytes(), BluetoothConstants.MODE_REQUEST);
                            //0 - 잠금 1 - 열림
                            if(state == 0) {
                                lock.setVisibility(View.VISIBLE);
                                unlock.setVisibility(View.GONE);
                                refresh.setVisibility(View.GONE);
                            }
                            break;

                        case BluetoothConstants.STATE_FAIL:
                            Toast.makeText(getApplicationContext(),"블루투스연결실패",Toast.LENGTH_SHORT).show();
                            lock.setVisibility(View.GONE);
                            unlock.setVisibility(View.GONE);
                            refresh.setVisibility(View.VISIBLE);
                            break;

                    }
                    break;

                case BluetoothConstants.MESSAGE_WRITE:
                    String writeMessage=null;
                    if(msg.arg1 == 1){
                        refresh.setVisibility(View.GONE);
                        lock.setVisibility(View.GONE);
                        unlock.setVisibility(View.VISIBLE);
                    }else if(msg.arg1 == 0){
                        Toast.makeText(getApplicationContext(), "전송실패! 다시시도해주세용ㅠㅅㅠ",Toast.LENGTH_SHORT).show();
                    }
                    break;

                case BluetoothConstants.MESSAGE_READ:

                    byte[] readBuf = (byte[]) msg.obj;


                    String strInput = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, "read data!"+strInput+"end!");
                    stringBuilder.append(strInput);

                    int len = stringBuilder.indexOf("\r\n");
                    Toast.makeText(getApplicationContext(), "len : " + len, Toast.LENGTH_LONG).show();

                    if(len > 0){
                        String print = stringBuilder.substring(0, len);
                        stringBuilder.delete(0, stringBuilder.length());
                        if('1' != (print.charAt(0))) {
                            refresh.setVisibility(View.GONE);
                            unlock.setVisibility(View.GONE);
                            lock.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(getApplicationContext(), "print : " + print.charAt(0), Toast.LENGTH_LONG).show();
                    }
                    break;

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


                    BluetoothDevice device;
                    //lockManager.get(0);//첫번째 자물쇠 디바이스의 맥 어드레스 가져오기
                    if(btService == null)
                        btService = new BluetoothService(this, handler);
                    device = btService.getDeviceInfo("98:D3:63:00:01:44");
                    btService.connect(device);
                    //btService.write("3".getBytes(), BluetoothConstants.MODE_REQUEST);

                }
                else
                {
                    Log.d(TAG, "Bluetooth is not enable");
                    Toast.makeText(getApplicationContext(), "블루투스 연결이 필요합니다!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            /*case BluetoothConstants.REQUEST_CONNECT_DEVICE:
                if(resultCode==Activity.RESULT_OK)
                {
                    BluetoothDevice device = btService.getDeviceInfo(data);
                    btService.connect(device);
                }
                break;*/
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        lockManager = intent.getParcelableArrayListExtra("lock_manager");

        btService = null;
        unlock = (ImageView)findViewById(R.id.unlock);
        lock = (ImageView)findViewById(R.id.lock);
        refresh = (Button)findViewById(R.id.btn_refresh);

        outStringBuffer = new StringBuffer("");

        //여기서 디비에서 불러온 상태값에 따라 unlock을 visible할지 lock을 visible할지 결정(default는 lock이 visible)
        final TextView tem = (TextView)findViewById(R.id.tem);
        Button getWeatherBtn = (Button)findViewById(R.id.getWeatherBtn);

        getWeatherBtn.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                int longitude;
                int latitude;
                EditText tvLon = (EditText) findViewById(R.id.lon);
                EditText tvLat = (EditText) findViewById(R.id.lat);

                longitude = Integer.parseInt(tvLon.getText().toString());
                latitude = Integer.parseInt(tvLat.getText().toString());

                // 날씨를 읽어오는 API 호출
                OpenWeatherAPITask t= new OpenWeatherAPITask();
                try
                {
                    Weather w = t.execute(latitude, longitude).get();

                    String temperature = String.valueOf(w.getTemp() - 273.15);
                    tem.setText(temperature);

                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
        });
        lockManager = (ArrayList<Parcelable>)intent.getParcelableArrayListExtra("lock_manager");

    }
    @Override
    protected void onResume() {
        super.onResume();

        if(btService == null)
        {
            btService = new BluetoothService(this, handler);
        }
        //등록된 디바이스 bluetooth test할때는 != 0으로
        if(lockManager.size() != 0)
        {
            /*
             *등록된 자물쇠가 없는경우
             */
            startActivity(new Intent(getApplicationContext(), AddLockActivity.class));
            finish();
        }
        else {
            /*
             * 등록된 자물쇠가 존재하는 경우
             */
            if (btService.getBtAdapter().isEnabled()) {
                // 블루투스 기기의 사용가능여부가 true 일때
                Log.d(TAG, "Bluetooth Enable Now");

                BluetoothDevice device;
                //String address = ((Lock)lockManager.get(0)).getMacAddr();//첫번째 자물쇠 디바이스의 맥 어드레스 가져오기
                device = btService.getDeviceInfo("98:D3:63:00:01:44");
                btService.connect(device);
                //btService.write("3".getBytes(), BluetoothConstants.MODE_REQUEST);

            } else {
                Log.d(TAG, "Bluetooth Enable Request");
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i, BluetoothConstants.REQUEST_ENABLE_BT);
            }
        }
    }
    void do_refresh(View v){
        onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        btService.stop();
    }
    void do_unlock(View v)
    {
        if( btService.getState() == BluetoothConstants.STATE_CONNECTED)
        {
            //연결된 상태에서만 값을 보낸다.
            sendMessage("1", BluetoothConstants.MODE_REQUEST);
            //btService.write("1".getBytes(), BluetoothConstants.MODE_REQUEST);
        }
        else
        {
            unlock.setVisibility(View.GONE);
            lock.setVisibility(View.GONE);
            refresh.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "블루투스 연결을 먼저 해 주세요!! ", Toast.LENGTH_SHORT).show();
        }

    }



    void do_lock(View v)
    {
        if( btService.getState() == BluetoothConstants.STATE_CONNECTED)
        {
            //연결된 상태에서만 값을 보낸다.
            sendMessage("0", BluetoothConstants.MODE_REQUEST);
            //오픈이미지로 바꾸기
        }
    }
    void go_management(View v)
    {
        startActivity(new Intent(getApplicationContext(), LockManageActivity.class));
    }
    /*메시지를 보낼 메소드 정의*/
    private synchronized void sendMessage( String message, int mode )
    {
        if ( sendingState == BluetoothConstants.STATE_SENDING )
        {
            try
            {
                wait() ;
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        sendingState = BluetoothConstants.STATE_SENDING ;
        // Check that we're actually connected before trying anything
        if ( btService.getState() != BluetoothConstants.STATE_CONNECTED )
        {
            sendingState = BluetoothConstants.STATE_NO_SENDING ;
            return ;
        }

        // Check that there's actually something to send
        if ( message.length() > 0 )
        {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes() ;
            btService.write(send, mode) ;

            // Reset out string buffer to zero and clear the edit text field
            outStringBuffer.setLength(0);

        }
        sendingState = BluetoothConstants.STATE_NO_SENDING ;
        notify() ;
    }
}

