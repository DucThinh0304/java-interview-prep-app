
You are helping me build an Android app from scratch. Work in small, verifiable steps and summarize what you changed after each step.

PRODUCT
- "Java Interview Prep": a minimalist, developer-native app for Java interview preparation. Application ID: io.github.ducthinh0304.javainterviewprep.
- Core loop: one daily question (chosen by date, offline), quick practice sessions, 1-minute mini lessons followed by a short quiz, and a spaced-repetition "Re-run" queue for missed questions.
- Gamification is light and dev-flavored: XP, a combo multiplier for consecutive correct answers, a daily streak, and a level ladder SNAPSHOT -> ALPHA -> BETA -> RC -> GA -> LTS. NO hearts, no cartoon mascots.
- Fully offline. Content is bundled JSON. No accounts or backend in v1.

ARCHITECTURE GOALS
- Reusable engine + separate content modules + thin app modules, so I can later ship Spring Boot / Linux / Kotlin apps from the same engine with different content AND a different theme.
- Design tokens (colors, type, shape, spacing, copy tone) must live in a swappable "theme pack" so each app can have its own identity.

TECH STACK
- Kotlin, Jetpack Compose, single activity, Navigation Compose
- MVVM + unidirectional data flow (ViewModel + StateFlow)
- Hilt, Room, DataStore, kotlinx.serialization, Coroutines/Flow
- Gradle Kotlin DSL + version catalog
- Tests: JUnit, MockK, Turbine, Compose UI tests
- Use the latest STABLE versions. Check official docs rather than guessing versions. Target the API level Google Play currently requires.

DESIGN RULES (non-negotiable)
- Do NOT use the default Material 3 look: no default purple/blue color scheme, no default elevation shadows, no default pill nav indicator, no default FAB, no default ripple.
- Do NOT use gradients as decoration, emoji as icons, stock illustrations, or soft rounded-2xl "card with shadow" layouts.
- Minimal means: few colors, strong typography, generous spacing, hairline borders instead of shadows, motion only when it carries meaning.
- All visual values come from design tokens. No hard-coded colors, sizes or fonts inside screens.
- When unsure about a design decision, ask me or propose 2 options. Don't silently fall back to defaults.

RULES
- Simple, readable code. No unused dependencies. Verify any new library's maintenance status (recent releases, open issues) before adding it.
- Every step must compile and run before you stop. Unit tests for all non-UI logic.
- Original content only. No copying questions, explanations or UI copy from other apps or websites.
- Ask before big architectural changes. Keep CHANGELOG.md with one line per completed step.
- Never create a git commit after finishing work, even if the step compiles and tests pass. Run the relevant tests, report the results, and leave the changes uncommitted so I can review and commit myself.