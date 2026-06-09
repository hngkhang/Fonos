package com.example.fonosfinal.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.fonosfinal.BookDetailActivity;
import com.example.fonosfinal.PlayerActivity;
import com.example.fonosfinal.R;
import com.example.fonosfinal.models.Chapter;

import java.io.IOException;
import java.util.ArrayList;

public class AudioPlaybackService extends Service {

    public static final String ACTION_PLAY_AUDIO =
            "com.example.fonosfinal.action.PLAY_AUDIO";
    private static final String ACTION_TOGGLE_PLAYBACK =
            "com.example.fonosfinal.action.TOGGLE_PLAYBACK";
    private static final String ACTION_STOP_PLAYBACK =
            "com.example.fonosfinal.action.STOP_PLAYBACK";
    private static final String ACTION_PREVIOUS =
            "com.example.fonosfinal.action.PREVIOUS";
    private static final String ACTION_NEXT =
            "com.example.fonosfinal.action.NEXT";
    private static final String TAG = "AudioPlaybackService";
    private static final String CHANNEL_ID = "audio_playback";
    private static final int NOTIFICATION_ID = 1001;

    public interface PlaybackListener {
        void onPreparing();
        void onReady();
        void onPlaybackChanged(boolean isPlaying);
        void onChapterChanged(Chapter chapter, int position);
        void onCompleted();
        void onError(String message);
    }

    public class LocalBinder extends Binder {
        public AudioPlaybackService getService() {
            return AudioPlaybackService.this;
        }
    }

    private final IBinder binder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private PlaybackListener playbackListener;
    private String currentAudioUrl;
    private boolean preparing;
    private boolean foreground;
    private Bundle playerExtras = new Bundle();
    private final ArrayList<Chapter> chapters = new ArrayList<>();
    private int currentChapterPosition = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if (ACTION_PLAY_AUDIO.equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                playerExtras = new Bundle(extras);
            }
            readPlaylist(intent);
            promoteToForeground();
            if (currentChapterPosition >= 0) {
                playChapterAt(currentChapterPosition);
            } else {
                play(intent.getStringExtra(PlayerActivity.EXTRA_AUDIO_URL));
            }
        } else if (ACTION_TOGGLE_PLAYBACK.equals(action)) {
            togglePlayPause();
        } else if (ACTION_STOP_PLAYBACK.equals(action)) {
            stopPlayback();
        } else if (ACTION_PREVIOUS.equals(action)) {
            playPrevious();
        } else if (ACTION_NEXT.equals(action)) {
            playNext();
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        playbackListener = null;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        releasePlayer();
        super.onDestroy();
    }

    public void setPlaybackListener(PlaybackListener listener) {
        playbackListener = listener;
    }

    public void play(String audioUrl) {
        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            notifyError("Chapter does not have an audio URL.");
            stopPlayback();
            return;
        }

        String normalizedUrl = audioUrl.trim();
        if (normalizedUrl.equals(currentAudioUrl) && mediaPlayer != null) {
            if (!preparing && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                notifyPlaybackChanged(true);
            }
            updateNotification();
            return;
        }

        releasePlayer();
        currentAudioUrl = normalizedUrl;
        preparing = true;
        notifyPreparing();
        updateNotification();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
        mediaPlayer.setOnPreparedListener(player -> {
            preparing = false;
            player.start();
            if (playbackListener != null) {
                playbackListener.onReady();
            }
            notifyPlaybackChanged(true);
        });
        mediaPlayer.setOnCompletionListener(player -> {
            notifyPlaybackChanged(false);
            if (hasNextChapter()) {
                playNext();
                return;
            }
            if (playbackListener != null) {
                playbackListener.onCompleted();
            }
        });
        mediaPlayer.setOnErrorListener((player, what, extra) -> {
            preparing = false;
            releasePlayer();
            notifyError("Unable to play this chapter.");
            stopPlayback();
            return true;
        });

        try {
            mediaPlayer.setDataSource(normalizedUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException | IllegalArgumentException | SecurityException exception) {
            preparing = false;
            releasePlayer();
            notifyError("Invalid or inaccessible audio URL.");
            stopPlayback();
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer == null || preparing) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notifyPlaybackChanged(false);
        } else {
            mediaPlayer.start();
            notifyPlaybackChanged(true);
        }
    }

    public void playPrevious() {
        int previousPosition = findPlayableChapter(currentChapterPosition - 1, -1);
        if (previousPosition >= 0) {
            playChapterAt(previousPosition);
        }
    }

    public void playNext() {
        int nextPosition = findPlayableChapter(currentChapterPosition + 1, 1);
        if (nextPosition >= 0) {
            playChapterAt(nextPosition);
        }
    }

    public void playChapterAt(int position) {
        if (position < 0 || position >= chapters.size()) return;

        Chapter chapter = chapters.get(position);
        if (chapter.getAudioUrl() == null || chapter.getAudioUrl().trim().isEmpty()) {
            notifyError("Selected chapter does not have an audio URL.");
            return;
        }

        currentChapterPosition = position;
        updateCurrentChapterExtras(chapter);
        notifyChapterChanged(chapter, position);
        play(chapter.getAudioUrl());
    }

    public ArrayList<Chapter> getChapters() {
        return new ArrayList<>(chapters);
    }

    public int getCurrentChapterPosition() {
        return currentChapterPosition;
    }

    public Chapter getCurrentChapter() {
        if (currentChapterPosition < 0 || currentChapterPosition >= chapters.size()) return null;
        return chapters.get(currentChapterPosition);
    }

    public boolean hasPreviousChapter() {
        return findPlayableChapter(currentChapterPosition - 1, -1) >= 0;
    }

    public boolean hasNextChapter() {
        return findPlayableChapter(currentChapterPosition + 1, 1) >= 0;
    }

    public void seekTo(int positionMs) {
        if (mediaPlayer == null || preparing) return;
        int safePosition = Math.max(0, Math.min(positionMs, getDuration()));
        mediaPlayer.seekTo(safePosition);
    }

    public void skipBy(int offsetMs) {
        seekTo(getCurrentPosition() + offsetMs);
    }

    public boolean isPlaying() {
        return mediaPlayer != null && !preparing && mediaPlayer.isPlaying();
    }

    public boolean isPreparing() {
        return preparing;
    }

    public int getCurrentPosition() {
        if (mediaPlayer == null || preparing) return 0;
        try {
            return mediaPlayer.getCurrentPosition();
        } catch (IllegalStateException ignored) {
            return 0;
        }
    }

    public int getDuration() {
        if (mediaPlayer == null || preparing) return 0;
        try {
            return mediaPlayer.getDuration();
        } catch (IllegalStateException ignored) {
            return 0;
        }
    }

    public String getCurrentAudioUrl() {
        return currentAudioUrl;
    }

    private void promoteToForeground() {
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
        foreground = true;
    }

    private void updateNotification() {
        if (!foreground) return;
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, buildNotification());
    }

    private Notification buildNotification() {
        boolean playing = isPlaying();
        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent playerIntent = new Intent(this, PlayerActivity.class);
        playerIntent.putExtras(playerExtras);
        playerIntent.putExtra(PlayerActivity.EXTRA_FROM_NOTIFICATION, true);
        playerIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, playerIntent, pendingIntentFlags);

        Intent toggleIntent = new Intent(this, AudioPlaybackService.class)
                .setAction(ACTION_TOGGLE_PLAYBACK);
        PendingIntent togglePendingIntent = PendingIntent.getService(
                this, 1, toggleIntent, pendingIntentFlags);

        Intent stopIntent = new Intent(this, AudioPlaybackService.class)
                .setAction(ACTION_STOP_PLAYBACK);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 2, stopIntent, pendingIntentFlags);

        Intent previousIntent = new Intent(this, AudioPlaybackService.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent previousPendingIntent = PendingIntent.getService(
                this, 3, previousIntent, pendingIntentFlags);

        Intent nextIntent = new Intent(this, AudioPlaybackService.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(
                this, 4, nextIntent, pendingIntentFlags);

        String chapterTitle = playerExtras.getString(PlayerActivity.EXTRA_CHAPTER_TITLE);
        String bookTitle = playerExtras.getString(BookDetailActivity.EXTRA_TITLE);
        if (chapterTitle == null || chapterTitle.trim().isEmpty()) {
            chapterTitle = bookTitle == null ? "Fonos audio" : bookTitle;
        }
        if (bookTitle == null || bookTitle.trim().isEmpty()) {
            bookTitle = preparing ? "Loading audio..." : "Audiobook";
        }

        Notification.Action toggleAction = new Notification.Action.Builder(
                playing ? R.drawable.ic_pause : R.drawable.ic_play,
                playing ? "Pause" : "Play",
                togglePendingIntent
        ).build();
        Notification.Action previousAction = new Notification.Action.Builder(
                R.drawable.ic_skip_previous,
                "Previous",
                previousPendingIntent
        ).build();
        Notification.Action nextAction = new Notification.Action.Builder(
                R.drawable.ic_skip_next,
                "Next",
                nextPendingIntent
        ).build();
        Notification.Action stopAction = new Notification.Action.Builder(
                R.drawable.ic_stop,
                "Stop",
                stopPendingIntent
        ).build();

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        return builder
                .setSmallIcon(R.drawable.ic_headphones)
                .setContentTitle(chapterTitle)
                .setContentText(bookTitle)
                .setContentIntent(contentIntent)
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setOngoing(playing || preparing)
                .addAction(previousAction)
                .addAction(toggleAction)
                .addAction(nextAction)
                .addAction(stopAction)
                .setStyle(new Notification.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Audio playback",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Controls for audiobook playback");
        channel.setSound(null, null);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private void stopPlayback() {
        releasePlayer();
        if (foreground) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            foreground = false;
        }
        stopSelf();
        notifyPlaybackChanged(false);
    }

    private void releasePlayer() {
        preparing = false;
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentAudioUrl = null;
    }

    private void notifyPreparing() {
        if (playbackListener != null) {
            playbackListener.onPreparing();
        }
    }

    private void notifyPlaybackChanged(boolean isPlaying) {
        updateNotification();
        if (playbackListener != null) {
            playbackListener.onPlaybackChanged(isPlaying);
        }
    }

    private void notifyError(String message) {
        if (playbackListener != null) {
            playbackListener.onError(message);
        }
    }

    @SuppressWarnings("unchecked")
    private void readPlaylist(Intent intent) {
        Object value = intent.getSerializableExtra(PlayerActivity.EXTRA_CHAPTERS);
        chapters.clear();
        if (value instanceof ArrayList<?>) {
            for (Object item : (ArrayList<?>) value) {
                if (item instanceof Chapter) {
                    chapters.add((Chapter) item);
                }
            }
        }

        int requestedIndex = intent.getIntExtra(PlayerActivity.EXTRA_CHAPTER_INDEX, -1);
        currentChapterPosition = findChapterPosition(requestedIndex);
    }

    private int findChapterPosition(int chapterIndex) {
        for (int position = 0; position < chapters.size(); position++) {
            if (chapters.get(position).getChapterIndex() == chapterIndex) {
                return position;
            }
        }
        return chapters.isEmpty() ? -1 : 0;
    }

    private int findPlayableChapter(int startPosition, int direction) {
        for (int position = startPosition;
             position >= 0 && position < chapters.size();
             position += direction) {
            String url = chapters.get(position).getAudioUrl();
            if (url != null && !url.trim().isEmpty()) {
                return position;
            }
        }
        return -1;
    }

    private void updateCurrentChapterExtras(Chapter chapter) {
        playerExtras.putString(PlayerActivity.EXTRA_CHAPTER_TITLE, chapter.getTitle());
        playerExtras.putInt(PlayerActivity.EXTRA_CHAPTER_INDEX, chapter.getChapterIndex());
        playerExtras.putString(PlayerActivity.EXTRA_CHAPTER_DURATION, chapter.getDuration());
        playerExtras.putString(PlayerActivity.EXTRA_AUDIO_URL, chapter.getAudioUrl());
        playerExtras.putSerializable(PlayerActivity.EXTRA_CHAPTERS, chapters);
    }

    private void notifyChapterChanged(Chapter chapter, int position) {
        updateNotification();
        if (playbackListener != null) {
            playbackListener.onChapterChanged(chapter, position);
        }
    }
}
