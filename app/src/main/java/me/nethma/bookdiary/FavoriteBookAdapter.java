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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.nethma.bookdiary.database.Book;

/**
 * RecyclerView adapter for the horizontal "Favourite Books" strip on the Home screen.
 */
public class FavoriteBookAdapter extends RecyclerView.Adapter<FavoriteBookAdapter.ViewHolder> {

    // ── Callback interface ───────────────────────────────────────────────────

    public interface OnBookClickListener {
        void onBookClick(Book book);
        void onFavoriteToggle(Book book);
    }

    // ── Placeholder colours (used when no cover image is set) ────────────────
    private static final int[] PLACEHOLDER_COLORS = {
            0xFF6366F1, 0xFF8B5CF6, 0xFF06B6D4,
            0xFF10B981, 0xFFF59E0B, 0xFFEF4444, 0xFFEC4899
    };

    // ── State ────────────────────────────────────────────────────────────────

    private List<Book> books = new ArrayList<>();
    private final OnBookClickListener listener;

    public FavoriteBookAdapter(OnBookClickListener listener) {
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
                .inflate(R.layout.item_book_favorite, parent, false);
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

        ViewHolder(View v) {
            super(v);
            ivCover  = v.findViewById(R.id.iv_cover);
            ivHeart  = v.findViewById(R.id.iv_heart);
            tvTitle  = v.findViewById(R.id.tv_title);
            tvAuthor = v.findViewById(R.id.tv_author);
            tvRating = v.findViewById(R.id.tv_rating);
        }

        void bind(Book book, OnBookClickListener listener) {
            tvTitle.setText(book.title);
            tvAuthor.setText(book.author);
            tvRating.setText(String.format(Locale.US, "%.1f", book.rating));

            // ── Always show placeholder first (correct recycled-view state) ──
            int colorIdx = Math.abs(book.title.hashCode()) % PLACEHOLDER_COLORS.length;
            GradientDrawable placeholder = new GradientDrawable();
            placeholder.setShape(GradientDrawable.RECTANGLE);
            placeholder.setCornerRadius(24f);
            placeholder.setColor(PLACEHOLDER_COLORS[colorIdx]);
            ivCover.setBackground(placeholder);
            ivCover.setImageResource(R.drawable.ic_book_logo);
            ivCover.setImageTintList(ColorStateList.valueOf(0x80FFFFFF));

            // ── Async cover load via Glide (handles both http URLs and local files) ──
            if (book.coverUrl != null && !book.coverUrl.isEmpty()) {
                String url = book.coverUrl.startsWith("http")
                        ? book.coverUrl
                        : "file://" + book.coverUrl;
                Glide.with(itemView.getContext())
                        .load(url)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_book_logo)
                                .error(R.drawable.ic_book_logo)
                                .transform(new RoundedCorners(24)))
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e,
                                    Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                    boolean isFirstResource) { return false; }
                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                    Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                    com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                ivCover.setBackground(null);
                                ivCover.setImageTintList(null);
                                return false;
                            }
                        })
                        .into(ivCover);
            } else {
                Glide.with(itemView.getContext()).clear(ivCover);
            }

            // ── Heart icon ─────────────────────────────────────────────────
            if (book.isFavorite) {
                ivHeart.setImageResource(R.drawable.ic_heart_filled);
                ivHeart.setImageTintList(null);
            } else {
                ivHeart.setImageResource(R.drawable.ic_heart);
                ivHeart.setImageTintList(ColorStateList.valueOf(0xFFFFFFFF));
            }

            // ── Click listeners ────────────────────────────────────────────
            itemView.setOnClickListener(v -> listener.onBookClick(book));
            ivHeart.setOnClickListener(v -> listener.onFavoriteToggle(book));
        }
    }
}



