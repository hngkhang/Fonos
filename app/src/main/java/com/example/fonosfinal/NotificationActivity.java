package com.example.fonosfinal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.adapters.NotificationAdapter;
import com.example.fonosfinal.models.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        findViewById(R.id.button_notification_back).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new NotificationAdapter(createNotifications()));
    }

    private List<NotificationItem> createNotifications() {
        List<NotificationItem> notifications = new ArrayList<>();
        notifications.add(new NotificationItem("New audiobook available", "The Creative Act is now ready in your recommendations.", "10 min ago", true, R.drawable.ic_headphones));
        notifications.add(new NotificationItem("Continue listening", "Continue listening to Atomic Habits from chapter 3.", "1 hour ago", true, R.drawable.ic_play));
        notifications.add(new NotificationItem("Download completed", "Deep Work is available for offline listening.", "Yesterday", false, R.drawable.ic_download));
        notifications.add(new NotificationItem("New recommendation", "We found a new business audiobook for you.", "2 days ago", false, R.drawable.ic_notification));
        return notifications;
    }
}
