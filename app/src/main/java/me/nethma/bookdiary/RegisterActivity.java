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
import me.nethma.bookdiary.database.User;
import me.nethma.bookdiary.database.UserDao;
import me.nethma.bookdiary.utils.PasswordUtils;

public class RegisterActivity extends BaseActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private ImageButton btnTogglePassword, btnBack;
    private Button btnRegister;
    private TextView tvLogin;

    private boolean passwordVisible = false;
    private UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.register_root), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                });

        userDao = AppDatabase.getInstance(this).userDao();

        bindViews();
        setListeners();
    }

    private void bindViews() {
        etUsername        = findViewById(R.id.et_username);
        etEmail           = findViewById(R.id.et_email);
        etPassword        = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnRegister       = findViewById(R.id.btn_register);
        btnBack           = findViewById(R.id.btn_back);
        tvLogin           = findViewById(R.id.tv_login);
    }

    private void setListeners() {

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Toggle password visibility
        btnTogglePassword.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Create Account button
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirm  = etConfirmPassword.getText().toString();

            // Validate inputs on UI thread
            if (username.isEmpty()) {
                etUsername.setError("Please enter your username");
                etUsername.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                etEmail.setError("Please enter your email");
                etEmail.requestFocus();
                return;
            }
            if (password.length() < 8) {
                etPassword.setError("Password must be at least 8 characters");
                etPassword.requestFocus();
                return;
            }
            if (!password.equals(confirm)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            // Database work on background thread
            btnRegister.setEnabled(false);
            executor.execute(() -> {
                User existing = userDao.findByEmail(email);
                if (existing != null) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        etEmail.setError("Email already registered");
                        etEmail.requestFocus();
                    });
                    return;
                }

                try {
                    String hashedPassword = PasswordUtils.hash(password);
                    userDao.insertUser(new User(username, email, hashedPassword));
                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                "Account created! Please log in.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        Toast.makeText(this,
                                "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        // Already have an account? → go back to Login
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
