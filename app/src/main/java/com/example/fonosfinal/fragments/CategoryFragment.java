package com.example.fonosfinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.BookDetailActivity;
import com.example.fonosfinal.R;
import com.example.fonosfinal.SearchResultActivity;
import com.example.fonosfinal.adapters.CategoryBookAdapter;
import com.example.fonosfinal.data.repository.BookRepository;
import com.example.fonosfinal.models.Book;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CategoryFragment extends Fragment {

    private final List<Book> allBooks = new ArrayList<>();
    private CategoryBookAdapter adapter;
    private LinearLayout chipContainer;
    private TextView sectionTitle;
    private TextView emptyText;
    private View progress;
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipContainer = view.findViewById(R.id.layout_category_chips);
        sectionTitle = view.findViewById(R.id.text_category_section);
        emptyText = view.findViewById(R.id.text_category_empty);
        progress = view.findViewById(R.id.progress_category);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_category_books);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CategoryBookAdapter(book ->
                startActivity(BookDetailActivity.createBookDetailIntent(requireContext(), book)));
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.layout_category_search_bar)
                .setOnClickListener(v -> openSearchResults(""));

        loadBooks();
    }

    private void loadBooks() {
        new BookRepository(requireContext()).loadAllBooks(new BookRepository.BooksCallback() {
            @Override
            public void onLocalLoaded(List<Book> books) {
                if (!books.isEmpty()) {
                    showBooks(books);
                }
            }

            @Override
            public void onRemoteSynced(List<Book> books) {
                showBooks(books);
            }

            @Override
            public void onError(Exception e) {
                progress.setVisibility(View.GONE);
                if (allBooks.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Cannot sync categories. Showing offline data.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showBooks(List<Book> books) {
        allBooks.clear();
        allBooks.addAll(books);
        progress.setVisibility(View.GONE);
        buildCategoryChips();
        filterBooks(selectedCategory);
    }

    private void buildCategoryChips() {
        Set<String> categories = new LinkedHashSet<>();
        categories.add("All");
        for (Book book : allBooks) {
            for (String category : splitCategories(book.getCategory())) {
                categories.add(category);
            }
        }

        chipContainer.removeAllViews();
        boolean first = true;
        for (String category : categories) {
            int chipStyle = category.equals(selectedCategory)
                    ? R.style.FonosChipSelected
                    : R.style.FonosChipText;
            TextView chip = new TextView(requireContext(), null, 0, chipStyle);
            chip.setText(category);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            if (!first) {
                params.setMarginStart(dpToPx(10));
            }
            chip.setLayoutParams(params);
            chip.setOnClickListener(v -> {
                selectedCategory = category;
                buildCategoryChips();
                filterBooks(category);
            });
            chipContainer.addView(chip);
            first = false;
        }
    }

    private void filterBooks(String category) {
        List<Book> filtered = new ArrayList<>();
        if ("All".equals(category)) {
            filtered.addAll(allBooks);
            sectionTitle.setText("All audiobooks");
        } else {
            String target = category.toLowerCase(Locale.ROOT);
            for (Book book : allBooks) {
                for (String value : splitCategories(book.getCategory())) {
                    if (value.toLowerCase(Locale.ROOT).equals(target)) {
                        filtered.add(book);
                        break;
                    }
                }
            }
            sectionTitle.setText("Popular in " + category);
        }

        adapter.updateBooks(filtered);
        emptyText.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<String> splitCategories(String rawCategories) {
        List<String> categories = new ArrayList<>();
        if (rawCategories == null) return categories;

        for (String value : rawCategories.split(",")) {
            String category = value.trim();
            if (!category.isEmpty() && !"Audiobook".equalsIgnoreCase(category)) {
                categories.add(category);
            }
        }
        return categories;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void openSearchResults(String query) {
        Intent intent = new Intent(requireContext(), SearchResultActivity.class);
        intent.putExtra(SearchResultActivity.EXTRA_QUERY, query);
        startActivity(intent);
    }
}
