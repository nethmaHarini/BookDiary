package me.nethma.bookdiary;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.api.BookApiClient;
import me.nethma.bookdiary.api.OpenLibraryWorkDetail;
import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.Book;
import me.nethma.bookdiary.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows full details of a book discovered from the Open Library API.
 * "Add to Library" actually inserts the book into the local Room DB,
 * then opens the existing BookDetailActivity for all local functions
 * (favourite, review, reading status, delete, share, etc.)
 */
public class ApiBookDetailActivity extends BaseActivity {

    public static final String EXTRA_WORK_KEY  = "api_work_key";
    public static final String EXTRA_TITLE     = "api_title";
    public static final String EXTRA_AUTHOR    = "api_author";
    public static final String EXTRA_COVER_URL = "api_cover_url";
    public static final String EXTRA_YEAR      = "api_year";
    public static final String EXTRA_RATING    = "api_rating";
    public static final String EXTRA_CATEGORY  = "api_category";

    private ImageView   ivCover;
    private TextView    tvTitle, tvAuthor, tvYear, tvDescription, tvRatingBadge;
    private ProgressBar pbLoading;
    private ScrollView  scrollContent;
    private Button      btnAddToLibrary;
    private View        subjectsSection;
    private ChipGroup   chipGroupSubjects;

    private String workKey, title, author, coverUrl, category;
    private int    year;
    private float  rating;

    // Fetched description & subjects from works API
    private String  fetchedDescription = null;
    private List<String> fetchedSubjects = null;

    private final Handler         mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private SessionManager        sessionManager;
    private AppDatabase           db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_api_book_detail);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(android.R.id.content), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        sessionManager = new SessionManager(this);
        db             = AppDatabase.getInstance(this);

        // Bind views
        ivCover           = findViewById(R.id.iv_cover);
        tvTitle           = findViewById(R.id.tv_book_title);
        tvAuthor          = findViewById(R.id.tv_author);
        tvYear            = findViewById(R.id.tv_year);
        tvRatingBadge     = findViewById(R.id.tv_rating_badge);
        tvDescription     = findViewById(R.id.tv_description);
        pbLoading         = findViewById(R.id.pb_loading);
        scrollContent     = findViewById(R.id.scroll_content);
        btnAddToLibrary   = findViewById(R.id.btn_add_to_library);
        subjectsSection   = findViewById(R.id.subjects_section);
        chipGroupSubjects = findViewById(R.id.chip_group_subjects);

        // Read intent extras
        workKey  = getIntent().getStringExtra(EXTRA_WORK_KEY);
        title    = getIntent().getStringExtra(EXTRA_TITLE);
        author   = getIntent().getStringExtra(EXTRA_AUTHOR);
        coverUrl = getIntent().getStringExtra(EXTRA_COVER_URL);
        year     = getIntent().getIntExtra(EXTRA_YEAR, 0);
        rating   = getIntent().getFloatExtra(EXTRA_RATING, 0f);
        category = getIntent().getStringExtra(EXTRA_CATEGORY);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareBook());
        btnAddToLibrary.setOnClickListener(v -> addToLibraryAndOpen());

        populateBasicInfo();

        if (workKey != null && !workKey.isEmpty()) {
            loadWorkDetail(workKey);
        } else {
            showContent();
        }
    }

    private void populateBasicInfo() {
        tvTitle.setText(title != null ? title : "");
        tvAuthor.setText(author != null ? author : "");

        if (year > 0) {
            tvYear.setText(String.valueOf(year));
            tvYear.setVisibility(View.VISIBLE);
        } else {
            tvYear.setVisibility(View.GONE);
        }

        if (rating > 0) {
            tvRatingBadge.setText("★ " + String.format(Locale.US, "%.1f", rating));
            tvRatingBadge.setVisibility(View.VISIBLE);
        } else {
            tvRatingBadge.setVisibility(View.GONE);
        }

        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_book_logo)
                            .error(R.drawable.ic_book_logo)
                            .transform(new RoundedCorners(16)))
                    .into(ivCover);
        } else {
            ivCover.setImageResource(R.drawable.ic_book_logo);
        }
    }

    private void loadWorkDetail(String key) {
        String workId = key.replace("/works/", "");
        BookApiClient.getService(this).getWorkDetail(workId)
                .enqueue(new Callback<OpenLibraryWorkDetail>() {
                    @Override
                    public void onResponse(Call<OpenLibraryWorkDetail> call,
                                           Response<OpenLibraryWorkDetail> response) {
                        mainHandler.post(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                bindWorkDetail(response.body());
                            } else {
                                tvDescription.setText("No description available for this book.");
                            }
                            showContent();
                        });
                    }

                    @Override
                    public void onFailure(Call<OpenLibraryWorkDetail> call, Throwable t) {
                        mainHandler.post(() -> {
                            tvDescription.setText("Could not load description. Check your connection.");
                            showContent();
                        });
                    }
                });
    }

    private void bindWorkDetail(OpenLibraryWorkDetail detail) {
        // Upgrade to large cover if the works API has a better one
        String largeCover = detail.getCoverUrl();
        if (largeCover != null) {
            coverUrl = largeCover; // keep for "add to library"
            Glide.with(this)
                    .load(largeCover)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_book_logo)
                            .error(R.drawable.ic_book_logo)
                            .transform(new RoundedCorners(16)))
                    .into(ivCover);
        }

        // Description
        fetchedDescription = detail.getDescription();
        if (fetchedDescription != null && !fetchedDescription.isEmpty()) {
            tvDescription.setText(fetchedDescription);
        } else {
            tvDescription.setText("No description available for this book.");
        }

        // Publish date fallback
        if (year == 0 && detail.firstPublishDate != null && !detail.firstPublishDate.isEmpty()) {
            tvYear.setText(detail.firstPublishDate);
            tvYear.setVisibility(View.VISIBLE);
        }

        // Subjects chips
        fetchedSubjects = detail.subjects;
        if (fetchedSubjects != null && !fetchedSubjects.isEmpty()) {
            subjectsSection.setVisibility(View.VISIBLE);
            chipGroupSubjects.removeAllViews();
            int limit = Math.min(fetchedSubjects.size(), 10);
            for (int i = 0; i < limit; i++) {
                Chip chip = new Chip(this);
                chip.setText(fetchedSubjects.get(i));
                chip.setClickable(false);
                chip.setFocusable(false);
                chipGroupSubjects.addView(chip);
            }
            // Use first subject as category if we don't have one
            if (category == null || category.isEmpty()) {
                category = fetchedSubjects.get(0);
            }
        }
    }

    private void showContent() {
        pbLoading.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);
    }

    /** Insert book into local DB, then open BookDetailActivity for full local UI */
    private void addToLibraryAndOpen() {
        btnAddToLibrary.setEnabled(false);
        btnAddToLibrary.setText("Adding…");

        int userId = sessionManager.getUserId();

        executor.execute(() -> {
            // Check if this book is already in library (by title + author)
            List<Book> existing = db.bookDao().getAllBooks(userId);
            for (Book b : existing) {
                if (b.title != null && b.title.equalsIgnoreCase(title)
                        && b.author != null && b.author.equalsIgnoreCase(author)) {
                    // Already in library — just open it
                    mainHandler.post(() -> {
                        Toast.makeText(this,
                                "Already in your library!", Toast.LENGTH_SHORT).show();
                        openBookDetail((int) b.id);
                    });
                    return;
                }
            }

            // Build the Book entity from API data
            Book book         = new Book();
            book.userId       = userId;
            book.title        = title != null ? title : "";
            book.author       = author != null ? author : "";
            book.category     = (category != null && !category.isEmpty()) ? category : "Fiction";
            book.rating       = rating > 0 ? Math.min(rating, 5f) : 0f;
            book.coverUrl     = coverUrl;   // remote URL — Glide handles loading
            book.notes        = fetchedDescription;
            book.isFavorite   = false;
            book.readingStatus = "Want to Read";
            book.dateAdded    = System.currentTimeMillis();

            long newId = db.bookDao().insert(book);

            mainHandler.post(() -> {
                Toast.makeText(this,
                        "\"" + title + "\" added to your library!",
                        Toast.LENGTH_SHORT).show();
                openBookDetail((int) newId);
            });
        });
    }

    /** Open the existing BookDetailActivity (full local functions) */
    private void openBookDetail(int bookId) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, bookId);
        startActivity(intent);
        finish(); // close this preview screen
    }

    private void shareBook() {
        String text = "📖 " + (title != null ? title : "")
                + "\n✍️ " + (author != null ? author : "")
                + (year > 0 ? "\n📅 " + year : "")
                + (rating > 0 ? "\n★ " + String.format(Locale.US, "%.1f", rating) : "")
                + "\n\nDiscovered on Book Diary";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, "Share book"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
