package me.nethma.bookdiary.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookDao {

    @Insert
    long insert(Book book);

    @Update
    void update(Book book);

    @Delete
    void delete(Book book);

    /** All books for a user, newest first */
    @Query("SELECT * FROM books WHERE userId = :userId ORDER BY dateAdded DESC")
    List<Book> getAllBooks(int userId);

    /** Favourite books for a user, newest first */
    @Query("SELECT * FROM books WHERE userId = :userId AND isFavorite = 1 ORDER BY dateAdded DESC")
    List<Book> getFavoriteBooks(int userId);

    /** Books filtered by category */
    @Query("SELECT * FROM books WHERE userId = :userId AND category = :category ORDER BY dateAdded DESC")
    List<Book> getBooksByCategory(int userId, String category);

    /** Full-text search across title and author */
    @Query("SELECT * FROM books WHERE userId = :userId " +
           "AND (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%') " +
           "ORDER BY dateAdded DESC")
    List<Book> searchBooks(int userId, String query);

    /**
     * Combined search + category filter.
     * Pass category = "All" to skip category filtering.
     */
    @Query("SELECT * FROM books WHERE userId = :userId " +
           "AND (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%') " +
           "AND (:category = 'All' OR category = :category) " +
           "ORDER BY dateAdded DESC")
    List<Book> searchAndFilter(int userId, String query, String category);

    /** Single book by primary key */
    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    Book getBookById(int bookId);

    /** Total count for a user */
    @Query("SELECT COUNT(*) FROM books WHERE userId = :userId")
    int getBookCount(int userId);

    /** Favourite count for a user */
    @Query("SELECT COUNT(*) FROM books WHERE userId = :userId AND isFavorite = 1")
    int getFavoriteCount(int userId);
}

