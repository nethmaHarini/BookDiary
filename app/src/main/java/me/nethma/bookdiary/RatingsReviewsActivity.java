package me.nethma.bookdiary;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.Book;
import me.nethma.bookdiary.database.Review;
import me.nethma.bookdiary.utils.SessionManager;

/**
 * Displays overall ratings summary, star distribution, and individual user
 * reviews for a given book.  Supports adding, editing and deleting your own review.
 */
public class RatingsReviewsActivity extends AppCompatActivity {

    /** Caller must put this extra: the book id to show reviews for. */
    public static final String EXTRA_BOOK_ID = "book_id";

    // ── Sort modes ────────────────────────────────────────────────────────────
    private static final int SORT_RECENT  = 0;
    private static final int SORT_HELPFUL = 1;

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView       tvAvgRating, tvReviewCount, tvSortLabel;
    private ImageView[]    sumStars;
    private ProgressBar    bar5, bar4, bar3, bar2, bar1;
    private TextView       tvPct5, tvPct4, tvPct3, tvPct2, tvPct1;
    private LinearLayout   reviewsContainer, layoutEmpty;
    private TextView       btnWriteReview;

    // ── State ─────────────────────────────────────────────────────────────────
    private int    bookId   = -1;
    private int    userId   = -1;
    private String userName = "";
    private int    sortMode = SORT_RECENT;
    private List<Review> reviews = new ArrayList<>();

    // ── Threading ─────────────────────────────────────────────────────────────
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ratings_reviews);

        // Session
        SessionManager session = new SessionManager(this);
        userId   = session.getUserId();
        userName = session.getUsername();

        // Bind views
        tvAvgRating     = findViewById(R.id.tv_avg_rating);
        tvReviewCount   = findViewById(R.id.tv_review_count);
        tvSortLabel     = findViewById(R.id.tv_sort_label);
        bar5            = findViewById(R.id.bar_5);
        bar4            = findViewById(R.id.bar_4);
        bar3            = findViewById(R.id.bar_3);
        bar2            = findViewById(R.id.bar_2);
        bar1            = findViewById(R.id.bar_1);
        tvPct5          = findViewById(R.id.tv_pct_5);
        tvPct4          = findViewById(R.id.tv_pct_4);
        tvPct3          = findViewById(R.id.tv_pct_3);
        tvPct2          = findViewById(R.id.tv_pct_2);
        tvPct1          = findViewById(R.id.tv_pct_1);
        reviewsContainer = findViewById(R.id.reviews_container);
        layoutEmpty     = findViewById(R.id.layout_empty);
        btnWriteReview  = findViewById(R.id.btn_write_review);

        // Summary stars
        sumStars = new ImageView[]{
                findViewById(R.id.sum_star_1),
                findViewById(R.id.sum_star_2),
                findViewById(R.id.sum_star_3),
                findViewById(R.id.sum_star_4),
                findViewById(R.id.sum_star_5)
        };

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Write / edit own review
        btnWriteReview.setOnClickListener(v -> openWriteReviewSheet(null));

        // Sort
        findViewById(R.id.btn_sort).setOnClickListener(this::showSortMenu);

        // Book id from intent
        bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, -1);
        if (bookId == -1) {
            finish();
            return;
        }

        loadReviews();
    }

    // ── Data loading ───────────────────────────────────────────────────────────

    private void loadReviews() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            List<Review> list = sortMode == SORT_HELPFUL
                    ? db.reviewDao().getReviewsForBookByHelpful(bookId)
                    : db.reviewDao().getReviewsForBook(bookId);

            float avg  = db.reviewDao().getAverageRating(bookId);
            int total  = list.size();
            int c5     = db.reviewDao().getCountForStars(bookId, 5);
            int c4     = db.reviewDao().getCountForStars(bookId, 4);
            int c3     = db.reviewDao().getCountForStars(bookId, 3);
            int c2     = db.reviewDao().getCountForStars(bookId, 2);
            int c1     = db.reviewDao().getCountForStars(bookId, 1);

            // Check if current user already has a review → update button label
            Review ownReview = db.reviewDao().getOwnReview(bookId, userId);

            mainHandler.post(() -> {
                reviews = list;
                updateSummaryUI(avg, total, c5, c4, c3, c2, c1);
                renderReviewCards();
                btnWriteReview.setText(ownReview != null
                        ? getString(R.string.reviews_edit_title)
                        : getString(R.string.reviews_write));
            });
        });
    }

    // ── Summary UI ─────────────────────────────────────────────────────────────

    private void updateSummaryUI(float avg, int total,
                                  int c5, int c4, int c3, int c2, int c1) {
        // Average number
        tvAvgRating.setText(total == 0 ? "0.0"
                : String.format(Locale.US, "%.1f", avg));

        // Summary stars
        renderSummaryStars(avg);

        // Count label
        tvReviewCount.setText(total == 1
                ? getString(R.string.reviews_count_single)
                : getString(R.string.reviews_count, total));

        // Distribution bars
        if (total > 0) {
            setBar(bar5, tvPct5, c5, total);
            setBar(bar4, tvPct4, c4, total);
            setBar(bar3, tvPct3, c3, total);
            setBar(bar2, tvPct2, c2, total);
            setBar(bar1, tvPct1, c1, total);
        } else {
            resetBar(bar5, tvPct5);
            resetBar(bar4, tvPct4);
            resetBar(bar3, tvPct3);
            resetBar(bar2, tvPct2);
            resetBar(bar1, tvPct1);
        }
    }

    private void setBar(ProgressBar bar, TextView pctView, int count, int total) {
        int pct = (int) Math.round(count * 100.0 / total);
        bar.setProgress(pct);
        pctView.setText(pct + "%");
    }

    private void resetBar(ProgressBar bar, TextView pctView) {
        bar.setProgress(0);
        pctView.setText("0%");
    }

    private void renderSummaryStars(float avg) {
        int filled  = 0xFFFBBF24;
        int empty   = 0xFFCBD5E1;
        for (int i = 0; i < 5; i++) {
            sumStars[i].setImageTintList(
                    ColorStateList.valueOf(i < Math.round(avg) ? filled : empty));
        }
    }

    // ── Review cards ───────────────────────────────────────────────────────────

    private void renderReviewCards() {
        reviewsContainer.removeAllViews();

        if (reviews.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            reviewsContainer.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        reviewsContainer.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Review r : reviews) {
            View card = inflater.inflate(R.layout.item_review_card, reviewsContainer, false);
            bindReviewCard(card, r);
            reviewsContainer.addView(card);
        }
    }

    private void bindReviewCard(View card, Review r) {
        // Initials avatar
        TextView tvInitials = card.findViewById(R.id.tv_initials);
        tvInitials.setText(r.reviewerInitials != null ? r.reviewerInitials : "?");
        // Color the avatar based on name hash
        int[] colors = {0xFF6366F1, 0xFF8B5CF6, 0xFF06B6D4,
                        0xFF10B981, 0xFFF59E0B, 0xFFEF4444, 0xFFEC4899};
        int color = colors[Math.abs(r.reviewerName.hashCode()) % colors.length];
        // Set background color on the FrameLayout parent
        ((android.widget.FrameLayout) tvInitials.getParent()).setBackgroundTintList(
                ColorStateList.valueOf(color));

        // Name
        TextView tvName = card.findViewById(R.id.tv_reviewer_name);
        tvName.setText(r.reviewerName);

        // Stars
        renderCardStars(card, Math.round(r.rating));

        // Date
        TextView tvDate = card.findViewById(R.id.tv_date);
        tvDate.setText(DATE_FMT.format(new Date(r.dateMs)));

        // Review text
        TextView tvText = card.findViewById(R.id.tv_review_text);
        tvText.setText(r.reviewText);

        // Thumbs
        TextView tvUp   = card.findViewById(R.id.tv_thumbs_up);
        TextView tvDown = card.findViewById(R.id.tv_thumbs_down);
        tvUp.setText(String.valueOf(r.thumbsUp));
        tvDown.setText(String.valueOf(r.thumbsDown));

        // Thumbs-up click
        card.findViewById(R.id.btn_thumbs_up).setOnClickListener(v -> {
            executor.execute(() -> {
                AppDatabase.getInstance(this).reviewDao().incrementThumbsUp(r.id);
                r.thumbsUp++;
                mainHandler.post(() -> tvUp.setText(String.valueOf(r.thumbsUp)));
            });
        });

        // Thumbs-down click
        card.findViewById(R.id.btn_thumbs_down).setOnClickListener(v -> {
            executor.execute(() -> {
                AppDatabase.getInstance(this).reviewDao().incrementThumbsDown(r.id);
                r.thumbsDown++;
                mainHandler.post(() -> tvDown.setText(String.valueOf(r.thumbsDown)));
            });
        });

        // Options button (edit/delete) – only for own review
        View btnOptions = card.findViewById(R.id.btn_options);
        if (r.isOwn && r.userId == userId) {
            btnOptions.setVisibility(View.VISIBLE);
            btnOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, v);
                popup.getMenu().add(0, 1, 0, getString(R.string.reviews_edit_title));
                popup.getMenu().add(0, 2, 1, getString(R.string.reviews_delete));
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) { openWriteReviewSheet(r); return true; }
                    if (item.getItemId() == 2) { confirmDeleteReview(r);  return true; }
                    return false;
                });
                popup.show();
            });
        } else {
            btnOptions.setVisibility(View.GONE);
        }
    }

    private void renderCardStars(View card, int rating) {
        int[] ids = {R.id.star_1, R.id.star_2, R.id.star_3, R.id.star_4, R.id.star_5};
        int filled = 0xFFFBBF24;
        int empty  = 0xFFCBD5E1;
        for (int i = 0; i < 5; i++) {
            ImageView iv = card.findViewById(ids[i]);
            iv.setImageTintList(
                    ColorStateList.valueOf(i < rating ? filled : empty));
        }
    }

    // ── Write / Edit Review Sheet ──────────────────────────────────────────────

    private void openWriteReviewSheet(Review existingReview) {
        // Check if user already has a review (if existingReview is null, look it up)
        if (existingReview == null) {
            executor.execute(() -> {
                Review own = AppDatabase.getInstance(this).reviewDao().getOwnReview(bookId, userId);
                mainHandler.post(() -> showReviewBottomSheet(own));
            });
        } else {
            showReviewBottomSheet(existingReview);
        }
    }

    private void showReviewBottomSheet(Review existingReview) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.dialog_add_review, null);
        dialog.setContentView(sheetView);

        // Sheet title
        TextView tvSheetTitle = sheetView.findViewById(R.id.tv_sheet_title);
        tvSheetTitle.setText(existingReview != null
                ? getString(R.string.reviews_edit_title)
                : getString(R.string.reviews_write_title));

        // Star selectors
        ImageView[] dialStars = {
                sheetView.findViewById(R.id.dial_star_1),
                sheetView.findViewById(R.id.dial_star_2),
                sheetView.findViewById(R.id.dial_star_3),
                sheetView.findViewById(R.id.dial_star_4),
                sheetView.findViewById(R.id.dial_star_5)
        };
        final int[] selectedRating = {existingReview != null ? Math.round(existingReview.rating) : 0};

        // Render initial stars
        renderDialogStars(dialStars, selectedRating[0]);

        // Set up star click listeners
        for (int i = 0; i < 5; i++) {
            final int starIndex = i + 1;
            dialStars[i].setOnClickListener(v -> {
                selectedRating[0] = starIndex;
                renderDialogStars(dialStars, starIndex);
            });
        }

        // Review text
        android.widget.EditText etReview = sheetView.findViewById(R.id.et_review);
        if (existingReview != null && existingReview.reviewText != null) {
            etReview.setText(existingReview.reviewText);
            etReview.setSelection(existingReview.reviewText.length());
        }

        // Delete button (only for editing own review)
        TextView btnDelete = sheetView.findViewById(R.id.btn_delete_review);
        if (existingReview != null) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                dialog.dismiss();
                confirmDeleteReview(existingReview);
            });
        }

        // Save button
        sheetView.findViewById(R.id.btn_save_review).setOnClickListener(v -> {
            if (selectedRating[0] == 0) {
                Toast.makeText(this, R.string.reviews_err_no_rating, Toast.LENGTH_SHORT).show();
                return;
            }
            String text = etReview.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.reviews_err_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            saveReview(existingReview, selectedRating[0], text);
        });

        dialog.show();
    }

    private void renderDialogStars(ImageView[] stars, int rating) {
        int filled = 0xFFFBBF24;
        int empty  = 0xFFCBD5E1;
        for (int i = 0; i < 5; i++) {
            stars[i].setImageTintList(
                    ColorStateList.valueOf(i < rating ? filled : empty));
        }
    }

    // ── Save / Delete review ───────────────────────────────────────────────────

    private void saveReview(Review existing, int rating, String text) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            boolean isNew = existing == null;

            // Build initials from username
            String initials = makeInitials(userName);

            if (isNew) {
                Review r = new Review();
                r.bookId           = bookId;
                r.userId           = userId;
                r.reviewerName     = userName.isEmpty() ? "You" : userName;
                r.reviewerInitials = initials;
                r.rating           = rating;
                r.reviewText       = text;
                r.dateMs           = System.currentTimeMillis();
                r.thumbsUp         = 0;
                r.thumbsDown       = 0;
                r.isOwn            = true;
                db.reviewDao().insert(r);

                // Also update the book's own rating field
                Book book = db.bookDao().getBookById(bookId);
                if (book != null) {
                    book.rating = rating;
                    book.notes  = text;
                    db.bookDao().update(book);
                }

                mainHandler.post(() -> {
                    Toast.makeText(this, R.string.reviews_saved, Toast.LENGTH_SHORT).show();
                    loadReviews();
                    setResult(RESULT_OK);
                });
            } else {
                existing.rating     = rating;
                existing.reviewText = text;
                existing.dateMs     = System.currentTimeMillis();
                db.reviewDao().update(existing);

                // Sync to book
                Book book = db.bookDao().getBookById(bookId);
                if (book != null) {
                    book.rating = rating;
                    book.notes  = text;
                    db.bookDao().update(book);
                }

                mainHandler.post(() -> {
                    Toast.makeText(this, R.string.reviews_updated, Toast.LENGTH_SHORT).show();
                    loadReviews();
                    setResult(RESULT_OK);
                });
            }
        });
    }

    private void confirmDeleteReview(Review r) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reviews_delete_confirm_title)
                .setMessage(R.string.reviews_delete_confirm_msg)
                .setPositiveButton(R.string.delete_confirm_yes, (d, w) -> deleteReview(r))
                .setNegativeButton(R.string.delete_confirm_no, null)
                .show();
    }

    private void deleteReview(Review r) {
        executor.execute(() -> {
            AppDatabase.getInstance(this).reviewDao().delete(r);
            mainHandler.post(() -> {
                Toast.makeText(this, R.string.reviews_deleted, Toast.LENGTH_SHORT).show();
                loadReviews();
                setResult(RESULT_OK);
            });
        });
    }

    // ── Sort menu ──────────────────────────────────────────────────────────────

    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, SORT_RECENT,  0, R.string.reviews_sort_recent);
        popup.getMenu().add(0, SORT_HELPFUL, 1, R.string.reviews_sort_helpful);
        popup.setOnMenuItemClickListener(item -> {
            sortMode = item.getItemId();
            tvSortLabel.setText(sortMode == SORT_HELPFUL
                    ? getString(R.string.reviews_sort_helpful)
                    : getString(R.string.reviews_sort_recent));
            loadReviews();
            return true;
        });
        popup.show();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Make up to 2-char initials from a display name. */
    private String makeInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
    }
}


