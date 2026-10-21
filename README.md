# Smart Predict Parking — Predictive Parking Slot Availability

Android (Jetpack Compose) module for the **Smart Predict Parking** peer-to-peer parking platform. This project implements your component: **Predictive Parking Slot Availability**, including user/admin login and a **statistical admin dashboard**.

## Features

- **User login & registration** (vehicle owners / drivers)
- **Admin login** with analytics dashboard (model metrics, CSV stats, in-app prediction charts)
- **On-device ML**: logistic regression trained from `app/src/main/assets/parking_availability_training.csv`
- **Prediction UI**: enter the same feature set as your dataset (Parking ID, hour, day, holiday, occupancy, duration, cancellation, events)
- **Room database** for users, listed spaces, and prediction logs

## Demo accounts

| Role         | Email                 | Password   |
|--------------|------------------------|------------|
| Admin        | `admin@smartpark.lk`   | `admin123` |
| Driver       | `user@smartpark.lk`    | `user123`  |
| House owner  | `owner@smartpark.lk`   | `owner123` |

## Use your own CSV

Replace the file (keep the same name):

`app/src/main/assets/parking_availability_training.csv`

**Important:** The CSV must live under `app/src/main/assets/` (not only in `app/build/`). Then click **Build → Rebuild Project** and run again.

Supported columns (your dataset format is supported):

- `Parking_ID`
- `Requested_Time` (e.g. `19:00`) or `Requested_Hour`
- `Day_of_Week` (`Mon`–`Sun` or 0–6)
- `Public_Holiday` (0/1)
- `Avg_Occupancy_Last_7_Days` or `Avg_Occupancy`
- `Avg_Parking_Duration` (hours, e.g. `3.1`)
- `Cancellations_Last_7_Days` (count) or `Cancellation_Rate`
- `Event_Nearby` (0/1)
- `Available` (0/1)

The app detects CSV changes automatically (hash) and retrains on next launch. If something looks stuck, **clear app storage** or reinstall.

Regenerate sample data (optional):

```bash
python scripts/generate_training_csv.py
```

## How to run

### Android Studio (recommended)

1. Open **Android Studio** (Ladybug or newer recommended).
2. **File → Open** → select this folder: `AndroidSmartPredictParkingGihan`.
3. Wait for **Gradle sync** to finish (download dependencies if prompted).
4. Connect an Android device (USB debugging) or start an **AVD** emulator (API 24+).
5. Click **Run ▶** on the `app` configuration.

### Command line

```bash
cd AndroidSmartPredictParkingGihan
gradlew.bat assembleDebug
gradlew.bat installDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## App flow

1. **Login** as user or admin.
2. **User homepage**: browse listed P2P spaces → set requirements (location, max price, time, day, holiday, event) → **Search parking spaces** → each result shows **Likely available / Likely unavailable** with ML confidence.
3. **Advanced prediction** (optional): fine-tune one slot manually.
4. **House owner**: login or **Register as house owner** → add/remove your parking spaces (ID, title, location, price, security).
5. **Admin dashboard** (overview): users, spaces, predictions → **User management panel** (add/edit/delete users) → **Statistical analytics dashboard**.
6. **Statistical analytics**: charts, CSV breakdowns, model metrics, prediction logs by hour/day.

**Note:** Database version 2 resets local DB on upgrade (demo users/spaces are re-seeded on first launch).

## ML notes (for viva / report)

- Algorithm: **logistic regression** (batch gradient descent, 80/20 train/test split).
- Features: normalized hour, day, holiday, occupancy, duration, cancellation, event flag, parking ID signal, hour sin/cos.
- Training runs **once on first app launch** (background coroutine) from the bundled CSV.

## Project structure (component)

- `ml/` — CSV loader, feature encoding, training, inference
- `ui/screens/AdminDashboardScreen.kt` — statistical admin dashboard
- `ui/screens/PredictionScreen.kt` — availability prediction UI
- `data/` — Room entities, auth, prediction logs

## Requirements

- **minSdk** 24, **compileSdk** 37
- Kotlin, Jetpack Compose, Room, Navigation Compose
