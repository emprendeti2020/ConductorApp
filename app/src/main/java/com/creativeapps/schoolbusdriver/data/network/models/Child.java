package com.creativeapps.schoolbusdriver.data.network.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Child implements Parcelable {

    @Expose
    @SerializedName("id")
    private Integer id;

    @Expose
    @SerializedName("childName")
    private String childName;

    @Expose
    @SerializedName("child_absent_till")
    private String childAbsentTill;


    @Expose
    @SerializedName("latest_check_status")
    public CheckStatus lastCheckStatus;


    protected Child(Parcel in) {
        id = in.readInt();
        childName = in.readString();
        childAbsentTill = in.readString();
        lastCheckStatus = in.readParcelable(CheckStatus.class.getClassLoader());
    }

    public static final Creator<Child> CREATOR = new Creator<Child>() {
        @Override
        public Child createFromParcel(Parcel in) {

            return new Child(in);
        }

        @Override
        public Child[] newArray(int size) {
            return new Child[size];
        }
    };

    public Child(Integer id, String childName, String childAbsentTill) {
        this.id = id;
        this.childName = childName;
        this.childAbsentTill = childAbsentTill;

    }


    public Integer getId() {
        return id;
    }

    public String getchildName() {
        return childName;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setchildName(String childName) {
        this.childName = childName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(childName);
    }

    public String getChild_AbsentTill() {
        return childAbsentTill;
    }

    public void setChild_AbsentTill(String childAbsentTill) {
        this.childAbsentTill = childAbsentTill;
    }
}
