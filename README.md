# COLOR PREDICTOR PRO

**Color Predictor Pro** is a fully offline, privacy-first color and number prediction statistics analyzer built for Android using Kotlin and Jetpack Compose. It allows users to manually record outcomes from color prediction rounds and inspect rigorous empirical telemetry—without any third-party API scraping, gambling automation, or fabricated win-rate signals.

---

## 🔒 Ethical & Statistical Foundations

- **No Server "Hacks" or Guaranteed Signals:** The app explicitly rejects the false premise that future server-generated pseudo-random seeds can be predicted with certainty.
- **Independence of Random Trials:** Highlights the **Gambler's Fallacy** across all screens. Historical frequencies explain what happened in past recorded games; in independent random draws, past streaks do not increase or decrease the likelihood of future outcomes (a color is never "due").
- **100% Offline & Private:** No user accounts, no subscriptions, no tracking SDKs, and zero internet requests. All records are stored locally in on-device SQLite via Room Database.

---

## 🚀 Key Features

### 1. Home Telemetry Dashboard
- Live overview metrics: Total recorded entries, total Red, total Green, and total Violet results.
- Top recorded number (empirical mode) with sample size indication.
- Horizontal recent outcome timeline with visual color badges.
- Prominent 1-tap "Record New Result" action button.
- Comprehensive educational card on the Gambler's Fallacy and sample size variance.

### 2. Module A: Red / Green Analyzer
- Binary outcome tracking (RED vs GREEN).
- Real-time frequency counts and percentages with explicit sample sizes (`e.g. 48.0% (N=25)`).
- Consecutive historical streak telemetry:
  - Current streak detection.
  - Maximum consecutive RED runs.
  - Maximum consecutive GREEN runs.
- Observation windows: All, Last 10, Last 25, Last 50, Last 100 rounds.
- Custom Jetpack Compose Canvas bar chart and proportion ratio bar.
- Experimental statistical estimate explicitly documented as uncertain and assumption-bound.

### 3. Module B: Red / Green / Violet Analyzer
- Tri-color distribution tracking (RED, GREEN, VIOLET).
- Comparative ratio bars and frequency histograms.
- Temporal subset comparison: compares the earlier half of recorded rounds against the recent half to evaluate short-term variance.
- Clear demarcation between descriptive past statistics and future probability.

### 4. Module C: Number Analyzer (0–9)
- Full 10-digit outcome frequency tracking.
- Hot (empirical mode) and cold (least frequent) number identification.
- 10-column Canvas histogram with color-coded boundaries corresponding to standard digit rules.
- Complete 0–9 frequency table with exact counts and percentages.
- Small dataset warning banner when sample size is below $N < 10$.

### 5. Add / Edit Result Module
- Dynamic outcome inputs:
  - Red / Green mode: RED or GREEN buttons.
  - Three-color mode: RED, GREEN, or VIOLET buttons.
  - Number mode: 0–9 interactive touch grid.
- Auto-suggests next sequential round ID based on latest entry.
- Validates against blank fields, invalid numbers, and accidental duplicate round identifiers.
- Supports editing previously saved records with instant database updates.

### 6. Searchable History Management
- Full searchable list of recorded rounds by ID or notes.
- Filtering by game type (All, Red/Green, 3-Color, Numbers).
- Reversible sorting: Newest First or Oldest First.
- Edit and delete actions with confirmation dialogs.
- Clear All feature with double-confirmation protection.

### 7. Settings & Data Portability
- Theme selector: Dark, Light, or System Default (persisted via Jetpack DataStore).
- Haptic tactile feedback toggle.
- Storage Access Framework (SAF) CSV Export: creates clean `.csv` backups directly to device storage with zero broad storage permissions.
- SAF CSV Import: validates imported rows individually, rejecting malformed rows and reporting detailed line-by-line error messages.
- Complete local database wipe function.

---

## 🛠 Technology Stack

- **Platform:** Native Android (Kotlin 2.x)
- **UI Toolkit:** Jetpack Compose with Material Design 3 (M3)
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Local Persistence:** Room Database (SQLite) with multi-column indexing
- **Preferences:** Jetpack DataStore (Preferences)
- **Navigation:** Navigation Compose
- **Concurrency:** Kotlin Coroutines & Flow (`StateFlow`, `collectAsStateWithLifecycle`)
- **Testing:** Local JVM Unit Tests (JUnit 4)

---

## 📊 Database Schema

Entity: `PredictionRecord`
- `id`: `Long` (Primary Key, autoGenerate = true)
- `gameType`: `String` ("RED_GREEN", "RED_GREEN_VIOLET", "NUMBER")
- `roundId`: `String` (Indexed)
- `colorResult`: `String?` ("RED", "GREEN", "VIOLET")
- `numberResult`: `Int?` (0..9)
- `notes`: `String?`
- `timestamp`: `Long` (Indexed)

---

## 🧪 Unit Tests

- `StatisticalAnalysisEngineTest`:
  - Empty dataset handling without crashes or division by zero.
  - Frequency counts and percentage rounding.
  - Number validation (0..9 bounds).
  - Streak calculations (current streak & maximum streaks).
  - Observation window slicing.
- `CsvHandlerTest`:
  - CSV export and import round-trip consistency.
  - Malformed row rejection and error reporting.
  - Duplicate round identifier logic.
