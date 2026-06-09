package com.example.fonosfinal.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.fonosfinal.data.local.BookLocalDao;
import com.example.fonosfinal.models.Book;
import com.example.fonosfinal.util.SearchTextUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class BookRepository {

    private static final String TAG = "BookRepository";
    private static final int SEARCH_LIMIT = 30;
    private static final int FALLBACK_REMOTE_LIMIT = 250;

    private final BookLocalDao bookLocalDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface HomeBooksCallback {
        void onLocalLoaded(List<Book> trending, List<Book> recommended, List<Book> newReleases);
        void onRemoteSynced(List<Book> trending, List<Book> recommended, List<Book> newReleases);
        void onError(Exception e);
    }

    public interface BooksCallback {
        void onLocalLoaded(List<Book> books);
        void onRemoteSynced(List<Book> books);
        void onError(Exception e);
    }

    public interface SearchCallback {
        void onLocalResult(List<Book> books);
        void onRemoteResult(List<Book> books);
        void onError(Exception e);
    }

    public BookRepository(Context context) {
        this.bookLocalDao = new BookLocalDao(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void loadHomeBooks(HomeBooksCallback callback) {
        executor.execute(() -> {
            List<Book> trending = bookLocalDao.getTrendingBooks(10);
            List<Book> recommended = bookLocalDao.getRecommendedBooks(10);
            List<Book> newReleases = bookLocalDao.getNewReleaseBooks(10);

            mainHandler.post(() ->
                    callback.onLocalLoaded(trending, recommended, newReleases)
            );
        });

        firestore.collection("books")
                .limit(250)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Book> remoteBooks = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        remoteBooks.add(mapFirestoreBook(doc.getId(), doc.getData()));
                    }

                    executor.execute(() -> {
                        bookLocalDao.upsertBooks(remoteBooks);

                        List<Book> trending = bookLocalDao.getTrendingBooks(10);
                        List<Book> recommended = bookLocalDao.getRecommendedBooks(10);
                        List<Book> newReleases = bookLocalDao.getNewReleaseBooks(10);

                        mainHandler.post(() ->
                                callback.onRemoteSynced(trending, recommended, newReleases)
                        );
                    });
                })
                .addOnFailureListener(e ->
                        mainHandler.post(() -> callback.onError(e))
                );
    }

    public void loadAllBooks(BooksCallback callback) {
        executor.execute(() -> {
            List<Book> books = bookLocalDao.getAllBooks();
            mainHandler.post(() -> callback.onLocalLoaded(books));
        });

        firestore.collection("books")
                .limit(250)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Book> remoteBooks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        remoteBooks.add(mapFirestoreBook(doc.getId(), doc.getData()));
                    }

                    executor.execute(() -> {
                        bookLocalDao.upsertBooks(remoteBooks);
                        List<Book> books = bookLocalDao.getAllBooks();
                        mainHandler.post(() -> callback.onRemoteSynced(books));
                    });
                })
                .addOnFailureListener(e ->
                        mainHandler.post(() -> callback.onError(e))
                );
    }

    public void searchBooksByTitle(String query, SearchCallback callback) {
        String normalizedQuery = SearchTextUtils.normalizeSearchText(query);
        Log.d(TAG, "Search query: " + normalizedQuery);

        if (normalizedQuery.isEmpty()) {
            callback.onLocalResult(new ArrayList<>());
            callback.onRemoteResult(new ArrayList<>());
            return;
        }

        executor.execute(() -> {
            List<Book> localBooks = bookLocalDao.searchBooksByTitle(normalizedQuery, SEARCH_LIMIT);
            Log.d(TAG, "Local result count: " + localBooks.size());
            mainHandler.post(() -> callback.onLocalResult(localBooks));
        });

        queryFirestoreBySearchTitle(normalizedQuery, callback);
    }

    private void queryFirestoreBySearchTitle(String normalizedQuery, SearchCallback callback) {
        firestore.collection("books")
                .whereGreaterThanOrEqualTo("searchTitle", normalizedQuery)
                .whereLessThanOrEqualTo("searchTitle", normalizedQuery + "\uf8ff")
                .limit(SEARCH_LIMIT)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Book> remoteBooks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        remoteBooks.add(mapFirestoreBook(doc.getId(), doc.getData()));
                    }

                    if (remoteBooks.isEmpty()) {
                        fallbackSearchFirestore(normalizedQuery, callback, null);
                        return;
                    }

                    syncRemoteSearchResults(remoteBooks, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore searchTitle query failed", e);
                    fallbackSearchFirestore(normalizedQuery, callback, e);
                });
    }

    private void fallbackSearchFirestore(String normalizedQuery, SearchCallback callback, Exception originalException) {
        firestore.collection("books")
                .limit(FALLBACK_REMOTE_LIMIT)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Book> remoteBooks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Book book = mapFirestoreBook(doc.getId(), doc.getData());
                        String normalizedTitle = SearchTextUtils.normalizeSearchText(book.getTitle());
                        if (normalizedTitle.contains(normalizedQuery)) {
                            remoteBooks.add(book);
                            if (remoteBooks.size() >= SEARCH_LIMIT) {
                                break;
                            }
                        }
                    }
                    syncRemoteSearchResults(remoteBooks, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore fallback search failed", e);
                    mainHandler.post(() -> callback.onError(originalException != null ? originalException : e));
                });
    }

    private void syncRemoteSearchResults(List<Book> remoteBooks, SearchCallback callback) {
        Log.d(TAG, "Remote result count: " + remoteBooks.size());
        executor.execute(() -> {
            if (!remoteBooks.isEmpty()) {
                bookLocalDao.upsertBooks(remoteBooks);
            }
            mainHandler.post(() -> callback.onRemoteResult(remoteBooks));
        });
    }

    private Book mapFirestoreBook(String documentId, Map<String, Object> data) {
        Book book = new Book();

        String fieldId = getFirstString(data, "id", "bookId");
        book.setRemoteId(isBlank(fieldId) ? documentId : fieldId);
        book.setSlug(getString(data, "slug"));
        book.setTitle(getString(data, "title"));
        book.setSearchTitle(getFirstString(data, "searchTitle"));
        book.setDescription(getFirstString(data, "description", "desc"));
        book.setCoverUrl(getFirstString(data, "coverUrl", "cover", "imageUrl"));
        book.setDuration(getString(data, "duration"));

        book.setAuthor(getFirstString(data, "authorNames", "authorName", "author"));
        book.setNarrator(getFirstString(data, "narratorNames", "narratorName", "narrator"));

        String category = getFirstString(data, "categoryNames", "categoryName", "category");
        String genre = getFirstString(data, "genreNames", "genreName", "genre");
        book.setCategory(joinValues(category, genre));

        Double rating = getDouble(data, "rating");
        book.setRating(rating == null ? getString(data, "rating") : String.valueOf(rating));

        Long listenCount = getLong(data, "listenCount");
        book.setListenCount(listenCount == null ? 0 : listenCount);

        book.setCreatedAt(timestampToText(data.get("createdAt")));
        book.setImportedAt(timestampToText(data.get("importedAt")));

        int coverType = Math.abs((book.getTitle() == null ? "" : book.getTitle()).hashCode()) % 4 + 1;
        book.setCoverType(coverType);
        if (isBlank(book.getSearchTitle())) {
            book.setSearchTitle(SearchTextUtils.normalizeSearchText(book.getTitle()));
        }

        return book;
    }

    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Double getDouble(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private Long getLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private String listOrString(Object value) {
        if (value == null) return null;

        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            List<String> parts = new ArrayList<>();

            for (Object item : list) {
                if (item != null) {
                    parts.add(String.valueOf(item));
                }
            }

            return String.join(", ", parts);
        }

        return String.valueOf(value);
    }

    private String getFirstString(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            String value = listOrString(data.get(key));
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String joinValues(String first, String second) {
        if (first == null || first.trim().isEmpty()) return second;
        if (second == null || second.trim().isEmpty() || first.equalsIgnoreCase(second)) return first;
        return first + ", " + second;
    }

    private String timestampToText(Object value) {
        try {
            if (value instanceof Timestamp) {
                return new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.US
                ).format(((Timestamp) value).toDate());
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
