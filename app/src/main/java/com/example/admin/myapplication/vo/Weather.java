package com.example.admin.myapplication.vo;

public class Weather {
    private int lat;
    private int ion;
    private double temp;
    private int cloudy;
    private String city;

    public void setLat(int lat) {
        this.lat = lat;
    }

    public void setIon(int ion) {
        this.ion = ion;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public void setCloudy(int cloudy) {
        this.cloudy = cloudy;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getLat() {
        return lat;
    }

    public int getIon() {
        return ion;
    }

    public double getTemp() {
        return temp;
    }

    public int getCloudy() {
        return cloudy;
    }

    public String getCity() {
        return city;
    }
}
