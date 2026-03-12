package me.nethma.bookdiary.database;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class Book {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String author;
    public String category;   // e.g. Fiction, Science, Mystery, History, Classic…

    public float rating;      // 0.0 – 5.0

    public boolean isFavorite;

    @Nullable
    public String coverUrl;   // local file path or remote URL

    @Nullable
    public String notes;      // user's personal review/diary notes

    @Nullable
    public String description; // book description from API (read-only, not user-entered)

    public long dateAdded;    // epoch milliseconds

    public int userId;        // links to the logged-in User row

    @Nullable
    public String readingStatus; // "Want to Read" | "Currently Reading" | "Finished"
}


