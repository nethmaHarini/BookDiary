package me.nethma.bookdiary;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.UserDao;
import me.nethma.bookdiary.utils.PasswordUtils;

public class ResetPasswordActivity extends BaseActivity {

    private ImageButton btnBack, btnToggleNew, btnToggleConfirm;
    private EditText etNewPassword, etConfirmPassword;
    private Button btnReset;
    private TextView tvLogin;

    private boolean newPasswordVisible   = false;
    private boolean confirmPasswordVisible = false;

    private String email;
    private UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.reset_root), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                });

        // Get the email passed from ForgotPasswordActivity
        email = getIntent().getStringExtra("email");

        userDao = AppDatabase.getInstance(this).userDao();

        bindViews();
        setListeners();
    }

    private void bindViews() {
        btnBack           = findViewById(R.id.btn_back);
        etNewPassword     = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnToggleNew      = findViewById(R.id.btn_toggle_new);
        btnToggleConfirm  = findViewById(R.id.btn_toggle_confirm);
        btnReset          = findViewById(R.id.btn_reset);
        tvLogin           = findViewById(R.id.tv_login);
    }

    private void setListeners() {

        btnBack.setOnClickListener(v -> finish());

        // Toggle new password visibility
        btnToggleNew.setOnClickListener(v -> {
            newPasswordVisible = !newPasswordVisible;
            if (newPasswordVisible) {
                etNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnToggleNew.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnToggleNew.setImageResource(R.drawable.ic_visibility);
            }
            etNewPassword.setSelection(etNewPassword.getText().length());
        });

        // Toggle confirm password visibility
        btnToggleConfirm.setOnClickListener(v -> {
            confirmPasswordVisible = !confirmPasswordVisible;
            if (confirmPasswordVisible) {
                etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnToggleConfirm.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnToggleConfirm.setImageResource(R.drawable.ic_visibility);
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });

        // Reset Password button
        btnReset.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString();
            String confirm     = etConfirmPassword.getText().toString();

            if (newPassword.length() < 8) {
                etNewPassword.setError("Password must be at least 8 characters");
                etNewPassword.requestFocus();
                return;
            }
            if (!newPassword.equals(confirm)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            btnReset.setEnabled(false);
            executor.execute(() -> {
                String hashedPassword = PasswordUtils.hash(newPassword);
                userDao.updatePassword(email, hashedPassword);
                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "Password reset successfully! Please log in.",
                            Toast.LENGTH_SHORT).show();
                    // Navigate to Login, clear the back stack
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            });
        });

        // Log in link
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

