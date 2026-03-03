package me.nethma.bookdiary.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.ColorUtils;

/**
 * Central helper that applies the user's chosen accent colour to
 * every relevant view at runtime.
 *
 * Usage — call from Activity.onResume() / Fragment.onResume():
 *   AccentColorHelper.apply(getContext(), rootView);
 */
public class AccentColorHelper {

    /**
     * Walk the view tree rooted at [root] and recolour every tagged view.
     * Views that should receive accent colour must carry the tag "accent".
     * Views that should receive accent/10 bg must carry "accent_bg".
     * Views that should receive accent border must carry "accent_border".
     *
     * This avoids touching unrelated views.
     */
    public static void apply(Context context, View root) {
        int accent = ThemePrefsManager.getAccentColor(context);
        applyToTree(root, accent);
    }

    public static void applyColor(View root, int accent) {
        applyToTree(root, accent);
    }

    // ── Internal tree walker ──────────────────────────────────────────────────

    private static void applyToTree(View view, int accent) {
        if (view == null) return;

        Object tag = view.getTag();
        if (tag instanceof String) {
            String tagStr = (String) tag;
            switch (tagStr) {
                case "accent":
                    applyAccentToView(view, accent);
                    break;
                case "accent_bg":
                    applyAccentBg(view, accent, 0x1A); // 10% opacity
                    break;
                case "accent_bg_full":
                    applyAccentBgFull(view, accent);
                    break;
                case "accent_border":
                    applyAccentBorder(view, accent);
                    break;
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyToTree(group.getChildAt(i), accent);
            }
        }
    }

    // ── Per-view accent applicators ───────────────────────────────────────────

    private static void applyAccentToView(View view, int accent) {
        ColorStateList csl = ColorStateList.valueOf(accent);

        if (view instanceof TextView) {
            ((TextView) view).setTextColor(accent);
        } else if (view instanceof ImageView) {
            ((ImageView) view).setImageTintList(csl);
        } else if (view instanceof ProgressBar) {
            ((ProgressBar) view).setProgressTintList(csl);
        } else if (view instanceof SwitchCompat) {
            SwitchCompat sw = (SwitchCompat) view;
            // Track tint: accent when checked, grey when off
            int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] {}
            };
            int[] trackColors = new int[] { accent, 0xFF334155 };
            int[] thumbColors = new int[] { 0xFFFFFFFF, 0xFF94A3B8 };
            sw.setTrackTintList(new ColorStateList(states, trackColors));
            sw.setThumbTintList(new ColorStateList(states, thumbColors));
        } else {
            // Generic view — tint background if it's a GradientDrawable
            if (view.getBackground() instanceof GradientDrawable) {
                ((GradientDrawable) view.getBackground()).setColor(accent);
            } else {
                view.setBackgroundTintList(csl);
            }
        }
    }

    private static void applyAccentBg(View view, int accent, int alpha) {
        int color = ColorUtils.setAlphaComponent(accent, alpha);
        if (view.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) view.getBackground()).setColor(color);
        } else {
            view.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    private static void applyAccentBgFull(View view, int accent) {
        if (view.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) view.getBackground()).setColor(accent);
        } else {
            view.setBackgroundTintList(ColorStateList.valueOf(accent));
        }
    }

    private static void applyAccentBorder(View view, int accent) {
        if (view.getBackground() instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) view.getBackground().mutate();
            gd.setStroke(2, accent);
        }
    }

    // ── Convenience: derive a darkened shade for pressed states ──────────────

    public static int darken(int color, float factor) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        hsl[2] = Math.max(0f, hsl[2] - factor);
        return ColorUtils.HSLToColor(hsl);
    }

    /** Returns accent at 10% opacity — useful for badge/icon-box backgrounds. */
    public static int accentAt10(int accent) {
        return ColorUtils.setAlphaComponent(accent, 0x1A);
    }

    /** Returns accent at 20% opacity — useful for borders. */
    public static int accentAt20(int accent) {
        return ColorUtils.setAlphaComponent(accent, 0x33);
    }
}


