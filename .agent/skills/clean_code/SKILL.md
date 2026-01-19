---
name: Clean Code Submission
description: Rules for ensuring code quality before submission.
---

# Clean Code Submission

Perform these checks before finishing a task.

## 1. Kotlin Code Style
- Follow official Kotlin Coding Conventions.
- Use explicit types for public API boundaries.
- Use `val` over `var` wherever possible.

## 2. Remove Detritus
- **Imports**: Remove all unused imports.
- **Comments**: Remove commented-out code blocks. Documentation comments (`/** ... */`) should remain.
- **Logging**: Remove temporary debugging logs (`Log.d`) unless they are vital for production diagnostics (use `Timber` or controlled wrappers if available).

## 3. String Resources
- Do not hardcode strings in Composables.
- Extract user-visible strings to `app/src/main/res/values/strings.xml`.
- Use `stringResource(R.string.id)` in Compose.

## 4. Final Verification
- Re-run the app to ensure no regressions.
- Verify the feature works as intended in specific scenarios (e.g., Light Mode vs Dark Mode/Industrial Mode).
