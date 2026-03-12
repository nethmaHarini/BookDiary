package me.nethma.bookdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import me.nethma.bookdiary.utils.SessionManager;

@SuppressWarnings("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private View logoContainer;
    private TextView tvAppName;
    private TextView tvTagline;
    private View loadingContainer;
    private View tvFooter;
    private View progressFill;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
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
        tvFooter         = findViewById(R.id.tv_footer);
        progressFill     = findViewById(R.id.progress_fill);
    }

    private void startSplashAnimation() {
        // Logo pop-in (scale + fade) — OvershootInterpolator for a bouncy feel
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoContainer, View.SCALE_X, 0.7f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoContainer, View.SCALE_Y, 0.7f, 1f);
        ObjectAnimator logoAlpha  = ObjectAnimator.ofFloat(logoContainer, View.ALPHA,  0f, 1f);
        logoScaleX.setInterpolator(new OvershootInterpolator(1.4f));
        logoScaleY.setInterpolator(new OvershootInterpolator(1.4f));
        logoAlpha.setInterpolator(new DecelerateInterpolator());
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(logoScaleX, logoScaleY, logoAlpha);
        logoAnim.setDuration(550);
        logoAnim.setStartDelay(200);

        // Title slide-up + fade
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

        // Progress bar container fade in
        ObjectAnimator progressAlpha = ObjectAnimator.ofFloat(loadingContainer, View.ALPHA, 0f, 1f);
        progressAlpha.setDuration(350);
        progressAlpha.setStartDelay(1050);

        // Footer fade in
        ObjectAnimator footerAlpha = ObjectAnimator.ofFloat(tvFooter, View.ALPHA, 0f, 1f);
        footerAlpha.setDuration(350);
        footerAlpha.setStartDelay(1050);

        AnimatorSet masterSet = new AnimatorSet();
        masterSet.playTogether(logoAnim, nameAnim, tagAnim, progressAlpha, footerAlpha);
        masterSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startProgressAnimation();
            }
        });
        masterSet.start();
    }

    private void startProgressAnimation() {
        loadingContainer.post(() -> {
            ViewGroup track = (ViewGroup) progressFill.getParent();
            int trackWidth = track.getWidth();

            ValueAnimator animator = ValueAnimator.ofInt(0, trackWidth);
            animator.setDuration(1600);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(animation -> {
                int w = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams lp = progressFill.getLayoutParams();
                lp.width = w;
                progressFill.setLayoutParams(lp);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    handler.postDelayed(SplashActivity.this::navigateToMain, 300);
                }
            });
            animator.start();
        });
    }

    private void navigateToMain() {
        View root = findViewById(R.id.splash_root);
        root.animate()
                .alpha(0f)
                .setDuration(350)
                .withEndAction(() -> {
                    SessionManager session = new SessionManager(this);
                    Intent intent;
                    if (session.isLoggedIn()) {
                        intent = session.hasSelectedTopics()
                                ? new Intent(this, MainActivity.class)
                                : new Intent(this, TopicSelectionActivity.class);
                    } else {
                        intent = new Intent(this, LoginActivity.class);
                    }
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
