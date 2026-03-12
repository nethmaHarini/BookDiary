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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.api.DiscoverBooksRepository;
import me.nethma.bookdiary.api.OpenLibraryBook;
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
    private RecyclerView       rvDiscover;
    private EditText           etSearch;
    private LinearLayout       chipContainer;
    private View               emptyFavoritesContainer;
    private View               emptyBooksContainer;
    private View               discoverHeader;
    private TextView           tvDiscoverSubtitle;
    private ProgressBar        pbDiscover;

    // ── Adapters ──────────────────────────────────────────────────────────────
    private FavoriteBookAdapter   favoriteAdapter;
    private AllBooksAdapter       allBooksAdapter;
    private DiscoverBookAdapter   discoverAdapter;

    // ── State ─────────────────────────────────────────────────────────────────
    private String selectedCategory = "All";
    private String searchQuery      = "";
    private String[] categories     = {"All", "Fiction", "Science", "Mystery", "History"};

    // ── Threading ─────────────────────────────────────────────────────────────
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ── Dependencies ──────────────────────────────────────────────────────────
    private SessionManager           sessionManager;
    private AppDatabase              db;
    private DiscoverBooksRepository  discoverRepo;

    // ── Launcher: refresh list when returning from BookDetailActivity ─────────
    private ActivityResultLauncher<Intent> detailLauncher;
    // ── Launcher: refresh list when returning from FavouritesActivity ────────
    private ActivityResultLauncher<Intent> favsLauncher;

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
        discoverRepo = new DiscoverBooksRepository(requireContext());

        // Build dynamic categories from selected topics
        List<String> topics = sessionManager.getSelectedTopics();
        if (!topics.isEmpty()) {
            String[] dynamic = new String[topics.size() + 1];
            dynamic[0] = "All";
            for (int i = 0; i < topics.size(); i++) dynamic[i + 1] = topics.get(i);
            categories = dynamic;
        }

        // Register result launchers before any possible start
        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) loadBooks();
                });
        favsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) loadBooks();
                });

        // Bind views
        rvFavorites             = view.findViewById(R.id.rv_favorites);
        rvAllBooks              = view.findViewById(R.id.rv_all_books);
        rvDiscover              = view.findViewById(R.id.rv_discover);
        etSearch                = view.findViewById(R.id.et_search);
        chipContainer           = view.findViewById(R.id.chip_container);
        emptyFavoritesContainer = view.findViewById(R.id.empty_favorites_container);
        emptyBooksContainer     = view.findViewById(R.id.empty_books_container);
        discoverHeader          = view.findViewById(R.id.discover_header);
        tvDiscoverSubtitle      = view.findViewById(R.id.tv_discover_subtitle);
        pbDiscover              = view.findViewById(R.id.pb_discover);

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

        // Discover RecyclerView (horizontal)
        discoverAdapter = new DiscoverBookAdapter(book ->
                Toast.makeText(requireContext(),
                        "\"" + book.title + "\" — tap Add to add it to your library",
                        Toast.LENGTH_SHORT).show());
        rvDiscover.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvDiscover.setAdapter(discoverAdapter);

        // Header buttons
        view.findViewById(R.id.btn_notifications).setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Notifications coming soon!", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_filter).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSearch(searchQuery);
            }
        });

        // "See all" → FavouritesActivity
        view.findViewById(R.id.tv_see_all).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), FavouritesActivity.class);
            favsLauncher.launch(i);
        });

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

        // Load local books (no seeding — real API books are in discover section)
        loadBooks();

        // Load discover books from API
        loadDiscoverBooks();

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
        for (String cat : categories) {
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
            applyChipStyle(chip, categories[i].equals(selectedCategory), accent);
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
        Intent intent = new Intent(requireContext(), BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.id);
        detailLauncher.launch(intent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Discover Books (Open Library API)
    // ─────────────────────────────────────────────────────────────────────────

    /** Fetch books from Open Library based on the user's selected topics. */
    private void loadDiscoverBooks() {
        if (!isAdded()) return;
        List<String> topics = sessionManager.getSelectedTopics();
        if (topics.isEmpty()) {
            discoverHeader.setVisibility(View.GONE);
            rvDiscover.setVisibility(View.GONE);
            return;
        }

        // Show loading
        discoverHeader.setVisibility(View.VISIBLE);
        pbDiscover.setVisibility(View.VISIBLE);
        rvDiscover.setVisibility(View.GONE);

        // Build subtitle with selected topics
        StringBuilder sb = new StringBuilder("Based on: ");
        for (int i = 0; i < topics.size(); i++) {
            sb.append(topics.get(i));
            if (i < topics.size() - 1) sb.append(", ");
        }
        tvDiscoverSubtitle.setText(sb.toString());

        discoverRepo.fetchBooksForTopics(topics, new DiscoverBooksRepository.BooksCallback() {
            @Override
            public void onSuccess(List<OpenLibraryBook> books) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    pbDiscover.setVisibility(View.GONE);
                    if (!books.isEmpty()) {
                        discoverAdapter.setBooks(books);
                        rvDiscover.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    pbDiscover.setVisibility(View.GONE);
                });
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static long days(int n) {
        return (long) n * 24 * 60 * 60 * 1000;
    }
}
