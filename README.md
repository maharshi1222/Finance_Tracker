# Finance Tracker (Android)

Android Studio project for tracking income, expenses, financial analysis, reminders, and account flows.

## Tech stack
- Java / Android SDK
- Gradle Android application plugin
- Firebase Authentication / Firestore / Realtime Database (as configured in the app)
- JUnit for local unit tests

## Open locally
1. Open this folder in Android Studio.
2. Install the Android SDK platform required by `compileSdk` (36) and a compatible JDK (17 recommended for the build toolchain).
3. Let Gradle sync.
4. Run the `app` configuration on an emulator or Android device.
5. Run unit tests with `./gradlew testDebugUnitTest` and build with `./gradlew assembleDebug`.

> `local.properties` is intentionally excluded because it contains a machine-specific SDK path. Review Firebase configuration and security rules before publishing. Never commit service-account keys, signing keys, or other private credentials.

## Agile/Scrum coursework
See [`docs/AGILE_SCRUM_PLAN.md`](docs/AGILE_SCRUM_PLAN.md) for the CSE706 deliverables, two-sprint plan, testing approach, risks, and honest solo-work contingency.

## CI
GitHub Actions workflow: `.github/workflows/android-ci.yml`. It runs local unit tests and assembles a debug APK on pushes and pull requests, then runs an Android instrumentation smoke test on an emulator. CI has not been executed from this handoff package; check the Actions tab after pushing it to GitHub.
