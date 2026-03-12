package me.nethma.bookdiary;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.Book;
import me.nethma.bookdiary.utils.SessionManager;

/**
 * Search & Filter Fragment.
 * Supports live text search, category chips, minimum rating buttons,
 * and a "Favourites Only" toggle — all combined in real-time.
 */
public class SearchFragment extends BaseFragment {

    // ── Bundle key ────────────────────────────────────────────────────────────
    private static final String ARG_QUERY = "initial_query";

    // ── Factory method (call from HomeFragment filter button) ─────────────────
    public static SearchFragment newInstance(String initialQuery) {
        SearchFragment f = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, initialQuery == null ? "" : initialQuery);
        f.setArguments(args);
        return f;
    }

    // ── Views ─────────────────────────────────────────────────────────────────
    private EditText       etSearch;
    private LinearLayout   chipContainer;
    private LinearLayout   ratingBtn1, ratingBtn2, ratingBtn3, ratingBtn4, ratingBtn5;
    private SwitchCompat   switchFavorites;
    private TextView       tvResultsHeader;
    private RecyclerView   rvResults;
    private View           emptyContainer;

    // ── Adapter ───────────────────────────────────────────────────────────────
    private SearchResultAdapter adapter;

    // ── Filter state ──────────────────────────────────────────────────────────
    private String  searchQuery      = "";
    private String  selectedCategory = "All";
    private float   minRating        = 0f;   // 0 = no filter
    private boolean favoritesOnly    = false;

    private static final String[] CATEGORIES =
            {"All", "Fiction", "Science", "Mystery", "History"};
    private static final float[]  RATINGS    = {1f, 2f, 3f, 4f, 5f};

    // ── Threading ─────────────────────────────────────────────────────────────
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ── Dependencies ──────────────────────────────────────────────────────────
    private SessionManager sessionManager;
    private AppDatabase    db;

    // ─────────────────────────────────────────────────────────────────────────
    // Fragment lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        sessionManager = new SessionManager(requireContext());
        db             = AppDatabase.getInstance(requireContext());

        // ── Bind views ────────────────────────────────────────────────────────
        etSearch        = view.findViewById(R.id.et_search);
        chipContainer   = view.findViewById(R.id.chip_container);
        ratingBtn1      = view.findViewById(R.id.rating_btn_1);
        ratingBtn2      = view.findViewById(R.id.rating_btn_2);
        ratingBtn3      = view.findViewById(R.id.rating_btn_3);
        ratingBtn4      = view.findViewById(R.id.rating_btn_4);
        ratingBtn5      = view.findViewById(R.id.rating_btn_5);
        switchFavorites = view.findViewById(R.id.switch_favorites);
        tvResultsHeader = view.findViewById(R.id.tv_results_header);
        rvResults       = view.findViewById(R.id.rv_results);
        emptyContainer  = view.findViewById(R.id.empty_container);

        // ── Adapter / RecyclerView ────────────────────────────────────────────
        adapter = new SearchResultAdapter(new SearchResultAdapter.OnBookActionListener() {
            @Override public void onBookClick(Book book)      { openBookDetail(book); }
            @Override public void onBookmarkToggle(Book book) { toggleFavorite(book); }
        });
        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResults.setAdapter(adapter);
        rvResults.setNestedScrollingEnabled(false);

        // ── Back button ───────────────────────────────────────────────────────
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().onBackPressed());

        // ── Pre-fill query from arguments (e.g. from HomeFragment filter btn) ─
        if (getArguments() != null) {
            String initial = getArguments().getString(ARG_QUERY, "");
            if (!initial.isEmpty()) {
                searchQuery = initial;
                etSearch.setText(initial);
                etSearch.setSelection(initial.length());
            }
        }

        // ── Search bar ────────────────────────────────────────────────────────
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c)     {}
            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                applyFilters();
            }
        });

        // ── Category chips ────────────────────────────────────────────────────
        buildCategoryChips();

        // ── Rating buttons ────────────────────────────────────────────────────
        setupRatingButtons();

        // ── Favourites toggle ─────────────────────────────────────────────────
        switchFavorites.setOnCheckedChangeListener((btn, checked) -> {
            favoritesOnly = checked;
            applyFilters();
        });

        // ── Initial load ──────────────────────────────────────────────────────
        applyFilters();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        applyFilters();
        refreshChipStyles();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Category chips
    // ─────────────────────────────────────────────────────────────────────────

    private void buildCategoryChips() {
        chipContainer.removeAllViews();
        int accent = accentColor();
        for (String cat : CATEGORIES) {
            TextView chip = (TextView) LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_category_chip, chipContainer, false);
            chip.setText(cat);
            applyChipStyle(chip, cat.equals(selectedCategory), accent);
            chip.setOnClickListener(v -> {
                selectedCategory = cat;
                refreshChipStyles();
                applyFilters();
            });
            chipContainer.addView(chip);
        }
    }

    private void refreshChipStyles() {
        if (chipContainer == null) return;
        int accent = accentColor();
        for (int i = 0; i < chipContainer.getChildCount(); i++) {
            applyChipStyle((TextView) chipContainer.getChildAt(i),
                    CATEGORIES[i].equals(selectedCategory), accent);
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
    // Rating buttons
    // ─────────────────────────────────────────────────────────────────────────

    private LinearLayout[] ratingButtons() {
        return new LinearLayout[]{ratingBtn1, ratingBtn2, ratingBtn3, ratingBtn4, ratingBtn5};
    }

    private void setupRatingButtons() {
        LinearLayout[] btns = ratingButtons();
        for (int i = 0; i < btns.length; i++) {
            final float rating = RATINGS[i];
            btns[i].setOnClickListener(v -> {
                // Tapping the already-selected rating clears the filter
                minRating = (minRating == rating) ? 0f : rating;
                refreshRatingButtons();
                applyFilters();
            });
        }
        refreshRatingButtons(); // set initial state (no selection)
    }

    private void refreshRatingButtons() {
        LinearLayout[] btns = ratingButtons();
        boolean dark = (requireContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        for (int i = 0; i < btns.length; i++) {
            boolean selected = (minRating == RATINGS[i]);
            if (selected) {
                btns[i].setBackground(
                        requireContext().getDrawable(R.drawable.bg_rating_btn_selected));
            } else {
                // Clear to transparent (inside the container background)
                btns[i].setBackgroundResource(android.R.color.transparent);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data loading & filtering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Queries Room (text + category), then in-memory filters for rating
     * and favourites-only. Updates the RecyclerView and result count label.
     */
    private void applyFilters() {
        int    userId   = sessionManager.getUserId();
        String query    = searchQuery;
        String category = selectedCategory;
        float  minRat   = minRating;
        boolean favOnly = favoritesOnly;

        executor.execute(() -> {
            // Room query: text search + category
            List<Book> raw = db.bookDao().searchAndFilter(userId, query, category);

            // In-memory: rating + favourites filters
            List<Book> filtered = new ArrayList<>();
            for (Book b : raw) {
                if (minRat > 0 && b.rating < minRat) continue;
                if (favOnly && !b.isFavorite)         continue;
                filtered.add(b);
            }

            final List<Book> result = filtered;
            mainHandler.post(() -> {
                if (!isAdded()) return;
                adapter.setBooks(result);

                // Update results header
                String header = getString(R.string.search_results_header)
                        + " (" + result.size() + ")";
                tvResultsHeader.setText(header);

                // Show empty state if needed
                boolean empty = result.isEmpty();
                rvResults.setVisibility(empty ? View.GONE : View.VISIBLE);
                emptyContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
            });
        });
    }

    /** Toggle isFavorite on a book and refresh results. */
    private void toggleFavorite(Book book) {
        executor.execute(() -> {
            book.isFavorite = !book.isFavorite;
            db.bookDao().update(book);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                applyFilters();
            });
        });
    }

    private void openBookDetail(Book book) {
        // TODO: launch BookDetailActivity
        Toast.makeText(requireContext(), book.title, Toast.LENGTH_SHORT).show();
    }
}
