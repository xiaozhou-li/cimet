# ADR M0: Backward-Compatible Organizational Integration Mode

## Status
Accepted (Milestone 0)

## Context
CIMET currently executes a technical analysis pipeline centered on:
- IR extraction,
- Delta extraction,
- IR merge,
- detection/reporting.

Milestone 0 requires an explicit code-level contract for introducing organizational analysis while guaranteeing:
- additive integration,
- disabled-by-default behavior,
- no changes to existing IR/Delta/Merge/Detection semantics when organizational mode is off.

## Decision

### Insertion point
The integration hook is inserted in:
- `edu.university.ecs.lab.detection.DetectionService#runDetection()`

The hook is invoked **after** legacy technical outputs are produced (after workbook write/close), to minimize risk of affecting existing technical flows.

### Integration contract
Added new contract components under `edu.university.ecs.lab.organizational.services`:
- `OrganizationalIntegrationContract`
- `SystemPropertyOrganizationalIntegrationContract`
- `OrganizationalIntegrationMode` (`DISABLED`, `STUB`)

Runtime switch:
- system property: `cimet.organizational.mode`
- default: absent/unknown value -> `DISABLED`
- enabled stub values: `stub`, `enabled`, `on`, `true` -> `STUB`

Current M0 behavior:
- `DISABLED`: no organizational invocation.
- `STUB`: invoke `OrganizationalAnalysisService.run(mode)` which is intentionally no-op in M0.

## What stays unchanged
1. Existing technical pipeline semantics in IR extraction, Delta extraction, merge, and detection logic.
2. Default runtime behavior (no property set) remains legacy behavior.
3. Existing output artifacts and formats remain unchanged when mode is disabled.
4. No organizational metrics or extraction logic is executed in M0.

## Future invocation path (M1-M4)
Future milestones will extend the existing stub call path rather than refactoring legacy flow:
1. `DetectionService` resolves integration mode via contract.
2. If enabled, control passes to organizational orchestration service.
3. Organizational service will later coordinate:
   - contribution extraction,
   - identity normalization,
   - service mapping,
   - metric calculation,
   - organizational export/report generation.

This keeps the integration boundary explicit and testable.

## Rollback strategy
Rollback is immediate and low-risk:
1. Runtime rollback: keep `cimet.organizational.mode` unset (or set to `disabled`).
2. Code rollback (if required): remove the single invocation method in `DetectionService` and contract classes under `organizational.services`.

Because mode defaults to disabled and M0 mode implementation is no-op, rollback generally requires no data migration or output compatibility handling.

## Consequences
### Positive
- Explicit, documented integration seam with minimal footprint.
- No invasive changes to current pipeline.
- Clear path for staged enablement in M1-M4.

### Tradeoffs
- Mode toggle currently uses JVM system property rather than config-file field.
- Organizational mode is intentionally non-functional in M0 (stub only), by design.
