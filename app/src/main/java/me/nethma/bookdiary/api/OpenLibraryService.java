package me.nethma.bookdiary.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Retrofit interface for Open Library search & works API */
public interface OpenLibraryService {

    /**
     * General search — used to find recent books by topic + year range.
     * Example: /search.json?q=fiction&sort=new&limit=30
     */
    @GET("search.json")
    Call<OpenLibraryResponse> search(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("fields") String fields,
            @Query("sort") String sort,
            @Query("language") String language
    );

    /**
     * Fetch full works detail by Open Library works key.
     * key format: "/works/OL12345W" — pass just "OL12345W"
     */
    @GET("works/{workId}.json")
    Call<OpenLibraryWorkDetail> getWorkDetail(@Path("workId") String workId);
}

