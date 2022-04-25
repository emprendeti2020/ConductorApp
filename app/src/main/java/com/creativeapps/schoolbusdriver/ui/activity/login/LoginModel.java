package com.creativeapps.schoolbusdriver.ui.activity.login;

import android.util.Log;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.data.DataManager;
import com.creativeapps.schoolbusdriver.data.network.models.Driver;
import com.creativeapps.schoolbusdriver.data.network.models.DriverResponse;
import com.creativeapps.schoolbusdriver.data.network.services.DriverApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginModel extends ViewModel {

    final String TAG = "LoginModel";

    //region Live data variables for EnterPhoneNumberFragment

    //boolean variable to indicate if the requested verification code received by the app or not
    private MutableLiveData<Boolean> mIsVerificationCodeReceived;
    //boolean variable to indicate if the process (request authentication code of the drive with
    // his telephone number) is running
    private MutableLiveData<Boolean> mIsWaitRespEnterMobile;
    //string variable that contains the response of the process (request authentication
    // code of the drive with his telephone number) from the server
    private MutableLiveData<String> mRespEnterMobile;
    //endregion

    //region Live data variables for ActivationCodeFragment

    //boolean variable to indicate if the process (verify authentication code of the driver)
    // is running
    private MutableLiveData<Boolean> mIsWaitRespVerifyDriver;
    //string variable that contains the response of the process (verify authentication code of
    // the drive) from the server
    private MutableLiveData<String> mRespVerifyDriver;
    //driver data
    private MutableLiveData<Driver> mDriver;
    //endregion

    //country Code
    private MutableLiveData<String> mCountryCode;

    //verification Id
    private MutableLiveData<String> mVerificationId;

    //mobile number
    private MutableLiveData<String> mMobileNumber;
    //driver Api service instance
    private DriverApiService mDriverApiService;


    //region Getters and setters for the private variables defined above
    public MutableLiveData<Boolean> getIsVerificationCodeReceived() {
        return mIsVerificationCodeReceived;
    }

    public void setIsVerificationCodeReceived(Boolean isVerificationCodeReceived) {
        this.mIsVerificationCodeReceived.postValue(isVerificationCodeReceived);
    }

    public MutableLiveData<Boolean> getIsWaitRespEnterMobile() {
        return mIsWaitRespEnterMobile;
    }

    public void setIsWaitRespEnterMobile(Boolean isWaitRespEnterMobile) {
        this.mIsWaitRespEnterMobile.postValue(isWaitRespEnterMobile);
    }

    public MutableLiveData<String> getRespEnterMobile() {
        return mRespEnterMobile;
    }

    public void setRespEnterMobile(String respEnterMobile) {
        this.mRespEnterMobile.postValue(respEnterMobile);
    }

    public MutableLiveData<String> getCountryCode() {
        return mCountryCode;
    }

    public void setCountryCode(String countryCode) {
        this.mCountryCode.setValue(countryCode);
    }

    public MutableLiveData<String> getMobileNumber() {
        return mMobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mMobileNumber.setValue(mobileNumber);
    }

    public MutableLiveData<Driver> getDriver() {
        return mDriver;
    }

    public void setDriver(Driver driver) {
        this.mDriver.postValue(driver);
    }

    public MutableLiveData<Boolean> getIsWaitRespVerifyDriver() {
        return mIsWaitRespVerifyDriver;
    }

    public void setIsWaitRespVerifyDriver(Boolean isWaitRespVerifyDriver) {
        this.mIsWaitRespVerifyDriver.postValue(isWaitRespVerifyDriver);
    }

    public MutableLiveData<String> getRespVerifyDriver() {
        return mRespVerifyDriver;
    }

    public void setRespVerifyDriver(String respVerifyDriver) {
        this.mRespVerifyDriver.postValue(respVerifyDriver);
    }

    public MutableLiveData<String> getVerificationId() {
        return mVerificationId;
    }

    public void setVerificationId(String mVerificationId) {
        this.mVerificationId.postValue(mVerificationId);
    }
    //endregion

    //region Constructor
    public LoginModel()
    {
        mIsVerificationCodeReceived = new MutableLiveData<>();
        mIsWaitRespEnterMobile = new MutableLiveData<>();
        mRespEnterMobile = new MutableLiveData<>();
        mIsWaitRespVerifyDriver = new MutableLiveData<>();
        mRespVerifyDriver = new MutableLiveData<>();
        mCountryCode = new MutableLiveData<>();
        mMobileNumber = new MutableLiveData<>();
        mDriver = new MutableLiveData<>();
        mVerificationId = new MutableLiveData<>();
        mDriverApiService = DataManager.getInstance().getDriverApiService();
    }
    //endregion

    /*send verification code of the driver to the server for authentication*/
    public void sendVerificationCode(final String countryCode, final String mobileNumber, final String VerificationCode)
    {
        //define a background thread
        Thread background = new Thread() {
            public void run() {
                //set RespVerifyDriver to empty
                setRespVerifyDriver("");
                //set IsWaitRespVerifyDriver to true
                setIsWaitRespVerifyDriver(true);
                try {
                    //call the verifyDriver function to communicate with the server
                    verifyDriver(countryCode, mobileNumber, VerificationCode);
                } catch (Exception e) {
                    //if error, set IsWaitRespVerifyDriver to false
                    setIsWaitRespVerifyDriver(false);
                    Log.d(TAG, "run: " + e.getMessage());
                }
            }
        };
        //start the thread
        background.start();
    }

    /*request verification code of the driver from the server*/
    public void requestVerificationCode(final PhoneAuthOptions options,
                                        final String countryCode, final String mobileNumber)
    {
        //set the country code and mobile number
        setCountryCode(countryCode);
        setMobileNumber(mobileNumber);
        //set RespEnterMobile to empty
        setRespEnterMobile("");
        //set IsWaitRespEnterMobile to true
        setIsWaitRespEnterMobile(true);

        //define a background thread
        Thread background = new Thread() {
            public void run() {
                try {
                    //call the authenticateDriver function to communicate with the server
                    //authenticateDriver(countryCode, mobileNumber);
                    PhoneAuthProvider.verifyPhoneNumber(options);
                } catch (Exception e) {
                    //if error, set IsWaitRespEnterMobile to false
                    setIsWaitRespEnterMobile(false);
                    Log.d(TAG, "run: " + e.getMessage());
                }
            }
        };
        background.start();
    }

    /*call the Api function verifyDriverTelNumber and define a callback for this Api*/
    public void verifyDriver(String countryCode, String mobileNumber, String VerificationCode) {
        Call<DriverResponse> driverApiCall = mDriverApiService.getDriverApi().verifyDriverTelNumber(countryCode, mobileNumber, VerificationCode);
        //define the callback verifyDriverCallback to set the appropriate live data when
        // the Api function returns
        driverApiCall.enqueue(new verifyDriverCallback());
    }

    /*Callback for verifyDriverTelNumber function*/
    private class verifyDriverCallback implements Callback<DriverResponse> {

        @Override
        public void onResponse( Call<DriverResponse> call, Response<DriverResponse> response) {
            //get the return code of the response
            int RetCode = response.code();
            //set IsWaitRespEnterMobile to false
            setIsWaitRespVerifyDriver(false);
            switch (RetCode)
            {
                //return code is OK
                case 200:
                    //set the status RespVerifyDriver to empty
                    setRespVerifyDriver("");
                    //extract the driver data from response and save it in the driver live data
                    DriverResponse r = response.body();
                    if(r!=null) {
                        setDriver(r.getDriver());
                    }
                    break;
                //if the return code has error, set the status RespVerifyDriver to an appropriate message
                case 404: //resource not found
                    setRespVerifyDriver(LoginActivity.getContext().getString(R.string.tel_number_not_exist));
                    setRespEnterMobile(LoginActivity.getContext().getString(R.string.tel_number_not_exist));
                    Log.d(TAG, response.message());
                    break;
                case 422: //validation error
                    setRespVerifyDriver(LoginActivity.getContext().getString(R.string.error_in_verification_code));
                    setRespEnterMobile(LoginActivity.getContext().getString(R.string.error_in_verification_code));
                    Log.d(TAG, response.message());
                    break;
                case 500: //general server error
                default:
                    setRespVerifyDriver(LoginActivity.getContext().getString(R.string.unexpected_error));
                    setRespEnterMobile(LoginActivity.getContext().getString(R.string.unexpected_error));
                    Log.d(TAG, response.message());
                    break;
            }
        }

        @Override
        public void onFailure(Call<DriverResponse> call, Throwable t) {
            //if failure in communication with the server
            setIsWaitRespVerifyDriver(false);
            setRespVerifyDriver(LoginActivity.getContext().getString(R.string.unable_to_connect_server));
            setRespEnterMobile(LoginActivity.getContext().getString(R.string.unable_to_connect_server));
        }
    }

}
