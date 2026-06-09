package com.example.fonosfinal.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fonosfinal.R;
import com.example.fonosfinal.SearchResultActivity;

public class SearchFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText searchEditText = view.findViewById(R.id.edit_search_query);
        view.findViewById(R.id.layout_search_search_bar).setOnClickListener(v -> focusSearch(searchEditText));
        view.findViewById(R.id.image_search_submit)
                .setOnClickListener(v -> openSearchResults(searchEditText.getText().toString()));
        view.findViewById(R.id.text_search_clear)
                .setOnClickListener(v -> searchEditText.setText(""));

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                openSearchResults(searchEditText.getText().toString());
                return true;
            }
            return false;
        });

        setSearchClick(view, R.id.chip_search_focus, "focus");
        setSearchClick(view, R.id.chip_search_money, "money");
        setSearchClick(view, R.id.chip_search_sleep, "sleep");
        setSearchClick(view, R.id.chip_search_fiction, "fiction");
        setSearchClick(view, R.id.chip_search_habits, "habits");
        view.findViewById(R.id.text_recent_searches).setVisibility(View.GONE);
        view.findViewById(R.id.layout_recent_searches).setVisibility(View.GONE);
        view.findViewById(R.id.text_suggested_audiobooks).setVisibility(View.GONE);
        view.findViewById(R.id.layout_search_books).setVisibility(View.GONE);
        view.findViewById(R.id.text_top_narrators).setVisibility(View.GONE);
        view.findViewById(R.id.layout_top_narrators).setVisibility(View.GONE);
    }

    private void setSearchClick(View parent, int viewId, String query) {
        parent.findViewById(viewId).setOnClickListener(v -> openSearchResults(query));
    }

    private void focusSearch(EditText searchEditText) {
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void openSearchResults(String query) {
        String safeQuery = query == null ? "" : query.trim();
        if (safeQuery.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a book title to search.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(requireContext(), SearchResultActivity.class);
        intent.putExtra(SearchResultActivity.EXTRA_QUERY, safeQuery);
        startActivity(intent);
    }
}
