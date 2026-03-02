package me.nethma.bookdiary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etEmail;
    private Button btnFindAccount;
    private TextView tvLogin;

    private UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.forgot_root), (v, insets) -> {
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
        btnBack        = findViewById(R.id.btn_back);
        etEmail        = findViewById(R.id.et_email);
        btnFindAccount = findViewById(R.id.btn_find_account);
        tvLogin        = findViewById(R.id.tv_login);
    }

    private void setListeners() {

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Find Account button → check if email exists in Room
        btnFindAccount.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Please enter your email address");
                etEmail.requestFocus();
                return;
            }

            btnFindAccount.setEnabled(false);
            executor.execute(() -> {
                User user = userDao.findByEmail(email);
                runOnUiThread(() -> {
                    btnFindAccount.setEnabled(true);
                    if (user != null) {
                        // Account found → navigate to Reset Password screen
                        Toast.makeText(this,
                                "Account found! You can now reset your password.",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, ResetPasswordActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this,
                                "No account found with this email.",
                                Toast.LENGTH_SHORT).show();
                        etEmail.setError("No account found with this email");
                        etEmail.requestFocus();
                    }
                });
            });
        });

        // "Log in" link → go back to Login
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




