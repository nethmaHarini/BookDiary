package me.nethma.bookdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

@SuppressWarnings("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private View logoContainer;
    private TextView tvAppName;
    private TextView tvTagline;
    private View loadingContainer;
    private View tvVersion;

    private View dot1, dot2, dot3;
    private final Handler dotHandler = new Handler(Looper.getMainLooper());
    private int dotStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.splash_root), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top,
                            systemBars.right, systemBars.bottom);
                    return insets;
                });

        bindViews();
        startSplashAnimation();
    }

    private void bindViews() {
        logoContainer    = findViewById(R.id.logo_container);
        tvAppName        = findViewById(R.id.tv_app_name);
        tvTagline        = findViewById(R.id.tv_tagline);
        loadingContainer = findViewById(R.id.loading_container);
        tvVersion        = findViewById(R.id.tv_version);
        dot1             = findViewById(R.id.dot1);
        dot2             = findViewById(R.id.dot2);
        dot3             = findViewById(R.id.dot3);
    }

    private void startSplashAnimation() {
        // Logo pop-in (scale + fade)
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoContainer, View.SCALE_X, 0.6f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoContainer, View.SCALE_Y, 0.6f, 1f);
        ObjectAnimator logoAlpha  = ObjectAnimator.ofFloat(logoContainer, View.ALPHA,  0f, 1f);
        logoScaleX.setInterpolator(new OvershootInterpolator(1.5f));
        logoScaleY.setInterpolator(new OvershootInterpolator(1.5f));
        logoAlpha.setInterpolator(new DecelerateInterpolator());
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(logoScaleX, logoScaleY, logoAlpha);
        logoAnim.setDuration(550);
        logoAnim.setStartDelay(200);

        // App name slide-up + fade
        ObjectAnimator nameAlpha  = ObjectAnimator.ofFloat(tvAppName, View.ALPHA, 0f, 1f);
        ObjectAnimator nameTransY = ObjectAnimator.ofFloat(tvAppName, View.TRANSLATION_Y, 24f, 0f);
        nameAlpha.setInterpolator(new DecelerateInterpolator());
        nameTransY.setInterpolator(new DecelerateInterpolator());
        AnimatorSet nameAnim = new AnimatorSet();
        nameAnim.playTogether(nameAlpha, nameTransY);
        nameAnim.setDuration(450);
        nameAnim.setStartDelay(650);

        // Tagline slide-up + fade
        ObjectAnimator tagAlpha  = ObjectAnimator.ofFloat(tvTagline, View.ALPHA, 0f, 1f);
        ObjectAnimator tagTransY = ObjectAnimator.ofFloat(tvTagline, View.TRANSLATION_Y, 16f, 0f);
        tagAlpha.setInterpolator(new DecelerateInterpolator());
        tagTransY.setInterpolator(new DecelerateInterpolator());
        AnimatorSet tagAnim = new AnimatorSet();
        tagAnim.playTogether(tagAlpha, tagTransY);
        tagAnim.setDuration(400);
        tagAnim.setStartDelay(850);

        // Loading dots + version fade in
        ObjectAnimator dotsAlpha    = ObjectAnimator.ofFloat(loadingContainer, View.ALPHA, 0f, 1f);
        ObjectAnimator versionAlpha = ObjectAnimator.ofFloat(tvVersion, View.ALPHA, 0f, 0.6f);
        dotsAlpha.setDuration(350);
        dotsAlpha.setStartDelay(1050);
        versionAlpha.setDuration(350);
        versionAlpha.setStartDelay(1050);

        AnimatorSet masterSet = new AnimatorSet();
        masterSet.playTogether(logoAnim, nameAnim, tagAnim, dotsAlpha, versionAlpha);
        masterSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startDotAnimation();
            }
        });
        masterSet.start();
    }

    private final Runnable dotRunnable = new Runnable() {
        @Override
        public void run() {
            Drawable active   = ContextCompat.getDrawable(SplashActivity.this, R.drawable.bg_dot_active);
            Drawable inactive = ContextCompat.getDrawable(SplashActivity.this, R.drawable.bg_dot_inactive);

            dot1.setBackground(dotStep == 0 ? active : inactive);
            dot2.setBackground(dotStep == 1 ? active : inactive);
            dot3.setBackground(dotStep == 2 ? active : inactive);

            dotStep = (dotStep + 1) % 3;
            dotHandler.postDelayed(this, 400);
        }
    };

    private void startDotAnimation() {
        dotHandler.post(dotRunnable);
        // Navigate after 1.6s of dots
        dotHandler.postDelayed(this::navigateToMain, 1600);
    }

    private void navigateToMain() {
        dotHandler.removeCallbacks(dotRunnable);
        View root = findViewById(R.id.splash_root);
        root.animate()
                .alpha(0f)
                .setDuration(350)
                .withEndAction(() -> {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dotHandler.removeCallbacksAndMessages(null);
    }
}
