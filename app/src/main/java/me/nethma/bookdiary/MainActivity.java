package me.nethma.bookdiary;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import me.nethma.bookdiary.utils.NotificationHelper;
import me.nethma.bookdiary.utils.NotificationScheduler;
import me.nethma.bookdiary.utils.SessionManager;
import me.nethma.bookdiary.utils.ThemePrefsManager;

public class MainActivity extends BaseActivity {

    // COLOR_ACTIVE is loaded from ThemePrefsManager at runtime
    private int COLOR_ACTIVE;
    private static final int COLOR_INACTIVE = 0xFF64748B;

    private FragmentContainerView fragmentContainer;
    private LinearLayout bottomNav;
    private SessionManager sessionManager;

    // Nav items
    private LinearLayout navHome, navSearch, navAdd, navDiary, navProfile;
    private ImageView icHome, icSearch, icDiary, icProfile;
    private TextView tvHome, tvSearch, tvAdd, tvDiary, tvProfile;

    // Add button specific views
    private FrameLayout addBtnBg;
    private ImageView icAdd;

    private int selectedNavId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Load accent colour from prefs
        COLOR_ACTIVE = ThemePrefsManager.getAccentColor(this);

        fragmentContainer = findViewById(R.id.fragment_container);
        bottomNav         = findViewById(R.id.bottom_nav);
        sessionManager    = new SessionManager(this);

        // Apply insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Bottom nav: bottom padding = system nav bar
            bottomNav.setPadding(
                    bottomNav.getPaddingLeft(),
                    bottomNav.getPaddingTop(),
                    bottomNav.getPaddingRight(),
                    systemBars.bottom
            );
            // Fragment container: top = status bar, bottom = nav bar + bottom nav height
            fragmentContainer.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom + getResources().getDimensionPixelSize(R.dimen.bottom_nav_height)
            );
            return insets;
        });

        bindNavViews();
        applyAccentToAddButton(); // set accent circle on add button immediately

        // Create notification channels and schedule background workers
        NotificationHelper.createNotificationChannels(this);
        NotificationScheduler.scheduleAll(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), false);
        }
        setNavSelected(R.id.nav_home);

        navHome.setOnClickListener(v    -> selectTab(R.id.nav_home,    new HomeFragment()));
        navSearch.setOnClickListener(v  -> selectTab(R.id.nav_search,  new SearchFragment()));
        navAdd.setOnClickListener(v     -> selectTab(R.id.nav_add,     new AddFragment()));
        navDiary.setOnClickListener(v   -> selectTab(R.id.nav_diary,   new DiaryFragment()));
        navProfile.setOnClickListener(v -> selectTab(R.id.nav_profile, new ProfileFragment()));
    }

    @Override
    protected void onResume() {
        super.onResume(); // BaseActivity scans the full view tree
        // Refresh nav bar active colour (set programmatically, not via XML)
        COLOR_ACTIVE = ThemePrefsManager.getAccentColor(this);
        setNavSelected(selectedNavId);
        // Apply accent to the floating add button
        applyAccentToAddButton();
    }

    private void applyAccentToAddButton() {
        int accent = ThemePrefsManager.getAccentColor(this);

        // Recolor the FrameLayout background border stroke with accent/20
        if (addBtnBg != null && addBtnBg.getBackground() instanceof GradientDrawable) {
            GradientDrawable bg = (GradientDrawable) addBtnBg.getBackground().mutate();
            bg.setStroke(2, (accent & 0x00FFFFFF) | 0x33000000);
        }

        if (icAdd == null) return;

        // Draw an accent-colored circle as the ImageView background
        // and show only the white plus on top — this way accent changes are visible
        // without the plus disappearing (which happens when tinting the full circle+plus vector)
        GradientDrawable accentCircle = new GradientDrawable();
        accentCircle.setShape(GradientDrawable.OVAL);
        accentCircle.setColor(accent);
        icAdd.setBackground(accentCircle);
        icAdd.setPadding(10, 10, 10, 10);

        // Use a white-plus-only icon (no circle path) — tint cleared
        icAdd.setImageTintList(null);
        icAdd.setImageResource(R.drawable.ic_add_plus_white);
    }

    private void bindNavViews() {
        navHome    = findViewById(R.id.nav_home);
        navSearch  = findViewById(R.id.nav_search);
        navAdd     = findViewById(R.id.nav_add);
        navDiary   = findViewById(R.id.nav_diary);
        navProfile = findViewById(R.id.nav_profile);

        icHome    = findViewById(R.id.ic_home);
        icSearch  = findViewById(R.id.ic_search);
        icDiary   = findViewById(R.id.ic_diary);
        icProfile = findViewById(R.id.ic_profile);

        addBtnBg  = findViewById(R.id.add_btn_bg);
        icAdd     = findViewById(R.id.ic_add);

        tvHome    = findViewById(R.id.tv_home);
        tvSearch  = findViewById(R.id.tv_search);
        tvAdd     = findViewById(R.id.tv_add);
        tvDiary   = findViewById(R.id.tv_diary);
        tvProfile = findViewById(R.id.tv_profile);
    }

    private void selectTab(int navId, Fragment fragment) {
        if (selectedNavId == navId) return;
        selectedNavId = navId;
        setNavSelected(navId);
        loadFragment(fragment, true);
    }

    /** Update icon tints and label colors to reflect the selected tab */
    private void setNavSelected(int navId) {
        // Reset all to inactive
        icHome.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        icSearch.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        icDiary.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        icProfile.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));

        tvHome.setTextColor(COLOR_INACTIVE);
        tvSearch.setTextColor(COLOR_INACTIVE);
        tvAdd.setTextColor(COLOR_INACTIVE);
        tvDiary.setTextColor(COLOR_INACTIVE);
        tvProfile.setTextColor(COLOR_INACTIVE);

        // Activate selected
        if (navId == R.id.nav_home) {
            icHome.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
            tvHome.setTextColor(COLOR_ACTIVE);
        } else if (navId == R.id.nav_search) {
            icSearch.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
            tvSearch.setTextColor(COLOR_ACTIVE);
        } else if (navId == R.id.nav_add) {
            tvAdd.setTextColor(COLOR_ACTIVE);
            // Add icon is always primary color (filled svg), no tint needed
        } else if (navId == R.id.nav_diary) {
            icDiary.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
            tvDiary.setTextColor(COLOR_ACTIVE);
        } else if (navId == R.id.nav_profile) {
            icProfile.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
            tvProfile.setTextColor(COLOR_ACTIVE);
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        tx.replace(R.id.fragment_container, fragment);
        if (addToBackStack) tx.addToBackStack(null);
        tx.commit();
    }

    /** Call from any Fragment to log out */
    public void logout() {
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            selectedNavId = R.id.nav_home;
            setNavSelected(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}