package me.nethma.bookdiary;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.Book;
import me.nethma.bookdiary.utils.SessionManager;

public class AddFragment extends BaseFragment {

    // ── Views ─────────────────────────────────────────────────────────────────
    private View      coverPlaceholder;
    private ImageView ivCoverPreview;
    private EditText  etTitle, etAuthor, etReview;
    private Spinner   spinnerCategory;
    private ImageView[] stars;

    // ── State ─────────────────────────────────────────────────────────────────
    private int    currentRating  = 0;
    private String coverImagePath = null;

    // ── Threading ─────────────────────────────────────────────────────────────
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ── Dependencies ──────────────────────────────────────────────────────────
    private SessionManager sessionManager;
    private AppDatabase    db;

    // ── Image picker launcher (registered before onStart) ────────────────────
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) onImagePicked(uri);
            });

    // ─────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add, container, false);

        sessionManager = new SessionManager(requireContext());
        db             = AppDatabase.getInstance(requireContext());

        // Bind views
        coverPlaceholder = view.findViewById(R.id.cover_placeholder);
        ivCoverPreview   = view.findViewById(R.id.iv_cover_preview);
        etTitle          = view.findViewById(R.id.et_title);
        etAuthor         = view.findViewById(R.id.et_author);
        etReview         = view.findViewById(R.id.et_review);
        spinnerCategory  = view.findViewById(R.id.spinner_category);

        stars = new ImageView[]{
                view.findViewById(R.id.star_1),
                view.findViewById(R.id.star_2),
                view.findViewById(R.id.star_3),
                view.findViewById(R.id.star_4),
                view.findViewById(R.id.star_5)
        };

        // Cover picker click → open gallery
        view.findViewById(R.id.cover_picker).setOnClickListener(v ->
                pickImage.launch("image/*"));

        // Star click listeners
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> setRating(rating));
        }
        setRating(0); // all grey initially

        // Save button
        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveBook());

        return view;
    }

    // ── Image handling ────────────────────────────────────────────────────────

    private void onImagePicked(Uri uri) {
        executor.execute(() -> {
            String path = copyImageToInternal(uri);
            mainHandler.post(() -> {
                if (!isAdded()) return;
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
            File dir = new File(requireContext().getFilesDir(), "covers");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File dest = new File(dir, UUID.randomUUID() + ".jpg");
            try (InputStream  in  = requireContext().getContentResolver().openInputStream(uri);
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

    // ── Interactive star rating ───────────────────────────────────────────────

    private void setRating(int rating) {
        currentRating = rating;
        boolean dark = (requireContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int filledClr = 0xFFFBBF24;                       // amber/yellow
        int emptyClr  = dark ? 0xFF334155 : 0xFFCBD5E1;  // slate grey

        for (int i = 0; i < 5; i++) {
            stars[i].setImageTintList(
                    ColorStateList.valueOf(i < rating ? filledClr : emptyClr));
        }
    }

    // ── Validate & save ───────────────────────────────────────────────────────

    private void saveBook() {
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

        String category  = spinnerCategory.getSelectedItem().toString();
        String review    = etReview.getText().toString().trim();
        int    userId    = sessionManager.getUserId();
        String coverPath = coverImagePath;
        int    rating    = currentRating;

        executor.execute(() -> {
            Book b        = new Book();
            b.userId      = userId;
            b.title       = title;
            b.author      = author;
            b.category    = category;
            b.rating      = rating;
            b.notes       = review;
            b.coverUrl    = coverPath;
            b.isFavorite  = false;
            b.readingStatus = getString(R.string.reading_status_default);
            b.dateAdded   = System.currentTimeMillis();
            db.bookDao().insert(b);

            mainHandler.post(() -> {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        getString(R.string.msg_book_saved), Toast.LENGTH_SHORT).show();
                clearForm();
                // Navigate to Home so the new book is immediately visible
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateHome();
                }
            });
        });
    }

    // ── Reset form ────────────────────────────────────────────────────────────

    private void clearForm() {
        etTitle.setText("");
        etAuthor.setText("");
        etReview.setText("");
        spinnerCategory.setSelection(0);
        setRating(0);
        coverImagePath = null;
        ivCoverPreview.setVisibility(View.GONE);
        coverPlaceholder.setVisibility(View.VISIBLE);
    }
}
