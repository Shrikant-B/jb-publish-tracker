# Changelog

All notable changes to the JetBrains Plugin Publish Tracker will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-10-14

### 🎉 Initial Release

#### Added
- **Real-time Status Tracking**: Monitor JetBrains Marketplace plugin verification status directly from IntelliJ IDEA
- **Phase-by-Phase Timeline**: Track verification phases (Upload → Review → Approved → Published) with detailed timestamps
- **Visual Analytics Dashboard**: 
  - Interactive charts showing submission trends over time
  - Average phase duration visualizations
  - Success rate metrics
- **Smart Notifications**: Configurable notification system with multiple alert types
  - Status change notifications
  - Approval/rejection alerts
  - Error notifications
- **Auto-polling**: Configurable automatic status checks (1-60 minutes interval)
- **Multi-plugin Support**: Track multiple plugins simultaneously from one dashboard
- **Historical Data**: Persistent storage of all plugin submissions and verification history
- **Settings Panel**: Comprehensive configuration UI for:
  - API token management
  - Plugin ID tracking
  - Polling intervals
  - Notification preferences
  - Data retention policies
- **Tool Window**: Dedicated UI with three tabs:
  - Status Dashboard: Real-time status of all tracked plugins
  - Analytics: Charts and trends visualization
  - Timeline: Detailed phase-by-phase breakdown
- **CI/CD Pipeline**: 
  - Automated build and test workflows
  - Code quality checks with Detekt
  - Dependency vulnerability scanning
  - Automated test reporting

#### Technical Features
- Built with Kotlin and Ktor HTTP client
- JFreeChart integration for data visualization
- Kotlin Coroutines for async operations
- kotlinx-serialization for JSON handling
- Persistent state management using IntelliJ Platform APIs
- Compatible with IntelliJ IDEA 2023.3+

#### Documentation
- Comprehensive README with setup instructions
- CI/CD setup documentation
- Contributing guidelines
- Code of Conduct
- Apache 2.0 License

### Requirements
- IntelliJ IDEA 2023.3 or later
- JetBrains Marketplace API token (get from https://plugins.jetbrains.com/author/me/tokens)

### Known Limitations
- API only returns approved/published plugins (pending plugins not visible via public API)
- Phase transitions are inferred from status changes
- Historical data limited to approved/published versions

---

## [Unreleased]

### Planned Features
- Email notifications for status changes
- Export/Import functionality for settings
- Advanced filtering and search in history
- Custom alert rules and conditions
- Integration with CI/CD pipelines for automated checks
- Support for beta/EAP channel tracking
- Webhook notifications
- Slack/Discord integration

---

For detailed information about each release, visit the [GitHub Releases](https://github.com/shrikantbadwaik/jb-publish-tracker/releases) page.

[1.0.0]: https://github.com/shrikantbadwaik/jb-publish-tracker/releases/tag/v1.0.0

