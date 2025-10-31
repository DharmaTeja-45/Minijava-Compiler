#!/usr/bin/env bash
set -euo pipefail

# Run P5 on each .microIR in public_test_cases_microIR/
# Save generated miniRA files in output_miniRA/
# Then run kgi.jar on each generated miniRA and save outputs in outputs/

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$ROOT_DIR/public_test_cases_microIR"
OUT_MINIRA_DIR="$ROOT_DIR/output_miniRA"
OUT_KGI_DIR="$ROOT_DIR/outputs"
EXPECTED_DIR="$ROOT_DIR/expected_outputs"
COMPARISONS_DIR="$ROOT_DIR/comparisons"

mkdir -p "$OUT_MINIRA_DIR" "$OUT_KGI_DIR" "$EXPECTED_DIR" "$COMPARISONS_DIR"

shopt -s nullglob
for file in "$SRC_DIR"/*.microIR; do
  base=$(basename "$file" .microIR)
  echo "--- Processing: $base ---"

  # Run P5 to generate miniRA
  if java P5 < "$file" > "$OUT_MINIRA_DIR/${base}.miniRA" 2> "$OUT_MINIRA_DIR/${base}.err"; then
    echo "P5: ok -> $OUT_MINIRA_DIR/${base}.miniRA"
  else
    echo "P5: failed for $base (see $OUT_MINIRA_DIR/${base}.err)"
    # continue to next file; do not attempt kgi on failed generation
    continue
  fi

  # Run pgi on the original microIR to produce the expected output
  if [ -f "$ROOT_DIR/pgi.jar" ]; then
    if java -jar "$ROOT_DIR/pgi.jar" < "$file" > "$EXPECTED_DIR/${base}.out" 2> "$EXPECTED_DIR/${base}.err"; then
      echo "pgi: ok -> $EXPECTED_DIR/${base}.out"
    else
      echo "pgi: failed for $base (see $EXPECTED_DIR/${base}.err)"
    fi
  else
    echo "pgi.jar not found in $ROOT_DIR; skipping pgi for $base" > "$EXPECTED_DIR/${base}.err"
  fi

  # Run kgi on the generated miniRA (if kgi.jar exists)
  if [ -f "$ROOT_DIR/kgi.jar" ]; then
    if java -jar "$ROOT_DIR/kgi.jar" < "$OUT_MINIRA_DIR/${base}.miniRA" > "$OUT_KGI_DIR/${base}.out" 2> "$OUT_KGI_DIR/${base}.err"; then
      echo "kgi: ok -> $OUT_KGI_DIR/${base}.out"
    else
      echo "kgi: failed for $base (see $OUT_KGI_DIR/${base}.err)"
    fi
  else
    echo "kgi.jar not found in $ROOT_DIR; skipping kgi for $base" > "$OUT_KGI_DIR/${base}.err"
  fi

  # Compare kgi output with expected pgi output (if both exist)
  if [ -f "$OUT_KGI_DIR/${base}.out" ] && [ -f "$EXPECTED_DIR/${base}.out" ]; then
    if diff -u "$EXPECTED_DIR/${base}.out" "$OUT_KGI_DIR/${base}.out" > "$COMPARISONS_DIR/${base}.diff"; then
      echo "MATCH: outputs identical for $base"
    else
      echo "DIFF: outputs differ for $base (see $COMPARISONS_DIR/${base}.diff)"
    fi
  else
    echo "SKIP-CMP: missing outputs for $base (kgi or pgi failed)"
  fi

done

# Summary
echo
echo "Summary:"
echo "  miniRA files:"
ls -1 "$OUT_MINIRA_DIR" || true

echo
echo "  kgi outputs:"
ls -1 "$OUT_KGI_DIR" || true

exit 0
