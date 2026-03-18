# ADHD Monitor

ADHD Monitor is an Android application focused on helping users manage ADHD-related routines, focus, emotional regulation, and parent or psychologist collaboration. The project combines task support, focus tracking, mood journaling, reminders, reports, and role-based flows inside a single app.

## Highlights

- Focus mode with an accessibility-based blocker for distracting apps such as YouTube variants
- ADHD questionnaire flow with saved reports
- Progress reporting with focus-session history and charts
- Task management with goals and child task views
- Mood journaling and mood history tracking
- Budget tracking for patients and parents
- Community support chat with encouragement and participation points
- Medical history and treatment report workflows with PDF generation
- Scheduled notifications, smart reminders, and priority alerts
- Separate experiences for user, parent, psychologist, and admin roles

## Tech Stack

- Java
- Android SDK
- Gradle Kotlin DSL
- Room database
- Material Components
- WorkManager
- MPAndroidChart
- iText PDF
- Google Play Services Auth

## Project Structure

- `app/src/main/java/com/example/adhd_monitor/`
  Core activities, entities, DAOs, database setup, focus mode, budget, mood, and support modules
- `app/src/main/java/com/example/adhd_monitor/Questionnaire/`
  ADHD questionnaire and report flow
- `app/src/main/java/com/example/adhd_monitor/TreatmentReport/`
  Treatment report creation, listing, and PDF export
- `app/src/main/java/com/example/adhd_monitor/MedicalHistory/`
  Medical and behavioral history features
- `app/src/main/java/com/example/adhd_monitor/notificationManagement/`
  Notification and reminder-related components
- `app/src/main/res/`
  Layouts, drawables, strings, and XML configuration

## Getting Started

### Requirements

- Android Studio
- Android SDK installed locally
- JDK 8+ compatible Android setup

### Setup

1. Clone the repository.
2. Open the project in Android Studio.
3. Let Gradle sync and download dependencies.
4. Create your own local `local.properties` file if Android Studio does not generate it automatically.
5. If your local build uses Google/Firebase configuration, add your own `app/google-services.json`.
6. Run the app on an emulator or Android device.

## Security Notes

- Private local files are intentionally excluded from version control, including `local.properties`, `app/google-services.json`, build outputs, APKs, and keystore files.
- Review authentication, seeded accounts, and local database defaults before using this project in production.
- If you add third-party service credentials, keep them in local-only configuration and never commit them.

## Database

The app uses Room for local persistence. Current modules stored in the local database include:

- Users, parents, psychologists, and admin records
- ADHD questionnaire reports
- Parent-child links
- Medical and behavioral history
- Treatment reports
- Tasks, steps, goals, and focus sessions
- Expenses, monthly budget limits, and savings ratings
- Community messages
- Mood journal entries

## Notes

- The project currently targets `compileSdk 35`, `targetSdk 34`, and `minSdk 24`.
- Some features depend on Android permissions or services such as notifications, exact alarms, foreground service support, and accessibility service access.
- This repository is intended to contain source code only, not local secrets or generated build artifacts.
