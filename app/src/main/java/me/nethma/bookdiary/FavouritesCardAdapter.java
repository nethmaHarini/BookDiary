package me.nethma.bookdiary;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.Book;

/**
 * RecyclerView adapter for the Favourites screen vertical book list.
 */
public class FavouritesCardAdapter extends RecyclerView.Adapter<FavouritesCardAdapter.ViewHolder> {

    // ── Callback ──────────────────────────────────────────────────────────────
    public interface OnBookActionListener {
        void onBookClick(Book book);
        void onUnfavourite(Book book);   // ❤ tap → remove from favourites
        void onEditClick(Book book);     // ✏ tap → edit book
    }

    private static final int[] PLACEHOLDER_COLORS = {
            0xFF6366F1, 0xFF8B5CF6, 0xFF06B6D4,
            0xFF10B981, 0xFFF59E0B, 0xFFEF4444, 0xFFEC4899
    };

    private List<Book> books = new ArrayList<>();
    private final OnBookActionListener listener;
    private final ExecutorService imgExecutor = Executors.newFixedThreadPool(2);

    public FavouritesCardAdapter(OnBookActionListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<Book> books) {
        this.books = new ArrayList<>(books);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fav_card, parent, false);
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

        final ImageView  ivCover, ivFavStar;
        final FrameLayout btnUnfav;
        final ImageView  btnEdit;
        final TextView   tvTitle, tvAuthor, tvRating, tvCategory, tvStatus;
        final ImageView  star1, star2, star3, star4, star5;

        ViewHolder(View v) {
            super(v);
            ivCover    = v.findViewById(R.id.iv_cover);
            ivFavStar  = v.findViewById(R.id.iv_fav_star);
            btnUnfav   = v.findViewById(R.id.btn_unfav);
            btnEdit    = v.findViewById(R.id.btn_edit);
            tvTitle    = v.findViewById(R.id.tv_title);
            tvAuthor   = v.findViewById(R.id.tv_author);
            tvRating   = v.findViewById(R.id.tv_rating);
            tvCategory = v.findViewById(R.id.tv_category);
            tvStatus   = v.findViewById(R.id.tv_status);
            star1 = v.findViewById(R.id.star_1);
            star2 = v.findViewById(R.id.star_2);
            star3 = v.findViewById(R.id.star_3);
            star4 = v.findViewById(R.id.star_4);
            star5 = v.findViewById(R.id.star_5);
        }

        void bind(Book book, OnBookActionListener listener, ExecutorService executor) {
            tvTitle.setText(book.title);
            tvAuthor.setText(book.author);
            tvRating.setText(String.format(Locale.US, "%.1f", book.rating));

            // Stars
            renderStars(Math.round(book.rating));

            // Heart = rose filled (always filled since these are all favourites)
            ivFavStar.setImageTintList(ColorStateList.valueOf(0xFFEF4444));

            // Status badge
            applyStatusBadge(tvStatus, book.readingStatus);

            // Category badge
            if (book.category != null && !book.category.isEmpty()) {
                tvCategory.setText(book.category);
                tvCategory.setVisibility(View.VISIBLE);
                applyTagBadge(tvCategory);
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            // Cover
            loadCover(book, executor);

            // Card tap → Book Details
            itemView.setOnClickListener(v -> listener.onBookClick(book));

            // Heart tap → remove from favourites
            btnUnfav.setOnClickListener(v -> listener.onUnfavourite(book));

            // Edit tap → edit book
            btnEdit.setOnClickListener(v -> listener.onEditClick(book));
        }

        private void loadCover(Book book, ExecutorService executor) {
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

        private void renderStars(int rating) {
            ImageView[] stars = {star1, star2, star3, star4, star5};
            int filled = 0xFFFBBF24;
            int empty  = 0xFFCBD5E1;
            for (int i = 0; i < 5; i++) {
                stars[i].setImageTintList(
                        ColorStateList.valueOf(i < rating ? filled : empty));
            }
        }

        private static void applyStatusBadge(TextView badge, String status) {
            if (status == null || status.isEmpty()) {
                badge.setVisibility(View.GONE);
                return;
            }
            badge.setVisibility(View.VISIBLE);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(6f);
            if ("Currently Reading".equals(status)) {
                bg.setColor(0x1A1152D4);
                badge.setTextColor(0xFF1152D4);
                badge.setText("Reading");
            } else if ("Finished".equals(status)) {
                bg.setColor(0x1A10B981);
                badge.setTextColor(0xFF10B981);
                badge.setText("Finished");
            } else {
                bg.setColor(0x1AFBBF24);
                badge.setTextColor(0xFFF59E0B);
                badge.setText("Want to Read");
            }
            badge.setBackground(bg);
        }

        private static void applyTagBadge(TextView badge) {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(6f);
            bg.setColor(0x1A1152D4);
            badge.setBackground(bg);
            badge.setTextColor(0xFF1152D4);
        }
    }
}

