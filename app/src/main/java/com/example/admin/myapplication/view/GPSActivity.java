package com.example.admin.myapplication.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.vo.GpsInfo;
import com.github.anastr.speedviewlib.AwesomeSpeedometer;

public class GPSActivity extends AppCompatActivity {

    private TextView txtLat, txtLon;

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;

    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    // GPSTracker class
    private GpsInfo gps;

    /// 이하는 속도를 위한 추가
    double mySpeed = 0, maxSpeed = 0;
    private TextView txtCur, txtMax;
    private LocationManager locationManager;
    private LocationListener locationListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final AwesomeSpeedometer asview = findViewById(R.id.awsomeSpeedmeter); //18.08.06


        txtLat = (TextView) findViewById(R.id.tv_latitude);
        txtLon = (TextView) findViewById(R.id.tv_longitude);

        //현재속도 최고속도 추가
        txtCur = (TextView) findViewById(R.id.tv_current);
        txtMax = (TextView) findViewById(R.id.tv_maximum);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                gps = new GpsInfo(GPSActivity.this);

                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    txtLat.setText(String.format("%.6f",latitude));
                    txtLon.setText(String.format("%.6f",longitude));

                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }

                if (location != null) {

                    mySpeed = ((location.getSpeed() * 3600) / 1000);

                    if (mySpeed > maxSpeed) {
                        maxSpeed = mySpeed;
                    }

                    txtCur.setText(String.format("%.2f",mySpeed) + " km/h");
                    txtMax.setText(String.format("%.2f",maxSpeed) + " km/h");

                    asview.speedTo((float)mySpeed);
                } // 속도 계산

            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //GPS PROVIDER일때 최소 0초마다 혹은 0미터 변동 되었을때 마다 리스너를 호출 한다.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


        callPermission();  // 권한 요청을 해야 함




    }

    public void goMap(View view){
        Intent intent = new Intent(this, DriveMapActivity.class);
        startActivity(intent);

        // overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in); //효과주는것


    } // 클릭시 맵으로 전환 18.08.07


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;

        }


        if (isAccessFineLocation && isAccessCoarseLocation) {

            isPermission = true;

        }

    }



    // 권한 요청

    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
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


}
