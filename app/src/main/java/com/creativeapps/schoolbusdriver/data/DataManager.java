package com.creativeapps.schoolbusdriver.data;

import com.creativeapps.schoolbusdriver.data.network.services.DriverApiService;


public class DataManager {

    private static DataManager sInstance;

    /*get the data manager instance*/
    public static synchronized DataManager getInstance() {
        if (sInstance == null) {
            sInstance = new DataManager();
        }
        return sInstance;
    }

    public DriverApiService getDriverApiService() {
        return DriverApiService.getInstance();
    }

}
