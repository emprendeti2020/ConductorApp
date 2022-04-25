package com.creativeapps.schoolbusdriver.data.network.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Bus implements Parcelable{
    @Expose
    @SerializedName("id")
    private Integer id;

    @Expose
    @SerializedName("license")
    private String license;


    protected Bus(Parcel in) {
        id = in.readInt();
        license = in.readString();
    }

    public static final Creator<Bus> CREATOR = new Creator<Bus>() {
        @Override
        public Bus createFromParcel(Parcel in) {

            return new Bus(in);
        }

        @Override
        public Bus[] newArray(int size) {
            return new Bus[size];
        }
    };

    public Bus(Integer id, String license) {
        this.id = id;
        this.license = license;

    }


    public Integer getId() {
        return id;
    }

    public String getLicense() {
        return license;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLicense(String license) {
        this.license = license;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(license);
    }
}
