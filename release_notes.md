## 📚 BookDiary v1.0 — Initial Release

> *Read. Review. Remember.* — Your personal book tracking & recommendation diary for Android.

---

### ✨ What's Included

#### 🔐 Authentication
- Email & password registration with full field validation and duplicate-email detection
- BCrypt password hashing — no plain text ever stored
- Login with BCrypt hash verification against a local Room database
- **Google Sign-In** via Android Credential Manager + Firebase Auth token verification
- Auto-register on first Google sign-in; links existing account if email matches
- Forgot Password — find account by registered email
- Reset Password — set a new BCrypt-hashed password directly in Room DB (no email link required)
- Persistent session via `SharedPreferences` — stays logged in across app restarts

#### 🏠 Home Dashboard
- Horizontal **Favourites** strip with quick access to marked books
- Full **All Books** vertical list — newest first
- Live search bar (searches title and author simultaneously)
- Category filter chips built from the user's selected reading topics
- **Discover Books** section — fetches real books from the Open Library API based on your genres
- "See All" → dedicated Favourites screen
- 🔔 **Notification Bell** — opens the new in-app Notification Center with unread badge dot

#### 🔍 Search & Filter
- Real-time text search across title and author
- Category filter chips (Fiction, Science, Mystery, History, and more)
- Minimum star rating buttons (★★★+, ★★★★+, ★★★★★)
- Favourites-only toggle
- Tap any result to open full Book Details

#### ➕ Add Book
- Cover photo picker (camera or gallery)
- Title, Author, Category (spinner), Reading Status (spinner)
- Interactive 1–5 star rating picker
- Personal review / diary notes
- Saves to local Room database instantly

#### ✏️ Edit Book
- Pre-filled form from the database
- Update any field, re-pick cover photo
- Delete book with confirmation dialog
- Changes propagate back to all screens

#### 📋 Book Details
- Hero book cover (200×300 dp, rounded, gradient overlay)
- Title, Author, accent-coloured category tag
- Stats bar: ⭐ Rating · Category · Reading Status
- "Read Now" primary button + Edit shortcut
- My Review card: 5-star display + review text / empty-state prompt
- Favourite toggle (add/remove with animated label)
- **Ratings & Reviews** row — opens dedicated review screen
- Share button → system share sheet
- More (⋮) popup → Mark Favourite / Remove Favourite / Delete Book

#### ⭐ Ratings & Reviews
- Overall average rating — large number + 5-star summary + total review count
- Star distribution bars — animated progress for each star level (1–5) with percentages
- Community review cards: coloured initials avatar, reviewer name, star rating, date, review text
- 👍 / 👎 helpful voting (persisted to database)
- Options (⋮) on your own review → Edit / Delete
- Sort: Recent ↔ Most Helpful
- Write / Edit review via bottom sheet — interactive star picker + multi-line input
- Save syncs the rating and notes back to the Book record

#### 📓 Reading Diary
- Stats row: Total Books | Reviewed | Favourites (live from Room DB)
- Live search (title / author)
- Status filter chips: All · Want to Read · Reading · Finished
- Book cards with cover thumbnail, stars, coloured status badge, date added
- Action buttons per card: 👁 View · ❤ Favourite · ✏️ Edit

#### ❤️ Favourites Screen
- Dedicated full-screen Favourites activity
- Large book cards (96×144 dp covers)
- Live search + Status filter chips
- Count badge in the header
- Tap card → opens Book Details
- Accessible from Home "See All" and Profile "My Favourites"

#### 🔔 Notification Center *(NEW)*
- In-app notification inbox accessible from the Home screen bell icon
- Red badge dot shows unread notification count
- Notification types: 📚 Recommendations · 📖 Reading Reminders · ✨ Daily Quotes · 🚀 App Updates
- Swipe left/right to dismiss individual notifications
- "Mark all read" and "Clear all" actions
- Notifications are stored locally and survive app restarts
- Background workers (WorkManager) automatically log notifications to the inbox

#### 👤 Profile & Settings
- Avatar (Google photo or camera / gallery pick → saved to private storage)
- Display name, email, reading stats (Total Books / Favourites / Reviews) — live from DB
- **Edit Profile** → update display name or change password (current password verified)
- **Notification Settings** → per-type toggles (Reading Reminders, Daily Quote, Recommendations, App Updates) + time picker + WorkManager scheduling + Android 13+ runtime permission
- **Theme Preference** → Light / Dark / Follow Device + 4 accent colours
- **My Favourites** shortcut → FavouritesActivity
- Log Out with confirmation dialog

#### 🎨 Theme & Accent Colour System
- Light mode, Dark mode, or Follow Device (system default)
- 4 accent colour options: 🌊 Ocean Blue · 👑 Royal Purple · 🌿 Emerald · 🌅 Sunset
- Entire app re-themes dynamically without restart — every view, icon, button, and chip
- Custom `BaseActivity` / `BaseFragment` re-apply accent on every `onResume`

#### 📱 Navigation
- Fully custom bottom navigation bar (supports floating Add button)
- **⊕ Add button** — enlarged floating pill icon that rises above the nav bar
- Smooth `fade_in / fade_out` fragment transitions
- 5 tabs: 🏠 Home · 🔍 Search · **⊕ Add** · 📓 Diary · 👤 Profile

---

### 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 11 |
| Platform | Android — Min SDK 24 (Android 7.0+), Target SDK 36 |
| UI | XML Layouts + Material Design 3 |
| Local Database | Room Persistence Library v2.6.1 |
| Background Tasks | WorkManager v2.9.1 |
| Auth | Firebase Auth + Android Credential Manager |
| Google Sign-In | Credential Manager API v1.3.0 |
| Network | Open Library API (book discovery) |
| Security | BCrypt password hashing |
| Notifications | NotificationManager + WorkManager + in-app NotificationStore |

---

### 📲 Installation

1. Download **BookDiary-v1.0.apk** below
2. On your Android device: **Settings → Security → Allow unknown sources** (or "Install unknown apps")
3. Open the downloaded APK and tap **Install**
4. Launch **BookDiary**, create an account or sign in with Google, and start your reading diary!

> ⚠️ **Minimum Android version:** 7.0 (API 24 — Nougat)

---

### 📝 Known Limitations

- Password reset is local only (no email link — resets directly in the on-device database)
- Book discovery requires an internet connection (Open Library API)
- Data is stored on-device only — no cloud sync in v1.0
- Google Sign-In requires the bundled `google-services.json` to be configured for your Firebase project

---

### 🔮 Coming in v2.0

- ☁️ Cloud sync via Firestore
- 📊 Reading stats dashboard with charts
- 🔁 Per-book reading progress tracking
- 📤 Export diary as PDF / CSV
- 🔖 Custom reading lists
