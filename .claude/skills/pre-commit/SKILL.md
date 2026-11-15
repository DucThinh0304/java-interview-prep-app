---
name: pre-commit
description: Run before suggesting a commit message, or when the user mentions IDE/Android Studio warnings, code inspection results, lint, or a crash after a Room schema change. Runs tests, Kotlin compiler warnings and Android Lint, triages an exported inspection report, and smoke-tests the app.
---

# Pre-commit check

1. Run `.claude/skills/pre-commit/check.sh` (about 25 seconds). Pass an inspection export dir if the user mentions IDE warnings (see below).
2. Fix every `ACTION` item in code touched this step, following the triage rules below. Re-run until it prints `== CLEAN`.
3. If the step changed app behaviour, a Room entity, or Hilt wiring, smoke test it (see below).
4. Report results, then suggest a Conventional Commits message (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`).
   Never commit.

## Where warnings come from

| Source | How to get it | Notes |
|---|---|---|
| Kotlin compiler | `check.sh` (forced recompile) | Incremental builds hide warnings; that's why it uses `--rerun-tasks`. |
| Android Lint | `check.sh` (`lintDebug`) | Reports in `*/build/reports/lint-results-debug.txt`. |
| Android Studio inspections | Only inside the IDE | The commit dialog's "N warnings" are these. They can't be run headless while the IDE is open. |

For IDE warnings, ask the user to: **Code → Inspect Code… → scope "Uncommitted files" → Analyze**, then
**Export → XML** to `~/inspections`. Then run `check.sh ~/inspections`.
The export is a snapshot: after fixing, ask for a fresh export before calling it clean, and don't re-fix items that are already fixed.
If the commit dialog's Review button doesn't work: Sync Project with Gradle Files (needed after
`build.gradle.kts`/`libs.versions.toml` changes), wait for indexing, else File → Invalidate Caches → Restart.

## Triage: ignore (the script already marks these `ignore`)

- `UnusedSymbol` in `db/Converters.kt`: Room's generated code calls the `@TypeConverter`s.
- `UnusedSymbol` in `di/*Module.kt` (the module object and its `@Provides`): Hilt's generated code calls them.
- `UnusedSymbol` on API kept on purpose for the upcoming notes/stats screens:
  `ProgressRepository.questionStat/noteProgress/recordNoteRead/recordNoteQuizResult`, `StatsRepository.dailyActivity`.
  Once a screen uses them, remove them here and from `IGNORE` in `check.sh`.
- Grazie grammar/style on `.agents/*.md` or `.claude/**/*.md`, identifiers (`github` in the app ID) or British spelling.
- Lint `Library lint checks reference invalid APIs` (lifecycle-runtime jar): comes from a dependency, not our code.

New false positive? Add it here **and** to `IGNORE` in `check.sh`, in the same edit.

## Triage: fix

- `UnusedSymbol` on our own code: delete it.
- `KDocUnresolvedReference`: `[X]` in KDoc is a link. Use backticks for code that isn't a symbol: `` `ARRAY[n]` ``.
- Grazie on a code comment: reword the comment.
- `NewerVersionAvailable` / `GradleDependency`: **never** bundle into a feature commit. Propose a separate
  `chore(deps)` step, check each library's release notes, and bump the Kotlin, KSP and Compose plugins together.

## Smoke test on the emulator

In-memory unit tests can't catch schema-identity crashes or missing Hilt bindings, so for those changes use the device:

```bash
A=~/Android/Sdk/platform-tools/adb   # not on PATH
./gradlew :app:installDebug -q --offline          # install OVER the previous build; don't uninstall first
$A logcat -c
$A shell monkey -p io.github.ducthinh0304.javainterviewprep -c android.intent.category.LAUNCHER 1
sleep 6; $A shell pidof io.github.ducthinh0304.javainterviewprep   # empty = crashed
$A logcat -d -b crash | grep -A20 'FATAL EXCEPTION'
```

`Room cannot verify the data integrity` means an entity changed without bumping `EngineDatabase.version`.
