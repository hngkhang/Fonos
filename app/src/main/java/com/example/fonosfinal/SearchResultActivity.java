package com.example.fonosfinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.adapters.SearchResultAdapter;
import com.example.fonosfinal.models.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "extra_query";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        String query = getIntent().getStringExtra(EXTRA_QUERY);
        if (query == null) {
            query = "business";
        }

        ((TextView) findViewById(R.id.text_search_result_query)).setText(query);
        ((TextView) findViewById(R.id.text_results_for)).setText("Results for '" + query + "'");
        findViewById(R.id.button_search_result_back).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recycler_search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SearchResultAdapter(createResults(), this::openBookDetail));
    }

    private void openBookDetail(SearchResult result) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_TITLE, result.getTitle());
        intent.putExtra(BookDetailActivity.EXTRA_AUTHOR, result.getAuthor());
        intent.putExtra(BookDetailActivity.EXTRA_NARRATOR, result.getNarrator());
        intent.putExtra(BookDetailActivity.EXTRA_DURATION, result.getDuration());
        intent.putExtra(BookDetailActivity.EXTRA_RATING, result.getRating());
        intent.putExtra(BookDetailActivity.EXTRA_CATEGORY, result.getCategory());
        intent.putExtra(BookDetailActivity.EXTRA_COVER_TYPE, result.getCoverType());
        startActivity(intent);
    }

    private List<SearchResult> createResults() {
        List<SearchResult> results = new ArrayList<>();
        results.add(new SearchResult("Atomic Habits", "James Clear", "Narrated by John Smith", "5h 20m", "4.9", "Self-help", 1));
        results.add(new SearchResult("Deep Work", "Cal Newport", "Narrated by Alan Reed", "7h 45m", "4.7", "Productivity", 2));
        results.add(new SearchResult("The Psychology of Money", "Morgan Housel", "Narrated by Chris Hill", "6h 10m", "4.8", "Business", 3));
        results.add(new SearchResult("The Alchemist", "Paulo Coelho", "Narrated by Mark Bramhall", "4h 30m", "4.8", "Fiction", 4));
        results.add(new SearchResult("Ikigai", "Hector Garcia", "Narrated by Daniel Lee", "3h 55m", "4.7", "Lifestyle", 2));
        results.add(new SearchResult("Rich Dad Poor Dad", "Robert Kiyosaki", "Narrated by Tim Wheeler", "8h 15m", "4.6", "Finance", 1));
        return results;
    }
}
