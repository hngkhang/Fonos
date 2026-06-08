package com.example.fonosfinal.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fonoslocal.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_LOCAL_BOOKS = "local_books";

    public LocalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS local_books (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "remote_id TEXT UNIQUE NOT NULL, " +
                        "slug TEXT, " +
                        "title TEXT NOT NULL, " +
                        "author_names TEXT, " +
                        "narrator_names TEXT, " +
                        "category_names TEXT, " +
                        "genre_names TEXT, " +
                        "description TEXT, " +
                        "cover_url TEXT, " +
                        "duration TEXT, " +
                        "rating REAL, " +
                        "listen_count INTEGER DEFAULT 0, " +
                        "created_at TEXT, " +
                        "imported_at TEXT, " +
                        "cached_at TEXT" +
                        ")"
        );

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_books_listen_count ON local_books(listen_count)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_books_created_at ON local_books(created_at)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_books_title ON local_books(title)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS local_books");
        onCreate(db);
    }
}