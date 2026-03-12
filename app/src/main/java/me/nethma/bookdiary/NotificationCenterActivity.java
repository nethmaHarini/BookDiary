package me.nethma.bookdiary;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.nethma.bookdiary.utils.NotificationItem;
import me.nethma.bookdiary.utils.NotificationStore;

public class NotificationCenterActivity extends BaseActivity {

    private RecyclerView     rvNotifications;
    private LinearLayout     emptyState;
    private TextView         tvMarkAllRead;
    private TextView         tvClearAll;

    private NotificationAdapter adapter;
    private NotificationStore   store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);

        store = new NotificationStore(this);

        // Seed a welcome notification on first open if store is empty
        if (store.loadAll().isEmpty()) {
            store.addNotification(
                    NotificationItem.Type.UPDATE,
                    "👋 Welcome to BookDiary!",
                    "Track your reading journey, discover new books, and never miss a reading session."
            );
            store.addNotification(
                    NotificationItem.Type.RECOMMENDATION,
                    "📚 Explore Recommendations",
                    "Scroll down on the Home screen to discover books tailored to your favourite genres."
            );
        }

        // Views
        rvNotifications = findViewById(R.id.rv_notifications);
        emptyState      = findViewById(R.id.empty_state);
        tvMarkAllRead   = findViewById(R.id.tv_mark_all_read);
        tvClearAll      = findViewById(R.id.tv_clear_all);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Mark all read
        tvMarkAllRead.setOnClickListener(v -> {
            store.markAllRead();
            adapter.refresh(store.loadAll());
            updateEmptyState();
        });

        // Clear all
        tvClearAll.setOnClickListener(v -> {
            store.clearAll();
            adapter.refresh(store.loadAll());
            updateEmptyState();
        });

        // RecyclerView
        adapter = new NotificationAdapter(store.loadAll());
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        // Swipe-to-dismiss
        new ItemTouchHelper(new SwipeToDismissCallback()).attachToRecyclerView(rvNotifications);

        // Mark all as read when screen opens
        store.markAllRead();
        adapter.refresh(store.loadAll());

        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvMarkAllRead.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvClearAll.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Adapter
    // ═══════════════════════════════════════════════════════════════════════

    class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

        private List<NotificationItem> items;

        NotificationAdapter(List<NotificationItem> items) {
            this.items = items;
        }

        void refresh(List<NotificationItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        void removeAt(int position) {
            if (position < 0 || position >= items.size()) return;
            items.remove(position);
            notifyItemRemoved(position);
            updateEmptyState();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView  tvTitle, tvMessage, tvTime;
            View      unreadDot;

            VH(View itemView) {
                super(itemView);
                ivIcon    = itemView.findViewById(R.id.iv_notif_icon);
                tvTitle   = itemView.findViewById(R.id.tv_notif_title);
                tvMessage = itemView.findViewById(R.id.tv_notif_message);
                tvTime    = itemView.findViewById(R.id.tv_notif_time);
                unreadDot = itemView.findViewById(R.id.unread_dot);
            }

            void bind(NotificationItem item) {
                tvTitle.setText(item.getTitle());
                tvMessage.setText(item.getMessage());
                tvTime.setText(formatRelativeTime(item.getTimestamp()));
                unreadDot.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

                // Icon by type
                switch (item.getType()) {
                    case RECOMMENDATION:
                        ivIcon.setImageResource(R.drawable.ic_book_logo);
                        break;
                    case REMINDER:
                        ivIcon.setImageResource(R.drawable.ic_notification);
                        break;
                    case QUOTE:
                        ivIcon.setImageResource(R.drawable.ic_bookmark);
                        break;
                    case UPDATE:
                    default:
                        ivIcon.setImageResource(R.drawable.ic_notification);
                        break;
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Swipe to dismiss
    // ═══════════════════════════════════════════════════════════════════════

    private class SwipeToDismissCallback extends ItemTouchHelper.SimpleCallback {

        private final Paint paint = new Paint();

        SwipeToDismissCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            paint.setColor(0xFFEF4444); // red
        }

        @Override
        public boolean onMove(@NonNull RecyclerView rv,
                              @NonNull RecyclerView.ViewHolder vh,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            if (pos < 0 || pos >= adapter.items.size()) return;
            String removedId = adapter.items.get(pos).getId();

            // Rebuild store without the removed item
            List<NotificationItem> remaining = store.loadAll();
            remaining.removeIf(item -> item.getId().equals(removedId));
            store.clearAll();
            // Re-insert in reverse order (addNotification inserts at 0)
            for (int i = remaining.size() - 1; i >= 0; i--) {
                NotificationItem it = remaining.get(i);
                store.addNotification(it.getType(), it.getTitle(), it.getMessage());
            }
            adapter.removeAt(pos);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                @NonNull RecyclerView.ViewHolder vh, float dX, float dY,
                                int actionState, boolean isActive) {
            View itemView = vh.itemView;
            if (dX < 0) {
                // Swipe left — draw red bg on right side
                RectF rect = new RectF(itemView.getRight() + dX,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
                c.drawRect(rect, paint);
            } else if (dX > 0) {
                RectF rect = new RectF(itemView.getLeft(),
                        itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom());
                c.drawRect(rect, paint);
            }
            super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════

    private static String formatRelativeTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < TimeUnit.MINUTES.toMillis(1))   return "Just now";
        if (diff < TimeUnit.HOURS.toMillis(1))
            return TimeUnit.MILLISECONDS.toMinutes(diff) + "m ago";
        if (diff < TimeUnit.DAYS.toMillis(1))
            return TimeUnit.MILLISECONDS.toHours(diff) + "h ago";
        if (diff < TimeUnit.DAYS.toMillis(7))
            return TimeUnit.MILLISECONDS.toDays(diff) + "d ago";
        return new SimpleDateFormat("MMM d", Locale.getDefault()).format(new Date(timestamp));
    }
}


