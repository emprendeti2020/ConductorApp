package com.creativeapps.schoolbusdriver.data.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ChildResponse {

    @Expose
    @SerializedName("child")
    private Child child;

    public Child getChild() {

        return child;
    }

}
