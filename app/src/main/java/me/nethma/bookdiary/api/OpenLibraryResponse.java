package me.nethma.bookdiary.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Top-level response from Open Library search API */
public class OpenLibraryResponse {

    @SerializedName("numFound")
    public int numFound;

    @SerializedName("docs")
    public List<OpenLibraryBook> docs;
}

