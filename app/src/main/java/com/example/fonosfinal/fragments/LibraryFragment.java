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
import com.example.fonosfinal.PlayerActivity;
import com.example.fonosfinal.R;
import com.example.fonosfinal.models.Book;

public class LibraryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Book deepWork = new Book("Deep Work", "Cal Newport", "7h 45m", "4.7", 2, "Productivity", "Narrated by Alan Reed");
        view.findViewById(R.id.layout_library_continue_card).setOnClickListener(v -> openPlayer(deepWork));
        setBookClick(view, R.id.card_library_atomic_habits, new Book("Atomic Habits", "James Clear", "5h 20m", "4.9", 1, "Self-help", "Narrated by John Smith"));
        setBookClick(view, R.id.card_library_creative_act, new Book("The Creative Act", "Rick Rubin", "5h 45m", "4.9", 4, "Creativity", "Narrated by Rick Rubin"));
        setBookClick(view, R.id.card_library_ikigai, new Book("Ikigai", "Hector Garcia", "3h 55m", "4.7", 3, "Lifestyle", "Narrated by Daniel Lee"));
    }

    private void setBookClick(View parent, int viewId, Book book) {
        parent.findViewById(viewId).setOnClickListener(v ->
                startActivity(BookDetailActivity.createBookDetailIntent(requireContext(), book)));
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
        startActivity(intent);
    }
}
