package me.nethma.bookdiary;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.Book;

/**
 * RecyclerView adapter for the Diary screen book list.
 */
public class DiaryBookAdapter extends RecyclerView.Adapter<DiaryBookAdapter.ViewHolder> {

    // ── Callback interface ────────────────────────────────────────────────────
    public interface OnBookActionListener {
        void onBookClick(Book book);
        void onFavouriteToggle(Book book);
        void onEditClick(Book book);
    }

    // ── Placeholder cover colours ─────────────────────────────────────────────
    private static final int[] PLACEHOLDER_COLORS = {
            0xFF6366F1, 0xFF8B5CF6, 0xFF06B6D4,
            0xFF10B981, 0xFFF59E0B, 0xFFEF4444, 0xFFEC4899
    };

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    // ── State ─────────────────────────────────────────────────────────────────
    private List<Book> books = new ArrayList<>();
    private final OnBookActionListener listener;
    private final ExecutorService imgExecutor = Executors.newFixedThreadPool(2);

    public DiaryBookAdapter(OnBookActionListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<Book> books) {
        this.books = new ArrayList<>(books);
        notifyDataSetChanged();
    }

    // ── RecyclerView.Adapter ──────────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        h.bind(books.get(position), listener, imgExecutor);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivCover;
        final TextView  tvTitle, tvAuthor, tvStatus, tvDate;
        final ImageView star1, star2, star3, star4, star5;
        final ImageView btnView, btnFav, btnEdit;

        ViewHolder(View v) {
            super(v);
            ivCover  = v.findViewById(R.id.iv_cover);
            tvTitle  = v.findViewById(R.id.tv_title);
            tvAuthor = v.findViewById(R.id.tv_author);
            tvStatus = v.findViewById(R.id.tv_status);
            tvDate   = v.findViewById(R.id.tv_date);
            star1    = v.findViewById(R.id.star_1);
            star2    = v.findViewById(R.id.star_2);
            star3    = v.findViewById(R.id.star_3);
            star4    = v.findViewById(R.id.star_4);
            star5    = v.findViewById(R.id.star_5);
            btnView  = v.findViewById(R.id.btn_view);
            btnFav   = v.findViewById(R.id.btn_fav);
            btnEdit  = v.findViewById(R.id.btn_edit);
        }

        void bind(Book book, OnBookActionListener listener, ExecutorService executor) {
            tvTitle.setText(book.title);
            tvAuthor.setText(book.author);
            tvDate.setText(DATE_FMT.format(new Date(book.dateAdded)));

            // Stars
            renderStars(Math.round(book.rating));

            // Status badge
            applyStatusBadge(tvStatus, book.readingStatus);

            // Favourite icon
            if (book.isFavorite) {
                btnFav.setImageResource(R.drawable.ic_heart_filled);
                btnFav.setImageTintList(null);
            } else {
                btnFav.setImageResource(R.drawable.ic_heart);
                btnFav.setImageTintList(ColorStateList.valueOf(0xFF94A3B8));
            }

            // Cover image
            loadCover(book, executor);

            // Clicks
            itemView.setOnClickListener(v -> listener.onBookClick(book));
            btnView.setOnClickListener(v  -> listener.onBookClick(book));
            btnFav.setOnClickListener(v   -> listener.onFavouriteToggle(book));
            btnEdit.setOnClickListener(v  -> listener.onEditClick(book));
        }

        // ── Cover loading ─────────────────────────────────────────────────────

        private void loadCover(Book book, ExecutorService executor) {
            // Coloured placeholder
            int c = PLACEHOLDER_COLORS[
                    Math.abs(book.title.hashCode()) % PLACEHOLDER_COLORS.length];
            GradientDrawable ph = new GradientDrawable();
            ph.setShape(GradientDrawable.RECTANGLE);
            ph.setCornerRadius(24f);
            ph.setColor(c);
            ivCover.setBackground(ph);
            ivCover.setImageResource(R.drawable.ic_book_logo);
            ivCover.setImageTintList(ColorStateList.valueOf(0x80FFFFFF));

            if (book.coverUrl != null && !book.coverUrl.isEmpty()) {
                final String path = book.coverUrl;
                executor.execute(() -> {
                    Bitmap bm = BitmapFactory.decodeFile(path);
                    if (bm != null) {
                        ivCover.post(() -> {
                            ivCover.setBackground(null);
                            ivCover.setImageBitmap(bm);
                            ivCover.setImageTintList(null);
                        });
                    }
                });
            }
        }

        // ── Stars ─────────────────────────────────────────────────────────────

        private void renderStars(int rating) {
            ImageView[] stars = {star1, star2, star3, star4, star5};
            int filled = 0xFFFBBF24;
            int empty  = 0xFFCBD5E1;
            for (int i = 0; i < 5; i++) {
                stars[i].setImageTintList(
                        ColorStateList.valueOf(i < rating ? filled : empty));
            }
        }

        // ── Status badge ──────────────────────────────────────────────────────

        private static void applyStatusBadge(TextView badge, String status) {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(6f);

            if ("Currently Reading".equals(status)) {
                bg.setColor(0x1A1152D4);          // primary/10
                badge.setTextColor(0xFF1152D4);
                badge.setText("Reading");
            } else if ("Finished".equals(status)) {
                bg.setColor(0x1A10B981);           // green/10
                badge.setTextColor(0xFF10B981);
                badge.setText("Finished");
            } else {                               // Want to Read or null
                bg.setColor(0x1AFBBF24);           // amber/10
                badge.setTextColor(0xFFF59E0B);
                badge.setText(status != null ? "Want to Read" : "—");
            }
            badge.setBackground(bg);
        }
    }
}

