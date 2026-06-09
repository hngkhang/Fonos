package com.example.fonosfinal.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.fonosfinal.BookDetailActivity;
import com.example.fonosfinal.MainActivity;
import com.example.fonosfinal.R;
import com.example.fonosfinal.data.local.DownloadLocalDao;
import com.example.fonosfinal.models.Book;
import com.example.fonosfinal.models.Chapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioDownloadService extends Service {

    public static final String ACTION_DOWNLOAD_CHAPTERS =
            "com.example.fonosfinal.action.DOWNLOAD_CHAPTERS";
    public static final String EXTRA_CHAPTERS = "extra_download_chapters";
    public static final String EXTRA_FULL_DOWNLOAD = "extra_full_download";
    public static final String EXTRA_TOTAL_CHAPTER_COUNT = "extra_total_chapter_count";

    private static final String CHANNEL_ID = "audio_downloads";
    private static final int NOTIFICATION_ID = 2001;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || !ACTION_DOWNLOAD_CHAPTERS.equals(intent.getAction())) {
            return START_NOT_STICKY;
        }

        startForeground(NOTIFICATION_ID, buildNotification());

        Book book = readBook(intent);
        ArrayList<Chapter> chapters = readChapters(intent);
        boolean fullDownload = intent.getBooleanExtra(EXTRA_FULL_DOWNLOAD, false);
        int totalChapterCount = intent.getIntExtra(EXTRA_TOTAL_CHAPTER_COUNT, chapters.size());
        String userId = getUserId();

        executor.execute(() -> {
            try {
                downloadChapters(userId, book, chapters, totalChapterCount, fullDownload);
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE);
                stopSelf(startId);
            }
        });

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void downloadChapters(String userId, Book book, ArrayList<Chapter> chapters,
                                  int totalChapterCount, boolean fullDownload) {
        DownloadLocalDao dao = new DownloadLocalDao(this);
        String bookId = DownloadLocalDao.resolveBookId(book);

        for (Chapter chapter : chapters) {
            String chapterId = DownloadLocalDao.resolveChapterId(chapter);
            if (dao.isChapterCompleted(userId, bookId, chapterId)
                    || dao.isChapterDownloading(userId, bookId, chapterId)) {
                continue;
            }

            dao.markChapterDownloading(userId, book, chapter, totalChapterCount, fullDownload);

            File tempFile = null;
            File finalFile = null;
            try {
                if (!isNetworkAvailable()) {
                    throw new IllegalStateException("Network is unavailable");
                }
                finalFile = getChapterFile(userId, bookId, chapter);
                tempFile = new File(finalFile.getAbsolutePath() + ".download");
                downloadToFile(chapter.getAudioUrl(), tempFile);
                if (finalFile.exists() && !finalFile.delete()) {
                    throw new IllegalStateException("Cannot replace old audio file");
                }
                if (!tempFile.renameTo(finalFile)) {
                    throw new IllegalStateException("Cannot finalize audio file");
                }
                dao.markChapterCompleted(
                        userId,
                        book,
                        chapter,
                        finalFile.getAbsolutePath(),
                        finalFile.length(),
                        totalChapterCount,
                        fullDownload
                );
            } catch (Exception exception) {
                deleteIfExists(tempFile);
                deleteIfExists(finalFile);
                dao.markChapterFailed(userId, book, chapter, totalChapterCount, fullDownload);
            }
        }
    }

    private void downloadToFile(String audioUrl, File destination) throws Exception {
        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Audio URL is empty");
        }

        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Cannot create download folder");
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(audioUrl.trim()).openConnection();
        connection.setConnectTimeout(15_000);
        connection.setReadTimeout(20_000);
        connection.setInstanceFollowRedirects(true);

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IllegalStateException("HTTP " + responseCode);
            }

            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(destination, false)) {
                byte[] buffer = new byte[16 * 1024];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Download interrupted");
                    }
                    output.write(buffer, 0, read);
                }
                output.flush();
            }
        } finally {
            connection.disconnect();
        }
    }

    private File getChapterFile(String userId, String bookId, Chapter chapter) {
        File root = getExternalFilesDir(null);
        if (root == null) {
            root = getFilesDir();
        }
        File folder = new File(root,
                "audiobooks/" + sanitize(userId) + "/" + sanitize(bookId));
        String chapterName = sanitize(DownloadLocalDao.resolveChapterId(chapter));
        return new File(folder, chapterName + ".mp3");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return false;

        Network network = manager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private Notification buildNotification() {
        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, intent, pendingIntentFlags);

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        return builder
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle("Downloading...")
                .setContentText("Saving audiobook chapters for offline listening")
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Audio downloads",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Audiobook download status");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private Book readBook(Intent intent) {
        Book book = new Book();
        book.setRemoteId(intent.getStringExtra(BookDetailActivity.EXTRA_BOOK_ID));
        book.setTitle(intent.getStringExtra(BookDetailActivity.EXTRA_TITLE));
        book.setAuthor(intent.getStringExtra(BookDetailActivity.EXTRA_AUTHOR));
        book.setNarrator(intent.getStringExtra(BookDetailActivity.EXTRA_NARRATOR));
        book.setDuration(intent.getStringExtra(BookDetailActivity.EXTRA_DURATION));
        book.setRating(intent.getStringExtra(BookDetailActivity.EXTRA_RATING));
        book.setCategory(intent.getStringExtra(BookDetailActivity.EXTRA_CATEGORY));
        book.setCoverType(intent.getIntExtra(BookDetailActivity.EXTRA_COVER_TYPE, 1));
        book.setCoverUrl(intent.getStringExtra(BookDetailActivity.EXTRA_COVER_URL));
        book.setDescription(intent.getStringExtra(BookDetailActivity.EXTRA_DESCRIPTION));
        return book;
    }

    private ArrayList<Chapter> readChapters(Intent intent) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        Object value = intent.getSerializableExtra(EXTRA_CHAPTERS);
        if (value instanceof ArrayList<?>) {
            for (Object item : (ArrayList<?>) value) {
                if (item instanceof Chapter) {
                    chapters.add((Chapter) item);
                }
            }
        }
        return chapters;
    }

    private String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? "guest" : user.getUid();
    }

    private void deleteIfExists(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    private String sanitize(String value) {
        return value == null ? "unknown" : value.trim().replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
