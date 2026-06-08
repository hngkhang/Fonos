package com.example.fonosfinal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        findViewById(R.id.button_forgot_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_send_reset_link).setOnClickListener(v -> finish());
        findViewById(R.id.text_back_to_login).setOnClickListener(v -> finish());
    }
}
