<div align="center">
<img src="app/src/main/res/drawable/logoforreadme.png" width="110" alt="BookDiary Logo"/>

# BookDiary

### *Read. Review. Remember.*

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Room](https://img.shields.io/badge/Database-Room-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Material3](https://img.shields.io/badge/UI-Material%20Design%203-757de8?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Firebase](https://img.shields.io/badge/Auth-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)
[![Google Sign-In](https://img.shields.io/badge/Sign--In-Google-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://developers.google.com/identity)
[![License](https://img.shields.io/badge/License-Academic-teal?style=for-the-badge)](LICENSE)

> **Module:** ICT3214 — Mobile Application Development
>
> **Project Idea:** #5 — Book Recommendation Diary

</div>

---

## 📖 About BookDiary

**BookDiary** is a personal book tracking and recommendation diary built for Android.

It gives readers a beautiful, distraction-free space to **log the books they read**, **write reviews and ratings**, **track their reading diary**, and **mark favourites** — all stored privately per user on the device using a local Room database.

Unlike heavyweight reading apps, BookDiary is intentionally personal.
It's about **your relationship with books** — the ones you loved, the ones you abandoned, and the ones you want to remember forever.

---

## ✨ Features

| Feature | Status | Description |
|---|---|---|
| 💫 **Splash Screen** | ✅ Done | Animated logo pop-in with progress bar — routes by session state |
| 🔐 **Register** | ✅ Done | Full validation, duplicate email check, BCrypt-hashed password, saves to Room DB |
| 🔑 **Login** | ✅ Done | Email/password auth with BCrypt hash verification against local Room database |
| 🔵 **Google Sign-In** | ✅ Done | One-tap Google sign-in via Credential Manager + Firebase Auth token verification |
| 🔓 **Forgot Password** | ✅ Done | Find account by registered email address |
| 🔁 **Reset Password** | ✅ Done | Set new BCrypt-hashed password directly in Room DB — no email link required |
| 📱 **Session Management** | ✅ Done | Persistent login via `SharedPreferences` — stay logged in across app restarts |
| 👤 **Profile Screen** | ✅ Done | Avatar, username, email, accent-coloured stats, edit & settings menu rows |
| ✏️ **Edit Profile** | ✅ Done | Update display name, change password with current-password verification |
| 📸 **Change Profile Picture** | ✅ Done | Camera capture or gallery pick, saved to private app storage |
| 🔔 **Notification Settings** | ✅ Done | Per-type toggles (Reading Reminders, Daily Quote, Recommendations), time picker, WorkManager scheduling, runtime permission (Android 13+) |
| 🌙 **Theme Preference** | ✅ Done | Light / Dark / System mode + 4 accent colours; entire app re-themes on save |
| 🎨 **Accent Color System** | ✅ Done | Ocean Blue, Royal Purple, Emerald, Sunset — dynamic accent applied app-wide |
| 🔒 **Password Hashing** | ✅ Done | BCrypt hashing via `PasswordUtils` for all password store and verify operations |
| 🏠 **Home Dashboard** | ✅ Done | Favourites horizontal strip, full books list, live search, category filter chips, empty states, sample data seed on first launch |
| 🔍 **Search & Filter** | ✅ Done | Live text search (title/author), category chips, min-rating buttons, favourites-only toggle |
| ➕ **Add Book** | ✅ Done | Title, author, category & status spinners, 1-5 star rating, cover photo picker, review notes — saves to Room DB |
| ✏️ **Edit Book** | ✅ Done | Pre-filled form from DB, update all fields, cover re-pick, delete with confirmation dialog |
| 📋 **Book Details** | ✅ Done | Hero cover, stats bar (rating/category/status), Read Now button, review card with stars, favourite toggle, Ratings & Reviews entry row, Share intent, popup menu (favourite/delete), edit launcher with back-refresh |
| 📓 **Reading Diary** | ✅ Done | Stats row (total/reviewed/favourites), live search, status filter chips, book cards with cover, star rating, coloured status badge, favourite/edit/view actions |
| ❤️ **Favourites Screen** | ✅ Done | Dedicated full-screen Favourites activity — large book cards, live search, status filter chips (All / Reading / Want to Read / Finished), count badge, opens Book Details; linked from Home "See All" and Profile "My Favourites" row |
| 👤 **Profile Stats (Live)** | ✅ Done | Total Books, Favourites, and Reviews counts loaded live from Room DB on every tab visit |
| ⭐ **Ratings & Reviews** | ✅ Done | Dedicated ratings & reviews screen per book — overall avg rating, 5-star distribution bars, pageable review cards with coloured initials avatars, thumbs up/down voting, sort by Recent or Most Helpful, write/edit/delete own review via bottom sheet with interactive star picker, syncs rating & notes back to the book |

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| **Language** | Java | 11 |
| **Platform** | Android | Min SDK 24 (Android 7.0+), Target SDK 36 |
| **UI Framework** | XML Layouts + Material Design 3 | — |
| **Material Components** | Material Design 3 | `1.13.0` |
| **AppCompat** | `androidx.appcompat` | `1.7.1` |
| **Activity** | `androidx.activity` | `1.12.4` |
| **ConstraintLayout** | `androidx.constraintlayout` | `2.2.1` |
| **Local Database** | Room Persistence Library | `2.6.1` |
| **Google Sign-In** | Credential Manager API | `1.3.0` |
| **Google Identity** | `googleid` | `1.1.1` |
| **Play Services Auth** | `play-services-auth` | `21.2.0` |
| **Firebase** | Firebase BoM | `34.10.0` |
| **Firebase Auth** | `firebase-auth` | *(BoM managed)* |
| **Google Services Plugin** | `google-services` | `4.4.2` |
| **Background Tasks** | WorkManager | `2.9.1` |
| **Password Security** | BCrypt via `PasswordUtils` | — |
| **Session Handling** | `SharedPreferences` — `SessionManager` | — |
| **Theme Handling** | `SharedPreferences` — `ThemePrefsManager` | — |
| **Notification Handling** | `NotificationManager` + `WorkManager` | — |
| **Background Threading** | `ExecutorService` for all Room ops | — |
| **Build Tool** | Android Gradle Plugin (Kotlin DSL) | `8.13.1` |
| **IDE** | Android Studio | — |
| **Version Control** | Git & GitHub | — |

---

## 📱 App Flow & Screens

```
┌──────────────────┐
│  Splash Screen   │  Animated logo + progress bar
└────────┬─────────┘
         │
         ├─── [Session exists] ───────────────────────────▶ MainActivity (Home Tab)
         │
         └─── [No session] ──────────────────────────────▶ Login Screen
                                                                │
                  ┌──────────────────────┬────────────────────┤
                  │                      │                     │
         [Google Sign-In]         [Create account]    [Forgot password?]
                  │                      │                     │
    ┌─────────────▼──────────┐ ┌─────────▼────────┐ ┌─────────▼───────────┐
    │  Google Account Picker │ │  Register Screen │ │  Forgot Password    │
    │  Auto-register on      │ │  Full validation │ │  Find by email      │
    │  first Google sign-in  │ │  BCrypt hashed   │ │         │           │
    └─────────────┬──────────┘ └─────────┬────────┘ │         ▼           │
                  │                      │           │  Reset Password     │
                  └──────────────────────┘           │  BCrypt hashed PW   │
                               │                     └──────────┬──────────┘
                               └────────────────────────────────┘
                                                │
                                                ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                        MainActivity — Home Tab  ✅                           │
│  • Favourites horizontal strip — "See All" → FavouritesActivity             │
│  • All Books vertical list — tap card → Book Details                        │
│  • Live search bar + category filter chips (All / Fiction / Science / …)    │
│  • Empty states for no books / no favourites                                │
│  • Filter button → Search tab · Notifications button (bell)                 │
│  • Bottom nav: Home ● | Search | [+ Add] | Diary | Profile                  │
└───────┬──────────────────┬───────────────────┬───────────────────────────────┘
        │ [nav Search]     │ [nav Add]          │ ["See All" on Favourites strip]
        ▼                  ▼                    ▼
┌──────────────────┐  ┌───────────────────┐  ┌──────────────────────────────────┐
│ Search & Filter  │  │   Add Book  ✅    │  │    Favourites Screen  ✅         │
│  ✅  Done        │  │  Cover photo      │  │  Large book cards (96×144dp)     │
│  Live text search│  │  Title, Author    │  │  Live search bar                 │
│  Category chips  │  │  Category/Status  │  │  Status filter chips             │
│  Min-rating btns │  │  1–5 star rating  │  │  Count badge in header           │
│  Favourites only │  │  Review notes     │  │  Tap card → Book Details         │
│  tap → Detail   │  │  Room DB insert   │  │  Empty state with ❤ icon         │
└──────────────────┘  └───────────────────┘  └──────────────────────────────────┘
        │ [nav Diary]
        ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                      Reading Diary Tab  ✅                                   │
│  • Stats row: Total Books | Reviewed | Favourites (live from DB)            │
│  • Live search bar (title / author)                                         │
│  • Status filter chips: All | Want to Read | Reading | Finished             │
│  • Book cards: cover thumbnail, title, author, 5 mini-stars, status badge, │
│    date added — action buttons: 👁 view / ❤ favourite / ✏️ edit             │
└──────────────────────────────────────────────────────────────────────────────┘
        │ [book tap / 👁 button]
        ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                      Book Details Screen  ✅                                 │
│  • Hero book cover (200×300 dp, rounded, gradient overlay)                 │
│  • Title + Author (accent coloured)                                         │
│  • Stats bar: ★ Rating | Category | Reading Status                          │
│  • "Read Now" primary button + Edit (square icon button)                    │
│  • My Review card: 5 stars + review text / empty-state hint                 │
│  • Favourite toggle row (add/remove with icon + label)                      │
│  • ⭐ Ratings & Reviews row → opens RatingsReviewsActivity                  │
│  • Share button → system share sheet                                        │
│  • More (⋮) popup menu → Mark Favourite / Remove Favourite / Delete Book    │
│  • Edit opens EditBookActivity — result refreshes Detail & propagates up    │
└──────────────┬───────────────────────────────────────────────────────────────┘
               │ [Ratings & Reviews row]
               ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                  Ratings & Reviews Screen  ✅  NEW                           │
│  • Overall avg rating — large number + 5-star summary + total count         │
│  • Star distribution bars — animated progress bars for each star (1–5)      │
│    with percentage labels                                                    │
│  • User Reviews section:                                                    │
│    – Review cards: coloured initials avatar, reviewer name, mini stars,     │
│      date, review body text, 👍 / 👎 voting buttons (persisted to DB)       │
│    – Options (⋮) button visible only on your own review → Edit / Delete     │
│  • Sort toggle: Recent ↔ Most Helpful                                       │
│  • Empty state when no reviews exist yet                                    │
│  • "Write Review" / "Edit Your Review" header button                        │
│    → Bottom Sheet: interactive 5-star picker + multi-line text input        │
│    → Save syncs rating & notes back to the Book record                      │
│    → Delete review with confirmation dialog                                 │
└──────────────────────────────────────────────────────────────────────────────┘
        │ [nav Profile]
        ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     Profile & Settings Screen  ✅                            │
│  • Avatar + username + email                                                │
│  • Reading stats (Total Books / Favourites / Reviews) — live from Room DB   │
│  • Change profile photo → camera or gallery → saves to private storage     │
│  • Edit Profile → update name / change password (current PW verified)      │
│  • Notification Settings → per-type toggles + time picker + WorkManager    │
│  • Theme Preference → Light / Dark / System + 4 accent colours             │
│  • My Favourites → opens FavouritesActivity                                 │
│  • Log Out button with confirmation dialog                                  │
└─────────┬───────────────────┬──────────────────────────┬─────────────────────┘
          │ [Edit Profile]    │ [Notification/Theme rows] │ [My Favourites row]
          ▼                   ▼                           ▼
  EditProfileActivity   Settings Activities        FavouritesActivity
```

---

## 🗄️ Database Schema

BookDiary uses the **Room Persistence Library** backed by SQLite.

**Database:** `bookdiary.db` — version `5`

---

### `users` table — `User.java`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INTEGER` | PK, autoGenerate | Auto-generated user ID |
| `username` | `TEXT` | NOT NULL | Display name |
| `email` | `TEXT` | NOT NULL, UNIQUE | Login identifier |
| `password` | `TEXT` | NULLABLE | BCrypt-hashed password — `null` for Google-only accounts |
| `googleId` | `TEXT` | NULLABLE | Firebase UID — `null` for email/password accounts |
| `photoUrl` | `TEXT` | NULLABLE | Google profile photo URL or local file path |

#### DAO Methods — `UserDao.java`

| Method | Description |
|---|---|
| `insertUser(User)` | Register new user — aborts on duplicate email |
| `login(email)` | Fetch user by email for BCrypt verification |
| `findByEmail(email)` | Check if email is already registered |
| `updatePassword(email, newHash)` | Reset BCrypt-hashed password in DB |
| `findByGoogleId(googleId)` | Lookup Google-authenticated user by Firebase UID |
| `insertGoogleUser(User)` | Insert Google user — ignores conflict if already exists |

---

### `books` table — `Book.java`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INTEGER` | PK, autoGenerate | Auto-generated book ID |
| `title` | `TEXT` | NOT NULL | Book title |
| `author` | `TEXT` | NOT NULL | Author name |
| `category` | `TEXT` | — | Fiction / Non-Fiction / Science / Mystery / History / Sci-Fi & Fantasy / Biography |
| `rating` | `FLOAT` | — | Star rating 0.0–5.0 (synced from own review when saved) |
| `isFavorite` | `BOOLEAN` | — | Whether marked as favourite |
| `coverUrl` | `TEXT` | NULLABLE | Local file path for picked cover image |
| `notes` | `TEXT` | NULLABLE | Personal diary / review notes (synced from own review) |
| `dateAdded` | `LONG` | — | Epoch milliseconds — used for default sort order |
| `userId` | `INTEGER` | FK (logical) | Scopes all queries to the logged-in user |
| `readingStatus` | `TEXT` | NULLABLE | `"Want to Read"` · `"Currently Reading"` · `"Finished"` |

#### DAO Methods — `BookDao.java`

| Method | Description |
|---|---|
| `insert(Book)` | Add a new book |
| `update(Book)` | Save changes to an existing book |
| `delete(Book)` | Remove a book |
| `getAllBooks(userId)` | All books for user, newest first |
| `getFavoriteBooks(userId)` | Favourite books for user, newest first |
| `getBooksByCategory(userId, category)` | Filter by genre |
| `searchBooks(userId, query)` | Full-text search across title and author |
| `searchAndFilter(userId, query, category)` | Combined text + category filter (Home & Search tabs) |
| `searchAndFilterByStatus(userId, query, status)` | Combined text + reading-status filter (Diary tab) |
| `searchFavourites(userId, query, status)` | Favourite books only, with text + status filter (Favourites screen) |
| `getBookById(bookId)` | Single book by primary key |
| `getBookCount(userId)` | Total book count for a user |
| `getFavoriteCount(userId)` | Favourite count for a user |
| `getReviewCount(userId)` | Count books with non-empty review notes |

---

### `reviews` table — `Review.java` *(NEW)*

Each row represents a single user review attached to a specific book.  Foreign-key cascade ensures reviews are deleted automatically when their parent book is deleted.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INTEGER` | PK, autoGenerate | Auto-generated review ID |
| `bookId` | `INTEGER` | FK → `books.id` CASCADE | The book being reviewed |
| `userId` | `INTEGER` | — | Owner user ID — `0` for community/demo entries |
| `reviewerName` | `TEXT` | — | Display name shown on the review card |
| `reviewerInitials` | `TEXT` | — | 1–2 char initials for the avatar circle |
| `rating` | `FLOAT` | — | Star rating 1.0–5.0 |
| `reviewText` | `TEXT` | — | The written review body |
| `dateMs` | `LONG` | — | Epoch milliseconds — used for "Recent" sort |
| `thumbsUp` | `INTEGER` | — | Helpful vote count |
| `thumbsDown` | `INTEGER` | — | Not-helpful vote count |
| `isOwn` | `BOOLEAN` | — | `true` when written by the logged-in user → shows options (⋮) button |

#### DAO Methods — `ReviewDao.java`

| Method | Description |
|---|---|
| `insert(Review)` | Persist a new review |
| `update(Review)` | Update an existing review |
| `delete(Review)` | Remove a review |
| `getReviewsForBook(bookId)` | All reviews for a book ordered by date descending (Recent) |
| `getReviewsForBookByHelpful(bookId)` | All reviews ordered by thumbsUp descending (Most Helpful) |
| `getOwnReview(bookId, userId)` | Current user's own review for a book (if any) |
| `getReviewCount(bookId)` | Total review count for a book |
| `getAverageRating(bookId)` | Computed average rating across all reviews |
| `getCountForStars(bookId, stars)` | Count of reviews for a specific star level — used for distribution bars |
| `deleteAllForBook(bookId)` | Bulk-delete all reviews when a book is deleted |
| `incrementThumbsUp(reviewId)` | Atomic +1 to helpful votes |
| `incrementThumbsDown(reviewId)` | Atomic +1 to not-helpful votes |

---

## 🔐 Authentication Flow

### Email / Password

```
Register:
  Validate fields → findByEmail() → duplicate? show error
  → PasswordUtils.hash(password) → insertUser() → navigate to Login

Login:
  Validate fields → findByEmail(email)
  → found? PasswordUtils.verify(input, storedHash)
      → match? saveSession() → MainActivity
      → no match? show "Invalid email or password"
  → not found? show "Invalid email or password"

Forgot Password:
  Enter email → findByEmail()
  → found? navigate to ResetPasswordActivity
  → not found? show inline field error

Reset Password:
  Enter new + confirm → passwords match?
  → PasswordUtils.hash(newPassword) → updatePassword()
  → navigate to LoginActivity (back stack cleared)
```

### Google Sign-In

```
Tap "Continue with Google"
    → CredentialManager shows Google account picker
    → GoogleIdTokenCredential returned (ID token + email + display name + photo)
    → FirebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken))
    → Firebase verifies token → returns Firebase UID
    → findByGoogleId(firebaseUid)
        ├── Found     → saveSession(user) → MainActivity
        └── Not found → findByEmail(email)
                            ├── Found  → link googleId + photoUrl → saveSession() → MainActivity
                            └── New    → insertGoogleUser() → saveSession() → MainActivity
```

---

## 📱 Session Management

`SessionManager` uses `SharedPreferences` (file: `bookdiary_session`) to persist login state across app restarts.

| Key | Type | Description |
|---|---|---|
| `is_logged_in` | `Boolean` | Whether a user is currently logged in |
| `user_id` | `Int` | Room database user ID |
| `username` | `String` | Display name |
| `email` | `String` | User's email address |
| `photo_url` | `String` | Profile photo URL/path |

**Usage from any Fragment:**
```java
SessionManager session = new SessionManager(requireContext());
String username = session.getUsername();
String email    = session.getEmail();
String photoUrl = session.getPhotoUrl();
int    userId   = session.getUserId();
```

**Logout from ProfileFragment:**
```java
((MainActivity) requireActivity()).logout();
```

---

## 🌙 Theme & Accent Colour System

`ThemePrefsManager` persists the chosen mode and accent colour in `SharedPreferences` (file: `bookdiary_theme_prefs`).

### Theme Modes

| Constant | Behaviour |
|---|---|
| `MODE_LIGHT` | Always light |
| `MODE_DARK` | Always dark |
| `MODE_SYSTEM` | Follows device dark/light setting automatically |

### Accent Colours

| Name | Hex | Applied to |
|---|---|---|
| Ocean Blue *(default)* | `#1152D4` | Buttons, FAB, active nav icon, chip pills, stat highlights |
| Royal Purple | `#7C3AED` | Same targets |
| Emerald | `#10B981` | Same targets |
| Sunset | `#F97316` | Same targets |

`BaseActivity` and `BaseFragment` re-apply the accent on every `onResume` via `AccentColorHelper`, so the whole app re-colours dynamically without a restart.

---

## 🔔 Notification System

| Notification Type | Worker | Description |
|---|---|---|
| Reading Reminder | `ReadingReminderWorker` | Daily reminder at user-chosen time |
| Daily Quote | `DailyQuoteWorker` | Inspirational reading quote each morning |
| Recommendations | `RecommendationWorker` | Periodic book recommendation nudges |

`NotificationScheduler` uses **WorkManager** to enqueue/cancel workers. `BootReceiver` re-schedules after reboot. Runtime `POST_NOTIFICATIONS` permission requested on Android 13+.

---

## 🧭 Navigation

Fully custom bottom navigation bar (not `BottomNavigationView`) to support the floating "Add" button that rises above the nav bar.

| Tab | Fragment | Status |
|---|---|---|
| 🏠 Home | `HomeFragment` | ✅ Full — books list, favourites strip, search, filter |
| 🔍 Search | `SearchFragment` | ✅ Full — text search, category, rating, favourites toggle |
| ➕ **Add** *(floating)* | `AddFragment` | ✅ Full — cover photo, form, star rating, Room DB save |
| 📓 Diary | `DiaryFragment` | ✅ Full — stats, search, status filter, book cards |
| 👤 Profile | `ProfileFragment` | ✅ Full — avatar, stats, edit, settings, logout |

Fragment transitions use `fade_in / fade_out` animation. Back press from Login is blocked — users must log out from Profile.

---

## 🗂️ Project Structure

```
BookDiary/
├── app/
│   ├── google-services.json              ← Firebase config (⚠️ replace with your file)
│   ├── build.gradle.kts                  ← App-level Gradle (plugins, deps)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/me/nethma/bookdiary/
│       │   ├── database/
│       │   │   ├── AppDatabase.java      ← Room @Database singleton (version 5)
│       │   │   ├── User.java             ← @Entity — users table
│       │   │   ├── UserDao.java          ← @Dao — all user queries
│       │   │   ├── Book.java             ← @Entity — books table
│       │   │   ├── BookDao.java          ← @Dao — all book queries
│       │   │   ├── Review.java           ← @Entity — reviews table (NEW)
│       │   │   └── ReviewDao.java        ← @Dao — all review queries (NEW)
│       │   │
│       │   ├── utils/
│       │   │   ├── AccentColorHelper.java        ← Applies accent colour to all tinted views
│       │   │   ├── BootReceiver.java              ← Re-schedules notifications after reboot
│       │   │   ├── DailyQuoteWorker.java          ← WorkManager — daily quote notification
│       │   │   ├── GoogleSignInHelper.java        ← Credential Manager + Firebase Auth wrapper
│       │   │   ├── NotificationHelper.java        ← Creates channels & posts notifications
│       │   │   ├── NotificationPrefsManager.java  ← SharedPreferences for notification settings
│       │   │   ├── NotificationScheduler.java     ← WorkManager enqueue/cancel logic
│       │   │   ├── PasswordUtils.java             ← BCrypt hash & verify
│       │   │   ├── ReadingReminderWorker.java     ← WorkManager — daily reading reminder
│       │   │   ├── RecommendationWorker.java      ← WorkManager — recommendation nudge
│       │   │   ├── SessionManager.java            ← SharedPreferences session handler
│       │   │   └── ThemePrefsManager.java         ← SharedPreferences theme & accent prefs
│       │   │
│       │   ├── BaseActivity.java                  ← Applies saved theme/accent on every Activity
│       │   ├── BaseFragment.java                  ← Applies accent colour on every Fragment resume
│       │   ├── BookDiaryApp.java                  ← Application class — sets theme mode at startup
│       │   │
│       │   ├── SplashActivity.java                ← Animated launch screen, session-aware routing
│       │   ├── LoginActivity.java                 ← Email/password + Google Sign-In
│       │   ├── RegisterActivity.java              ← New user registration with validation
│       │   ├── ForgotPasswordActivity.java        ← Find account by email
│       │   ├── ResetPasswordActivity.java         ← Set new BCrypt-hashed password in Room DB
│       │   │
│       │   ├── MainActivity.java                  ← Fragment host + custom bottom nav controller
│       │   ├── HomeFragment.java                  ← Home: favourites strip + all books + search + filter chips
│       │   ├── SearchFragment.java                ← Search & filter: text + category + rating + fav toggle
│       │   ├── AddFragment.java                   ← Add book: form + cover picker + star rating + DB save
│       │   ├── DiaryFragment.java                 ← Diary: stats + search + status filter + book cards
│       │   ├── ProfileFragment.java               ← Profile: avatar + stats + settings menu + logout
│       │   │
│       │   ├── BookDetailActivity.java            ← Full book detail: cover hero, stats, review, fav, ratings row
│       │   ├── EditBookActivity.java              ← Edit/delete book with pre-filled form
│       │   ├── FavouritesActivity.java            ← Dedicated Favourites screen: search + filter + full cards
│       │   ├── EditProfileActivity.java           ← Update display name / change password
│       │   ├── NotificationSettingsActivity.java  ← Notification toggles + time picker
│       │   ├── ThemePreferenceActivity.java       ← Theme mode + accent colour picker
│       │   ├── RatingsReviewsActivity.java        ← Ratings & Reviews: summary, distribution, cards (NEW)
│       │   │
│       │   ├── AllBooksAdapter.java               ← RecyclerView adapter — Home "All Books" vertical list
│       │   ├── FavoriteBookAdapter.java           ← RecyclerView adapter — Home favourites horizontal strip
│       │   ├── FavouritesCardAdapter.java         ← RecyclerView adapter — Favourites screen full cards
│       │   ├── SearchResultAdapter.java           ← RecyclerView adapter — Search results list
│       │   └── DiaryBookAdapter.java              ← RecyclerView adapter — Diary book cards
│       │
│       └── res/
│           ├── layout/
│           │   ├── activity_splash.xml
│           │   ├── activity_login.xml
│           │   ├── activity_register.xml
│           │   ├── activity_forgot_password.xml
│           │   ├── activity_reset_password.xml
│           │   ├── activity_main.xml                    ← CoordinatorLayout + custom bottom nav
│           │   ├── activity_book_detail.xml             ← Hero cover, stats bar, review card, fav row, ratings row
│           │   ├── activity_edit_book.xml               ← Pre-filled edit form + delete button
│           │   ├── activity_favourites.xml              ← Favourites screen: search + filter chips + RecyclerView
│           │   ├── activity_edit_profile.xml
│           │   ├── activity_notification_settings.xml
│           │   ├── activity_theme_preference.xml
│           │   ├── activity_ratings_reviews.xml         ← Ratings & Reviews screen layout (NEW)
│           │   ├── fragment_home.xml
│           │   ├── fragment_search.xml
│           │   ├── fragment_add.xml
│           │   ├── fragment_diary.xml                   ← Stats row + search + chips + RecyclerView
│           │   ├── fragment_profile.xml
│           │   ├── item_book_list.xml                   ← Card: cover + title + author + rating + category + fav
│           │   ├── item_book_favorite.xml               ← Compact card for favourites horizontal strip
│           │   ├── item_search_result.xml               ← Search result card with bookmark toggle
│           │   ├── item_category_chip.xml               ← Reusable pill chip (Home, Search, Diary, Favourites)
│           │   ├── item_diary_entry.xml                 ← Diary card: cover + stars + status badge + actions
│           │   ├── item_fav_card.xml                    ← Favourites card: large cover + stars + category badge
│           │   ├── item_review_card.xml                 ← Review card: avatar + stars + text + thumbs (NEW)
│           │   └── dialog_add_review.xml                ← Bottom sheet: star picker + review input (NEW)
│           ├── drawable/                                ← 80+ vector icons, bg shapes, gradients, selectors
│           │   ├── ic_thumb_up.xml                      ← Thumbs up icon (NEW)
│           │   ├── ic_thumb_down.xml                    ← Thumbs down icon (NEW)
│           │   ├── bg_reviewer_avatar.xml               ← Oval avatar background (NEW)
│           │   ├── bg_user_review_card.xml              ← Rounded review card background (NEW)
│           │   └── bg_rating_bar_progress.xml           ← Custom progress bar for distribution (NEW)
│           ├── drawable-night/                          ← Dark-mode drawable overrides
│           └── values/
│               ├── colors.xml                           ← Semantic colour aliases (light defaults)
│               ├── values-night/colors.xml              ← Dark-mode overrides
│               ├── strings.xml                          ← All app strings (reviews_ group added)
│               ├── themes.xml                           ← Material3 theme + BookCoverShape style + FormLabel
│               └── dimens.xml
│
├── gradle/
│   └── libs.versions.toml                ← Version catalog
├── build.gradle.kts                      ← Root Gradle config
└── settings.gradle.kts
```

---

## 🎨 Design System

| Token | Light | Dark | Usage |
|---|---|---|---|
| `app_background` | `#F6F6F8` | `#101622` | Screen backgrounds |
| `app_surface` | `#FFFFFF` | `#1A2236` | Cards, input containers |
| `app_surface2` | `#F1F5F9` | `#0D1829` | Review cards, secondary surfaces |
| `primary` *(default)* | `#1152D4` | — | Buttons, active nav, links, chips |
| `text_primary` | `#0F172A` | `#F1F5F9` | Headings, body text |
| `text_secondary` | `#64748B` | `#94A3B8` | Subtitles, labels |
| `text_hint` | `#94A3B8` | `#64748B` | Placeholder text, empty states |
| `app_divider` | `#E2E8F0` | `#1E293B` | Borders, separators |
| `star_color` | `#FBBF24` | — | Star ratings |

**Accent options:** Ocean Blue `#1152D4` · Royal Purple `#7C3AED` · Emerald `#10B981` · Sunset `#F97316`

**Typography:** Manrope (Google Fonts) — letter-spacing and weight tuned per screen.

**Book cover shape:** `BookCoverShape` — 12 dp rounded corners via `ShapeableImageView`.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Meerkat 2024.3 or later)
- Android SDK API 24+
- Java 11
- A Google account (for Firebase / Google Sign-In setup)

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/your-username/BookDiary.git

# 2. Open in Android Studio
#    File → Open → Select the BookDiary folder

# 3. Sync Gradle
#    File → Sync Project with Gradle Files

# 4. Run on emulator or physical device
#    Run → Run 'app'
```

> Email/password features work fully offline with no extra setup.
> Google Sign-In requires the Firebase configuration steps below.

---

## 🔵 Google Sign-In Setup

### Step 1 — Create a Firebase Project

1. Go to **[Firebase Console](https://console.firebase.google.com)**
2. Click **Add project** → name it `BookDiary` → Continue

### Step 2 — Register your Android App

1. Firebase Console → click the **Android** icon
2. Enter package name: `me.nethma.bookdiary`
3. Get your **SHA-1 fingerprint**:
   ```bash
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" ^
     -alias androiddebugkey -storepass android -keypass android
   ```
4. Paste the SHA-1 → **Register app**
5. Download **`google-services.json`** → place in `/app/`

### Step 3 — Enable Google Sign-In

Firebase Console → **Authentication** → **Sign-in method** → Enable **Google** → Save.

### Step 4 — Add Web Client ID

```java
// utils/GoogleSignInHelper.java
public static final String WEB_CLIENT_ID =
        "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com";
```

---

## 🔒 Security

| Area | Implementation |
|---|---|
| **Password storage** | BCrypt hash (via `PasswordUtils`) — no plain text ever stored |
| **Google auth** | Firebase ID Token verified server-side via `FirebaseAuth` |
| **Data isolation** | All book & review queries scoped by `userId` — complete per-user privacy |
| **Thread safety** | All Room operations run on background `ExecutorService` threads |
| **Credential Manager** | Uses Android's modern `CredentialManager` API (not legacy `GoogleSignInClient`) |

---

## 🔮 Planned / Future Enhancements

- [ ] 🌐 **Cloud Sync** — Back up diary to Firestore for cross-device access
- [ ] 📚 **Google Books API** — Search for books and auto-fill cover, author, page count
- [ ] 🔁 **Reading Progress** — Track percentage progress for "Currently Reading" books
- [ ] 📊 **Stats Dashboard** — Charts for books read per month, genre distribution, avg rating
- [ ] 🔖 **Reading Lists** — Create and share custom curated book lists
- [ ] 🌍 **Export Diary** — Export reading history as PDF or CSV

---

## 📋 Module Information

| Detail | Info |
|---|---|
| **Module Code** | ICT3214 |
| **Module Name** | Mobile Application Development |
| **Project Idea** | #5 — Book Recommendation Diary |
| **Package Name** | `me.nethma.bookdiary` |
| **Version** | 1.0 (versionCode 1) |
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Target SDK** | 36 |
| **Last Updated** | March 2026 |

---

## 📄 License

This project is submitted as academic coursework for ICT3214 — Mobile Application Development.
© 2026 BookDiary. All rights reserved.

---

<div align="center">
  <i>"Read. Review. Remember."</i><br><br>
  Built with ❤️ for ICT3214 — Mobile Application Development
</div>

