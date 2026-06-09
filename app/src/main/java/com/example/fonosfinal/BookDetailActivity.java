package com.example.fonosfinal;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fonosfinal.adapters.ChapterAdapter;
import com.example.fonosfinal.adapters.ReviewAdapter;
import com.example.fonosfinal.data.local.DownloadLocalDao;
import com.example.fonosfinal.data.local.DownloadStatus;
import com.example.fonosfinal.data.repository.ChapterRepository;
import com.example.fonosfinal.models.Book;
import com.example.fonosfinal.models.Chapter;
import com.example.fonosfinal.models.DownloadedChapter;
import com.example.fonosfinal.models.Review;
import com.example.fonosfinal.services.AudioDownloadService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_AUTHOR = "extra_author";
    public static final String EXTRA_NARRATOR = "extra_narrator";
    public static final String EXTRA_DURATION = "extra_duration";
    public static final String EXTRA_RATING = "extra_rating";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_COVER_TYPE = "extra_cover_type";
    public static final String EXTRA_COVER_URL = "extra_cover_url";
    public static final String EXTRA_BOOK_ID = "extra_book_id";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    private static final int REQUEST_NOTIFICATIONS = 101;

    private String title;
    private String author;
    private String narrator;
    private String duration;
    private String rating;
    private String category;
    private int coverType;
    private String coverUrl;
    private String bookId;
    private String description;
    private ChapterAdapter chapterAdapter;
    private final ArrayList<Chapter> chapters = new ArrayList<>();
    private DownloadLocalDao downloadLocalDao;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        readBookData();
        downloadLocalDao = new DownloadLocalDao(this);
        userId = getUserId();
        bindHeader();
        setupChapters();
        setupReviews();
        setupNavigation();
        requestNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (chapterAdapter != null && !chapters.isEmpty()) {
            List<Chapter> updatedChapters = applyDownloadState(chapters);
            chapters.clear();
            chapters.addAll(updatedChapters);
            chapterAdapter.updateChapters(chapters);
            updateDownloadButtonState();
        }
        updateLovedIcon();
    }

    private void readBookData() {
        Intent intent = getIntent();
        title = intent.getStringExtra(EXTRA_TITLE);
        author = intent.getStringExtra(EXTRA_AUTHOR);
        narrator = intent.getStringExtra(EXTRA_NARRATOR);
        duration = intent.getStringExtra(EXTRA_DURATION);
        rating = intent.getStringExtra(EXTRA_RATING);
        category = intent.getStringExtra(EXTRA_CATEGORY);
        coverType = intent.getIntExtra(EXTRA_COVER_TYPE, 1);
        coverUrl = intent.getStringExtra(EXTRA_COVER_URL);
        bookId = intent.getStringExtra(EXTRA_BOOK_ID);
        description = intent.getStringExtra(EXTRA_DESCRIPTION);

        if (title == null) {
            title = "Atomic Habits";
        }
        if (author == null) {
            author = "James Clear";
        }
        if (narrator == null) {
            narrator = getNarratorForTitle(title);
        }
        if (duration == null) {
            duration = "5h 20m";
        }
        if (rating == null) {
            rating = "4.9";
        }
        if (category == null) {
            category = "Self-help";
        }
    }

    private void bindHeader() {
        ImageView coverView = findViewById(R.id.view_detail_cover);
        int fallbackCover = getCoverDrawable(coverType);
        coverView.setBackgroundResource(fallbackCover);
        Glide.with(this)
                .load(coverUrl)
                .placeholder(fallbackCover)
                .error(fallbackCover)
                .centerCrop()
                .into(coverView);
        findViewById(R.id.image_detail_headphones).setVisibility(
                coverUrl == null || coverUrl.trim().isEmpty() ? View.VISIBLE : View.GONE);

        ((TextView) findViewById(R.id.text_detail_title)).setText(title);
        ((TextView) findViewById(R.id.text_detail_author)).setText(author);
        ((TextView) findViewById(R.id.text_detail_narrator)).setText(narrator);
        ((TextView) findViewById(R.id.text_detail_rating)).setText("★ " + rating);
        ((TextView) findViewById(R.id.text_detail_duration)).setText(duration);
        ((TextView) findViewById(R.id.text_detail_category)).setText(category);
        ((TextView) findViewById(R.id.text_detail_about)).setText(
                description == null || description.trim().isEmpty()
                        ? "No description available."
                        : description.trim());
        ((TextView) findViewById(R.id.text_detail_metadata)).setText(
                "Duration: " + duration + "\nLanguage: English\nCategory: " + category + "\nRelease year: 2024");
        ((TextView) findViewById(R.id.text_rating_summary)).setText(rating + " average rating from audiobook listeners");
    }

    private void setupChapters() {
        RecyclerView recyclerView = findViewById(R.id.recycler_chapters);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chapterAdapter = new ChapterAdapter(new ArrayList<>(), this::openChapterPlayer);
        recyclerView.setAdapter(chapterAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        loadChapters();
    }

    private void loadChapters() {
        View progress = findViewById(R.id.progress_chapters);
        TextView emptyText = findViewById(R.id.text_chapters_empty);
        progress.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        new ChapterRepository().loadChapters(bookId, new ChapterRepository.ChaptersCallback() {
            @Override
            public void onLoaded(List<Chapter> loadedChapters) {
                progress.setVisibility(View.GONE);
                chapters.clear();
                chapters.addAll(applyDownloadState(loadedChapters));
                chapterAdapter.updateChapters(chapters);
                updateDownloadButtonState();
                emptyText.setText(bookId == null
                        ? "This book has no Firestore ID, so its chapters cannot be loaded."
                        : "No chapters found for this audiobook.");
                emptyText.setVisibility(loadedChapters.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception exception) {
                progress.setVisibility(View.GONE);
                emptyText.setText("Unable to load chapters.");
                emptyText.setVisibility(View.VISIBLE);
                Toast.makeText(BookDetailActivity.this,
                        "Cannot load chapters from Firestore.",
                        Toast.LENGTH_SHORT).show();
                updateDownloadButtonState();
            }
        });
    }

    private void setupReviews() {
        RecyclerView recyclerView = findViewById(R.id.recycler_reviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ReviewAdapter(createReviews()));
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupNavigation() {
        findViewById(R.id.button_book_detail_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_play_audiobook).setOnClickListener(v -> openPlayer());
        findViewById(R.id.button_book_detail_favorite).setOnClickListener(v -> toggleLoved());
        findViewById(R.id.button_download_audiobook).setOnClickListener(v -> showDownloadOptions());
        findViewById(R.id.button_add_review).setOnClickListener(v -> {
            Intent intent = createBookIntent(AddReviewActivity.class);
            startActivity(intent);
        });
        updateLovedIcon();
        updateDownloadButtonState();
    }

    private void openPlayer() {
        for (Chapter chapter : chapters) {
            if (chapter.getAudioUrl() != null && !chapter.getAudioUrl().trim().isEmpty()) {
                openChapterPlayer(chapter);
                return;
            }
        }
        Intent intent = createBookIntent(PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_CHAPTERS, chapters);
        startActivity(intent);
    }

    private void openChapterPlayer(Chapter chapter) {
        Intent intent = createBookIntent(PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_CHAPTER_TITLE, chapter.getTitle());
        intent.putExtra(PlayerActivity.EXTRA_CHAPTER_INDEX, chapter.getChapterIndex());
        intent.putExtra(PlayerActivity.EXTRA_CHAPTER_DURATION, chapter.getDuration());
        intent.putExtra(PlayerActivity.EXTRA_AUDIO_URL, chapter.getPlaybackUrl());
        intent.putExtra(PlayerActivity.EXTRA_LOCAL_PATH, chapter.getLocalPath());
        intent.putExtra(PlayerActivity.EXTRA_CHAPTERS, chapters);
        startActivity(intent);
    }

    private List<Chapter> applyDownloadState(List<Chapter> loadedChapters) {
        List<Chapter> updatedChapters = new ArrayList<>();
        if (loadedChapters == null) return updatedChapters;

        Map<String, DownloadedChapter> downloads =
                downloadLocalDao.getChapterDownloads(userId, bookId);
        for (Chapter chapter : loadedChapters) {
            String chapterId = DownloadLocalDao.resolveChapterId(chapter);
            DownloadedChapter downloaded = downloads.get(chapterId);
            String status = chapter.getStatus();
            String localPath = null;

            if (downloaded != null) {
                if (DownloadStatus.COMPLETED.equals(downloaded.getStatus())) {
                    status = "downloaded";
                    localPath = downloaded.getLocalPath();
                } else if (DownloadStatus.DOWNLOADING.equals(downloaded.getStatus())) {
                    status = "downloading";
                } else if (DownloadStatus.FAILED.equals(downloaded.getStatus())) {
                    status = "failed";
                }
            }

            updatedChapters.add(new Chapter(
                    chapter.getBookId(),
                    chapter.getChapterId(),
                    chapter.getNumber(),
                    chapter.getTitle(),
                    chapter.getDuration(),
                    status,
                    chapter.getChapterIndex(),
                    chapter.getAudioUrl(),
                    localPath
            ));
        }
        return updatedChapters;
    }

    private void showDownloadOptions() {
        if (bookId == null || bookId.trim().isEmpty()) {
            Toast.makeText(this, "This book cannot be downloaded because it has no Firestore ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (chapters.isEmpty()) {
            Toast.makeText(this, "No chapters available to download.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (downloadLocalDao.isBookFullyDownloaded(userId, bookId, chapters.size())) {
            Toast.makeText(this, "This book is already fully downloaded.", Toast.LENGTH_SHORT).show();
            updateDownloadButtonState();
            return;
        }

        ArrayList<Chapter> candidates = getDownloadCandidates();
        if (candidates.isEmpty()) {
            Toast.makeText(this, "No downloadable chapters available right now.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Download")
                .setItems(new String[]{"Download whole book", "Select chapters"}, (dialog, which) -> {
                    if (which == 0) {
                        startDownload(candidates, true);
                    } else {
                        showChapterSelection(candidates);
                    }
                })
                .show();
    }

    private void showChapterSelection(ArrayList<Chapter> candidates) {
        String[] labels = new String[candidates.size()];
        boolean[] checked = new boolean[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            Chapter chapter = candidates.get(i);
            labels[i] = chapter.getNumber() + ". " + chapter.getTitle() + "\n" + chapter.getDuration();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select chapters")
                .setMultiChoiceItems(labels, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Download", (dialog, which) -> {
                    ArrayList<Chapter> selected = new ArrayList<>();
                    for (int i = 0; i < candidates.size(); i++) {
                        if (checked[i]) {
                            selected.add(candidates.get(i));
                        }
                    }
                    if (selected.isEmpty()) {
                        Toast.makeText(this, "Please select at least one chapter.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startDownload(selected, false);
                })
                .show();
    }

    private ArrayList<Chapter> getDownloadCandidates() {
        ArrayList<Chapter> candidates = new ArrayList<>();
        for (Chapter chapter : chapters) {
            String chapterId = DownloadLocalDao.resolveChapterId(chapter);
            if (chapter.getAudioUrl() == null || chapter.getAudioUrl().trim().isEmpty()) {
                continue;
            }
            if (downloadLocalDao.isChapterCompleted(userId, bookId, chapterId)
                    || downloadLocalDao.isChapterDownloading(userId, bookId, chapterId)) {
                continue;
            }
            candidates.add(chapter);
        }
        return candidates;
    }

    private void startDownload(ArrayList<Chapter> selectedChapters, boolean fullDownload) {
        Intent intent = createBookIntent(AudioDownloadService.class);
        intent.setAction(AudioDownloadService.ACTION_DOWNLOAD_CHAPTERS);
        intent.putExtra(AudioDownloadService.EXTRA_CHAPTERS, selectedChapters);
        intent.putExtra(AudioDownloadService.EXTRA_FULL_DOWNLOAD, fullDownload);
        intent.putExtra(AudioDownloadService.EXTRA_TOTAL_CHAPTER_COUNT, chapters.size());
        ContextCompat.startForegroundService(this, intent);
        Toast.makeText(this, "Download started.", Toast.LENGTH_SHORT).show();
    }

    private void updateDownloadButtonState() {
        Button downloadButton = findViewById(R.id.button_download_audiobook);
        if (downloadButton == null) return;

        boolean fullyDownloaded = bookId != null
                && downloadLocalDao != null
                && downloadLocalDao.isBookFullyDownloaded(userId, bookId, chapters.size());
        downloadButton.setEnabled(!fullyDownloaded && !chapters.isEmpty());
        downloadButton.setAlpha(downloadButton.isEnabled() ? 1f : 0.55f);
        downloadButton.setText(fullyDownloaded ? "Downloaded" : "Download");
    }

    private void toggleLoved() {
        Book book = createCurrentBook();
        String resolvedBookId = DownloadLocalDao.resolveBookId(book);
        boolean loved = downloadLocalDao.isBookLoved(userId, resolvedBookId);
        downloadLocalDao.setBookLoved(userId, book, !loved);
        updateLovedIcon();
        Toast.makeText(this, !loved ? "Added to Loved." : "Removed from Loved.", Toast.LENGTH_SHORT).show();
    }

    private void updateLovedIcon() {
        ImageView favorite = findViewById(R.id.button_book_detail_favorite);
        if (favorite == null || downloadLocalDao == null) return;

        boolean loved = downloadLocalDao.isBookLoved(userId, DownloadLocalDao.resolveBookId(createCurrentBook()));
        favorite.setColorFilter(ContextCompat.getColor(
                this,
                loved ? R.color.warning_optional : R.color.text_secondary
        ));
    }

    private Book createCurrentBook() {
        Book book = new Book(title, author, duration, rating, coverType, category, narrator);
        book.setRemoteId(bookId);
        book.setCoverUrl(coverUrl);
        book.setDescription(description);
        return book;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATIONS
            );
        }
    }

    private String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? "guest" : user.getUid();
    }

    private Intent createBookIntent(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_AUTHOR, author);
        intent.putExtra(EXTRA_NARRATOR, narrator);
        intent.putExtra(EXTRA_DURATION, duration);
        intent.putExtra(EXTRA_RATING, rating);
        intent.putExtra(EXTRA_CATEGORY, category);
        intent.putExtra(EXTRA_COVER_TYPE, coverType);
        intent.putExtra(EXTRA_COVER_URL, coverUrl);
        intent.putExtra(EXTRA_BOOK_ID, bookId);
        intent.putExtra(EXTRA_DESCRIPTION, description);
        return intent;
    }

    public static Intent createBookDetailIntent(android.content.Context context, Book book) {
        Intent intent = new Intent(context, BookDetailActivity.class);
        intent.putExtra(EXTRA_TITLE, book.getTitle());
        intent.putExtra(EXTRA_AUTHOR, book.getAuthor());
        intent.putExtra(EXTRA_NARRATOR, book.getNarrator());
        intent.putExtra(EXTRA_DURATION, book.getDuration());
        intent.putExtra(EXTRA_RATING, book.getRating());
        intent.putExtra(EXTRA_CATEGORY, book.getCategory());
        intent.putExtra(EXTRA_COVER_TYPE, book.getCoverType());
        intent.putExtra(EXTRA_COVER_URL, book.getCoverUrl());
        intent.putExtra(EXTRA_BOOK_ID, book.getRemoteId());
        intent.putExtra(EXTRA_DESCRIPTION, book.getDescription());
        return intent;
    }

    private List<Review> createReviews() {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("Minh Anh", "★★★★★", "Clear narration and short chapters make it easy to continue during commutes.", "2 days ago"));
        reviews.add(new Review("David Nguyen", "★★★★☆", "A polished audiobook experience with practical ideas and strong pacing.", "1 week ago"));
        return reviews;
    }

    private String getNarratorForTitle(String bookTitle) {
        if ("Atomic Habits".equals(bookTitle)) {
            return "Narrated by John Smith";
        } else if ("Deep Work".equals(bookTitle)) {
            return "Narrated by Alan Reed";
        } else if ("The Psychology of Money".equals(bookTitle)) {
            return "Narrated by Chris Hill";
        } else if ("Ikigai".equals(bookTitle)) {
            return "Narrated by Daniel Lee";
        } else if ("The Alchemist".equals(bookTitle)) {
            return "Narrated by Mark Bramhall";
        }
        return "Narrated by Fonos Studio";
    }

    private int getCoverDrawable(int type) {
        if (type == 1) {
            return R.drawable.bg_cover_gradient_1;
        } else if (type == 2) {
            return R.drawable.bg_cover_gradient_2;
        } else if (type == 3) {
            return R.drawable.bg_cover_gradient_3;
        } else if (type == 4) {
            return R.drawable.bg_cover_gradient_4;
        }
        return R.drawable.bg_cover_gradient_1;
    }
}
