package com.example.fonosfinal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        findViewById(R.id.button_get_started).setOnClickListener(v ->
                startActivity(new Intent(OnboardingActivity.this, RegisterActivity.class)));
        findViewById(R.id.text_already_have_account).setOnClickListener(v ->
                startActivity(new Intent(OnboardingActivity.this, LoginActivity.class)));
    }
}
