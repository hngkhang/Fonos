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

public class CategoryBookAdapter extends RecyclerView.Adapter<CategoryBookAdapter.BookViewHolder> {

    private final List<Book> books = new ArrayList<>();
    private final BookAdapter.OnBookClickListener listener;

    public CategoryBookAdapter(BookAdapter.OnBookClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        int fallbackCover = getCoverDrawable(book.getCoverType());

        holder.title.setText(book.getTitle());
        holder.author.setText(book.getAuthor());
        holder.meta.setText(book.getDuration() + "  |  " + book.getRating());
        holder.category.setText(book.getCategory());
        holder.cover.setBackgroundResource(fallbackCover);

        Glide.with(holder.cover)
                .load(book.getCoverUrl())
                .placeholder(fallbackCover)
                .error(fallbackCover)
                .centerCrop()
                .into(holder.cover);

        holder.itemView.setOnClickListener(v -> listener.onBookClick(book));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void updateBooks(List<Book> newBooks) {
        books.clear();
        if (newBooks != null) {
            books.addAll(newBooks);
        }
        notifyDataSetChanged();
    }

    private int getCoverDrawable(int coverType) {
        if (coverType == 1) return R.drawable.bg_cover_blue;
        if (coverType == 2) return R.drawable.bg_cover_green;
        if (coverType == 3) return R.drawable.bg_cover_orange;
        if (coverType == 4) return R.drawable.bg_cover_purple;
        return R.drawable.bg_book_placeholder;
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private final ImageView cover;
        private final TextView title;
        private final TextView author;
        private final TextView meta;
        private final TextView category;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.image_category_book_cover);
            title = itemView.findViewById(R.id.text_category_book_title);
            author = itemView.findViewById(R.id.text_category_book_author);
            meta = itemView.findViewById(R.id.text_category_book_meta);
            category = itemView.findViewById(R.id.text_category_book_category);
        }
    }
}
