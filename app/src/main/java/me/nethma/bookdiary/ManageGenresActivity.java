package me.nethma.bookdiary;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.nethma.bookdiary.utils.SessionManager;
import me.nethma.bookdiary.utils.ThemePrefsManager;

/**
 * Allows the user to add/remove their genre preferences at any time.
 * Pre-fills with currently saved topics. Saves on "Save Changes" click.
 * Returns RESULT_OK so HomeFragment can reload discover books.
 */
public class ManageGenresActivity extends BaseActivity {

    private static final String[] ALL_TOPICS = {
            "Fiction", "Mystery", "Romance", "Science", "History",
            "Fantasy", "Biography", "Thriller", "Self-Help", "Horror",
            "Classic", "Poetry", "Science Fiction", "Adventure", "Children"
    };
    private static final int MIN_SELECTIONS = 3;
    private static final int CHIPS_PER_ROW  = 3;

    private LinearLayout topicChipsContainer;
    private TextView     tvSelectionCount;
    private Button       btnSave;

    private final Set<String> selectedTopics = new HashSet<>();
    private SessionManager    sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_genres);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.manage_genres_root), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        sessionManager      = new SessionManager(this);
        topicChipsContainer = findViewById(R.id.topic_chips_container);
        tvSelectionCount    = findViewById(R.id.tv_selection_count);
        btnSave             = findViewById(R.id.btn_save_genres);

        // Pre-fill with current saved topics
        List<String> saved = sessionManager.getSelectedTopics();
        selectedTopics.addAll(saved);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Save button
        btnSave.setOnClickListener(v -> saveAndFinish());

        buildTopicChips();
        updateCounter();
    }

    private void buildTopicChips() {
        topicChipsContainer.removeAllViews();
        int accent = ThemePrefsManager.getAccentColor(this);

        LinearLayout currentRow = null;
        for (int i = 0; i < ALL_TOPICS.length; i++) {
            if (i % CHIPS_PER_ROW == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.bottomMargin = (int)(10 * getResources().getDisplayMetrics().density);
                topicChipsContainer.addView(currentRow, rowParams);
            }
            String topic = ALL_TOPICS[i];
            TextView chip = (TextView) LayoutInflater.from(this)
                    .inflate(R.layout.item_topic_chip, currentRow, false);
            chip.setText(topic);
            applyChipStyle(chip, selectedTopics.contains(topic), accent);
            chip.setOnClickListener(v -> toggleTopic(chip, topic, accent));
            currentRow.addView(chip);
        }
    }

    private void toggleTopic(TextView chip, String topic, int accent) {
        if (selectedTopics.contains(topic)) {
            selectedTopics.remove(topic);
            applyChipStyle(chip, false, accent);
        } else {
            selectedTopics.add(topic);
            applyChipStyle(chip, true, accent);
        }
        updateCounter();
    }

    private void updateCounter() {
        int count = selectedTopics.size();
        if (count < MIN_SELECTIONS) {
            tvSelectionCount.setText(count + " selected (need at least " + MIN_SELECTIONS + ")");
            tvSelectionCount.setTextColor(0xFFEF4444);
            btnSave.setEnabled(false);
        } else {
            tvSelectionCount.setText(count + " genres selected ✓");
            tvSelectionCount.setTextColor(0xFF22C55E);
            btnSave.setEnabled(true);
        }
    }

    private void saveAndFinish() {
        List<String> topics = new ArrayList<>(selectedTopics);
        sessionManager.saveTopics(topics);
        Toast.makeText(this, "Genres updated!", Toast.LENGTH_SHORT).show();
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void applyChipStyle(TextView chip, boolean selected, int accent) {
        float density = getResources().getDisplayMetrics().density;
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(50 * density);
        if (selected) {
            bg.setColor(accent);
            bg.setStroke(0, 0);
            chip.setBackground(bg);
            chip.setTextColor(0xFFFFFFFF);
        } else {
            boolean isDark = (getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            bg.setColor(isDark ? 0xFF1E293B : 0xFFE2E8F0);
            bg.setStroke((int)(1.5f * density), isDark ? 0xFF334155 : 0xFFCBD5E1);
            chip.setBackground(bg);
            chip.setTextColor(isDark ? 0xFF94A3B8 : 0xFF475569);
        }
    }
}

