package me.nethma.bookdiary.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A single user review attached to a Book.
 * reviewerName  – display name of the reviewer
 * reviewerInitials – 1-2 char initials for the avatar circle
 * rating        – 1-5 stars
 * reviewText    – the written review body
 * dateMs        – timestamp in epoch milliseconds
 * thumbsUp      – number of helpful votes
 * thumbsDown    – number of not-helpful votes
 * isOwn         – true when this review was written by the current logged-in user
 * userId        – owning user (for "own" reviews); 0 for imported/community entries
 * bookId        – the book this review belongs to
 */
@Entity(
    tableName = "reviews",
    foreignKeys = @ForeignKey(
        entity = Book.class,
        parentColumns = "id",
        childColumns = "bookId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("bookId")
)
public class Review {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int    bookId;
    public int    userId;          // 0 for community reviews
    public String reviewerName;
    public String reviewerInitials;
    public float  rating;          // 1.0 – 5.0
    public String reviewText;
    public long   dateMs;
    public int    thumbsUp;
    public int    thumbsDown;
    public boolean isOwn;          // true = written by the logged-in user
}

