# 📦 Publishing Guide: JetBrains Plugin Publish Tracker

This guide explains how to publish the plugin to the JetBrains Marketplace.

---

## 🎯 Prerequisites

Before publishing, ensure you have:

### 1. JetBrains Marketplace Account
- Sign up at [JetBrains Marketplace](https://plugins.jetbrains.com/)
- Verify your email address

### 2. Generate Marketplace API Token
1. Go to [https://plugins.jetbrains.com/author/me/tokens](https://plugins.jetbrains.com/author/me/tokens)
2. Click **"Generate New Token"**
3. Give it a descriptive name (e.g., "Publishing Token")
4. Copy the token (you won't see it again!)
5. Store it securely

### 3. Set Up GitHub Secret
1. Go to your GitHub repository
2. Navigate to **Settings → Secrets and variables → Actions**
3. Click **"New repository secret"**
4. Name: `JETBRAINS_MARKETPLACE_TOKEN`
5. Value: Paste your JetBrains token
6. Click **"Add secret"**

---

## 🚀 Publishing Methods

You have **3 ways** to publish your plugin:

### Method 1: Automated GitHub Actions (Recommended) ⭐

This is the easiest and most reliable method.

#### For Stable Release:
```bash
# Commit all changes
git add .
git commit -m "Release v1.0.0"
git push origin main

# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

The GitHub Actions workflow will automatically:
1. ✅ Run all tests
2. ✅ Execute Detekt code quality checks
3. ✅ Build the plugin
4. ✅ Publish to JetBrains Marketplace
5. ✅ Create a GitHub Release with artifacts

#### For Beta/EAP Release:
```bash
# For beta
git tag v1.0.0-beta.1
git push origin v1.0.0-beta.1

# For EAP (Early Access Program)
git tag v1.0.0-eap.1
git push origin v1.0.0-eap.1
```

#### Manual Trigger (Testing):
1. Go to GitHub → **Actions** → **Publish Plugin to JetBrains Marketplace**
2. Click **"Run workflow"**
3. Select branch and channel (default/beta/eap)
4. Click **"Run workflow"**

---

### Method 2: Local Publishing via Gradle

For testing or one-off releases:

#### Setup Local Token:
Create `~/.gradle/gradle.properties` (or use environment variable):
```properties
jetbrainsMarketplaceToken=YOUR_TOKEN_HERE
```

Or export as environment variable:
```bash
export JETBRAINS_MARKETPLACE_TOKEN="your_token_here"
```

#### Publish Commands:
```bash
# Verify everything works
./gradlew verifyPluginConfiguration

# Build the plugin
./gradlew buildPlugin

# Publish to stable channel
./gradlew publishPlugin

# Publish to beta channel
./gradlew publishPlugin -Pchannel=beta

# Publish to EAP channel
./gradlew publishPlugin -Pchannel=eap
```

**Important:** The plugin ZIP will be in `build/distributions/`

---

### Method 3: Manual Upload (Fallback)

If automated methods fail:

1. **Build the plugin locally:**
   ```bash
   ./gradlew buildPlugin
   ```

2. **Find the ZIP file:**
   - Location: `build/distributions/publish-time-tracker-1.0.0.zip`

3. **Upload manually:**
   - Go to [JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/add)
   - Click **"Upload plugin"**
   - Select your ZIP file
   - Fill in the required information
   - Submit for review

---

## 📋 Pre-Publishing Checklist

Before publishing a new version, verify:

### Code Quality ✅
- [ ] All tests pass: `./gradlew test`
- [ ] Detekt passes: `./gradlew detekt`
- [ ] Build succeeds: `./gradlew build`
- [ ] Plugin configuration valid: `./gradlew verifyPluginConfiguration`

### Version & Documentation ✅
- [ ] Updated version in `build.gradle.kts`
- [ ] Updated `CHANGELOG.md` with new features/fixes
- [ ] Updated `README.md` if needed
- [ ] Reviewed `plugin.xml` description and change notes
- [ ] All PRs merged to `main`

### Testing ✅
- [ ] Tested in IntelliJ IDEA sandbox
- [ ] Verified all features work as expected
- [ ] Checked compatibility with target IDE versions
- [ ] No critical bugs or issues

### Legal & Compliance ✅
- [ ] License file present (Apache 2.0)
- [ ] No proprietary or sensitive code
- [ ] Dependencies are properly licensed
- [ ] Attribution for third-party libraries

---

## 🔄 Release Process (Recommended Workflow)

### 1. Prepare Release
```bash
# Ensure you're on main and up-to-date
git checkout main
git pull origin main

# Update version in build.gradle.kts
# version = "1.0.0"

# Update CHANGELOG.md with release notes

# Commit changes
git add .
git commit -m "chore: prepare release v1.0.0"
git push origin main
```

### 2. Create Release Tag
```bash
# Create annotated tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push tag (triggers automatic publishing)
git push origin v1.0.0
```

### 3. Monitor GitHub Actions
1. Go to GitHub → **Actions**
2. Watch the **Publish Plugin to JetBrains Marketplace** workflow
3. Verify all jobs pass:
   - ✅ Validate Before Publishing
   - ✅ Publish to Marketplace
   - ✅ Verify Plugin on Marketplace

### 4. Verify on Marketplace
1. Wait ~5-10 minutes for JetBrains to process
2. Visit: https://plugins.jetbrains.com/plugin/com.shrikantbadwaik.jb-publish-tracker
3. Verify the new version is listed
4. Check that description and screenshots are correct

### 5. Announce Release (Optional)
- Share on social media
- Post in relevant forums/communities
- Update project documentation
- Notify users via GitHub Discussions/Issues

---

## 🎨 Marketplace Listing Enhancement

### Adding Screenshots
1. Take high-quality screenshots (1280x800px recommended)
2. Upload via [Plugin Management](https://plugins.jetbrains.com/author/me/plugins)
3. Click your plugin → **Edit** → **Screenshots**

### Adding Icon
1. Create a 40x40px icon (SVG or PNG)
2. Place in `src/main/resources/META-INF/pluginIcon.svg`
3. Will be automatically included in next release

### Adding Logo
1. Create a 200x200px logo
2. Upload via Plugin Management → **Edit** → **Logo**

### Categories and Tags
1. Go to Plugin Management → **Edit**
2. Select appropriate categories
3. Add relevant tags for discoverability

---

## 🐛 Troubleshooting Publishing Issues

### Issue: "Token is invalid"
**Solution:**
1. Regenerate token at https://plugins.jetbrains.com/author/me/tokens
2. Update GitHub secret `JETBRAINS_MARKETPLACE_TOKEN`
3. Try publishing again

### Issue: "Plugin already exists with this version"
**Solution:**
1. Increment version in `build.gradle.kts`
2. Create new tag with updated version
3. You cannot re-publish the same version

### Issue: "Verification failed"
**Solution:**
1. Check `verifyPluginConfiguration` output
2. Ensure `sinceBuild` and `untilBuild` are valid
3. Verify plugin.xml is well-formed

### Issue: "Build failed on CI"
**Solution:**
1. Check GitHub Actions logs
2. Run `./gradlew build` locally to reproduce
3. Fix errors and commit changes
4. Push again to trigger re-run

### Issue: "Plugin not visible on marketplace"
**Solution:**
- Wait 5-10 minutes for processing
- Check for email from JetBrains (approval/rejection)
- First-time submissions may require manual approval
- Check spam folder for JetBrains emails

---

## 🔢 Versioning Strategy

Follow [Semantic Versioning](https://semver.org/):

### Version Format: `MAJOR.MINOR.PATCH`

- **MAJOR** (1.x.x): Breaking changes, incompatible API changes
- **MINOR** (x.1.x): New features, backward-compatible
- **PATCH** (x.x.1): Bug fixes, backward-compatible

### Examples:
```
1.0.0   - Initial release
1.0.1   - Bug fix
1.1.0   - New feature added
2.0.0   - Breaking change
```

### Pre-release Versions:
```
1.0.0-beta.1   - Beta release
1.0.0-eap.1    - Early Access Program
1.0.0-rc.1     - Release Candidate
```

---

## 📊 Post-Publishing Monitoring

### Check Marketplace Statistics
1. Go to [Plugin Management](https://plugins.jetbrains.com/author/me/plugins)
2. View downloads, ratings, and reviews
3. Monitor for issues reported by users

### Respond to Reviews
- Reply to user reviews (both positive and negative)
- Address concerns and provide support
- Thank users for feedback

### Track Issues
- Monitor GitHub Issues for bug reports
- Link marketplace feedback to GitHub Issues
- Prioritize critical bugs for patch releases

---

## 🎯 Release Cadence Recommendations

### Patch Releases (x.x.1)
- **Frequency:** As needed for critical bugs
- **Timeline:** Within 24-48 hours of bug discovery

### Minor Releases (x.1.0)
- **Frequency:** Monthly or when significant features are ready
- **Timeline:** 2-4 weeks development + 1 week testing

### Major Releases (2.0.0)
- **Frequency:** Annually or when major changes are needed
- **Timeline:** 2-3 months development + 2 weeks beta testing

---

## 📝 Release Notes Template

Use this template for `CHANGELOG.md`:

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added
- New feature descriptions

### Changed
- Modified functionality descriptions

### Fixed
- Bug fix descriptions

### Deprecated
- Features planned for removal

### Removed
- Removed features

### Security
- Security vulnerability fixes
```

---

## 🔗 Useful Links

- **JetBrains Marketplace:** https://plugins.jetbrains.com/
- **Plugin Portal:** https://plugins.jetbrains.com/author/me/plugins
- **API Tokens:** https://plugins.jetbrains.com/author/me/tokens
- **Publishing Docs:** https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html
- **Marketplace Guidelines:** https://plugins.jetbrains.com/docs/marketplace/plugin-overview-page.html

---

## 🆘 Getting Help

If you encounter issues:

1. **Check CI Logs:** GitHub Actions provides detailed error messages
2. **Review Gradle Output:** Run with `--stacktrace` for details
3. **Consult JetBrains Docs:** [IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/)
4. **Community Support:** [JetBrains Platform Slack](https://plugins.jetbrains.com/slack)
5. **Create Issue:** File a bug report in this repository

---

## 🎉 First Time Publishing?

Congratulations! Here's what to expect:

1. **Initial Review:** First plugin submissions may take 1-3 business days for manual review
2. **Approval Email:** You'll receive an email when approved
3. **Listing Goes Live:** Plugin becomes searchable on marketplace
4. **Updates:** Future updates are typically approved automatically

**Good luck with your plugin! 🚀**

