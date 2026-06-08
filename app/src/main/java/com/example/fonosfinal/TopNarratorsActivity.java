package com.example.fonosfinal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fonosfinal.adapters.NarratorAdapter;
import com.example.fonosfinal.models.Narrator;

import java.util.ArrayList;
import java.util.List;

public class TopNarratorsActivity extends AppCompatActivity {

    public static final String EXTRA_NARRATOR_NAME = "extra_narrator_name";
    public static final String EXTRA_NARRATOR_SUBTITLE = "extra_narrator_subtitle";
    public static final String EXTRA_NARRATOR_RATING = "extra_narrator_rating";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_narrators);

        findViewById(R.id.button_top_narrators_back).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recycler_top_narrators);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new NarratorAdapter(createNarrators(), this::openNarratorProfile));
    }

    private void openNarratorProfile(Narrator narrator) {
        Intent intent = new Intent(this, NarratorProfileActivity.class);
        intent.putExtra(EXTRA_NARRATOR_NAME, narrator.getName());
        intent.putExtra(EXTRA_NARRATOR_SUBTITLE, narrator.getSubtitle());
        intent.putExtra(EXTRA_NARRATOR_RATING, narrator.getRating());
        startActivity(intent);
    }

    private List<Narrator> createNarrators() {
        List<Narrator> narrators = new ArrayList<>();
        narrators.add(new Narrator("John Smith", "24 audiobooks", "★ 4.9"));
        narrators.add(new Narrator("Alan Reed", "18 audiobooks", "★ 4.8"));
        narrators.add(new Narrator("Chris Hill", "21 audiobooks", "★ 4.8"));
        narrators.add(new Narrator("Daniel Lee", "16 audiobooks", "★ 4.7"));
        narrators.add(new Narrator("Sophia Brown", "29 audiobooks", "★ 4.9"));
        narrators.add(new Narrator("Michael Turner", "14 audiobooks", "★ 4.6"));
        return narrators;
    }
}
