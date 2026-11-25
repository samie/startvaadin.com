#!/usr/bin/env bash
# Test gate for the UI templates. For each template id:
#   1. generate the project through the UI generation path and run `mvnw verify`
#      on it (compiles, runs the template's browserless tests, builds the jar),
#   2. start the jar on a free port,
#   3. run the template's Playwright smoke spec, which also takes a screenshot to
#      target/example-projects/<id>.png. With --update-screenshots it replaces the
#      catalog image in src/main/resources/META-INF/resources/images/examples/ instead.
#      The light/dark and phone review shots (<id>-light.png, <id>-dark.png,
#      <id>-phone.png, <id>-phone-dark.png) always go to target/example-projects/.
#
# Usage: check.sh <id>[,<id>...]|all [--skip-build] [--update-screenshots]
set -euo pipefail

cd "$(dirname "$0")"
EXAMPLES_DIR=$(pwd)
REPO_ROOT=$(cd ../../.. && pwd)
OUTPUT="$REPO_ROOT/start-site/target/example-projects"

ids=${1:?usage: check.sh <id>[,<id>...]|all [--skip-build] [--update-screenshots]}
shift
skip_build=false
export SCREENSHOT_DIR="$OUTPUT"
export REVIEW_SCREENSHOT_DIR="$OUTPUT"
for arg in "$@"; do
  case "$arg" in
    --skip-build) skip_build=true ;;
    --update-screenshots) SCREENSHOT_DIR="$REPO_ROOT/start-site/src/main/resources/META-INF/resources/images/examples" ;;
    *) echo "unknown option: $arg" >&2; exit 2 ;;
  esac
done
unset JAVA_TOOL_OPTIONS
mkdir -p "$OUTPUT"

if [[ "$ids" == "all" ]]; then
  ids=$(ls "$REPO_ROOT/start-site/src/main/resources/examples" | paste -sd, -)
fi

if [[ "$skip_build" == false ]]; then
  for id in ${ids//,/ }; do
    python3 "$EXAMPLES_DIR/sync-back.py" "$id" --check || exit 1
  done
  echo "==> Generating and verifying: $ids"
  build_ok=true
  (cd "$REPO_ROOT" && ./mvnw -B -ntp -q -pl start-site test \
      -Dtest=ExampleProjectsVerificationTests -Dexamples="$ids" -Dsurefire.failIfNoSpecifiedTests=false \
      >"$OUTPUT.log" 2>&1) || build_ok=false
  for id in ${ids//,/ }; do
    if [[ -d "$OUTPUT/$id" ]]; then
      python3 "$EXAMPLES_DIR/sync-back.py" "$id" --snapshot
    fi
  done
  if [[ "$build_ok" == false ]]; then
    for id in ${ids//,/ }; do
      if grep -q "BUILD FAILURE" "$OUTPUT/$id.log" 2>/dev/null; then
        echo "FAIL: build of generated '$id' project. Log: $OUTPUT/$id.log" >&2
        grep -E "<<< (FAILURE|ERROR)|^\[ERROR\] (  |/.*\.java|The build process|Failed to execute goal)" "$OUTPUT/$id.log" | head -20 >&2
      fi
    done
    echo "FAIL: generation harness. Log: $OUTPUT.log" >&2
    exit 1
  fi
fi

if [[ ! -d ../node_modules/@playwright/test ]]; then
  (cd .. && npm install --silent)
fi
(cd .. && npx playwright install chromium >/dev/null)

free_port() {
  python3 -c 'import socket; s=socket.socket(); s.bind(("",0)); print(s.getsockname()[1]); s.close()'
}

for id in ${ids//,/ }; do
  jar=$(ls "$OUTPUT/$id"/*/target/*.jar 2>/dev/null | grep -v '\.original$' | head -1 || true)
  if [[ -z "$jar" ]]; then
    echo "FAIL: no jar for '$id' in $OUTPUT/$id (run without --skip-build)" >&2
    exit 1
  fi
  port=$(free_port)
  log="$OUTPUT/$id-run.log"
  echo "==> Starting $id on port $port"
  java -jar "$jar" --server.port="$port" --vaadin.launch-browser=false >"$log" 2>&1 &
  pid=$!
  trap 'kill $pid 2>/dev/null || true' EXIT
  for _ in $(seq 1 120); do
    if curl -sf -o /dev/null "http://localhost:$port/"; then
      break
    fi
    if ! kill -0 "$pid" 2>/dev/null; then
      echo "FAIL: $id exited during startup. Log: $log" >&2
      tail -40 "$log" >&2
      exit 1
    fi
    sleep 1
  done
  echo "==> Smoke test $id"
  if ! EXAMPLE_BASE_URL="http://localhost:$port" npx playwright test "$id.spec.js" --config "$EXAMPLES_DIR/playwright.config.js"; then
    echo "FAIL: smoke test for $id. App log: $log" >&2
    exit 1
  fi
  kill "$pid" 2>/dev/null || true
  wait "$pid" 2>/dev/null || true
  trap - EXIT
done

echo "PASS: $ids (screenshots in $SCREENSHOT_DIR)"
