# User Stories: CIMET Organizational-Analysis Extension


## Terminology Contract

This document follows [`planning-glossary.md`](./planning-glossary.md) for mandatory terminology.

Operational guardrails:
- Distinguish **technical coupling** from **organizational coupling** in every section.
- Distinguish **cohesion (within-service)** from **coupling (between-service)** explicitly.
- Use role semantics consistently: **Jack=breadth**, **Maven=depth**, **Connector=brokerage**, **RSI=role co-occurrence intensity**.
- Treat `organizationalCoupling` as compatibility alias and `noc` as explicit pairwise normalized value where both appear.


## Epic 1: Repository Mining and Contribution Extraction

### Story ID
ORG-001

### Title
Create organizational contribution extraction service

### User story statement
As a platform engineer, I want a dedicated contribution extraction service so that organizational analysis is implemented as a clean pipeline stage.

### Description
Implement `ContributionExtractionService` under an organizational package. It should use existing CIMET config and repository acquisition behavior, and produce normalized commit-level records consumable by downstream metrics.

### Acceptance criteria
- Service exists with clear input/output contracts.
- Service can run after repository acquisition without modifying technical IR extraction behavior.
- Produces an in-memory or serialized list of commit contribution records.

### Dependencies
- Existing `Config`/`ConfigUtil`.
- Existing `GitService` clone/reset/log capabilities.

### Priority
P0

### Notes / technical hints
- Start with a read-only workflow on Git history.
- Keep API model-first to support test fixtures.

---

### Story ID
ORG-002

### Title
Extract commit metadata required for organizational metrics

### User story statement
As an analyst, I want commit metadata extracted so that ownership, coupling, and role metrics can be computed.

### Description
For each commit in scope, extract commit ID, author name/email, timestamp, touched files, and basic commit message metadata.

### Acceptance criteria
- All required fields exist in contribution records.
- Supports full-history and bounded-history execution.
- Handles empty commit ranges gracefully.

### Dependencies
- ORG-001

### Priority
P0

### Notes / technical hints
- Prefer deterministic commit ordering (e.g., ascending by commit time then hash).

---

### Story ID
ORG-003

### Title
Extract file-level churn (added/deleted LOC)

### User story statement
As an architect, I want LOC churn per commit/file so that contribution weights can reflect change magnitude.

### Description
Capture added/deleted LOC at file level where available, and aggregate to commit level.

### Acceptance criteria
- `addedLOC` and `deletedLOC` captured for mapped files when diff data exists.
- Missing diff stats do not fail run; values default safely.
- Aggregation is deterministic and test-covered.

### Dependencies
- ORG-002

### Priority
P1

### Notes / technical hints
- Keep fallback path for binary or unparsable diffs.

---

### Story ID
ORG-004

### Title
Support commit range and date-bound filtering

### User story statement
As a researcher, I want filtered commit scopes so that I can run targeted longitudinal analyses.

### Description
Enable filtering by explicit commit range and by date bounds before metric computation.

### Acceptance criteria
- Filter behavior documented and deterministic.
- Invalid bounds return actionable errors.
- Filtered commit count logged.

### Dependencies
- ORG-002
- CLI/config stories in Epic 7

### Priority
P1

### Notes / technical hints
- Apply filtering once upstream to avoid duplicate logic across metrics.

---

### Story ID
ORG-005

### Title
Infer issue/PR references from commit messages (optional)

### User story statement
As an engineering manager, I want optional issue linking so that traceability can connect work items and code changes.

### Description
Implement heuristic extraction for issue/PR IDs from commit messages, behind a configuration flag.

### Acceptance criteria
- Feature disabled by default.
- When enabled, linked issue identifiers appear in commit records.
- Parsing failures are non-fatal and logged at debug/warn level.

### Dependencies
- ORG-002

### Priority
P2

### Notes / technical hints
- Regex strategy should be explicit and versioned.

---

## Epic 2: Service Boundary Mapping and Identity Normalization

### Story ID
ORG-006

### Title
Map touched files to current CIMET microservice boundaries

### User story statement
As a system architect, I want file changes mapped to services so that organizational metrics align with CIMET’s technical model.

### Description
Use existing `MicroserviceSystem` path semantics to map each touched file to a microservice or unmapped bucket.

### Acceptance criteria
- Mapping uses technical IR service path rules.
- Unmapped files are explicitly represented.
- Coverage metrics (mapped/unmapped counts) are output.

### Dependencies
- ORG-001
- Existing technical IR extraction outputs

### Priority
P0

### Notes / technical hints
- Reuse existing path normalization conventions (`/` style repo paths).

---

### Story ID
ORG-007

### Title
Handle multi-service commits and shared/global file touches

### User story statement
As an analyst, I want robust handling of cross-service commits so that ownership and coupling metrics are not biased.

### Description
For commits touching multiple services or global files, assign contributions using configurable weighting/exclusion rules.

### Acceptance criteria
- Configurable treatment for global/shared files exists.
- Multi-service contributions are deterministically allocated.
- Behavior is covered by fixture-based tests.

### Dependencies
- ORG-006

### Priority
P1

### Notes / technical hints
- Provide baseline policy: include mapped service files; configurable low weight or exclude global files.

---

### Story ID
ORG-008

### Title
Implement developer identity normalization

### User story statement
As a technical lead, I want aliases normalized so that developer-level metrics are accurate.

### Description
Create `DeveloperIdentityResolver` to merge author names/emails into stable developer IDs.

### Acceptance criteria
- Deterministic ID assignment for equivalent aliases.
- Alias list retained for each developer profile.
- Unresolved ambiguities captured in output metadata.

### Dependencies
- ORG-002

### Priority
P0

### Notes / technical hints
- Normalize case and common no-reply patterns; avoid irreversible destructive merges without rules.

---

### Story ID
ORG-009

### Title
Support optional manual alias mapping file

### User story statement
As a repository maintainer, I want to provide alias overrides so that known identity edge cases are resolved correctly.

### Description
Allow optional mapping file that pins aliases to canonical developer IDs.

### Acceptance criteria
- Mapping file is optional and validated.
- Manual mappings override automatic resolver.
- Invalid mapping entries produce actionable warnings/errors.

### Dependencies
- ORG-008
- CLI/config stories in Epic 7

### Priority
P1

### Notes / technical hints
- Keep file format simple JSON map for initial version.

---

### Story ID
ORG-010

### Title
Emit traceability edges for contribution graph

### User story statement
As an auditor, I want explicit traceability edges so that every metric can be explained back to raw events.

### Description
Generate `developerToCommit`, `commitToFile`, `developerToMicroservice`, and optional `commitToIssue` edge sets.

### Acceptance criteria
- All required relation tables emitted.
- Edge IDs reference existing entities.
- No duplicate edges after normalization.

### Dependencies
- ORG-005
- ORG-006
- ORG-008

### Priority
P0

### Notes / technical hints
- Use deterministic sorted edges for stable diffs.

---

## Epic 3: Temporal Windowing and Metric Engine Foundation

### Story ID
ORG-011

### Title
Implement temporal window generator

### User story statement
As a researcher, I want configurable windows so that I can analyze organizational changes over time.

### Description
Support full-history, trailing-duration, and calendar-bucket windows.

### Acceptance criteria
- Window modes implemented and documented.
- Timezone handling explicit and configurable.
- Generated windows persisted in run metadata.

### Dependencies
- ORG-002
- Epic 7 CLI/config

### Priority
P0

### Notes / technical hints
- Use inclusive/exclusive boundary convention and document it clearly.

---

### Story ID
ORG-012

### Title
Assign contributions to windows deterministically

### User story statement
As an engineer, I want deterministic window assignment so that repeated runs are reproducible.

### Description
Map each contribution event to exactly one window according to configured boundary rules.

### Acceptance criteria
- No event belongs to multiple windows unless explicitly configured.
- Boundary events behave per documented convention.
- Re-run stability verified by tests.

### Dependencies
- ORG-011

### Priority
P0

### Notes / technical hints
- Add tests for timezone transitions and DST-adjacent timestamps.

---

### Story ID
ORG-013

### Title
Build metric computation orchestration layer

### User story statement
As a developer, I want a single orchestrator so that ownership, PTC, AOC, role, and RSI computations run consistently.

### Description
Implement `OrganizationalAnalysisService` coordinating extraction outputs, windowing, metric calculators, and export model assembly.

### Acceptance criteria
- Orchestrator invokes calculators in stable order.
- Failed optional sub-components do not corrupt mandatory outputs.
- Run summary includes per-stage timing/logging.

### Dependencies
- ORG-010
- ORG-011

### Priority
P0

### Notes / technical hints
- Keep calculators pure and side-effect free for testability.

---

### Story ID
ORG-014

### Title
Implement service ownership and contributor share computation

### User story statement
As an architect, I want service ownership metrics so that I can identify concentrated or fragmented responsibility.

### Description
Compute per-service contribution shares, ownership concentration, and primary developers using configurable threshold/top-k.

### Acceptance criteria
- `serviceOwnership` includes primary developers and concentration values.
- Ownership computation is window-aware.
- Handles services with zero contributions in a window.

### Dependencies
- ORG-006
- ORG-011
- ORG-013

### Priority
P0

### Notes / technical hints
- Expose configurable weighting strategy (commit count vs LOC-weighted baseline).

---

## Epic 4: Organizational Metrics (PTC, NOC/AOC, Roles, RSI)

### Story ID
ORG-015

### Title
Implement PTC calculator

### User story statement
As an engineering manager, I want PTC per service so that I can assess focused and balanced participation patterns.

### Description
Create `PTCCalculator` computing service-level cohesion from contributor distribution within each window.

### Acceptance criteria
- PTC computed for each service-window pair with contributions.
- PTC value range and interpretation documented.
- Edge cases (single contributor, no contributor) handled explicitly.

### Dependencies
- ORG-014

### Priority
P0

### Notes / technical hints
- Keep formula implementation versioned (`ptcFormulaVersion`).

---

### Story ID
ORG-016

### Title
Implement pairwise normalized organizational coupling (NOC) calculator

### User story statement
As a system architect, I want pairwise coupling scores so that I can detect risky cross-service collaboration dependencies.

### Description
Compute normalized pairwise coupling between service pairs using developer overlap and switching behavior inputs.

### Acceptance criteria
- `servicePairRelations` populated with pairwise coupling.
- Includes shared developer lists and switching score components.
- Produces stable values with deterministic ordering.

### Dependencies
- ORG-011
- ORG-013
- ORG-014

### Priority
P0

### Notes / technical hints
- Keep component sub-scores available for debugging.

---

### Story ID
ORG-017

### Title
Implement AOC aggregation

### User story statement
As a portfolio owner, I want system-level AOC so that I can track overall organizational coupling trend.

### Description
Aggregate pairwise NOC values into average organizational coupling per window, with optional weighted variants.

### Acceptance criteria
- AOC emitted per window.
- Aggregation method documented in metadata.
- Missing pair data handled gracefully.

### Dependencies
- ORG-016

### Priority
P0

### Notes / technical hints
- Record denominator and excluded-pair count for transparency.

---

### Story ID
ORG-018

### Title
Implement Jack role score computation

### User story statement
As an engineering manager, I want Jack scores so that I can identify high-breadth contributors across services.

### Description
Compute Jack score per developer-window based on cross-service breadth signal.

### Acceptance criteria
- Jack score emitted for all active developers.
- Score normalization documented.
- Developers with single-service activity score lower breadth by definition.

### Dependencies
- ORG-006
- ORG-011
- ORG-013

### Priority
P1

### Notes / technical hints
- Keep raw breadth counts alongside normalized score in internal model.

---

### Story ID
ORG-019

### Title
Implement Maven role score computation

### User story statement
As a tech lead, I want Maven scores so that I can identify developers with deep, service-specific expertise.

### Description
Compute Maven score per developer-window emphasizing depth and concentration in specific services.

### Acceptance criteria
- Maven score emitted for all active developers.
- Deep single-service contributors are distinguishable from broad contributors.
- Formula version tagged in metadata.

### Dependencies
- ORG-014
- ORG-011
- ORG-013

### Priority
P1

### Notes / technical hints
- Use ownership share and concentration as primary signal candidates.

---

### Story ID
ORG-020

### Title
Implement Connector role score computation

### User story statement
As an architect, I want Connector scores so that I can identify developers brokering across service boundaries.

### Description
Compute Connector score per developer-window from cross-service bridging behavior.

### Acceptance criteria
- Connector score emitted for all active developers.
- Developers with no cross-service transitions have low brokerage score.
- Score behavior validated with synthetic fixtures.

### Dependencies
- ORG-016
- ORG-011
- ORG-013

### Priority
P1

### Notes / technical hints
- Candidate signal: service-switching sequence and pair participation centrality.

---

### Story ID
ORG-021

### Title
Implement role classification thresholds

### User story statement
As a manager, I want score-to-label classification so that role assignments are interpretable.

### Description
Classify developers into Jack/Maven/Connector labels using configurable thresholds.

### Acceptance criteria
- Thresholds configurable via config/CLI.
- Labels and raw scores both emitted.
- Invalid thresholds fail fast with clear errors.

### Dependencies
- ORG-018
- ORG-019
- ORG-020
- Epic 7 config

### Priority
P1

### Notes / technical hints
- Include profile presets and explicit override precedence.

---

### Story ID
ORG-022

### Title
Implement RSI computation

### User story statement
As a researcher, I want RSI values so that I can quantify role co-occurrence rather than isolated roles.

### Description
Compute Role Stacking Index per developer-window based on the combined Jack/Maven/Connector signals.

### Acceptance criteria
- RSI emitted per developer-window.
- RSI metadata includes formula version.
- RSI robust for sparse-contribution developers.

### Dependencies
- ORG-018
- ORG-019
- ORG-020

### Priority
P1

### Notes / technical hints
- Keep RSI continuous; optional categorical bins can be derived later.

---

### Story ID
ORG-023

### Title
Add metric explainability fields

### User story statement
As an auditor, I want explainability metadata so that metric outputs can be validated and trusted.

### Description
Attach metric-level provenance: input counts, excluded records, formula versions, and normalization settings.

### Acceptance criteria
- Per-window metadata includes formula and normalization versions.
- Exclusion counts available for ownership/PTC/AOC/roles.
- Explainability fields do not break example-compatible JSON shape.

### Dependencies
- ORG-015
- ORG-016
- ORG-017
- ORG-022

### Priority
P2

### Notes / technical hints
- Keep provenance in dedicated metadata subsection.

---

## Epic 5: Socio-Technical IR Model and JSON Export Compatibility

### Story ID
ORG-024

### Title
Implement socio-technical IR data model

### User story statement
As a tooling engineer, I want a structured socio-technical model so that organizational and technical data can be consumed together.

### Description
Create model classes for top-level socio-technical artifact and organizational sections while preserving existing technical IR models.

### Acceptance criteria
- Model includes `type`, `name`, `technical`, `organizational`, `traceability`.
- Organizational entities include developers, commits, ownership, service pair relations.
- Models support JSON serialization via existing style.

### Dependencies
- ORG-010
- ORG-013

### Priority
P0

### Notes / technical hints
- Keep package separation: `organizational.models`.

---

### Story ID
ORG-025

### Title
Implement JSON export service for organizational artifact

### User story statement
As a platform integrator, I want stable JSON export so that downstream systems can ingest results automatically.

### Description
Add `OrganizationalIRExportService` to serialize socio-technical artifact using `JsonReadWriteUtils`.

### Acceptance criteria
- Output path configurable.
- Parent directories auto-created as needed.
- Export is deterministic in field/array ordering policy.

### Dependencies
- ORG-024
- Existing `JsonReadWriteUtils`

### Priority
P0

### Notes / technical hints
- Reuse existing serialization conventions and pretty-printing.

---

### Story ID
ORG-026

### Title
Guarantee compatibility with docs/example_ir.json target shape

### User story statement
As a consumer of CIMET outputs, I want schema compatibility so that existing parsers can adapt with minimal changes.

### Description
Validate exported JSON contains required sections and field names compatible with `docs/example_ir.json` when possible.

### Acceptance criteria
- Required top-level fields exist.
- Required organizational arrays/objects exist (empty arrays allowed for optional content).
- Compatibility test compares exported artifact to expected key-path set.

### Dependencies
- ORG-025

### Priority
P0

### Notes / technical hints
- Use tolerant schema checks (field presence + types), not strict value matching.

---

### Story ID
ORG-027

### Title
Add schema versioning and provenance metadata

### User story statement
As a maintainer, I want schema/provenance metadata so that output evolution remains manageable.

### Description
Add metadata fields for schema version, generation time, analysis window settings, and formula versions.

### Acceptance criteria
- Metadata included in every export.
- Version values centrally defined constants.
- Metadata changes covered by tests.

### Dependencies
- ORG-023
- ORG-025

### Priority
P1

### Notes / technical hints
- Use semantic version string for schema.

---

## Epic 6: CLI, Configuration, and Runner Integration

### Story ID
ORG-028

### Title
Create OrganizationalAnalysisRunner CLI entrypoint

### User story statement
As a user, I want a dedicated runner so that organizational analysis can be invoked independently and predictably.

### Description
Add new runner class with explicit args parsing for config, output path, and windowing options.

### Acceptance criteria
- Runner accepts required `--config` and optional `--output`.
- Help/usage text available on invalid args.
- Runner exits with non-zero on validation errors.

### Dependencies
- ORG-013
- ORG-025

### Priority
P0

### Notes / technical hints
- Avoid hardcoded args pattern present in some legacy runners.

---

### Story ID
ORG-029

### Title
Add organizational configuration block parsing

### User story statement
As an operator, I want config-driven defaults so that runs are reproducible without long CLI commands.

### Description
Extend config handling to parse optional `organizational` block and apply defaults.

### Acceptance criteria
- Missing block uses documented defaults.
- Invalid values trigger clear validation errors.
- Parsed config available to all organizational services.

### Dependencies
- ORG-028

### Priority
P0

### Notes / technical hints
- Preserve backward compatibility of existing config fields.

---

### Story ID
ORG-030

### Title
Implement CLI-over-config precedence rules

### User story statement
As a power user, I want CLI overrides so that I can run ad hoc experiments without editing config files.

### Description
Define and implement precedence: CLI flags override config values for all overlapping organizational options.

### Acceptance criteria
- Precedence behavior documented and tested.
- Effective runtime configuration logged.
- Conflicting options resolved deterministically.

### Dependencies
- ORG-029

### Priority
P1

### Notes / technical hints
- Include a debug dump of effective config (with sensitive info redacted if needed).

---

### Story ID
ORG-031

### Title
Expose threshold and weighting controls in CLI/config

### User story statement
As a researcher, I want threshold controls so that I can calibrate roles and ownership metrics for different repositories.

### Description
Add controls for ownership thresholds/top-k, role thresholds, window parameters, and issue linking toggle.

### Acceptance criteria
- All PRD-defined controls available via config and key CLI flags.
- Type/range validation enforced.
- Defaults documented.

### Dependencies
- ORG-029
- ORG-030
- ORG-014
- ORG-021

### Priority
P1

### Notes / technical hints
- Keep controls names aligned with output metadata keys.

---

## Epic 7: Validation, Golden Tests, and Regression Safety

### Story ID
ORG-032

### Title
Add unit tests for extraction and mapping primitives

### User story statement
As a developer, I want foundational unit tests so that core ingestion logic remains stable during iteration.

### Description
Add unit tests for commit parsing, churn extraction, path mapping, and identity resolution.

### Acceptance criteria
- Tests cover happy path and edge cases.
- Deterministic snapshot assertions for normalized outputs.
- CI integration for new test suites.

### Dependencies
- ORG-003
- ORG-006
- ORG-008

### Priority
P0

### Notes / technical hints
- Build small synthetic fixtures independent from external repos.

---

### Story ID
ORG-033

### Title
Add golden tests for PTC, NOC/AOC, roles, and RSI

### User story statement
As a maintainer, I want golden metric tests so that formula implementations do not drift unintentionally.

### Description
Create canonical fixtures with expected metric values for ownership/PTC/NOC/AOC/Jack/Maven/Connector/RSI.

### Acceptance criteria
- Golden tests fail on metric drift.
- Formula version bumps require explicit golden update.
- Test data and expected values documented.

### Dependencies
- ORG-015
- ORG-016
- ORG-017
- ORG-022

### Priority
P0

### Notes / technical hints
- Keep fixture dataset compact but structurally diverse.

---

### Story ID
ORG-034

### Title
Add schema compatibility and export contract tests

### User story statement
As an integrator, I want contract tests so that JSON shape remains compatible with expected consumers.

### Description
Test exported artifact against required key paths and type expectations aligned with `docs/example_ir.json`.

### Acceptance criteria
- Contract test validates required sections and arrays.
- Optional sections accepted as empty arrays where permitted.
- Regression test protects against accidental field renames.

### Dependencies
- ORG-026

### Priority
P0

### Notes / technical hints
- Prefer JSONPath/key-path assertions over full-file snapshot alone.

---

### Story ID
ORG-035

### Title
Protect existing CIMET technical flows with regression tests

### User story statement
As a CIMET maintainer, I want regression safety so that organizational extension does not break existing IR/Delta/Merge/detection behavior.

### Description
Add tests ensuring legacy outputs and entrypoints remain functionally unchanged when organizational mode is disabled.

### Acceptance criteria
- Baseline technical tests continue to pass.
- No behavior change for legacy configs without organizational block.
- Regression report included in CI summary.

### Dependencies
- ORG-028
- ORG-029

### Priority
P0

### Notes / technical hints
- Compare representative existing output artifacts before/after extension.

---

### Story ID
ORG-036

### Title
Add performance and scalability test scenario

### User story statement
As an operator, I want performance checks so that large-history repos remain practical to analyze.

### Description
Create benchmark-style test scenario for large commit histories and measure runtime/memory for windowed and full-history modes.

### Acceptance criteria
- Performance baseline recorded.
- No unbounded memory growth observed for tested size.
- Results documented with hardware/context notes.

### Dependencies
- ORG-011
- ORG-013

### Priority
P2

### Notes / technical hints
- Use synthetic generated history when real large repo fixtures are unavailable.

---

## Epic 8: Reporting, Output Artifacts, and Documentation

### Story ID
ORG-037

### Title
Generate concise run summary report/log

### User story statement
As an engineering manager, I want a concise summary so that I can quickly interpret organizational analysis results.

### Description
Emit summary including developer/commit counts, mapped coverage, top PTC/AOC observations, and top role holders.

### Acceptance criteria
- Summary emitted on successful run.
- Summary values align with JSON artifact.
- Failure paths include partial progress diagnostics.

### Dependencies
- ORG-013
- ORG-017
- ORG-022
- ORG-025

### Priority
P1

### Notes / technical hints
- Keep summary machine-parseable friendly (structured log line keys).

---

### Story ID
ORG-038

### Title
Define output file naming and directory conventions

### User story statement
As an automation engineer, I want deterministic file naming so that pipelines can reliably locate generated artifacts.

### Description
Specify naming conventions for socio-technical JSON outputs including window mode/date scope markers.

### Acceptance criteria
- Default output naming documented.
- Naming deterministic for identical inputs.
- Custom output path overrides default safely.

### Dependencies
- ORG-025
- ORG-028

### Priority
P1

### Notes / technical hints
- Include sanitized repository/system identifiers in file name.

---

### Story ID
ORG-039

### Title
Document formulas, assumptions, and interpretation guidance

### User story statement
As a user of organizational metrics, I want clear documentation so that I can interpret results correctly.

### Description
Add docs covering PTC/AOC/roles/RSI definitions, assumptions, normalization rules, and caveats.

### Acceptance criteria
- Documentation added under `docs/` with examples.
- Includes guidance for sparse data and edge cases.
- References configuration options affecting results.

### Dependencies
- ORG-015
- ORG-017
- ORG-022
- ORG-031

### Priority
P1

### Notes / technical hints
- Include “what this metric is not” notes to reduce misuse.

---

### Story ID
ORG-040

### Title
Add user guide for runner usage and migration path

### User story statement
As an existing CIMET user, I want migration guidance so that I can adopt organizational analysis without disrupting current workflows.

### Description
Document runner commands, config examples, backward compatibility guarantees, and integration with existing outputs.

### Acceptance criteria
- Quick-start commands included.
- Config templates for full-history and windowed modes included.
- Backward compatibility section explicitly states unchanged legacy flow.

### Dependencies
- ORG-028
- ORG-029
- ORG-035

### Priority
P1

### Notes / technical hints
- Add troubleshooting section for common configuration/mapping errors.

---

## Epic 9: Research Spikes and Risk Reduction

### Story ID
ORG-041

### Title
Spike metric formula calibration against reference examples

### User story statement
As a research engineer, I want to calibrate PTC/NOC/AOC/role/RSI formulas so that implementations remain faithful to the conceptual model.

### Description
Run a focused spike to validate formula interpretation, normalization ranges, and sensitivity using controlled fixtures and paper-aligned scenarios.

### Acceptance criteria
- Formula assumptions and alternatives documented.
- Recommended default formulas selected and justified.
- Follow-up implementation notes captured for metric modules.

### Dependencies
- ORG-015
- ORG-016
- ORG-017
- ORG-022

### Priority
P0

### Notes / technical hints
- Prefer examples that expose edge behavior (sparse and highly skewed contributions).

---

### Story ID
ORG-042

### Title
Spike identity normalization quality

### User story statement
As a maintainer, I want to evaluate alias-merging quality so that role and ownership results are trustworthy.

### Description
Measure false-merge/false-split behavior of identity rules across representative author datasets.

### Acceptance criteria
- Quality criteria defined for merge/split errors.
- Ambiguous identity patterns cataloged.
- Resolver rule adjustments proposed.

### Dependencies
- ORG-008
- ORG-009

### Priority
P1

### Notes / technical hints
- Include no-reply and renamed-email scenarios.

---

### Story ID
ORG-043

### Title
Spike switching-signal design for NOC

### User story statement
As an architect, I want to compare switching formulations so that pairwise coupling is stable and interpretable.

### Description
Evaluate commit-count-based vs LOC-weighted switching signals and pick defaults per metric metadata.

### Acceptance criteria
- Comparison results documented.
- Selected default and rationale recorded.
- Backward-compatible metadata version strategy defined.

### Dependencies
- ORG-016

### Priority
P1

### Notes / technical hints
- Validate with both highly coupled and weakly coupled synthetic systems.

---

### Story ID
ORG-044

### Title
Spike incremental/windowed performance strategy

### User story statement
As an operator, I want a scaling strategy so that large histories remain practical to process.

### Description
Prototype incremental and window-cached processing patterns and estimate performance impact.

### Acceptance criteria
- Performance bottlenecks identified and measured.
- Candidate optimization path documented.
- Follow-up backlog tasks identified for chosen approach.

### Dependencies
- ORG-011
- ORG-013
- ORG-036

### Priority
P2

### Notes / technical hints
- Keep spike scoped to measurement and architecture recommendation.

---

### Story ID
ORG-045

### Title
Spike schema evolution and migration policy

### User story statement
As an integrator, I want a schema evolution policy so that downstream consumers can upgrade safely.

### Description
Define compatibility levels, versioning rules, and migration guidance for socio-technical JSON schema changes.

### Acceptance criteria
- Versioning policy documented with examples.
- Breaking vs non-breaking change criteria defined.
- Migration note template created for future changes.

### Dependencies
- ORG-026
- ORG-027

### Priority
P2

### Notes / technical hints
- Keep policy aligned with `docs/example_ir.json` compatibility target.

---

## Suggested implementation order

1. ORG-001, ORG-002, ORG-006, ORG-008, ORG-010
2. ORG-011, ORG-012, ORG-013, ORG-014
3. ORG-015, ORG-016, ORG-017
4. ORG-018, ORG-019, ORG-020, ORG-021, ORG-022
5. ORG-024, ORG-025, ORG-026, ORG-027
6. ORG-028, ORG-029, ORG-030, ORG-031
7. ORG-032, ORG-033, ORG-034, ORG-035
8. ORG-003, ORG-004, ORG-007, ORG-009, ORG-005, ORG-023, ORG-036
9. ORG-037, ORG-038, ORG-039, ORG-040
10. ORG-041, ORG-042, ORG-043, ORG-044, ORG-045

Rationale:
- Build deterministic data foundation first.
- Add windowing and core metrics before role stacking.
- Lock schema/export contract before extensive docs and reporting.
- Enforce regression protection before broad rollout.


## Data model implementation stories

### Story ID
ORG-046

### Title
Define organizational model class contracts

### User story statement
As an architect, I want concrete organizational model contracts so that implementation and serialization are consistent across modules.

### Description
Define model contracts for developers, commits, contributor-service relations, service metrics, pair relations, and windows with required fields and ID rules.

### Acceptance criteria
- Contract document embedded in code/docs with field-level definitions.
- Required/optional fields are explicit.
- Field names align with `docs/example_ir.json` compatibility requirements.

### Dependencies
- ORG-024
- ORG-026

### Priority
P0

### Notes / technical hints
- Keep additive semantics to avoid breaking existing technical IR usage.

---

### Story ID
ORG-047

### Title
Implement contributor-service relationship entity

### User story statement
As a data engineer, I want an explicit contributor-service relation entity so that ownership and role calculations are explainable.

### Description
Implement `contributorServiceRelations[]` with developer, service, window, share, and supporting counters/churn fields.

### Acceptance criteria
- Relation entity serialized into organizational IR.
- Derived ownership can be reconstructed from relation rows.
- Relation rows are deterministic and deduplicated.

### Dependencies
- ORG-014
- ORG-027

### Priority
P0

### Notes / technical hints
- Include `isPrimaryOwner` flag for quick reporting queries.

---

### Story ID
ORG-048

### Title
Implement service organizational metrics entity

### User story statement
As an engineering manager, I want a service metrics entity so that service-level organizational risk can be consumed directly.

### Description
Implement `serviceOrganizationalMetrics[]` storing per-service-window ownership concentration, PTC, active contributors, and metric metadata.

### Acceptance criteria
- Metrics entity includes window linkage.
- PTC and ownership concentration represented consistently.
- Metric metadata records formula/version info.

### Dependencies
- ORG-015
- ORG-023

### Priority
P1

### Notes / technical hints
- Keep both human-readable and machine-friendly metric keys.

---

### Story ID
ORG-049

### Title
Implement service-pair coupling entity with compatibility aliasing

### User story statement
As an integrator, I want pairwise coupling represented with both NOC and compatibility fields so that old and new consumers both work.

### Description
Add pair entity fields `noc` and `organizationalCoupling` (compat alias), plus `switchingScore` and `overlapScore`.

### Acceptance criteria
- Pair relation contains both `noc` and `organizationalCoupling`.
- `organizationalCoupling` remains available for `example_ir.json` compatibility.
- Values and ranges validated by tests.

### Dependencies
- ORG-016
- ORG-026

### Priority
P1

### Notes / technical hints
- Document if/when alias fields diverge in future schema versions.

---

### Story ID
ORG-050

### Title
Implement temporal analysis window entity

### User story statement
As a researcher, I want explicit window entities so that longitudinal results are queryable and reproducible.

### Description
Add `analysisWindows[]` with boundaries, mode, timezone, aggregate AOC, and count metadata.

### Acceptance criteria
- Window entities emitted for all computed windows.
- Commit/window linkage is explicit and testable.
- Timezone and boundary conventions are persisted.

### Dependencies
- ORG-011
- ORG-012

### Priority
P1

### Notes / technical hints
- Persist `windowId` references in commit and metric entities.

---

### Story ID
ORG-051

### Title
Implement role profile history with RSI by window

### User story statement
As a lead, I want role profiles and RSI per window so that developer role dynamics over time are visible.

### Description
Extend developer representation with per-window role profiles, including Jack/Maven/Connector scores and RSI.

### Acceptance criteria
- Developer output includes current roles and windowed role history.
- RSI is available both in current-role view and windowed view.
- Role labels and raw scores remain available together.

### Dependencies
- ORG-021
- ORG-022

### Priority
P1

### Notes / technical hints
- Keep compact current snapshot and detailed history to balance size/readability.

---

### Story ID
ORG-052

### Title
Publish illustrative organizational IR instance and validation checks

### User story statement
As a maintainer, I want a realistic sample IR instance so that developers and consumers can validate field usage quickly.

### Description
Provide and maintain an illustrative `proposed-organizational-ir.json` plus tests/checks that verify expected key paths and field semantics.

### Acceptance criteria
- Sample instance file exists under `docs/`.
- Sample includes developers, contributor-service relations, service metrics, pair relations, windows, and traceability edges.
- Contract checks verify compatibility with `example_ir.json` core shape.

### Dependencies
- ORG-026
- ORG-034

### Priority
P1

### Notes / technical hints
- Keep sample small but representative enough for integration tests.


## Test coverage stories

### Story ID
ORG-053

### Title
Implement organizational unit test suite

### User story statement
As a developer, I want a comprehensive unit suite so that core organizational algorithms are stable and deterministic.

### Description
Add unit tests for extraction transforms, mapping, identity normalization, windowing, and metric helper utilities.

### Acceptance criteria
- Unit tests cover all core organizational modules.
- Determinism and invariant checks are included.
- CI executes the suite on every PR.

### Dependencies
- ORG-046
- ORG-047
- ORG-050

### Priority
P0

### Notes / technical hints
- Add shared assertion helpers for numeric tolerance and sorting determinism.

---

### Story ID
ORG-054

### Title
Implement golden JSON export and contract tests

### User story statement
As an integrator, I want golden and contract tests so that export shape and compatibility remain stable.

### Description
Introduce golden snapshot tests plus required-key contract checks for exported organizational IR artifacts.

### Acceptance criteria
- Golden snapshots exist for representative fixtures.
- Contract checks validate required top-level and compatibility keys.
- Golden update workflow is documented.

### Dependencies
- ORG-026
- ORG-052

### Priority
P0

### Notes / technical hints
- Verify both `noc` and `organizationalCoupling` in pair entities.

---

### Story ID
ORG-055

### Title
Implement metric correctness and oracle tests

### User story statement
As a maintainer, I want metric oracle tests so that formula regressions are detected immediately.

### Description
Create exact-value and tolerance-based tests for PTC, OC/NOC/AOC, Jack, Maven, Connector, and RSI.

### Acceptance criteria
- All metric families have scenario-based correctness tests.
- Invariant checks (symmetry, ranges, aggregation) are included.
- Failing tests provide readable expected vs actual breakdown.

### Dependencies
- ORG-015
- ORG-016
- ORG-017
- ORG-021
- ORG-022

### Priority
P0

### Notes / technical hints
- Keep oracle fixtures minimal and deterministic.

---

### Story ID
ORG-056

### Title
Implement service mapping and longitudinal window test suites

### User story statement
As a researcher, I want mapping and window tests so that longitudinal analyses are reproducible and correct.

### Description
Add tests for longest-prefix mapping, global file policies, window boundaries, timezone handling, and per-window metric consistency.

### Acceptance criteria
- Mapping edge cases and window boundary cases are covered.
- Timezone and boundary semantics are explicitly asserted.
- Per-window counts/aggregates are internally consistent.

### Dependencies
- ORG-006
- ORG-011
- ORG-012

### Priority
P1

### Notes / technical hints
- Include DST-adjacent timestamps in fixture cases.

---

### Story ID
ORG-057

### Title
Implement regression, malformed-data, and performance smoke suites

### User story statement
As a release owner, I want hardening suites so that new organizational features do not break legacy CIMET behavior or fail on bad inputs.

### Description
Add regression tests for legacy CIMET behavior, malformed/edge input tests, and performance smoke checks for large-ish synthetic histories.

### Acceptance criteria
- Legacy mode regression suite passes unchanged.
- Malformed input cases produce expected failures or graceful handling.
- Performance smoke thresholds and reports are defined and executed.

### Dependencies
- ORG-035
- ORG-036

### Priority
P1

### Notes / technical hints
- Separate performance smoke from full benchmark work.