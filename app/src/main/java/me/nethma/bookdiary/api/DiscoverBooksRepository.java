package me.nethma.bookdiary.api;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fetches recent, popular books from the Open Library API based on user-selected topics.
 *
 * Strategy:
 *  - Search using q="<topic> <year_range>" to bias results toward recent publications
 *  - Sort by "new" to get recently added/published books first
 *  - Filter client-side: must have a cover image, publish year >= MIN_YEAR
 *  - Fetch enough candidates (FETCH_LIMIT) to still get DESIRED_PER_TOPIC after filtering
 */
public class DiscoverBooksRepository {

    private static final String TAG              = "DiscoverBooksRepo";
    private static final int    MIN_YEAR         = 2010;   // only books from 2010 onward
    private static final int    FETCH_LIMIT      = 50;     // fetch more to survive filtering
    private static final int    DESIRED_PER_TOPIC = 15;    // keep up to 15 per topic
    private static final String FIELDS           =
            "key,title,author_name,cover_i,first_publish_year,ratings_average,ratings_count";

    // Maps user-facing topic → Open Library search term
    private static final Map<String, String> TOPIC_TO_QUERY = new HashMap<>();

    static {
        TOPIC_TO_QUERY.put("Fiction",         "popular fiction novel");
        TOPIC_TO_QUERY.put("Mystery",         "mystery thriller detective");
        TOPIC_TO_QUERY.put("Romance",         "romance love story novel");
        TOPIC_TO_QUERY.put("Science",         "popular science nonfiction");
        TOPIC_TO_QUERY.put("History",         "history historical nonfiction");
        TOPIC_TO_QUERY.put("Fantasy",         "fantasy magic novel");
        TOPIC_TO_QUERY.put("Biography",       "biography memoir");
        TOPIC_TO_QUERY.put("Thriller",        "thriller suspense novel");
        TOPIC_TO_QUERY.put("Self-Help",       "self help personal development");
        TOPIC_TO_QUERY.put("Horror",          "horror scary novel");
        TOPIC_TO_QUERY.put("Classic",         "classic literature bestseller");
        TOPIC_TO_QUERY.put("Poetry",          "poetry poems collection");
        TOPIC_TO_QUERY.put("Science Fiction", "science fiction space adventure");
        TOPIC_TO_QUERY.put("Adventure",       "adventure action novel");
        TOPIC_TO_QUERY.put("Children",        "children picture book story");
    }

    public interface BooksCallback {
        void onSuccess(List<OpenLibraryBook> books);
        void onError(String message);
    }

    private final OpenLibraryService service;

    public DiscoverBooksRepository(Context context) {
        this.service = BookApiClient.getService(context);
    }

    public void fetchBooksForTopics(List<String> topics, BooksCallback callback) {
        if (topics == null || topics.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<OpenLibraryBook> merged   = new ArrayList<>();
        Set<String>           seenKeys = new HashSet<>();
        AtomicInteger         pending  = new AtomicInteger(topics.size());

        for (String topic : topics) {
            String query = TOPIC_TO_QUERY.getOrDefault(topic, topic);

            service.search(query, FETCH_LIMIT, FIELDS, "new", "eng")
                    .enqueue(new Callback<OpenLibraryResponse>() {
                        @Override
                        public void onResponse(Call<OpenLibraryResponse> call,
                                               Response<OpenLibraryResponse> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().docs != null) {

                                List<OpenLibraryBook> filtered = filterAndLimit(
                                        response.body().docs, seenKeys);

                                synchronized (merged) {
                                    merged.addAll(filtered);
                                }
                            }
                            checkDone();
                        }

                        @Override
                        public void onFailure(Call<OpenLibraryResponse> call, Throwable t) {
                            Log.e(TAG, "Failed to fetch topic: " + topic, t);
                            checkDone();
                        }

                        private void checkDone() {
                            if (pending.decrementAndGet() == 0) {
                                // Shuffle so books from different topics are interleaved
                                synchronized (merged) {
                                    Collections.shuffle(merged);
                                }
                                callback.onSuccess(new ArrayList<>(merged));
                            }
                        }
                    });
        }
    }

    /**
     * Keep only books that:
     *  1. Have a valid key and title
     *  2. Have a cover image (cover_i > 0)
     *  3. Were first published in MIN_YEAR or later
     *  4. Haven't been seen yet (deduplication)
     * Returns up to DESIRED_PER_TOPIC results.
     */
    private List<OpenLibraryBook> filterAndLimit(List<OpenLibraryBook> docs,
                                                  Set<String> seenKeys) {
        List<OpenLibraryBook> result = new ArrayList<>();
        for (OpenLibraryBook book : docs) {
            if (result.size() >= DESIRED_PER_TOPIC) break;

            // Must have key + title
            if (book.key == null || book.title == null || book.title.isEmpty()) continue;

            // Must have a cover image
            if (book.coverId <= 0) continue;

            // Must be recent enough
            if (book.firstPublishYear > 0 && book.firstPublishYear < MIN_YEAR) continue;

            // Deduplicate
            synchronized (seenKeys) {
                if (seenKeys.contains(book.key)) continue;
                seenKeys.add(book.key);
            }

            result.add(book);
        }
        return result;
    }
}
