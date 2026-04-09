# M0 Formula Calibration and Versioning Spike

## Purpose
This spike freezes how formula ambiguity will be handled before implementing organizational metric calculators (PTC/NOC/AOC/Jack/Maven/Connector/RSI).

It does **not** finalize all numeric constants. It converts uncertainty into a versioned decision checklist for M2/M3.

## Inputs reviewed
- `docs/algorithm-spec.md`
- `docs/product-requirements.md`
- `docs/user-stories.md`
- `docs/backlog.md`
- `docs/release-plan.md`
- `docs/test-strategy.md`
- `docs/planning-glossary.md`
- `docs/SEAA26cohesion.pdf` (file present)
- `docs/KD+EASE_2026_.pdf` (file present)

Note on PDFs in this environment:
- Direct text extraction tooling for the PDFs was unavailable here.
- Any paper-specific numeric/detail confirmation not already captured in `algorithm-spec.md` is explicitly listed below as **calibration required** (not silently assumed).

## 1) Formula areas already settled (M0 baseline)
These are settled enough to start implementation scaffolding and metadata contracts.

### 1.1 Scope and terminology
- Distinction between technical coupling vs organizational coupling is fixed.
- Distinction between cohesion (within-service) vs coupling (between-service) is fixed.
- Role semantics are fixed:
  - Jack = breadth
  - Maven = depth
  - Connector = brokerage
  - RSI = role co-occurrence intensity

### 1.2 Structural computation contracts
- Metrics are computed per analysis window.
- Pair coupling is symmetric at pair level.
- AOC is an aggregate over pairwise values in each window.
- Outputs are clamped/normalized to `[0,1]` where specified.
- Compatibility field policy is fixed: `organizationalCoupling` compatibility alias, `noc` explicit enriched field.

### 1.3 Traceability and explainability expectations
- Formula and normalization version metadata must be emitted.
- Window metadata is required where windowed analysis is used.
- Deterministic behavior is required (fixed ordering, reproducible assignment rules).

## 2) Ambiguous points requiring calibration (explicit checklist)
These items must be resolved before production formulas are declared stable.

### 2.1 PTC calibration checklist
1. Confirm exact PTC definition source-to-implementation mapping from SEAA paper.
2. Validate singleton service behavior (`n=1`) default value policy.
3. Validate focus penalty term shape for large contributor sets.
4. Confirm handling for zero-contribution services (omit vs emit zero).

### 2.2 OC/NOC/AOC calibration checklist
1. Confirm overlap component formula (Jaccard vs weighted overlap) against source definitions.
2. Confirm switching component definition:
   - transition unit (commit adjacency vs time adjacency),
   - weighting (uniform vs LOC/commit-weighted).
3. Confirm OC composition weights (e.g., overlap vs switching blend).
4. Confirm NOC normalization scope (per-window pair set vs global run set).
5. Confirm behavior when all OC values are identical.

### 2.3 Jack/Maven/Connector calibration checklist
1. Jack:
   - breadth denominator definition (active services vs all technical services),
   - switching propensity denominator behavior in sparse activity.
2. Maven:
   - concentration/depth blend weights,
   - dominance term definition for multi-service contributors.
3. Connector:
   - bridge potential term shape,
   - normalization population (active developers per window vs cross-window).

### 2.4 RSI calibration checklist
1. Confirm co-occurrence formula family (dispersion-adjusted mean vs alternatives) against KD paper intent.
2. Confirm epsilon and zero-activity behavior.
3. Confirm whether categorical stacking/bins are required in MVP or deferred.

### 2.5 Threshold/profile calibration checklist
1. Default role classification thresholds for Jack/Maven/Connector.
2. Whether thresholds are global defaults or profile-based by repo characteristics.
3. Whether role labels require hysteresis/stability constraints across windows.

## 3) Proposed formula version identifiers
Use explicit, machine-readable IDs to avoid undocumented formula drift.

## 3.1 Bundle-level version
- `organizationalFormulaBundleVersion`
  - M0 value: `m0-draft`
  - First implementation-ready baseline target: `m2-v1` (cohesion/coupling) and `m3-v1` (roles/RSI)

## 3.2 Metric-level versions
- `ptcFormulaVersion`: `ptc-v1-draft`
- `ocFormulaVersion`: `oc-v1-draft`
- `nocNormalizationVersion`: `nocnorm-v1-draft`
- `aocAggregationVersion`: `aocagg-v1-draft`
- `jackFormulaVersion`: `jack-v1-draft`
- `mavenFormulaVersion`: `maven-v1-draft`
- `connectorFormulaVersion`: `connector-v1-draft`
- `rsiFormulaVersion`: `rsi-v1-draft`

## 3.3 Policy/profile versions
- `roleThresholdProfileVersion`: `role-threshold-v1-draft`
- `contributionWeightPolicyVersion`: `contrib-weight-v1-draft`
- `windowAssignmentPolicyVersion`: `window-assign-v1`

## 4) Required metric metadata payload (when metrics are emitted)
At minimum, each metric-bearing section should carry:
- `formulaVersion` (or metric-specific version key)
- `normalizationVersion` (if normalized)
- `weightingPolicy` (e.g., LOC-weighted vs commit-weighted)
- `thresholdProfileId` and `thresholdProfileVersion` (for role classification)
- `windowId` and window mode context
- `exclusions`/`fallbacks` applied (e.g., missing LOC fallback)
- `calibrationStatus`:
  - `draft` / `provisional` / `stable`

System-level metadata should include:
- `organizationalFormulaBundleVersion`
- `schemaVersion`
- `calibrationRecordId` (link to decision log/ADR once created)

## 5) What must be deferred to M2/M3 implementation

### Deferred to M2 (cohesion/coupling)
- Final numeric constants and blend weights for PTC/OC/NOC/AOC.
- Final normalization scope decisions for NOC.
- Production explainability payload fields for pair/component decomposition.
- Golden expected values for real repositories after calibration lock.

### Deferred to M3 (roles/RSI)
- Final Jack/Maven/Connector formula constants and edge behavior.
- Final RSI formulation choice and optional category bins.
- Final default role threshold profiles and tuning guidance.

## 6) Synthetic scenarios required for later validation
The following fixture families should be created before formula lock:

1. **No-overlap/no-switch pair**
- expected OC/NOC/AOC near zero.

2. **High-overlap/low-switch pair**
- isolates overlap component sensitivity.

3. **Low-overlap/high-switch pair**
- isolates switching component sensitivity.

4. **Single-owner service**
- tests ownership concentration and singleton PTC behavior.

5. **Balanced multi-contributor service**
- tests high PTC behavior.

6. **Breadth-heavy developer**
- Jack > Maven expected.

7. **Depth-heavy specialist**
- Maven > Jack expected.

8. **Broker developer across strongly coupled pairs**
- Connector elevated vs controls.

9. **Role stacking profile**
- high Jack + high Connector + medium Maven to validate RSI response.

10. **Sparse/edge windows**
- no commits, one service only, all equal OC values.

## 7) Decision record policy
No formula assumption is considered final unless recorded with:
- formula version ID,
- rationale,
- affected tests/fixtures,
- migration impact on existing outputs.

Any formula change after first stable version must increment the relevant version keys and update golden fixtures.

## 8) Exit condition for this spike
This spike is complete when:
- all ambiguity is captured as explicit checklist items,
- version key scheme is defined,
- metadata requirements are documented,
- M2/M3 teams can implement without hidden assumptions.

Status: **complete for M0 planning**.
