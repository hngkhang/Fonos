package com.example.fonosfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fonosfinal.R;
import com.example.fonosfinal.models.Book;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    public interface OnSearchResultClickListener {
        void onSearchResultClick(Book book);
    }

    private final List<Book> books;
    private final OnSearchResultClickListener listener;

    public SearchResultAdapter(List<Book> books, OnSearchResultClickListener listener) {
        this.books = books == null ? new ArrayList<>() : books;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        Book book = books.get(position);
        int fallbackCover = getCoverDrawable(book.getCoverType());
        holder.coverView.setBackgroundResource(fallbackCover);
        Glide.with(holder.coverView)
                .load(book.getCoverUrl())
                .placeholder(fallbackCover)
                .error(fallbackCover)
                .centerCrop()
                .into(holder.coverView);
        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());
        holder.narratorTextView.setText(book.getNarrator());
        holder.metaTextView.setText(book.getDuration() + "  |  " + book.getRating() + "  |  " + book.getCategory());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSearchResultClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void updateData(List<Book> newBooks) {
        books.clear();
        if (newBooks != null) {
            books.addAll(newBooks);
        }
        notifyDataSetChanged();
    }

    static class SearchResultViewHolder extends RecyclerView.ViewHolder {

        private final ImageView coverView;
        private final TextView titleTextView;
        private final TextView authorTextView;
        private final TextView narratorTextView;
        private final TextView metaTextView;

        SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            coverView = itemView.findViewById(R.id.view_search_result_cover);
            titleTextView = itemView.findViewById(R.id.text_search_result_title);
            authorTextView = itemView.findViewById(R.id.text_search_result_author);
            narratorTextView = itemView.findViewById(R.id.text_search_result_narrator);
            metaTextView = itemView.findViewById(R.id.text_search_result_meta);
        }
    }

    private int getCoverDrawable(int coverType) {
        if (coverType == 1) {
            return R.drawable.bg_cover_gradient_1;
        } else if (coverType == 2) {
            return R.drawable.bg_cover_gradient_2;
        } else if (coverType == 3) {
            return R.drawable.bg_cover_gradient_3;
        } else if (coverType == 4) {
            return R.drawable.bg_cover_gradient_4;
        }
        return R.drawable.bg_book_placeholder;
    }
}
