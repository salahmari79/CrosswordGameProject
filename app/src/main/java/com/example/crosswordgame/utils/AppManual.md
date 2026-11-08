# English Crossword Learning Game - Technical Architecture Manual

## 1. Overview
An educational Android application designed for Arabic speakers to learn English through crossword puzzles. Key features include a high-performance **Drag & Drop** mechanic, an educational layer with **pronunciation (TTS)**, and a production-ready **Firebase Authentication** system.

## 2. Architecture Diagram (Textual)
- **UI Layer (Compose):** `GameScreen`, `ScrambledLettersPanel`, `CrosswordGrid`.
- **Logic Layer (MVVM):** `GameViewModel` (State management), `CrosswordGenerator` (Stage creation).
- **Data Layer (Persistence):** `AppDatabase` (Room), `SaveManager` (Cloud/Local sync).
- **Auth Layer:** `AuthManager` (Google/Facebook/Firebase).

## 3. Key Mechanisms

### Drag & Drop System
- **State:** Managed by `DragDropState` and `DragDropContainer`.
- **Physics:** `CollisionDetector` uses a 0.4f tolerance for mobile touch accuracy.
- **Visuals:** Tiles scale to 1.15x and lift 1.8x height during drag to ensure finger visibility.

### Educational Layer
- **Spaced Repetition:** `WordDao` prioritizes words with a high `errorCount` and older `lastAttemptTimestamp`.
- **Pronunciation:** Handled by `GameAudioManager` using Android's native `TextToSpeech` engine.
- **Localization:** Designed for Arabic RTL hints and English LTR grid solutions.

## 4. Local Storage Structure
- **Room Database:** Table `words` stores difficulty, category, error counts, and solved state.
- **SharedPreferences:** Stores user profile settings and guest mode progress.

## 5. Build & Setup Instructions
1. **Database Seeding:** Automatically populated from `.csv` files in `res/raw` during first launch.
2. **OAuth Configuration (Action Required):**
    - Obtain **SHA-1 Fingerprint** (Run `./gradlew signingReport`).
    - Register Android app in **Firebase Console**.
    - Update `google-services.json` in the `app` directory.
    - Update `facebook_app_id` and `default_web_client_id` in `res/values/strings.xml`.

## 6. Known Improvements / Backlog
- [ ] Integration of remote image assets for illustrative hints.
- [ ] Global Leaderboard implementation via Firestore.
- [ ] Support for multi-word phrases.
