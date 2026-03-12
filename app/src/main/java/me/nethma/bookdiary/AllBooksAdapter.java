package me.nethma.bookdiary;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.nethma.bookdiary.database.Book;
import me.nethma.bookdiary.utils.ThemePrefsManager;

/**
 * RecyclerView adapter for the vertical "All Books" list on the Home screen.
 */
public class AllBooksAdapter extends RecyclerView.Adapter<AllBooksAdapter.ViewHolder> {

    // ── Callback interface ───────────────────────────────────────────────────

    public interface OnBookClickListener {
        void onBookClick(Book book);
        void onFavoriteToggle(Book book);
    }

    // ── Placeholder colours ──────────────────────────────────────────────────
    private static final int[] PLACEHOLDER_COLORS = {
            0xFF6366F1, 0xFF8B5CF6, 0xFF06B6D4,
            0xFF10B981, 0xFFF59E0B, 0xFFEF4444, 0xFFEC4899
    };

    // ── State ────────────────────────────────────────────────────────────────

    private List<Book> books = new ArrayList<>();
    private final OnBookClickListener listener;

    public AllBooksAdapter(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<Book> books) {
        this.books = new ArrayList<>(books);
        notifyDataSetChanged();
    }

    // ── RecyclerView.Adapter ─────────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(books.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivCover;
        final ImageView ivHeart;
        final TextView  tvTitle;
        final TextView  tvAuthor;
        final TextView  tvRating;
        final TextView  tvCategory;

        ViewHolder(View v) {
            super(v);
            ivCover    = v.findViewById(R.id.iv_cover);
            ivHeart    = v.findViewById(R.id.iv_heart);
            tvTitle    = v.findViewById(R.id.tv_title);
            tvAuthor   = v.findViewById(R.id.tv_author);
            tvRating   = v.findViewById(R.id.tv_rating);
            tvCategory = v.findViewById(R.id.tv_category);
        }

        void bind(Book book, OnBookClickListener listener) {
            tvTitle.setText(book.title);
            tvAuthor.setText(book.author);
            tvRating.setText(String.format(Locale.US, "%.1f", book.rating));
            tvCategory.setText(book.category);

            // ── Always show placeholder first (correct recycled-view state) ──
            int colorIdx = Math.abs(book.title.hashCode()) % PLACEHOLDER_COLORS.length;
            GradientDrawable placeholder = new GradientDrawable();
            placeholder.setShape(GradientDrawable.RECTANGLE);
            placeholder.setCornerRadius(24f);
            placeholder.setColor(PLACEHOLDER_COLORS[colorIdx]);
            ivCover.setBackground(placeholder);
            ivCover.setImageResource(R.drawable.ic_book_logo);
            ivCover.setImageTintList(ColorStateList.valueOf(0x80FFFFFF));

            // ── Async cover load with recycling guard ──────────────────────
            if (book.coverUrl != null && !book.coverUrl.isEmpty()) {
                final String path = book.coverUrl;
                ivCover.setTag(path);
                new Thread(() -> {
                    android.graphics.Bitmap bm =
                            android.graphics.BitmapFactory.decodeFile(path);
                    if (bm != null) {
                        ivCover.post(() -> {
                            if (path.equals(ivCover.getTag())) {
                                ivCover.setBackground(null);
                                ivCover.setImageBitmap(bm);
                                ivCover.setImageTintList(null); // clear placeholder tint
                            }
                        });
                    }
                }).start();
            } else {
                ivCover.setTag(null);
            }

            // ── Heart icon ─────────────────────────────────────────────────
            if (book.isFavorite) {
                ivHeart.setImageResource(R.drawable.ic_heart_filled);
                ivHeart.setImageTintList(null);
            } else {
                ivHeart.setImageResource(R.drawable.ic_heart);
                // Use text_hint colour for unfilled heart
                int hintColor = itemView.getContext().getColor(R.color.text_hint);
                ivHeart.setImageTintList(ColorStateList.valueOf(hintColor));
            }

            // ── Category badge ─────────────────────────────────────────────
            boolean isDark = (itemView.getContext().getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

            // Use accent/primary colour for first position as a design accent,
            // neutral for the rest.
            int accent = ThemePrefsManager.getAccentColor(itemView.getContext());
            boolean useAccent = (getBindingAdapterPosition() == 0);

            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setShape(GradientDrawable.RECTANGLE);
            badgeBg.setCornerRadius(6f * itemView.getContext()
                    .getResources().getDisplayMetrics().density);

            if (useAccent) {
                // primary/10 background + primary text
                badgeBg.setColor((accent & 0x00FFFFFF) | 0x1A000000);
                tvCategory.setTextColor(accent);
            } else {
                // neutral: gray bg + secondary text
                badgeBg.setColor(isDark ? 0xFF1E293B : 0xFFF1F5F9);
                tvCategory.setTextColor(isDark ? 0xFF94A3B8 : 0xFF64748B);
            }
            tvCategory.setBackground(badgeBg);

            // ── Click listeners ────────────────────────────────────────────
            itemView.setOnClickListener(v -> listener.onBookClick(book));
            ivHeart.setOnClickListener(v -> listener.onFavoriteToggle(book));
        }
    }
}



