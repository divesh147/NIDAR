package com.example.nidar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignupUser extends AppCompatActivity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Button btnGenerateOTP, btnSignIn;
    TextInputEditText etPhoneNumber, etOTP;
    String phoneNumber, otp;
    FirebaseAuth auth;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_user);

        pref = getSharedPreferences("NIDAR", MODE_PRIVATE);
        editor = pref.edit();
        findViews1();

        StartFirebaseLogin();

        btnGenerateOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = etPhoneNumber.getText().toString();

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+91" + phoneNumber,          // Phone number to verify
                        60,                           // Timeout duration
                        TimeUnit.SECONDS,                // Unit of timeout
                        SignupUser.this,         // Activity (for callback binding)
                        mCallback);                      // OnVerificationStateChangedCallbacks
            }
        });
    }

    // Checks whether sent OTP matches or not
    private void SigninWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            editor.putBoolean("isSignedIn", true);
                            editor.putString("phoneNumber", phoneNumber);
                            editor.commit();

                            startActivity(new Intent(SignupUser.this, SignedIn.class));
                            finish();
                        }
                        else {
                            Toast.makeText(SignupUser.this,"Incorrect OTP", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Initialise View Items of first Screen that is generating OTP
    private void findViews1() {
        btnGenerateOTP = findViewById(R.id.btn_generate_otp);
        etPhoneNumber = findViewById(R.id.et_phone_number);
    }

    // Initialise View Items of second Screen that is validating OTP
    private void findViews2() {
        btnSignIn = findViewById(R.id.btn_sign_in);
        etOTP = findViewById(R.id.et_otp);
    }

    // Function to start phone number authentication
    private void StartFirebaseLogin() {
        auth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(SignupUser.this,"Verification Completed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                etPhoneNumber.setError( "Enter a valid 10 digit Phone Number" );
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                Toast.makeText(SignupUser.this,"Code Sent", Toast.LENGTH_SHORT).show();

                setContentView(R.layout.activity_check_o_t_p);
                findViews2();

                btnSignIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        otp = etOTP.getText().toString();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                        SigninWithPhone(credential);
                    }
                });
            }
        };
    }

}