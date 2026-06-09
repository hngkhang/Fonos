package com.example.fonosfinal;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.adapters.SearchResultAdapter;
import com.example.fonosfinal.data.repository.BookRepository;
import com.example.fonosfinal.models.Book;

import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "extra_query";
    private static final String TAG = "SearchResultActivity";

    private EditText queryEditText;
    private TextView resultsForTextView;
    private TextView emptyTextView;
    private TextView errorTextView;
    private View progressView;
    private SearchResultAdapter adapter;
    private BookRepository bookRepository;
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        bookRepository = new BookRepository(this);
        bindViews();
        setupSearchInput();
        setupResultsList();

        String query = getIntent().getStringExtra(EXTRA_QUERY);
        queryEditText.setText(query == null ? "" : query);
        performSearch(query);
    }

    private void bindViews() {
        queryEditText = findViewById(R.id.text_search_result_query);
        resultsForTextView = findViewById(R.id.text_results_for);
        emptyTextView = findViewById(R.id.text_search_empty);
        errorTextView = findViewById(R.id.text_search_error);
        progressView = findViewById(R.id.progress_search_results);
        findViewById(R.id.button_search_result_back).setOnClickListener(v -> finish());
    }

    private void setupSearchInput() {
        findViewById(R.id.image_search_result_submit)
                .setOnClickListener(v -> {
                    performSearch(queryEditText.getText().toString());
                    hideKeyboard();
                });
        queryEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(queryEditText.getText().toString());
                hideKeyboard();
                return true;
            }
            return false;
        });
        findViewById(R.id.text_search_result_query).setOnClickListener(v -> queryEditText.requestFocus());
    }

    private void setupResultsList() {
        RecyclerView recyclerView = findViewById(R.id.recycler_search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter(new ArrayList<>(), this::openBookDetail);
        recyclerView.setAdapter(adapter);
    }

    private void performSearch(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim();
        currentQuery = query;
        errorTextView.setVisibility(View.GONE);

        if (query.isEmpty()) {
            resultsForTextView.setText("Search by book title");
            adapter.updateData(new ArrayList<>());
            progressView.setVisibility(View.GONE);
            emptyTextView.setText("Enter a book title to search.");
            emptyTextView.setVisibility(View.VISIBLE);
            return;
        }

        Log.d(TAG, "Search query: " + query);
        resultsForTextView.setText("Results for '" + query + "'");
        emptyTextView.setVisibility(View.GONE);
        progressView.setVisibility(View.VISIBLE);
        adapter.updateData(new ArrayList<>());

        final String submittedQuery = query;
        bookRepository.searchBooksByTitle(query, new BookRepository.SearchCallback() {
            @Override
            public void onLocalResult(List<Book> books) {
                if (!submittedQuery.equals(currentQuery)) return;
                if (!books.isEmpty()) {
                    adapter.updateData(books);
                    progressView.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onRemoteResult(List<Book> books) {
                if (!submittedQuery.equals(currentQuery)) return;
                progressView.setVisibility(View.GONE);
                errorTextView.setVisibility(View.GONE);
                adapter.updateData(books);
                emptyTextView.setText("No books found for \"" + submittedQuery + "\".");
                emptyTextView.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                if (!submittedQuery.equals(currentQuery)) return;
                progressView.setVisibility(View.GONE);
                Log.e(TAG, "Search failed", e);
                if (adapter.getItemCount() == 0) {
                    errorTextView.setText("Unable to search online. No cached books found for this title.");
                    errorTextView.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.GONE);
                } else {
                    errorTextView.setText("Unable to refresh online results. Showing cached books.");
                    errorTextView.setVisibility(View.VISIBLE);
                }
                Toast.makeText(SearchResultActivity.this,
                        "Cannot refresh search results.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openBookDetail(Book book) {
        startActivity(BookDetailActivity.createBookDetailIntent(this, book));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(queryEditText.getWindowToken(), 0);
        }
    }
}
