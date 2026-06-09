package com.example.fonosfinal.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fonoslocal.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_LOCAL_BOOKS = "local_books";
    public static final String TABLE_DOWNLOADED_BOOKS = "downloaded_books";
    public static final String TABLE_DOWNLOADED_CHAPTERS = "downloaded_chapters";
    public static final String TABLE_LOVED_BOOKS = "loved_books";
    public static final String TABLE_LISTENING_PROGRESS = "listening_progress";

    public LocalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createLocalBooksTable(db);
        createDownloadTables(db);
    }

    private void createLocalBooksTable(SQLiteDatabase db) {
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

    private void createDownloadTables(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS downloaded_books (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id TEXT NOT NULL, " +
                        "book_remote_id TEXT NOT NULL, " +
                        "slug TEXT, " +
                        "title TEXT NOT NULL, " +
                        "author_names TEXT, " +
                        "narrator_names TEXT, " +
                        "category_names TEXT, " +
                        "description TEXT, " +
                        "cover_url TEXT, " +
                        "duration TEXT, " +
                        "rating REAL, " +
                        "total_chapter_count INTEGER DEFAULT 0, " +
                        "completed_chapter_count INTEGER DEFAULT 0, " +
                        "is_full_download_requested INTEGER DEFAULT 0, " +
                        "status TEXT NOT NULL, " +
                        "created_at TEXT, " +
                        "updated_at TEXT, " +
                        "UNIQUE(user_id, book_remote_id)" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS downloaded_chapters (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id TEXT NOT NULL, " +
                        "book_remote_id TEXT NOT NULL, " +
                        "chapter_id TEXT NOT NULL, " +
                        "chapter_index INTEGER NOT NULL, " +
                        "chapter_number TEXT, " +
                        "title TEXT NOT NULL, " +
                        "duration TEXT, " +
                        "audio_url TEXT NOT NULL, " +
                        "local_path TEXT, " +
                        "file_size_bytes INTEGER DEFAULT 0, " +
                        "status TEXT NOT NULL, " +
                        "downloaded_at TEXT, " +
                        "updated_at TEXT, " +
                        "UNIQUE(user_id, book_remote_id, chapter_id), " +
                        "UNIQUE(user_id, book_remote_id, chapter_index)" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS loved_books (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id TEXT NOT NULL, " +
                        "book_remote_id TEXT NOT NULL, " +
                        "slug TEXT, " +
                        "title TEXT NOT NULL, " +
                        "author_names TEXT, " +
                        "narrator_names TEXT, " +
                        "category_names TEXT, " +
                        "description TEXT, " +
                        "cover_url TEXT, " +
                        "duration TEXT, " +
                        "rating REAL, " +
                        "created_at TEXT, " +
                        "updated_at TEXT, " +
                        "UNIQUE(user_id, book_remote_id)" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS listening_progress (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id TEXT NOT NULL, " +
                        "book_remote_id TEXT NOT NULL, " +
                        "slug TEXT, " +
                        "title TEXT NOT NULL, " +
                        "author_names TEXT, " +
                        "narrator_names TEXT, " +
                        "category_names TEXT, " +
                        "description TEXT, " +
                        "cover_url TEXT, " +
                        "duration TEXT, " +
                        "rating REAL, " +
                        "chapter_id TEXT, " +
                        "chapter_index INTEGER DEFAULT 0, " +
                        "chapter_number TEXT, " +
                        "chapter_title TEXT, " +
                        "chapter_duration TEXT, " +
                        "audio_url TEXT, " +
                        "local_path TEXT, " +
                        "position_ms INTEGER DEFAULT 0, " +
                        "duration_ms INTEGER DEFAULT 0, " +
                        "updated_at TEXT, " +
                        "UNIQUE(user_id, book_remote_id)" +
                        ")"
        );

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_downloaded_books_user ON downloaded_books(user_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_downloaded_chapters_book ON downloaded_chapters(user_id, book_remote_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_loved_books_user ON loved_books(user_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_listening_progress_user ON listening_progress(user_id, updated_at)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createDownloadTables(db);
        }
    }
}
