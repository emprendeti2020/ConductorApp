package com.creativeapps.schoolbusdriver.ui.activity.login.activationCode;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.ui.activity.login.LoginActivity;
import com.creativeapps.schoolbusdriver.ui.activity.login.LoginModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OtpView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;


public class ActivationCodeFragment extends Fragment implements View.OnClickListener {

    final String TAG = "ActivationCodeFragment";

    //OtpView used to enter the verification code received by the driver
    private OtpView mOtpView;
    //view model for the activity
    private LoginModel mViewModel;
    //Text view that display the entered phone number
    private TextView mMobNumberTxtView;
    //Text view that display the entered country code
    private TextView mCountryCodeTxtView;
    //overlay that prevent the user from interacting with any gui element on the screen while the
    // spinner is shown
    private Dialog mOverlayDialog;
    //spinner that is used to indicate a long running process such as communicating with the backend
    private ProgressBar mSpinner;
    //Text view that display the status of the verification process after entering the verification code
    private TextView mStatus;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        //inflate the layout
        View view = inflater.inflate(R.layout.fragment_activation_code, container, false);
        //create the view model for this activity
        mViewModel = ((LoginActivity)getActivity()).createViewModel();

        return view;
    }

    private void signInWithPhoneAuthCredential() {

        String code = mOtpView.getText().toString().trim();
        String verificationId = mViewModel.getVerificationId().getValue();
        mViewModel.setIsWaitRespVerifyDriver(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this.getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();

                            user.getIdToken(true).addOnCompleteListener(
                                    new OnCompleteListener<GetTokenResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                                            if (task.isSuccessful()) {
                                                String token = task.getResult().getToken();

                                                mViewModel.sendVerificationCode(
                                                        mCountryCodeTxtView.getText().toString().trim(),
                                                        mMobNumberTxtView.getText().toString().trim(),
                                                        token);
                                            }
                                            else
                                            {
                                                mViewModel.setRespVerifyDriver(LoginActivity.getContext().getString(R.string.unexpected_error));
                                            }
                                            mViewModel.setIsWaitRespVerifyDriver(false);
                                        }
                                    }
                            );
                            // Update UI

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mViewModel.setRespVerifyDriver(LoginActivity.getContext().getString(R.string.verification_code_not_correct));
                            }
                            mViewModel.setIsWaitRespVerifyDriver(false);
                        }
                    }
                });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //initialize all gui elements here after the view is created

        mMobNumberTxtView = view.findViewById(R.id.MobNumber);
        mCountryCodeTxtView = view.findViewById(R.id.CountryCode);

        final Button nextBtn = view.findViewById(R.id.NextBtn);
        nextBtn.setEnabled(false);
        nextBtn.setAlpha(0.5f);
        nextBtn.setOnClickListener(this);
        //resend code button to return back to the enter phone number fragment
        Button resendCodeBtn = view.findViewById(R.id.ResendCodeBtn);
        resendCodeBtn.setOnClickListener(this);

        mOtpView = view.findViewById(R.id.otp_view);
        //text listener for the opt view. This is implemented to detect when the driver enters
        // the 6 digit code sent to him. When detected, the app load the Main activity to show map, ...
        mOtpView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //if the driver enters the 6 digit code sent to him
                if(charSequence.length()== mOtpView.getItemCount())
                {
                    //enable the next button
                    nextBtn.setEnabled(true);
                    nextBtn.setAlpha(1.0f);

                    signInWithPhoneAuthCredential();
                }
                else
                {
                    //else, keep the next button dimmed and disabled
                    nextBtn.setEnabled(false);
                    nextBtn.setAlpha(0.5f);
                }
                Log.d(TAG, "onTextChanged=>" + charSequence.length());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog = new Dialog(this.getContext(), android.R.style.Theme_Panel);

        mSpinner = view.findViewById(R.id.MobNumberProgressBar);
        mSpinner.setVisibility(View.GONE);

        mStatus = view.findViewById(R.id.MobNumberStatus);

        //handle the back button press of the device to close the fragment appropriately
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if( i == KeyEvent.KEYCODE_BACK )
                {
                    //if back button pressed
                    goBack();
                }
                return false;
            }

        } );
    }


    @Override
    public void onResume()
    {
        super.onResume();
        //start observing the live data from the view model, which are

        //boolean IsWaitRespVerifyDriver that indicates if the process (verify authentication
        // code of the driver) is running
        mViewModel.getIsWaitRespVerifyDriver().observe(this, new LoadingObserver());
        //string RespVerifyDriver which is the response of the process (verify authentication
        // code of the drive) from the server
        mViewModel.getRespVerifyDriver().observe(this, new statusObserver());
        //strings CountryCode and MobileNumber
        mViewModel.getCountryCode().observe(this, new CountryCodeObserver());
        mViewModel.getMobileNumber().observe(this, new MobileNumberObserver());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //stop observing live data when the fragment is paused

        mViewModel.getCountryCode().removeObservers(this);
        mViewModel.getMobileNumber().removeObservers(this);
        mViewModel.getIsWaitRespVerifyDriver().removeObservers(this);
        mViewModel.getRespVerifyDriver().removeObservers(this);
    }

    @Override
    public void onClick(View view) {
        //handle clicks on gui elements

        //if the gui element is the Next button
        if(view.getId() == R.id.NextBtn) {
            //call the model view function to verify authentication
            //code of the drive with his telephone number and country code
            signInWithPhoneAuthCredential();
        }
        //if the gui element is the ResendCode button
        else if(view.getId() == R.id.ResendCodeBtn) {
            //go back
            goBack();
        }
    }

    /*close the fragment appropriately and set the live data variables IsVerificationCodeReceived
    to false and RespVerifyDriver to empty string. This is to halt the observers in the
    EnterPhoneNumberFragment associated with these variables*/
    private void goBack() {
        mViewModel.setIsVerificationCodeReceived(false);
        mViewModel.setRespVerifyDriver("");
        mViewModel.setRespEnterMobile("");
        mViewModel.setIsWaitRespEnterMobile(false);
        mViewModel.setIsWaitRespVerifyDriver(false);
        getActivity().onBackPressed();
    }

    /*Observer for the string live data CountryCode. This is used to display the selected
    country code from previous fragment (EnterPhoneNumberFragment) in this one*/
    private class CountryCodeObserver implements Observer<String>
    {
        @Override
        public void onChanged(@Nullable String countryCode) {
            if (countryCode == null) return;
            mCountryCodeTxtView.setText(countryCode);
        }
    }

    /*Observer for the string live data MobileNumber. This is used to display the entered
    mobile number from previous fragment (EnterPhoneNumberFragment) in this one*/
    private class MobileNumberObserver implements Observer<String>
    {
        @Override
        public void onChanged(@Nullable String mobileNumber) {
            if (mobileNumber == null) return;
            mMobNumberTxtView.setText(mobileNumber);
        }
    }

    /*Observer for the boolean live data IsWaitRespVerifyDriver that indicates if the process
    (verify authentication code of the drive with his telephone number) is running*/
    private class LoadingObserver implements Observer<Boolean> {

        @Override
        public void onChanged(@Nullable Boolean isLoading) {
            if (isLoading == null) return;
            //if the process is running
            if (isLoading) {
                //show spinner and overlay
                mOverlayDialog.show();
                mSpinner.setVisibility(View.VISIBLE);
            } else {
                //hide spinner and overlay
                mOverlayDialog.dismiss();
                mSpinner.setVisibility(View.GONE);
            }
        }
    }
    /*Observer for the string live data RespVerifyDriver which is the response of the process
    (verify authentication code of the drive with his telephone number) from the server*/
    private class statusObserver implements Observer<String>
    {
        @Override
        public void onChanged(@Nullable String statusTxt) {
            if (statusTxt == null) return;
            //set the text in the mStatus text view with the response
            mStatus.setText(statusTxt);
        }
    }
}