package com.example.fonosfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.R;
import com.example.fonosfinal.models.Chapter;

import java.util.ArrayList;
import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    public interface OnChapterPlayClickListener {
        void onChapterPlayClick(Chapter chapter);
    }

    private final List<Chapter> chapters;
    private final OnChapterPlayClickListener playClickListener;

    public ChapterAdapter(List<Chapter> chapters) {
        this(chapters, null);
    }

    public ChapterAdapter(List<Chapter> chapters, OnChapterPlayClickListener playClickListener) {
        this.chapters = chapters == null ? new ArrayList<>() : chapters;
        this.playClickListener = playClickListener;
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
        holder.playImageView.setOnClickListener(v -> {
            if (playClickListener != null) {
                playClickListener.onChapterPlayClick(chapter);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public void updateChapters(List<Chapter> newChapters) {
        chapters.clear();
        if (newChapters != null) {
            chapters.addAll(newChapters);
        }
        notifyDataSetChanged();
    }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {

        private final TextView numberTextView;
        private final TextView titleTextView;
        private final TextView durationTextView;
        private final TextView statusTextView;
        private final ImageView playImageView;

        ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            numberTextView = itemView.findViewById(R.id.text_chapter_number);
            titleTextView = itemView.findViewById(R.id.text_chapter_title);
            durationTextView = itemView.findViewById(R.id.text_chapter_duration);
            statusTextView = itemView.findViewById(R.id.text_chapter_status);
            playImageView = itemView.findViewById(R.id.image_chapter_play);
        }
    }
}
