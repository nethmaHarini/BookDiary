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

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageButton btnTogglePassword;
    private Button btnLogin;
    private LinearLayout btnGoogle;
    private TextView tvForgotPassword, tvCreateAccount;

    private boolean passwordVisible = false;
    private UserDao userDao;
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

        userDao = AppDatabase.getInstance(this).userDao();

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

        // Log In button → query Room on background thread
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
                User user = userDao.login(email, password);
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    if (user != null) {
                        Toast.makeText(this,
                                "Welcome back, " + user.username + "!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Invalid email or password.", Toast.LENGTH_SHORT).show();
                        etPassword.setError("Invalid email or password");
                        etPassword.requestFocus();
                    }
                });
            });
        });

        // Google sign-in
        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google sign-in coming soon", Toast.LENGTH_SHORT).show()
        );

        // Forgot password
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );

        // Create account
        tvCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
