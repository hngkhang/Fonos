package com.example.fonosfinal;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        EditText nameEditText = findViewById(R.id.edit_text_register_name);
        EditText emailEditText = findViewById(R.id.edit_text_register_email);
        EditText passwordEditText = findViewById(R.id.edit_text_register_password);

        Button signUpButton = findViewById(R.id.button_sign_up);
        TextView loginTextView = findViewById(R.id.text_login);

        signUpButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (name.isEmpty()) {
                nameEditText.setError("Name is required");
                return;
            }

            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                return;
            }

            if (password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }

            signUpButton.setEnabled(false);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", uid);
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

                        firestore.collection("users")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener(unused -> {
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    signUpButton.setEnabled(true);
                                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        signUpButton.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        setupAuthLink(
                loginTextView,
                "Already Have An Account? Login",
                "Login",
                v -> {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
        );
    }

    private void setupAuthLink(TextView textView, String fullText, String clickableText, View.OnClickListener listener) {
        SpannableString spannableString = new SpannableString(fullText);
        int start = fullText.indexOf(clickableText);
        int end = start + clickableText.length();

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                listener.onClick(widget);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getColor(R.color.fonos_main));
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }
}
