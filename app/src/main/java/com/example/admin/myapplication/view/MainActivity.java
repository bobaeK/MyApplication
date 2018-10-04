package com.example.admin.myapplication.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.BluetoothConstants;
import com.example.admin.myapplication.controller.BluetoothService;
//import com.example.admin.myapplication.controller.OpenWeatherAPITask;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.vo.Distance;
import com.example.admin.myapplication.vo.GMail;
import com.example.admin.myapplication.vo.GpsInfo;
import com.example.admin.myapplication.vo.Lock;
import com.example.admin.myapplication.vo.Weather;
import com.github.anastr.speedviewlib.SpeedView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, PlacesListener
{
    private static boolean lock_cut = false;
    private static boolean lock_wave = false;
    private static final String TAG = "MainActivity";
    private Lock curLock;
    private static final String TESTMACADDR = "98:D3:63:00:01:44";

    private ImageView unlock;
    private ImageView lock;
    private Button refresh;
    private ProgressBar connecting;
    private TextView battery;
    private ArrayList<Parcelable> lockManager;
    private BluetoothService btService;

    private StringBuffer outStringBuffer;
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
                case BluetoothConstants.MESSAGE_STATE_CHANGE:
                    if(msg.arg1 == BluetoothConstants.STATE_FAIL) {

                        unlock.setVisibility(View.GONE);
                        lock.setVisibility(View.GONE);
                        connecting.setVisibility(View.GONE);
                        refresh.setVisibility(View.VISIBLE);
                        battery.setText("");
                        Toast.makeText(MainActivity.this, "연결실패!! 다시시도해주세요", Toast.LENGTH_SHORT).show();
                    }else if(msg.arg1 == BluetoothConstants.STATE_CONNECTED) {
                        connecting.setVisibility(View.GONE);
                        battery.setText("배터리 정보 불러오는중...");

                    }else if(msg.arg1 == BluetoothConstants.STATE_LISTEN){
                        unlock.setVisibility(View.GONE);
                        lock.setVisibility(View.GONE);
                        connecting.setVisibility(View.GONE);
                        refresh.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "연결이 끊어졌어요!! 다시연결해주세요", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothConstants.MESSAGE_WRITE:
                    String writeMessage=null;
                    if(msg.arg1 == 1){

                    }else if(msg.arg1 == 0){
                        Toast.makeText(getApplicationContext(), "전송실패! 다시시도해주세요",Toast.LENGTH_SHORT).show();
                    }
                    break;

                case BluetoothConstants.MESSAGE_READ:

                    byte[] readBuf = (byte[]) msg.obj;


                    String strInput = new String(readBuf, 0, msg.arg1);

                    stringBuilder.append(strInput);

                    int len = stringBuilder.indexOf("\r\n");
                    //Toast.makeText(getApplicationContext(), "len : " + len, Toast.LENGTH_LONG).show();

                    if(len > 0){
                        String print = stringBuilder.substring(0, len);

                        Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                        NotificationCompat.Builder mBuilder;
                        NotificationManager mNotificationManager;

                        stringBuilder.delete(0, stringBuilder.length());
                        char c = print.charAt(0);

                        switch(c){
                            case '1':
                                //열림
                                refresh.setVisibility(View.GONE);
                                lock.setVisibility(View.GONE);
                                unlock.setVisibility(View.VISIBLE);

                                break;
                            case '2':
                                //닫힘
                                refresh.setVisibility(View.GONE);
                                unlock.setVisibility(View.GONE);
                                lock.setVisibility(View.VISIBLE);

                                break;
                            case '3':
                                vibrator.vibrate(500);//0.5초동안 울리기
                                //위험감지(진동)
                                if(!lock_wave) {
                                    lock_wave = true;
                                    mBuilder = createNotification("위험", "누군가 당신의 자물쇠를 억지로 열기위해 시도하고 있습니다.");
                                    mBuilder.setContentIntent(createPendingIntent());

                                    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(1, mBuilder.build());
                                }
                                break;
                            case '4':
                                //위험감지(끊어짐)
                                vibrator.vibrate(2000);//일초동안 울리기
                                if(!lock_cut) {
                                    lock_cut = true;
                                    mBuilder = createNotification("긴급", "누군가 당신의 자물쇠를 끊었습니다.");
                                    mBuilder.setContentIntent(createPendingIntent());

                                    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(1, mBuilder.build());
                                }
                                break;
                            default:
                                //베터리 잔량 표시
                                battery.setTextSize(35);
                                int index = 0;
                                while(print.charAt(++index) == '$');
                                int b = Integer.parseInt(print.substring(index));
                                b = 4500;
                                if(b > 5000)
                                    b = 5000;
                                else if(b < 3000)
                                    b = 3000;
                                b -= 3000;
                                b /= 20;
                                battery.setText(b + "%");
                                break;

                        }
                        //Log.d(TAG, "read : " + print);
                        //Toast.makeText(getApplicationContext(), "print : " + print, Toast.LENGTH_SHORT).show();
                    }
                    break;

            }
        }
    };



    /***********GPS 관련 변수***********/
    private GoogleApiClient googleApiClient = null;
    private GoogleMap googleMap = null;
    private Marker currentMarker = null;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    //private AppCompatActivity  mActivity;
    boolean askPermissionOnceAgain = false;
    boolean requestingLocationUpdates = false;
    Location currentLocatiion;
    boolean moveMapByUser = true;
    boolean moveMapByAPI = true;
    LatLng currentPosition;


    LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


    List<Marker> previous_marker = null; //18.07.31

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;

    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    //2018-08-14 추가내용
    private Button btn_timer_start, btn_timer_finish;
    private boolean isReset=true;
    private boolean isBtnClickStart = false;
    private int timer = 0;
    private double bef_lat=0.0,bef_long=0.0;
    Handler time_handler;
    private TextView tv_rideDis, tv_rideTime;
    double sum_dist=0, cur_lat=0, cur_long=0;

    // GPSTracker class
    private GpsInfo gps;

    /// 이하는 속도를 위한 추가
    double mySpeed = 0, maxSpeed = 0;
    private TextView txtCur, txtMax;
    private LocationManager locationManager;
    private LocationListener locationListener;


    //18.08.16 주행화면, 지도에서 사용되어지는 아이콘, 레이아웃
    private FrameLayout spdToMap, mapToSpd;

    //18.08.19 주행화면<-> 지도 애니메이션
    Animation translateLeftIn = null;
    Animation translateRightIn = null;
    Animation translateLeftOut = null;
    Animation translateRightOut = null;

    //Template 관련
    BottomNavigationView bottomNavigationView;
    private FrameLayout window1, window2, window3;

    //18.08.26 리스트뷰 관련
    ActionBarDrawerToggle drawerToggle;
    String [] drawer_str={"OBELOCK BODY"};
    DrawerLayout drawerLayout;
    ListView listView;

    //18.08.29 이메일 관련 변수
    FrameLayout windowEmail;
    TextView eAdress;
    EditText eContents, esubject;

    //18.08.29 상세설정 관련 변수
    Switch bluetoothSwitch, gpsSwitch;
    FrameLayout windowNotice, windowQna, windowStory;


    //18.08.28
    android.support.v7.app.ActionBar titleName;

    //18.09.03
    private RecyclerView recyclerNotice, recyclerQna;

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
                    device = btService.getDeviceInfo(TESTMACADDR);
                    btService.connect(device);
                }
                else
                {
                    Log.d(TAG, "Bluetooth is not enable");
                    Toast.makeText(getApplicationContext(), "블루투스 연결이 필요합니다!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : 퍼미션 가지고 있음");


                        if ( googleApiClient.isConnected() == false ) {

                            Log.d( TAG, "onActivityResult : mGoogleApiClient connect ");
                            googleApiClient.connect();
                        }
                        return;
                    }
                }
                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //18.08.28
        titleName = getSupportActionBar();
        //18.08.29 인터넷 사용권한 허용
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        Intent intent = getIntent();
        lockManager = intent.getParcelableArrayListExtra("lock_manager");

        unlock = (ImageView)findViewById(R.id.unlock);
        lock = (ImageView)findViewById(R.id.lock);
        refresh = (Button)findViewById(R.id.btn_refresh);
        connecting = (ProgressBar)findViewById(R.id.connecting);
        battery = (TextView)findViewById(R.id.battery);

        outStringBuffer = new StringBuffer("");




        //18.09.03 리사이클 공지사항
        recyclerNotice = (RecyclerView) findViewById(R.id.recyclerNotice);
        recyclerNotice.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        List<ExpandableListAdapter.Item> data = new ArrayList<>();
        ExpandableListAdapter.Item info = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, getResources().getString(R.string.info1));
        info.invisibleChildren = new ArrayList<>();
        info.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, getString(R.string.info1Context)));

        data.add(info);
        recyclerNotice.setAdapter(new ExpandableListAdapter(data));

        //18.09.03 리사이클 QnA
        recyclerQna = (RecyclerView) findViewById(R.id.recyclerQna);
        recyclerQna.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        List<ExpandableListAdapter.Item> answer = new ArrayList<>();

        ExpandableListAdapter.Item problem1 = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, getResources().getString(R.string.question1));
        problem1.invisibleChildren = new ArrayList<>();
        problem1.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, getString(R.string.answer1)));

        ExpandableListAdapter.Item problem2 = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, getResources().getString(R.string.question2));
        problem2.invisibleChildren = new ArrayList<>();
        problem2.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, getString(R.string.answer2)));

        ExpandableListAdapter.Item problem3 = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, getResources().getString(R.string.question3));
        problem3.invisibleChildren = new ArrayList<>();
        problem3.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, getString(R.string.answer3)));


        answer.add(problem1);
        answer.add(problem2);
        answer.add(problem3);
        recyclerQna.setAdapter(new ExpandableListAdapter(answer));



        //18.08.29 이메일 사전준비
        eAdress = (TextView)findViewById(R.id.eAdress);
        esubject = (EditText)findViewById(R.id.eSubject);
        eContents = (EditText)findViewById(R.id.eCon);
        windowEmail = (FrameLayout)findViewById(R.id.windowEmail);



        //18.08.29 상세설정
        bluetoothSwitch = (Switch)findViewById(R.id.bluetooth_switch);
        gpsSwitch = (Switch)findViewById(R.id.gps_switch);

        //현재 블루투스랑 gps가 On 인지 확인해서 스위치 온오프 상태 설정하기
        bluetoothSwitch.setChecked(false);
        gpsSwitch.setChecked(false);
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    //체크On 상태
                    Toast.makeText(getApplicationContext(),"블루투스를 켰습니다.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"블루투스를 껐습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    //체크On 상태
                    Toast.makeText(getApplicationContext(),"gps를 켰습니다.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"gps를 껐습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //18.08.29 설정창 -> 화면간의 이동
        windowNotice = (FrameLayout)findViewById(R.id.windowNotice);
        windowQna = (FrameLayout)findViewById(R.id.windowQna);
        windowStory = (FrameLayout)findViewById(R.id.windowStory);

        //MAP
        previous_marker = new ArrayList<Marker>();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //18.08.29 리스트뷰 관련 추가
        final View header = getLayoutInflater().inflate(R.layout.listitem_header, null, false); //헤더관련
        drawerLayout=(DrawerLayout)findViewById(R.id.drawerlayout); // 최상위 레이아웃 이름
        listView=(ListView)findViewById(R.id.drawer); // 하위 리스트뷰 레이아웃 이름
        listView.addHeaderView(header); //헤더 추가
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.listitem,drawer_str);
        listView.setAdapter(adapter);

        drawerToggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //속도계
        final SpeedView spdview = findViewById(R.id.speedView); //18.08.06

        //현재속도 최고속도 추가
        txtCur = (TextView) findViewById(R.id.tv_current);
        txtMax = (TextView) findViewById(R.id.tv_maximum);

        //2018-08-14
        tv_rideDis = (TextView)findViewById(R.id.tv_rideDis);
        tv_rideTime=(TextView)findViewById(R.id.tv_rideTime);
        btn_timer_start = (Button)findViewById(R.id.btn_timer_start);
        btn_timer_finish = (Button)findViewById(R.id.btn_timer_finish);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                gps = new GpsInfo(MainActivity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {
                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
                if (location != null) {
                    mySpeed = ((location.getSpeed() * 3600) / 1000);
                    if (mySpeed > maxSpeed) {
                        maxSpeed = mySpeed;
                    }
                } // 속도 계산
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override
            public void onProviderEnabled(String provider) { }
            @Override
            public void onProviderDisabled(String provider) { }
        };
        //18.08.16 스피드미터 <-> 맵 화면전환
        mapToSpd = (FrameLayout)findViewById(R.id.mapToSpd);
        spdToMap = (FrameLayout)findViewById(R.id.spdToMap);


        //18.08.29 변경 스피드미터 <-> 맵 화면전환 애니메이션
        translateLeftIn = AnimationUtils.loadAnimation(this,R.anim.translate_left_in);
        translateRightIn = AnimationUtils.loadAnimation(this,R.anim.translate_right_in);
        translateLeftOut = AnimationUtils.loadAnimation(this,R.anim.translate_left_out);
        translateRightOut =  AnimationUtils.loadAnimation(this,R.anim.translate_right_out);



        //템플릿의  메뉴 화면전환
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);

        window1 = (FrameLayout) findViewById(R.id.window1);
        window2 = (FrameLayout) findViewById(R.id.window2);
        window3 = (FrameLayout) findViewById(R.id.window3);



        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.btnWindow1:
                        titleName.setTitle("OBELOCK"); //18.08.28
                        window1.setVisibility(View.VISIBLE);
                        window2.setVisibility(View.GONE);
                        window3.setVisibility(View.GONE);
                        windowEmail.setVisibility(View.GONE);//18.08.29
//                        windowDetailSetting.setVisibility(View.GONE);//18.08.29
                        windowQna.setVisibility(View.GONE);//18.08.29
                        windowNotice.setVisibility(View.GONE);//18.08.29
                        windowStory.setVisibility(View.GONE);//18.08.29
                        break;

                    case R.id.btnWindow2:
                        titleName.setTitle("주행모드(속도계)");//18.08.28
                        window1.setVisibility(View.GONE);
                        window2.setVisibility(View.VISIBLE);
                        window3.setVisibility(View.GONE);
                        windowEmail.setVisibility(View.GONE);//18.08.29
//                        windowDetailSetting.setVisibility(View.GONE);//18.08.29
                        windowQna.setVisibility(View.GONE);//18.08.29
                        windowNotice.setVisibility(View.GONE);//18.08.29
                        windowStory.setVisibility(View.GONE);//18.08.29
                        break;

                    case R.id.btnWindow3:
                        titleName.setTitle("설정");//18.08.28
                        window1.setVisibility(View.GONE);
                        window2.setVisibility(View.GONE);
                        window3.setVisibility(View.VISIBLE);
                        windowEmail.setVisibility(View.GONE);//18.08.29
//                        windowDetailSetting.setVisibility(View.GONE); //18.08.29
                        windowQna.setVisibility(View.GONE);//18.08.29
                        windowNotice.setVisibility(View.GONE);//18.08.29
                        windowStory.setVisibility(View.GONE);//18.08.29
                        break;
                }
                return true;
            }
        });

        //18.08.14
        btn_timer_start.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onClick(View view) {

                if (view.getId() == R.id.btn_timer_start) {

                    if (isBtnClickStart == true) { // 시작 버튼이 눌렸는데 유저가 다시 한번 누른 경우
                        isBtnClickStart = false;
                        time_handler.removeMessages(0);
                        btn_timer_start.setText("주행시작");
                        btn_timer_start.setBackgroundColor(Color.rgb(52,152,255));
                        btn_timer_start.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.starticon,0,0,0);//18.08.26
                        Toast.makeText(getApplicationContext(), "주행을 정지합니다.", Toast.LENGTH_SHORT).show();

                        return;
                    } //일시정지는
                    Toast.makeText(getApplicationContext(), "주행을 시작합니다.", Toast.LENGTH_SHORT).show();

                    btn_timer_start.setText("일시정지");
                    btn_timer_start.setBackgroundColor(Color.rgb(241,196,15));
                    btn_timer_start.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.pauseicon,0,0,0);//18.08.26

                    // Flag 설정
                    isReset = false;
                    isBtnClickStart = true;

                    // GPS 설정 18.09.07
                    GpsInfo gps = new GpsInfo(getApplicationContext());
                    if (gps.isGetLocation()) {
                        /* 첫 시작 지점*/
                        Log.d("GPS사용", "찍힘" + timer);
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);

                        /* 이전의 GPS 정보 저장*/
                        bef_lat = latitude;
                        bef_long = longitude;

                    }

                    /* 타이머를 위한 Handler */
                    time_handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            time_handler.sendEmptyMessageDelayed(0, 1000); // 1초 간격으로
                            timer++; // Timer 증가
                            /* Text View 갱신*/
                            tv_rideTime.setText(String.format("%02d",(timer/3600))+" : "  + String.format("%02d",(timer%3600)/60) +" : "+String.format("%02d",(timer%3600)%60));


                            /* 3초 마다 GPS를 찍기 위한 소스*/
                            if (timer % 3 == 0) {
                                GpsInfo gps = new GpsInfo(getApplicationContext());
                                // GPS 사용유무 가져오기
                                if (gps.isGetLocation()) {
                                    Log.d("GPS사용", "찍힘 : " + timer);
                                    double latitude = gps.getLatitude(); // 위도
                                    double longitude = gps.getLongitude(); // 경도

                                    /* 현재의 GPS 정보 저장*/
                                    cur_lat = latitude;
                                    cur_long = longitude;

                                    /* 이전의 GPS 정보와 현재의 GPS 정보로 거리를 구한다.*/
                                    Distance calcDistance = new Distance(bef_lat,bef_long,cur_lat,cur_long); // 거리계산하는 클래스 호출
                                    double dist = calcDistance.getDistance();
                                    dist = Math.round(dist*100d) / 100d; // 소숫점 2째자리
                                    sum_dist += dist;

                                    /* 이전의 GPS 정보를 현재의 GPS 정보로 변환한다. */
                                    bef_lat = cur_lat;
                                    bef_long = cur_long;


                                    //위의 로케이션리스너에서 값을 수시로 바꾸고 있을때 스타트 버튼을 눌렀을 때에만 밑에 표기 하기 위함
                                    //test중
                                    txtCur.setText(String.format("%.2f",mySpeed) + " Km/h");
                                    txtMax.setText(String.format("%.2f",maxSpeed) + " Km/h");
                                    tv_rideDis.setText(String.format("%.2f",sum_dist) + " Km");

                                    //18.09.07
                                    spdview.speedTo((float)mySpeed,2100); //18.09.07 스피드 띄운후 2.1초동안 지속
                                }
                            }
                        }
                    };
                    time_handler.sendEmptyMessage(0);
                }
            }
        });//주행시작

        btn_timer_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "주행을 종료합니다.", Toast.LENGTH_SHORT).show();

                /* Timer Handler 제거 */
                time_handler.removeMessages(0);

                /* Checking 변수 */
                isBtnClickStart = false;
                isReset = true;

                /*값 초기화*/
                sum_dist=0;
                mySpeed = 0;
                maxSpeed = 0;
                timer = 0;

                tv_rideTime.setText("00 : 00 : 00");
                tv_rideDis.setText("0.00 Km");
                txtCur.setText("0.00 Km/h");
                txtMax.setText("0.00 Km/h");
                spdview.speedTo(0,2100);

                btn_timer_start.setText("주행시작");
                btn_timer_start.setBackgroundColor(Color.rgb(52,152,255));
                btn_timer_start.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.starticon,0,0,0); //18.08.26

            }
        }); // 주행종료

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        //GPS PROVIDER 일때 최소 2.1초마다 혹은 0미터 변동 되었을때 마다 리스너를 호출 한다.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2100, 0, locationListener);

        callPermission();  // 권한 요청을 해야 함
        //자물쇠 디바이스 정보 전달받기
        lockManager = (ArrayList<Parcelable>)intent.getParcelableArrayListExtra("lock_manager");
        
    }
    @Override
    protected void onResume() {
        super.onResume();
        connectBluetooth();
        if (googleApiClient.isConnected()) {

            Log.d(TAG, "onResume : call startLocationUpdates");
            if (!requestingLocationUpdates) startLocationUpdates();
        }
        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;

                checkPermissions();
            }
        }
    }
    private void connectBluetooth(){
        Log.i(TAG, "connectBluetooth");
        unlock.setVisibility(View.GONE);
        lock.setVisibility(View.GONE);
        refresh.setVisibility(View.GONE);
        connecting.setVisibility(View.VISIBLE);

        if(btService == null)
        {
            btService = new BluetoothService(this, handler);
        }
        if(!btService.getDeviceState()){
            Toast.makeText(getApplicationContext(), "블루투스지원안함!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(lockManager.size() == 0)
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
            //curLock = (Lock)lockManager.get(0);

            if (btService.getBtAdapter().isEnabled()) {
                // 블루투스 기기의 사용가능여부가 true 일때
                Log.d(TAG, "Bluetooth Enable Now");

                BluetoothDevice device;
                //String address = ((Lock)lockManager.get(0)).getMacAddr();//첫번째 자물쇠 디바이스의 맥 어드레스 가져오기
                device = btService.getDeviceInfo(TESTMACADDR);
                btService.connect(device);

            } else {
                Log.d(TAG, "Bluetooth Enable Request");
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(i, BluetoothConstants.REQUEST_ENABLE_BT);
            }
        }
    }
    void do_refresh(View v){
        connectBluetooth();
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
    public void onMapIconClick(View v) {
        switch (v.getId()){

            case R.id.spdButton:

                spdToMap.startAnimation(translateRightIn);
                spdToMap.setVisibility(View.VISIBLE);
                mapToSpd.setVisibility(View.GONE);
                break;

            case R.id.mapButton:
                mapToSpd.startAnimation(translateLeftIn);
                spdToMap.startAnimation(translateLeftOut);
                mapToSpd.setVisibility(View.VISIBLE);
                spdToMap.setVisibility(View.GONE);
                titleName.setTitle("주행모드(지도)");//18.08.28
                break;

            //18.08.29
            case R.id.txt3btn1:
                titleName.setTitle("공지사항");
                window3.startAnimation(translateLeftOut);
                windowNotice.startAnimation(translateLeftIn);
                window3.setVisibility(View.GONE);
                windowNotice.setVisibility(View.VISIBLE);
                break;

            //18.08.29
            case R.id.txt3btn2:
                titleName.setTitle("QnA");
                window3.startAnimation(translateLeftOut);
                windowQna.startAnimation(translateLeftIn);
                window3.setVisibility(View.GONE);
                windowQna.setVisibility(View.VISIBLE);
                break;

            //18.08.29
            case R.id.txt3btn3:
                titleName.setTitle("이메일 문의");
                window3.startAnimation(translateLeftOut);
                windowEmail.startAnimation(translateLeftIn);
                window3.setVisibility(View.GONE);
                windowEmail.setVisibility(View.VISIBLE);
                esubject.setText("");
                eContents.setText("");
                break;

            //18.08.29
            case R.id.txt3btn4:
                titleName.setTitle("개발스토리");
                window3.startAnimation(translateLeftOut);
                windowStory.startAnimation(translateLeftIn);
                window3.setVisibility(View.GONE);
                windowStory.setVisibility(View.VISIBLE);
                break;


            //18.08.29
            case R.id.backNotice:
                window3.startAnimation(translateRightIn);
                windowNotice.startAnimation(translateRightOut);
                windowNotice.setVisibility(View.GONE);
                window3.setVisibility(View.VISIBLE);
                titleName.setTitle("설정");
                break;

            //18.08.29
            case R.id.backQna:
                titleName.setTitle("설정");
                window3.startAnimation(translateRightIn);
                windowQna.startAnimation(translateRightOut);
                windowQna.setVisibility(View.GONE);
                window3.setVisibility(View.VISIBLE);
                break;

            //18.08.29
            case R.id.backEmail:
                titleName.setTitle("설정");
                window3.startAnimation(translateRightIn);
                windowEmail.startAnimation(translateRightOut);
                windowEmail.setVisibility(View.GONE);
                window3.setVisibility(View.VISIBLE);
                break;

            //18.08.29
            case R.id.backStory:
                titleName.setTitle("설정");
                window3.startAnimation(translateRightIn);
                windowStory.startAnimation(translateRightOut);
                windowStory.setVisibility(View.GONE);
                window3.setVisibility(View.VISIBLE);
                break;


            case R.id.mapRestaurant:
                showRestaurantInformation(currentPosition);
                break;

            case R.id.mapConvenience:
                showConvenienceInformation(currentPosition);
                break;

            case R.id.mapBike:
                showBikeMarketInformation(currentPosition);
                break;

            //18.08.29
            case R.id.sendEmail:
                try {
                    GMail gMailSender = new GMail("obelock12@gmail.com", "team-chaser1");
                    gMailSender.sendMail(esubject.getText().toString(), eContents.getText().toString(), eAdress.getText().toString());
                    Toast.makeText(getApplicationContext(), "이메일을 성공적으로 보냈습니다.", Toast.LENGTH_SHORT).show();

                    window3.startAnimation(translateRightIn);
                    windowEmail.startAnimation(translateRightOut);
                    windowEmail.setVisibility(View.GONE);
                    window3.setVisibility(View.VISIBLE);
                    titleName.setTitle("설정");

                } catch (SendFailedException e) {
                    Toast.makeText(getApplicationContext(), "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show();

                } catch (MessagingException e) {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주십시오", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                    e.printStackTrace();
                }
                break;

        }
    }
    // 권한 요청
    private void callPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }

    }
    private void startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }
            Log.d(TAG, "startLocationUpdates : call FusedLocationApi.requestLocationUpdates");
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            requestingLocationUpdates = true;

            googleMap.setMyLocationEnabled(true);

        }

    }
    private void stopLocationUpdates() {
        Log.d(TAG,"stopLocationUpdates : LocationServices.FusedLocationApi.removeLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        requestingLocationUpdates = false;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");
        this.googleMap = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);//18.08.26
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        this.googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        this.googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){

            @Override
            public boolean onMyLocationButtonClick() {
                Log.d( TAG, "onMyLocationButtonClick : 위치에 따른 카메라 이동 활성화");
                moveMapByAPI = true;
                return true;
            }
        });
        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                Log.d( TAG, "onMapClick :");
            }
        });

        this.googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {

            @Override
            public void onCameraMoveStarted(int i) {

                if (moveMapByUser == true && requestingLocationUpdates){
                    Log.d(TAG, "onCameraMove : 위치에 따른 카메라 이동 비활성화");
                    moveMapByAPI = false;
                }
                moveMapByUser = true;
            }
        });


        this.googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {

            @Override
            public void onCameraMove() {
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

        currentPosition
                = new LatLng( location.getLatitude(), location.getLongitude());


        Log.d(TAG, "onLocationChanged : ");

        String markerTitle = getCurrentAddress(currentPosition);
        String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                + " 경도:" + String.valueOf(location.getLongitude());

        //현재 위치에 마커 생성하고 이동
        setCurrentLocation(location, markerTitle, markerSnippet);

        currentLocatiion = location;
    }

    @Override
    protected void onStart() {
        if(googleApiClient != null && googleApiClient.isConnected() == false){
            Log.d(TAG, "onStart: mGoogleApiClient connect");
            googleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (requestingLocationUpdates) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            stopLocationUpdates();
        }

        if ( googleApiClient.isConnected()) {
            Log.d(TAG, "onStop : mGoogleApiClient disconnect");
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if ( requestingLocationUpdates == false ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                } else {
                    Log.d(TAG, "onConnected : 퍼미션 가지고 있음");
                    Log.d(TAG, "onConnected : call startLocationUpdates");
                    startLocationUpdates();
                    googleMap.setMyLocationEnabled(true);
                }
            }else{
                Log.d(TAG, "onConnected : call startLocationUpdates");
                startLocationUpdates();
                googleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        setDefaultLocation();
    }

    @Override
    public void onConnectionSuspended(int cause) {

        Log.d(TAG, "onConnectionSuspended");
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    /*현재 위치 아이콘 수정 할 수 있는 부분 */
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        moveMapByUser = false;

//      18.08.26 주석처리
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if ( moveMapByAPI ) {
            Log.d( TAG, "setCurrentLocation :  mGoogleMap moveCamera "
                    + location.getLatitude() + " " + location.getLongitude() ) ;
            // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            googleMap.moveCamera(cameraUpdate);
        }
    }


    public void setDefaultLocation() {

        moveMapByUser = false;
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.496375, 126.956879);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) {
            currentMarker.remove();

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(DEFAULT_LOCATION);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            currentMarker = googleMap.addMarker(markerOptions);
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        googleMap.moveCamera(cameraUpdate);

    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat
                .shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager
                .PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

        else if (hasFineLocationPermission
                == PackageManager.PERMISSION_DENIED && !fineLocationRationale) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {


            Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");

            if ( googleApiClient.isConnected() == false) {

                Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");
                googleApiClient.connect();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {


        if (isAccessFineLocation && isAccessCoarseLocation) {

            isPermission = true;

        }

        if (permsRequestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {
            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionAccepted) {
                if ( googleApiClient.isConnected() == false) {
                    Log.d(TAG, "onRequestPermissionsResult : mGoogleApiClient connect");
                    googleApiClient.connect();
                }
            } else {
                checkPermissions();
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                askPermissionOnceAgain = true;

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + MainActivity.this.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "GPS 설정을 확인 하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<Place> places) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                for (noman.googleplaces.Place place : places) {
                    LatLng latLng  = new LatLng(place.getLatitude() , place.getLongitude());

                    Log.d("color::",place.toString());

                    String markerSnippet = getCurrentAddress(latLng);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);

                    if(place.getTypes()[0].equals("restaurant") || place.getTypes()[1].equals("restaurant")){
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));// 아이콘 색
                    } else if (place.getTypes()[0].equals("convenience_store")){
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    }else if(place.getTypes()[0].equals("bicycle_store")){
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    }

                    Marker item = googleMap.addMarker(markerOptions);
                    previous_marker.add(item);
                }


                //중복 마커 제거

                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);


            }

        });


    }

    @Override
    public void onPlacesFinished() {

    }

    //음식점 위치 검색!
    public void showRestaurantInformation(LatLng location) {
        googleMap.clear();//지도 클리어

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(MainActivity.this)
                .key("AIzaSyB17qhzUpLlMGgrBm6E1RqM5FNURvZ-rRA")
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.RESTAURANT) //음식점
                .language("ko", "KR")
                .build()
                .execute();

    }

    // 편의점 검색
    public void showConvenienceInformation(LatLng location)    {
        googleMap.clear();//지도 클리어

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(MainActivity.this)
                .key("AIzaSyB17qhzUpLlMGgrBm6E1RqM5FNURvZ-rRA")
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.CONVENIENCE_STORE) //편의점
                .language("ko","KR")
                .build()
                .execute();
    }

    // 주변 자전거 상점
    public void showBikeMarketInformation(LatLng location)    {
        googleMap.clear();//지도 클리어

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(MainActivity.this)
                .key("AIzaSyB17qhzUpLlMGgrBm6E1RqM5FNURvZ-rRA")
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.BICYCLE_STORE) //자전거 상점
                .language("ko","KR")
                .build()
                .execute();
    }


    // 애니메이션 리스너를 상속받은뒤, 메소드 오버라이딩
    class  SlidingAnimaionListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {


        }
        @Override
        public void onAnimationEnd(Animation animation) {

        }
        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    //리스트뷰를 위한 오버라이딩
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
    private PendingIntent createPendingIntent(){
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private NotificationCompat.Builder createNotification(String title, String content){
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        String channelId = "channel";
        String channelName = "Channel Name";

        NotificationManager notifManager

                = (NotificationManager) getSystemService  (Context.NOTIFICATION_SERVICE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int requestID = (int) System.currentTimeMillis();

        PendingIntent pendingIntent
                = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent

                , PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle(title) // required
                .setContentText(content)  // required
                .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(android.R.drawable.btn_star)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.obelock_launcher72))
                .setBadgeIconType(R.drawable.obelock_launcher72)
                .setContentIntent(pendingIntent);

        notifManager.notify(0, builder.build());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return builder;
    }

}