package com.example.fonosfinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fonosfinal.BookDetailActivity;
import com.example.fonosfinal.PlayerActivity;
import com.example.fonosfinal.R;
import com.example.fonosfinal.data.local.DownloadLocalDao;
import com.example.fonosfinal.models.Book;
import com.example.fonosfinal.models.Chapter;
import com.example.fonosfinal.models.ListeningProgress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LibraryFragment extends Fragment {

    private static final String TAB_SAVED = "saved";
    private static final String TAB_LOVED = "loved";
    private static final String TAB_LISTENING = "listening";

    private DownloadLocalDao downloadLocalDao;
    private String userId;
    private LinearLayout cardsContainer;
    private TextView sectionTitle;
    private TextView savedChip;
    private TextView lovedChip;
    private TextView listeningChip;
    private String selectedTab = TAB_SAVED;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        downloadLocalDao = new DownloadLocalDao(requireContext());
        userId = getUserId();
        cardsContainer = view.findViewById(R.id.layout_library_cards);
        sectionTitle = view.findViewById(R.id.text_library_saved_section);
        savedChip = view.findViewById(R.id.chip_library_saved);
        lovedChip = view.findViewById(R.id.chip_library_loved);
        listeningChip = view.findViewById(R.id.chip_library_listening);

        view.findViewById(R.id.layout_library_continue_card).setVisibility(View.GONE);

        savedChip.setOnClickListener(v -> showTab(TAB_SAVED));
        lovedChip.setOnClickListener(v -> showTab(TAB_LOVED));
        listeningChip.setOnClickListener(v -> showTab(TAB_LISTENING));

        showTab(TAB_SAVED);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (downloadLocalDao != null) {
            showTab(selectedTab);
        }
    }

    private void showTab(String tab) {
        selectedTab = tab;
        updateChipState();
        cardsContainer.removeAllViews();

        if (TAB_LOVED.equals(tab)) {
            sectionTitle.setText("Loved audiobooks");
            renderBooks(downloadLocalDao.getLovedBooks(userId), "No loved books yet.", false);
        } else if (TAB_LISTENING.equals(tab)) {
            sectionTitle.setText("Continue listening");
            renderListening(downloadLocalDao.getListeningProgress(userId));
        } else {
            sectionTitle.setText("Saved audiobooks");
            renderBooks(downloadLocalDao.getSavedBooks(userId), "No downloaded books yet.", true);
        }
    }

    private void renderBooks(List<Book> books, String emptyMessage, boolean openOfflinePlayer) {
        if (books.isEmpty()) {
            addEmptyText(emptyMessage);
            return;
        }

        for (Book book : books) {
            View item = createBookRow(book);
            item.setOnClickListener(v -> {
                if (openOfflinePlayer) {
                    openSavedBook(book);
                } else {
                    startActivity(BookDetailActivity.createBookDetailIntent(requireContext(), book));
                }
            });
            cardsContainer.addView(item);
        }
    }

    private void renderListening(List<ListeningProgress> progressItems) {
        if (progressItems.isEmpty()) {
            addEmptyText("No listening progress yet.");
            return;
        }

        for (ListeningProgress progress : progressItems) {
            Book book = progress.getBook();
            Chapter chapter = progress.getChapter();
            View item = createBookRow(book);
            TextView author = item.findViewById(R.id.text_category_book_author);
            TextView meta = item.findViewById(R.id.text_category_book_meta);
            TextView category = item.findViewById(R.id.text_category_book_category);

            author.setText(chapter.getTitle());
            meta.setText(formatProgress(progress.getPositionMs(), progress.getDurationMs()) + "  |  Listening");
            category.setText(book.getCategory());
            item.setOnClickListener(v -> openListening(progress));
            cardsContainer.addView(item);
        }
    }

    private View createBookRow(Book book) {
        View item = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_category_book, cardsContainer, false);
        ImageView cover = item.findViewById(R.id.image_category_book_cover);
        TextView title = item.findViewById(R.id.text_category_book_title);
        TextView author = item.findViewById(R.id.text_category_book_author);
        TextView meta = item.findViewById(R.id.text_category_book_meta);
        TextView category = item.findViewById(R.id.text_category_book_category);

        int fallbackCover = getCoverDrawable(book.getCoverType());
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        meta.setText(book.getDuration() + "  |  " + book.getRating());
        category.setText(book.getCategory());
        cover.setBackgroundResource(fallbackCover);

        Glide.with(cover)
                .load(book.getCoverUrl())
                .placeholder(fallbackCover)
                .error(fallbackCover)
                .centerCrop()
                .into(cover);
        return item;
    }

    private void openSavedBook(Book book) {
        ArrayList<Chapter> downloadedChapters =
                downloadLocalDao.getCompletedChaptersForBook(userId, book.getRemoteId());
        if (downloadedChapters.isEmpty()) {
            Toast.makeText(requireContext(), "No downloaded chapters found.", Toast.LENGTH_SHORT).show();
            return;
        }
        openPlayer(book, downloadedChapters, downloadedChapters.get(0), 0);
    }

    private void openListening(ListeningProgress progress) {
        Book book = progress.getBook();
        ArrayList<Chapter> chapters =
                downloadLocalDao.getCompletedChaptersForBook(userId, book.getRemoteId());
        if (chapters.isEmpty()) {
            chapters.add(progress.getChapter());
        }
        Chapter selectedChapter = progress.getChapter();
        for (Chapter chapter : chapters) {
            if (chapter.getChapterIndex() == selectedChapter.getChapterIndex()) {
                selectedChapter = chapter;
                break;
            }
        }
        openPlayer(book, chapters, selectedChapter, progress.getPositionMs());
    }

    private void openPlayer(Book book, ArrayList<Chapter> chapters,
                            Chapter chapter, int startPositionMs) {
        Intent intent = new Intent(requireContext(), PlayerActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_TITLE, book.getTitle());
        intent.putExtra(BookDetailActivity.EXTRA_AUTHOR, book.getAuthor());
        intent.putExtra(BookDetailActivity.EXTRA_NARRATOR, book.getNarrator());
        intent.putExtra(BookDetailActivity.EXTRA_DURATION, book.getDuration());
        intent.putExtra(BookDetailActivity.EXTRA_RATING, book.getRating());
        intent.putExtra(BookDetailActivity.EXTRA_CATEGORY, book.getCategory());
        intent.putExtra(BookDetailActivity.EXTRA_COVER_TYPE, book.getCoverType());
        intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getRemoteId());
        intent.putExtra(BookDetailActivity.EXTRA_COVER_URL, book.getCoverUrl());
        intent.putExtra(BookDetailActivity.EXTRA_DESCRIPTION, book.getDescription());
        intent.putExtra(PlayerActivity.EXTRA_CHAPTER_TITLE, chapter.getTitle());
        intent.putExtra(PlayerActivity.EXTRA_CHAPTER_INDEX, chapter.getChapterIndex());
        intent.putExtra(PlayerActivity.EXTRA_CHAPTER_DURATION, chapter.getDuration());
        intent.putExtra(PlayerActivity.EXTRA_AUDIO_URL, chapter.getPlaybackUrl());
        intent.putExtra(PlayerActivity.EXTRA_LOCAL_PATH, chapter.getLocalPath());
        intent.putExtra(PlayerActivity.EXTRA_START_POSITION, startPositionMs);
        intent.putExtra(PlayerActivity.EXTRA_CHAPTERS, chapters);
        startActivity(intent);
    }

    private void addEmptyText(String message) {
        TextView empty = new TextView(requireContext());
        empty.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        empty.setText(message);
        empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        empty.setTextSize(14);
        empty.setGravity(android.view.Gravity.CENTER);
        empty.setPadding(0, dpToPx(18), 0, dpToPx(18));
        cardsContainer.addView(empty);
    }

    private void updateChipState() {
        applyChipStyle(savedChip, TAB_SAVED.equals(selectedTab));
        applyChipStyle(lovedChip, TAB_LOVED.equals(selectedTab));
        applyChipStyle(listeningChip, TAB_LISTENING.equals(selectedTab));
    }

    private void applyChipStyle(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        chip.setTextColor(ContextCompat.getColor(
                requireContext(),
                selected ? R.color.chip_selected_text : R.color.text_secondary
        ));
    }

    private String formatProgress(int positionMs, int durationMs) {
        if (durationMs <= 0) {
            return formatTime(positionMs);
        }
        int percent = Math.max(0, Math.min(100, Math.round(positionMs * 100f / durationMs)));
        return String.format(Locale.US, "%s  |  %d%%", formatTime(positionMs), percent);
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

    private int getCoverDrawable(int coverType) {
        if (coverType == 1) return R.drawable.bg_cover_blue;
        if (coverType == 2) return R.drawable.bg_cover_green;
        if (coverType == 3) return R.drawable.bg_cover_orange;
        if (coverType == 4) return R.drawable.bg_cover_purple;
        return R.drawable.bg_book_placeholder;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? "guest" : user.getUid();
    }
}
