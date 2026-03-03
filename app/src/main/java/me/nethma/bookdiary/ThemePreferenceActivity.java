package me.nethma.bookdiary;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import me.nethma.bookdiary.utils.ThemePrefsManager;

public class ThemePreferenceActivity extends AppCompatActivity {

    private ThemePrefsManager themePrefs;

    // Theme mode rows
    private LinearLayout rowLight, rowDark, rowSystem;
    private ImageView radioLight, radioDark, radioSystem;

    // Accent colour circles & checks
    private View circleOceanBlue, circleRoyalPurple, circleEmerald, circleSunset;
    private ImageView checkOceanBlue, checkRoyalPurple, checkEmerald, checkSunset;

    // Preview
    private TextView tvPreviewMode, tvPreviewAccent;
    private View vAccentPreview;
    private ImageView ivPreviewIcon;

    // Currently selected values (pending save)
    private int selectedMode;
    private int selectedAccent;

    // Accent colours
    private static final int[] ACCENT_COLORS = {
            ThemePrefsManager.ACCENT_OCEAN_BLUE,
            ThemePrefsManager.ACCENT_ROYAL_PURPLE,
            ThemePrefsManager.ACCENT_EMERALD,
            ThemePrefsManager.ACCENT_SUNSET
    };
    private static final String[] ACCENT_NAMES = {
            "Ocean Blue", "Royal Purple", "Emerald", "Sunset"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before inflation
        ThemePrefsManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_preference);

        themePrefs = new ThemePrefsManager(this);
        selectedMode   = themePrefs.getThemeMode();
        selectedAccent = themePrefs.getAccentColor();

        bindViews();
        paintAccentCircles();
        refreshModeSelection();
        refreshAccentSelection();
        refreshPreview();
        setListeners();
    }

    // ── View binding ──────────────────────────────────────────────────────────

    private void bindViews() {
        rowLight  = findViewById(R.id.row_light);
        rowDark   = findViewById(R.id.row_dark);
        rowSystem = findViewById(R.id.row_system);

        radioLight  = findViewById(R.id.radio_light);
        radioDark   = findViewById(R.id.radio_dark);
        radioSystem = findViewById(R.id.radio_system);

        circleOceanBlue   = findViewById(R.id.circle_ocean_blue);
        circleRoyalPurple = findViewById(R.id.circle_royal_purple);
        circleEmerald     = findViewById(R.id.circle_emerald);
        circleSunset      = findViewById(R.id.circle_sunset);

        checkOceanBlue   = findViewById(R.id.check_ocean_blue);
        checkRoyalPurple = findViewById(R.id.check_royal_purple);
        checkEmerald     = findViewById(R.id.check_emerald);
        checkSunset      = findViewById(R.id.check_sunset);

        tvPreviewMode   = findViewById(R.id.tv_preview_mode);
        tvPreviewAccent = findViewById(R.id.tv_preview_accent);
        vAccentPreview  = findViewById(R.id.v_accent_preview);
        ivPreviewIcon   = findViewById(R.id.iv_preview_icon);
    }

    // ── Colour painting ───────────────────────────────────────────────────────

    /** Set the actual hex colour on each circle View background. */
    private void paintAccentCircles() {
        setCircleColor(circleOceanBlue,   ThemePrefsManager.ACCENT_OCEAN_BLUE);
        setCircleColor(circleRoyalPurple, ThemePrefsManager.ACCENT_ROYAL_PURPLE);
        setCircleColor(circleEmerald,     ThemePrefsManager.ACCENT_EMERALD);
        setCircleColor(circleSunset,      ThemePrefsManager.ACCENT_SUNSET);
    }

    private void setCircleColor(View circle, int color) {
        GradientDrawable oval = new GradientDrawable();
        oval.setShape(GradientDrawable.OVAL);
        oval.setColor(color);
        circle.setBackground(oval);
    }

    // ── Selection refresh ─────────────────────────────────────────────────────

    private void refreshModeSelection() {
        // Row backgrounds
        setRowSelected(rowLight,  selectedMode == AppCompatDelegate.MODE_NIGHT_NO);
        setRowSelected(rowDark,   selectedMode == AppCompatDelegate.MODE_NIGHT_YES);
        setRowSelected(rowSystem, selectedMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Radio drawables (checked state via ImageView tint + drawable swap)
        setRadioChecked(radioLight,  selectedMode == AppCompatDelegate.MODE_NIGHT_NO);
        setRadioChecked(radioDark,   selectedMode == AppCompatDelegate.MODE_NIGHT_YES);
        setRadioChecked(radioSystem, selectedMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private void setRowSelected(LinearLayout row, boolean selected) {
        if (selected) {
            row.setBackgroundResource(R.drawable.bg_theme_option_selected);
        } else {
            row.setBackgroundResource(R.drawable.bg_notif_card);
        }
    }

    private void setRadioChecked(ImageView radio, boolean checked) {
        if (checked) {
            // Filled primary circle with white dot
            GradientDrawable outer = new GradientDrawable();
            outer.setShape(GradientDrawable.OVAL);
            outer.setColor(selectedAccent);
            radio.setBackground(outer);
            radio.setImageResource(R.drawable.ic_save);
            radio.setImageTintList(ColorStateList.valueOf(0xFFFFFFFF));
            radio.setPadding(4, 4, 4, 4);
        } else {
            radio.setBackground(null);
            radio.setImageDrawable(null);
            // Draw hollow ring
            GradientDrawable ring = new GradientDrawable();
            ring.setShape(GradientDrawable.OVAL);
            ring.setColor(android.graphics.Color.TRANSPARENT);
            ring.setStroke(dpToPx(2), 0xFF334155);
            radio.setBackground(ring);
            radio.setImageDrawable(null);
            radio.setPadding(0, 0, 0, 0);
        }
    }

    private void refreshAccentSelection() {
        // Hide all check icons
        checkOceanBlue.setVisibility(View.GONE);
        checkRoyalPurple.setVisibility(View.GONE);
        checkEmerald.setVisibility(View.GONE);
        checkSunset.setVisibility(View.GONE);

        // Show ring + check on selected circle
        showAccentSelected(circleOceanBlue,   checkOceanBlue,   ThemePrefsManager.ACCENT_OCEAN_BLUE);
        showAccentSelected(circleRoyalPurple, checkRoyalPurple, ThemePrefsManager.ACCENT_ROYAL_PURPLE);
        showAccentSelected(circleEmerald,     checkEmerald,     ThemePrefsManager.ACCENT_EMERALD);
        showAccentSelected(circleSunset,      checkSunset,      ThemePrefsManager.ACCENT_SUNSET);
    }

    private void showAccentSelected(View circle, ImageView check, int color) {
        boolean selected = (selectedAccent == color);
        GradientDrawable oval = new GradientDrawable();
        oval.setShape(GradientDrawable.OVAL);
        oval.setColor(color);
        if (selected) {
            oval.setStroke(dpToPx(3), 0xFFFFFFFF);
            check.setVisibility(View.VISIBLE);
        } else {
            oval.setStroke(0, android.graphics.Color.TRANSPARENT);
            check.setVisibility(View.GONE);
        }
        circle.setBackground(oval);
    }

    private void refreshPreview() {
        // Mode label
        String modeLabel = ThemePrefsManager.modeLabel(selectedMode);
        tvPreviewMode.setText(modeLabel + " Mode");

        // Accent label
        String accentName = accentName(selectedAccent);
        tvPreviewAccent.setText("Accent: " + accentName);

        // Accent swatch
        GradientDrawable swatch = new GradientDrawable();
        swatch.setShape(GradientDrawable.OVAL);
        swatch.setColor(selectedAccent);
        vAccentPreview.setBackground(swatch);

        // Tint preview icon with selected accent
        ivPreviewIcon.setImageTintList(ColorStateList.valueOf(selectedAccent));
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void setListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rowLight.setOnClickListener(v  -> selectMode(AppCompatDelegate.MODE_NIGHT_NO));
        rowDark.setOnClickListener(v   -> selectMode(AppCompatDelegate.MODE_NIGHT_YES));
        rowSystem.setOnClickListener(v -> selectMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));

        findViewById(R.id.accent_ocean_blue).setOnClickListener(v   -> selectAccent(ThemePrefsManager.ACCENT_OCEAN_BLUE));
        findViewById(R.id.accent_royal_purple).setOnClickListener(v -> selectAccent(ThemePrefsManager.ACCENT_ROYAL_PURPLE));
        findViewById(R.id.accent_emerald).setOnClickListener(v      -> selectAccent(ThemePrefsManager.ACCENT_EMERALD));
        findViewById(R.id.accent_sunset).setOnClickListener(v       -> selectAccent(ThemePrefsManager.ACCENT_SUNSET));

        findViewById(R.id.btn_save).setOnClickListener(v -> saveAndApply());
    }

    private void selectMode(int mode) {
        selectedMode = mode;
        refreshModeSelection();
        refreshPreview();
    }

    private void selectAccent(int color) {
        selectedAccent = color;
        refreshAccentSelection();
        refreshPreview();
        refreshModeSelection(); // re-draw radio buttons with new accent colour
    }

    private void saveAndApply() {
        themePrefs.setThemeMode(selectedMode);
        themePrefs.setAccentColor(selectedAccent);

        // Apply night mode globally — affects all future activity creations
        AppCompatDelegate.setDefaultNightMode(selectedMode);

        Toast.makeText(this, "Theme saved!", Toast.LENGTH_SHORT).show();

        // Recreate so this screen and the entire task back-stack apply the new theme
        finish();
        startActivity(new android.content.Intent(this, MainActivity.class)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String accentName(int color) {
        for (int i = 0; i < ACCENT_COLORS.length; i++) {
            if (ACCENT_COLORS[i] == color) return ACCENT_NAMES[i];
        }
        return "Custom";
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}




