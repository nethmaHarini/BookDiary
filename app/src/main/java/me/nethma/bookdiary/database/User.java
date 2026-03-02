package me.nethma.bookdiary.database;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "users",
    indices = { @Index(value = "email", unique = true) }
)
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String email;

    @Nullable
    public String password;   // null for Google-only accounts

    @Nullable
    public String googleId;   // null for email/password accounts

    @Nullable
    public String photoUrl;   // Google profile photo URL

    // Email/password constructor — ignored by Room, used by app code
    @Ignore
    public User(String username, String email, String password) {
        this.username = username;
        this.email    = email;
        this.password = password;
    }

    // Google sign-in constructor — used by Room as the primary constructor
    public User(String username, String email, String googleId, String photoUrl) {
        this.username = username;
        this.email    = email;
        this.googleId = googleId;
        this.photoUrl = photoUrl;
    }
}


