package me.nethma.bookdiary;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.Book;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "book_id";

    // ── Views ──────────────────────────────────────────────────────────────
    private ImageView ivCover;
    private TextView  tvBookTitle, tvAuthor, tvRating, tvCategory, tvStatus;
    private ImageView rvStar1, rvStar2, rvStar3, rvStar4, rvStar5;
    private TextView  tvReview, tvReviewEmpty;
    private ImageView ivFavIcon;
    private TextView  tvFavLabel;

    // ── State ──────────────────────────────────────────────────────────────
    private int  bookId = -1;
    private Book currentBook = null;

    // ── Threading ─────────────────────────────────────────────────────────
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ── Launch EditBookActivity and refresh on return ──────────────────────
    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadBook(bookId);           // refresh detail
                            setResult(RESULT_OK);       // propagate up
                        }
                    });

    // ─────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_detail);

        // Bind views
        ivCover       = findViewById(R.id.iv_cover);
        tvBookTitle   = findViewById(R.id.tv_book_title);
        tvAuthor      = findViewById(R.id.tv_author);
        tvRating      = findViewById(R.id.tv_rating);
        tvCategory    = findViewById(R.id.tv_category);
        tvStatus      = findViewById(R.id.tv_status);
        rvStar1       = findViewById(R.id.rv_star_1);
        rvStar2       = findViewById(R.id.rv_star_2);
        rvStar3       = findViewById(R.id.rv_star_3);
        rvStar4       = findViewById(R.id.rv_star_4);
        rvStar5       = findViewById(R.id.rv_star_5);
        tvReview      = findViewById(R.id.tv_review);
        tvReviewEmpty = findViewById(R.id.tv_review_empty);
        ivFavIcon     = findViewById(R.id.iv_fav_icon);
        tvFavLabel    = findViewById(R.id.tv_fav_label);

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Edit button
        findViewById(R.id.btn_edit).setOnClickListener(v -> openEdit());

        // "Edit" next to My Review
        findViewById(R.id.btn_edit_review).setOnClickListener(v -> openEdit());

        // Share
        findViewById(R.id.btn_share).setOnClickListener(v -> shareBook());

        // More options
        findViewById(R.id.btn_more).setOnClickListener(this::showMoreMenu);

        // Read Now
        findViewById(R.id.btn_read_now).setOnClickListener(v -> markAsReading());

        // Favourite toggle row
        findViewById(R.id.btn_favourite).setOnClickListener(v -> toggleFavourite());

        // Ratings & Reviews row
        findViewById(R.id.btn_ratings_reviews).setOnClickListener(v -> openRatingsReviews());

        // Load book
        bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, -1);
        if (bookId != -1) loadBook(bookId);
        else finish();
    }

    // ── Data loading ───────────────────────────────────────────────────────

    private void loadBook(int id) {
        executor.execute(() -> {
            Book book = AppDatabase.getInstance(this).bookDao().getBookById(id);
            mainHandler.post(() -> {
                if (book == null) { finish(); return; }
                currentBook = book;
                populateUI(book);
            });
        });
    }

    private void populateUI(Book book) {
        // Title & Author
        tvBookTitle.setText(book.title);
        tvAuthor.setText(book.author);

        // Stats
        tvRating.setText(String.format(Locale.US, "%.1f", book.rating));
        tvCategory.setText(book.category != null ? book.category : "—");
        tvStatus.setText(shortStatus(book.readingStatus));

        // Review stars
        renderStars(Math.round(book.rating));

        // Review text
        boolean hasReview = book.notes != null && !book.notes.trim().isEmpty();
        tvReview.setVisibility(hasReview ? View.VISIBLE : View.GONE);
        tvReviewEmpty.setVisibility(hasReview ? View.GONE : View.VISIBLE);
        if (hasReview) {
            tvReview.setText("\u201C" + book.notes.trim() + "\u201D");
        }

        // Favourite row
        updateFavouriteRow(book.isFavorite);

        // Cover image (async)
        loadCover(book);
    }

    // ── Cover image loading ────────────────────────────────────────────────

    private void loadCover(Book book) {
        // Coloured placeholder based on title hash
        int[] colors = {0xFF6366F1, 0xFF8B5CF6, 0xFF06B6D4,
                        0xFF10B981, 0xFFF59E0B, 0xFFEF4444, 0xFFEC4899};
        int c = colors[Math.abs(book.title.hashCode()) % colors.length];

        android.graphics.drawable.GradientDrawable ph =
                new android.graphics.drawable.GradientDrawable();
        ph.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        ph.setCornerRadius(24f);
        ph.setColor(c);
        ivCover.setBackground(ph);
        ivCover.setImageTintList(ColorStateList.valueOf(0x80FFFFFF));
        ivCover.setImageResource(R.drawable.ic_book_logo);

        if (book.coverUrl != null && !book.coverUrl.isEmpty()) {
            // Determine if it's a remote URL or local file path
            String url = book.coverUrl.startsWith("http")
                    ? book.coverUrl
                    : "file://" + book.coverUrl;
            Glide.with(this)
                    .load(url)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_book_logo)
                            .error(R.drawable.ic_book_logo)
                            .transform(new RoundedCorners(24)))
                    .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                        @Override
                        public void onResourceReady(
                                android.graphics.drawable.Drawable resource,
                                com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
                            ivCover.setBackground(null);
                            ivCover.setImageTintList(null);
                            ivCover.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {}
                    });
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Render 5 review stars based on rating (1-5, 0 = no rating) */
    private void renderStars(int rating) {
        ImageView[] stars = {rvStar1, rvStar2, rvStar3, rvStar4, rvStar5};
        int filled = 0xFFFBBF24;
        int empty  = 0xFFCBD5E1;
        for (int i = 0; i < 5; i++) {
            stars[i].setImageTintList(
                    ColorStateList.valueOf(i < rating ? filled : empty));
        }
    }

    /** Shortened reading status for the stats bar */
    private String shortStatus(String status) {
        if (status == null) return "—";
        switch (status) {
            case "Currently Reading": return "Reading";
            case "Want to Read":      return "To Read";
            case "Finished":          return "Finished";
            default:                  return status;
        }
    }

    private void updateFavouriteRow(boolean isFav) {
        if (isFav) {
            ivFavIcon.setImageResource(R.drawable.ic_heart_filled);
            ivFavIcon.setImageTintList(null);
            tvFavLabel.setText(R.string.detail_fav_remove);
        } else {
            ivFavIcon.setImageResource(R.drawable.ic_heart);
            ivFavIcon.setImageTintList(
                    ColorStateList.valueOf(getColor(R.color.text_hint)));
            tvFavLabel.setText(R.string.detail_fav_add);
        }
    }

    // ── Actions ────────────────────────────────────────────────────────────

    private void openEdit() {
        if (currentBook == null) return;
        Intent intent = new Intent(this, EditBookActivity.class);
        intent.putExtra(EditBookActivity.EXTRA_BOOK_ID, currentBook.id);
        editLauncher.launch(intent);
    }

    private void openRatingsReviews() {
        if (currentBook == null) return;
        Intent intent = new Intent(this, RatingsReviewsActivity.class);
        intent.putExtra(RatingsReviewsActivity.EXTRA_BOOK_ID, currentBook.id);
        editLauncher.launch(intent); // reuse editLauncher so we refresh on return
    }

    private void shareBook() {
        if (currentBook == null) return;
        String text = "📖 " + currentBook.title + "\n✍️ " + currentBook.author
                + "\n⭐ " + String.format(Locale.US, "%.1f", currentBook.rating)
                + "\n\nShared from Book Diary";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, getString(R.string.detail_share)));
    }

    private void showMoreMenu(View anchor) {
        if (currentBook == null) return;
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0,
                currentBook.isFavorite ? R.string.detail_menu_unfav : R.string.detail_menu_fav);
        popup.getMenu().add(0, 2, 1, R.string.detail_menu_delete);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) { toggleFavourite(); return true; }
            if (item.getItemId() == 2) { confirmDelete(); return true; }
            return false;
        });
        popup.show();
    }

    private void toggleFavourite() {
        if (currentBook == null) return;
        currentBook.isFavorite = !currentBook.isFavorite;
        boolean nowFav = currentBook.isFavorite;
        executor.execute(() -> {
            AppDatabase.getInstance(this).bookDao().update(currentBook);
            mainHandler.post(() -> {
                updateFavouriteRow(nowFav);
                Toast.makeText(this,
                        nowFav ? getString(R.string.msg_marked_favourite)
                               : getString(R.string.msg_removed_favourite),
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // signal list to refresh
            });
        });
    }

    private void markAsReading() {
        if (currentBook == null) return;
        String newStatus = "Currently Reading";
        if (newStatus.equals(currentBook.readingStatus)) {
            newStatus = "Finished";
        }
        currentBook.readingStatus = newStatus;
        final String finalStatus = newStatus;
        executor.execute(() -> {
            AppDatabase.getInstance(this).bookDao().update(currentBook);
            mainHandler.post(() -> {
                tvStatus.setText(shortStatus(finalStatus));
                Toast.makeText(this,
                        String.format(getString(R.string.msg_status_updated), finalStatus),
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
            });
        });
    }

    private void confirmDelete() {
        if (currentBook == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.delete_confirm_yes, (d, w) -> deleteBook())
                .setNegativeButton(R.string.delete_confirm_no, null)
                .show();
    }

    private void deleteBook() {
        Book b = currentBook;
        executor.execute(() -> {
            AppDatabase.getInstance(this).bookDao().delete(b);
            mainHandler.post(() -> {
                Toast.makeText(this, R.string.msg_book_deleted, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}

