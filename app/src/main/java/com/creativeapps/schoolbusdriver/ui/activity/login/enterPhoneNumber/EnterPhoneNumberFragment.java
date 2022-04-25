package com.creativeapps.schoolbusdriver.ui.activity.login.enterPhoneNumber;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.creativeapps.schoolbusdriver.R;
import com.creativeapps.schoolbusdriver.ui.activity.login.LoginActivity;
import com.creativeapps.schoolbusdriver.ui.activity.login.LoginModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.concurrent.TimeUnit;

public class EnterPhoneNumberFragment extends Fragment implements View.OnClickListener {

    final String TAG = "EnterPhoneNumberFrag";

    //view model for the activity
    private LoginModel mViewModel;
    //Edit text used to enter the phone number of a driver
    private EditText mPhoneNumberEdt;
    //spinner that is used to indicate a long running process such as communicating with the backend
    private ProgressBar mSpinner;
    //overlay that prevent the user from interacting with any gui element on the screen while the
    // spinner is shown
    private Dialog mOverlayDialog;
    //Text view that display the status of the sign in process after entering the phone number
    private TextView mStatus;
    //picker for country code
    private CountryCodePicker mCountryCodePicker;
    //navigation controller used to navigate between fragments in this activity
    private NavController mNavController;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate the layout
        View view = inflater.inflate(R.layout.fragment_enter_phone_number, container, false);
        //create the view model for this activity
        mViewModel = ((LoginActivity)getActivity()).createViewModel();

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    mViewModel.setRespEnterMobile(LoginActivity.getContext().getString(R.string.error_in_tel));
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    mViewModel.setRespEnterMobile(getString(R.string.sms_quota_exceeded));
                }
                hideSpinner();


                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mResendToken = token;
                mViewModel.setVerificationId(verificationId);
                hideSpinner();
                mNavController.navigate(R.id.action_navigation_home_to_activation_code);
            }
        };
        // [END phone_auth_callbacks]

        return view;
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
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
                                                Log.d(TAG, token);

                                                String country_code = mViewModel.getCountryCode().getValue();
                                                String tel_number = mViewModel.getMobileNumber().getValue();

                                                mViewModel.sendVerificationCode(
                                                        country_code,
                                                        tel_number,
                                                        token);
                                            }
                                            else
                                            {
                                                mViewModel.setRespEnterMobile(LoginActivity.getContext().getString(R.string.unexpected_error));
                                            }
                                            mViewModel.setIsWaitRespEnterMobile(false);
                                        }
                                    }
                            );
                            // Update UI

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mViewModel.setRespEnterMobile(LoginActivity.getContext().getString(R.string.verification_code_not_correct));
                            }
                            mViewModel.setIsWaitRespEnterMobile(false);
                        }
                    }
                });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //initialize all gui elements here after the view is created

        mOverlayDialog = new Dialog(this.getContext(), android.R.style.Theme_Panel);
        mSpinner =view.findViewById(R.id.MobNumberProgressBar);
        mSpinner.setVisibility(View.GONE);

        mStatus =view.findViewById(R.id.MobNumberStatus);

        Button nextBtn = view.findViewById(R.id.NextBtn);
        nextBtn.setOnClickListener(this);

        mCountryCodePicker = view.findViewById(R.id.ccp);
        mPhoneNumberEdt = view.findViewById(R.id.PhoneNumberEdt);
        mCountryCodePicker.registerPhoneNumberTextView(mPhoneNumberEdt);

        mNavController = Navigation.findNavController(EnterPhoneNumberFragment.this.getActivity(),
                R.id.nav_host_fragment_login);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //start observing the live data from the view model, which are

        //boolean IsVerificationCodeReceived that indicates if the requested verification code
        // received by the app or not
        mViewModel.getIsVerificationCodeReceived().observe(this, new VerificationCodeReceivedObserver());
        //boolean IsWaitRespEnterMobile that indicates if the process (request authentication
        // code of the drive with his telephone number) is running
        mViewModel.getIsWaitRespEnterMobile().observe(this, new LoadingObserver());
        //string RespEnterMobile which is the response of the process (request authentication
        // code of the drive with his telephone number) from the server
        mViewModel.getRespEnterMobile().observe(this, new statusObserver());
        //string CountryCode
        mViewModel.getCountryCode().observe(this, new CountryCodeObserver());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //stop observing live data when the fragment is paused
        mViewModel.getIsVerificationCodeReceived().removeObservers(this);
        mViewModel.getIsWaitRespEnterMobile().removeObservers(this);
        mViewModel.getRespEnterMobile().removeObservers(this);
        mViewModel.getCountryCode().removeObservers(this);
    }
    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: " + view.getId());
        //handle clicks on gui elements
        if(view.getId()== R.id.NextBtn) {
            //if the gui element is the Next button
            try {
                //get the entered telephone number
                String full_tel_number = mCountryCodePicker.getFullNumber().replace(" ","");
                full_tel_number = full_tel_number.replace("-","");
                full_tel_number = full_tel_number.replace("(","");
                full_tel_number = full_tel_number.replace(")","");
                String country_code = mCountryCodePicker.getSelectedCountryCode();
                //get the entered telephone number
                String tel_number = full_tel_number.substring(country_code.length());
                //call the model view function to request authentication
                //set the country code and mobile number
                mViewModel.setCountryCode(country_code);
                mViewModel.setMobileNumber(tel_number);
                //call the model view function to request authentication
                //code of the drive with his telephone number and country code

                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(mAuth)
                                .setPhoneNumber("+"+country_code+tel_number)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(this.getActivity())                 // Activity (for callback binding)
                                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                .build();

                mViewModel.requestVerificationCode(options, country_code, tel_number);
            }
            catch (Exception e)
            {
                Toast.makeText(this.getContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*function used to show spinner and the overlay dialog*/
    private void showSpinner() {
        mOverlayDialog.show();
        mSpinner.setVisibility(View.VISIBLE);
    }

    /*function used to hide spinner and the overlay dialog*/
    private void hideSpinner() {
        mOverlayDialog.dismiss();
        mSpinner.setVisibility(View.GONE);
    }

    /*Observer for the boolean live data IsWaitRespEnterMobile that indicates if the process
    (request authentication code of the drive with his telephone number) is running*/
    private class LoadingObserver implements Observer<Boolean> {

        @Override
        public void onChanged(@Nullable Boolean isLoading) {
            if (isLoading == null) return;

            //if the process is running
            if (isLoading) {
                //show spinner and overlay
                showSpinner();
            } else {
                //otherwise, hide the spinner and overlay
                hideSpinner();
            }
        }
    }

    /*Observer for the string live data RespEnterMobile which is the response of the process
    (request authentication code of the drive with his telephone number) from the server*/
    private class statusObserver implements Observer<String>
    {
        @Override
        public void onChanged(@Nullable String statusTxt) {
            if (statusTxt == null) return;
            //set the text in the mStatus text view with the response
            mStatus.setText(statusTxt);
        }
    }

    /*Observer for the string live data CountryCode. This is used to display the last selected
    country code in this fragment when the user press ResendCode button from the next
    fragment (ActivationCodeFragment)*/
    private class CountryCodeObserver implements Observer<String>
    {
        @Override
        public void onChanged(@Nullable String countryCode) {
            if (countryCode == null) return;
            try {
                //set the country in the mCountryCodePicker country picker
                mCountryCodePicker.setCountryForPhoneCode(Integer.parseInt(countryCode));
            }
            catch (Exception e )
            {

            }
        }
    }


    /*Observer for the boolean live data IsVerificationCodeReceived that indicates if the requested
    verification code received by the app or not*/
    private class VerificationCodeReceivedObserver implements Observer<Boolean> {

        @Override
        public void onChanged(@Nullable Boolean isVerificationCodeReceived) {
            if (isVerificationCodeReceived == null) return;
            //if the verification code is received, go to the next fragment (ActivationCode)
            if (isVerificationCodeReceived) {
                mNavController.navigate(R.id.action_navigation_home_to_activation_code);
            }
        }
    }
}
