# CIMET Reconnaissance for Organizational Analysis Extension


## Terminology Contract

This document follows [`planning-glossary.md`](./planning-glossary.md) for mandatory terminology.

Operational guardrails:
- Distinguish **technical coupling** from **organizational coupling** in every section.
- Distinguish **cohesion (within-service)** from **coupling (between-service)** explicitly.
- Use role semantics consistently: **Jack=breadth**, **Maven=depth**, **Connector=brokerage**, **RSI=role co-occurrence intensity**.
- Treat `organizationalCoupling` as compatibility alias and `noc` as explicit pairwise normalized value where both appear.


## Concise architecture summary of current CIMET codebase

CIMET is implemented as a staged pipeline with reusable shared layers:

1. **Config + repository acquisition**
- `Config`/`ConfigUtil` parse JSON config (`systemName`, `repositoryURL`, `branch`).
- `GitService` clones and resets the local repo snapshot used by extraction (`src/main/java/edu/university/ecs/lab/common/services/GitService.java`).

2. **IR extraction**
- `IRExtractionService` discovers service roots (`pom.xml`/`build.gradle`), parses Java/config files, and builds `MicroserviceSystem` (`src/main/java/edu/university/ecs/lab/intermediate/create/services/IRExtractionService.java`).
- IR core model is in `common.models.ir` (`MicroserviceSystem`, `Microservice`, `JClass`, `Method`, `MethodCall`, `ConfigFile`, etc.).

3. **Delta extraction**
- `DeltaExtractionService` computes commit-to-commit file changes via JGit and serializes `SystemChange` composed of `Delta` entries (`src/main/java/edu/university/ecs/lab/delta/services/DeltaExtractionService.java`).

4. **Merge**
- `MergeService` applies Delta onto prior IR, including microservice boundary updates and orphan adoption/orphanization (`src/main/java/edu/university/ecs/lab/intermediate/merge/services/MergeService.java`).

5. **Detection + reporting**
- `DetectionService` iterates commit history and runs anti-pattern + metric analysis, then writes Excel output (`src/main/java/edu/university/ecs/lab/detection/DetectionService.java`).
- Metric/anti-pattern modules are under `detection.metrics` and `detection.antipatterns`.
- There is also a Freemarker HTML template (`src/main/resources/templates/report.ftl`), while the active integrated output path today is Excel + JSON artifacts.

Major packages:
- `edu.university.ecs.lab.common`
- `edu.university.ecs.lab.intermediate.create`
- `edu.university.ecs.lab.delta`
- `edu.university.ecs.lab.intermediate.merge`
- `edu.university.ecs.lab.detection`

## Existing IR models and JSON export logic

Current technical IR schema centers on:
- `MicroserviceSystem { name, commitID, microservices[], orphans[] }`
- `Microservice { name, path, controllers[], services[], repositories[], entities[], feignClients[], files[] }`
- `JClass`/`ConfigFile` subclasses of `ProjectFile`.

JSON serialization patterns:
- Model-owned serialization via `toJsonObject()` (implementing `JsonSerializable`).
- Disk I/O via `JsonReadWriteUtils.writeToJSON(...)` and `readFromJSON(...)`.
- Custom deserializers registered for polymorphic IR types (`Method`, `MethodCall`, `ProjectFile`) in `JsonReadWriteUtils.registerDeserializers()`.

## Existing metric/detection/reporting modules

1. **Anti-pattern detection** (`detection.antipatterns.services`)
- Greedy, Hub-like, Service Chain, Wrong Cuts, Cyclic Dependency, Wobbly Service Interaction, No Healthcheck, No API Gateway.

2. **Architecture-rule detection** (`detection.architecture`)
- AR models (`AR1`, `AR2`, ... `AR24`) with `ARDetectionService`.

3. **Metrics** (`detection.metrics`)
- Coupling/modularity metrics (`DegreeCoupling`, `StructuralCoupling`, `ConnectedComponentsModularity`).
- Cohesion metrics (`RunCohesionMetrics`, `MetricCalculator`, SIDC/SSIC/LOMLC families).

4. **Reporting/output**
- Excel history report from `DetectionService`/`ExcelOutputRunner`.
- JSON outputs for IR/Delta and standalone detection outputs in `./output`.

## example_ir.json existence and structure

Status:
- `example_ir.json` **exists in repo** at `docs/example_ir.json`.
- No `example_ir.json` exists at repo root.

Observed structure (template-style, not a strict machine-ready sample):
- Top-level:
  - `type` (expected `SocioTechnicalMicroserviceSystem`)
  - `name`
  - `technical`
  - `organizational`
  - `traceability`
- `technical.microservices`: placeholder for existing CIMET IR microservices.
- `organizational` includes:
  - `developers[]` with aliases + role scores (`jack`, `maven`, `connector`, `roleStackingIndex`)
  - `commits[]` with author/time/touched services/files/change size
  - `issues[]`
  - `serviceOwnership[]` (ownership concentration + team cohesion)
  - `servicePairRelations[]` (organizational coupling + shared developers + switching score)
- `traceability` includes relation tables (`developerToCommit`, `commitToIssue`, `commitToFile`, `developerToMicroservice`).

## Extension points

1. **Data acquisition extension point**
- Extend `GitService` (or add companion service) to extract per-commit metadata needed for organizational analysis:
  - author identity (name/email)
  - timestamp
  - touched files
  - per-file added/deleted LOC
  - optional issue/PR IDs from commit messages.

2. **IR extension point (non-breaking)**
- Keep existing `MicroserviceSystem` unchanged for technical analysis.
- Introduce a new composite socio-technical IR object that embeds current technical IR and adds organizational fields.

3. **Pipeline insertion point**
- Add an organizational analysis stage after IR extraction (or after merge for history mode), because ownership/PTC/AOC depend on repository contribution history and service-file mapping.

4. **Detection layer integration point**
- Mirror existing `detection.*` pattern with `detection.organizational` package and a dedicated runner/service.

5. **Output/export point**
- Reuse `JsonReadWriteUtils` for a new organizational IR JSON artifact.
- Optionally integrate summary columns into Excel later, but JSON should be first-class output.

## Proposed organizational-analysis module layout

Recommended package layout (preserving current architecture):

- `edu.university.ecs.lab.organizational.models`
  - `SocioTechnicalMicroserviceSystem`
  - `OrganizationalSection`
  - `DeveloperProfile`
  - `CommitContribution`
  - `IssueArtifact`
  - `ServiceOwnership`
  - `ServicePairRelation`
  - `TraceabilitySection`
  - typed edge records (developer->commit, commit->file, etc.)

- `edu.university.ecs.lab.organizational.extract`
  - `ContributionExtractionService`
  - `DeveloperIdentityResolver` (alias merge: name/email/noreply)
  - `IssueLinkExtractor` (regex for `#123`, `PR #...`, etc.; conservative)

- `edu.university.ecs.lab.organizational.metrics`
  - `OwnershipMapper` (contribution-based service ownership)
  - `PTCCalculator` (Pairwise Team Cohesion)
  - `NOCCalculator` (Normalized Organizational Coupling for service pairs)
  - `AOCCalculator` (Average Organizational Coupling aggregate over pairwise NOC)
  - `KeyDeveloperRoleScorer` (`jack`, `maven`, `connector`)
  - `RSICalculator` (Role Stacking Index)

- `edu.university.ecs.lab.organizational.services`
  - `OrganizationalAnalysisService` (orchestrates extraction + metric calculation + assembly)

- `edu.university.ecs.lab.organizational.output`
  - `OrganizationalIRExportService` (writes IR-style JSON with example schema compatibility)

- `edu.university.ecs.lab.organizational.runner`
  - `OrganizationalAnalysisRunner`

Integration with existing pipeline:
1. Reuse current config + `GitService` clone/reset.
2. Reuse existing technical IR extraction output (`MicroserviceSystem`) to map files to services.
3. Build organizational structures from git history.
4. Emit socio-technical IR JSON.

## Assumptions

1. `docs/example_ir.json` is a target shape reference, not strict validated schema with required-field constraints.
2. Organizational metrics are computed from Git repository history and service/file mapping derived from existing CIMET path rules.
3. Issue/PR linking can initially be heuristic (commit message parsing) unless a tracker API is introduced.
4. Existing Java-centric technical extraction remains intact; organizational layer must work even if some files are non-Java.
5. Metrics definitions from the two PDFs are authoritative; where formulas are ambiguous in code-level implementation, formulas will be codified explicitly in new docs/tests.

## Risks and unknowns

1. **PDF text extraction limitation in current shell**
- `pdftotext` is unavailable in this environment; exact formula transcription from PDFs was not machine-extracted here.
- Mitigation: implement formulas behind explicit interfaces and add validation fixtures once formulas are transcribed manually.

2. **Identity resolution quality risk**
- Same developer may appear under multiple aliases/emails.
- Mitigation: deterministic alias normalization + optional manual alias mapping file.

3. **Service ownership ambiguity**
- Commits touching shared libs/root files can distort ownership.
- Mitigation: weighting policy (service-local files weighted higher; global files lower/excluded).

4. **Temporal window sensitivity**
- PTC/AOC/roles can vary significantly with lookback window.
- Mitigation: configurable analysis window in config (e.g., all-time vs trailing N months).

5. **Runner inconsistencies in current codebase**
- Some runners hardcode args and diverge from comments/usage.
- Mitigation: add new runner with explicit CLI contract and avoid propagating hardcoded-arg behavior.

## Schema compatibility with example_ir.json

Planned compatibility mapping:

1. Top-level alignment
- Emit:
  - `type: "SocioTechnicalMicroserviceSystem"`
  - `name`
  - `technical`
  - `organizational`
  - `traceability`

2. `technical` section
- Populate `technical.microservices` from existing `MicroserviceSystem.microservices` (same object content used today by CIMET IR).
- Optionally include technical `commitID` in `technical` or traceability metadata for provenance.

3. `organizational.developers`
- Include normalized developer ID, display name, aliases.
- Include role scores: `jack`, `maven`, `connector`, `roleStackingIndex`.

4. `organizational.commits`
- Include commit id, normalized author id, timestamp.
- Include touched microservices/files and change size (`addedLOC`, `deletedLOC`).
- Include linked issues if available.

5. `organizational.serviceOwnership`
- Include contribution-based primary developers.
- Include concentration metric and `teamCohesion` (PTC-derived aggregate for service team).

6. `organizational.servicePairRelations`
- Include per-pair organizational coupling (input to AOC).
- Include shared developers and switching score.

7. `traceability`
- Emit explicit relation tables that can be consumed independently from the nested sections.

8. Compatibility posture
- The generated JSON should be backward-compatible with current technical IR by embedding, not replacing, existing microservice objects.
- If strict schema validation is added later, maintain a version field (e.g., `schemaVersion`) and a documented migration path.