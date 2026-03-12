package me.nethma.bookdiary.api;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
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
 * Fetches books from Open Library API based on user-selected topics.
 * Maps display topic names to Open Library subject query slugs.
 */
public class DiscoverBooksRepository {

    private static final String TAG = "DiscoverBooksRepo";
    private static final String FIELDS = "key,title,author_name,cover_i,first_publish_year";
    private static final int LIMIT_PER_TOPIC = 15;

    // Maps user-facing topic name → Open Library subject query string
    private static final Map<String, String> TOPIC_TO_SUBJECT = new HashMap<>();

    static {
        TOPIC_TO_SUBJECT.put("Fiction",      "fiction");
        TOPIC_TO_SUBJECT.put("Mystery",      "mystery");
        TOPIC_TO_SUBJECT.put("Romance",      "romance");
        TOPIC_TO_SUBJECT.put("Science",      "science");
        TOPIC_TO_SUBJECT.put("History",      "history");
        TOPIC_TO_SUBJECT.put("Fantasy",      "fantasy");
        TOPIC_TO_SUBJECT.put("Biography",    "biography");
        TOPIC_TO_SUBJECT.put("Thriller",     "thriller");
        TOPIC_TO_SUBJECT.put("Self-Help",    "self_help");
        TOPIC_TO_SUBJECT.put("Horror",       "horror");
        TOPIC_TO_SUBJECT.put("Classic",      "classic_literature");
        TOPIC_TO_SUBJECT.put("Poetry",       "poetry");
        TOPIC_TO_SUBJECT.put("Science Fiction", "science_fiction");
        TOPIC_TO_SUBJECT.put("Adventure",    "adventure");
        TOPIC_TO_SUBJECT.put("Children",     "children");
    }

    public interface BooksCallback {
        void onSuccess(List<OpenLibraryBook> books);
        void onError(String message);
    }

    private final OpenLibraryService service;

    public DiscoverBooksRepository(Context context) {
        this.service = BookApiClient.getService(context);
    }

    /**
     * Fetches books for each topic in parallel, merges and deduplicates results,
     * then calls the callback on the calling thread (use with Handler if needed).
     */
    public void fetchBooksForTopics(List<String> topics, BooksCallback callback) {
        if (topics == null || topics.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<OpenLibraryBook> merged = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        AtomicInteger pending = new AtomicInteger(topics.size());
        List<String> errors = new ArrayList<>();

        for (String topic : topics) {
            String subject = TOPIC_TO_SUBJECT.getOrDefault(topic, topic.toLowerCase().replace(" ", "_"));
            service.searchBySubject(subject, LIMIT_PER_TOPIC, FIELDS)
                    .enqueue(new Callback<OpenLibraryResponse>() {
                        @Override
                        public void onResponse(Call<OpenLibraryResponse> call,
                                               Response<OpenLibraryResponse> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().docs != null) {
                                synchronized (merged) {
                                    for (OpenLibraryBook book : response.body().docs) {
                                        if (book.key != null && !seenKeys.contains(book.key)
                                                && book.title != null && !book.title.isEmpty()) {
                                            seenKeys.add(book.key);
                                            merged.add(book);
                                        }
                                    }
                                }
                            }
                            checkDone();
                        }

                        @Override
                        public void onFailure(Call<OpenLibraryResponse> call, Throwable t) {
                            Log.e(TAG, "Failed to fetch topic: " + topic, t);
                            synchronized (errors) { errors.add(topic); }
                            checkDone();
                        }

                        private void checkDone() {
                            if (pending.decrementAndGet() == 0) {
                                callback.onSuccess(new ArrayList<>(merged));
                            }
                        }
                    });
        }
    }
}

