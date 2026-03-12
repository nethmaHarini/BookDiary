package me.nethma.bookdiary.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReviewDao {

    @Insert
    long insert(Review review);

    @Update
    void update(Review review);

    @Delete
    void delete(Review review);

    /** All reviews for a given book, newest first */
    @Query("SELECT * FROM reviews WHERE bookId = :bookId ORDER BY dateMs DESC")
    List<Review> getReviewsForBook(int bookId);

    /** Reviews for a book, sorted by thumbsUp descending (most helpful first) */
    @Query("SELECT * FROM reviews WHERE bookId = :bookId ORDER BY thumbsUp DESC")
    List<Review> getReviewsForBookByHelpful(int bookId);

    /** The current user's own review for a book (if any) */
    @Query("SELECT * FROM reviews WHERE bookId = :bookId AND userId = :userId AND isOwn = 1 LIMIT 1")
    Review getOwnReview(int bookId, int userId);

    /** Total number of reviews for a book */
    @Query("SELECT COUNT(*) FROM reviews WHERE bookId = :bookId")
    int getReviewCount(int bookId);

    /** Average rating for a book based on all reviews */
    @Query("SELECT AVG(rating) FROM reviews WHERE bookId = :bookId")
    float getAverageRating(int bookId);

    /** Count of reviews per star level (1-5) for a book */
    @Query("SELECT COUNT(*) FROM reviews WHERE bookId = :bookId AND CAST(rating + 0.5 AS INTEGER) = :stars")
    int getCountForStars(int bookId, int stars);

    /** Delete all reviews for a book */
    @Query("DELETE FROM reviews WHERE bookId = :bookId")
    void deleteAllForBook(int bookId);

    /** Thumbs-up a review */
    @Query("UPDATE reviews SET thumbsUp = thumbsUp + 1 WHERE id = :reviewId")
    void incrementThumbsUp(int reviewId);

    /** Thumbs-down a review */
    @Query("UPDATE reviews SET thumbsDown = thumbsDown + 1 WHERE id = :reviewId")
    void incrementThumbsDown(int reviewId);
}

