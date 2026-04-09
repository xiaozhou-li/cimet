# Release Plan: CIMET Organizational-Analysis Extension


## Terminology Contract

This document follows [`planning-glossary.md`](./planning-glossary.md) for mandatory terminology.

Operational guardrails:
- Distinguish **technical coupling** from **organizational coupling** in every section.
- Distinguish **cohesion (within-service)** from **coupling (between-service)** explicitly.
- Use role semantics consistently: **Jack=breadth**, **Maven=depth**, **Connector=brokerage**, **RSI=role co-occurrence intensity**.
- Treat `organizationalCoupling` as compatibility alias and `noc` as explicit pairwise normalized value where both appear.


## 1. Release objective

Deliver a production-baseline organizational-analysis capability for CIMET that:
- preserves the existing IR -> Delta -> Merge -> Detection pipeline,
- adds version-control-based organizational analytics (ownership, PTC, NOC/AOC, Jack/Maven/Connector, RSI),
- exports a socio-technical JSON artifact compatible with `docs/example_ir.json` where possible,
- is validated by deterministic tests, golden metric fixtures, and regression safety for existing CIMET flows.

## 2. Scope by milestone

- **M0**: Architecture and schema reconnaissance finalization.
- **M1**: Contribution extraction, identity normalization, service mapping, traceability base.
- **M2**: Temporal windows + cohesion/coupling metrics (ownership, PTC, NOC/AOC).
- **M3**: Key developer role scoring and RSI.
- **M4**: IR export compatibility, runner/CLI/config, reporting outputs.
- **M5**: Hardening, regression safety, docs, release readiness.

## 3. Milestone names and outcomes

### M0: Architecture and Schema Reconnaissance

#### Included backlog IDs
- BL-001
- BL-003
- BL-004 (model scaffold only)
- BL-041

#### Expected deliverables
- Finalized module architecture and package boundaries.
- Confirmed non-breaking integration pattern with legacy pipeline.
- Initial socio-technical schema contract and versioning strategy.
- Resolved open technical assumptions to unblock implementation.

#### Measurable success criteria
- Architecture decision record or equivalent note accepted by team.
- No changes to legacy output behavior in smoke checks.
- Draft schema key-paths mapped to `docs/example_ir.json` fields.

#### Known risks
- Ambiguity in metric formula interpretation from research sources.
- Early schema lock-in before implementation learnings.

---

### M1: Contribution Extraction and Service Mapping

#### Included backlog IDs
- BL-002
- BL-005
- BL-006
- BL-007
- BL-008
- BL-009
- BL-010
- BL-011
- BL-012
- BL-013

#### Expected deliverables
- Working extraction of commit metadata and churn.
- Developer identity normalization with optional alias overrides.
- Mapping from touched files to CIMET microservices.
- Traceability edge generation (`developerToCommit`, `commitToFile`, etc.).

#### Measurable success criteria
- Extraction succeeds on at least one representative repository end-to-end.
- Mapping coverage metric reported (mapped vs unmapped files).
- Deterministic output for same input/config across reruns.
- Traceability tables pass referential integrity checks.

#### Known risks
- Alias ambiguity can skew ownership and role signals.
- Shared/global file handling can bias service mapping.

---

### M2: Organizational Cohesion/Coupling Metrics

#### Included backlog IDs
- BL-014
- BL-015
- BL-016
- BL-017
- BL-018
- BL-019
- BL-025
- BL-043

#### Expected deliverables
- Temporal window engine (full/trailing/calendar).
- Service ownership and concentration outputs per window.
- PTC per service-window.
- Pairwise organizational coupling as NOC and aggregate organizational coupling as AOC per window.
- Explainability metadata (formula versions, exclusions, counts).

#### Measurable success criteria
- Metrics produced for all non-empty windows.
- PTC/AOC deterministic across reruns.
- Golden fixtures for at least one synthetic dataset precomputed (initial).
- Metadata includes formula and normalization versions.

#### Known risks
- Window boundary semantics can produce subtle inconsistencies.
- NOC switching component sensitivity may require calibration.

---

### M3: Key Developer Roles and RSI

#### Included backlog IDs
- BL-020
- BL-021
- BL-022
- BL-023
- BL-024
- BL-042

#### Expected deliverables
- Jack, Maven, Connector score calculators.
- Configurable role threshold classification.
- RSI calculator with versioned formula metadata.

#### Measurable success criteria
- Role scores available for all active developers in each window.
- Role labels and raw scores exported together.
- RSI produced for same scope as role scores.
- Golden checks added for at least one known role-pattern fixture.

#### Known risks
- Threshold defaults may not generalize across repos.
- Role overlap behavior may need tuning per context.

---

### M4: IR Export and Reporting

#### Included backlog IDs
- BL-026
- BL-027
- BL-028
- BL-029
- BL-030
- BL-031
- BL-037
- BL-038

#### Expected deliverables
- Organizational config parsing/validation with defaults.
- Dedicated `OrganizationalAnalysisRunner` CLI.
- CLI-over-config precedence.
- Stable JSON export service and naming conventions.
- Compatibility checks against `docs/example_ir.json` target shape.
- Run summary reporting/logging.

#### Measurable success criteria
- CLI run produces valid socio-technical JSON artifact.
- Export contract tests pass for required key paths and types.
- Output naming deterministic for same run parameters.
- Structured logs include stage counts and failures.

#### Known risks
- Config UX complexity may cause misconfiguration.
- Export compatibility may drift if model evolves without strict contracts.

---

### M5: Hardening, Regression, Documentation

#### Included backlog IDs
- BL-032
- BL-033
- BL-034
- BL-035
- BL-036
- BL-039
- BL-040
- BL-044
- BL-045

#### Expected deliverables
- Comprehensive test fixtures and unit coverage.
- Golden metric and schema contract tests in CI.
- Legacy CIMET regression safety verification.
- Metric documentation, usage guide, and migration guide.

#### Measurable success criteria
- CI green for unit + golden + contract + regression suites.
- Legacy baseline outputs unchanged when organizational mode off.
- Documentation complete for formulas, config, CLI, troubleshooting.
- Release candidate checklist signed off by engineering lead.

#### Known risks
- Performance bottlenecks on long histories.
- Insufficient fixture diversity may hide edge-case regressions.

## 4. Exit criteria per milestone

- **M0 exit**: architecture and schema contracts agreed; non-breaking integration strategy approved.
- **M1 exit**: extraction/mapping/identity/traceability operational and deterministic.
- **M2 exit**: windowed ownership/PTC/NOC/AOC complete with explainability metadata.
- **M3 exit**: Jack/Maven/Connector + RSI complete and configurable.
- **M4 exit**: CLI/config/export/reporting complete; schema compatibility checks passing.
- **M5 exit**: testing/documentation/regression gates fully green; release baseline approved.

## 5. Dependencies and sequencing

### High-level sequencing
1. Architecture and data model scaffolding must precede metric work.
2. Extraction/mapping/identity must precede all metrics.
3. Windowing must precede PTC/AOC and role calculations.
4. Role/RSI can run in parallel with export integration once metric inputs are stable.
5. Contract tests and regression tests are mandatory before release sign-off.

### Cross-milestone dependency highlights
- M2 depends on M1 completion for reliable contribution/service graph.
- M3 depends on M2 ownership/coupling foundations.
- M4 depends on M2+M3 output model stabilization.
- M5 depends on all prior milestones, especially schema and CLI stabilization.

## 6. Testing gates

### Gate T1 (after M1)
- Foundational unit tests for extraction/mapping/identity pass.
- Determinism check for same input/config rerun.

### Gate T2 (after M2)
- Golden tests for ownership/PTC/NOC/AOC pass.
- Window-boundary edge-case tests pass.

### Gate T3 (after M3)
- Golden tests for roles/RSI pass.
- Threshold validation tests pass.

### Gate T4 (after M4)
- JSON export contract tests pass.
- CLI/config precedence and validation tests pass.

### Gate T5 (release gate, after M5)
- Full CI suite green: unit + golden + contract + regression.
- Legacy CIMET regression suite green with organizational mode disabled.
- Minimum performance baseline documented and accepted.

## 7. Documentation gates

### Gate D1 (after M2)
- Draft metric definitions and assumptions documented.

### Gate D2 (after M4)
- CLI/config usage and output schema docs updated.

### Gate D3 (release gate, after M5)
- Final documentation set complete:
  - formulas and caveats,
  - runner quick-start,
  - migration/backward compatibility guidance,
  - troubleshooting.

## 8. Rollback / compatibility considerations

- Organizational extension is additive and should remain feature-flag/config-gated.
- Default behavior must preserve legacy CIMET outputs and runner behavior.
- If severe issue is found post-integration:
  - disable organizational mode via config/runner path,
  - continue serving legacy technical pipeline,
  - retain schema versioning so consumers can detect incompatible output variants.
- Export compatibility policy:
  - preserve required key paths (`type`, `name`, `technical`, `organizational`, `traceability`),
  - evolve via explicit schema version increments,
  - avoid breaking field renames without migration notes.

## 9. Proposed timeline (relative phases)

- **Phase A (short)**: M0 architecture/schema confirmation.
- **Phase B (short-medium)**: M1 extraction/mapping/identity.
- **Phase C (medium)**: M2 windowing + ownership/PTC/NOC/AOC.
- **Phase D (medium)**: M3 roles + RSI.
- **Phase E (short-medium)**: M4 CLI/config/export/reporting integration.
- **Phase F (medium)**: M5 hardening, regression, docs, release readiness.

Recommended overlap:
- Start fixture/test scaffolding (BL-032) near end of Phase B.
- Run spike activities in parallel with Phases C-E as needed.

## 10. MVP definition

### MVP scope
- M0 through M4 complete, with minimum M5 testing subset.

### MVP must-have capabilities
- Contribution extraction + identity normalization + service mapping.
- Temporal windowing.
- Ownership, PTC, pairwise NOC, and aggregate AOC.
- Jack/Maven/Connector and RSI.
- CLI runner and organizational config.
- JSON export compatible with `docs/example_ir.json` target shape.
- Core tests: foundational unit tests + schema contract + key golden metrics.

### MVP non-blocking items
- Advanced performance optimization beyond baseline.
- Extended issue-tracker integrations.
- Rich reporting outputs beyond summary/log + JSON.

## 11. Post-MVP enhancements

1. Performance and scale enhancements
- Incremental computation and caching for repeated window runs.
- Streamed processing for very large histories.

2. Quality of identity and traceability
- Enhanced alias disambiguation workflows.
- Optional integration with external identity sources.

3. Metric maturity
- Alternative calibration profiles by repository type/team size.
- Additional socio-technical indicators and confidence scoring.

4. Export ecosystem
- Formal JSON Schema publication and migration tooling.
- Optional CSV/Parquet export for analytics platforms.

5. Reporting evolution
- Optional Excel integration and richer visual summaries.
- Trend/comparison reports across windows/release slices.