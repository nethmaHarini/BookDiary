package me.nethma.bookdiary.api;

import android.content.Context;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;

/** Singleton Retrofit client for Open Library API */
public class BookApiClient {

    private static final String BASE_URL = "https://openlibrary.org/";
    private static OpenLibraryService instance;

    public static synchronized OpenLibraryService getService(Context context) {
        if (instance == null) {
            // 10 MB disk cache to avoid redundant calls
            File cacheDir = new File(context.getCacheDir(), "open_library_cache");
            Cache cache = new Cache(cacheDir, 10 * 1024 * 1024);

            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(cache)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            instance = retrofit.create(OpenLibraryService.class);
        }
        return instance;
    }
}

