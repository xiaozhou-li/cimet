# CIMET: Comprehensive Project Description and Extension Baseline

## 1. What CIMET is

**CIMET (Change Impact Microservice Evolution Tool)** analyzes the evolution of a microservice-based system across Git commits. It builds a structured representation of the system at a commit, computes change deltas between commits, merges deltas into new snapshots, and runs architecture-quality analyses (anti-patterns, rules, metrics).

In practical terms, CIMET is a pipeline:

1. Clone and parse repository source/build/config files.
2. Build an Intermediate Representation (IR) of the system at a commit.
3. Compute Delta between two commits.
4. Merge old IR + Delta into new IR.
5. Run detection/metrics across commits and export results.

This document is designed as a **technical baseline for extending CIMET to other programming languages using Codex**.

---

## 2. Current scope (today)

CIMET currently targets **Java-centric microservice repositories** with Spring-like conventions.

Why:

- Service root detection is based on `pom.xml` or `build.gradle`.
- Source parsing is implemented for `.java` files using JavaParser.
- Class role inference is annotation-driven (`@RestController`, `@Service`, `@Repository`, `@Entity`, `@FeignClient`, etc.).
- Endpoint and REST-call extraction follows Java/Spring idioms.

CIMET can ingest non-Java files (some config/build files), but the high-value semantic extraction is Java-oriented today.

---

## 3. End-to-end architecture

### 3.1 High-level flow

1. **Config input** (`config.json`) provides system name, repository URL, and branch.
2. **Repository acquisition** clones to `./clone/<repoName>` if missing.
3. **IR extraction** scans candidate service roots and parses source/config files.
4. **Delta extraction** computes commit-to-commit file changes and parses changed artifacts.
5. **IR merge** applies Delta onto old IR to produce new IR.
6. **Detection layer** computes anti-patterns, architecture-rule violations, and metrics.
7. **Output layer** writes JSON artifacts and Excel summaries under `./output/...`.

### 3.2 Main packages and responsibilities

- `edu.university.ecs.lab.common`
  - Shared config, models, utilities, serialization, Git access.
- `edu.university.ecs.lab.intermediate.create`
  - IR extraction from repo snapshot.
- `edu.university.ecs.lab.delta`
  - Commit diff extraction and normalized change model.
- `edu.university.ecs.lab.intermediate.merge`
  - Applies delta to IR and maintains microservice/orphan consistency.
- `edu.university.ecs.lab.detection`
  - Anti-pattern detection, architectural rule checks, and metrics.

---

## 4. Core data model

### 4.1 `Config`

Represents run configuration (system, repo URL, branch). Parsed from JSON via `ConfigUtil.readConfig(...)`.

### 4.2 `MicroserviceSystem`

Top-level IR object:

- `name`
- `commitID`
- `microservices` (set)
- `orphans` (files/classes not currently mapped to a service root)

`orphans` are important during structural changes: when service roots are deleted/added, files can be orphanized and later adopted.

### 4.3 `Microservice`

Represents one service boundary with:

- `name`, `path`
- `controllers`, `services`, `repositories`, `entities`, `feignClients`
- `files` (config/build/static project files)

### 4.4 Behavior-level entities

- `JClass` with role, annotations, methods, fields, method calls.
- `Endpoint` (specialized method with URL + HTTP verb).
- `RestCall` (specialized method call inferred as HTTP call).
- `SystemChange` + `Delta` for commit diffs.

### 4.5 Graph models

- `ServiceDependencyGraph`: service-to-service edges based on REST call ↔ endpoint matching.
- `MethodDependencyGraph`: method-level call graph (including cross-service endpoint links).

---

## 5. IR extraction in detail

IR extraction is implemented by `IRExtractionService`.

### 5.1 Service root discovery

A directory is treated as a service root if it contains:

- a `pom.xml` **without** `<modules>` (non-aggregator module), or
- a `build.gradle`.

This is why Maven parent aggregators are skipped while leaf modules are included.

### 5.2 Service object creation

For each discovered root:

- service name is derived from folder name.
- path is normalized to repo-style (`/...`) format.

### 5.3 File scanning and parsing

Valid files are filtered via `FileUtils`.

- Config/build/static -> parsed as `ConfigFile`.
- `.java` -> parsed into `JClass` via `SourceToObjectUtils`.

### 5.4 Class role inference

Role is inferred from class annotations:

- `RestController` / `Controller` -> `CONTROLLER`
- `Service` -> `SERVICE`
- `Repository` -> `REPOSITORY`
- `RepositoryRestResource` -> `REP_REST_RSC`
- `Entity` / `Embeddable` -> `ENTITY`
- `FeignClient` -> `FEIGN_CLIENT`

Unknown role classes are dropped from IR.

### 5.5 Endpoint and REST-call extraction

- Endpoint extraction recognizes Spring mapping annotations.
- Method-call parsing resolves object/type and attempts REST-call conversion for known REST client patterns.

---

## 6. Delta extraction in detail

`DeltaExtractionService`:

1. Resets local repo to old commit.
2. Computes JGit diff old -> new.
3. Resets repo to new commit (for parsing new-state files).
4. Converts each diff entry into normalized `Delta` object.

Filtering and semantics:

- Root `pom.xml` is skipped.
- Non-valid file paths are skipped.
- Java files are reparsed into `JClass`; config files into `ConfigFile`.
- DELETE operations carry empty payloads.
- Paths are normalized with `/dev/null` for add/delete endpoints.

---

## 7. Merge engine in detail

`MergeService` constructs next IR from old IR + `SystemChange`.

### 7.1 Structural updates first

Before per-file operations, build-file deltas (`pom.xml`, `build.gradle`) are processed to update service boundaries.

It handles:

- service add/remove due to build-file add/delete,
- path specificity conflicts,
- duplicate build deltas,
- orphanize/adopt flow for files when boundaries change.

### 7.2 File-level updates

For each delta:

- `ADD`: add parsed class/config to mapped service or orphan pool.
- `MODIFY`: remove old then add new.
- `DELETE`: remove from service or orphan pool.

Final system commit ID is updated to `newCommit`.

---

## 8. Detection layer in detail

`DetectionService` performs commit-history analysis:

1. Build initial IR at first commit.
2. For each commit transition:
   - extract Delta,
   - merge to next IR,
   - run anti-pattern/metric calculations,
   - write results to Excel.

### 8.1 Anti-patterns

Includes checks such as:

- Greedy Microservices
- Hub-like Microservices
- Service Chains
- Wrong Cuts
- Cyclic Dependencies
- Wobbly Service Interactions
- No Healthchecks
- No API Gateway

### 8.2 Metrics

Includes coupling, structural, modularity, and cohesion metrics.

### 8.3 Architectural rules

`ARDetectionService` supports rule scans over delta and system snapshots (e.g., AR3, AR4, AR6, AR7 flows).

---

## 9. Operational notes and current caveats

These are important for contributors before extension work:

1. **Java/Maven baseline mismatch in docs vs build**
   - Build configuration currently targets a modern Java level (`pom.xml` source/target), while README wording may lag.
2. **Runner argument handling is not production-ready**
   - Some runner classes currently hardcode arguments in `main(...)` rather than honoring CLI args.
3. **Build warnings**
   - Duplicate `exec-maven-plugin` declaration warning exists.
4. **Tight coupling to Java/Spring semantics**
   - Parsing and role inference are annotation-centric and language-specific.

These do not block understanding but matter for robust extension.

---

## 10. Why CIMET is still a strong base for multi-language support

Even though parsing is Java-specific, the **pipeline architecture is reusable**:

- repository lifecycle is language-agnostic,
- IR/Delta/Merge abstractions already exist,
- graph and detection modules mostly consume normalized IR,
- output/reporting is independent from parser internals.

This means we should preserve the pipeline and **swap/augment language adapters**.

---

## 11. Extension blueprint: support other languages using Codex

### 11.1 Design goals

1. Keep existing Java behavior stable.
2. Add language support incrementally behind interfaces.
3. Normalize all languages into existing IR as much as possible.
4. Avoid rewriting anti-pattern/metric logic unless absolutely required.

### 11.2 Target architecture refactor (recommended)

Introduce extraction extension points:

1. `ServiceRootDetector`
   - Given repo tree, emit candidate microservice roots.
   - Java impl uses current `pom.xml`/`build.gradle` logic.
2. `LanguageFileClassifier`
   - Decide if a file is source/config/ignored for a language.
3. `SourceParserAdapter`
   - Parse source file -> normalized `ProjectFile` (usually `JClass`-like model or new polymorphic subtype).
4. `RoleInferenceStrategy`
   - Map language/framework constructs to CIMET roles.
5. `EndpointAndCallExtractor`
   - Framework-aware extraction of API endpoints and inter-service calls.

The IR pipeline then orchestrates adapters by repo/language profile.

### 11.3 Minimal migration strategy (low risk)

Phase 1: Internal abstraction without behavior change.

- Wrap current Java parsing and detection rules as `JavaSpringAdapter`.
- Keep outputs byte-compatible for Java repos.

Phase 2: Add one new language with constrained scope (recommended first: TypeScript/Node).

- Service root detection: `package.json`, workspace files.
- Endpoint extraction: Express/NestJS route decorators/calls.
- REST call extraction: `axios`, `fetch`, framework clients.

Phase 3: Add Go and Python support.

- Go: module roots (`go.mod`), handlers/routers (`gin`, `chi`, `mux`), HTTP client calls.
- Python: service roots (`pyproject.toml`, `requirements.txt`), frameworks (`FastAPI`, `Flask`, `Django REST`), requests/httpx calls.

Phase 4: Cross-language hardening.

- Unify confidence scoring and fallback behavior.
- Add language-specific golden test corpora.

### 11.4 IR compatibility decisions

To keep detection working, preserve these invariants:

- Every service has stable `name` and `path`.
- Endpoint-like operations must have URL + HTTP method when inferable.
- Outbound call model should map to `RestCall` equivalent.
- Unknown constructs should degrade gracefully (do not crash; emit empty or low-confidence nodes).

When a language cannot map 1:1 to `JClass`, introduce a generic source artifact model that still exposes:

- declared operations,
- annotations/decorators/attributes,
- call sites,
- ownership (service/path/module).

### 11.5 Detection-layer implications

Anti-pattern and metric modules mostly consume dependency graphs and service boundaries. If we keep graph construction consistent, most detectors can be reused.

Areas likely requiring adaptation:

- heuristics tied to Java naming/annotation assumptions,
- healthcheck and gateway detection if framework-specific today,
- cohesion metrics if they rely on Java-only class semantics.

### 11.6 Testing strategy for extension

1. Golden IR tests
   - fixed repo snapshots -> expected IR JSON.
2. Golden Delta tests
   - known commit pairs -> expected deltas.
3. Merge invariants
   - old IR + delta == new IR consistency assertions.
4. Graph invariants
   - endpoint/rest-call link consistency.
5. End-to-end smoke tests
   - one repo per language.

### 11.7 Codex-driven implementation workflow

Recommended way to use Codex for this program:

1. Ask Codex to extract seams and propose minimal interface set.
2. Ask Codex to perform non-behavior-changing refactor first (Java adapter extraction).
3. Add one language adapter at a time with fixture-driven tests.
4. Use Codex for repetitive parser mapping boilerplate and regression fixes.
5. Keep PRs language-bounded to avoid cross-cutting instability.

---

## 12. Concrete roadmap (backlog-ready)

1. Stabilize runners and CLI contract.
2. Create extraction SPI interfaces and Java default implementations.
3. Introduce language profile in config (`language`, `framework`, or `auto`).
4. Implement adapter selection in IR/Delta extraction.
5. Add TypeScript adapter MVP.
6. Add Go adapter MVP.
7. Add Python adapter MVP.
8. Re-tune anti-pattern and metric heuristics for multi-language parity.
9. Expand docs and sample configs.

---

## 13. What success looks like

CIMET should evolve from a Java-centric analyzer into a **language-agnostic evolution-analysis platform** where:

- IR/Delta/Merge contracts remain stable,
- language adapters are pluggable,
- detection quality is comparable across ecosystems,
- Codex can rapidly generate and iterate adapters with confidence via strong test fixtures.

