package com.example.instachat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.instachat.databinding.ActivityOTPBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {
    ActivityOTPBinding binding;
    private static final String TAG = OTPActivity.class.getSimpleName();
    FirebaseAuth auth;
    String verificationId;
    private static final String KEY_VERIFICATION_ID = "key_verification_id";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOTPBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog =  new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();
        String phoneNumber = getIntent().getStringExtra("phoneNumber");

        try {
            Log.d(TAG, phoneNumber);
            binding.phoneLabel.setText("Verify " + phoneNumber);
        } catch (NullPointerException e) {
            Log.e(TAG,"phone number is returning null value");
        }
        try {
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTPActivity.this)
                .setCallbacks (new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    Log.d(TAG,"on Verification Completed");
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.d(TAG,"on Verification failed");
                }

                @Override
                public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(verifyId, forceResendingToken);
                    verificationId =verifyId;
                    InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    binding.otpView.requestFocus();
                    progressDialog.dismiss();
                    progressDialog.setCancelable(true);
                }

            }).build();
            PhoneAuthProvider.verifyPhoneNumber(options);
            if (verificationId == null && savedInstanceState != null) {
                    onRestoreInstanceState(savedInstanceState);
            }
        } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
        }
        try {
            binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
                @Override
                public void onOtpCompleted(String otp) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                    Log.d(TAG,"credential is : " + credential.toString());
                    auth.signInWithCredential(credential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(OTPActivity.this,SetupProfileActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    } else {
                                        Toast.makeText(OTPActivity.this, "Logged In.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        } catch(IllegalArgumentException e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_VERIFICATION_ID,verificationId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        verificationId = savedInstanceState.getString(KEY_VERIFICATION_ID);
    }

}