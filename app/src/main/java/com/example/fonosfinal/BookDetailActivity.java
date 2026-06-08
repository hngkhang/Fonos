package com.example.fonosfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.adapters.ChapterAdapter;
import com.example.fonosfinal.adapters.ReviewAdapter;
import com.example.fonosfinal.models.Book;
import com.example.fonosfinal.models.Chapter;
import com.example.fonosfinal.models.Review;

import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_AUTHOR = "extra_author";
    public static final String EXTRA_NARRATOR = "extra_narrator";
    public static final String EXTRA_DURATION = "extra_duration";
    public static final String EXTRA_RATING = "extra_rating";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_COVER_TYPE = "extra_cover_type";
    public static final String EXTRA_BOOK_ID = "extra_book_id";
    private String title;
    private String author;
    private String narrator;
    private String duration;
    private String rating;
    private String category;
    private int coverType;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        readBookData();
        bindHeader();
        setupChapters();
        setupReviews();
        setupNavigation();
    }

    private void readBookData() {
        Intent intent = getIntent();
        title = intent.getStringExtra(EXTRA_TITLE);
        author = intent.getStringExtra(EXTRA_AUTHOR);
        narrator = intent.getStringExtra(EXTRA_NARRATOR);
        duration = intent.getStringExtra(EXTRA_DURATION);
        rating = intent.getStringExtra(EXTRA_RATING);
        category = intent.getStringExtra(EXTRA_CATEGORY);
        coverType = intent.getIntExtra(EXTRA_COVER_TYPE, 1);
        bookId = intent.getStringExtra(EXTRA_BOOK_ID);

        if (title == null) {
            title = "Atomic Habits";
        }
        if (author == null) {
            author = "James Clear";
        }
        if (narrator == null) {
            narrator = getNarratorForTitle(title);
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

    private void bindHeader() {
        View coverView = findViewById(R.id.view_detail_cover);
        coverView.setBackgroundResource(getCoverDrawable(coverType));

        ((TextView) findViewById(R.id.text_detail_title)).setText(title);
        ((TextView) findViewById(R.id.text_detail_author)).setText(author);
        ((TextView) findViewById(R.id.text_detail_narrator)).setText(narrator);
        ((TextView) findViewById(R.id.text_detail_rating)).setText("★ " + rating);
        ((TextView) findViewById(R.id.text_detail_duration)).setText(duration);
        ((TextView) findViewById(R.id.text_detail_category)).setText(category);
        ((TextView) findViewById(R.id.text_detail_about)).setText(
                title + " is a premium audiobook built for focused listening. Explore practical ideas, memorable stories, and clear chapters designed for short sessions or deep listening.");
        ((TextView) findViewById(R.id.text_detail_metadata)).setText(
                "Duration: " + duration + "\nLanguage: English\nCategory: " + category + "\nRelease year: 2024");
        ((TextView) findViewById(R.id.text_rating_summary)).setText(rating + " average rating from audiobook listeners");
    }

    private void setupChapters() {
        RecyclerView recyclerView = findViewById(R.id.recycler_chapters);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ChapterAdapter(createChapters()));
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupReviews() {
        RecyclerView recyclerView = findViewById(R.id.recycler_reviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ReviewAdapter(createReviews()));
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupNavigation() {
        findViewById(R.id.button_book_detail_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_play_audiobook).setOnClickListener(v -> openPlayer());

        findViewById(R.id.button_add_review).setOnClickListener(v -> {
            Intent intent = createBookIntent(AddReviewActivity.class);
            startActivity(intent);
        });
    }

    private void openPlayer() {
        Intent intent = createBookIntent(PlayerActivity.class);
        startActivity(intent);
    }

    private Intent createBookIntent(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_AUTHOR, author);
        intent.putExtra(EXTRA_NARRATOR, narrator);
        intent.putExtra(EXTRA_DURATION, duration);
        intent.putExtra(EXTRA_RATING, rating);
        intent.putExtra(EXTRA_CATEGORY, category);
        intent.putExtra(EXTRA_COVER_TYPE, coverType);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        return intent;
    }

    public static Intent createBookDetailIntent(android.content.Context context, Book book) {
        Intent intent = new Intent(context, BookDetailActivity.class);
        intent.putExtra(EXTRA_TITLE, book.getTitle());
        intent.putExtra(EXTRA_AUTHOR, book.getAuthor());
        intent.putExtra(EXTRA_NARRATOR, book.getNarrator());
        intent.putExtra(EXTRA_DURATION, book.getDuration());
        intent.putExtra(EXTRA_RATING, book.getRating());
        intent.putExtra(EXTRA_CATEGORY, book.getCategory());
        intent.putExtra(EXTRA_COVER_TYPE, book.getCoverType());
        intent.putExtra(EXTRA_BOOK_ID, book.getRemoteId());
        return intent;
    }

    private List<Chapter> createChapters() {
        List<Chapter> chapters = new ArrayList<>();
        chapters.add(new Chapter("01", "Introduction", "15 min", "free"));
        chapters.add(new Chapter("02", "Chapter 1: The Beginning", "28 min", "free"));
        chapters.add(new Chapter("03", "Chapter 2: Building the Habit", "35 min", "locked"));
        chapters.add(new Chapter("04", "Chapter 3: Small Changes", "42 min", "locked"));
        chapters.add(new Chapter("05", "Chapter 4: Long-term Growth", "31 min", "locked"));
        return chapters;
    }

    private List<Review> createReviews() {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("Minh Anh", "★★★★★", "Clear narration and short chapters make it easy to continue during commutes.", "2 days ago"));
        reviews.add(new Review("David Nguyen", "★★★★☆", "A polished audiobook experience with practical ideas and strong pacing.", "1 week ago"));
        return reviews;
    }

    private String getNarratorForTitle(String bookTitle) {
        if ("Atomic Habits".equals(bookTitle)) {
            return "Narrated by John Smith";
        } else if ("Deep Work".equals(bookTitle)) {
            return "Narrated by Alan Reed";
        } else if ("The Psychology of Money".equals(bookTitle)) {
            return "Narrated by Chris Hill";
        } else if ("Ikigai".equals(bookTitle)) {
            return "Narrated by Daniel Lee";
        } else if ("The Alchemist".equals(bookTitle)) {
            return "Narrated by Mark Bramhall";
        }
        return "Narrated by Fonos Studio";
    }

    private int getCoverDrawable(int type) {
        if (type == 1) {
            return R.drawable.bg_cover_gradient_1;
        } else if (type == 2) {
            return R.drawable.bg_cover_gradient_2;
        } else if (type == 3) {
            return R.drawable.bg_cover_gradient_3;
        } else if (type == 4) {
            return R.drawable.bg_cover_gradient_4;
        }
        return R.drawable.bg_cover_gradient_1;
    }
}
