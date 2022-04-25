package com.creativeapps.schoolbusdriver.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class DriverResponse {

    @Expose
    @SerializedName("driver")
    private Driver driver;

    public Driver getDriver() {

        return driver;
    }

}
