package com.example.fonosfinal.data.repository;

import com.example.fonosfinal.models.Chapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChapterRepository {

    public interface ChaptersCallback {
        void onLoaded(List<Chapter> chapters);
        void onError(Exception exception);
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void loadChapters(String bookId, ChaptersCallback callback) {
        if (bookId == null || bookId.trim().isEmpty()) {
            callback.onLoaded(new ArrayList<>());
            return;
        }

        firestore.collection("books")
                .document(bookId)
                .collection("chapters")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Chapter> chapters = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshot) {
                        Chapter chapter = mapChapter(document.getData());
                        if (chapter != null) {
                            chapters.add(chapter);
                        }
                    }
                    chapters.sort(Comparator.comparingInt(Chapter::getChapterIndex));
                    if (chapters.isEmpty()) {
                        loadTopLevelChapters(bookId, callback);
                    } else {
                        callback.onLoaded(chapters);
                    }
                })
                .addOnFailureListener(e -> loadTopLevelChapters(bookId, callback));
    }

    private void loadTopLevelChapters(String bookId, ChaptersCallback callback) {
        firestore.collection("chapters")
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Chapter> chapters = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshot) {
                        Chapter chapter = mapChapter(document.getData());
                        if (chapter != null) {
                            chapters.add(chapter);
                        }
                    }
                    chapters.sort(Comparator.comparingInt(Chapter::getChapterIndex));
                    callback.onLoaded(chapters);
                })
                .addOnFailureListener(callback::onError);
    }

    private Chapter mapChapter(Map<String, Object> data) {
        Boolean isActive = getBoolean(data.get("isActive"));
        if (Boolean.FALSE.equals(isActive)) {
            return null;
        }

        int chapterIndex = getInt(data.get("chapterIndex"), 0);
        String title = getString(data.get("title"));
        long durationMs = getLong(data.get("durationMs"), 0L);
        boolean isSample = Boolean.TRUE.equals(getBoolean(data.get("isSample")));

        return new Chapter(
                String.format(Locale.US, "%02d", chapterIndex),
                title == null || title.trim().isEmpty() ? "Untitled chapter" : title.trim(),
                formatDuration(durationMs),
                isSample ? "free" : "locked",
                chapterIndex,
                extractAudioUrl(data.get("audioUrl"))
        );
    }

    private String extractAudioUrl(Object value) {
        if (value instanceof String) {
            String url = ((String) value).trim();
            return url.isEmpty() ? null : url;
        }
        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            String[] preferredKeys = {"url", "downloadUrl", "audioUrl"};
            for (String key : preferredKeys) {
                String nestedUrl = extractAudioUrl(map.get(key));
                if (nestedUrl != null) return nestedUrl;
            }
            for (Object nestedValue : map.values()) {
                String nestedUrl = extractAudioUrl(nestedValue);
                if (nestedUrl != null && nestedUrl.startsWith("http")) return nestedUrl;
            }
        }
        return null;
    }

    private String getString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        return String.valueOf(value);
    }

    private Boolean getBoolean(Object value) {
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private int getInt(Object value, int fallback) {
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    private long getLong(Object value, long fallback) {
        return value instanceof Number ? ((Number) value).longValue() : fallback;
    }

    private String formatDuration(long durationMs) {
        if (durationMs <= 0) return "Unknown duration";

        long totalSeconds = durationMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.US, "%dh %02dm", hours, minutes);
        }
        if (minutes > 0) {
            return String.format(Locale.US, "%dm %02ds", minutes, seconds);
        }
        return String.format(Locale.US, "%ds", seconds);
    }
}
