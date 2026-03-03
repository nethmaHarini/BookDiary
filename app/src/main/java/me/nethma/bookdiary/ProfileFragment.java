package me.nethma.bookdiary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private SessionManager sessionManager;
    private ImageView ivAvatar;
    private TextView tvUsername, tvEmail, tvTotalBooks, tvFavourites, tvReviews;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Launch EditProfileActivity and refresh on OK result
    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    // Refresh displayed data after save
                    populateUserInfo();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(requireContext());

        ivAvatar     = view.findViewById(R.id.iv_avatar);
        tvUsername   = view.findViewById(R.id.tv_username);
        tvEmail      = view.findViewById(R.id.tv_email);
        tvTotalBooks = view.findViewById(R.id.tv_total_books);
        tvFavourites = view.findViewById(R.id.tv_favourites);
        tvReviews    = view.findViewById(R.id.tv_reviews);

        // Back button
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });

        // Settings button
        view.findViewById(R.id.btn_settings).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Settings coming soon", Toast.LENGTH_SHORT).show());

        // Edit avatar badge — open edit profile directly
        view.findViewById(R.id.btn_edit_avatar).setOnClickListener(v -> openEditProfile());

        // Edit Profile row — opens EditProfileActivity
        view.findViewById(R.id.row_edit_profile).setOnClickListener(v -> openEditProfile());

        view.findViewById(R.id.row_notifications).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NotificationSettingsActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.row_theme).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Theme preferences coming soon", Toast.LENGTH_SHORT).show());

        // Logout
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutDialog());

        populateUserInfo();
        return view;
    }

    private void openEditProfile() {
        Intent intent = new Intent(requireContext(), EditProfileActivity.class);
        editProfileLauncher.launch(intent);
    }

    private void populateUserInfo() {
        String username = sessionManager.getUsername();
        String email    = sessionManager.getEmail();
        String photoUrl = sessionManager.getPhotoUrl();

        tvUsername.setText(username != null && !username.isEmpty() ? username : "Book Reader");
        tvEmail.setText(email != null && !email.isEmpty() ? email : "");

        if (photoUrl != null && !photoUrl.isEmpty()) {
            // Check if it's a local file path or a remote URL
            if (photoUrl.startsWith("/") || photoUrl.startsWith("file://")) {
                loadLocalImage(photoUrl);
            } else {
                loadNetworkImage(photoUrl);
            }
        } else {
            showInitialsAvatar(username);
        }

        tvTotalBooks.setText("0");
        tvFavourites.setText("0");
        tvReviews.setText("0");
    }

    private void loadLocalImage(String path) {
        executor.execute(() -> {
            try {
                Bitmap raw = BitmapFactory.decodeFile(path.replace("file://", ""));
                if (raw != null) {
                    Bitmap circular = toCircleBitmap(raw);
                    mainHandler.post(() -> setAvatarBitmap(circular));
                } else {
                    mainHandler.post(() -> showInitialsAvatar(sessionManager.getUsername()));
                }
            } catch (Exception e) {
                mainHandler.post(() -> showInitialsAvatar(sessionManager.getUsername()));
            }
        });
    }

    /** Load a remote image URL and set it as a circular avatar. */
    private void loadNetworkImage(String urlStr) {
        executor.execute(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                Bitmap raw = BitmapFactory.decodeStream(conn.getInputStream());
                Bitmap circular = toCircleBitmap(raw);
                mainHandler.post(() -> setAvatarBitmap(circular));
            } catch (Exception e) {
                mainHandler.post(() -> showInitialsAvatar(sessionManager.getUsername()));
            }
        });
    }

    private void setAvatarBitmap(Bitmap bitmap) {
        if (isAdded() && ivAvatar != null) {
            ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivAvatar.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        }
    }

    /** Crop a bitmap into a circle. */
    private Bitmap toCircleBitmap(Bitmap src) {
        int size = Math.min(src.getWidth(), src.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap scaled = Bitmap.createScaledBitmap(src, size, size, true);
        BitmapShader shader = new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        return output;
    }

    /** Draw an initials circle as the avatar when no photo is available. */
    private void showInitialsAvatar(String name) {
        String initials = getInitials(name);
        int size = 200;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xFF1152D4);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.38f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(initials, size / 2f, yPos, textPaint);
        setAvatarBitmap(bitmap);
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of your account?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
