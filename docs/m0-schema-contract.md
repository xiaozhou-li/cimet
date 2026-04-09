# M0 Schema Contract: Socio-Technical IR and Compatibility Mapping

## Status and intent
- Status: **Authoritative for M0**.
- Scope: freeze a minimal, implementable schema contract for the organizational extension.
- Constraint: additive only; no replacement of existing technical IR semantics.

## Inputs compared
This contract is derived from:
- `docs/example_ir.json` (compatibility reference)
- `docs/proposed-organizational-ir.json` (target enriched direction)
- current technical IR model (`MicroserviceSystem` and related `toJsonObject()` behavior)
- current JSON utility conventions (`JsonSerializable`, `JsonReadWriteUtils`)

## Terminology baseline alignment (M0)
This contract follows `docs/planning-glossary.md` as the authoritative terminology source.

In particular:
- **technical coupling** and **organizational coupling** are distinct concepts.
- **cohesion** is within-service (for organizational analysis: PTC), while **coupling** is between-service (OC/NOC/AOC).
- `organizationalCoupling` is preserved as compatibility name, and `noc` is the explicit enriched pairwise normalized field.

## 1. Required top-level keys (M0)
A socio-technical artifact in M0 MUST contain these top-level keys:
1. `type`
2. `name`
3. `technical`
4. `organizational`
5. `traceability`

Optional top-level keys allowed in M0:
- `schemaVersion`
- `metadata`

No other top-level keys are required in M0.

## 2. Required M0 model classes and fields

### 2.1 `SocioTechnicalMicroserviceSystem` (required)
Required fields:
- `type: string`
- `name: string`
- `technical: MicroserviceSystem | null`
- `organizational: OrganizationalSection | null`
- `traceability: TraceabilitySection | null`

Optional fields:
- `schemaVersion: string`
- `metadata: object<string,string>`

### 2.2 `OrganizationalSection` (required)
M0 required scaffold fields:
- `status: string`
- `note: string`

### 2.3 `TraceabilitySection` (required)
M0 required scaffold fields:
- `contractVersion: string`
- `note: string`

## 3. Technical embedding contract (M0)
The `technical` section MUST embed existing technical IR content without remapping semantic meaning.

M0 technical embedding rules:
- Field type is existing `MicroserviceSystem` model content.
- Existing technical JSON shape remains governed by technical model serializers:
  - `name`
  - `commitID`
  - `microservices`
  - `orphans`
- M0 MUST NOT remove or reinterpret these technical fields.

## 4. Scaffold-only vs deferred fields

### 4.1 M0 scaffold-only fields
These fields are intentionally placeholders in M0:
- `organizational.status`
- `organizational.note`
- `traceability.contractVersion`
- `traceability.note`

### 4.2 Deferred to M1+
The following are **deferred** (not required in M0):
- `organizational.developers`
- `organizational.commits`
- `organizational.issues`
- `organizational.contributorServiceRelations`
- `organizational.serviceOwnership`
- `organizational.serviceOrganizationalMetrics`
- `organizational.servicePairRelations`
- `organizational.analysisWindows`
- all concrete traceability relation arrays (`developerToCommit`, `commitToIssue`, etc.)

For explicit `example_ir.json` compatibility mapping, these paths are deferred in M0 and expected in later milestones:
- `traceability.developerToCommit`
- `traceability.commitToIssue`
- `traceability.commitToFile`
- `traceability.developerToMicroservice`

### 4.3 Deferred by milestone intent
- M1: contribution extraction, identity normalization, service mapping base entities
- M2: PTC + OC/NOC/AOC service/service-pair metrics
- M3: Jack/Maven/Connector + RSI and role history
- M4: export/reporting completion and compatibility hardening

If a field is ambiguous and not listed as required in M0, it is treated as **deferred**.

## 5. Compatibility rules for `example_ir.json`

### 5.1 Type discriminator compatibility
Ambiguity exists between:
- `SocioTechnicalMicroserviceSystem` (example/proposed docs)
- `socio-technical-ir` (current M0 scaffold implementation)

M0 contract resolution:
- Canonical semantic type: **SocioTechnicalMicroserviceSystem**.
- Compatibility alias accepted: **socio-technical-ir**.
- Producers MAY emit either in M0, but compatibility checks MUST treat them as equivalent.
- Post-M0 alignment to a single emitted value is deferred (explicit backlog item required before enforcement).

### 5.2 Organizational coupling compatibility key
For service-pair organizational coupling (M2+):
- `organizationalCoupling` is a required compatibility key when service pair relations are emitted.
- `noc` is the explicit normalized field in enriched output.
- When both are present, they MUST represent the same numeric value in M2+.

### 5.3 Top-level compatibility obligation
A compatibility-mode socio-technical artifact MUST preserve the top-level sections expected by `example_ir.json`:
- `type`, `name`, `technical`, `organizational`, `traceability`.

## 6. Additive-field policy
M0 additive policy:
- New fields MAY be added under `organizational`, `traceability`, `metadata`, and future nested structures.
- Existing required M0 keys MUST NOT be removed.
- Existing technical IR field meanings MUST remain unchanged.
- Compatibility aliases (for example `organizationalCoupling`) MUST NOT be removed once introduced.

## 7. Deterministic ordering policy
To keep outputs predictable and diff-friendly:
- Top-level key order SHOULD be:
  1. `type`
  2. `name`
  3. `technical`
  4. `organizational`
  5. `traceability`
  6. `schemaVersion` (if present)
  7. `metadata` (if present)
- `metadata` keys SHOULD be emitted in sorted lexical order.
- Existing technical serializers control ordering inside `technical`.

Note: JSON object order is not semantic, but this ordering is a project convention for determinism.

## 8. `schemaVersion` policy for initial release
- M0 default value: `m0`.
- `schemaVersion` is optional in M0 scaffold artifacts but strongly recommended.
- First stable organizational release (post-M0) MUST define an explicit semantic version string and migration note.

## 9. Naming conflict closure in M0 contract
This contract resolves M0 naming ambiguity explicitly:
- `type` accepts two equivalent values (`SocioTechnicalMicroserviceSystem`, `socio-technical-ir`) under compatibility policy.
- `organizationalCoupling` is reserved compatibility field name for pair coupling outputs when those outputs exist.
- `noc` is reserved enriched pair-coupling field name.

No additional naming conflicts are left unresolved in this M0 contract.

## 10. Implementation checklist (M0 gate)
M0 schema gate is satisfied when:
- the three required model classes exist and serialize,
- top-level required keys are present,
- technical embedding uses existing `MicroserviceSystem` semantics,
- scaffold/deferred boundaries are respected,
- compatibility alias rules above are documented and testable.
