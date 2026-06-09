package com.example.fonosfinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.BookDetailActivity;
import com.example.fonosfinal.NotificationActivity;
import com.example.fonosfinal.NarratorProfileActivity;
import com.example.fonosfinal.PlayerActivity;
import com.example.fonosfinal.R;
import com.example.fonosfinal.TopNarratorsActivity;
import com.example.fonosfinal.adapters.BookAdapter;
import com.example.fonosfinal.models.Book;

import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;

import com.example.fonosfinal.data.repository.BookRepository;

public class HomeFragment extends Fragment {

    private BookAdapter trendingAdapter;
    private BookAdapter recommendedAdapter;
    private BookAdapter newReleaseAdapter;
    private BookRepository bookRepository;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookRepository = new BookRepository(requireContext());

        trendingAdapter = setupBookList(view.findViewById(R.id.recycler_trending_books));
        recommendedAdapter = setupBookList(view.findViewById(R.id.recycler_recommended_books));
        newReleaseAdapter = setupBookList(view.findViewById(R.id.recycler_new_release_books));

        setupHeaderNavigation(view);
        setupNarratorNavigation(view);

        loadHomeBooks();
    }

    private BookAdapter setupBookList(RecyclerView recyclerView) {
        BookAdapter adapter = new BookAdapter(new ArrayList<>(), this::openBookDetail);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        return adapter;
    }
    private void loadHomeBooks() {
        bookRepository.loadHomeBooks(new BookRepository.HomeBooksCallback() {
            @Override
            public void onLocalLoaded(List<Book> trending, List<Book> recommended, List<Book> newReleases) {
                trendingAdapter.updateBooks(trending);
                recommendedAdapter.updateBooks(recommended);
                newReleaseAdapter.updateBooks(newReleases);
            }

            @Override
            public void onRemoteSynced(List<Book> trending, List<Book> recommended, List<Book> newReleases) {
                trendingAdapter.updateBooks(trending);
                recommendedAdapter.updateBooks(recommended);
                newReleaseAdapter.updateBooks(newReleases);
            }

            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Cannot sync books. Showing offline data.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupHeaderNavigation(View view) {
        Book heroBook = new Book("Atomic Habits", "James Clear", "5h 20m", "4.9", 1, "Self-help", "Narrated by John Smith");
        view.findViewById(R.id.image_home_notification).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationActivity.class)));
        view.findViewById(R.id.layout_home_search_bar).setOnClickListener(v -> openSearchResults("business"));
        view.findViewById(R.id.layout_home_hero_card).setOnClickListener(v -> openBookDetail(heroBook));
        view.findViewById(R.id.button_hero_listen_now).setOnClickListener(v -> openPlayer(heroBook));
    }

    private void setupNarratorNavigation(View view) {
        view.findViewById(R.id.card_home_narrator_john).setOnClickListener(v -> openNarratorProfile("John Smith", "24 audiobooks", "★ 4.9"));
        view.findViewById(R.id.card_home_narrator_alan).setOnClickListener(v -> openNarratorProfile("Alan Reed", "18 audiobooks", "★ 4.8"));
    }

    private void openBookDetail(Book book) {
        startActivity(BookDetailActivity.createBookDetailIntent(requireContext(), book));
    }

    private void openPlayer(Book book) {
        Intent intent = new Intent(requireContext(), PlayerActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_TITLE, book.getTitle());
        intent.putExtra(BookDetailActivity.EXTRA_AUTHOR, book.getAuthor());
        intent.putExtra(BookDetailActivity.EXTRA_NARRATOR, book.getNarrator());
        intent.putExtra(BookDetailActivity.EXTRA_DURATION, book.getDuration());
        intent.putExtra(BookDetailActivity.EXTRA_RATING, book.getRating());
        intent.putExtra(BookDetailActivity.EXTRA_CATEGORY, book.getCategory());
        intent.putExtra(BookDetailActivity.EXTRA_COVER_TYPE, book.getCoverType());
        intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getRemoteId());
        intent.putExtra(BookDetailActivity.EXTRA_COVER_URL, book.getCoverUrl());
        intent.putExtra(BookDetailActivity.EXTRA_DESCRIPTION, book.getDescription());
        startActivity(intent);
    }

    private void openSearchResults(String query) {
        Intent intent = new Intent(requireContext(), com.example.fonosfinal.SearchResultActivity.class);
        intent.putExtra(com.example.fonosfinal.SearchResultActivity.EXTRA_QUERY, query);
        startActivity(intent);
    }

    private void openNarratorProfile(String name, String subtitle, String rating) {
        Intent intent = new Intent(requireContext(), NarratorProfileActivity.class);
        intent.putExtra(TopNarratorsActivity.EXTRA_NARRATOR_NAME, name);
        intent.putExtra(TopNarratorsActivity.EXTRA_NARRATOR_SUBTITLE, subtitle);
        intent.putExtra(TopNarratorsActivity.EXTRA_NARRATOR_RATING, rating);
        startActivity(intent);
    }

    private List<Book> createTrendingBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book("Atomic Habits", "James Clear", "5h 20m", "4.9", 1, "Self-help", "Narrated by John Smith"));
        books.add(new Book("The Psychology of Money", "Morgan Housel", "6h 10m", "4.8", 2, "Business", "Narrated by Chris Hill"));
        books.add(new Book("Deep Work", "Cal Newport", "7h 45m", "4.7", 3, "Productivity", "Narrated by Alan Reed"));
        books.add(new Book("The Alchemist", "Paulo Coelho", "4h 30m", "4.8", 4, "Fiction", "Narrated by Mark Bramhall"));
        return books;
    }

    private List<Book> createRecommendedBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book("Ikigai", "Hector Garcia", "3h 55m", "4.7", 2, "Lifestyle", "Narrated by Daniel Lee"));
        books.add(new Book("Rich Dad Poor Dad", "Robert Kiyosaki", "8h 15m", "4.6", 1, "Finance", "Narrated by Tim Wheeler"));
        books.add(new Book("Mindfulness Journal", "Johnathan Swift", "2h 45m", "4.5", 4, "Wellness", "Narrated by Sophia Brown"));
        books.add(new Book("Start With Why", "Simon Sinek", "7h 18m", "4.8", 3, "Business", "Narrated by Michael Turner"));
        return books;
    }

    private List<Book> createNewReleaseBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book("The Creative Act", "Rick Rubin", "5h 45m", "4.9", 4, "Creativity", "Narrated by Rick Rubin"));
        books.add(new Book("Hidden Potential", "Adam Grant", "8h 35m", "4.6", 3, "Growth", "Narrated by Sophia Brown"));
        books.add(new Book("You'd Be Home", "Alex Michaelides", "6h 30m", "4.4", 2, "Fiction", "Narrated by Chris Hill"));
        books.add(new Book("The Whispers", "Greg Howard", "4h 55m", "4.5", 1, "Drama", "Narrated by Alan Reed"));
        return books;
    }
}
