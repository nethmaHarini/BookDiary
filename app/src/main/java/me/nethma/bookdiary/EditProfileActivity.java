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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.User;
import me.nethma.bookdiary.database.UserDao;
import me.nethma.bookdiary.utils.SessionManager;

public class EditProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private UserDao userDao;
    private ExecutorService executor;
    private Handler mainHandler;

    private ImageView ivAvatar;
    private EditText etFullName, etEmail;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private LinearLayout photoActions;
    private boolean photoActionsVisible = false;

    // password visibility states
    private boolean showCurrent = false, showNew = false, showConfirm = false;

    // selected local photo URI (if user picks from gallery / camera)
    private Uri selectedPhotoUri = null;
    private Bitmap selectedPhotoBitmap = null;
    private boolean photoRemoved = false; // true when user explicitly taps "Remove Photo"

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    photoRemoved     = false; // picking a new photo cancels any pending removal
                    loadBitmapFromUri(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);
        userDao = AppDatabase.getInstance(this).userDao();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        bindViews();
        populateExistingData();
        setupListeners();
    }

    private void bindViews() {
        ivAvatar          = findViewById(R.id.iv_avatar);
        etFullName        = findViewById(R.id.et_full_name);
        etEmail           = findViewById(R.id.et_email);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword     = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        photoActions      = findViewById(R.id.photo_actions);
    }

    /** Pre-populate all fields with current session / DB data */
    private void populateExistingData() {
        String username = sessionManager.getUsername();
        String email    = sessionManager.getEmail();
        String photoUrl = sessionManager.getPhotoUrl();

        etFullName.setText(username);
        etEmail.setText(email);

        // Move cursor to end of name field
        if (username != null) etFullName.setSelection(username.length());

        // Load avatar — local file path OR remote URL
        if (photoUrl != null && !photoUrl.isEmpty()) {
            if (photoUrl.startsWith("/") || photoUrl.startsWith("file://")) {
                loadLocalImage(photoUrl);
            } else {
                loadNetworkImage(photoUrl);
            }
        } else {
            showInitialsAvatar(username);
        }
    }

    private void setupListeners() {
        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Camera badge & "Change Photo" text — toggle action panel
        View cameraBadge = findViewById(R.id.btn_camera_badge);
        TextView changePhotoText = findViewById(R.id.btn_change_photo_text);
        View.OnClickListener togglePhoto = v -> togglePhotoActions();
        cameraBadge.setOnClickListener(togglePhoto);
        changePhotoText.setOnClickListener(togglePhoto);

        // Photo action buttons
        findViewById(R.id.btn_upload_photo).setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
            hidePhotoActions();
        });

        findViewById(R.id.btn_take_photo).setOnClickListener(v -> {
            Toast.makeText(this, "Camera coming soon", Toast.LENGTH_SHORT).show();
            hidePhotoActions();
        });

        findViewById(R.id.btn_remove_photo).setOnClickListener(v -> {
            selectedPhotoUri    = null;
            selectedPhotoBitmap = null;
            photoRemoved        = true;   // flag so save knows to wipe the path
            showInitialsAvatar(sessionManager.getUsername());
            hidePhotoActions();
        });

        // Password visibility toggles
        ImageView toggleCurrent = findViewById(R.id.toggle_current_password);
        ImageView toggleNew     = findViewById(R.id.toggle_new_password);
        ImageView toggleConfirm = findViewById(R.id.toggle_confirm_password);

        toggleCurrent.setOnClickListener(v -> {
            showCurrent = !showCurrent;
            toggleVisibility(etCurrentPassword, toggleCurrent, showCurrent);
        });
        toggleNew.setOnClickListener(v -> {
            showNew = !showNew;
            toggleVisibility(etNewPassword, toggleNew, showNew);
        });
        toggleConfirm.setOnClickListener(v -> {
            showConfirm = !showConfirm;
            toggleVisibility(etConfirmPassword, toggleConfirm, showConfirm);
        });

        // Save button
        findViewById(R.id.btn_save).setOnClickListener(v -> attemptSave());
    }

    private void togglePhotoActions() {
        if (photoActionsVisible) hidePhotoActions();
        else showPhotoActions();
    }

    private void showPhotoActions() {
        photoActions.setVisibility(View.VISIBLE);
        photoActionsVisible = true;
    }

    private void hidePhotoActions() {
        photoActions.setVisibility(View.GONE);
        photoActionsVisible = false;
    }

    private void toggleVisibility(EditText field, ImageView icon, boolean show) {
        int pos = field.getSelectionEnd();
        if (show) {
            field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            icon.setImageResource(R.drawable.ic_visibility);
        } else {
            field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            icon.setImageResource(R.drawable.ic_visibility_off);
        }
        field.setSelection(Math.max(0, pos));
    }

    private void attemptSave() {
        String newName        = etFullName.getText().toString().trim();
        String currentPwd     = etCurrentPassword.getText().toString();
        String newPwd         = etNewPassword.getText().toString();
        String confirmPwd     = etConfirmPassword.getText().toString();

        // Validate name
        if (newName.isEmpty()) {
            etFullName.setError("Name cannot be empty");
            etFullName.requestFocus();
            return;
        }

        boolean changingPassword = !currentPwd.isEmpty() || !newPwd.isEmpty() || !confirmPwd.isEmpty();

        if (changingPassword) {
            if (currentPwd.isEmpty()) {
                etCurrentPassword.setError("Enter your current password");
                etCurrentPassword.requestFocus();
                return;
            }
            if (newPwd.length() < 8) {
                etNewPassword.setError("Password must be at least 8 characters");
                etNewPassword.requestFocus();
                return;
            }
            if (!newPwd.equals(confirmPwd)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }
        }

        int userId = sessionManager.getUserId();
        String email = sessionManager.getEmail();
        String finalNewName = newName;
        String hashedCurrent = changingPassword ? hashPassword(currentPwd) : null;
        String hashedNew     = changingPassword ? hashPassword(newPwd)     : null;

        executor.execute(() -> {
            // Verify current password if changing
            if (changingPassword) {
                User user = userDao.findById(userId);
                if (user == null || user.password == null || !user.password.equals(hashedCurrent)) {
                    mainHandler.post(() -> {
                        etCurrentPassword.setError("Current password is incorrect");
                        etCurrentPassword.requestFocus();
                    });
                    return;
                }
            }

            // Save username
            userDao.updateUsername(userId, finalNewName);

            // Save password if changed
            if (changingPassword) {
                userDao.updatePasswordById(userId, hashedNew);
            }

            // Determine final photo URL:
            //   1. user removed photo  → clear it (empty string)
            //   2. user picked a new photo → save it and use the new path
            //   3. no change → keep existing
            String finalPhotoUrl;
            if (photoRemoved) {
                finalPhotoUrl = "";
            } else if (selectedPhotoBitmap != null) {
                String savedPath = savePhotoLocally(selectedPhotoBitmap);
                finalPhotoUrl = savedPath != null ? savedPath : sessionManager.getPhotoUrl();
            } else {
                finalPhotoUrl = sessionManager.getPhotoUrl();
            }

            // Persist session with (possibly updated) photo path
            final String photoUrlForSession = finalPhotoUrl;
            mainHandler.post(() -> {
                sessionManager.saveSession(userId, finalNewName, email, photoUrlForSession);
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    /** Saves bitmap to app-private storage and returns the absolute file path, or null on failure. */
    private String savePhotoLocally(Bitmap bitmap) {
        try {
            File dir = new File(getFilesDir(), "profile_photos");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "avatar_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }

    // ── Avatar loading helpers ──

    private void loadBitmapFromUri(Uri uri) {
        executor.execute(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap raw = BitmapFactory.decodeStream(is);
                Bitmap circular = toCircleBitmap(raw);
                selectedPhotoBitmap = raw;
                mainHandler.post(() -> setAvatarBitmap(circular));
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT).show());
            }
        });
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
        ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivAvatar.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}









