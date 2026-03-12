package me.nethma.bookdiary;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.Book;
import me.nethma.bookdiary.utils.SessionManager;

/**
 * Diary Fragment – personal book diary with stats, search, status filter and a
 * per-book card that opens BookDetailActivity or EditBookActivity.
 */
public class DiaryFragment extends BaseFragment {

    // ── Filter options ────────────────────────────────────────────────────────
    private static final String[] FILTERS = {
            "All", "Want to Read", "Currently Reading", "Finished"
    };

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView       tvStatTotal, tvStatReviewed, tvStatFavorites;
    private EditText       etSearch;
    private LinearLayout   chipContainer;
    private TextView       tvBookCount;
    private RecyclerView   rvDiary;
    private View           emptyContainer;

    // ── Adapter ───────────────────────────────────────────────────────────────
    private DiaryBookAdapter adapter;

    // ── State ─────────────────────────────────────────────────────────────────
    private String searchQuery      = "";
    private String selectedFilter   = "All";

    // ── Threading ─────────────────────────────────────────────────────────────
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ── Dependencies ──────────────────────────────────────────────────────────
    private SessionManager sessionManager;
    private AppDatabase    db;

    // ── Launchers ─────────────────────────────────────────────────────────────
    private ActivityResultLauncher<Intent> detailLauncher;
    private ActivityResultLauncher<Intent> editLauncher;

    // ─────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        sessionManager = new SessionManager(requireContext());
        db             = AppDatabase.getInstance(requireContext());

        // Register result launchers
        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) loadData();
                });
        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) loadData();
                });

        // Bind views
        tvStatTotal     = view.findViewById(R.id.tv_stat_total);
        tvStatReviewed  = view.findViewById(R.id.tv_stat_reviewed);
        tvStatFavorites = view.findViewById(R.id.tv_stat_favorites);
        etSearch        = view.findViewById(R.id.et_search);
        chipContainer   = view.findViewById(R.id.chip_container);
        tvBookCount     = view.findViewById(R.id.tv_book_count);
        rvDiary         = view.findViewById(R.id.rv_diary);
        emptyContainer  = view.findViewById(R.id.empty_container);

        // Adapter
        adapter = new DiaryBookAdapter(new DiaryBookAdapter.OnBookActionListener() {
            @Override
            public void onBookClick(Book book) {
                Intent i = new Intent(requireContext(), BookDetailActivity.class);
                i.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.id);
                detailLauncher.launch(i);
            }

            @Override
            public void onFavouriteToggle(Book book) {
                toggleFavourite(book);
            }

            @Override
            public void onEditClick(Book book) {
                Intent i = new Intent(requireContext(), EditBookActivity.class);
                i.putExtra(EditBookActivity.EXTRA_BOOK_ID, book.id);
                editLauncher.launch(i);
            }
        });

        rvDiary.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDiary.setAdapter(adapter);
        rvDiary.setNestedScrollingEnabled(false);

        // Search bar
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                loadBooks();
            }
        });

        // Filter chips
        buildFilterChips();

        // Initial load
        loadData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        refreshChipStyles();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Filter chips
    // ─────────────────────────────────────────────────────────────────────────

    private void buildFilterChips() {
        chipContainer.removeAllViews();
        int accent = accentColor();
        for (String filter : FILTERS) {
            TextView chip = (TextView) LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_category_chip, chipContainer, false);
            chip.setText(filter.equals("Currently Reading") ? "Reading" : filter);
            applyChipStyle(chip, filter.equals(selectedFilter), accent);
            chip.setOnClickListener(v -> {
                selectedFilter = filter;
                refreshChipStyles();
                loadBooks();
            });
            chipContainer.addView(chip);
        }
    }

    private void refreshChipStyles() {
        if (chipContainer == null) return;
        int accent = accentColor();
        for (int i = 0; i < chipContainer.getChildCount(); i++) {
            applyChipStyle((TextView) chipContainer.getChildAt(i),
                    FILTERS[i].equals(selectedFilter), accent);
        }
    }

    private void applyChipStyle(TextView chip, boolean active, int accent) {
        float dp = getResources().getDisplayMetrics().density;
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(50 * dp);
        if (active) {
            bg.setColor(accent);
            chip.setBackground(bg);
            chip.setTextColor(0xFFFFFFFF);
        } else {
            boolean dark = (requireContext().getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            bg.setColor(dark ? 0xFF1E293B : 0xFFE2E8F0);
            chip.setBackground(bg);
            chip.setTextColor(dark ? 0xFF94A3B8 : 0xFF64748B);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────────────────────────────────

    /** Load stats + filtered book list. */
    private void loadData() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            int total    = db.bookDao().getBookCount(userId);
            int reviewed = db.bookDao().getReviewCount(userId);
            int favs     = db.bookDao().getFavoriteCount(userId);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                tvStatTotal.setText(String.valueOf(total));
                tvStatReviewed.setText(String.valueOf(reviewed));
                tvStatFavorites.setText(String.valueOf(favs));
            });
        });
        loadBooks();
    }

    private void loadBooks() {
        int    userId = sessionManager.getUserId();
        String query  = searchQuery;
        String filter = selectedFilter;

        executor.execute(() -> {
            List<Book> books = db.bookDao().searchAndFilterByStatus(userId, query, filter);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                adapter.setBooks(books);

                // Count label
                tvBookCount.setText(String.format(
                        getString(R.string.diary_books_count), books.size()));

                // Empty state
                boolean empty = books.isEmpty();
                rvDiary.setVisibility(empty ? View.GONE  : View.VISIBLE);
                emptyContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void toggleFavourite(Book book) {
        executor.execute(() -> {
            book.isFavorite = !book.isFavorite;
            db.bookDao().update(book);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                loadData();
            });
        });
    }
}
