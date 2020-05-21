package com.example.nidar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class HelpFeedback extends AppCompatActivity {

    TextInputEditText etEmail, etFeedback;
    Button btnSend;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_and_feedback);

        findViews();
        setButton();
    }

    private void findViews(){
        etEmail = findViewById(R.id.et_Email);
        etFeedback = findViewById(R.id.et_Feedback);
        btnSend = findViewById(R.id.btn_send);
    }

    private void setButton(){
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HelpFeedback.this, "Feedback Sent", Toast.LENGTH_LONG).show();
            }
        });
    }
}
