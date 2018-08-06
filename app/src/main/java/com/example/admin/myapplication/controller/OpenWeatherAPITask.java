package com.example.admin.myapplication.controller;

import android.os.AsyncTask;

import com.example.admin.myapplication.controller.OpenWeatherAPIClient;
import com.example.admin.myapplication.vo.Weather;

public class OpenWeatherAPITask extends AsyncTask<Integer, Void, Weather> {

    @Override
    public Weather doInBackground(Integer... params) {
        OpenWeatherAPIClient client = new OpenWeatherAPIClient();

        int lat = params[0];
        int lon = params[1];
        // API 호출
        Weather w = client.getWeather(lat,lon);
        //System.out.println("Weather : "+w.getTemprature());
        // 작업 후 리
        return w;
    }
}
/*출처: http://bcho.tistory.com/search/날씨 [조대협의 블로그]*/
