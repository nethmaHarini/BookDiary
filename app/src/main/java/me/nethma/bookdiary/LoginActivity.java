package me.nethma.bookdiary;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.nethma.bookdiary.database.AppDatabase;
import me.nethma.bookdiary.database.User;
import me.nethma.bookdiary.database.UserDao;
import me.nethma.bookdiary.utils.GoogleSignInHelper;
import me.nethma.bookdiary.utils.PasswordUtils;
import me.nethma.bookdiary.utils.SessionManager;

public class LoginActivity extends BaseActivity {

    private EditText etEmail, etPassword;
    private ImageButton btnTogglePassword;
    private Button btnLogin;
    private LinearLayout btnGoogle;
    private TextView tvForgotPassword, tvCreateAccount;

    private boolean passwordVisible = false;
    private UserDao userDao;
    private SessionManager sessionManager;
    private GoogleSignInHelper googleSignInHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.login_root), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                });

        userDao             = AppDatabase.getInstance(this).userDao();
        sessionManager      = new SessionManager(this);
        googleSignInHelper  = new GoogleSignInHelper(this);

        bindViews();
        setListeners();
    }

    private void bindViews() {
        etEmail           = findViewById(R.id.et_email);
        etPassword        = findViewById(R.id.et_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnLogin          = findViewById(R.id.btn_login);
        btnGoogle         = findViewById(R.id.btn_google);
        tvForgotPassword  = findViewById(R.id.tv_forgot_password);
        tvCreateAccount   = findViewById(R.id.tv_create_account);
    }

    private void setListeners() {

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

        // Email / password login
        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (email.isEmpty()) {
                etEmail.setError("Please enter your email");
                etEmail.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Please enter your password");
                etPassword.requestFocus();
                return;
            }

            btnLogin.setEnabled(false);
            executor.execute(() -> {
                User user = userDao.findByEmailForLogin(email);
                boolean matched = false;
                if (user != null && user.password != null) {
                    matched = PasswordUtils.verify(password, user.password);
                    // Auto-upgrade legacy plain-text passwords on successful login
                    if (matched && !user.password.contains("$")) {
                        String rehashed = PasswordUtils.hash(password);
                        userDao.updatePasswordById(user.id, rehashed);
                    }
                }
                final boolean success = matched;
                final User loggedInUser = user;
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    if (success) {
                        sessionManager.saveSession(loggedInUser.id, loggedInUser.username, loggedInUser.email, loggedInUser.photoUrl);
                        Toast.makeText(this,
                                "Welcome back, " + loggedInUser.username + "!", Toast.LENGTH_SHORT).show();
                        goToMain();
                    } else {
                        Toast.makeText(this,
                                "Invalid email or password.", Toast.LENGTH_SHORT).show();
                        etPassword.setError("Invalid email or password");
                        etPassword.requestFocus();
                    }
                });
            });
        });

        // Google Sign-In
        btnGoogle.setOnClickListener(v -> {
            btnGoogle.setEnabled(false);
            googleSignInHelper.signIn(new GoogleSignInHelper.Callback() {
                @Override
                public void onSuccess(String email, String displayName,
                                      String photoUrl, String googleId) {
                    executor.execute(() -> {
                        User existing = userDao.findByGoogleId(googleId);
                        if (existing == null) {
                            existing = userDao.findByEmail(email);
                            if (existing != null) {
                                // Link Google ID to existing email/password account
                                existing.googleId = googleId;
                                existing.photoUrl = photoUrl;
                                userDao.insertUser(existing);
                            } else {
                                // Brand new Google user
                                User newUser = new User(displayName, email, googleId, photoUrl);
                                userDao.insertGoogleUser(newUser);
                                existing = userDao.findByGoogleId(googleId);
                            }
                        }
                        final String welcomeName = existing != null ? existing.username : displayName;
                        final int    savedId    = existing != null ? existing.id : -1;
                        runOnUiThread(() -> {
                            btnGoogle.setEnabled(true);
                            sessionManager.saveSession(savedId, welcomeName, email, photoUrl);
                            Toast.makeText(LoginActivity.this,
                                    "Welcome, " + welcomeName + "!", Toast.LENGTH_SHORT).show();
                            goToMain();
                        });
                    });
                }

                @Override
                public void onError(String message) {
                    btnGoogle.setEnabled(true);
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });

        // Forgot password
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        // Create account
        tvCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void goToMain() {
        if (!sessionManager.hasSelectedTopics()) {
            // New user — must pick topics first
            Intent intent = new Intent(this, TopicSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
