package com.example.fonosfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.R;
import com.example.fonosfinal.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.nameTextView.setText(review.getReviewerName());
        holder.ratingTextView.setText(review.getRating());
        holder.bodyTextView.setText(review.getBody());
        holder.timeTextView.setText(review.getTime());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView;
        private final TextView ratingTextView;
        private final TextView bodyTextView;
        private final TextView timeTextView;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_review_name);
            ratingTextView = itemView.findViewById(R.id.text_review_rating);
            bodyTextView = itemView.findViewById(R.id.text_review_body);
            timeTextView = itemView.findViewById(R.id.text_review_time);
        }
    }
}
