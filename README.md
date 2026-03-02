<div align="center">

<!-- <img src="app/src/main/res/drawable/logo.png" width="110" alt="BookDiary Logo"/> -->

# 📚 BookDiary

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
>
> **Package:** `me.nethma.bookdiary`

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
| 🔐 **Register** | ✅ Done | Full validation, duplicate email check, saves to Room DB |
| 🔑 **Login** | ✅ Done | Email/password auth against local Room database, session creation |
| 🔵 **Google Sign-In** | ✅ Done | One-tap Google sign-in via Credential Manager + Firebase Auth token verification |
| 🔓 **Forgot Password** | ✅ Done | Find account by registered email address |
| 🔁 **Reset Password** | ✅ Done | Set new password directly in Room DB — no email link required |
| 📱 **Session Management** | ✅ Done | Persistent login via `SharedPreferences` — stay logged in across app restarts |
| 🏠 **Home Dashboard** | 🔄 Planned | Stats, reading progress, recent books, favourite recommendations |
| 🔍 **Search & Filter** | 🔄 Planned | Search books by title/author, filter by genre/status/rating |
| ➕ **Add Book** | 🔄 Planned | Title, author, genre, cover photo, reading status, personal notes |
| ✏️ **Edit Book** | 🔄 Planned | Update book details and reading status |
| 📋 **Book Details** | 🔄 Planned | Full book info, ratings, reviews, reading diary entries |
| 📓 **Reading Diary** | 🔄 Planned | Daily reading log entries per book with dates and notes |
| ⭐ **Ratings & Reviews** | 🔄 Planned | Star ratings and written reviews for each book |
| ❤️ **Favourites** | 🔄 Planned | Bookmark favourite books with a dedicated favourites screen |
| 👤 **Profile Screen** | 🔄 Planned | Avatar, reading stats, edit profile, change photo |
| ✏️ **Edit Profile** | 🔄 Planned | Update display name, change password |
| 📸 **Change Profile Picture** | 🔄 Planned | Camera capture or gallery pick |
| 🔔 **Notifications** | 🔄 Planned | Reading reminders, recommendation alerts |
| 🌙 **Theme Preference** | 🔄 Planned | Light/dark mode toggle |

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
| **Session Handling** | `SharedPreferences` — `SessionManager` | — |
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
     │  Auto-register on      │ │  Duplicate email │ │         │           │
     │  first Google sign-in  │ │  check via Room  │ │         ▼           │
     └─────────────┬──────────┘ └─────────┬────────┘ │  Reset Password     │
                   │                      │           │  Set new password   │
                   └──────────────────────┘           │  Updates Room DB    │
                                │                     └──────────┬──────────┘
                                │                                │
                                └────────────────────────────────┘
                                                 │
                                                 ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         MainActivity — Home Tab                              │
│  • Reading stats cards (books read, in progress, favourites)                │
│  • Daily inspiration / reading quote                                        │
│  • Recent books feed — last added books, clickable → Book Details           │
│  • "See All" → switches to Diary tab                                        │
│  • Bottom nav: Home ● | Search | [+ Add] | Diary | Profile                  │
└────────────┬────────────────────────┬────────────────────────────────────────┘
             │  [nav Search]          │  [nav Add — floating button]
             ▼                        ▼
┌─────────────────────┐   ┌──────────────────────────────────────────────────┐
│    Search Screen    │   │                  Add Book Screen                 │
│  Search by title    │   │  Title, Author, Genre (dropdown), Cover photo    │
│  Filter by genre    │   │  Reading Status: Want to Read / Reading / Read   │
│  Filter by status   │   │  Personal notes, Start/Finish date               │
│  Filter by rating   │   │  Star rating (1–5), Review text                  │
└─────────────────────┘   │  Save → adds to Room DB → refreshes Home        │
                           └──────────────────────────────────────────────────┘
             │  [nav Diary]
             ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                          Reading Diary Tab                                   │
│  • Chronological reading log entries                                        │
│  • Filter: All / Reading / Completed / Want to Read                        │
│  • Book cards — cover, title, author, status badge, star rating             │
│  • Tap card → Book Details screen                                           │
│  • FAB → Add Book screen                                                    │
└────────────┬───────────────────────────────────────────────────────────────── ┘
             │  [tap book card]
             ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                          Book Details Screen                                 │
│  • Cover photo, title, author, genre badge, reading status                  │
│  • Star rating display + written review                                     │
│  • Start date / finish date / pages read                                    │
│  • Personal diary notes section                                             │
│  • Edit button → opens Edit Book screen (pre-filled)                       │
│  • Delete button → confirmation dialog → removes from DB                   │
│  • Add to Favourites toggle                                                 │
└──────────────────────────────────────────────────────────────────────────────┘
             │  [nav Profile]
             ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Profile & Settings Screen                            │
│  • Avatar + username + reading stats (books read, reviews written)           │
│  • Edit Profile → update name, change password                              │
│  • Change Profile Picture → camera or gallery                               │
│  • Notifications toggle                                                     │
│  • Theme preference (Dark / Light)                                          │
│  • Log Out button with confirmation dialog                                  │
└──────────────────────────────────────────────────────────────────────────────┘
```

**Navigation rules:**
- Splash → auto-routes based on session state (logged in → Home, not logged in → Login)
- Google Sign-In → **auto-registers** new users on first sign-in
- Login → back button **blocked** (must log out explicitly from Profile)
- Register success → navigate to Login screen
- Forgot Password → Find account by email → Reset Password → Login
- Add/Edit/Delete a book → Home & Diary tabs refresh automatically
- Profile logout → confirmation dialog → clears session → Login

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
| `password` | `TEXT` | NULLABLE | Plain-text password — `null` for Google-only accounts |
| `googleId` | `TEXT` | NULLABLE | Firebase UID — `null` for email/password accounts |
| `photoUrl` | `TEXT` | NULLABLE | Google profile photo URL |

> 🔑 All book/diary queries will be filtered by the logged-in user's ID — complete data privacy between accounts.

### DAO Methods — `UserDao.java`

| Method | Description |
|---|---|
| `insertUser(User)` | Register new user — aborts on duplicate email |
| `login(email, password)` | Validate credentials, returns `User` or `null` |
| `findByEmail(email)` | Check if email is already registered |
| `updatePassword(email, newPassword)` | Reset password directly in DB |
| `findByGoogleId(googleId)` | Lookup Google-authenticated user by Firebase UID |
| `insertGoogleUser(User)` | Insert Google user — ignores conflict if already exists |

---

## 🔐 Authentication Flow

### Email / Password

```
Register:
  Validate fields → findByEmail() → duplicate? show error
  → insertUser() → navigate to Login

Login:
  Validate fields → login(email, password)
  → found? saveSession() → MainActivity
  → not found? show "Invalid email or password"

Forgot Password:
  Enter email → findByEmail()
  → found? navigate to ResetPasswordActivity (email passed as Extra)
  → not found? show inline field error

Reset Password:
  Enter new + confirm → passwords match? → updatePassword()
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
| `photo_url` | `String` | Profile photo URL (Google accounts) |

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

## 🧭 Navigation

The app uses a **fully custom bottom navigation bar** (not `BottomNavigationView`) to support the floating "Add" button that pops above the nav bar — matching the UI design exactly.

### Bottom Nav Tabs

| Tab | ID | Fragment | Icon | Active Colour |
|---|---|---|---|---|
| Home | `nav_home` | `HomeFragment` | Home | `#1152D4` |
| Search | `nav_search` | `SearchFragment` | Search | `#1152D4` |
| **Add** *(floating)* | `nav_add` | `AddFragment` | `add_circle` filled | Always primary |
| Diary | `nav_diary` | `DiaryFragment` | `menu_book` | `#1152D4` |
| Profile | `nav_profile` | `ProfileFragment` | Person | `#1152D4` |

- **Active colour:** `#1152D4` (primary blue)
- **Inactive colour:** `#64748B` (slate-500)
- **Add button** — floats **28dp above** the nav bar with a dark card container (`rounded-xl`, `border-primary/20`)
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
│       │   ├── SplashActivity.java       ← Animated launch screen, session-aware routing
│       │   ├── LoginActivity.java        ← Email/password + Google Sign-In
│       │   ├── RegisterActivity.java     ← New user registration with validation
│       │   ├── ForgotPasswordActivity.java  ← Find account by email
│       │   ├── ResetPasswordActivity.java   ← Set new password in Room DB
│       │   ├── MainActivity.java         ← Fragment host + custom bottom nav controller
│       │   │
│       │   ├── HomeFragment.java         ← Home tab
│       │   ├── SearchFragment.java       ← Search & filter tab
│       │   ├── AddFragment.java          ← Add book tab
│       │   ├── DiaryFragment.java        ← Reading diary tab
│       │   ├── ProfileFragment.java      ← Profile & settings tab
│       │   │
│       │   ├── GoogleSignInHelper.java   ← Credential Manager + Firebase Auth wrapper
│       │   └── SessionManager.java       ← SharedPreferences session handler
│       │
│       └── res/
│           ├── layout/
│           │   ├── activity_splash.xml
│           │   ├── activity_login.xml
│           │   ├── activity_register.xml
│           │   ├── activity_forgot_password.xml
│           │   ├── activity_reset_password.xml
│           │   ├── activity_main.xml         ← CoordinatorLayout + custom bottom nav
│           │   ├── fragment_home.xml
│           │   ├── fragment_search.xml
│           │   ├── fragment_add.xml
│           │   ├── fragment_diary.xml
│           │   └── fragment_profile.xml
│           ├── drawable/                 ← 20+ vector icons, bg shapes, gradients
│           ├── menu/
│           │   └── bottom_nav_menu.xml
│           ├── color/
│           │   └── bottom_nav_colors.xml
│           └── values/
│               ├── colors.xml
│               ├── strings.xml
│               ├── themes.xml
│               └── dimens.xml
│
├── gradle/
│   └── libs.versions.toml                ← Version catalog
├── build.gradle.kts                      ← Root Gradle config
├── settings.gradle.kts
└── UI_Screens/                           ← Design references (HTML + PNG per screen)
    ├── splash_screen/
    ├── login_screen_with_icons/
    ├── updated_registration_screen/
    ├── forgot_password_with_splash_logo/
    ├── reset_password_with_logo/
    ├── home_dashboard_updated_nav/
    ├── add_book_screen/
    ├── book_detail_one_review/
    ├── edit_book_with_status_selection/
    ├── reading_diary_balanced_consistent_font/
    ├── search_filter_no_reset/
    ├── ratings_reviews/
    ├── favourites_screen/
    ├── profile_screen_updated_nav/
    ├── edit_profile/
    ├── change_profile_picture_simplified/
    ├── dark_mode_notifications_recommendations_only/
    ├── notification_settings_simplified/
    ├── theme_preference_no_nav/
    └── logout_screen_updated_logo/
```

---

## 🎨 Design System

| Token | Value | Usage |
|---|---|---|
| `primary` | `#1152D4` | Buttons, active nav, links, FAB |
| `primary_dark` | `#0A3BA8` | Pressed/focused states |
| `background_dark` | `#101622` | App background (dark theme) |
| `surface_dark` | `#1A2236` | Cards, input containers |
| `text_primary` | `#F1F5F9` | Headings, body text |
| `text_secondary` | `#94A3B8` | Subtitles, hints, labels |
| `divider` | `#1E293B` | Borders, separators, bottom nav border |
| `star` | `#FBBF24` | Star ratings |

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

Open `GoogleSignInHelper.java` and replace the placeholder:

```java
// app/src/main/java/me/nethma/bookdiary/GoogleSignInHelper.java

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

## 🔒 Security Note

### Email / Password Authentication
Passwords are stored directly in the local Room database.
> ⚠️ **Planned improvement:** Implement SHA-256 hashing via `MessageDigest` before storing passwords — same pattern as the `Reflect` project's `PasswordUtils.java`.

### Google Sign-In Authentication
Google Sign-In is implemented using the **Android Credential Manager API** (`androidx.credentials`).
The Google **ID Token** is verified via **Firebase Authentication** using `GoogleAuthProvider.getCredential()`.
No password is stored for Google users — they are identified by their **Firebase UID** stored in the `googleId` column.

### Thread Safety
All Room database operations run on a **background thread** via `ExecutorService`, strictly following Android's main-thread policy.

---

## 🔮 Upcoming Features

- [ ] 📚 **Books Database** — `books` table with full schema (title, author, genre, cover, status, rating, notes)
- [ ] 📓 **Reading Diary** — per-book diary entries with timestamps
- [ ] ⭐ **Ratings & Reviews** — star ratings and written reviews
- [ ] ❤️ **Favourites** — bookmark favourite books
- [ ] 🔍 **Search & Filter** — search by title/author, filter by genre/status/rating
- [ ] 👤 **Profile Screen** — reading stats, edit name, change password, profile photo
- [ ] 📸 **Profile Photo** — camera capture + gallery pick, saved to private storage
- [ ] 🔔 **Notifications** — reading reminders with runtime permission (Android 13+)
- [ ] 🌙 **Theme Toggle** — light/dark mode preference persisted via `SharedPreferences`
- [ ] 🔒 **Password Hashing** — SHA-256 via `MessageDigest` before storing to Room DB

---

## 👥 Team & Responsibilities

| Member | Responsibility |
|---|---|
| **Sandun Madhushan** | Project Setup, Authentication (Login, Register, Forgot/Reset Password, Google Sign-In), Session Management, Room Database, Bottom Navigation, Splash Screen |
| *(Team Member 2)* | Home Dashboard, Book Search & Filter |
| *(Team Member 3)* | Add Book, Edit Book, Reading Status |
| *(Team Member 4)* | Reading Diary, Ratings & Reviews |
| *(Team Member 5)* | **Profile Screen**, Edit Profile, Change Profile Picture |
| *(Team Member 6)* | Favourites, Notifications, Theme Preference |

> ### 📌 Note for Profile Screen Developer
>
> The `ProfileFragment` is already **wired into `MainActivity`'s bottom navigation**.
> Build the UI in `fragment_profile.xml` and the logic in `ProfileFragment.java`.
>
> **Access logged-in user data:**
> ```java
> SessionManager session = new SessionManager(requireContext());
> String username = session.getUsername();
> String email    = session.getEmail();
> String photoUrl = session.getPhotoUrl(); // Google photo URL or ""
> int    userId   = session.getUserId();
> ```
>
> **Trigger logout:**
> ```java
> ((MainActivity) requireActivity()).logout();
> // Clears SharedPreferences + navigates to LoginActivity (back stack cleared)
> ```

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

---

## 📄 License

This project is submitted as academic coursework for ICT3214 — Mobile Application Development.
© 2026 BookDiary. All rights reserved.

---

<div align="center">
  <i>"Read. Review. Remember."</i><br><br>
  Built with ❤️ for ICT3214 — Mobile Application Development
</div>

