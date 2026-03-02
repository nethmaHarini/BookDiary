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

    // Login: find user by email + password
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    // Check if email already registered
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    // Reset password: update password for a given email
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    void updatePassword(String email, String newPassword);
}


