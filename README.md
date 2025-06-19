# 📱 Android Social Media Platform

A fully-featured Android-based social networking application that allows users to register, log in, create posts, interact with others, and chat in real time. Built using Java and Firebase, it replicates the core experiences of modern social media platforms.

---

## 📌 Core Features

The platform supports the following functionalities:

### 🔐 Authentication

- Checks if a user is already authenticated at app launch
- Login with email/password or Google
- Register new users with name, email, and password
- Handles invalid login credentials with appropriate error messages
- Redirects users to the feed after successful authentication
- Maintains session until logout

### 🏠 Feed Interaction

- View a feed populated with posts from followed users
- Like posts with a single tap (saves to Firebase)
- Add comments to posts (real-time sync with Firebase)
- Create new posts by entering title, text, and uploading/taking an image
- Posts are saved to Firebase and displayed instantly

### 💬 Messaging

- One-on-one real-time chat
- Select a user to open a private chat
- Messages are stored in Firebase and synced across devices

### 👥 Profiles & Social

- View and edit your profile (username, profile picture, bio)
- Access your friend list
- View other users' profiles and follow/unfollow them
- Search for users using keywords
- See posts and info of other users from their profiles

### 🔔 Notifications & Logout

- Push notifications for new posts

---

## 🔧 Technologies Used

- **Java** – Core development language
- **Android SDK** – App interface and lifecycle
- **Firebase Authentication** – Secure user authentication
- **Firebase Firestore / Realtime Database** – Real-time post, comment, and message storage
- **Firebase Storage** – Media storage (images, profile photos)
- **Picasso / Glide** – Image loading and caching
- **MVP Architecture** – Clear separation between View, Presenter, and Model layers

---

## ⚙️ Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/AndreeaDraghici/Android-Social-Media-Platform.git
cd Android-Social-Media-Platform
```

### 2. Open in Android Studio

- Open the project in Android Studio
- Sync all Gradle dependencies

### 3. Configure Firebase

- Go to [Firebase Console](https://console.firebase.google.com)
- Create a new project
- Enable:
  - Authentication (Email/Password)
  - Cloud Firestore or Realtime Database
  - Firebase Storage
- Download `google-services.json` and place it in the `app/` directory

### 4. Build & Run

- Set up an emulator or connect a real device
- Run the application
- Grant permissions for storage, camera, and notifications

---

## 🧱 App Architecture (MVP)

The application follows the **Model–View–Presenter (MVP)** pattern.

```
View (Activity/Fragment)
    ↔ Presenter (UI logic and interaction handling)
        ↔ Model (Firebase, Local Database, Repositories)
```

- **View**: Displays data and forwards user interactions
- **Presenter**: Contains UI logic and calls the model
- **Model**: Handles data operations (Firebase, Storage, etc.)

This architecture improves code separation, testability, and maintainability.

---

## 📂 Project Structure

The project follows a modular package structure grouped by functionality, which improves scalability and clarity.

```
com.ucv.ace.socialmediaplatform/
│
├── model/                      # Data models for Firebase and UI
│   ├── ModelChat.java
│   ├── ModelComment.java
│   ├── ModelPost.java
│   └── ModelUsers.java
│
├── service/
│   ├── activity/               # Screens and user-interactive components
│   │   ├── ChatActivity.java
│   │   ├── PostDetailsActivity.java
│   │   ├── PostLikedActivity.java
│   │   ├── UserProfilePageActivity.java
│   │   └── ViewUserProfileActivity.java
│   │
│   ├── authentication/        # Login and registration
│   │   ├── LoginActivity.java
│   │   └── RegisterActivity.java
│   │
│   ├── board/                 # Main container activity
│   │   └── DashboardActivity.java
│   │
│   └── post/                  # Post-related components
│       ├── adapter/           # RecyclerView adapters
│       │   ├── AdapterChat.java
│       │   ├── AdapterComment.java
│       │   ├── AdapterFriends.java
│       │   ├── AdapterLikes.java
│       │   ├── AdapterPosts.java
│       │   └── AdapterUsers.java
│       │
│       └── fragment/          # UI fragments for features
│           ├── AddPostFragment.java
│           ├── ChatListFragment.java
│           ├── EditProfileFragment.java
│           ├── FriendsFragment.java
│           ├── HomeFragment.java
│           ├── PostsFragment.java
│           ├── ProfileFragment.java
│           └── UsersFragment.java
│
├── AsyncTaskLoadPhoto.java     # Async task for image loading
├── SplashScreen.java           # Launch screen on app start
└── Utils.java                  # Utility functions (shared helpers)
```

### 🔍 Structure Explanation

- **`model/`**: Contains plain data objects used for Firebase mapping and UI rendering.
- **`service/activity/`**: Hosts `Activity` components for post views, chat, and profile pages.
- **`service/authentication/`**: Manages login and registration flows.
- **`service/board/`**: Contains the dashboard activity that hosts all fragments via bottom navigation.
- **`service/post/adapter/`**: Custom adapters for displaying dynamic lists (posts, comments, users, etc.).
- **`service/post/fragment/`**: UI fragments corresponding to the main features (home, post creation, profile, etc.).
- **`AsyncTaskLoadPhoto.java`**: Background image loading task.
- **`SplashScreen.java`**: Displays a loading screen on app launch.
- **`Utils.java`**: Utility class for helper functions used across the app.

---
