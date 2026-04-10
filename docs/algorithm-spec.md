# Organizational Metrics Algorithm Specification


## Terminology Contract

This document follows [`planning-glossary.md`](./planning-glossary.md) for mandatory terminology.

Operational guardrails:
- Distinguish **technical coupling** from **organizational coupling** in every section.
- Distinguish **cohesion (within-service)** from **coupling (between-service)** explicitly.
- Use role semantics consistently: **Jack=breadth**, **Maven=depth**, **Connector=brokerage**, **RSI=role co-occurrence intensity**.
- Treat `organizationalCoupling` as compatibility alias and `noc` as explicit pairwise normalized value where both appear.


## 1. Purpose

This document defines implementation-oriented algorithms for CIMET organizational analysis.
It operationalizes the conceptual ideas from the project papers into deterministic, testable procedures.

Scope:
- contribution extraction,
- identity normalization,
- service assignment,
- temporal windowing,
- PTC, OC/NOC/AOC,
- Jack/Maven/Connector,
- RSI,
- edge handling, normalization, complexity, and test oracles.

## 2. Notation and Conventions

- `d`: developer
- `s, t`: microservices
- `w`: analysis window
- `c`: commit
- `f`: file
- `W(d,s,w)`: weighted contribution of developer `d` to service `s` in window `w`
- `D(s,w)`: active developers of service `s` in window `w`
- `S(w)`: active services in window `w`
- `C(w)`: commits in window `w`

Conventions:
- All metric outputs are clamped to `[0,1]` unless explicitly stated.
- Time boundaries use half-open intervals: `[start, end)` in UTC unless configured otherwise.
- Arrays in output should be deterministically sorted by stable IDs.

## 3. Inputs and Outputs

## 3.1 Required inputs

1. Repository change history (commits + file diffs).
2. Technical service boundaries from CIMET IR (`microservice.path`, `microservice.name`).
3. Analysis configuration:
- window mode and params,
- weighting params,
- role thresholds,
- optional alias map.

## 3.2 Required output structures

At minimum the algorithm layer must produce:
- `developers[]`
- `commits[]`
- `contributorServiceRelations[]`
- `serviceOwnership[]`
- `serviceOrganizationalMetrics[]`
- `servicePairRelations[]`
- `analysisWindows[]`
- `traceability.*` edge sets

The resulting artifact must remain compatible with `docs/example_ir.json` top-level shape.

## 4. Contribution Extraction Rules

## 4.1 Commit record extraction

For each commit `c` in scope extract:
- `id` (full hash or stable shortened hash),
- `author` raw identity (name/email),
- `timestamp` UTC,
- changed files,
- per-file added/deleted LOC if available,
- commit message (for optional issue linking).

## 4.2 File-level contribution weight

For each file change `(c,f)` define base weight:
- `loc_delta = addedLOC + deletedLOC`
- `base_weight = loc_delta if loc_delta > 0 else 1`

Optional file-class multiplier:
- `m_file = 1.0` default
- configurable exceptions (for shared/global files) can reduce weight, e.g. `m_global = 0.25`

Final file-change weight:
- `w_file(c,f) = base_weight * m_file`

Commit-level service contribution increment:
- if `f` maps to service `s`, add `w_file(c,f)` to `W(author(c), s, window(c))`

## 4.3 Rename/delete handling

- `ADD`: use new path and new LOC values.
- `MODIFY`: use path and LOC delta.
- `DELETE`: use old path and deleted LOC; if LOC unavailable, fallback to `1`.
- `RENAME`: treat as modify with new path for mapping; if old/new service differ, count as cross-service switch candidate.

## 5. Developer Identity Normalization Rules

Normalization precedence:
1. Manual alias map override (if configured).
2. Exact normalized email match (lowercase, trimmed).
3. Provider-specific no-reply normalization (e.g., GitHub no-reply forms).
4. Conservative fallback by normalized name only when email absent.

Rules:
- Do not merge identities on fuzzy name similarity alone when emails conflict.
- Track all merged aliases in `developers[].aliases`.
- Emit ambiguity diagnostics for unresolved collisions.

Determinism:
- canonical `developerId` assignment is stable by first-seen order in sorted commit stream.

## 6. Service Assignment Rules for File Changes

Given file path `p` and microservice paths from technical IR:
1. Normalize path separators to `/`.
2. Find all services whose path is a prefix of `p`.
3. Select the **longest prefix match**.
4. If none matched, assign to `GLOBAL_UNMAPPED` bucket.

Shared/global files:
- configurable policy: `exclude`, `downweight`, or `include_full`.
- default: `downweight`.

Output traceability must preserve original file path regardless of mapping outcome.

## 7. Temporal Windowing Strategy

Supported modes:
- `full`: one window over full range.
- `trailing`: rolling windows of fixed duration and optional step.
- `calendar`: fixed buckets (e.g., month/quarter).

Assignment:
- a commit belongs to window `w` iff `start_w <= ts(c) < end_w`.
- default single-window assignment per commit; multi-window assignment only if explicitly enabled.

Window metadata:
- `id`, `mode`, `start`, `end`, `timezone`, `commitCount`, `developerCount`, `aoc`.

## 8. Metric Definitions and Computation Steps

## 8.1 Shared primitives

For each window `w`:
- Service-level total contribution:
  - `T(s,w) = sum_d W(d,s,w)`
- Developer contribution share in service:
  - `p(d|s,w) = W(d,s,w) / T(s,w)` for `T(s,w)>0`
- Developer service-distribution share:
  - `q(s|d,w) = W(d,s,w) / sum_x W(d,x,w)` for developer total > 0

## 8.2 PTC (Pairwise Team Cohesion)

Intent: represent focused + balanced participation inside a service team.

For service `s` in window `w`:
1. `n = |D(s,w)|`
2. If `n == 0`: `PTC(s,w) = 0`
3. If `n == 1`: `PTC(s,w) = singleton_ptc` (default `0.35`, configurable)
4. Else:
- entropy: `H = -sum_{d in D(s,w)} p(d|s,w) * ln(p(d|s,w))`
- balance: `B = H / ln(n)`
- focus: `F = 1 / (1 + ln(n))`
- `PTC(s,w) = clamp(B * F * focus_scale, 0, 1)`
  - default `focus_scale = 1.7` to keep practical range

Notes:
- Higher with balanced shares and moderate team size.
- Penalizes large diffuse contributor sets.

## 8.3 OC, NOC, and AOC

### 8.3.1 Pairwise overlap component

For pair `(s,t)` in `w`:
- developer sets: `Ds = D(s,w)`, `Dt = D(t,w)`
- overlap score (Jaccard):
  - `O(s,t,w) = |Ds ∩ Dt| / |Ds ∪ Dt|` (0 if union empty)

### 8.3.2 Pairwise switching component

For each developer `d`, build ordered service sequence from commits in `w`.
A transition contributes to `(s,t)` if adjacent services are `{s,t}`.

For developer `d`:
- `trans_d`: total cross-service transitions in `w`
- `st_trans_d`: transitions between `s` and `t`
- `sw_d(s,t,w) = st_trans_d / max(1, trans_d)`

Aggregate switching score:
- `SW(s,t,w) = avg_{d in Ds∪Dt}(sw_d(s,t,w))`
  (or weighted by developer activity; default simple average).

### 8.3.3 Raw organizational coupling (OC)

- `OC(s,t,w) = lambda_overlap * O(s,t,w) + (1 - lambda_overlap) * SW(s,t,w)`
- default `lambda_overlap = 0.5`

### 8.3.4 NOC normalization

Within window `w`, for all active unordered pairs:
- `oc_min = min OC`, `oc_max = max OC`
- If `oc_max == oc_min`: `NOC = OC` (already constant)
- Else:
  - `NOC(s,t,w) = (OC(s,t,w) - oc_min) / (oc_max - oc_min)`

Compatibility field:
- `organizationalCoupling = NOC` (for `example_ir.json` compatibility)

### 8.3.5 AOC aggregation

For window `w` with pair set `P(w)`:
- `AOC(w) = avg_{(s,t) in P(w)} NOC(s,t,w)`
- if `P(w)` empty, `AOC(w)=0`

## 8.4 Jack score

Intent: breadth-oriented role.

For developer `d` in `w`:
1. service coverage:
- `cov = |{s: W(d,s,w)>0}| / max(1, |S(w)|)`
2. switching propensity:
- `sp = cross_service_transitions(d,w) / max(1, all_transitions(d,w))`
3. `Jack(d,w) = sqrt(cov * sp)`

## 8.5 Maven score

Intent: depth/specialization role.

For developer `d` in `w`:
1. concentration in own service distribution:
- `hhi_d = sum_s q(s|d,w)^2`
2. dominance over a single strongest service:
- `dom_d = max_s p(d|s,w)` (0 if none)
3. `Maven(d,w) = 0.5 * hhi_d + 0.5 * dom_d`

## 8.6 Connector score

Intent: brokerage role across coupled services.

For each pair `(s,t)` and developer `d`:
- bridge potential:
  - `b_d(s,t,w) = 2 * min(q(s|d,w), q(t|d,w))`

Raw brokerage:
- `raw_conn(d,w) = sum_{(s,t) in P(w)} b_d(s,t,w) * NOC(s,t,w)`

Normalize across active developers in window:
- if `raw_max == raw_min`, `Connector = raw_conn`
- else min-max normalize to `[0,1]`

## 8.7 RSI (Role Stacking Index)

Intent: co-occurrence intensity across Jack/Maven/Connector dimensions.

Given role vector `r = [J, M, C]` for developer `d,w`:
1. `mu = mean(r)`
2. `sigma = std(r)`
3. `dispersion = min(1, sigma / max(mu, eps))` with `eps=1e-9`
4. `RSI(d,w) = clamp(mu * (1 - dispersion), 0, 1)`

Optional discrete stacking count:
- `stackCount = |{role in [J,M,C] : role >= role_threshold}|`

## 9. Edge Cases

1. No commits in window: output empty/zero metrics, preserve window metadata.
2. Service with no contributions: omit from metric arrays or emit zero-valued entry by config.
3. Single-contributor service: use configured `singleton_ptc`.
4. One active service in window: no pair metrics; `AOC=0`.
5. Identity collisions unresolved: isolate as separate developers and emit warning.
6. All OC values equal: set `NOC=OC` to avoid divide-by-zero.
7. Missing churn values: fallback contribution weight = 1.

## 10. Normalization Strategy

1. Clamp all computed role/cohesion/coupling outputs to `[0,1]`.
2. Use min-max normalization for pairwise OC -> NOC within each window.
3. Preserve raw subcomponents in metadata for explainability.
4. Record formula and normalization versions in output metadata.

## 11. Computational Complexity (per window unless noted)

Let:
- `C` = commits,
- `F` = changed files,
- `D` = developers,
- `S` = active services,
- `k_d` = services touched by developer `d`.

1. Contribution extraction + mapping: `O(F * match_cost)`
- with longest-prefix index/trie: near `O(F * path_length)`.
2. Identity normalization: `O(C)` average with hash maps.
3. Window assignment: `O(C log W)` or `O(C)` for direct bucket modes.
4. Ownership/PTC: `O(sum_s |D(s)|)`.
5. Pairwise OC naive: `O(S^2 * D)`.
6. Pairwise OC sparse optimized:
- build from developer service sets: `O(sum_d k_d^2)`.
7. Connector from pair relations: `O(D * |P(w)|)`.

## 12. Test Oracle Suggestions

## 12.1 Structural invariants

- `sum_d p(d|s,w) = 1` for each service with contributions.
- `NOC(s,t,w) == NOC(t,s,w)`.
- `AOC(w)` equals arithmetic mean of pairwise NOC values.
- all role/PTC/coupling scores in `[0,1]`.

## 12.2 Scenario oracles

1. **No overlap/no switching scenario**
- Expected: `OC=NOC=0`, `AOC=0`.

2. **Identical contributor sets across two services**
- Expected high overlap component and high OC/NOC (subject to switching).

3. **Single dominant contributor service**
- Expected high ownership concentration, low-moderate PTC.

4. **Broad developer touching many services**
- Expected higher Jack, lower Maven.

5. **Deep specialist developer**
- Expected higher Maven, lower Jack.

6. **Balanced high all-role developer**
- Expected high RSI.

## 12.3 Regression oracle strategy

- Maintain golden fixtures with hand-computed expected values.
- Version fixtures with formula revisions.
- Validate both compatibility fields (`organizationalCoupling`) and new fields (`noc`).

## 13. Implementation Checklist

- Keep algorithms pure and deterministic.
- Emit formula version metadata for all metrics.
- Preserve compatibility keys required by `docs/example_ir.json`.
- Prefer additive data model changes over mutation of existing technical IR model.

# Algorithm Specification: CIMET Organizational Metrics (Paper-Faithful)

## Terminology Contract

This document strictly follows definitions from:

* KD+EASE_2026_.pdf
* SEAA26cohesion.pdf

No proxy or heuristic substitutions are used. All formulas are aligned with the original research.

---

# 1. Contribution Model

For each developer d and service s:

* c_{d,s}: number of contributions (commits or LOC-weighted)
* C_s = Σ_d c_{d,s}

## Contribution share

w_{d,s} = c_{d,s} / C_s

## Contribution focus

f_{d,s} = c_{d,s} / Σ_{s'} c_{d,s'}

---

# 2. Organizational Cohesion (PTC)

Defined per service s.

Let D_s be the set of developers contributing to s.

If |D_s| < 2:

PTC(s) = 0

Else:

PTC(s) = (1 / |pairs|) * Σ_{i < j} ( sqrt(f_{i,s} * f_{j,s}) * ((w_{i,s} + w_{j,s}) / 2) )

Where:

* pairs = all unordered developer pairs in D_s

Interpretation:

* High when developers are both focused on s and evenly contributing

---

# 3. Organizational Coupling (OC)

Defined for service pair (s_i, s_j).

For each developer d:

OC_d(s_i, s_j) = (2 * w_{d,s_i} * w_{d,s_j}) / (w_{d,s_i} + w_{d,s_j}) * S_d(s_i, s_j)

Where:

* S_d(s_i, s_j): switching degree between services for developer d

Then:

OC(s_i, s_j) = Σ_d OC_d(s_i, s_j)

---

# 4. Normalized Organizational Coupling (NOC)

NOC(s_i, s_j) = OC(s_i, s_j) / OC_max(s_i, s_j)

Where:

* OC_max(s_i, s_j) is computed assuming perfect alternation of contributions between the two services

Notes:

* This is NOT min-max normalization
* Normalization is analytical, not dataset-dependent

---

# 5. Average Organizational Coupling (AOC)

Defined per service s:

AOC(s) = (1 / (|S| - 1)) * Σ_{t ≠ s} NOC(s, t)

Where:

* S = set of services

---

# 6. Developer Roles (KD+EASE)

## 6.1 Jack (Breadth)

Jack_d = |Files_d| / |ReachableFiles|

Where:

* Files_d: files modified by developer d
* ReachableFiles: all files reachable via traceability graph

---

## 6.2 Maven (Depth)

Maven_d = |RareFiles_d| / |RareFiles|

Where:

* RareFiles: files rarely modified globally

---

## 6.3 Connector (Brokerage)

Connector_d = BetweennessCentrality(d)

Computed on developer graph where:

* Nodes = developers
* Edges = shared artifact interaction (files, commits, services)

---

# 7. Role Stacking Index (RSI)

RSI_d = (Jack_d * Maven_d * Connector_d)^(1/3)

Properties:

* Penalizes imbalance
* Rewards simultaneous strength across roles

---

# 8. Temporal Windowing

All metrics are computed per time window W.

Pipeline:

1. Filter contributions by W
2. Compute c_{d,s}
3. Derive w_{d,s}, f_{d,s}
4. Compute PTC, OC/NOC/AOC
5. Compute roles and RSI

---

# 9. Traceability Requirements

Required relations:

* developer → commit
* commit → file
* file → service

Derived:

* developer → service

---

# 10. Implementation Constraints

* Deterministic ordering required
* Floating point precision standardized
* All formulas versioned

---

# 11. Explicit Non-Goals

The following are intentionally NOT used:

* entropy-based PTC
* min-max normalization for coupling
* heuristic role scoring
* statistical RSI variants

---

# 12. Output Requirements

For each window:

* serviceOwnership
* PTC per service
* NOC per service pair
* AOC per service
* developer roles (Jack/Maven/Connector)
* RSI per developer

All outputs must include formula version metadata.
