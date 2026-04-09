# CIMET Organizational Analysis Engineering Backlog


## Terminology Contract

This document follows [`planning-glossary.md`](./planning-glossary.md) for mandatory terminology.

Operational guardrails:
- Distinguish **technical coupling** from **organizational coupling** in every section.
- Distinguish **cohesion (within-service)** from **coupling (between-service)** explicitly.
- Use role semantics consistently: **Jack=breadth**, **Maven=depth**, **Connector=brokerage**, **RSI=role co-occurrence intensity**.
- Treat `organizationalCoupling` as compatibility alias and `noc` as explicit pairwise normalized value where both appear.


This backlog translates `docs/user-stories.md` into execution-ready items for a research-grade system evolving toward production quality.

## Phase 0: Foundations and Architecture Baseline

### Epic A: Architecture and Pipeline Integration

#### Backlog ID
BL-001

#### Title
Establish organizational module package layout

#### Summary
Create package structure for `organizational.models`, `organizational.extract`, `organizational.metrics`, `organizational.services`, `organizational.output`, `organizational.runner` aligned with existing CIMET pipeline.

#### Type
refactor

#### Priority
P0

#### Estimate
S

#### Dependencies
- None

#### Definition of done
- Package directories/classes scaffolding committed.
- Naming conventions documented in code comments or package-info.
- No impact on existing runtime flow.

#### Suggested owner role
architect

---

#### Backlog ID
BL-002

#### Title
Introduce OrganizationalAnalysisService orchestration skeleton

#### Summary
Add orchestration service that defines stage order: extract contributions -> normalize identity -> map services -> windowing -> metrics -> export.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-001

#### Definition of done
- Service class with explicit stage interfaces exists.
- Stage order deterministic and testable.
- Placeholder execution path returns structured empty result.

#### Suggested owner role
backend engineer

---

#### Backlog ID
BL-003

#### Title
Define backward-compatible integration mode with existing pipeline

#### Summary
Specify and implement additive organizational stage invocation without changing legacy IR/Delta/Merge/Detection outputs by default.

#### Type
refactor

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-002

#### Definition of done
- Organizational mode toggled explicitly (not default-on).
- Existing runners behave unchanged in legacy configs.
- Regression check added for unchanged technical artifacts.

#### Suggested owner role
architect

---

### Epic B: Data Model Changes

#### Backlog ID
BL-004

#### Title
Create socio-technical top-level model classes

#### Summary
Implement `SocioTechnicalMicroserviceSystem` model with `type`, `name`, `technical`, `organizational`, `traceability`, and metadata.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-001

#### Definition of done
- Model classes compile and serialize to JSON.
- `technical` supports embedding existing `MicroserviceSystem` content.
- Metadata section includes schema/version placeholders.

#### Suggested owner role
backend engineer

---

#### Backlog ID
BL-005

#### Title
Implement organizational entity models

#### Summary
Add `DeveloperProfile`, `CommitContribution`, `ServiceOwnership`, `ServicePairRelation`, `IssueArtifact`, and relation-edge classes.

#### Type
feature

#### Priority
P0

#### Estimate
L

#### Dependencies
- BL-004

#### Definition of done
- Required fields from PRD exist with clear types.
- Entities support serialization/deserialization tests.
- Deterministic ordering strategy documented (IDs/time).

#### Suggested owner role
backend engineer

---

## Phase 1: Repository Mining and Identity/Mapping

### Epic C: Repository Mining and Contribution Extraction

#### Backlog ID
BL-006

#### Title
Implement contribution extraction core (commit metadata)

#### Summary
Build extraction path for commit ID, author name/email, timestamp, touched files, and message.

#### Type
feature

#### Priority
P0

#### Estimate
L

#### Dependencies
- BL-002

#### Definition of done
- Commit records produced for configurable analysis scope.
- Extraction robust for empty ranges.
- Extraction logs count of commits processed.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-007

#### Title
Add churn extraction (added/deleted LOC)

#### Summary
Extend contribution extractor to include per-file and per-commit LOC churn with safe fallback for unsupported diffs.

#### Type
feature

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-006

#### Definition of done
- LOC churn fields populated where possible.
- Unsupported diff cases handled without run failure.
- Test fixture covers binary/unparsable diff path.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-008

#### Title
Add optional issue/PR reference parsing

#### Summary
Implement configurable heuristic parser for issue/PR IDs from commit messages.

#### Type
feature

#### Priority
P2

#### Estimate
S

#### Dependencies
- BL-006

#### Definition of done
- Feature flag controls behavior.
- Extracted issue refs attached to commit records.
- Invalid patterns logged and ignored.

#### Suggested owner role
backend engineer

---

### Epic D: Service Boundary Mapping and Identity Normalization

#### Backlog ID
BL-009

#### Title
Implement file-to-microservice mapping using current IR paths

#### Summary
Map touched files to services using existing CIMET path semantics, including unmapped bucket.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-005
- BL-006

#### Definition of done
- Mapping function deterministic and reusable.
- Unmapped files counted and exposed.
- Mapping coverage metrics emitted.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-010

#### Title
Handle multi-service and shared/global file contribution allocation

#### Summary
Implement allocation policy for commits touching multiple services or non-service paths.

#### Type
feature

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-009

#### Definition of done
- Policy configurable (include/exclude/low-weight global files).
- Allocation deterministic and documented.
- Edge-case tests for mixed commits pass.

#### Suggested owner role
architect

---

#### Backlog ID
BL-011

#### Title
Implement developer identity normalization resolver

#### Summary
Normalize aliases/emails to stable developer IDs and retain alias provenance.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-006

#### Definition of done
- Canonical developer IDs generated deterministically.
- Alias lists retained in model.
- Ambiguous unresolved identities captured in warnings/metadata.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-012

#### Title
Support optional manual alias mapping file

#### Summary
Add override mechanism for canonical identity mapping from external config file.

#### Type
feature

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-011
- BL-026

#### Definition of done
- Mapping file path configurable.
- Override precedence over auto-resolver implemented.
- Validation catches malformed mapping entries.

#### Suggested owner role
backend engineer

---

#### Backlog ID
BL-013

#### Title
Generate traceability relation edges

#### Summary
Emit `developerToCommit`, `commitToFile`, `developerToMicroservice`, and optional `commitToIssue` edge tables.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-005
- BL-008
- BL-009
- BL-011

#### Definition of done
- All required edge tables generated.
- Edge referential consistency checks pass.
- Deduplication rules applied deterministically.

#### Suggested owner role
backend engineer

---

## Phase 2: Temporal Windowing and Core Metrics

### Epic E: Temporal Windowing

#### Backlog ID
BL-014

#### Title
Implement window specification engine

#### Summary
Support full-history, trailing-duration, and calendar-bucket windows with explicit timezone behavior.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-006
- BL-026

#### Definition of done
- Window generation supports required modes.
- Boundary semantics documented and tested.
- Window metadata stored for export.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-015

#### Title
Implement deterministic contribution-to-window assignment

#### Summary
Assign each contribution event to windows according to configured rules.

#### Type
feature

#### Priority
P0

#### Estimate
S

#### Dependencies
- BL-014

#### Definition of done
- No accidental duplicate event assignment.
- Boundary event behavior covered by tests.
- Deterministic output order guaranteed.

#### Suggested owner role
backend engineer

---

### Epic F: Metric Algorithms (Ownership, PTC, NOC/AOC, Roles, RSI)

#### Backlog ID
BL-016

#### Title
Implement service ownership and concentration calculator

#### Summary
Compute per-service contributor shares, primary developers, and concentration per window.

#### Type
feature

#### Priority
P0

#### Estimate
L

#### Dependencies
- BL-010
- BL-015

#### Definition of done
- Ownership outputs populated for service-window pairs.
- Primary owner selection configurable (threshold/top-k).
- Handles no-contribution services gracefully.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-017

#### Title
Implement PTC algorithm module

#### Summary
Compute Pairwise Team Cohesion per service-window, reflecting focused and balanced participation.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-016

#### Definition of done
- PTC values generated for eligible service-window pairs.
- PTC edge cases explicitly handled and documented.
- Formula version tag included in output metadata.

#### Suggested owner role
architect

---

#### Backlog ID
BL-018

#### Title
Implement pairwise normalized organizational coupling (NOC)

#### Summary
Compute pairwise service coupling using developer overlap and switching behavior signals.

#### Type
feature

#### Priority
P0

#### Estimate
L

#### Dependencies
- BL-015
- BL-016

#### Definition of done
- `servicePairRelations` includes coupling values and shared developer info.
- Switching score computed and emitted per pair.
- Normalization method documented and test-covered.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-019

#### Title
Implement AOC aggregation

#### Summary
Aggregate pairwise NOC into average organizational coupling per window.

#### Type
feature

#### Priority
P0

#### Estimate
S

#### Dependencies
- BL-018

#### Definition of done
- AOC generated per window.
- Aggregation denominator/exclusions emitted.
- Stable behavior for sparse pair sets.

#### Suggested owner role
backend engineer

---

#### Backlog ID
BL-020

#### Title
Implement Jack score calculator

#### Summary
Compute breadth-oriented developer role score per window.

#### Type
feature

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-016
- BL-015

#### Definition of done
- Jack score emitted for active developers.
- Normalization and interpretation documented.
- Unit tests include single-service vs multi-service contrast.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-021

#### Title
Implement Maven score calculator

#### Summary
Compute depth-oriented developer role score per window.

#### Type
feature

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-016

#### Definition of done
- Maven score emitted for active developers.
- Deep specialization pattern validated on fixtures.
- Formula version tracked.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-022

#### Title
Implement Connector score calculator

#### Summary
Compute brokerage-oriented developer role score per window.

#### Type
feature

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-018
- BL-015

#### Definition of done
- Connector score emitted for active developers.
- Low-bridge behavior validated for non-switching developers.
- Algorithm assumptions documented.

#### Suggested owner role
architect

---

#### Backlog ID
BL-023

#### Title
Implement role classification thresholds and presets

#### Summary
Map Jack/Maven/Connector scores to role labels based on configurable thresholds.

#### Type
feature

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-020
- BL-021
- BL-022
- BL-026

#### Definition of done
- Labeling configuration parsed and applied.
- Invalid threshold sets rejected with clear errors.
- Output includes both raw scores and labels.

#### Suggested owner role
backend engineer

---

#### Backlog ID
BL-024

#### Title
Implement RSI calculator

#### Summary
Compute Role Stacking Index capturing co-occurrence of Jack/Maven/Connector signals.

#### Type
feature

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-020
- BL-021
- BL-022

#### Definition of done
- RSI emitted per developer-window.
- Sparse activity edge cases handled.
- RSI formula version in metadata.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-025

#### Title
Add metric explainability metadata

#### Summary
Attach per-metric provenance: formula version, normalization settings, exclusions, and input counts.

#### Type
feature

#### Priority
P2

#### Estimate
S

#### Dependencies
- BL-017
- BL-018
- BL-019
- BL-024

#### Definition of done
- Metadata emitted consistently by all metric modules.
- Explainability section validated by tests.
- Does not break target example-compatible shape.

#### Suggested owner role
architect

---

## Phase 3: CLI, Config, Export/Schema Contract

### Epic G: CLI and Config Work

#### Backlog ID
BL-026

#### Title
Add organizational config parsing and validation

#### Summary
Extend config model to include optional organizational block with defaults, validations, and runtime access.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-003

#### Definition of done
- Organizational block parsed when present.
- Missing block keeps legacy behavior.
- Validation errors are actionable and deterministic.

#### Suggested owner role
backend engineer

---

#### Backlog ID
BL-027

#### Title
Create OrganizationalAnalysisRunner CLI

#### Summary
Implement dedicated runner with `--config`, `--output`, window options, threshold controls, and help output.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-002
- BL-026

#### Definition of done
- Runner executes end-to-end organizational pipeline.
- Invalid args return usage + non-zero exit.
- CLI help documents options and defaults.

#### Suggested owner role
backend engineer

---

#### Backlog ID
BL-028

#### Title
Implement CLI-over-config precedence

#### Summary
Apply precedence rules so CLI options override config values consistently.

#### Type
feature

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-027

#### Definition of done
- Precedence behavior tested for all overlapping options.
- Effective runtime config logged.
- Conflict behavior deterministic and documented.

#### Suggested owner role
backend engineer

---

### Epic H: Export and Schema Compatibility

#### Backlog ID
BL-029

#### Title
Implement OrganizationalIRExportService

#### Summary
Serialize socio-technical artifact to JSON using existing utility conventions.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-004
- BL-005
- BL-013

#### Definition of done
- Export service writes artifact to configured path.
- Directory creation and write failures handled.
- Export output deterministic for same input.

#### Suggested owner role
backend engineer

---

#### Backlog ID
BL-030

#### Title
Enforce compatibility with docs/example_ir.json target shape

#### Summary
Ensure exported JSON aligns with expected top-level and organizational sections when possible.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-029

#### Definition of done
- Required sections/fields present and typed correctly.
- Optional arrays allowed empty.
- Compatibility checker/test implemented.

#### Suggested owner role
architect

---

#### Backlog ID
BL-031

#### Title
Define output naming convention and metadata/versioning

#### Summary
Add deterministic output filename strategy and include schema/provenance metadata in artifact.

#### Type
feature

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-029
- BL-030

#### Definition of done
- Naming convention documented and tested.
- Metadata includes schema version, generation time, window/config summary.
- Defaults and override behavior clear.

#### Suggested owner role
backend engineer

---

## Phase 4: Tests, Fixtures, and Regression Safety

### Epic I: Tests and Fixtures

#### Backlog ID
BL-032

#### Title
Build synthetic fixture generator for socio-technical test datasets

#### Summary
Create reusable fixtures with controlled commits, aliases, service mappings, and switching patterns.

#### Type
test

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-005
- BL-006

#### Definition of done
- Fixture generator or static fixtures available for unit/integration tests.
- Covers at least: alias collision, multi-service commits, sparse windows.
- Fixture docs explain intended metric outcomes.

#### Suggested owner role
QA

---

#### Backlog ID
BL-033

#### Title
Unit tests: extraction, mapping, identity, windowing

#### Summary
Add unit coverage for foundational data pipeline components.

#### Type
test

#### Priority
P0

#### Estimate
L

#### Dependencies
- BL-007
- BL-009
- BL-011
- BL-015
- BL-032

#### Definition of done
- Core primitives have high-signal tests.
- Edge cases covered for empty/malformed inputs.
- Tests deterministic and CI-enabled.

#### Suggested owner role
QA

---

#### Backlog ID
BL-034

#### Title
Golden tests: ownership/PTC/NOC/AOC/roles/RSI

#### Summary
Add fixed expected-value tests to protect metric algorithms from silent drift.

#### Type
test

#### Priority
P0

#### Estimate
L

#### Dependencies
- BL-017
- BL-018
- BL-019
- BL-020
- BL-021
- BL-022
- BL-024
- BL-032

#### Definition of done
- Golden expected outputs committed and reviewed.
- Formula version bumps require explicit golden updates.
- Failure diffs readable for diagnosis.

#### Suggested owner role
QA

---

#### Backlog ID
BL-035

#### Title
Export/schema contract tests

#### Summary
Validate exported artifact compatibility and field contracts including optional sections.

#### Type
test

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-030

#### Definition of done
- Key-path/type contract tests pass.
- Required fields enforced.
- Optional sections accepted as empty arrays.

#### Suggested owner role
QA

---

### Epic J: Backward Compatibility and Production Hardening

#### Backlog ID
BL-036

#### Title
Regression tests for legacy CIMET flows

#### Summary
Ensure existing IR/Delta/Merge/detection behavior is unchanged when organizational mode disabled.

#### Type
test

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-003
- BL-027

#### Definition of done
- Legacy integration tests pass unchanged.
- Representative old outputs compared baseline vs new code.
- Regression report attached to CI artifacts.

#### Suggested owner role
QA

---

#### Backlog ID
BL-037

#### Title
Performance baseline and scalability scenario

#### Summary
Establish runtime and memory baseline for full-history vs windowed analysis on medium/large histories.

#### Type
spike

#### Priority
P2

#### Estimate
M

#### Dependencies
- BL-027
- BL-034

#### Definition of done
- Benchmark scenario and results documented.
- Bottlenecks identified with recommended optimizations.
- Follow-up backlog tickets created for confirmed hotspots.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-038

#### Title
Structured logging and failure diagnostics hardening

#### Summary
Standardize logs for stage progress, counts, warnings, and failure reasons.

#### Type
tech-debt

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-027

#### Definition of done
- Each stage logs start/end with counts.
- Failures include actionable context (invalid config, mapping misses, parse failures).
- Log format consistent across runner and services.

#### Suggested owner role
backend engineer

---

## Phase 5: Documentation and Release Readiness

### Epic K: Documentation and Adoption

#### Backlog ID
BL-039

#### Title
Document metric definitions and caveats

#### Summary
Add documentation for PTC, NOC/AOC, Jack/Maven/Connector, RSI, assumptions, and interpretation limits.

#### Type
docs

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-017
- BL-018
- BL-019
- BL-024

#### Definition of done
- Docs include formulas/semantics and edge-case interpretation.
- Assumptions and limitations explicitly stated.
- Links to config controls affecting each metric.

#### Suggested owner role
docs

---

#### Backlog ID
BL-040

#### Title
Publish runner usage and migration guide

#### Summary
Document CLI usage, config examples, output interpretation, and backward compatibility guarantees.

#### Type
docs

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-027
- BL-031
- BL-036

#### Definition of done
- Quick-start examples for full-history and windowed runs.
- Migration guidance for existing CIMET users.
- Troubleshooting section added.

#### Suggested owner role
docs

---

---

## Phase 6: Research Spikes and Governance

### Epic L: Research Spikes

#### Backlog ID
BL-041

#### Title
Metric formula calibration spike

#### Summary
Validate and calibrate PTC/NOC/AOC/role/RSI formulations against reference examples and synthetic scenarios.

#### Type
spike

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-017
- BL-018
- BL-019
- BL-024

#### Definition of done
- Formula alternatives evaluated and documented.
- Default formulations selected with rationale.
- Metric metadata/version updates identified.

#### Suggested owner role
architect

---

#### Backlog ID
BL-042

#### Title
Identity normalization quality spike

#### Summary
Assess alias normalization quality (false merges/splits) and tune resolution rules.

#### Type
spike

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-011
- BL-012

#### Definition of done
- Quality assessment on representative datasets completed.
- High-risk ambiguity patterns documented.
- Resolver refinement recommendations captured.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-043

#### Title
NOC switching-signal design spike

#### Summary
Compare switching signal variants (commit-count vs LOC-weighted) and select default strategy.

#### Type
spike

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-018

#### Definition of done
- Variant comparison results documented.
- Default strategy selected with rationale.
- Compatibility impact on AOC metadata documented.

#### Suggested owner role
architect

---

#### Backlog ID
BL-044

#### Title
Incremental/windowed scaling spike

#### Summary
Prototype incremental processing and caching strategy for large-history repositories.

#### Type
spike

#### Priority
P2

#### Estimate
M

#### Dependencies
- BL-037

#### Definition of done
- Prototype design and measurements documented.
- Bottlenecks and recommended path identified.
- Follow-up implementation tasks proposed.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-045

#### Title
Schema evolution and migration policy spike

#### Summary
Define schema evolution policy, compatibility levels, and migration guidance for socio-technical IR outputs.

#### Type
spike

#### Priority
P2

#### Estimate
S

#### Dependencies
- BL-030
- BL-031

#### Definition of done
- Versioning policy documented with examples.
- Breaking vs non-breaking criteria established.
- Migration-note template created for future schema changes.

#### Suggested owner role
architect



---

## Phase 7: Organizational IR Model Concretization

### Epic M: Data Model Concretization and Contracts

#### Backlog ID
BL-046

#### Title
Define organizational IR model contracts

#### Summary
Specify concrete model contracts for developers, contributor-service relations, service metrics, pair coupling, windows, and role/RSI profiles.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-004
- BL-005

#### Definition of done
- Field-level contracts documented and code-aligned.
- Required/optional fields clearly identified.
- Compatibility obligations with `docs/example_ir.json` captured.

#### Suggested owner role
architect

---

#### Backlog ID
BL-047

#### Title
Implement contributor-service relation model and serialization

#### Summary
Add and serialize contributor-service relationship entities with per-window contribution share and support metrics.

#### Type
feature

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-016
- BL-029

#### Definition of done
- `contributorServiceRelations[]` emitted with deterministic ordering.
- Ownership derivation traceable from relation rows.
- Unit tests validate referential integrity and deduplication.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-048

#### Title
Implement service organizational metrics entity

#### Summary
Add dedicated service-level organizational metrics entity including ownership concentration, PTC, and metadata.

#### Type
feature

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-017
- BL-025

#### Definition of done
- `serviceOrganizationalMetrics[]` emitted per service-window.
- Includes `ptc`, `ownershipConcentration`, contributor counts, and metric metadata.
- Contract tests validate field presence and ranges.

#### Suggested owner role
backend engineer

---

#### Backlog ID
BL-049

#### Title
Add pairwise coupling compatibility alias fields

#### Summary
Represent pair coupling with both `noc` and compatibility field `organizationalCoupling` in exported entity.

#### Type
refactor

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-018
- BL-030

#### Definition of done
- Pair entities include both fields with documented semantics.
- `example_ir.json` compatibility maintained.
- No regression in existing export contract tests.

#### Suggested owner role
architect

---

#### Backlog ID
BL-050

#### Title
Implement temporal window entity and references

#### Summary
Add `analysisWindows[]` and ensure `windowId` references exist across commit, relation, and metric entities.

#### Type
feature

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-014
- BL-015
- BL-029

#### Definition of done
- Windows serialized with boundaries/timezone/mode and aggregate fields.
- Cross-entity `windowId` references validated.
- Window metadata deterministic and test-covered.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-051

#### Title
Implement developer role-history model with RSI by window

#### Summary
Extend developer output with per-window role profile history including Jack/Maven/Connector scores and RSI.

#### Type
feature

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-023
- BL-024
- BL-029

#### Definition of done
- Developer profile includes current and historical role data.
- RSI available in role history and top-level role snapshot.
- Role history tested for sparse and dense windows.

#### Suggested owner role
backend engineer

---

#### Backlog ID
BL-052

#### Title
Publish and validate illustrative organizational IR instance

#### Summary
Create and maintain `docs/proposed-organizational-ir.json` as concrete sample data for tooling and contract validation.

#### Type
docs

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-030
- BL-035

#### Definition of done
- Sample IR instance committed and valid JSON.
- Sample covers all new model entities and references.
- Contract checks include sample-based key-path validation.

#### Suggested owner role
docs


---

## Phase 8: Algorithm Correctness and Conformance

### Epic N: Algorithm Formalization and Validation

#### Backlog ID
BL-053

#### Title
Implement algorithm-spec reference constants and numeric policy

#### Summary
Centralize algorithm constants and numeric conventions from `docs/algorithm-spec.md` (clamping, epsilon, default lambdas, singleton PTC handling, normalization behavior).

#### Type
feature

#### Priority
P0

#### Estimate
S

#### Dependencies
- BL-046

#### Definition of done
- Constants and numeric policy module implemented.
- All metric modules consume shared policy instead of ad hoc literals.
- Policy version emitted in metadata.

#### Suggested owner role
architect

---

#### Backlog ID
BL-054

#### Title
Build algorithm conformance harness from algorithm-spec worked examples

#### Summary
Create deterministic conformance tests mapping known input fixtures to expected outputs for PTC, OC/NOC/AOC, Jack/Maven/Connector, and RSI.

#### Type
test

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-053
- BL-034

#### Definition of done
- Harness executes end-to-end metric checks against fixed fixtures.
- Expected outputs stored and reviewed.
- Failures provide readable metric-by-metric deltas.

#### Suggested owner role
QA

---

#### Backlog ID
BL-055

#### Title
Add property-based invariants for organizational metrics

#### Summary
Add invariant/property tests (range bounds, conservation checks, symmetry, denominator safety, deterministic ordering) as described in algorithm-spec.

#### Type
test

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-053
- BL-033

#### Definition of done
- Property test suite implemented and CI-enabled.
- Invariants cover contribution shares, NOC symmetry, AOC aggregation, and role score ranges.
- Edge-case generators include sparse/empty/singleton windows.

#### Suggested owner role
QA

---

#### Backlog ID
BL-056

#### Title
Implement sparse-pair optimization for coupling/connector calculations

#### Summary
Replace naive `O(S^2 * D)` pair iteration with sparse active-pair construction (`sum k_d^2`) where possible, preserving exact results.

#### Type
refactor

#### Priority
P1

#### Estimate
L

#### Dependencies
- BL-018
- BL-022
- BL-037

#### Definition of done
- Optimized path enabled for large windows.
- Numerical parity with baseline algorithm verified by tests.
- Complexity/performance notes updated in docs.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-057

#### Title
Add algorithm versioning and migration metadata support

#### Summary
Emit algorithm-version identifiers and migration notes in output metadata to preserve reproducibility across formula or policy changes.

#### Type
feature

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-025
- BL-045
- BL-053

#### Definition of done
- Output contains algorithm version bundle (PTC/NOC/AOC/Roles/RSI + numeric policy).
- Backward-compatible defaults preserved for existing consumers.
- Migration guidance documented for version changes.

#### Suggested owner role
backend engineer


---

## Phase 9: Test Strategy Implementation

### Epic O: Test Coverage Execution

#### Backlog ID
BL-058

#### Title
Implement organizational unit test suite execution plan

#### Summary
Implement unit coverage for extraction, mapping, identity normalization, windowing, and metric helpers based on `docs/test-strategy.md`.

#### Type
test

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-053
- BL-033

#### Definition of done
- Unit suite covers all core organizational modules.
- Determinism and invariant assertions added.
- CI runs suite on each PR.

#### Suggested owner role
QA

---

#### Backlog ID
BL-059

#### Title
Implement golden export and JSON contract test suite

#### Summary
Add golden snapshot tests and contract validations for organizational JSON export compatibility.

#### Type
test

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-052
- BL-054
- BL-035

#### Definition of done
- Golden fixtures for representative scenarios exist.
- Contract checks validate required keys and compatibility aliases.
- Golden update policy documented and enforced.

#### Suggested owner role
QA

---

#### Backlog ID
BL-060

#### Title
Implement metric correctness and oracle suites

#### Summary
Implement exact-value and tolerance-based oracle tests for PTC, OC/NOC/AOC, Jack/Maven/Connector, RSI.

#### Type
test

#### Priority
P0

#### Estimate
L

#### Dependencies
- BL-054
- BL-055

#### Definition of done
- Metric oracles cover all required metric families.
- Invariant checks (symmetry/range/aggregation) are automated.
- Failure output is diagnostic and actionable.

#### Suggested owner role
QA

---

#### Backlog ID
BL-061

#### Title
Implement service mapping and longitudinal window test suites

#### Summary
Add focused suites for service assignment edge cases and temporal window correctness across modes/timezones.

#### Type
test

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-009
- BL-014
- BL-015

#### Definition of done
- Mapping edge cases and boundary windows are covered.
- Timezone semantics validated by fixtures.
- Window aggregate consistency checks implemented.

#### Suggested owner role
QA

---

#### Backlog ID
BL-062

#### Title
Implement malformed-data and edge-case hardening tests

#### Summary
Create malformed-input suites for identity, timestamps, churn, config, and missing data paths.

#### Type
test

#### Priority
P1

#### Estimate
M

#### Dependencies
- BL-033
- BL-038

#### Definition of done
- Malformed cases are codified and automated.
- Expected fail-fast vs graceful-degradation behaviors are asserted.
- Diagnostic message expectations are included.

#### Suggested owner role
QA

---

#### Backlog ID
BL-063

#### Title
Implement legacy CIMET regression compatibility suite

#### Summary
Expand regression suite to explicitly validate unchanged legacy outputs when organizational mode is disabled.

#### Type
test

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-036

#### Definition of done
- Legacy artifact comparisons are automated.
- Regression pass/fail summary attached in CI artifacts.
- No organizational-side effects in legacy mode.

#### Suggested owner role
QA

---

#### Backlog ID
BL-064

#### Title
Implement performance smoke gates for organizational pipeline

#### Summary
Define and run performance smoke thresholds (runtime/memory/stage timing) for medium synthetic histories.

#### Type
test

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-037
- BL-056

#### Definition of done
- Smoke thresholds are defined and codified.
- CI/nightly smoke job runs and reports metrics.
- Breach behavior (warn/fail) is documented.

#### Suggested owner role
data engineer

---

#### Backlog ID
BL-065

#### Title
Create canonical organizational fixture packs

#### Summary
Build fixture packs for small synthetic history, multi-service patterns, alias cases, role stacking, and coupling edge scenarios.

#### Type
test

#### Priority
P0

#### Estimate
M

#### Dependencies
- BL-032
- BL-052

#### Definition of done
- Fixture packs exist with expected outputs and readme metadata.
- Fixtures are versioned and deterministic.
- Suites consume shared fixture packs rather than ad hoc test data.

#### Suggested owner role
QA

---

#### Backlog ID
BL-066

#### Title
Publish test strategy and CI test-gate mapping docs

#### Summary
Document practical execution of the test strategy, including PR vs nightly gates and release criteria evidence.

#### Type
docs

#### Priority
P1

#### Estimate
S

#### Dependencies
- BL-058
- BL-059
- BL-060
- BL-063

#### Definition of done
- `docs/test-strategy.md` maintained and linked from planning docs.
- CI workflow references documented test gates.
- Release checklist references required test evidence.

#### Suggested owner role
docs

## Critical path

1. BL-001 -> BL-002 -> BL-003
2. BL-004 -> BL-005
3. BL-006 -> BL-009 -> BL-011 -> BL-013
4. BL-014 -> BL-015 -> BL-016
5. BL-017 + BL-018 -> BL-019
6. BL-020 + BL-021 + BL-022 -> BL-023 -> BL-024
7. BL-026 -> BL-027 -> BL-028
8. BL-029 -> BL-030 -> BL-031
9. BL-032 -> BL-033 + BL-034 + BL-035
10. BL-036 (must pass before release)
11. BL-046 -> BL-047 -> BL-050 -> BL-051 -> BL-052
12. BL-053 -> BL-054
13. BL-058 -> BL-059 -> BL-060 -> BL-063

Release gate for production-quality baseline:
- Critical path complete
- BL-034/BL-035/BL-036 green in CI
- BL-039 and BL-040 published

## Risks that need spikes

1. **Metric formula calibration risk**
- Spike: validate PTC/NOC/AOC/role/RSI formulations against paper examples and synthetic data.
- Backlog item: BL-041.

2. **Identity normalization ambiguity risk**
- Spike: evaluate false-merge/false-split rates on diverse commit author datasets.
- Backlog item: BL-042.

3. **Switching-behavior signal design risk for NOC**
- Spike: compare commit-count vs LOC-weighted switching formulations.
- Backlog item: BL-043.

4. **Large-history performance risk**
- Spike: prototype streaming/window incremental pipeline.
- Backlog item: BL-044.

5. **Schema evolution risk**
- Spike: compatibility policy and migration strategy for future schema versions.
- Backlog item: BL-045.

## Milestone mapping

### Milestone M0: Architecture + Schema Baseline
Includes: BL-001, BL-003, BL-004, BL-041, BL-046

Exit criteria:
- Architecture and schema contracts agreed.
- Non-breaking integration strategy validated.

### Milestone M1: Contribution Extraction + Mapping
Includes: BL-002, BL-005 to BL-013, BL-042, BL-047

Exit criteria:
- Organizational extraction pipeline runs end-to-end with traceability edges.
- Identity normalization and mapping coverage metrics available.

### Milestone M2: Windowing + Cohesion/Coupling Core
Includes: BL-014 to BL-019, BL-025, BL-043, BL-048, BL-050, BL-053

Exit criteria:
- Ownership/PTC/NOC/AOC calculations implemented and explainable.
- Window semantics and metric metadata stable.

### Milestone M3: Roles + RSI
Includes: BL-020 to BL-024, BL-051, BL-056

Exit criteria:
- Jack/Maven/Connector and RSI calculations implemented with configurable thresholds.
- Role history model available for longitudinal analysis.

### Milestone M4: CLI + Export + Reporting
Includes: BL-026 to BL-031, BL-037, BL-038, BL-049, BL-057

Exit criteria:
- Runner/config UX stable.
- JSON export compatible with `docs/example_ir.json` target shape.

### Milestone M5: Verification + Docs + Release Readiness
Includes: BL-032 to BL-036, BL-039, BL-040, BL-044, BL-045, BL-052, BL-054, BL-055, BL-058 to BL-066

Exit criteria:
- Golden/contract/regression suites pass in CI.
- Documentation complete for formulas, usage, migration, troubleshooting, and test strategy gates.
- Production-quality baseline release candidate approved.