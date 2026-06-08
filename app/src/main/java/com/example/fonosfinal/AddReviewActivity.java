package com.example.fonosfinal;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AddReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        String title = getIntent().getStringExtra(BookDetailActivity.EXTRA_TITLE);
        String author = getIntent().getStringExtra(BookDetailActivity.EXTRA_AUTHOR);
        String narrator = getIntent().getStringExtra(BookDetailActivity.EXTRA_NARRATOR);

        if (title == null) {
            title = "Atomic Habits";
        }
        if (author == null) {
            author = "James Clear";
        }
        if (narrator == null) {
            narrator = "Narrated by John Smith";
        }

        ((TextView) findViewById(R.id.text_add_review_title)).setText(title);
        ((TextView) findViewById(R.id.text_add_review_meta)).setText(author + "  |  " + narrator);

        findViewById(R.id.button_add_review_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_submit_review).setOnClickListener(v -> finish());
    }
}
