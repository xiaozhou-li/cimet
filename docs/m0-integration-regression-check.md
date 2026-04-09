# M0-US2 Regression Check: Backward-Compatible Integration Mode

## Scope
Validate that the M0 organizational integration contract is additive and does not alter legacy behavior by default.

## 1) Commands executed

### Legacy compile path
```bash
./mvnw -DskipTests compile -Dstyle.color=never
```

### Build runtime classpath for direct runner execution
```bash
./mvnw -q -DskipTests dependency:build-classpath \
  -Dmdep.outputFile=/tmp/cimet.cp.txt -Dstyle.color=never
```

### Representative legacy flow (Detection/Excel runner) - default mode
```bash
CP="$(cat /tmp/cimet.cp.txt):target/classes"
rm -f logs/app.log
java -cp "$CP" edu.university.ecs.lab.detection.ExcelOutputRunner
```

### Representative legacy flow - explicit disabled mode
```bash
CP="$(cat /tmp/cimet.cp.txt):target/classes"
rm -f logs/app.log
java -Dcimet.organizational.mode=disabled -cp "$CP" \
  edu.university.ecs.lab.detection.ExcelOutputRunner
```

### Control check - explicit stub mode (hook enabled but still no-op in M0)
```bash
CP="$(cat /tmp/cimet.cp.txt):target/classes"
rm -f logs/app.log
java -Dcimet.organizational.mode=stub -cp "$CP" \
  edu.university.ecs.lab.detection.ExcelOutputRunner
```

## 2) Observed legacy behavior

All three representative runs completed successfully (exit code `0`) and executed the legacy detection flow.

Observed invariants from `logs/app.log` and output artifact metadata:

| Mode | Org hook log present | `Delta changes extracted` lines | `Merged to new IR` lines | XLSX size (bytes) |
|---|---:|---:|---:|---:|
| default (no property) | 0 | 294 | 294 | 42989 |
| disabled | 0 | 294 | 294 | 42989 |
| stub | 1 | 294 | 294 | 42989 |

Key log evidence in `stub` mode:
- `Organizational integration mode enabled: STUB (M0 stub)`

Key log evidence in `default` and `disabled` modes:
- No `Organizational integration mode enabled` line detected.

## 3) Proof that organizational path is additive and inactive by default

1. **Inactive by default**: default run (`no -D cimet.organizational.mode`) showed no organizational hook log line.
2. **Explicitly disabled behavior matches default**: `default` and `disabled` runs had identical legacy execution counts (`294` delta + `294` merge log lines) and same XLSX output size.
3. **Additive integration only**: enabling `stub` adds one organizational integration log line while legacy execution counts and output size remain unchanged.
4. **No semantic change in legacy stages**: IR/Delta/Merge/Detection stages still run in the same sequence and volume for all modes tested.

## 4) Warnings noted (non-blocking)

- Maven model warning during compile: duplicate `org.codehaus.mojo:exec-maven-plugin` declaration.
- Runtime warnings: SLF4J fallback to NOP binder and JDK `Unsafe` deprecation warnings from dependencies.

These warnings pre-existed and did not cause regression failure.

## Verdict

M0-US2 regression check status: **PASS**

- Compile succeeded.
- Legacy representative flow remained stable in default mode.
- Organizational integration path is additive and disabled by default.
