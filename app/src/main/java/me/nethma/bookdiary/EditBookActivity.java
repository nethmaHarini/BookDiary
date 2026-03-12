package me.nethma.bookdiary;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.Book;

public class EditBookActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "book_id";

    // ── Views ─────────────────────────────────────────────────────────────────
    private View      coverPlaceholder;
    private ImageView ivCoverPreview;
    private EditText  etTitle, etAuthor, etReview;
    private Spinner   spinnerCategory, spinnerStatus;
    private ImageView[] stars;

    // ── State ─────────────────────────────────────────────────────────────────
    private int    currentRating  = 0;
    private String coverImagePath = null;
    private Book   currentBook    = null;

    // ── Threading ─────────────────────────────────────────────────────────────
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ── Image picker ──────────────────────────────────────────────────────────
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) onImagePicked(uri);
            });

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_book);

        // Bind views
        coverPlaceholder = findViewById(R.id.cover_placeholder);
        ivCoverPreview   = findViewById(R.id.iv_cover_preview);
        etTitle          = findViewById(R.id.et_title);
        etAuthor         = findViewById(R.id.et_author);
        etReview         = findViewById(R.id.et_review);
        spinnerCategory  = findViewById(R.id.spinner_category);
        spinnerStatus    = findViewById(R.id.spinner_status);

        stars = new ImageView[]{
                findViewById(R.id.star_1),
                findViewById(R.id.star_2),
                findViewById(R.id.star_3),
                findViewById(R.id.star_4),
                findViewById(R.id.star_5)
        };

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Cover picker
        findViewById(R.id.cover_picker).setOnClickListener(v ->
                pickImage.launch("image/*"));

        // Stars
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> setRating(rating));
        }

        // Buttons
        findViewById(R.id.btn_save).setOnClickListener(v -> saveChanges());
        findViewById(R.id.btn_delete).setOnClickListener(v -> confirmDelete());

        // Load book data
        int bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, -1);
        if (bookId != -1) {
            loadBook(bookId);
        } else {
            finish();
        }
    }

    // ── Load existing book data ───────────────────────────────────────────────

    private void loadBook(int bookId) {
        executor.execute(() -> {
            Book book = AppDatabase.getInstance(this).bookDao().getBookById(bookId);
            mainHandler.post(() -> {
                if (book == null) { finish(); return; }
                currentBook = book;
                populateForm(book);
            });
        });
    }

    private void populateForm(Book book) {
        etTitle.setText(book.title);
        etAuthor.setText(book.author);
        etReview.setText(book.notes != null ? book.notes : "");

        // Category spinner
        String[] categories = getResources().getStringArray(R.array.book_categories);
        int catIndex = Arrays.asList(categories).indexOf(book.category);
        if (catIndex >= 0) spinnerCategory.setSelection(catIndex);

        // Status spinner
        String[] statuses = getResources().getStringArray(R.array.reading_statuses);
        if (book.readingStatus != null) {
            int stIdx = Arrays.asList(statuses).indexOf(book.readingStatus);
            if (stIdx >= 0) spinnerStatus.setSelection(stIdx);
        }

        // Rating stars
        setRating(Math.round(book.rating));

        // Cover image
        if (book.coverUrl != null && !book.coverUrl.isEmpty()) {
            coverImagePath = book.coverUrl;
            Bitmap bm = BitmapFactory.decodeFile(book.coverUrl);
            if (bm != null) {
                ivCoverPreview.setImageBitmap(bm);
                ivCoverPreview.setVisibility(View.VISIBLE);
                coverPlaceholder.setVisibility(View.GONE);
            }
        }
    }

    // ── Image handling ────────────────────────────────────────────────────────

    private void onImagePicked(Uri uri) {
        executor.execute(() -> {
            String path = copyImageToInternal(uri);
            mainHandler.post(() -> {
                coverImagePath = path;
                if (path != null) {
                    Bitmap bm = BitmapFactory.decodeFile(path);
                    if (bm != null) {
                        ivCoverPreview.setImageBitmap(bm);
                        ivCoverPreview.setVisibility(View.VISIBLE);
                        coverPlaceholder.setVisibility(View.GONE);
                    }
                }
            });
        });
    }

    private String copyImageToInternal(Uri uri) {
        try {
            File dir = new File(getFilesDir(), "covers");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File dest = new File(dir, UUID.randomUUID() + ".jpg");
            try (InputStream  in  = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[4096];
                int len;
                while (in != null && (len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
            return dest.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    // ── Star rating ───────────────────────────────────────────────────────────

    private void setRating(int rating) {
        currentRating = rating;
        boolean dark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int filledClr = 0xFFFBBF24;
        int emptyClr  = dark ? 0xFF334155 : 0xFFCBD5E1;

        for (int i = 0; i < 5; i++) {
            stars[i].setImageTintList(
                    ColorStateList.valueOf(i < rating ? filledClr : emptyClr));
        }
    }

    // ── Save changes ──────────────────────────────────────────────────────────

    private void saveChanges() {
        if (currentBook == null) return;

        String title  = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError(getString(R.string.err_title_empty));
            etTitle.requestFocus();
            return;
        }
        if (author.isEmpty()) {
            etAuthor.setError(getString(R.string.err_author_empty));
            etAuthor.requestFocus();
            return;
        }

        currentBook.title         = title;
        currentBook.author        = author;
        currentBook.category      = spinnerCategory.getSelectedItem().toString();
        currentBook.readingStatus = spinnerStatus.getSelectedItem().toString();
        currentBook.rating        = currentRating;
        currentBook.notes         = etReview.getText().toString().trim();
        currentBook.coverUrl      = coverImagePath;

        Book bookToSave = currentBook;
        executor.execute(() -> {
            AppDatabase.getInstance(this).bookDao().update(bookToSave);
            mainHandler.post(() -> {
                Toast.makeText(this,
                        getString(R.string.msg_book_updated), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    // ── Delete with confirmation ───────────────────────────────────────────────

    private void confirmDelete() {
        if (currentBook == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.delete_confirm_yes, (d, w) -> deleteBook())
                .setNegativeButton(R.string.delete_confirm_no, null)
                .show();
    }

    private void deleteBook() {
        Book bookToDelete = currentBook;
        executor.execute(() -> {
            AppDatabase.getInstance(this).bookDao().delete(bookToDelete);
            mainHandler.post(() -> {
                Toast.makeText(this,
                        getString(R.string.msg_book_deleted), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}

