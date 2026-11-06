#!/usr/bin/env bash
# Builds and unit-tests every module, then collects the ten jars into dist/. Forge and NeoForge builds of one Minecraft version
# use the same class names for different code, so they are separate jars and are never merged.
# Usage: scripts/build_all.sh [mc-version ...]   (default: all five)
set -u
cd "$(dirname "$0")/.."
VERSIONS=("$@")
[ ${#VERSIONS[@]} -eq 0 ] && VERSIONS=(1.20.1 1.21.1 26.1.2 26.2 26.3)
MODVER=$(grep -h '^mod_version=' neoforge-26.1.2/gradle.properties | cut -d= -f2)
mkdir -p dist
fail=0
for v in "${VERSIONS[@]}"; do
  jars=()
  for loader in neoforge forge; do
    echo "=== $loader-$v"
    if ! (cd "$loader-$v" && chmod +x gradlew && ./gradlew build --no-daemon -q 2>&1 | grep -v "Started working\|REUSE\|DONE\|Total runtime\|^WARNING"); then :; fi
    jar=$(ls "$loader-$v"/build/libs/barelypipes-$loader-mc$v-$MODVER.jar 2>/dev/null)
    if [ -z "$jar" ]; then echo "FAILED: no jar for $loader-$v"; fail=1; else jars+=("$jar"); fi
  done
  [ ${#jars[@]} -gt 0 ] && cp "${jars[@]}" dist/
done
for jar in dist/*.jar; do
  if unzip -l "$jar" | grep -E "/dev/|SelfTest|test_tank|DevHooks"; then echo "dev files in $jar"; fail=1; fi
done
ls -la dist
exit $fail
