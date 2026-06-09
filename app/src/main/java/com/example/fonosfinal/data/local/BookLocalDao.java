package com.example.fonosfinal.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.fonosfinal.models.Book;
import com.example.fonosfinal.util.SearchTextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookLocalDao {

    private final LocalDatabaseHelper dbHelper;

    public BookLocalDao(Context context) {
        this.dbHelper = new LocalDatabaseHelper(context.getApplicationContext());
    }

    public void upsertBooks(List<Book> books) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            for (Book book : books) {
                ContentValues values = new ContentValues();

                values.put("remote_id", book.getRemoteId());
                values.put("slug", book.getSlug());
                values.put("title", book.getTitle());
                values.put("search_title", getSearchTitle(book));
                values.put("author_names", book.getAuthor());
                values.put("narrator_names", book.getNarrator());
                values.put("category_names", book.getCategory());
                values.put("genre_names", book.getCategory());
                values.put("description", book.getDescription());
                values.put("cover_url", book.getCoverUrl());
                values.put("duration", book.getDuration());
                values.put("rating", parseDouble(book.getRating()));
                values.put("listen_count", book.getListenCount());
                values.put("created_at", book.getCreatedAt());
                values.put("imported_at", book.getImportedAt());
                values.put("cached_at", now());

                db.insertWithOnConflict(
                        LocalDatabaseHelper.TABLE_LOCAL_BOOKS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Book> getTrendingBooks(int limit) {
        return queryBooks("listen_count DESC, rating DESC", limit);
    }

    public List<Book> getNewReleaseBooks(int limit) {
        return queryBooks("created_at DESC, imported_at DESC", limit);
    }

    public List<Book> getRecommendedBooks(int limit) {
        return queryBooks("rating DESC, listen_count DESC", limit);
    }

    public List<Book> getAllBooks() {
        return queryBooks("title COLLATE NOCASE ASC", 0);
    }

    public List<Book> searchBooksByTitle(String query, int limit) {
        String normalizedQuery = SearchTextUtils.normalizeSearchText(query);
        List<Book> books = new ArrayList<>();
        if (normalizedQuery.isEmpty()) {
            return books;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                LocalDatabaseHelper.TABLE_LOCAL_BOOKS,
                null,
                null,
                null,
                null,
                null,
                "title COLLATE NOCASE ASC",
                null
        );

        try {
            while (cursor.moveToNext()) {
                Book book = cursorToBook(cursor);
                String normalizedTitle = book.getSearchTitle();
                if (normalizedTitle == null || normalizedTitle.trim().isEmpty()) {
                    normalizedTitle = SearchTextUtils.normalizeSearchText(book.getTitle());
                }

                if (normalizedTitle.contains(normalizedQuery)) {
                    books.add(book);
                    if (limit > 0 && books.size() >= limit) {
                        break;
                    }
                }
            }
        } finally {
            cursor.close();
        }

        return books;
    }

    private List<Book> queryBooks(String orderBy, int limit) {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                LocalDatabaseHelper.TABLE_LOCAL_BOOKS,
                null,
                null,
                null,
                null,
                null,
                orderBy,
                limit > 0 ? String.valueOf(limit) : null
        );

        try {
            while (cursor.moveToNext()) {
                books.add(cursorToBook(cursor));
            }
        } finally {
            cursor.close();
        }

        return books;
    }

    private Book cursorToBook(Cursor cursor) {
        Book book = new Book();

        book.setRemoteId(getString(cursor, "remote_id"));
        book.setSlug(getString(cursor, "slug"));
        book.setTitle(getString(cursor, "title"));
        book.setSearchTitle(getString(cursor, "search_title"));
        book.setAuthor(getString(cursor, "author_names"));
        book.setNarrator(getString(cursor, "narrator_names"));

        String category = getString(cursor, "category_names");
        String genre = getString(cursor, "genre_names");
        book.setCategory(category != null ? category : genre);

        book.setDescription(getString(cursor, "description"));
        book.setCoverUrl(getString(cursor, "cover_url"));
        book.setDuration(getString(cursor, "duration"));

        int ratingIndex = cursor.getColumnIndex("rating");
        if (ratingIndex >= 0 && !cursor.isNull(ratingIndex)) {
            book.setRating(String.valueOf(cursor.getDouble(ratingIndex)));
        }

        int listenIndex = cursor.getColumnIndex("listen_count");
        if (listenIndex >= 0 && !cursor.isNull(listenIndex)) {
            book.setListenCount(cursor.getLong(listenIndex));
        }

        book.setCreatedAt(getString(cursor, "created_at"));
        book.setImportedAt(getString(cursor, "imported_at"));

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

    private String getSearchTitle(Book book) {
        String searchTitle = book.getSearchTitle();
        if (searchTitle != null && !searchTitle.trim().isEmpty()) {
            return SearchTextUtils.normalizeSearchText(searchTitle);
        }
        return SearchTextUtils.normalizeSearchText(book.getTitle());
    }
}
