package com.example.fonosfinal;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.fonosfinal.data.local.DownloadLocalDao;
import com.example.fonosfinal.models.Chapter;
import com.example.fonosfinal.services.AudioPlaybackService;
import com.example.fonosfinal.models.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity
        implements AudioPlaybackService.PlaybackListener {

    public static final String EXTRA_CHAPTER_TITLE = "extra_chapter_title";
    public static final String EXTRA_CHAPTER_INDEX = "extra_chapter_index";
    public static final String EXTRA_CHAPTER_DURATION = "extra_chapter_duration";
    public static final String EXTRA_AUDIO_URL = "extra_audio_url";
    public static final String EXTRA_LOCAL_PATH = "extra_local_path";
    public static final String EXTRA_START_POSITION = "extra_start_position";
    public static final String EXTRA_FROM_NOTIFICATION = "extra_from_notification";
    public static final String EXTRA_CHAPTERS = "extra_chapters";
    private static final int REQUEST_NOTIFICATIONS = 100;

    private String title;
    private String author;
    private String narrator;
    private String duration;
    private String rating;
    private String category;
    private String bookId;
    private String coverUrl;
    private String description;
    private String chapterTitle;
    private String chapterDuration;
    private String audioUrl;
    private String localPath;
    private int chapterIndex;
    private int startPositionMs;
    private int coverType;
    private boolean shouldStartPlayback;
    private final ArrayList<Chapter> chapters = new ArrayList<>();

    private AudioPlaybackService playbackService;
    private boolean serviceBound;
    private ImageView playPauseButton;
    private SeekBar playbackSeekBar;
    private TextView currentTimeText;
    private TextView totalTimeText;
    private View loadingIndicator;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());

    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            progressHandler.postDelayed(this, 500);
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            AudioPlaybackService.LocalBinder localBinder =
                    (AudioPlaybackService.LocalBinder) binder;
            playbackService = localBinder.getService();
            serviceBound = true;
            playbackService.setPlaybackListener(PlayerActivity.this);
            ArrayList<Chapter> serviceChapters = playbackService.getChapters();
            if (!serviceChapters.isEmpty()) {
                chapters.clear();
                chapters.addAll(serviceChapters);
            }
            Chapter currentChapter = playbackService.getCurrentChapter();
            if (currentChapter != null) {
                applyChapter(currentChapter);
            }
            updatePlaybackButton(playbackService.isPlaying());
            loadingIndicator.setVisibility(
                    playbackService.isPreparing() ? View.VISIBLE : View.GONE);
            updateChapterNavigationState();
            updateProgress();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            playbackService = null;
            updatePlaybackButton(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        readBookData();
        shouldStartPlayback = savedInstanceState == null
                && !getIntent().getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
        bindViews();
        bindContent();
        setupControls();
        setupNavigation();
        requestNotificationPermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readBookData();
        bindContent();
        if (serviceBound && playbackService != null) {
            Chapter currentChapter = playbackService.getCurrentChapter();
            if (currentChapter != null) {
                applyChapter(currentChapter);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, AudioPlaybackService.class);
        if (shouldStartPlayback && audioUrl != null && !audioUrl.trim().isEmpty()) {
            serviceIntent.setAction(AudioPlaybackService.ACTION_PLAY_AUDIO);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                serviceIntent.putExtras(extras);
            }
            ContextCompat.startForegroundService(this, serviceIntent);
            shouldStartPlayback = false;
        }
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        progressHandler.post(progressUpdater);
    }

    @Override
    protected void onStop() {
        progressHandler.removeCallbacks(progressUpdater);
        saveListeningProgress();
        if (serviceBound) {
            playbackService.setPlaybackListener(null);
            unbindService(serviceConnection);
            serviceBound = false;
            playbackService = null;
        }
        super.onStop();
    }

    private void readBookData() {
        Intent intent = getIntent();
        title = intent.getStringExtra(BookDetailActivity.EXTRA_TITLE);
        author = intent.getStringExtra(BookDetailActivity.EXTRA_AUTHOR);
        narrator = intent.getStringExtra(BookDetailActivity.EXTRA_NARRATOR);
        duration = intent.getStringExtra(BookDetailActivity.EXTRA_DURATION);
        rating = intent.getStringExtra(BookDetailActivity.EXTRA_RATING);
        category = intent.getStringExtra(BookDetailActivity.EXTRA_CATEGORY);
        bookId = intent.getStringExtra(BookDetailActivity.EXTRA_BOOK_ID);
        coverUrl = intent.getStringExtra(BookDetailActivity.EXTRA_COVER_URL);
        description = intent.getStringExtra(BookDetailActivity.EXTRA_DESCRIPTION);
        coverType = intent.getIntExtra(BookDetailActivity.EXTRA_COVER_TYPE, 1);

        chapterTitle = intent.getStringExtra(EXTRA_CHAPTER_TITLE);
        chapterDuration = intent.getStringExtra(EXTRA_CHAPTER_DURATION);
        audioUrl = intent.getStringExtra(EXTRA_AUDIO_URL);
        localPath = intent.getStringExtra(EXTRA_LOCAL_PATH);
        startPositionMs = intent.getIntExtra(EXTRA_START_POSITION, 0);
        if ((audioUrl == null || audioUrl.trim().isEmpty())
                && localPath != null && !localPath.trim().isEmpty()) {
            audioUrl = localPath;
        }
        chapterIndex = intent.getIntExtra(EXTRA_CHAPTER_INDEX, 0);
        Object serializedChapters = intent.getSerializableExtra(EXTRA_CHAPTERS);
        chapters.clear();
        if (serializedChapters instanceof ArrayList<?>) {
            for (Object item : (ArrayList<?>) serializedChapters) {
                if (item instanceof Chapter) {
                    chapters.add((Chapter) item);
                }
            }
        }

        if (title == null) title = "Audiobook";
        if (author == null) author = "Unknown author";
        if (narrator == null) narrator = "Unknown narrator";
        if (duration == null) duration = "Unknown";
        if (rating == null) rating = "0";
        if (category == null) category = "Audiobook";
    }

    private void bindViews() {
        playPauseButton = findViewById(R.id.button_player_play_pause);
        playbackSeekBar = findViewById(R.id.seekbar_player_progress);
        currentTimeText = findViewById(R.id.text_player_current_time);
        totalTimeText = findViewById(R.id.text_player_total_time);
        loadingIndicator = findViewById(R.id.progress_player_loading);
    }

    private void bindContent() {
        String displayTitle = chapterTitle == null || chapterTitle.trim().isEmpty()
                ? title
                : chapterTitle;
        String subtitle = chapterTitle == null
                ? author + "  |  " + narrator
                : title + "  |  Chapter " + chapterIndex;

        ((TextView) findViewById(R.id.text_player_title)).setText(displayTitle);
        ((TextView) findViewById(R.id.text_player_subtitle)).setText(subtitle);
        totalTimeText.setText(chapterDuration == null ? "00:00" : chapterDuration);

        ImageView coverImage = findViewById(R.id.image_player_cover);
        int fallbackCover = getCoverDrawable(coverType);
        Glide.with(this)
                .load(coverUrl)
                .placeholder(fallbackCover)
                .error(fallbackCover)
                .centerCrop()
                .into(coverImage);
    }

    private void setupControls() {
        playPauseButton.setOnClickListener(v -> {
            if (!serviceBound || playbackService == null) return;
            if (audioUrl == null || audioUrl.trim().isEmpty()) {
                Toast.makeText(this,
                        "This chapter does not have an audio URL.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (playbackService.getCurrentAudioUrl() == null) {
                startForegroundPlayback();
            } else {
                playbackService.togglePlayPause();
            }
        });

        findViewById(R.id.button_player_rewind)
                .setOnClickListener(v -> skipBy(-15_000));
        findViewById(R.id.button_player_forward)
                .setOnClickListener(v -> skipBy(15_000));
        findViewById(R.id.button_player_previous).setOnClickListener(v -> {
            if (serviceBound && playbackService != null) {
                playbackService.playPrevious();
            }
        });
        findViewById(R.id.button_player_next).setOnClickListener(v -> {
            if (serviceBound && playbackService != null) {
                playbackService.playNext();
            }
        });
        findViewById(R.id.button_player_chapters)
                .setOnClickListener(v -> showChapterPicker());

        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && serviceBound && playbackService != null) {
                    currentTimeText.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (serviceBound && playbackService != null) {
                    playbackService.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    private void setupNavigation() {
        findViewById(R.id.button_player_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_back_to_details).setOnClickListener(v -> {
            Intent intent = new Intent(PlayerActivity.this, BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_TITLE, title);
            intent.putExtra(BookDetailActivity.EXTRA_AUTHOR, author);
            intent.putExtra(BookDetailActivity.EXTRA_NARRATOR, narrator);
            intent.putExtra(BookDetailActivity.EXTRA_DURATION, duration);
            intent.putExtra(BookDetailActivity.EXTRA_RATING, rating);
            intent.putExtra(BookDetailActivity.EXTRA_CATEGORY, category);
            intent.putExtra(BookDetailActivity.EXTRA_COVER_TYPE, coverType);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, bookId);
            intent.putExtra(BookDetailActivity.EXTRA_COVER_URL, coverUrl);
            intent.putExtra(BookDetailActivity.EXTRA_DESCRIPTION, description);
            startActivity(intent);
            finish();
        });
    }

    private void startForegroundPlayback() {
        Intent serviceIntent = new Intent(this, AudioPlaybackService.class)
                .setAction(AudioPlaybackService.ACTION_PLAY_AUDIO);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            serviceIntent.putExtras(extras);
        }
        ContextCompat.startForegroundService(this, serviceIntent);
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

    private void showChapterPicker() {
        ArrayList<Chapter> availableChapters = serviceBound && playbackService != null
                ? playbackService.getChapters()
                : new ArrayList<>();
        if (availableChapters.isEmpty()) {
            availableChapters.addAll(chapters);
        }
        if (availableChapters.isEmpty()) {
            Toast.makeText(this, "No chapters available.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] chapterLabels = new String[availableChapters.size()];
        for (int i = 0; i < availableChapters.size(); i++) {
            Chapter chapter = availableChapters.get(i);
            chapterLabels[i] = chapter.getNumber() + ". " + chapter.getTitle()
                    + "\n" + chapter.getDuration();
        }

        int selectedPosition = serviceBound && playbackService != null
                ? playbackService.getCurrentChapterPosition()
                : findChapterPosition(chapterIndex, availableChapters);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select chapter")
                .setSingleChoiceItems(chapterLabels, selectedPosition, null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getListView().setOnItemClickListener(
                (parent, view, position, id) -> {
                    Chapter selectedChapter = availableChapters.get(position);
                    if (selectedChapter.getAudioUrl() == null
                            || selectedChapter.getAudioUrl().trim().isEmpty()) {
                        Toast.makeText(this,
                                "This chapter does not have an audio URL.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (serviceBound && playbackService != null) {
                        playbackService.playChapterAt(position);
                    }
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private int findChapterPosition(int targetChapterIndex, ArrayList<Chapter> source) {
        for (int i = 0; i < source.size(); i++) {
            if (source.get(i).getChapterIndex() == targetChapterIndex) return i;
        }
        return -1;
    }

    private void skipBy(int offsetMs) {
        if (serviceBound && playbackService != null) {
            playbackService.skipBy(offsetMs);
            updateProgress();
        }
    }

    private void updateProgress() {
        if (!serviceBound || playbackService == null) return;

        int currentPosition = playbackService.getCurrentPosition();
        int totalDuration = playbackService.getDuration();
        playbackSeekBar.setMax(Math.max(totalDuration, 1));
        if (!playbackSeekBar.isPressed()) {
            playbackSeekBar.setProgress(currentPosition);
        }
        currentTimeText.setText(formatTime(currentPosition));
        if (totalDuration > 0) {
            totalTimeText.setText(formatTime(totalDuration));
        }
    }

    private String formatTime(int milliseconds) {
        int totalSeconds = Math.max(milliseconds, 0) / 1000;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updatePlaybackButton(boolean isPlaying) {
        playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void applyChapter(Chapter chapter) {
        chapterTitle = chapter.getTitle();
        chapterDuration = chapter.getDuration();
        audioUrl = chapter.getPlaybackUrl();
        localPath = chapter.getLocalPath();
        chapterIndex = chapter.getChapterIndex();
        getIntent().putExtra(EXTRA_CHAPTER_TITLE, chapterTitle);
        getIntent().putExtra(EXTRA_CHAPTER_DURATION, chapterDuration);
        getIntent().putExtra(EXTRA_AUDIO_URL, audioUrl);
        getIntent().putExtra(EXTRA_LOCAL_PATH, localPath);
        getIntent().putExtra(EXTRA_CHAPTER_INDEX, chapterIndex);
        bindContent();
        playbackSeekBar.setProgress(0);
        currentTimeText.setText("00:00");
        updateChapterNavigationState();
    }

    private void updateChapterNavigationState() {
        boolean hasPrevious = serviceBound && playbackService != null
                && playbackService.hasPreviousChapter();
        boolean hasNext = serviceBound && playbackService != null
                && playbackService.hasNextChapter();
        View previousButton = findViewById(R.id.button_player_previous);
        View nextButton = findViewById(R.id.button_player_next);
        previousButton.setEnabled(hasPrevious);
        previousButton.setAlpha(hasPrevious ? 1f : 0.4f);
        nextButton.setEnabled(hasNext);
        nextButton.setAlpha(hasNext ? 1f : 0.4f);
    }

    @Override
    public void onPreparing() {
        loadingIndicator.setVisibility(View.VISIBLE);
        playPauseButton.setEnabled(false);
    }

    @Override
    public void onReady() {
        loadingIndicator.setVisibility(View.GONE);
        playPauseButton.setEnabled(true);
        updateProgress();
    }

    @Override
    public void onPlaybackChanged(boolean isPlaying) {
        updatePlaybackButton(isPlaying);
    }

    @Override
    public void onChapterChanged(Chapter chapter, int position) {
        applyChapter(chapter);
    }

    @Override
    public void onCompleted() {
        updatePlaybackButton(false);
        updateProgress();
    }

    @Override
    public void onError(String message) {
        loadingIndicator.setVisibility(View.GONE);
        playPauseButton.setEnabled(true);
        updatePlaybackButton(false);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void saveListeningProgress() {
        if (!serviceBound || playbackService == null) return;

        Chapter currentChapter = playbackService.getCurrentChapter();
        int currentPosition = playbackService.getCurrentPosition();
        int totalDuration = playbackService.getDuration();
        if (currentChapter == null || currentPosition <= 0) return;

        new DownloadLocalDao(this).saveListeningProgress(
                getUserId(),
                createCurrentBook(),
                currentChapter,
                currentPosition,
                totalDuration
        );
    }

    private Book createCurrentBook() {
        Book book = new Book(title, author, duration, rating, coverType, category, narrator);
        book.setRemoteId(bookId);
        book.setCoverUrl(coverUrl);
        book.setDescription(description);
        return book;
    }

    private String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? "guest" : user.getUid();
    }

    private int getCoverDrawable(int type) {
        if (type == 1) return R.drawable.bg_cover_gradient_1;
        if (type == 2) return R.drawable.bg_cover_gradient_2;
        if (type == 3) return R.drawable.bg_cover_gradient_3;
        if (type == 4) return R.drawable.bg_cover_gradient_4;
        return R.drawable.bg_cover_gradient_1;
    }
}
