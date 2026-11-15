#!/usr/bin/env bash
# Pre-commit check. Run before suggesting a commit message.
#   .claude/skills/pre-commit/check.sh                -> unit tests + Kotlin compiler warnings + Android Lint
#   .claude/skills/pre-commit/check.sh ~/inspections  -> also triage an Android Studio inspection export (XML)
# Exit code is non-zero if tests fail or anything needs action. Triage rules: SKILL.md next to this script
set -uo pipefail
cd "$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
status=0

echo "== Unit tests"
./gradlew testDebugUnitTest :core-model:test -q --offline 2>&1 | grep -v '^OpenJDK' || true
[ "${PIPESTATUS[0]}" -eq 0 ] && echo "passed" || { echo "FAIL: unit tests"; status=1; }

echo "== Kotlin compiler warnings (main + test, forced recompile)"
# ponytail: --rerun-tasks recompiles everything (~1 min); warnings are only printed when a task actually runs.
warnings=$(./gradlew compileDebugKotlin compileDebugUnitTestKotlin :core-model:compileKotlin :core-model:compileTestKotlin \
    --rerun-tasks -q --offline 2>&1 | grep '^w: ' | sed "s|file://$PWD/||" | sort -u)
if [ -n "$warnings" ]; then echo "$warnings"; status=1; else echo "none"; fi

echo "== Android Lint"
./gradlew lintDebug -q --offline >/dev/null 2>&1 || { echo "FAIL: lint task"; status=1; }
# The lifecycle-runtime 'Library lint checks reference invalid APIs' line comes from a dependency's jar, not our code.
issues=$(grep -hE ': (Warning|Error): ' */build/reports/lint-results-debug.txt 2>/dev/null | grep -v 'Library lint checks reference invalid APIs')
if [ -n "$issues" ]; then echo "$issues"; status=1; else echo "none"; fi

if [ $# -ge 1 ]; then
    echo "== IDE inspection export: $1"
    python3 -I - "$1" <<'EOF' || status=1
import glob, os, re, sys
import xml.etree.ElementTree as ET

# (inspection, regex over "<path> <description>", reason) -> known false positive / not for this commit. Keep in sync with SKILL.md.
IGNORE = [
    ("UnusedSymbol", r"/db/Converters\.kt ", "called by Room-generated code"),
    ("UnusedSymbol", r"/di/[^/]+Module\.kt ", "called by Hilt-generated code"),
    ("AndroidLintNewerVersionAvailable", r"", "dependency bump: separate chore(deps) step"),
    ("AndroidLintGradleDependency", r"", "dependency bump: separate chore(deps) step"),
    ("GrazieInspection", r"\.agents/", "prose in agent docs"),
    ("GrazieStyle", r"", "style suggestion only"),
    ("UnusedSymbol", r"Repository\.kt .*\b(questionStat|noteProgress|recordNoteRead|recordNoteQuizResult|dailyActivity)\b",
     "kept on purpose for notes/stats screens"),
]
action = 0
for f in sorted(glob.glob(os.path.join(sys.argv[1], "*.xml"))):
    name = os.path.basename(f)[:-4]
    if name.startswith("."):
        continue
    for p in ET.parse(f).getroot().iter("problem"):
        path = (p.findtext("file") or "").replace("file://$PROJECT_DIR$/", "")
        where = f"{path}:{p.findtext('line')}"
        desc = re.sub(r"<[^>]+>", "", p.findtext("description") or "").strip()
        why = next((r for i, rx, r in IGNORE if i == name and re.search(rx, f"{path} {desc}")), None)
        if why:
            print(f"  ignore  {name} {where} ({why})")
        else:
            action += 1
            print(f"  ACTION  {name} {where}: {desc}")
print(f"{action} item(s) need action")
sys.exit(1 if action else 0)
EOF
fi

[ $status -eq 0 ] && echo "== CLEAN" || echo "== NEEDS ATTENTION"
exit $status
