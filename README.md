# JetBrains Publish Tracker

A comprehensive IntelliJ Platform plugin for tracking JetBrains Marketplace plugin verification and publishing status.

## 🎯 Problem It Solves

Plugin developers currently have to:
- Manually check the Marketplace web dashboard for status updates
- Wait without feedback during verification
- Get no insight into how long each phase takes (upload, verification, approval, live)
- Have no alert system when their update goes live

**This plugin solves these problems by:**
- ✅ Tracking plugin submissions over time
- ✅ Showing detailed timelines (upload → verification → approval → published)
- ✅ Sending notifications when state changes
- ✅ Building visual charts of verification trends
- ✅ Calculating average durations for each phase

## ✨ Core Features

### 🔐 Marketplace API Integration
- Connects to JetBrains Marketplace REST API
- Securely stores and uses your private API token
- Fetches real-time plugin status updates

### 🧾 Status Dashboard (Tool Window)
- **Status Tab**: Real-time view of all tracked plugins
- Shows latest version, verification progress, timestamps
- Manual refresh and auto-polling (configurable interval)
- Color-coded status indicators

### 📈 Analytics & Charts
- **Analytics Tab**: Visual charts powered by JFreeChart
- Bar chart showing average phase durations (Upload→Verification→Approval→Published)
- Time series chart of submission trends (last 30 days)
- Plugin performance metrics table

### ⏱️ Phase Timeline Tracking
- **Timeline Tab**: Detailed phase-by-phase visualization
- Visual timeline showing progress through each stage
- Automatic detection of state transitions
- Duration tracking for each phase
- Complete transition history

### 🔔 Notifications & Alerts
- Push notifications when plugins pass verification
- Alerts for rejected submissions
- Error notifications for API failures
- Configurable notification types (Balloon, Sticky, None)

### ⚙️ Settings Page
- Configure JetBrains Marketplace API token
- Add/remove plugins to track
- Adjust polling interval (1 min - 2 hours)
- Notification preferences
- Data retention settings
- Export/import configuration

## 🚀 Getting Started

### Installation

1. **Build the Plugin:**
   ```bash
   ./gradlew build
   ```

2. **Install in IntelliJ:**
   - Go to `Settings/Preferences` → `Plugins`
   - Click gear icon → `Install Plugin from Disk...`
   - Select the generated JAR from `build/distributions/`

### Configuration

1. **Get your API Token:**
   - Visit [JetBrains Marketplace](https://plugins.jetbrains.com/)
   - Go to your account settings
   - Generate an API token

2. **Configure the Plugin:**
   - Open `Settings/Preferences` → `Tools` → `Publish Tracker Settings`
   - Enter your API token
   - Add plugin IDs you want to track (e.g., `12345`, `67890`)
   - Configure polling interval and notification preferences

3. **Use the Tool Window:**
   - Open `View` → `Tool Windows` → `Publish Tracker`
   - Click **Refresh Now** to fetch latest status
   - Enable **Auto Poll** for automatic updates
   - Switch between tabs to view different insights

## 📊 Understanding the Data

### Verification Stages

The plugin tracks your submission through these stages:

1. **SUBMITTED** - Plugin version submitted to marketplace (manual tracking)
2. **UNDER_REVIEW** - JetBrains team is reviewing (inferred when not in API)
3. **APPROVED** - Review complete, approved for publication (`approve=true, listed=false`)
4. **PUBLISHED** - Live on the marketplace (`approve=true, listed=true`)
5. **REJECTED** - Submission rejected (`approve=false`)

> **⚠️ Important**: The JetBrains Marketplace API **only returns approved/published plugins**. Plugins currently in review or pending approval are not returned by the API. The plugin detects when a new version first appears in the API response as an "approval" event.

### Phase Durations

The plugin calculates and displays:
- **Upload to Verification**: Time waiting for review to start
- **Verification to Approval**: Time spent in review
- **Approval to Published**: Time to go live after approval
- **Total (Upload to Published)**: Complete end-to-end duration

### How Tracking Works

Since the API only returns approved plugins, the plugin uses this strategy:

1. **Polling**: Continuously checks the API at configured intervals
2. **Appearance Detection**: When a new version appears in API = approval detected
3. **State Transitions**: Tracks when `listed` flag changes = publication detected
4. **Duration Calculation**: Time between first appearance and state changes

> **💡 Tip**: For accurate verification time tracking, enable auto-polling **before** submitting a new version. The plugin will detect when your submission is approved by noticing the new version appear in the API.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│                   UI Layer                      │
│  - PluginStatusToolWindowFactory (3 tabs)      │
│  - TimelinePanel (phase visualization)         │
│  - ChartsPanel (analytics with JFreeChart)     │
│  - SettingsPanel (configuration)               │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│                 Domain Layer                    │
│  - AnalyticsCalculator (metrics)               │
│  - VerificationStage (state machine)           │
│  - PhaseTransition (state tracking)            │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│                  Data Layer                     │
│  - Repository: Fetches plugin status           │
│  - MarketplaceApi: HTTP client (Ktor)          │
│  - Storage: Persistent state (XML-based)       │
│  - StatusFetcher: Background worker            │
└─────────────────────────────────────────────────┘
```

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9.0
- **Platform**: IntelliJ Platform SDK 2023.3
- **HTTP Client**: Ktor 3.0.0 with OkHttp engine
- **Serialization**: kotlinx-serialization-json 1.6.0
- **Charts**: JFreeChart 1.5.4
- **Concurrency**: Kotlin Coroutines 1.7.3
- **UI**: Swing (IntelliJ UI components)
- **Build**: Gradle 8.x with Kotlin DSL
- **JVM**: Java 17

## 📦 Dependencies

```kotlin
implementation("io.ktor:ktor-client-okhttp:3.0.0")
implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
implementation("org.jfree:jfreechart:1.5.4")
```

## 🔒 Privacy & Security

- API tokens are stored securely using IntelliJ's persistent storage
- Tokens are never logged or transmitted except to JetBrains Marketplace API
- All data is stored locally in XML format
- No external services or analytics tracking

## 🗂️ Data Storage

The plugin stores data in:
- **Location**: `~/.config/JetBrains/{IDE}/options/publish-tracker.xml`
- **Contents**: 
  - API token (encrypted by IDE)
  - Tracked plugin IDs
  - Historical status checks
  - Phase transitions
  - Settings/preferences

## 📝 Development

### Project Structure

```
src/main/java/com/shrikantbadwaik/publish.time.tracker/
├── data/
│   ├── api/
│   │   ├── MarketplaceApi.kt          # API client
│   │   └── di/MarketplaceApiModule.kt # DI module
│   ├── repo/
│   │   ├── Repository.kt              # Data repository
│   │   ├── worker/StatusFetcher.kt    # Background tasks
│   │   └── di/RepoModule.kt           # DI module
│   ├── DataModels.kt                  # Data classes
│   └── Storage.kt                     # Persistence layer
├── domain/
│   └── AnalyticsCalculator.kt         # Business logic
├── ui/
│   ├── PluginStatusToolWindowFactory.kt # Main tool window
│   ├── TimelinePanel.kt               # Timeline visualization
│   ├── ChartsPanel.kt                 # Analytics charts
│   └── SettingsPanel.kt               # Settings UI
└── PluginEntry.kt                     # Entry point
```

### Running Tests

```bash
./gradlew test
```

### Building Distribution

```bash
./gradlew buildPlugin
```

Output: `build/distributions/publish-time-tracker-{version}.zip`

## 🐛 Troubleshooting

### Plugin not fetching data
- Verify API token in Settings
- Check if plugin IDs are correct
- Look for error notifications
- Check IDE logs: `Help` → `Show Log in Finder/Explorer`

### Charts not displaying
- Ensure you have historical data (requires multiple fetches)
- Try refreshing analytics manually
- Check for state transitions in Timeline tab

### Auto-polling not working
- Verify polling is started (green indicator)
- Check polling interval in Settings
- Restart IDE if scheduler is stuck

## 🤝 Contributing

Contributions are welcome! Areas for improvement:
- Additional chart types (pie charts, stacked bars)
- Email notification integration
- Export data to CSV/JSON
- Custom alert rules
- SQLite backend for better query performance
- Multi-IDE support (Android Studio, etc.)

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

## 👤 Author

**Shrikant Badwaik**
- Email: shrikant.badwaik@gmail.com
- Plugin: [JetBrains Publish Tracker](https://plugins.jetbrains.com/)

## 🙏 Acknowledgments

- JetBrains for the excellent Platform SDK
- Ktor team for the HTTP client
- JFreeChart for visualization library
- Kotlin community for coroutines and serialization

## 📊 Version History

### v0.1.0 (Initial Release)
- ✅ Marketplace API integration with authentication
- ✅ Real-time status tracking
- ✅ Phase-by-phase timeline visualization
- ✅ Visual analytics charts
- ✅ Notification system
- ✅ Configurable settings
- ✅ Auto-polling capability
- ✅ Historical data tracking

---

**Made with ❤️ for JetBrains Plugin Developers**
