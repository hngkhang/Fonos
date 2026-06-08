package com.example.fonosfinal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.R;
import com.example.fonosfinal.models.NotificationItem;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<NotificationItem> notifications;

    public NotificationAdapter(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notifications.get(position);
        holder.iconImageView.setImageResource(item.getIconResId());
        holder.titleTextView.setText(item.getTitle());
        holder.descriptionTextView.setText(item.getDescription());
        holder.timeTextView.setText(item.getTime());
        holder.badgeTextView.setVisibility(item.isNew() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iconImageView;
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView timeTextView;
        private final TextView badgeTextView;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.image_notification_icon);
            titleTextView = itemView.findViewById(R.id.text_notification_title);
            descriptionTextView = itemView.findViewById(R.id.text_notification_description);
            timeTextView = itemView.findViewById(R.id.text_notification_time);
            badgeTextView = itemView.findViewById(R.id.text_notification_badge);
        }
    }
}
