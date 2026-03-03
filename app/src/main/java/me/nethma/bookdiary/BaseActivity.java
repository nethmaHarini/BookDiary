package me.nethma.bookdiary;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import me.nethma.bookdiary.utils.ThemePrefsManager;

/**
 * Base Activity — auto-applies the saved accent colour on every resume
 * by scanning the view tree and replacing any view whose tint/textColor
 * matches the default primary colour (0xFF1152D4).
 */
public abstract class BaseActivity extends AppCompatActivity {

    // Default Ocean Blue — the compile-time @color/primary value
    static final int DEFAULT_PRIMARY = 0xFF1152D4;

    @Override
    protected void onResume() {
        super.onResume();
        int accent = ThemePrefsManager.getAccentColor(this);
        if (accent != DEFAULT_PRIMARY) {
            applyAccentToTree(getWindow().getDecorView(), accent);
        }
    }

    /** Walk the entire view tree and swap DEFAULT_PRIMARY → accent. */
    static void applyAccentToTree(View view, int accent) {
        if (view == null) return;

        // TextView / Button — swap textColor
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            ColorStateList csl = tv.getTextColors();
            if (csl != null && csl.getDefaultColor() == DEFAULT_PRIMARY) {
                tv.setTextColor(accent);
            }
        }

        // ImageView — swap imageTint
        if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            ColorStateList tint = iv.getImageTintList();
            if (tint != null && tint.getDefaultColor() == DEFAULT_PRIMARY) {
                iv.setImageTintList(ColorStateList.valueOf(accent));
            }
        }

        // SwitchCompat — swap track tint when checked
        if (view instanceof SwitchCompat) {
            SwitchCompat sw = (SwitchCompat) view;
            ColorStateList track = sw.getTrackTintList();
            if (track != null) {
                int checked = track.getColorForState(
                        new int[]{android.R.attr.state_checked}, DEFAULT_PRIMARY);
                if (checked == DEFAULT_PRIMARY) {
                    int[][] states = {new int[]{android.R.attr.state_checked}, new int[]{}};
                    sw.setTrackTintList(new ColorStateList(states,
                            new int[]{accent, 0xFF334155}));
                    sw.setThumbTintList(new ColorStateList(states,
                            new int[]{0xFFFFFFFF, 0xFF94A3B8}));
                }
            }
        }

        // View background tint (e.g. icon boxes with primary/10 bg)
        ColorStateList bgTint = view.getBackgroundTintList();
        if (bgTint != null) {
            int bgColor = bgTint.getDefaultColor();
            // Match primary/10 (#1A1152D4) or primary/20 (#331152D4) or solid primary
            if ((bgColor & 0x00FFFFFF) == (DEFAULT_PRIMARY & 0x00FFFFFF)) {
                int alpha = (bgColor >> 24) & 0xFF;
                int newColor = (accent & 0x00FFFFFF) | (alpha << 24);
                view.setBackgroundTintList(ColorStateList.valueOf(newColor));
            }
        }

        // GradientDrawable background (shape drawables)
        if (view.getBackground() instanceof GradientDrawable) {
            // We can't read back the fill color reliably, skip for shapes
        }

        // Recurse into children
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            for (int i = 0; i < g.getChildCount(); i++) {
                applyAccentToTree(g.getChildAt(i), accent);
            }
        }
    }

    protected int accentColor() {
        return ThemePrefsManager.getAccentColor(this);
    }
}
