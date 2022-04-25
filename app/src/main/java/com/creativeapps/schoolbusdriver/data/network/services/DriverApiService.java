package com.creativeapps.schoolbusdriver.data.network.services;


import com.creativeapps.schoolbusdriver.data.Util;
import com.creativeapps.schoolbusdriver.data.network.models.ChildResponse;
import com.creativeapps.schoolbusdriver.data.network.models.DriverResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;


public class DriverApiService {
    //URL of the backend for APIs
    private static final String URL = Util.WEB_SERVER_URL + "/api/drivers/";
    //Driver API instance
    private DriverApi driverApi;
    //Driver API service instance
    private static DriverApiService instance;

    /*get driver API instance*/
    public DriverApi getDriverApi() {
        return driverApi;
    }

    /*get driver API service instance*/
    public static DriverApiService getInstance() {
        if (instance == null) {
            //if the driver API service is not already instantiated, create new one
            instance = new DriverApiService();
        }
        //return the created instance
        return instance;
    }

    /*constructor for driver API service*/
    private DriverApiService() {
        //create a Retrofit object with json enabled
        Retrofit mRetrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(URL).build();
        //initialize the driver API instance
        driverApi = mRetrofit.create(DriverApi.class);
    }

    /*define the interface with required methods from the API*/
    public interface DriverApi {
        //method that get the details of a driver (including school and bus data) using a telephone number
        @GET("getSchoolBusDriverTelNumber")
        Call<DriverResponse> getSchoolBusDriverTelNumber(@Query("country_code") String countryCode,
                                                         @Query("tel_number") String tel_number,
                                                         @Query("secretKey") String secretKey);

        //method that verify the telephone number of a driver after sign in using a verification code
        @POST("verifyDriverTelNumber")
        Call<DriverResponse> verifyDriverTelNumber(@Query("country_code") String countryCode,
                                                   @Query("tel_number") String tel_number,
                                                   @Query("v_code") String v_code);

        //method that update the position of the driver
        @PUT("updatePosition")
        Call<ResponseBody> updatePosition(@Query("id") Integer id,
                                          @Query("secretKey") String secretKey,
                                          @Query("last_latitude") Double last_latitude,
                                          @Query("last_longitude") Double last_longitude);

        //method that update the position of the driver
        @PUT("updatePositionWithSpeed")
        Call<ResponseBody> updatePositionSpeed(
                                          @Query("secretKey") String secretKey,
                                          @Query("last_latitude") Double last_latitude,
                                          @Query("last_longitude") Double last_longitude,
                                          @Query("speed") Double speed);

        //method that check in/out child in bus
        @POST("checkInOut")
        Call<ChildResponse> checkInOutChild(
                                            @Query("secretKey") String secretKey,
                                            @Query("child_id") Integer child_id,
                                            @Query("case_id") Integer case_id,
                                            @Query("checked_in_string") String checked_in_string,
                                            @Query("checked_out_string") String checked_out_string);
    }
}


