package com.example.admin.myapplication.vo;

import android.os.Parcel;
import android.os.Parcelable;


public class Lock implements Parcelable{

    private int battery;
    private int state;
    private GpsInfo gspInfo;
    private String name;
    private String macAddr;
    private int order;

    public Lock(){

    }
    protected Lock(Parcel in) {
        battery = in.readInt();
        state = in.readInt();
        name = in.readString();
        macAddr = in.readString();
        order = in.readInt();
    }

    public static final Creator<Lock> CREATOR = new Creator<Lock>() {
        @Override
        public Lock createFromParcel(Parcel in) {
            return new Lock(in);
        }

        @Override
        public Lock[] newArray(int size) {
            return new Lock[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(battery);
        parcel.writeInt(state);
        parcel.writeString(name);
        parcel.writeString(macAddr);
        parcel.writeInt(order);
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
    public GpsInfo getGspInfo() {
        return gspInfo;
    }

    public void setGspInfo(GpsInfo gspInfo) {
        this.gspInfo = gspInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
