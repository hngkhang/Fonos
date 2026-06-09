package com.example.fonosfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.R;
import com.example.fonosfinal.models.Book;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    private final List<Book> books;
    private final OnBookClickListener listener;

    public BookAdapter(List<Book> books) {
        this(books, null);
    }

    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books == null ? new ArrayList<>() : books;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());
        holder.metaTextView.setText(book.getDuration() + "  |  " + book.getRating());
        int fallbackCover = getCoverDrawable(book.getCoverType());
        holder.coverView.setBackgroundResource(fallbackCover);
        Glide.with(holder.coverView)
                .load(book.getCoverUrl())
                .placeholder(fallbackCover)
                .error(fallbackCover)
                .centerCrop()
                .into(holder.coverView);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {

        private final ImageView coverView;
        private final TextView titleTextView;
        private final TextView authorTextView;
        private final TextView metaTextView;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            coverView = itemView.findViewById(R.id.view_book_cover_placeholder);
            titleTextView = itemView.findViewById(R.id.text_book_title);
            authorTextView = itemView.findViewById(R.id.text_book_author);
            metaTextView = itemView.findViewById(R.id.text_book_meta);
        }
    }

    private int getCoverDrawable(int coverType) {
        if (coverType == 1) {
            return R.drawable.bg_cover_blue;
        } else if (coverType == 2) {
            return R.drawable.bg_cover_green;
        } else if (coverType == 3) {
            return R.drawable.bg_cover_orange;
        } else if (coverType == 4) {
            return R.drawable.bg_cover_purple;
        }
        return R.drawable.bg_book_placeholder;
    }
    public void updateBooks(List<Book> newBooks) {
        books.clear();
        if (newBooks != null) {
            books.addAll(newBooks);
        }
        notifyDataSetChanged();
    }
}
