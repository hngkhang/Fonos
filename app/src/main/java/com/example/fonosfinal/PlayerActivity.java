package com.example.fonosfinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    private String title;
    private String author;
    private String narrator;
    private String duration;
    private String rating;
    private String category;
    private int coverType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        readBookData();
        bindContent();
        setupNavigation();
    }

    private void readBookData() {
        Intent intent = getIntent();
        title = intent.getStringExtra(BookDetailActivity.EXTRA_TITLE);
        author = intent.getStringExtra(BookDetailActivity.EXTRA_AUTHOR);
        narrator = intent.getStringExtra(BookDetailActivity.EXTRA_NARRATOR);
        duration = intent.getStringExtra(BookDetailActivity.EXTRA_DURATION);
        rating = intent.getStringExtra(BookDetailActivity.EXTRA_RATING);
        category = intent.getStringExtra(BookDetailActivity.EXTRA_CATEGORY);
        coverType = intent.getIntExtra(BookDetailActivity.EXTRA_COVER_TYPE, 1);

        if (title == null) {
            title = "Atomic Habits";
        }
        if (author == null) {
            author = "James Clear";
        }
        if (narrator == null) {
            narrator = "Narrated by John Smith";
        }
        if (duration == null) {
            duration = "5h 20m";
        }
        if (rating == null) {
            rating = "4.9";
        }
        if (category == null) {
            category = "Self-help";
        }
    }

    private void bindContent() {
        ((TextView) findViewById(R.id.text_player_title)).setText(title);
        ((TextView) findViewById(R.id.text_player_subtitle)).setText(author + "  |  " + narrator);
    }

    private void setupNavigation() {
        findViewById(R.id.button_player_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_back_to_details).setOnClickListener(v -> {
            Intent intent = new Intent(PlayerActivity.this, BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_TITLE, title);
            intent.putExtra(BookDetailActivity.EXTRA_AUTHOR, author);
            intent.putExtra(BookDetailActivity.EXTRA_NARRATOR, narrator);
            intent.putExtra(BookDetailActivity.EXTRA_DURATION, duration);
            intent.putExtra(BookDetailActivity.EXTRA_RATING, rating);
            intent.putExtra(BookDetailActivity.EXTRA_CATEGORY, category);
            intent.putExtra(BookDetailActivity.EXTRA_COVER_TYPE, coverType);
            startActivity(intent);
            finish();
        });
    }
}
