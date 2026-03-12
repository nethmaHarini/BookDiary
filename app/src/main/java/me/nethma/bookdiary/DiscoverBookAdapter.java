package me.nethma.bookdiary;

import android.content.Intent;
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

import me.nethma.bookdiary.api.OpenLibraryBook;

/** Adapter for displaying API-discovered books in HomeFragment */
public class DiscoverBookAdapter extends RecyclerView.Adapter<DiscoverBookAdapter.ViewHolder> {

    private List<OpenLibraryBook> books = new ArrayList<>();

    public DiscoverBookAdapter() {}

    public void setBooks(List<OpenLibraryBook> books) {
        this.books = books != null ? books : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discover_book, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(books.get(position));
    }

    @Override
    public int getItemCount() { return books.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvAuthor, tvYear;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.img_discover_cover);
            tvTitle  = itemView.findViewById(R.id.tv_discover_title);
            tvAuthor = itemView.findViewById(R.id.tv_discover_author);
            tvYear   = itemView.findViewById(R.id.tv_discover_year);
        }

        void bind(OpenLibraryBook book) {
            tvTitle.setText(book.title);
            tvAuthor.setText(book.getAuthor());
            tvYear.setText(book.firstPublishYear > 0 ? String.valueOf(book.firstPublishYear) : "");

            String coverUrl = book.getCoverUrl();
            if (coverUrl != null) {
                Glide.with(itemView.getContext())
                        .load(coverUrl)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_book_logo)
                                .error(R.drawable.ic_book_logo)
                                .transform(new RoundedCorners(16)))
                        .into(imgCover);
            } else {
                imgCover.setImageResource(R.drawable.ic_book_logo);
            }

            // On click: open ApiBookDetailActivity with full book data
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ApiBookDetailActivity.class);
                intent.putExtra(ApiBookDetailActivity.EXTRA_WORK_KEY,  book.key);
                intent.putExtra(ApiBookDetailActivity.EXTRA_TITLE,     book.title);
                intent.putExtra(ApiBookDetailActivity.EXTRA_AUTHOR,    book.getAuthor());
                intent.putExtra(ApiBookDetailActivity.EXTRA_COVER_URL, coverUrl);
                intent.putExtra(ApiBookDetailActivity.EXTRA_YEAR,      book.firstPublishYear);
                intent.putExtra(ApiBookDetailActivity.EXTRA_RATING,    book.ratingsAverage);
                v.getContext().startActivity(intent);
            });
        }
    }
}



