
# 🍔 FoodTrack - Android Food Delivery App (Kotlin)

FoodTrack is a Kotlin-based Android application that implements a multi-role architecture using Firebase Firestore and Realtime Database.  
The app supports Admin, Delivery Boy, and User workflows, enabling food management, order handling, and delivery tracking.

It features secure authentication using BCrypt hashing, REST API integration for image uploads via Imgur, and location-based services using OSMDroid and Google Play Services.  

The project demonstrates core concepts such as CRUD operations, role-based access control, and modular Android development with a clean Material UI.
## Features

- Multi-role system (Admin, Delivery Boy, User)
- Custom authentication using BCrypt (hashed passwords)
- Add, edit, and delete food items (CRUD operations)
- Image upload via Imgur API
- Firebase Firestore for data storage
- Firebase Realtime Database for live data updates
- Live delivery tracking using location services
- Map display using OSMDroid (street view/location visualization)
- Role-based access control
- Light and Dark mode support


## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/bhara5t/FoodTrack.git
```

### 2. Open in Android Studio

* Open Android Studio
* Click **Open**
* Select the cloned project folder

### 3. Add Firebase Configuration

* Go to Firebase Console
* Create a new project
* Add Android app with package name:

```
com.example.foodytrack
```

* Download `google-services.json`
* Place it inside:

```
app/
```

### 4. Enable Firebase Services

* Firestore Database
* Firestore Authentication (Email and Password)
* Realtime Database

### 5. Build and Run

* Connect emulator or physical device
* Click ▶ Run

---

## ⚙️ Configuration

### 🔐 Admin & Delivery Login Setup (BCrypt)

This project uses **custom authentication with BCrypt hashing**.

#### Option 1: Generate hash using Kotlin

```kotlin
val hash = BCrypt.hashpw("your_password", BCrypt.gensalt())
println(hash)
```

#### Option 2: Generate hash online

Use any BCrypt generator website.

#### Add manually in Firestore

Collection: `admins` or `delivery_boys`

```
name: Bharat
password: $2a$10$your_hashed_password
```

---

### 🖼️ Imgur API Setup (Image Upload)

Update in `AFoodAddActivity.kt`:

```kotlin
private val IMGUR_CLIENT_ID = "YOUR_CLIENT_ID"
private val IMGUR_UPLOAD_URL = "https://api.imgur.com/3/image"
```

👉 Get Client ID from:
https://api.imgur.com/oauth2/addclient

---

### 🔥 Firebase Setup Notes

* Ensure Firestore rules allow read/write (for testing)
* Realtime DB used for live updates (if enabled)

---

### 📱 Build Requirements

* Android Studio: Latest version recommended
* Java Version: 11
* Kotlin JVM Target: 11
* Compile SDK: 35
* Target SDK: 35
* Minimum SDK: 24

---

### 📦 Dependencies Used

* Firebase Firestore
* Firebase Realtime Database
* BCrypt (password hashing)
* Glide (image loading)
* OkHttp (network requests)
* OSMDroid (map display)
* Google Play Services Location
