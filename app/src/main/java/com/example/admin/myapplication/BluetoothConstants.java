package com.example.admin.myapplication;

public interface BluetoothConstants {



    //생성자 작성 끝!
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_LISTEN = 1; // now listening for incoming
    // connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing
    // connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote
    public static final int STATE_FAIL = 4;

    public static final int STATE_SENDING = 5;
    public static final int STATE_NO_SENDING = 6;


    public static final int REQUEST_CONNECT_DEVICE = 7;
    public static final int REQUEST_ENABLE_BT = 8;

    public static final int MODE_REQUEST = 9;

    public static final int MESSAGE_STATE_CHANGE = 10;
    public static final int MESSAGE_WRITE = 11;
    public static final int MESSAGE_READ = 12;
}
