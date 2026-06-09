package com.example.fonosfinal.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fonosfinal.models.Book;
import com.example.fonosfinal.models.Chapter;
import com.example.fonosfinal.models.DownloadedChapter;
import com.example.fonosfinal.models.ListeningProgress;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DownloadLocalDao {

    private final LocalDatabaseHelper dbHelper;

    public DownloadLocalDao(Context context) {
        this.dbHelper = new LocalDatabaseHelper(context.getApplicationContext());
    }

    public static String resolveBookId(Book book) {
        if (book != null && hasText(book.getRemoteId())) {
            return book.getRemoteId().trim();
        }
        String title = book == null ? null : book.getTitle();
        return "local:" + sanitizeId(hasText(title) ? title : "unknown_book");
    }

    public static String resolveChapterId(Chapter chapter) {
        if (chapter != null && hasText(chapter.getChapterId())) {
            return chapter.getChapterId().trim();
        }
        int chapterIndex = chapter == null ? 0 : chapter.getChapterIndex();
        return "chapter_" + chapterIndex;
    }

    public Map<String, DownloadedChapter> getChapterDownloads(String userId, String bookRemoteId) {
        Map<String, DownloadedChapter> downloads = new HashMap<>();
        if (!hasText(userId) || !hasText(bookRemoteId)) return downloads;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                LocalDatabaseHelper.TABLE_DOWNLOADED_CHAPTERS,
                null,
                "user_id = ? AND book_remote_id = ?",
                new String[]{userId, bookRemoteId},
                null,
                null,
                null
        );

        try {
            while (cursor.moveToNext()) {
                DownloadedChapter chapter = cursorToDownloadedChapter(cursor);
                downloads.put(chapter.getChapterId(), chapter);
            }
        } finally {
            cursor.close();
        }
        return downloads;
    }

    public boolean isChapterCompleted(String userId, String bookRemoteId, String chapterId) {
        return hasChapterStatus(userId, bookRemoteId, chapterId, DownloadStatus.COMPLETED);
    }

    public boolean isChapterDownloading(String userId, String bookRemoteId, String chapterId) {
        return hasChapterStatus(userId, bookRemoteId, chapterId, DownloadStatus.DOWNLOADING);
    }

    public boolean isBookFullyDownloaded(String userId, String bookRemoteId, int totalChapterCount) {
        if (!hasText(userId) || !hasText(bookRemoteId) || totalChapterCount <= 0) return false;
        return getCompletedChapterCount(userId, bookRemoteId) >= totalChapterCount;
    }

    public void markChapterDownloading(String userId, Book book, Chapter chapter,
                                       int totalChapterCount, boolean fullDownloadRequested) {
        String bookRemoteId = resolveBookId(book);
        String chapterId = resolveChapterId(chapter);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            upsertDownloadedBook(db, userId, bookRemoteId, book, totalChapterCount,
                    fullDownloadRequested, DownloadStatus.DOWNLOADING);

            ContentValues values = baseChapterValues(userId, bookRemoteId, chapterId, chapter);
            values.put("local_path", (String) null);
            values.put("file_size_bytes", 0);
            values.put("status", DownloadStatus.DOWNLOADING);
            values.put("downloaded_at", (String) null);
            values.put("updated_at", now());

            db.insertWithOnConflict(
                    LocalDatabaseHelper.TABLE_DOWNLOADED_CHAPTERS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void markChapterCompleted(String userId, Book book, Chapter chapter,
                                     String localPath, long fileSizeBytes,
                                     int totalChapterCount, boolean fullDownloadRequested) {
        String bookRemoteId = resolveBookId(book);
        String chapterId = resolveChapterId(chapter);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            upsertDownloadedBook(db, userId, bookRemoteId, book, totalChapterCount,
                    fullDownloadRequested, DownloadStatus.COMPLETED);

            ContentValues values = baseChapterValues(userId, bookRemoteId, chapterId, chapter);
            values.put("local_path", localPath);
            values.put("file_size_bytes", fileSizeBytes);
            values.put("status", DownloadStatus.COMPLETED);
            values.put("downloaded_at", now());
            values.put("updated_at", now());

            db.insertWithOnConflict(
                    LocalDatabaseHelper.TABLE_DOWNLOADED_CHAPTERS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
            refreshBookDownloadCounts(db, userId, bookRemoteId, totalChapterCount);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void markChapterFailed(String userId, Book book, Chapter chapter,
                                  int totalChapterCount, boolean fullDownloadRequested) {
        String bookRemoteId = resolveBookId(book);
        String chapterId = resolveChapterId(chapter);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            upsertDownloadedBook(db, userId, bookRemoteId, book, totalChapterCount,
                    fullDownloadRequested, DownloadStatus.FAILED);

            ContentValues values = baseChapterValues(userId, bookRemoteId, chapterId, chapter);
            values.put("local_path", (String) null);
            values.put("file_size_bytes", 0);
            values.put("status", DownloadStatus.FAILED);
            values.put("downloaded_at", (String) null);
            values.put("updated_at", now());

            db.insertWithOnConflict(
                    LocalDatabaseHelper.TABLE_DOWNLOADED_CHAPTERS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
            refreshBookDownloadCounts(db, userId, bookRemoteId, totalChapterCount);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<Chapter> getCompletedChaptersForBook(String userId, String bookRemoteId) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        if (!hasText(userId) || !hasText(bookRemoteId)) return chapters;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                LocalDatabaseHelper.TABLE_DOWNLOADED_CHAPTERS,
                null,
                "user_id = ? AND book_remote_id = ? AND status = ?",
                new String[]{userId, bookRemoteId, DownloadStatus.COMPLETED},
                null,
                null,
                "chapter_index ASC"
        );

        try {
            while (cursor.moveToNext()) {
                chapters.add(cursorToChapter(cursor));
            }
        } finally {
            cursor.close();
        }
        return chapters;
    }

    public List<Book> getSavedBooks(String userId) {
        List<Book> books = new ArrayList<>();
        if (!hasText(userId)) return books;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                LocalDatabaseHelper.TABLE_DOWNLOADED_BOOKS,
                null,
                "user_id = ? AND completed_chapter_count > 0",
                new String[]{userId},
                null,
                null,
                "updated_at DESC"
        );

        try {
            while (cursor.moveToNext()) {
                books.add(cursorToBook(cursor, "book_remote_id"));
            }
        } finally {
            cursor.close();
        }
        return books;
    }

    public boolean isBookLoved(String userId, String bookRemoteId) {
        if (!hasText(userId) || !hasText(bookRemoteId)) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                LocalDatabaseHelper.TABLE_LOVED_BOOKS,
                new String[]{"id"},
                "user_id = ? AND book_remote_id = ?",
                new String[]{userId, bookRemoteId},
                null,
                null,
                null,
                "1"
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public void setBookLoved(String userId, Book book, boolean loved) {
        String bookRemoteId = resolveBookId(book);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (!loved) {
            db.delete(
                    LocalDatabaseHelper.TABLE_LOVED_BOOKS,
                    "user_id = ? AND book_remote_id = ?",
                    new String[]{userId, bookRemoteId}
            );
            return;
        }

        ContentValues values = baseBookValues(userId, bookRemoteId, book);
        values.put("created_at", now());
        values.put("updated_at", now());
        db.insertWithOnConflict(
                LocalDatabaseHelper.TABLE_LOVED_BOOKS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public List<Book> getLovedBooks(String userId) {
        List<Book> books = new ArrayList<>();
        if (!hasText(userId)) return books;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                LocalDatabaseHelper.TABLE_LOVED_BOOKS,
                null,
                "user_id = ?",
                new String[]{userId},
                null,
                null,
                "updated_at DESC"
        );

        try {
            while (cursor.moveToNext()) {
                books.add(cursorToBook(cursor, "book_remote_id"));
            }
        } finally {
            cursor.close();
        }
        return books;
    }

    public void saveListeningProgress(String userId, Book book, Chapter chapter,
                                      int positionMs, int durationMs) {
        if (!hasText(userId) || book == null || chapter == null || positionMs <= 0) return;

        String bookRemoteId = resolveBookId(book);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = baseBookValues(userId, bookRemoteId, book);
        values.put("chapter_id", resolveChapterId(chapter));
        values.put("chapter_index", chapter.getChapterIndex());
        values.put("chapter_number", chapter.getNumber());
        values.put("chapter_title", chapter.getTitle());
        values.put("chapter_duration", chapter.getDuration());
        values.put("audio_url", chapter.getAudioUrl());
        values.put("local_path", chapter.getLocalPath());
        values.put("position_ms", positionMs);
        values.put("duration_ms", durationMs);
        values.put("updated_at", now());

        db.insertWithOnConflict(
                LocalDatabaseHelper.TABLE_LISTENING_PROGRESS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public List<ListeningProgress> getListeningProgress(String userId) {
        List<ListeningProgress> progress = new ArrayList<>();
        if (!hasText(userId)) return progress;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                LocalDatabaseHelper.TABLE_LISTENING_PROGRESS,
                null,
                "user_id = ? AND position_ms > 0",
                new String[]{userId},
                null,
                null,
                "updated_at DESC"
        );

        try {
            while (cursor.moveToNext()) {
                Book book = cursorToBook(cursor, "book_remote_id");
                Chapter chapter = new Chapter(
                        book.getRemoteId(),
                        getString(cursor, "chapter_id"),
                        getString(cursor, "chapter_number"),
                        getString(cursor, "chapter_title"),
                        getString(cursor, "chapter_duration"),
                        "listening",
                        getInt(cursor, "chapter_index", 0),
                        getString(cursor, "audio_url"),
                        getString(cursor, "local_path")
                );
                progress.add(new ListeningProgress(
                        book,
                        chapter,
                        getInt(cursor, "position_ms", 0),
                        getInt(cursor, "duration_ms", 0)
                ));
            }
        } finally {
            cursor.close();
        }
        return progress;
    }

    private boolean hasChapterStatus(String userId, String bookRemoteId,
                                     String chapterId, String status) {
        if (!hasText(userId) || !hasText(bookRemoteId) || !hasText(chapterId)) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                LocalDatabaseHelper.TABLE_DOWNLOADED_CHAPTERS,
                new String[]{"id"},
                "user_id = ? AND book_remote_id = ? AND chapter_id = ? AND status = ?",
                new String[]{userId, bookRemoteId, chapterId, status},
                null,
                null,
                null,
                "1"
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    private int getCompletedChapterCount(String userId, String bookRemoteId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return getCompletedChapterCount(db, userId, bookRemoteId);
    }

    private int getCompletedChapterCount(SQLiteDatabase db, String userId, String bookRemoteId) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM downloaded_chapters " +
                        "WHERE user_id = ? AND book_remote_id = ? AND status = ?",
                new String[]{userId, bookRemoteId, DownloadStatus.COMPLETED}
        );
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    private void refreshBookDownloadCounts(SQLiteDatabase db, String userId,
                                           String bookRemoteId, int totalChapterCount) {
        int completedCount = getCompletedChapterCount(db, userId, bookRemoteId);
        ContentValues values = new ContentValues();
        values.put("completed_chapter_count", completedCount);
        values.put("total_chapter_count", totalChapterCount);
        values.put("status", completedCount > 0 ? DownloadStatus.COMPLETED : DownloadStatus.FAILED);
        values.put("updated_at", now());
        db.update(
                LocalDatabaseHelper.TABLE_DOWNLOADED_BOOKS,
                values,
                "user_id = ? AND book_remote_id = ?",
                new String[]{userId, bookRemoteId}
        );
    }

    private void upsertDownloadedBook(SQLiteDatabase db, String userId, String bookRemoteId,
                                      Book book, int totalChapterCount,
                                      boolean fullDownloadRequested, String status) {
        ContentValues values = baseBookValues(userId, bookRemoteId, book);
        values.put("total_chapter_count", totalChapterCount);
        values.put("is_full_download_requested", fullDownloadRequested ? 1 : 0);
        values.put("status", status);
        values.put("created_at", now());
        values.put("updated_at", now());
        db.insertWithOnConflict(
                LocalDatabaseHelper.TABLE_DOWNLOADED_BOOKS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    private ContentValues baseBookValues(String userId, String bookRemoteId, Book book) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("book_remote_id", bookRemoteId);
        values.put("slug", book.getSlug());
        values.put("title", book.getTitle());
        values.put("author_names", book.getAuthor());
        values.put("narrator_names", book.getNarrator());
        values.put("category_names", book.getCategory());
        values.put("description", book.getDescription());
        values.put("cover_url", book.getCoverUrl());
        values.put("duration", book.getDuration());
        values.put("rating", parseDouble(book.getRating()));
        return values;
    }

    private ContentValues baseChapterValues(String userId, String bookRemoteId,
                                            String chapterId, Chapter chapter) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("book_remote_id", bookRemoteId);
        values.put("chapter_id", chapterId);
        values.put("chapter_index", chapter.getChapterIndex());
        values.put("chapter_number", chapter.getNumber());
        values.put("title", chapter.getTitle());
        values.put("duration", chapter.getDuration());
        values.put("audio_url", chapter.getAudioUrl());
        return values;
    }

    private DownloadedChapter cursorToDownloadedChapter(Cursor cursor) {
        return new DownloadedChapter(
                getString(cursor, "user_id"),
                getString(cursor, "book_remote_id"),
                getString(cursor, "chapter_id"),
                getInt(cursor, "chapter_index", 0),
                getString(cursor, "chapter_number"),
                getString(cursor, "title"),
                getString(cursor, "duration"),
                getString(cursor, "audio_url"),
                getString(cursor, "local_path"),
                getLong(cursor, "file_size_bytes", 0),
                getString(cursor, "status")
        );
    }

    private Chapter cursorToChapter(Cursor cursor) {
        return new Chapter(
                getString(cursor, "book_remote_id"),
                getString(cursor, "chapter_id"),
                getString(cursor, "chapter_number"),
                getString(cursor, "title"),
                getString(cursor, "duration"),
                DownloadStatus.COMPLETED,
                getInt(cursor, "chapter_index", 0),
                getString(cursor, "audio_url"),
                getString(cursor, "local_path")
        );
    }

    private Book cursorToBook(Cursor cursor, String idColumn) {
        Book book = new Book();
        book.setRemoteId(getString(cursor, idColumn));
        book.setSlug(getString(cursor, "slug"));
        book.setTitle(getString(cursor, "title"));
        book.setAuthor(getString(cursor, "author_names"));
        book.setNarrator(getString(cursor, "narrator_names"));
        book.setCategory(getString(cursor, "category_names"));
        book.setDescription(getString(cursor, "description"));
        book.setCoverUrl(getString(cursor, "cover_url"));
        book.setDuration(getString(cursor, "duration"));

        double rating = getDouble(cursor, "rating", 0.0);
        if (rating > 0) {
            book.setRating(String.valueOf(rating));
        }
        int coverType = Math.abs((book.getTitle() == null ? "" : book.getTitle()).hashCode()) % 4 + 1;
        book.setCoverType(coverType);
        return book;
    }

    private String getString(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index >= 0 && !cursor.isNull(index)) {
            return cursor.getString(index);
        }
        return null;
    }

    private int getInt(Cursor cursor, String columnName, int fallback) {
        int index = cursor.getColumnIndex(columnName);
        if (index >= 0 && !cursor.isNull(index)) {
            return cursor.getInt(index);
        }
        return fallback;
    }

    private long getLong(Cursor cursor, String columnName, long fallback) {
        int index = cursor.getColumnIndex(columnName);
        if (index >= 0 && !cursor.isNull(index)) {
            return cursor.getLong(index);
        }
        return fallback;
    }

    private double getDouble(Cursor cursor, String columnName, double fallback) {
        int index = cursor.getColumnIndex(columnName);
        if (index >= 0 && !cursor.isNull(index)) {
            return cursor.getDouble(index);
        }
        return fallback;
    }

    private double parseDouble(String value) {
        try {
            return value == null ? 0.0 : Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String sanitizeId(String value) {
        return value == null ? "unknown" : value.trim().replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
