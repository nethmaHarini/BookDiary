package me.nethma.bookdiary.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Represents a single book result from the Open Library search API. */
public class OpenLibraryBook {

    @SerializedName("key")
    public String key;

    @SerializedName("title")
    public String title;

    @SerializedName("author_name")
    public List<String> authorName;

    @SerializedName("cover_i")
    public int coverId;

    @SerializedName("first_publish_year")
    public int firstPublishYear;

    /** Returns the first author or "Unknown Author" */
    public String getAuthor() {
        if (authorName != null && !authorName.isEmpty()) {
            return authorName.get(0);
        }
        return "Unknown Author";
    }

    /** Returns cover URL (medium size) or null if no cover */
    public String getCoverUrl() {
        if (coverId > 0) {
            return "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
        }
        return null;
    }
}

