# Contributing to JB Publish Tracker

Thank you for your interest in contributing! This document provides guidelines and instructions for contributing to this project.

## 🔒 Branch Protection Rules

To maintain code quality and prevent bad code from reaching `main`/`master`, we enforce the following rules:

### Required Checks (must pass before merging)

1. **Build Success** - Project must build without errors
2. **All Tests Pass** - Unit tests must pass with 100% success rate
3. **Detekt Analysis** - No code quality violations allowed
4. **Code Review** - At least 1 approval required from maintainers

### Branch Naming Convention

Use descriptive branch names following this pattern:
- `feature/description` - New features
- `bugfix/description` - Bug fixes
- `hotfix/description` - Critical fixes for production
- `refactor/description` - Code refactoring
- `docs/description` - Documentation updates

Examples:
- `feature/add-email-notifications`
- `bugfix/fix-api-polling-crash`
- `refactor/improve-analytics-performance`

## 🚀 Development Workflow

### 1. Setting Up Your Environment

```bash
# Clone the repository
git clone https://github.com/yourusername/jb-publish-tracker.git
cd jb-publish-tracker

# Create a new branch
git checkout -b feature/your-feature-name

# Build the project
./gradlew build
```

### 2. Making Changes

1. Write your code following Kotlin coding conventions
2. Add or update tests for your changes
3. Run local verification before committing:

```bash
# Run all verification checks
./gradlew verify

# Or run individual checks:
./gradlew test          # Run unit tests
./gradlew detekt        # Run code quality checks
./gradlew build         # Build the project
```

### 3. Code Quality Standards

#### Detekt Rules
We use Detekt for static code analysis. The configuration is in `config/detekt/detekt-config.yml`.

Common violations to avoid:
- Long methods (> 60 lines)
- Long parameter lists (> 6 parameters)
- Complex conditions (> 4 conditions)
- Magic numbers (use constants)
- Empty catch blocks
- Unused imports

#### Testing Requirements
- **Unit tests** are required for all new features
- Aim for **80%+ code coverage**
- Tests should be:
  - Fast (no external dependencies)
  - Isolated (no shared state)
  - Repeatable (same result every time)
  - Self-validating (clear pass/fail)

#### Code Style
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions focused and single-purpose
- Prefer immutability (use `val` over `var`)
- Use Kotlin idioms (e.g., `apply`, `let`, `run`)

### 4. Committing Changes

Write clear, descriptive commit messages:

```bash
# Good commit messages
git commit -m "feat: Add email notification support"
git commit -m "fix: Resolve API polling crash on network error"
git commit -m "refactor: Extract analytics calculations to separate class"
git commit -m "docs: Update README with installation instructions"

# Follow conventional commits format
# type(scope): description
# 
# Types: feat, fix, docs, style, refactor, test, chore
```

### 5. Creating a Pull Request

1. Push your branch to GitHub:
```bash
git push origin feature/your-feature-name
```

2. Create a PR using the provided template
3. Fill out all sections of the PR template
4. Link any related issues
5. Wait for CI/CD checks to complete
6. Request review from maintainers

### 6. Code Review Process

- All PRs require at least **1 approval**
- Address review comments promptly
- Keep PRs focused and reasonably sized
- Be responsive to feedback
- CI/CD checks must pass before merging

## 🔍 CI/CD Pipeline

Our CI/CD pipeline runs automatically on every PR and push to main branches.

### Pipeline Stages

1. **Build and Verify**
   - Builds the project
   - Runs all unit tests
   - Executes Detekt analysis
   - Uploads test results and reports

2. **Code Quality Checks**
   - Verifies code formatting
   - Checks plugin configuration
   - Validates dependencies

3. **Dependency Check**
   - Scans for vulnerable dependencies
   - Generates dependency list

4. **Build Plugin** (main/master only)
   - Creates distribution ZIP
   - Uploads artifacts
   - Creates GitHub releases (on tags)

### Viewing CI/CD Results

- Check the **Actions** tab in GitHub for pipeline status
- Test results are attached as artifacts
- Detekt reports show code quality issues
- SARIF reports are uploaded to GitHub Security tab

### Local CI/CD Simulation

Before pushing, you can run the same checks locally:

```bash
# Run the complete verification suite
./gradlew verify

# This runs:
# - build (compilation)
# - test (unit tests)
# - detekt (code quality)
```

## 📋 Testing Guidelines

### Writing Unit Tests

```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MyComponentTest {
    
    @Test
    fun `test descriptive name of what is being tested`() {
        // Arrange
        val input = "test"
        
        // Act
        val result = myFunction(input)
        
        // Assert
        assertEquals("expected", result)
    }
}
```

### Test Organization
- Place tests in `src/test/java` mirroring the source structure
- Use descriptive test names with backticks
- Group related tests in the same class
- Use `@BeforeEach` and `@AfterEach` for setup/teardown

### Mocking
We use Mockito for mocking dependencies:

```kotlin
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

val mockApi = mock<MarketplaceApi>()
whenever(mockApi.fetchUpdates("123")).thenReturn(mockResponse)
```

## 🐛 Reporting Issues

When reporting issues, please include:
1. Clear description of the problem
2. Steps to reproduce
3. Expected vs actual behavior
4. IntelliJ IDEA version
5. Plugin version
6. Relevant logs or screenshots

## 🎯 Code Review Checklist

For reviewers, ensure:
- [ ] Code follows project conventions
- [ ] Tests are comprehensive and meaningful
- [ ] Documentation is updated
- [ ] No obvious bugs or security issues
- [ ] Performance implications considered
- [ ] Error handling is appropriate
- [ ] Code is maintainable and readable

## 📚 Additional Resources

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Detekt Rules](https://detekt.dev/docs/intro)
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)

## 🤝 Getting Help

- Open an issue for bugs or feature requests
- Start a discussion for questions or ideas
- Tag maintainers for urgent matters

## 📄 License

By contributing, you agree that your contributions will be licensed under the same license as the project (see LICENSE file).

---

Thank you for contributing to make JB Publish Tracker better! 🚀

