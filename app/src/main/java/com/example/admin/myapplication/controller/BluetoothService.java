package com.example.admin.myapplication.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.os.Build;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.example.admin.myapplication.BluetoothConstants;


public class BluetoothService {
    //변수 설정

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "BluetoothService";

    public int mode ;
    private BluetoothAdapter btAdapter;
    private Activity activity;
    private Handler handler;
    private ConnectThread connectThread; // 변수명 다시
    private ConnectedThread connectedThread; // 변수명 다시
    private int state;// device
    private ArrayList<String> pairedDevicesArrayList = null;



    public BluetoothService(Activity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;

        //bluetoothAdapter 얻기
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    protected BluetoothService(Parcel in) {
        mode = in.readInt();
        state = in.readInt();
        pairedDevicesArrayList = in.createStringArrayList();
    }

    /* (1) getDeviceState() : 가장먼저 기기의 블루투스 지원여부를 확인한다.*/
    public boolean getDeviceState() {
        Log.d(TAG, "Check the Bluetooth support");
        if (btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");
            return false;
        } else {
            Log.d(TAG, "Bluetooth is available");
            return true;
        }
    }
    public void scanDevice() {

        Log.d(TAG, "Scan Device");
        this.pairedDevicesArrayList = new ArrayList<String>();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        Iterator<BluetoothDevice> iterator = pairedDevices.iterator();
        String macAddr;

        while(iterator.hasNext()){
            BluetoothDevice bluetoothDevice = iterator.next();
            macAddr = new String(bluetoothDevice.getAddress());
        }
    }
    /* getter */
    public List<String> getPairedDevicesArrayList() {
        return pairedDevicesArrayList;
    }
    public BluetoothAdapter getBtAdapter() { return btAdapter; }
    /* getState() : Bluetooth 상태를 get한다. */
    public synchronized int getState() {
        return state;
    }

    /* getDeviceInfo() : 기기의 주소를 가져와 정보를 리턴*/
    public BluetoothDevice getDeviceInfo(Intent data) {
        //MAC address를 가져온다.
        //String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        String address = "98:D3:63:00:01:44";
        Log.d(TAG, "Get Device Info \n" + "address : " + address);

        //BluetoothDevice object를 가져온다
        return btAdapter.getRemoteDevice(address);
    }
    public BluetoothDevice getDeviceInfo(String address) {

        //bobae - test할 맥어드레스
        address = "98:D3:63:00:01:44";

        Log.d(TAG, "Get Device Info \n" + "address : " + address);

        //BluetoothDevice object를 가져온다
        return btAdapter.getRemoteDevice(address);
    }



    /* connect() : ConnectThread 초기화와 시작 device의 모든 연결 제거*/
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (state == BluetoothConstants.STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }
        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(BluetoothConstants.STATE_CONNECTING);
    }


    /* start() : Thread관련 service를 시작합니다.*/
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }


    /* connected() : ConnectedThread 초기화*/
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        setState(BluetoothConstants.STATE_CONNECTED);
    }



    /* stop() : 모든 thread stop */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        setState(BluetoothConstants.STATE_NONE);
    }



    /* write() : 값을 쓰는 부분(보내는 부분) */
    public void write(byte[] out,int mode) { // Create temporary object
        ConnectedThread r; // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (state != BluetoothConstants.STATE_CONNECTED)
                return;
            r = connectedThread;
        } // Perform the write unsynchronized r.write(out); }
        r.write(out, mode) ;
    }


    /* connectionFailed() : 연결 실패했을때 */
    private void connectionFailed() {
        setState(BluetoothConstants.STATE_FAIL);
    }


    /* connectionLost() : 연결을 잃었을 때 */
    private void connectionLost() {
        setState(BluetoothConstants.STATE_LISTEN);
    }


    /* setState() : Bluetooth 상태를 set한다.*/
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + this.state + " -> " + state);
        this.state = state;

        // 핸들러를 통해 상태를 메인에 넘겨준다.
        handler.obtainMessage(BluetoothConstants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    private class ConnectThread extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;

        public ConnectThread(BluetoothDevice device)
        {
            bluetoothDevice = device;
            BluetoothSocket tmp = null;
            //디바이스 정보를 얻어서 BluetoothSocket 생성
            try {
                if (Build.VERSION.SDK_INT >= 10) {
                    final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                    tmp = (BluetoothSocket) m.invoke(device, MY_UUID);

                }else{
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            bluetoothSocket = tmp;
        }
        public void run()
        {
            Log.i(TAG, "BEGIN connectThread");
            setName("ConnectThread");

            // 연결을 시도하기 전에는 항상 기기 검색을 중지한다.
            // 기기 검색이 계속되면 연결속도가 느려지기 때문이다.
            btAdapter.cancelDiscovery();

            // BluetoothSocket 연결 시도
            try
            {
                // BluetoothSocket 연결 시도에 대한 return 값은 succes 또는 exception이다.
                bluetoothSocket.connect();
                Log.d(TAG, "Connect Success");
            } catch(IOException e)
            {
                connectionFailed(); //연결 실패 시 불러오는 메소드
                Log.d(TAG, "Connect Fail");
                //소켓을 닫는다.
                try {
                    bluetoothSocket.close();
                }
                catch(IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                //연결 중 혹은 연결 대기상태인 메소드를 호출
                BluetoothService.this.start();
                return;
            }
            // ConnectThread 클래스를 reset한다.
            synchronized (BluetoothService.this) {
                connectThread = null;
            }
            // ConnectThread를 시작한다.
            connected(bluetoothSocket, bluetoothDevice);
        }
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create connectedThread");
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // BluetoothSocket의 inputstream 과 outputstream을 얻는다.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }
        public void run() {

            Log.i(TAG, "BEGIN connectedThread");
            byte[] buffer = new byte[256];
            int bytes;
            BluetoothService.this.write("9".getBytes(), BluetoothConstants.MODE_REQUEST);

            // Keep listening to the InputStream while connected
            while (true) {

                try {
                    // InputStream으로부터 값을 받는 읽는 부분(값을 받는다)
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(BluetoothConstants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer The bytes to write
         */
        public synchronized void write( byte[] buffer, int mode )
        {
            try {
                // 값을 쓰는 부분(값을 보낸다)
                outputStream.write(buffer);
                BluetoothService.this.mode = mode ;
                if(mode==BluetoothConstants.MODE_REQUEST)
                {
                    handler.obtainMessage(BluetoothConstants.MESSAGE_WRITE,1,-1,buffer).sendToTarget();
                }

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
                handler.obtainMessage(BluetoothConstants.MESSAGE_WRITE,0,-1,buffer).sendToTarget();
            }
        }
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }


    }

}
