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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.Book;
import me.nethma.bookdiary.utils.SessionManager;
import me.nethma.bookdiary.utils.ThemePrefsManager;

/**
 * Full-screen Favourites activity.
 *  • Tap card   → BookDetailActivity
 *  • Tap ❤ icon → remove from favourites (with Snackbar Undo)
 *  • Search bar + status filter chips
 * Accessible from: Home "See All" / Profile "My Favourites" / Home header ❤ button.
 */
public class FavouritesActivity extends BaseActivity {

    // ── Filter values ─────────────────────────────────────────────────────────
    private static final String[] FILTERS = {
            "All Books", "Currently Reading", "Want to Read", "Finished"
    };
    private static final String[] FILTER_LABELS = {
            "All", "Reading", "Want to Read", "Finished"
    };

    // ── Views ─────────────────────────────────────────────────────────────────
    private View         rootView;
    private LinearLayout chipContainer;
    private TextView     tvCount, tvHeaderCount;
    private RecyclerView rvFavourites;
    private View         emptyContainer;

    // ── Adapter ───────────────────────────────────────────────────────────────
    private FavouritesCardAdapter adapter;

    // ── State ─────────────────────────────────────────────────────────────────
    private String searchQuery    = "";
    private String selectedFilter = "All Books";

    // ── Threading ─────────────────────────────────────────────────────────────
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ── Dependencies ──────────────────────────────────────────────────────────
    private SessionManager sessionManager;
    private AppDatabase    db;

    // ── Launcher: refresh list on return from Book Detail ─────────────────────
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            loadFavourites();
                            setResult(RESULT_OK);
                        }
                    });

    // ── Launcher: refresh list on return from Edit Book ───────────────────────
    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            loadFavourites();
                            setResult(RESULT_OK);
                        }
                    });

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favourites);

        sessionManager = new SessionManager(this);
        db             = AppDatabase.getInstance(this);

        rootView       = findViewById(android.R.id.content);
        chipContainer  = findViewById(R.id.chip_container);
        tvCount        = findViewById(R.id.tv_count);
        tvHeaderCount  = findViewById(R.id.tv_header_count);
        rvFavourites   = findViewById(R.id.rv_favourites);
        emptyContainer = findViewById(R.id.empty_container);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Adapter
        adapter = new FavouritesCardAdapter(new FavouritesCardAdapter.OnBookActionListener() {
            @Override
            public void onBookClick(Book book) {
                Intent i = new Intent(FavouritesActivity.this, BookDetailActivity.class);
                i.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.id);
                detailLauncher.launch(i);
            }

            @Override
            public void onUnfavourite(Book book) {
                removeFromFavourites(book);
            }

            @Override
            public void onEditClick(Book book) {
                Intent i = new Intent(FavouritesActivity.this, EditBookActivity.class);
                i.putExtra("book_id", book.id);
                editLauncher.launch(i);
            }
        });

        rvFavourites.setLayoutManager(new LinearLayoutManager(this));
        rvFavourites.setAdapter(adapter);
        rvFavourites.setNestedScrollingEnabled(false);

        // Search
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                loadFavourites();
            }
        });

        buildFilterChips();
        loadFavourites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChipStyles();
        loadFavourites();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Remove from favourites with Snackbar Undo
    // ─────────────────────────────────────────────────────────────────────────

    private void removeFromFavourites(Book book) {
        // Optimistically remove from list immediately
        book.isFavorite = false;
        executor.execute(() -> db.bookDao().update(book));
        setResult(RESULT_OK); // tell caller to refresh

        // Refresh list
        loadFavourites();

        // Snackbar with Undo
        Snackbar.make(rootView,
                        "\"" + book.title + "\" removed from Favourites",
                        Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    // Re-add to favourites
                    book.isFavorite = true;
                    executor.execute(() -> {
                        db.bookDao().update(book);
                        mainHandler.post(this::loadFavourites);
                    });
                })
                .setActionTextColor(ThemePrefsManager.getAccentColor(this))
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Filter chips
    // ─────────────────────────────────────────────────────────────────────────

    private void buildFilterChips() {
        chipContainer.removeAllViews();
        int accent = ThemePrefsManager.getAccentColor(this);
        for (int i = 0; i < FILTERS.length; i++) {
            TextView chip = (TextView) LayoutInflater.from(this)
                    .inflate(R.layout.item_category_chip, chipContainer, false);
            chip.setText(FILTER_LABELS[i]);
            applyChipStyle(chip, FILTERS[i].equals(selectedFilter), accent);
            final String filter = FILTERS[i];
            chip.setOnClickListener(v -> {
                selectedFilter = filter;
                refreshChipStyles();
                loadFavourites();
            });
            chipContainer.addView(chip);
        }
    }

    private void refreshChipStyles() {
        if (chipContainer == null) return;
        int accent = ThemePrefsManager.getAccentColor(this);
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
            boolean dark = (getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            bg.setColor(dark ? 0xFF1E293B : 0xFFE2E8F0);
            chip.setBackground(bg);
            chip.setTextColor(dark ? 0xFF94A3B8 : 0xFF64748B);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────────────────────────────────

    private void loadFavourites() {
        int    userId = sessionManager.getUserId();
        String query  = searchQuery;
        String filter = selectedFilter;

        executor.execute(() -> {
            List<Book> books = db.bookDao().searchFavourites(userId, query, filter);
            mainHandler.post(() -> {
                adapter.setBooks(books);

                String countText = getString(R.string.favs_count, books.size());
                tvCount.setText(countText);
                tvHeaderCount.setText(String.valueOf(books.size()));

                boolean empty = books.isEmpty();
                rvFavourites.setVisibility(empty ? View.GONE  : View.VISIBLE);
                emptyContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
