package com.example.favoriteuserinterface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Book> bookList;
    BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // connect xml layout
        setContentView(R.layout.activity_favorites);

        // connect RecyclerView
        recyclerView = findViewById(R.id.bookRecycler);

        // set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create array list
        bookList = new ArrayList<>();

        // add books
        bookList.add(new Book(
                "The Great Gatsby",
                "F. Scott Fitzgerald",
                "4.8",
                R.drawable.gatsby
        ));

        bookList.add(new Book(
                "1984",
                "George Orwell",
                "4.9",
                R.drawable.book1984
        ));

        bookList.add(new Book(
                "Dune",
                "Frank Herbert",
                "4.7",
                R.drawable.dune
        ));

        bookList.add(new Book(
                "Atomic Habits",
                "James Clear",
                "5.0",
                R.drawable.atomic
        ));

        // set adapter
        adapter = new BookAdapter(this, bookList);
        recyclerView.setAdapter(adapter);
    }
}