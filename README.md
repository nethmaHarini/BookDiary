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

It gives readers a beautiful, distraction-free space to **log the books they read**, **write reviews and ratings**, **track their reading diary**, and **discover recommendations** — all stored privately per user on the device using a local Room database.

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
| 👤 **Profile Screen** | ✅ Done | Avatar, username, email display, accent-coloured stats, edit & settings menu rows |
| ✏️ **Edit Profile** | ✅ Done | Update display name, change password with current-password verification |
| 📸 **Change Profile Picture** | ✅ Done | Camera capture or gallery pick, saved to private app storage |
| 🔔 **Notification Settings** | ✅ Done | Per-type toggles (Reading Reminders, Daily Quote, Recommendations), time picker, WorkManager scheduling, runtime permission (Android 13+) |
| 🌙 **Theme Preference** | ✅ Done | Light / Dark / System (follow device) mode + 4 accent colours; entire app re-themes on save; device dark/light changes honoured automatically |
| 🎨 **Accent Color System** | ✅ Done | Ocean Blue, Royal Purple, Emerald, Sunset — buttons, FAB, nav active icon, profile pencil, stat cards all update dynamically |
| 🔒 **Password Hashing** | ✅ Done | BCrypt hashing via `PasswordUtils` for all password store and verify operations |
| 🏠 **Home Dashboard** | 🖼️ UI Only | Screen designed — logic & data not yet wired |
| 🔍 **Search & Filter** | 🖼️ UI Only | Screen designed — logic & data not yet wired |
| ➕ **Add Book** | 🖼️ UI Only | Screen designed — logic & data not yet wired |
| ✏️ **Edit Book** | 🖼️ UI Only | Screen designed — logic & data not yet wired |
| 📋 **Book Details** | 🖼️ UI Only | Screen designed — logic & data not yet wired |
| 📓 **Reading Diary** | 🖼️ UI Only | Screen designed — logic & data not yet wired |
| ⭐ **Ratings & Reviews** | 🖼️ UI Only | Screen designed — logic & data not yet wired |
| ❤️ **Favourites** | 🖼️ UI Only | Screen designed — logic & data not yet wired |

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| **Language** | Java | 11 |
| **Platform** | Android | Min SDK 24 (Android 7.0+), Target SDK 36 |
| **UI Framework** | XML Layouts, ConstraintLayout | — |
| **Material Components** | Material Design 3 | `1.13.0` |
| **AppCompat** | `androidx.appcompat` | `1.7.1` |
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
| **Notification Handling** | `NotificationManager` + `WorkManager` — `NotificationHelper`, `NotificationPrefsManager`, `NotificationScheduler` | — |
| **Background Threading** | `ExecutorService` for all Room ops | — |
| **Build Tool** | Android Gradle Plugin (Kotlin DSL) | `9.0.1` |
| **IDE** | Android Studio | — |
| **Version Control** | Git & GitHub | — |

---

## 📱 App Flow & Screens

```
┌──────────────────┐
│  Splash Screen   │  Animated logo + progress bar
└────────┬─────────┘
         │
         ├─── [Session exists] ─────────────────────────────▶ MainActivity (Home Tab)
         │
         └─── [No session] ────────────────────────────────▶ Login Screen
                                                                  │
                   ┌──────────────────────┬─────────────────────┤
                   │                      │                      │
          [Google Sign-In]         [Create account]     [Forgot password?]
                   │                      │                      │
     ┌─────────────▼──────────┐ ┌─────────▼────────┐ ┌──────────▼──────────┐
     │  Google Account Picker │ │  Register Screen │ │  Forgot Password    │
     │  (system bottom sheet) │ │  Full validation │ │  Find by email      │
     │  Auto-register on      │ │  Duplicate check │ │         │           │
     │  first Google sign-in  │ │  BCrypt hashed   │ │         ▼           │
     └─────────────┬──────────┘ └─────────┬────────┘ │  Reset Password     │
                   │                      │           │  BCrypt hashed PW   │
                   └──────────────────────┘           │  Updates Room DB    │
                                │                     └──────────┬──────────┘
                                │                                │
                                └────────────────────────────────┘
                                                 │
                                                 ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     MainActivity — Home Tab (UI Only)                        │
│  • Reading stats cards (books read, in progress, favourites)                │
│  • Daily inspiration / reading quote                                        │
│  • Recent books feed — last added books, clickable → Book Details           │
│  • "See All" → switches to Diary tab                                        │
│  • Bottom nav: Home ● | Search | [+ Add] | Diary | Profile                  │
└────────────┬────────────────────────┬────────────────────────────────────────┘
             │  [nav Search]          │  [nav Add — floating button]
             ▼                        ▼
┌─────────────────────┐   ┌──────────────────────────────────────────────────┐
│ Search (UI Only)    │   │            Add Book (UI Only)                    │
│  Search by title    │   │  Title, Author, Genre (dropdown), Cover photo    │
│  Filter by genre    │   │  Reading Status: Want to Read / Reading / Read   │
│  Filter by status   │   │  Personal notes, Start/Finish date               │
│  Filter by rating   │   │  Star rating (1–5), Review text                  │
└─────────────────────┘   └──────────────────────────────────────────────────┘
             │  [nav Diary]
             ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                      Reading Diary Tab (UI Only)                             │
│  • Chronological reading log entries                                        │
│  • Filter: All / Reading / Completed / Want to Read                        │
│  • Book cards — cover, title, author, status badge, star rating             │
└────────────────────────────────────────────────────────────────────────────── ┘
             │  [nav Profile]
             ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     Profile & Settings Screen  ✅ Done                       │
│  • Avatar + username + email + accent-coloured reading stat cards            │
│  • Change profile photo → camera or gallery → saves to private storage      │
│  • Edit Profile → update name / change password (current PW verified)      │
│  • Notification Settings → per-type toggles + time picker + WorkManager     │
│  • Theme Preference → Light / Dark / System + 4 accent colours             │
│  • Log Out button with confirmation dialog                                  │
└─────────┬─────────────────────────┬────────────────────────────────────────── ┘
          │  [Edit Profile]         │  [Notification Settings]
          ▼                         ▼
┌──────────────────────┐  ┌─────────────────────────────────────────────────┐
│  EditProfileActivity │  │         NotificationSettingsActivity             │
│  Update display name │  │  Toggles: Reading Reminder / Daily Quote /      │
│  Change password     │  │          Recommendations                        │
│  (BCrypt verified)   │  │  Time picker for reminder scheduling            │
└──────────────────────┘  │  WorkManager schedules actual notifications     │
                           │  Runtime permission for Android 13+             │
                           └─────────────────────────────────────────────────┘
          │  [Theme Preference]
          ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                      ThemePreferenceActivity  ✅ Done                        │
│  • Mode selector: Light / Dark / Follow Device (System)                     │
│  • Accent colour picker: Ocean Blue / Royal Purple / Emerald / Sunset       │
│  • Live colour preview on selection                                         │
│  • Save Preferences → applies AppCompatDelegate night mode across whole app │
│  • Device dark/light changes auto-applied when Follow Device is set         │
└──────────────────────────────────────────────────────────────────────────────┘
```

**Navigation rules:**
- Splash → auto-routes based on session state (logged in → Home, not logged in → Login)
- Google Sign-In → **auto-registers** new users on first sign-in
- Login → back button **blocked** (must log out explicitly from Profile)
- Register success → navigate to Login screen
- Forgot Password → Find account by email → Reset Password → Login
- Profile logout → confirmation dialog → clears session → Login
- Theme change → applied immediately app-wide via `AppCompatDelegate`
- Accent colour change → accent helper updates all tinted views via `AccentColorHelper`

---

## 🗄️ Database Schema

BookDiary uses the **Room Persistence Library** backed by SQLite.

**Database:** `bookdiary.db` — version `2`

### `users` table — `User.java`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INTEGER` | PK, autoGenerate | Auto-generated user ID |
| `username` | `TEXT` | NOT NULL | Display name |
| `email` | `TEXT` | NOT NULL, UNIQUE | Login identifier (unique index enforced) |
| `password` | `TEXT` | NULLABLE | BCrypt-hashed password — `null` for Google-only accounts |
| `googleId` | `TEXT` | NULLABLE | Firebase UID — `null` for email/password accounts |
| `photoUrl` | `TEXT` | NULLABLE | Google profile photo URL or local file path |

> 🔑 All book/diary queries will be filtered by the logged-in user's ID — complete data privacy between accounts.

### DAO Methods — `UserDao.java`

| Method | Description |
|---|---|
| `insertUser(User)` | Register new user — aborts on duplicate email |
| `login(email)` | Fetch user by email for BCrypt verification |
| `findByEmail(email)` | Check if email is already registered |
| `updatePassword(email, newHashedPassword)` | Reset BCrypt-hashed password in DB |
| `findByGoogleId(googleId)` | Lookup Google-authenticated user by Firebase UID |
| `insertGoogleUser(User)` | Insert Google user — ignores conflict if already exists |

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
  → found? navigate to ResetPasswordActivity (email passed as Extra)
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
                            ├── Found (existing email account)
                            │       → link googleId + photoUrl → saveSession() → MainActivity
                            └── Not found (brand new user)
                                    → insertGoogleUser() → saveSession() → MainActivity
```

---

## 📱 Session Management

`SessionManager` uses `SharedPreferences` (file: `bookdiary_session`) to persist login state across app restarts — no login required on reopen.

| Key | Type | Description |
|---|---|---|
| `is_logged_in` | `Boolean` | Whether a user is currently logged in |
| `user_id` | `Int` | Room database user ID |
| `username` | `String` | Display name |
| `email` | `String` | User's email address |
| `photo_url` | `String` | Profile photo URL/path (Google or local) |

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

| Constant | Value | Behaviour |
|---|---|---|
| `MODE_LIGHT` | `AppCompatDelegate.MODE_NIGHT_NO` | Always light |
| `MODE_DARK` | `AppCompatDelegate.MODE_NIGHT_YES` | Always dark |
| `MODE_SYSTEM` | `AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM` | Follows device dark/light setting automatically |

Applied in `BookDiaryApp.onCreate()` and `BaseActivity.onCreate()` so every screen respects the saved preference instantly, including when the device theme changes at runtime.

### Accent Colours

| Name | Hex | Usage |
|---|---|---|
| Ocean Blue *(default)* | `#1152D4` | Buttons, FAB, active nav icon, profile pencil |
| Royal Purple | `#7C3AED` | Same targets |
| Emerald | `#10B981` | Same targets |
| Sunset | `#F97316` | Same targets |

`AccentColorHelper` applies the current accent colour dynamically to all tinted UI elements — bottom nav add button, save/preferences buttons, profile avatar pencil icon, stat card accents, and more.

---

## 🔔 Notification System

`NotificationPrefsManager` stores per-type preferences in `SharedPreferences` (file: `bookdiary_notification_prefs`).

| Notification Type | Worker | Description |
|---|---|---|
| Reading Reminder | `ReadingReminderWorker` | Daily reminder at user-chosen time |
| Daily Quote | `DailyQuoteWorker` | Inspirational reading quote each morning |
| Recommendations | `RecommendationWorker` | Periodic book recommendation nudges |

`NotificationScheduler` uses **WorkManager** to enqueue/cancel each worker based on toggle state. `BootReceiver` re-schedules notifications after device reboot. Runtime permission (`POST_NOTIFICATIONS`) is requested on Android 13+.

---

## 🧭 Navigation

The app uses a **fully custom bottom navigation bar** (not `BottomNavigationView`) to support the floating "Add" button that pops above the nav bar — matching the UI design exactly.

### Bottom Nav Tabs

| Tab | ID | Fragment | Icon | Active Colour |
|---|---|---|---|---|
| Home | `nav_home` | `HomeFragment` | Home | Accent colour |
| Search | `nav_search` | `SearchFragment` | Search | Accent colour |
| **Add** *(floating)* | `nav_add` | `AddFragment` | `add_circle` filled | Accent colour |
| Diary | `nav_diary` | `DiaryFragment` | `menu_book` | Accent colour |
| Profile | `nav_profile` | `ProfileFragment` | Person | Accent colour |

- **Active colour:** current accent colour (default `#1152D4`)
- **Inactive colour:** `#64748B` (slate-500)
- **Add button** — floats **28dp above** the nav bar, always rendered in accent colour
- Fragment transitions use `fade_in / fade_out` animation

---

## 🗂️ Project Structure

```
BookDiary/
├── app/
│   ├── google-services.json              ← Firebase config (⚠️ replace with your real file)
│   ├── build.gradle.kts                  ← App-level Gradle (plugins, deps)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/me/nethma/bookdiary/
│       │   ├── database/
│       │   │   ├── AppDatabase.java      ← Room @Database singleton (version 2)
│       │   │   ├── User.java             ← @Entity — users table
│       │   │   └── UserDao.java          ← @Dao — all user queries
│       │   │
│       │   ├── utils/
│       │   │   ├── AccentColorHelper.java    ← Applies accent colour to all tinted views
│       │   │   ├── BootReceiver.java         ← Re-schedules notifications after reboot
│       │   │   ├── DailyQuoteWorker.java     ← WorkManager worker — daily quote notification
│       │   │   ├── GoogleSignInHelper.java   ← Credential Manager + Firebase Auth wrapper
│       │   │   ├── NotificationHelper.java   ← Creates notification channels & posts notifications
│       │   │   ├── NotificationPrefsManager.java ← SharedPreferences for notification settings
│       │   │   ├── NotificationScheduler.java    ← WorkManager enqueue/cancel logic
│       │   │   ├── PasswordUtils.java        ← BCrypt hash & verify for passwords
│       │   │   ├── ReadingReminderWorker.java ← WorkManager worker — daily reading reminder
│       │   │   ├── RecommendationWorker.java  ← WorkManager worker — recommendation nudge
│       │   │   ├── SessionManager.java       ← SharedPreferences session handler
│       │   │   └── ThemePrefsManager.java    ← SharedPreferences theme & accent prefs
│       │   │
│       │   ├── BaseActivity.java         ← Applies saved theme/accent on every Activity
│       │   ├── BaseFragment.java         ← Applies accent colour on every Fragment attach
│       │   ├── BookDiaryApp.java         ← Application class — sets theme mode at startup
│       │   ├── SplashActivity.java       ← Animated launch screen, session-aware routing
│       │   ├── LoginActivity.java        ← Email/password + Google Sign-In
│       │   ├── RegisterActivity.java     ← New user registration with validation
│       │   ├── ForgotPasswordActivity.java  ← Find account by email
│       │   ├── ResetPasswordActivity.java   ← Set new BCrypt-hashed password in Room DB
│       │   ├── MainActivity.java         ← Fragment host + custom bottom nav controller
│       │   ├── EditProfileActivity.java  ← Update display name / change password
│       │   ├── NotificationSettingsActivity.java ← Notification toggles + time picker
│       │   ├── ThemePreferenceActivity.java  ← Theme mode + accent colour picker
│       │   │
│       │   ├── HomeFragment.java         ← Home tab (UI stub)
│       │   ├── SearchFragment.java       ← Search & filter tab (UI stub)
│       │   ├── AddFragment.java          ← Add book tab (UI stub)
│       │   ├── DiaryFragment.java        ← Reading diary tab (UI stub)
│       │   └── ProfileFragment.java      ← Profile & settings tab (fully functional)
│       │
│       └── res/
│           ├── layout/
│           │   ├── activity_splash.xml
│           │   ├── activity_login.xml
│           │   ├── activity_register.xml
│           │   ├── activity_forgot_password.xml
│           │   ├── activity_reset_password.xml
│           │   ├── activity_main.xml              ← CoordinatorLayout + custom bottom nav
│           │   ├── activity_edit_profile.xml
│           │   ├── activity_notification_settings.xml
│           │   ├── activity_theme_preference.xml
│           │   ├── fragment_home.xml
│           │   ├── fragment_search.xml
│           │   ├── fragment_add.xml
│           │   ├── fragment_diary.xml
│           │   └── fragment_profile.xml
│           ├── drawable/                 ← 80+ vector icons, bg shapes, gradients, selectors
│           ├── drawable-night/           ← Dark-mode drawable overrides
│           └── values/
│               ├── colors.xml
│               ├── strings.xml
│               ├── themes.xml
│               └── dimens.xml
│
├── gradle/
│   └── libs.versions.toml                ← Version catalog
├── build.gradle.kts                      ← Root Gradle config
└── settings.gradle.kts
```

---

## 🎨 Design System

| Token | Value | Usage |
|---|---|---|
| `primary` *(default accent)* | `#1152D4` | Buttons, active nav, links, FAB — changes with accent selection |
| `primary_dark` | `#0A3BA8` | Pressed/focused states |
| `background_dark` | `#101622` | App background (dark theme) |
| `surface_dark` | `#1A2236` | Cards, input containers (dark theme) |
| `background_light` | `#F8FAFC` | App background (light theme) |
| `surface_light` | `#FFFFFF` | Cards, input containers (light theme) |
| `text_primary` | `#F1F5F9` / `#1E293B` | Headings, body text (dark / light) |
| `text_secondary` | `#94A3B8` / `#64748B` | Subtitles, hints, labels (dark / light) |
| `divider` | `#1E293B` | Borders, separators, bottom nav border |
| `star` | `#FBBF24` | Star ratings |

**Accent colour options:** Ocean Blue `#1152D4` · Royal Purple `#7C3AED` · Emerald `#10B981` · Sunset `#F97316`

**Typography:** Manrope (Google Fonts) — letter-spacing and weight tuned per screen

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

> Basic email/password features work offline with no setup needed.
> Google Sign-In requires the Firebase configuration steps below.

---

## 🔵 Google Sign-In Setup

Google Sign-In uses the **Android Credential Manager API** with a Firebase OAuth2 Web Client ID.

### Step 1 — Create a Firebase Project

1. Go to **[Firebase Console](https://console.firebase.google.com)**
2. Click **Add project** → name it `BookDiary` → Continue
3. Follow the setup wizard to create the project

### Step 2 — Register your Android App

1. In Firebase Console → click the **Android** icon
2. Enter package name: `me.nethma.bookdiary`
3. Enter app nickname: `BookDiary`
4. Get your **SHA-1 fingerprint**:
   ```bash
   # Windows
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" ^
     -alias androiddebugkey -storepass android -keypass android
   ```
   Copy the `SHA1:` value from the output
5. Paste the SHA-1 into Firebase → **Register app**
6. Download **`google-services.json`** → place it in the `/app/` folder, replacing the existing placeholder

### Step 3 — Enable Google Sign-In

1. Firebase Console → **Authentication** → **Sign-in method**
2. Click **Google** → toggle **Enable** → **Save**
3. Copy the **Web Client ID** (ends in `.apps.googleusercontent.com`)

### Step 4 — Add Web Client ID to the app

Open `utils/GoogleSignInHelper.java` and replace the placeholder:

```java
// app/src/main/java/me/nethma/bookdiary/utils/GoogleSignInHelper.java

public static final String WEB_CLIENT_ID =
        "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com";
```

**Where to find it:**
> Firebase Console → Project Settings → Your Apps → Web app → SDK config
> OR
> Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 Client IDs → **Web client (auto created by Google Service)**

### Step 5 — Sync & Run

Sync Gradle files, then run the app. Tap **Continue with Google** on the login screen — the system account picker will appear.

> **Note:** Google Sign-In works on physical devices and emulators with a Google account configured.

---

## 🔒 Security

### Password Hashing
All passwords are hashed with **BCrypt** via `utils/PasswordUtils.java` before being stored in Room DB.
- Registration: `PasswordUtils.hash(password)` → stored hash
- Login: `PasswordUtils.verify(inputPassword, storedHash)` → boolean
- Reset Password: new password is hashed before `updatePassword()` call
- Edit Profile (change password): current password is BCrypt-verified before allowing update

No plain-text passwords are stored anywhere in the app.

### Google Sign-In Authentication
Implemented using the **Android Credential Manager API** (`androidx.credentials`).
The Google **ID Token** is verified via **Firebase Authentication** (`GoogleAuthProvider.getCredential()`).
No password is stored for Google users — identified by **Firebase UID** in the `googleId` column.

### Thread Safety
All Room database operations run on a **background thread** via `ExecutorService`, strictly following Android's main-thread policy.

---

## 🔮 Upcoming / Planned Features

- [ ] 📚 **Books Database** — `books` table (title, author, genre, cover, status, rating, notes, dates)
- [ ] 🏠 **Home Dashboard** — wire up stats cards and recent books to Room DB
- [ ] 🔍 **Search & Filter** — live search by title/author, filter by genre/status/rating
- [ ] ➕ **Add Book** — save form data to Room DB
- [ ] ✏️ **Edit Book** — update book record in Room DB
- [ ] 📋 **Book Details** — full book view with edit/delete/favourite actions
- [ ] 📓 **Reading Diary** — per-book diary entries with timestamps
- [ ] ⭐ **Ratings & Reviews** — star ratings and written reviews per book
- [ ] ❤️ **Favourites** — bookmark favourite books, dedicated favourites screen

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



