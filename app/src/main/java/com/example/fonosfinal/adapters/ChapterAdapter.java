package com.example.fonosfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.R;
import com.example.fonosfinal.models.Chapter;

import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private final List<Chapter> chapters;

    public ChapterAdapter(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapters.get(position);
        holder.numberTextView.setText(chapter.getNumber());
        holder.titleTextView.setText(chapter.getTitle());
        holder.durationTextView.setText(chapter.getDuration());
        holder.statusTextView.setText(chapter.getStatus());
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {

        private final TextView numberTextView;
        private final TextView titleTextView;
        private final TextView durationTextView;
        private final TextView statusTextView;

        ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            numberTextView = itemView.findViewById(R.id.text_chapter_number);
            titleTextView = itemView.findViewById(R.id.text_chapter_title);
            durationTextView = itemView.findViewById(R.id.text_chapter_duration);
            statusTextView = itemView.findViewById(R.id.text_chapter_status);
        }
    }
}
