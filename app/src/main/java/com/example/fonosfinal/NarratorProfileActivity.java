package com.example.fonosfinal;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.adapters.BookAdapter;
import com.example.fonosfinal.models.Book;

import java.util.ArrayList;
import java.util.List;

public class NarratorProfileActivity extends AppCompatActivity {

    private String narratorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_narrator_profile);

        narratorName = getIntent().getStringExtra(TopNarratorsActivity.EXTRA_NARRATOR_NAME);
        String rating = getIntent().getStringExtra(TopNarratorsActivity.EXTRA_NARRATOR_RATING);
        if (narratorName == null) {
            narratorName = "John Smith";
        }
        if (rating == null) {
            rating = "★ 4.9";
        }

        ((TextView) findViewById(R.id.text_narrator_profile_name)).setText(narratorName);
        ((TextView) findViewById(R.id.text_narrator_profile_rating)).setText(rating);
        ((TextView) findViewById(R.id.text_narrator_profile_followers)).setText("18.4k followers");
        ((TextView) findViewById(R.id.text_narrator_about)).setText(
                narratorName + " brings warm pacing, clear pronunciation, and a calm audiobook presence across business, fiction, and self-development titles.");

        findViewById(R.id.button_narrator_profile_back).setOnClickListener(v -> finish());
        setupPopularBooks();
    }

    private void setupPopularBooks() {
        RecyclerView recyclerView = findViewById(R.id.recycler_narrator_books);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new BookAdapter(createPopularBooks(), book ->
                startActivity(BookDetailActivity.createBookDetailIntent(this, book))));
        recyclerView.setNestedScrollingEnabled(false);
    }

    private List<Book> createPopularBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book("Atomic Habits", "James Clear", "5h 20m", "4.9", 1, "Self-help", "Narrated by " + narratorName));
        books.add(new Book("Deep Work", "Cal Newport", "7h 45m", "4.7", 2, "Productivity", "Narrated by " + narratorName));
        books.add(new Book("The Psychology of Money", "Morgan Housel", "6h 10m", "4.8", 3, "Business", "Narrated by " + narratorName));
        return books;
    }
}
