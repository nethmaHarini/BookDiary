package me.nethma.bookdiary;

import android.content.res.ColorStateList;
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

/**
 * RecyclerView adapter for search results in SearchFragment.
 * Uses item_search_result.xml (smaller cover, bookmark icon).
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    // ── Callback ─────────────────────────────────────────────────────────────

    public interface OnBookActionListener {
        void onBookClick(Book book);
        void onBookmarkToggle(Book book);
    }

    // ── Placeholder colours ───────────────────────────────────────────────────

    private static final int[] PLACEHOLDER_COLORS = {
            0xFF6366F1, 0xFF8B5CF6, 0xFF06B6D4,
            0xFF10B981, 0xFFF59E0B, 0xFFEF4444, 0xFFEC4899
    };

    // ── State ────────────────────────────────────────────────────────────────

    private List<Book> books = new ArrayList<>();
    private final OnBookActionListener listener;

    public SearchResultAdapter(OnBookActionListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<Book> books) {
        this.books = new ArrayList<>(books);
        notifyDataSetChanged();
    }

    public List<Book> getCurrentBooks() {
        return books;
    }

    // ── RecyclerView.Adapter ─────────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(books.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    // ── ViewHolder ───────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivCover;
        final TextView  tvTitle;
        final TextView  tvAuthor;
        final TextView  tvRating;
        final TextView  tvCategory;
        final ImageView ivBookmark;

        ViewHolder(View v) {
            super(v);
            ivCover    = v.findViewById(R.id.iv_cover);
            tvTitle    = v.findViewById(R.id.tv_title);
            tvAuthor   = v.findViewById(R.id.tv_author);
            tvRating   = v.findViewById(R.id.tv_rating);
            tvCategory = v.findViewById(R.id.tv_category);
            ivBookmark = v.findViewById(R.id.iv_bookmark);
        }

        void bind(Book book, OnBookActionListener listener) {
            tvTitle.setText(book.title);
            tvAuthor.setText(book.author);
            tvRating.setText(String.format(Locale.US, "%.1f", book.rating));
            tvCategory.setText(book.category);

            // ── Always show placeholder first (correct recycled-view state) ──
            int colorIdx = Math.abs(book.title.hashCode()) % PLACEHOLDER_COLORS.length;
            GradientDrawable placeholder = new GradientDrawable();
            placeholder.setShape(GradientDrawable.RECTANGLE);
            placeholder.setCornerRadius(20f);
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

            // ── Bookmark icon: filled = saved/favourite ───────────────────────
            if (book.isFavorite) {
                ivBookmark.setImageResource(R.drawable.ic_bookmark_filled);
                ivBookmark.setImageTintList(
                        ColorStateList.valueOf(
                                me.nethma.bookdiary.utils.ThemePrefsManager
                                        .getAccentColor(itemView.getContext())));
            } else {
                ivBookmark.setImageResource(R.drawable.ic_bookmark);
                ivBookmark.setImageTintList(
                        ColorStateList.valueOf(
                                me.nethma.bookdiary.utils.ThemePrefsManager
                                        .getAccentColor(itemView.getContext())));
            }

            // ── Click listeners ───────────────────────────────────────────────
            itemView.setOnClickListener(v -> listener.onBookClick(book));
            ivBookmark.setOnClickListener(v -> listener.onBookmarkToggle(book));
        }
    }
}

