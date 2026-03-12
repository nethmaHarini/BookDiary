package me.nethma.bookdiary.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Retrofit interface for Open Library search & works API */
public interface OpenLibraryService {

    /**
     * Search books by subject/genre.
     * Example: /search.json?subject=fiction&limit=20&fields=key,title,author_name,cover_i,first_publish_year
     */
    @GET("search.json")
    Call<OpenLibraryResponse> searchBySubject(
            @Query("subject") String subject,
            @Query("limit") int limit,
            @Query("fields") String fields,
            @Query("sort") String sort,
            @Query("language") String language
    );

    /**
     * Fetch full works detail by Open Library works key.
     * key format: "/works/OL12345W" — pass just "OL12345W"
     * Example: /works/OL12345W.json
     */
    @GET("works/{workId}.json")
    Call<OpenLibraryWorkDetail> getWorkDetail(@Path("workId") String workId);
}



