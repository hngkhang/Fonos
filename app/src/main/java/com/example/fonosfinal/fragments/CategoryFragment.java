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
import com.example.fonosfinal.R;
import com.example.fonosfinal.SearchResultActivity;
import com.example.fonosfinal.models.Book;

public class CategoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.layout_category_search_bar).setOnClickListener(v -> openSearchResults("business"));
        setBookClick(view, R.id.card_category_money, new Book("The Psychology of Money", "Morgan Housel", "6h 10m", "4.8", 2, "Business", "Narrated by Chris Hill"));
        setBookClick(view, R.id.card_category_start_why, new Book("Start With Why", "Simon Sinek", "7h 18m", "4.8", 3, "Business", "Narrated by Michael Turner"));
        setBookClick(view, R.id.card_category_alchemist, new Book("The Alchemist", "Paulo Coelho", "4h 30m", "4.8", 4, "Fiction", "Narrated by Mark Bramhall"));
        setBookClick(view, R.id.card_category_home, new Book("You'd Be Home", "Alex Michaelides", "6h 30m", "4.4", 2, "Fiction", "Narrated by Chris Hill"));
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
}
