# Organizational Analysis Test Strategy


## Terminology Contract

This document follows [`planning-glossary.md`](./planning-glossary.md) for mandatory terminology.

Operational guardrails:
- Distinguish **technical coupling** from **organizational coupling** in every section.
- Distinguish **cohesion (within-service)** from **coupling (between-service)** explicitly.
- Use role semantics consistently: **Jack=breadth**, **Maven=depth**, **Connector=brokerage**, **RSI=role co-occurrence intensity**.
- Treat `organizationalCoupling` as compatibility alias and `noc` as explicit pairwise normalized value where both appear.


## 1. Purpose and Scope

This strategy defines how to validate the organizational-analysis extension end-to-end while preserving existing CIMET behavior.

Scope includes:
- new organizational extraction/mapping/window/metric/export layers,
- compatibility with `docs/example_ir.json` and `docs/proposed-organizational-ir.json`,
- backward compatibility with current CIMET IR/Delta/Merge/Detection flows.

Out of scope:
- full benchmark/perf engineering (covered by smoke thresholds in this document and follow-up optimization backlog).

## 2. Test Pyramid and Execution Model

1. **Unit tests** (fast, deterministic): core algorithms and transformations.
2. **Component/integration tests**: extraction -> mapping -> metrics -> export pipeline slices.
3. **Golden artifact tests**: JSON output snapshots + contract checks.
4. **Regression suites**: legacy CIMET behavior unchanged when organizational mode is off.
5. **Performance smoke tests**: upper-bound sanity checks, not full benchmarking.

CI recommendation:
- PR gate: unit + core integration + contract checks.
- nightly: full golden suite + malformed/edge fuzz set + perf smoke.

## 3. Unit Tests

Target modules:
- contribution parsing/churn aggregation,
- identity normalization,
- service-path assignment (longest-prefix logic),
- window generation and assignment,
- PTC (organizational cohesion), OC/NOC/AOC (organizational coupling), and Jack/Maven/Connector/RSI calculators,
- normalization/clamping utilities,
- traceability edge builders.

Required unit-test properties:
- deterministic outputs for same inputs,
- no hidden state between runs,
- stable sort order of entity arrays,
- denominator safety for zero/empty cases.

Minimum unit assertions:
- score ranges in `[0,1]`,
- NOC symmetry (`NOC(s,t)==NOC(t,s)`),
- AOC equals mean of pairwise NOC values,
- per-service contribution shares sum to 1 when contributions exist.

## 4. Golden JSON Export Tests

Goals:
- protect output shape and representative values from accidental drift.

Test types:
1. **Snapshot golden tests**
- compare generated artifact to canonical JSON fixtures for selected scenarios.
2. **Key-path contract tests**
- required top-level keys: `type`, `name`, `technical`, `organizational`, `traceability`.
- required compatibility keys in pair entities: `organizationalCoupling`.
- required model keys from new schema: `contributorServiceRelations`, `serviceOrganizationalMetrics`, `analysisWindows`.

Golden update policy:
- allowed only with explicit algorithm/schema version bump note,
- PR must include rationale and changed expected values summary.

## 5. Metric Correctness Tests

For each metric family:

### 5.1 PTC
- balanced 2-developer service > unbalanced 2-developer service,
- singleton service uses configured singleton fallback,
- no-contribution service yields configured behavior (omit or zero entry).

### 5.2 Organizational Coupling (OC/NOC/AOC)
- no overlap + no switching -> OC=0 and NOC=0 (single pair context),
- overlap-only and switching-only synthetic scenarios isolate each component,
- AOC equals arithmetic mean of NOC over active pairs.

### 5.3 Jack/Maven/Connector
- broad cross-service actor has higher Jack than specialist,
- specialist has higher Maven than broad actor,
- bridging actor scores higher Connector than isolated actor.

### 5.4 RSI
- high balanced role vector -> high RSI,
- highly skewed role vector with same mean -> lower RSI,
- sparse/no-activity developers handled without NaN/Inf.

Test style:
- exact expected values for small deterministic fixtures,
- tolerance-based checks (epsilon) for aggregated floating point values.

## 6. Service Mapping Tests

Mapping invariants:
- longest-prefix microservice path wins,
- unmapped files go to explicit global bucket behavior,
- normalized path separators independent of platform,
- rename events use new path mapping for post-change context.

Coverage cases:
- overlapping service path prefixes,
- nested module roots,
- root-level shared files,
- invalid/empty paths.

## 7. Longitudinal Window Tests

Windowing cases:
- `full`, `trailing`, `calendar` modes,
- boundary inclusion semantics `[start,end)`,
- timezone handling and daylight-saving boundary inputs,
- commit assigned to exactly one window unless explicitly configured otherwise.

Window-driven correctness:
- metric values shift appropriately when windows change,
- window metadata (`commitCount`, `developerCount`, `aoc`) consistent with included commits.

## 8. Regression Tests for Existing CIMET Functionality

Regression scope:
- IR extraction output shape/content for legacy runs,
- Delta extraction behavior,
- Merge behavior,
- Detection/metric reports currently in CIMET.

Mandatory assertion:
- with organizational mode disabled, legacy outputs are unchanged (byte-level where feasible, semantic equivalence otherwise).

## 9. Performance Smoke Tests

Purpose:
- detect severe regressions early, not perform final perf tuning.

Suggested smoke thresholds (configurable by CI environment):
- medium synthetic history run completes under predefined budget,
- memory does not exceed predefined ceiling,
- no algorithmic blow-up on sparse large-service scenarios.

Metrics captured:
- total runtime,
- peak memory (if available),
- per-stage timings (extraction/mapping/metrics/export).

## 10. Malformed Data and Edge-Case Tests

Required malformed scenarios:
- missing author email/name,
- malformed timestamps,
- missing LOC values,
- empty diff sets,
- duplicate aliases with conflicting mappings,
- corrupted/missing config fields,
- invalid window settings (`end <= start`, invalid duration token).

Expected behavior:
- fail fast for invalid config,
- degrade gracefully for malformed optional fields,
- produce actionable diagnostics without silent corruption.

## 11. Fixture Strategy

## 11.1 Fixture categories

### A) Small synthetic repository histories
- 3-10 commits, 2-3 services, deterministic file changes.
- Used for exact-value tests and golden snapshots.

### B) Multi-service contribution patterns
- developer touches 1, 2, and 3+ services with controlled churn weights.
- Enables Jack/Maven separation checks and ownership concentration tests.

### C) Identity aliasing cases
- same person with multiple emails,
- no-reply aliases,
- near-collision names with distinct emails.
- validates merge/split behavior and ambiguity handling.

### D) Role stacking scenarios
- crafted role vectors:
  - high Jack + high Connector + medium Maven,
  - high Maven only,
  - low all roles.
- validates RSI and role-labeling behavior.

### E) Coupling edge cases
- zero overlap/zero switching,
- high overlap/low switching,
- low overlap/high switching,
- dense pair graph vs sparse pair graph.
- validates OC decomposition, NOC normalization, and AOC aggregation.

## 11.2 Fixture artifact set

For each fixture set include:
- input commit/change records,
- expected intermediate relations (developer-service, pair relations),
- expected metric outputs,
- expected exported JSON.

Naming convention suggestion:
- `org_fx_<scenario>_<version>.json`
- `org_expected_<scenario>_<version>.json`

## 12. Test Data Governance

- version fixtures with algorithm/schema versions,
- avoid editing existing golden fixtures in place without changelog,
- enforce deterministic ordering before snapshot comparison,
- include fixture provenance comments/README.

## 13. Test Gates and Release Criteria

### Gate G1 (foundation)
- core unit suites green,
- mapping and identity tests green.

### Gate G2 (metrics)
- PTC/OC/NOC/AOC and role/RSI correctness suites green,
- algorithm conformance harness green.

### Gate G3 (export)
- golden JSON snapshots and key-path contracts green,
- compatibility assertions with `example_ir.json` satisfied.

### Gate G4 (compatibility)
- legacy CIMET regression suite green with organizational mode off.

### Gate G5 (release)
- malformed/edge suites green,
- performance smoke suites green,
- documentation for test scenarios updated.

## 14. Tooling and Implementation Notes

- Keep deterministic random seeds for generated fixtures.
- Provide helper assertions for floating comparisons (`abs(actual-expected) <= eps`).
- Keep reusable fixture builders for windows, contributions, and expected metrics.
- Integrate test summaries into CI logs with per-stage pass/fail counts.