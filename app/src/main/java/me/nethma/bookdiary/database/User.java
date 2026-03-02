package me.nethma.bookdiary.database;

import androidx.room.Entity;
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
    public String password;   // stored as plain text for now; hash in production

    public User(String username, String email, String password) {
        this.username = username;
        this.email    = email;
        this.password = password;
    }
}

