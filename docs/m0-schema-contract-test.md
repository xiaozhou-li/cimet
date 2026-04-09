# M0-US4 Test Report: Schema Contract and Compatibility Mapping

## Scope
This report validates M0 schema contract conformance for the current socio-technical scaffold against:
- `docs/m0-schema-contract.md`
- `docs/planning-glossary.md`
- `docs/example_ir.json`

## Implemented contract checks

Added test class:
- `src/test/java/unit/organizational/M0SchemaContractTest.java`

The checks assert:
1. Required scaffold top-level keys exist in serialized socio-technical JSON.
2. Contract terminology is aligned with planning glossary baseline.
3. `example_ir.json` compatibility is enforced at key-path level, including explicit deferred mapping rules.

## Commands executed

```bash
./mvnw -Dtest=unit.organizational.M0SchemaContractTest,unit.organizational.SocioTechnicalMicroserviceSystemTest test -Dstyle.color=never
```

## Validated key paths

### Required top-level keys (scaffold JSON)
Validated present:
- `type`
- `name`
- `technical`
- `organizational`
- `traceability`

### `example_ir.json` alignment (required in M0)
Validated present in compatibility reference and contract mapping:
- `type`
- `name`
- `technical`
- `organizational`
- `traceability`

### Type compatibility alias rule validated
Contract explicitly allows:
- `SocioTechnicalMicroserviceSystem`
- `socio-technical-ir`

## Mismatches found

No contract-breaking mismatches found for M0 required paths.

## Deferred items (explicit and accepted in M0)

The following `example_ir.json` paths are intentionally deferred and explicitly documented in `docs/m0-schema-contract.md`:
- `organizational.developers`
- `organizational.commits`
- `organizational.issues`
- `organizational.serviceOwnership`
- `organizational.servicePairRelations`
- `traceability.developerToCommit`
- `traceability.commitToIssue`
- `traceability.commitToFile`
- `traceability.developerToMicroservice`

These are expected to be implemented in M1+ and therefore are not failures in M0.

## Terminology alignment result

Contract terminology was verified against `docs/planning-glossary.md` for core terms:
- technical coupling
- organizational coupling
- cohesion
- PTC
- NOC
- AOC
- organizationalCoupling
- noc

Result: aligned.

## Test outcome

Status: **PASS**

M0 scaffold satisfies the agreed schema contract checks. The remaining gaps are deferred by design and documented explicitly.
