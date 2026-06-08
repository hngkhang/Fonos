package com.example.fonosfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.R;
import com.example.fonosfinal.models.Narrator;

import java.util.List;

public class NarratorAdapter extends RecyclerView.Adapter<NarratorAdapter.NarratorViewHolder> {

    public interface OnNarratorClickListener {
        void onNarratorClick(Narrator narrator);
    }

    private final List<Narrator> narrators;
    private final OnNarratorClickListener listener;

    public NarratorAdapter(List<Narrator> narrators, OnNarratorClickListener listener) {
        this.narrators = narrators;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NarratorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_narrator, parent, false);
        return new NarratorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NarratorViewHolder holder, int position) {
        Narrator narrator = narrators.get(position);
        holder.nameTextView.setText(narrator.getName());
        holder.subtitleTextView.setText(narrator.getSubtitle());
        holder.ratingTextView.setText(narrator.getRating());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNarratorClick(narrator);
            }
        });
    }

    @Override
    public int getItemCount() {
        return narrators.size();
    }

    static class NarratorViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView;
        private final TextView subtitleTextView;
        private final TextView ratingTextView;

        NarratorViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_narrator_name);
            subtitleTextView = itemView.findViewById(R.id.text_narrator_subtitle);
            ratingTextView = itemView.findViewById(R.id.text_narrator_rating);
        }
    }
}
