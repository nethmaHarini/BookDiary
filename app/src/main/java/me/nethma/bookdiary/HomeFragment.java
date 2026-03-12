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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.Book;
import me.nethma.bookdiary.utils.SessionManager;

/**
 * Home Dashboard Fragment — displays favourite books (horizontal scroll)
 * and all books (vertical list) with live search + category filtering.
 */
public class HomeFragment extends BaseFragment {

    // ── Views ─────────────────────────────────────────────────────────────────
    private RecyclerView       rvFavorites;
    private RecyclerView       rvAllBooks;
    private EditText           etSearch;
    private LinearLayout       chipContainer;
    private View               emptyFavoritesContainer;
    private View               emptyBooksContainer;

    // ── Adapters ──────────────────────────────────────────────────────────────
    private FavoriteBookAdapter favoriteAdapter;
    private AllBooksAdapter     allBooksAdapter;

    // ── State ─────────────────────────────────────────────────────────────────
    private String selectedCategory = "All";
    private String searchQuery      = "";
    private static final String[] CATEGORIES =
            {"All", "Fiction", "Science", "Mystery", "History"};

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

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sessionManager = new SessionManager(requireContext());
        db = AppDatabase.getInstance(requireContext());

        // Bind views
        rvFavorites             = view.findViewById(R.id.rv_favorites);
        rvAllBooks              = view.findViewById(R.id.rv_all_books);
        etSearch                = view.findViewById(R.id.et_search);
        chipContainer           = view.findViewById(R.id.chip_container);
        emptyFavoritesContainer = view.findViewById(R.id.empty_favorites_container);
        emptyBooksContainer     = view.findViewById(R.id.empty_books_container);

        // Set up adapters
        favoriteAdapter = new FavoriteBookAdapter(new FavoriteBookAdapter.OnBookClickListener() {
            @Override public void onBookClick(Book book)      { openBookDetail(book); }
            @Override public void onFavoriteToggle(Book book) { toggleFavorite(book); }
        });

        allBooksAdapter = new AllBooksAdapter(new AllBooksAdapter.OnBookClickListener() {
            @Override public void onBookClick(Book book)      { openBookDetail(book); }
            @Override public void onFavoriteToggle(Book book) { toggleFavorite(book); }
        });

        rvFavorites.setLayoutManager(
                new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.HORIZONTAL, false));
        rvFavorites.setAdapter(favoriteAdapter);

        rvAllBooks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAllBooks.setAdapter(allBooksAdapter);
        rvAllBooks.setNestedScrollingEnabled(false);

        // Header buttons
        view.findViewById(R.id.btn_notifications).setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Notifications coming soon!", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_filter).setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Advanced filter coming soon!", Toast.LENGTH_SHORT).show());

        // "See all" → Favourites screen
        view.findViewById(R.id.tv_see_all).setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Favourites screen coming soon!", Toast.LENGTH_SHORT).show());

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                loadBooks();
            }
        });

        // Category chips
        buildCategoryChips();

        // Seed demo data then load
        seedSampleDataThenLoad();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();   // BaseFragment applies accent colour
        loadBooks();
        // Refresh chip styles with potentially updated accent colour
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
                loadBooks();
            });
            chipContainer.addView(chip);
        }
    }

    private void refreshChipStyles() {
        if (chipContainer == null) return;
        int accent = accentColor();
        for (int i = 0; i < chipContainer.getChildCount(); i++) {
            TextView chip = (TextView) chipContainer.getChildAt(i);
            applyChipStyle(chip, CATEGORIES[i].equals(selectedCategory), accent);
        }
    }

    private void applyChipStyle(TextView chip, boolean active, int accent) {
        float density = getResources().getDisplayMetrics().density;
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(50 * density);

        if (active) {
            bg.setColor(accent);
            chip.setBackground(bg);
            chip.setTextColor(0xFFFFFFFF);
        } else {
            boolean isDark = (requireContext().getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            bg.setColor(isDark ? 0xFF1E293B : 0xFFE2E8F0);
            chip.setBackground(bg);
            chip.setTextColor(isDark ? 0xFF94A3B8 : 0xFF64748B);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────────────────────────────────

    /** Load (and filter) books from the database on a background thread. */
    private void loadBooks() {
        int    userId   = sessionManager.getUserId();
        String query    = searchQuery;
        String category = selectedCategory;

        executor.execute(() -> {
            List<Book> allBooks  = db.bookDao().searchAndFilter(userId, query, category);
            List<Book> favorites = db.bookDao().getFavoriteBooks(userId);

            mainHandler.post(() -> {
                if (!isAdded()) return;

                // Favourites section
                favoriteAdapter.setBooks(favorites);
                boolean hasFavorites = !favorites.isEmpty();
                rvFavorites.setVisibility(
                        hasFavorites ? View.VISIBLE : View.GONE);
                emptyFavoritesContainer.setVisibility(
                        hasFavorites ? View.GONE : View.VISIBLE);

                // All books section
                allBooksAdapter.setBooks(allBooks);
                boolean hasBooks = !allBooks.isEmpty();
                rvAllBooks.setVisibility(
                        hasBooks ? View.VISIBLE : View.GONE);
                emptyBooksContainer.setVisibility(
                        hasBooks ? View.GONE : View.VISIBLE);
            });
        });
    }

    /** Toggle the isFavorite flag for a book and refresh the UI. */
    private void toggleFavorite(Book book) {
        executor.execute(() -> {
            book.isFavorite = !book.isFavorite;
            db.bookDao().update(book);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                loadBooks();
            });
        });
    }

    private void openBookDetail(Book book) {
        // TODO: launch BookDetailActivity, passing book.id
        Toast.makeText(requireContext(), book.title, Toast.LENGTH_SHORT).show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sample data seeding (first launch only)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Seeds a handful of demo books when the user has no books yet.
     * Called once on first launch so the Home screen is never blank.
     */
    private void seedSampleDataThenLoad() {
        int userId = sessionManager.getUserId();

        executor.execute(() -> {
            if (db.bookDao().getBookCount(userId) == 0) {
                long now = System.currentTimeMillis();

                db.bookDao().insert(book(userId, "The Great Gatsby",
                        "F. Scott Fitzgerald", "Fiction",  4.8f, true,  now - days(7)));
                db.bookDao().insert(book(userId, "Midnight Library",
                        "Matt Haig",          "Fiction",  4.5f, true,  now - days(5)));
                db.bookDao().insert(book(userId, "Circe",
                        "Madeline Miller",    "Fiction",  4.9f, true,  now - days(3)));
                db.bookDao().insert(book(userId, "Project Hail Mary",
                        "Andy Weir",          "Science",  4.0f, false, now - days(2)));
                db.bookDao().insert(book(userId, "Norwegian Wood",
                        "Haruki Murakami",    "Fiction",  5.0f, false, now - days(1)));
                db.bookDao().insert(book(userId, "The Alchemist",
                        "Paulo Coelho",       "History",  4.3f, true,  now));
            }
            mainHandler.post(() -> {
                if (isAdded()) loadBooks();
            });
        });
    }

    /** Helper: create a Book entity with all required fields. */
    private static Book book(int userId, String title, String author,
                             String category, float rating,
                             boolean fav, long dateAdded) {
        Book b = new Book();
        b.userId    = userId;
        b.title     = title;
        b.author    = author;
        b.category  = category;
        b.rating    = rating;
        b.isFavorite = fav;
        b.dateAdded = dateAdded;
        return b;
    }

    private static long days(int n) {
        return (long) n * 24 * 60 * 60 * 1000;
    }
}
