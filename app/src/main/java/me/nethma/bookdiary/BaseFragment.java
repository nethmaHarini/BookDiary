package me.nethma.bookdiary;

import android.view.View;

import androidx.fragment.app.Fragment;

import me.nethma.bookdiary.utils.ThemePrefsManager;

/**
 * Base Fragment — auto-applies the saved accent colour on every resume.
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root == null) return;
        int accent = ThemePrefsManager.getAccentColor(requireContext());
        if (accent != BaseActivity.DEFAULT_PRIMARY) {
            BaseActivity.applyAccentToTree(root, accent);
        }
    }

    protected int accentColor() {
        return ThemePrefsManager.getAccentColor(requireContext());
    }
}
