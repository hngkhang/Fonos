package com.example.fonosfinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fonosfinal.BookDetailActivity;
import com.example.fonosfinal.NarratorProfileActivity;
import com.example.fonosfinal.R;
import com.example.fonosfinal.SearchResultActivity;
import com.example.fonosfinal.TopNarratorsActivity;
import com.example.fonosfinal.models.Book;

public class SearchFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.layout_search_search_bar).setOnClickListener(v -> openSearchResults("business"));
        setSearchClick(view, R.id.chip_search_focus, "focus");
        setSearchClick(view, R.id.chip_search_money, "money");
        setSearchClick(view, R.id.chip_search_sleep, "sleep");
        setSearchClick(view, R.id.chip_search_fiction, "fiction");
        setSearchClick(view, R.id.chip_search_habits, "habits");
        setSearchClick(view, R.id.text_recent_atomic_habits, "Atomic Habits");
        setSearchClick(view, R.id.text_recent_business, "business");

        setBookClick(view, R.id.card_search_deep_work, new Book("Deep Work", "Cal Newport", "7h 45m", "4.7", 2, "Productivity", "Narrated by Alan Reed"));
        setBookClick(view, R.id.card_search_ikigai, new Book("Ikigai", "Hector Garcia", "3h 55m", "4.7", 3, "Lifestyle", "Narrated by Daniel Lee"));

        view.findViewById(R.id.chip_search_james_clear).setOnClickListener(v ->
                openNarratorProfile("James Clear", "Author / 8 audiobooks", "★ 4.9"));
        view.findViewById(R.id.chip_search_morgan_housel).setOnClickListener(v ->
                openNarratorProfile("Morgan Housel", "Author / 6 audiobooks", "★ 4.8"));
    }

    private void setSearchClick(View parent, int viewId, String query) {
        parent.findViewById(viewId).setOnClickListener(v -> openSearchResults(query));
    }

    private void setBookClick(View parent, int viewId, Book book) {
        parent.findViewById(viewId).setOnClickListener(v ->
                startActivity(BookDetailActivity.createBookDetailIntent(requireContext(), book)));
    }

    private void openSearchResults(String query) {
        Intent intent = new Intent(requireContext(), SearchResultActivity.class);
        intent.putExtra(SearchResultActivity.EXTRA_QUERY, query);
        startActivity(intent);
    }

    private void openNarratorProfile(String name, String subtitle, String rating) {
        Intent intent = new Intent(requireContext(), NarratorProfileActivity.class);
        intent.putExtra(TopNarratorsActivity.EXTRA_NARRATOR_NAME, name);
        intent.putExtra(TopNarratorsActivity.EXTRA_NARRATOR_SUBTITLE, subtitle);
        intent.putExtra(TopNarratorsActivity.EXTRA_NARRATOR_RATING, rating);
        startActivity(intent);
    }
}
