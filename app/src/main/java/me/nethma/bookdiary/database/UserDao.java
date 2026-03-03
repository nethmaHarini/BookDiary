package me.nethma.bookdiary.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {

    // Register: insert a new user; throws if email already exists (ABORT)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(User user);

    // Login: find user by email only — password verified in Java via PasswordUtils
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmailForLogin(String email);

    // Check if email already registered
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    // Reset password: update password for a given email
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    void updatePassword(String email, String newPassword);

    // Google Sign-In: find existing user by Google ID
    @Query("SELECT * FROM users WHERE googleId = :googleId LIMIT 1")
    User findByGoogleId(String googleId);

    // Google Sign-In: insert with IGNORE so existing account is not overwritten
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertGoogleUser(User user);

    // Edit Profile: update username for a given user id
    @Query("UPDATE users SET username = :username WHERE id = :userId")
    void updateUsername(int userId, String username);

    // Edit Profile: update password for a given user id
    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    void updatePasswordById(int userId, String newPassword);

    // Get user by id
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User findById(int userId);
}


