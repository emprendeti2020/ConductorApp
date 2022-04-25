package com.creativeapps.schoolbusdriver.data.network.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class CheckStatus implements Parcelable{
    @Expose
    @SerializedName("id")
    private Integer id;

    @Expose
    @SerializedName("case_id")
    private Integer case_id;


    @Expose
    @SerializedName("updated_at")
    private String updated_at;

    protected CheckStatus(Parcel in) {
        id = in.readInt();
        case_id = in.readInt();
        updated_at = in.readString();
    }

    public static final Creator<CheckStatus> CREATOR = new Creator<CheckStatus>() {
        @Override
        public CheckStatus createFromParcel(Parcel in) {

            return new CheckStatus(in);
        }

        @Override
        public CheckStatus[] newArray(int size) {
            return new CheckStatus[size];
        }
    };

    public CheckStatus(Integer id, Integer case_id, String updated_at) {
        this.id = id;
        this.case_id = case_id;
        this.updated_at = updated_at;
    }


    public Integer getId() {
        return id;
    }

    public Integer getCase() {
        return case_id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCase(Integer case_id) {
        this.case_id = case_id;
    }

    public String getLastDate() {
        return updated_at;
    }
    public void setLastDate(String updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(case_id);
        parcel.writeString(updated_at);
    }
}
