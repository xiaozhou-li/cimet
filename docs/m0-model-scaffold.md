# M0 Model Scaffold: Socio-Technical IR Top-Level Artifact

## Overview
Milestone 0 adds a compile-ready, serialization-ready scaffold for a socio-technical IR wrapper that preserves the existing technical IR model and adds placeholder sections for organizational and traceability data.

No existing technical model (`MicroserviceSystem`) is replaced or modified.

## Added classes

- `edu.university.ecs.lab.organizational.models.SocioTechnicalMicroserviceSystem`
- `edu.university.ecs.lab.organizational.models.OrganizationalSection`
- `edu.university.ecs.lab.organizational.models.TraceabilitySection`

## Top-level JSON structure

`SocioTechnicalMicroserviceSystem#toJsonObject()` serializes the following top-level keys in explicit order:

1. `type`
2. `name`
3. `technical`
4. `organizational`
5. `traceability`
6. `schemaVersion` (optional)
7. `metadata` (optional)

This matches the required M0 scaffold shape while allowing future extension.

## Serialization strategy

### Project style alignment
- All three classes implement `JsonSerializable` and expose `toJsonObject()`.
- Serialization key insertion is explicit in `toJsonObject()` to keep deterministic top-level ordering.
- `metadata` is written using sorted keys (`TreeMap`) for deterministic output when present.

### JsonReadWriteUtils compatibility
- Existing utility `JsonReadWriteUtils.writeToJSON(path, object)` remains unchanged.
- Current project style already writes either model objects or `JsonObject` values.
- Recommended socio-technical write path:
  - `JsonReadWriteUtils.writeToJSON(path, socioTechnicalModel.toJsonObject())`

## Embedding technical content

- Field `technical` is typed as existing `MicroserviceSystem`.
- In `toJsonObject()`, technical content is embedded via `technical.toJsonObject()`.
- This preserves current IR semantics and avoids altering technical extraction, delta, merge, or detection behavior.

## What is intentionally stubbed until M1+

### OrganizationalSection
- Includes only minimal scaffold fields:
  - `status` (default: `stub`)
  - `note`
- No developers, ownership, cohesion/coupling, role, or window data yet.

### TraceabilitySection
- Includes only minimal scaffold fields:
  - `contractVersion` (default: `m0`)
  - `note`
- No commit lineage/provenance structures yet.

### SocioTechnicalMicroserviceSystem
- Provides top-level wrapper contract and optional `schemaVersion`/`metadata` only.
- No runtime wiring or production flow replacement in M0.

## Non-goals in M0

- No organizational metric computation.
- No changes to existing IR, Delta, Merge, Detection semantics.
- No migration of existing output formats.
