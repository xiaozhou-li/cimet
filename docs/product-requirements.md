# Product Requirements Document: Organizational Structure Analysis for CIMET


## Terminology Contract

This document follows [`planning-glossary.md`](./planning-glossary.md) for mandatory terminology.

Operational guardrails:
- Distinguish **technical coupling** from **organizational coupling** in every section.
- Distinguish **cohesion (within-service)** from **coupling (between-service)** explicitly.
- Use role semantics consistently: **Jack=breadth**, **Maven=depth**, **Connector=brokerage**, **RSI=role co-occurrence intensity**.
- Treat `organizationalCoupling` as compatibility alias and `noc` as explicit pairwise normalized value where both appear.


## 1. Problem statement

CIMET currently analyzes microservice evolution primarily through technical structure (IR, Delta, merge, antipatterns, and metrics) but lacks first-class organizational analysis. Teams therefore cannot assess how developer contribution patterns, ownership concentration, cross-service work overlap, and key human roles (Jack/Maven/Connector) co-evolve with system architecture.

The product gap is the absence of a reliable, pipeline-aligned module that transforms version-control history into organizational signals and exports them in a machine-consumable IR format.

## 2. Background and rationale

CIMET already provides a stable staged pipeline:
- Config input and repository acquisition.
- Technical IR extraction.
- Delta extraction and IR merge.
- Detection/metrics and reporting output.

This extension must preserve that pipeline and add an organizational lens, not replace existing technical analysis.

Rationale:
- Organizational properties are operationally important and distinct from technical structural quality signals; in this extension, cohesion (PTC) is within-service while coupling (NOC/AOC) is between-service.
- PTC, AOC, and role measures expose collaboration risks not visible in code structure alone.
- A socio-technical JSON artifact enables downstream dashboards, trend analysis, and governance workflows.

## 3. Goals

| ID | Goal |
|---|---|
| G1 | Add organizational analysis as a pipeline-compatible module that reuses existing CIMET architecture. |
| G2 | Compute service-level ownership and contributor mapping from Git history. |
| G3 | Compute Pairwise Team Cohesion (PTC) per service and window. |
| G4 | Compute Normalized Organizational Coupling / Average Organizational Coupling (AOC) for service pairs and aggregates. |
| G5 | Identify Jack, Maven, Connector roles and compute Role Stacking Index (RSI). |
| G6 | Support configurable temporal windows for longitudinal analysis. |
| G7 | Export a socio-technical IR JSON compatible with `docs/example_ir.json` when possible. |

## 4. Non-goals

- Replace or redesign existing IR/Delta/Merge architecture.
- Rewrite current detection/metrics modules.
- Introduce mandatory external issue-tracker integration in v1.
- Provide a new UI application in v1.
- Guarantee exact parity with every possible custom schema variant beyond documented compatibility target.

## 5. Target users

| User | Needs |
|---|---|
| Software architects | Understand socio-technical alignment and cross-service ownership patterns. |
| Tech leads / engineering managers | Identify risk from concentrated ownership, silos, or unstable collaboration patterns. |
| Researchers | Reproducible socio-technical datasets over commit history. |
| Platform/tooling engineers | Consume JSON outputs in automation and dashboards. |

## 6. Core use cases

1. Analyze a repository over full history and export socio-technical IR JSON.
2. Analyze a trailing window (for example, last 90 days) for current organizational risk snapshot.
3. Compare organizational metrics across windows/releases to detect drift.
4. Identify services with low cohesion (PTC) and high organizational coupling (AOC).
5. Identify key developers by role (Jack/Maven/Connector) and role stacking (RSI).
6. Trace developer -> commit -> file -> microservice links for audit and explainability.

## 7. Functional requirements

### 7.1 Pipeline integration

- FR-1: The extension shall run as an additive stage in CIMET’s pipeline.
- FR-2: The extension shall reuse existing config loading and repository acquisition.
- FR-3: The extension shall reuse technical IR outputs (service names/paths) for file-to-service mapping.
- FR-4: Existing technical analysis outputs shall remain unaffected unless explicitly configured to co-generate socio-technical output.

### 7.2 Contribution ingestion

- FR-5: System shall ingest commit metadata: commit ID, author name/email, timestamp, touched files.
- FR-6: System shall ingest file-level change size (added/deleted LOC) when available.
- FR-7: System shall normalize developer identities using deterministic alias resolution.
- FR-8: System shall persist unresolved alias collisions for traceability.

### 7.3 Service-level ownership and contributor mapping

- FR-9: System shall map touched files to microservices using existing CIMET path conventions.
- FR-10: System shall compute per-service contributor sets and weighted contribution shares.
- FR-11: System shall compute ownership concentration per service.
- FR-12: System shall identify primary developers per service using configurable threshold/top-k logic.

### 7.4 PTC

- FR-13: System shall compute PTC for each service per temporal window.
- FR-14: PTC computation shall reflect focused and balanced contributor participation for that service.
- FR-15: PTC formula implementation shall be encapsulated and versioned (to allow updates without data model break).

### 7.5 AOC

- FR-16: System shall compute pairwise organizational coupling for service pairs per temporal window.
- FR-17: Pairwise coupling shall include developer overlap and switching behavior signals.
- FR-18: System shall compute aggregated AOC at system level from pairwise values.
- FR-19: Coupling values shall be normalized to a common scale (for example [0,1]).

### 7.6 Roles and RSI

- FR-20: System shall compute Jack score (breadth), Maven score (depth), Connector score (brokerage) per developer per window.
- FR-21: System shall classify developers into role labels using configurable thresholds.
- FR-22: System shall compute Role Stacking Index (RSI) to capture co-occurrence of role characteristics.
- FR-23: System shall output both continuous scores and derived labels.

### 7.7 Temporal analysis

- FR-24: System shall support full-history and windowed analysis.
- FR-25: Window modes shall include at least fixed trailing duration and fixed calendar bucket (e.g., monthly).
- FR-26: Window definitions and timezone handling shall be explicit and persisted in output metadata.

### 7.8 Explainability and traceability

- FR-27: Every metric result shall be traceable to underlying contributions via relation tables.
- FR-28: Output shall include developer->commit, commit->file, commit->issue (if inferred), developer->microservice links.

## 8. Non-functional requirements

| ID | Requirement |
|---|---|
| NFR-1 | **Determinism**: same inputs/config produce identical output ordering and values. |
| NFR-2 | **Scalability**: support repositories with large histories without requiring full in-memory raw diff retention. |
| NFR-3 | **Performance**: windowed runs should avoid recomputation where possible (incremental/cache-ready design). |
| NFR-4 | **Backward compatibility**: existing technical pipeline behavior and outputs remain valid. |
| NFR-5 | **Observability**: structured logs for ingestion, identity resolution, metric computation, export. |
| NFR-6 | **Robustness**: graceful handling of missing/ambiguous author identity, deleted files, and unmapped files. |
| NFR-7 | **Testability**: formulas and mapping logic independently unit-testable with fixed fixtures. |

## 9. Data model requirements

### 9.1 New model families

Introduce a socio-technical top-level model with these sections:
- `technical` (embedded or referenced existing `MicroserviceSystem` content).
- `organizational` (developers, commits, issues, serviceOwnership, servicePairRelations, metric summaries).
- `traceability` (explicit relation edges).

### 9.2 Required entities

| Entity | Required fields |
|---|---|
| Developer | `id`, `name`, `aliases[]`, role scores (`jack`, `maven`, `connector`, `roleStackingIndex`) |
| CommitContribution | `id`, `author`, `timestamp`, `touchedFiles[]`, `touchedMicroservices[]`, `changeSize{addedLOC,deletedLOC}` |
| ServiceOwnership | `microservice`, `primaryDevelopers[]`, `ownershipConcentration`, `teamCohesion` |
| ServicePairRelation | `source`, `target`, `organizationalCoupling`, `sharedDevelopers[]`, `switchingScore` |
| Traceability edges | `developerToCommit[]`, `commitToIssue[]`, `commitToFile[]`, `developerToMicroservice[]` |

### 9.3 Modeling constraints

- Preserve existing technical IR model classes where possible.
- Add new classes under an organizational namespace/package.
- Keep formula-specific intermediate values optional and namespaced to avoid schema churn.
- Include schema/version metadata in output.

## 10. JSON export requirements

- JER-1: Export format shall be JSON via existing `JsonReadWriteUtils` path.
- JER-2: Output shall be compatible with `docs/example_ir.json` field layout when possible.
- JER-3: Top-level should include: `type`, `name`, `technical`, `organizational`, `traceability`.
- JER-4: Unknown/unavailable optional sections (e.g., issues) may be emitted as empty arrays.
- JER-5: Output shall include provenance metadata:
  - source repository
  - analyzed commit range/window
  - generation timestamp
  - schema version
- JER-6: Output file naming shall support deterministic and window-aware names.

## 11. CLI/config requirements

### 11.1 CLI

Minimum CLI options for new organizational runner:

| Option | Description |
|---|---|
| `--config <path>` | Required CIMET config path. |
| `--output <path>` | Optional output JSON path. |
| `--window-mode <full|trailing|calendar>` | Analysis mode. |
| `--window-size <duration>` | Required for trailing mode (e.g., `90d`). |
| `--window-step <duration>` | Optional longitudinal step size. |
| `--from <iso-date>` / `--to <iso-date>` | Optional explicit date bounds. |
| `--role-threshold-profile <name|path>` | Optional role classification thresholds. |
| `--ownership-threshold <float>` | Optional primary owner threshold. |
| `--include-issues <true|false>` | Optional issue-link inference toggle. |

### 11.2 Config file additions

Add optional organizational block in config:

```json
{
  "organizational": {
    "enabled": true,
    "windowMode": "trailing",
    "windowSize": "90d",
    "windowStep": "30d",
    "timezone": "UTC",
    "ownership": {
      "primaryThreshold": 0.2,
      "topK": 3
    },
    "roles": {
      "jackThreshold": 0.7,
      "mavenThreshold": 0.7,
      "connectorThreshold": 0.7
    },
    "issueLinking": {
      "enabled": false
    }
  }
}
```

Rules:
- Missing organizational config uses safe defaults.
- CLI flags override config values.

## 12. Reporting requirements

- RR-1: JSON IR is mandatory artifact for organizational analysis.
- RR-2: Provide concise summary log/report containing:
  - number of developers, commits, mapped files
  - services with highest/lowest PTC
  - service pairs with highest coupling
  - top Jack/Maven/Connector developers
  - RSI distribution summary
- RR-3: Enable optional integration into current Excel pipeline in a later phase (not required for initial release).
- RR-4: Document metric definitions and configuration used in report metadata.

## 13. Risks, assumptions, dependencies

### Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Developer alias fragmentation | Incorrect role/ownership attribution | Alias resolver + optional manual mapping file |
| Global/shared file commits | Distorted service ownership | configurable weighting/exclusion rules |
| Formula interpretation drift | Inconsistent results across versions | versioned formula module + regression fixtures |
| Large history size | Slow runs, high memory | streaming/incremental processing and windowing |
| Runner inconsistencies in existing code | adoption friction | new clean runner contract for organizational module |

### Assumptions

- Git history is available and complete for analysis scope.
- Existing service path mapping from technical IR is sufficiently accurate for first release.
- `docs/example_ir.json` is the compatibility target shape.

### Dependencies

- Existing CIMET config, Git, IR extraction, and JSON utility components.
- JGit commit/diff data availability.
- Finalized metric formula definitions from project papers/docs.

## 14. Open questions

1. What exact normalization formulas/weights should be used for PTC and AOC in v1?
2. Should switching behavior in AOC be commit-count based, LOC-weighted, or both?
3. What default thresholds classify Jack/Maven/Connector roles across heterogeneous repos?
4. Should RSI be continuous-only or also binned into categories?
5. How should commits touching no mapped microservice be represented in traceability?
6. What is the canonical order and stability guarantees for exported arrays (ID sort vs score sort)?
7. Should issue linking remain heuristic-only in v1, or include optional GitHub connector integration?

## 15. Acceptance criteria

### 15.1 Functional acceptance

- AC-1: Running organizational analysis with a valid CIMET config produces a socio-technical JSON artifact.
- AC-2: Artifact contains required top-level sections and fields compatible with `docs/example_ir.json` layout.
- AC-3: Artifact includes service ownership mapping and contributor shares per analyzed window.
- AC-4: Artifact includes PTC per service and AOC (pairwise and aggregate).
- AC-5: Artifact includes Jack/Maven/Connector scores and RSI per developer.
- AC-6: Artifact includes traceability relations linking developers, commits, files, and microservices.
- AC-7: Windowed runs produce different metrics when time bounds differ and include explicit window metadata.

### 15.2 Quality acceptance

- AC-8: Existing technical pipeline outputs (IR/Delta/merge/detection) remain unchanged by default.
- AC-9: Deterministic reruns under identical input/config produce identical metric values and ordering.
- AC-10: Unit tests cover ownership mapping, role scoring, RSI, and PTC/AOC computation with fixed fixtures.
- AC-11: Integration test validates schema compatibility and JSON serialization/deserialization path.

### 15.3 Operational acceptance

- AC-12: CLI usage and config options are documented in project docs.
- AC-13: Logs summarize ingestion counts, alias merges, mapping coverage, and metric completion status.
- AC-14: Failures due to malformed config or missing repo data are surfaced with actionable error messages.


## 16. Proposed Organizational IR Schema

This section proposes a concrete **instance-oriented IR shape** (not JSON Schema) aligned with current CIMET design:
- preserve existing technical IR semantics (`MicroserviceSystem` and `Microservice` content),
- keep additive organizational sections,
- use deterministic IDs and explicit traceability edges,
- remain compatible with `docs/example_ir.json` field expectations.

### 16.1 Design alignment with current CIMET architecture

1. **Additive embedding of technical IR**
- Keep current technical model under `technical` without modifying legacy technical extraction/merge logic.
- Existing fields (`name`, `commitID`, `microservices`, `orphans`) remain valid in embedded technical snapshot.

2. **Serialization compatibility**
- New organizational model classes should follow existing `JsonSerializable#toJsonObject()` pattern.
- Export should continue through `JsonReadWriteUtils.writeToJSON(...)`.

3. **Traceability-first structure**
- Every aggregate metric should be derivable from link tables in `traceability`.

### 16.2 Proposed top-level structure

```json
{
  "type": "SocioTechnicalMicroserviceSystem",
  "schemaVersion": "1.0.0",
  "name": "<system-name>",
  "technical": { "...": "embedded CIMET technical IR" },
  "organizational": { "...": "organizational entities + metrics" },
  "traceability": { "...": "relationship edges" },
  "metadata": { "...": "analysis provenance" }
}
```

### 16.3 Organizational entities and required fields

#### A) `organizational.developers[]`
Represents normalized developer identity and role signals.

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable synthetic ID (e.g., `dev_001`). |
| `name` | string | Preferred canonical display name. |
| `aliases` | string[] | Name/email aliases used in commits. |
| `roles.jack` | number | Breadth role score in [0,1]. |
| `roles.maven` | number | Depth role score in [0,1]. |
| `roles.connector` | number | Brokerage role score in [0,1]. |
| `roles.roleStackingIndex` | number | RSI score in [0,1]. |
| `roles.labels` | string[] | Optional derived labels (`jack`, `maven`, `connector`). |

#### B) `organizational.commits[]`
Commit-level contribution facts.

| Field | Type | Notes |
|---|---|---|
| `id` | string | Commit hash or shortened stable ID. |
| `author` | string | Developer ID reference (`developers[].id`). |
| `timestamp` | string | ISO-8601 UTC timestamp. |
| `issues` | string[] | Optional linked issue/PR IDs. |
| `touchedMicroservices` | string[] | Service names touched by mapped files. |
| `touchedFiles` | string[] | Repo-relative file paths. |
| `changeSize.addedLOC` | integer | Added LOC. |
| `changeSize.deletedLOC` | integer | Deleted LOC. |
| `windowIds` | string[] | Windows this commit belongs to (usually one). |

#### C) `organizational.contributorServiceRelations[]`
Explicit contributor-to-service relationship (required for ownership).

| Field | Type | Notes |
|---|---|---|
| `developerId` | string | FK to developer. |
| `microservice` | string | FK by service name. |
| `windowId` | string | FK to analysis window. |
| `contributionWeight` | number | Weighted contribution value (config-dependent). |
| `contributionShare` | number | Share within service-window in [0,1]. |
| `commitCount` | integer | Supporting count. |
| `addedLOC` | integer | Supporting churn metric. |
| `deletedLOC` | integer | Supporting churn metric. |
| `isPrimaryOwner` | boolean | Derived by threshold/top-k policy. |

#### D) `organizational.serviceOrganizationalMetrics[]`
Service-level organizational metrics.

| Field | Type | Notes |
|---|---|---|
| `microservice` | string | Service identifier. |
| `windowId` | string | Window reference. |
| `ownershipConcentration` | number | Concentration score in [0,1]. |
| `ptc` | number | Pairwise Team Cohesion score in [0,1]. |
| `primaryDevelopers` | string[] | Owner developer IDs. |
| `activeContributors` | integer | Number of active contributors. |
| `metricMetadata` | object | Formula/normalization/exclusion notes. |

#### E) `organizational.servicePairRelations[]`
Pairwise organizational coupling at service level.

| Field | Type | Notes |
|---|---|---|
| `source` | string | Source service. |
| `target` | string | Target service. |
| `windowId` | string | Window reference. |
| `noc` | number | Normalized Organizational Coupling (pairwise) in [0,1]. |
| `organizationalCoupling` | number | Alias/compat field (same as pairwise NOC for `example_ir.json` compatibility). |
| `sharedDevelopers` | string[] | Shared developer IDs. |
| `switchingScore` | number | Cross-service switching signal in [0,1]. |
| `overlapScore` | number | Developer overlap component in [0,1]. |

#### F) `organizational.analysisWindows[]`
Temporal windows for longitudinal analysis.

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable window ID (e.g., `w_2025Q1`). |
| `mode` | string | `full`, `trailing`, or `calendar`. |
| `start` | string | ISO-8601 boundary start. |
| `end` | string | ISO-8601 boundary end. |
| `timezone` | string | E.g., `UTC`. |
| `aoc` | number | Aggregate AOC for this window. |
| `commitCount` | integer | Commits in window. |
| `developerCount` | integer | Active developers in window. |

### 16.4 Traceability section

`traceability` should include explicit edges:
- `developerToCommit[]`
- `commitToIssue[]`
- `commitToFile[]`
- `developerToMicroservice[]`
- optional `windowToCommit[]`, `windowToService[]` for efficient downstream queries.

### 16.5 Compatibility with `docs/example_ir.json`

1. Preserve required existing keys: `type`, `name`, `technical`, `organizational`, `traceability`.
2. Keep `organizational.serviceOwnership[]` and `organizational.servicePairRelations[]` in compatible shape.
3. Add richer structures (`contributorServiceRelations`, `serviceOrganizationalMetrics`, `analysisWindows`) as additive fields.
4. Keep `organizationalCoupling` field present for compatibility even when `noc` is introduced.

### 16.6 Implementation notes for CIMET codebase

- New classes should be added under `edu.university.ecs.lab.organizational.models`.
- Export should be performed through existing JSON utility (`JsonReadWriteUtils`).
- Prefer deterministic ordering for arrays by IDs to stabilize diffs and tests.