# M0 Package Baseline: Organizational Analysis Scaffolding

## Purpose
This milestone establishes a compile-safe package baseline for organizational analysis in CIMET without wiring into any production execution path.

The structure is additive and prepares M1-M4 implementation work while preserving current runtime behavior.

## Created packages

| Package | Responsibility (M0 baseline) |
|---|---|
| `edu.university.ecs.lab.organizational.models` | Domain model namespace for organizational IR entities (developers, contributor-service relations, service and pair metrics, windows, role profiles). |
| `edu.university.ecs.lab.organizational.extract` | Contribution mining and preprocessing namespace (git contribution extraction, identity normalization, service mapping inputs). |
| `edu.university.ecs.lab.organizational.metrics` | Metric calculator namespace (PTC, OC/NOC/AOC, Jack/Maven/Connector, RSI). |
| `edu.university.ecs.lab.organizational.services` | Orchestration namespace for coordinating extract -> metric -> output stages. |
| `edu.university.ecs.lab.organizational.output` | Output/serialization namespace for IR-compatible organizational JSON/report artifacts. |
| `edu.university.ecs.lab.organizational.runner` | Standalone runner namespace for future organizational-analysis entry points. |

## Placeholder artifacts added

- `organizational/package-info.java`
- `organizational/models/package-info.java`
- `organizational/models/OrganizationalAnalysisModel.java`
- `organizational/extract/package-info.java`
- `organizational/extract/ContributionExtractor.java`
- `organizational/metrics/package-info.java`
- `organizational/metrics/OrganizationalMetricCalculator.java`
- `organizational/services/package-info.java`
- `organizational/services/OrganizationalAnalysisService.java`
- `organizational/output/package-info.java`
- `organizational/output/OrganizationalOutputWriter.java`
- `organizational/runner/package-info.java`
- `organizational/runner/OrganizationalAnalysisRunner.java`

## Mapping to existing CIMET architecture

Current CIMET flow (preserved):
- config + repo acquisition -> IR extraction (`intermediate.create`) -> Delta (`delta`) -> Merge (`intermediate.merge`) -> detection/reporting (`detection`).

Organizational baseline mapping:
- `organizational.extract` aligns with repository-history mining and service-boundary assignment inputs.
- `organizational.metrics` aligns with detection-style modular calculators.
- `organizational.services` aligns with orchestration/service patterns already used in `common`, `intermediate`, and `detection`.
- `organizational.output` aligns with existing output/reporting and JSON artifact generation conventions.
- `organizational.runner` aligns with current runner-based executable patterns, but is intentionally not called from production flows in M0.
- `organizational.models` aligns with existing `common.models.*` style by providing a dedicated model layer for future IR extension.

## Behavior impact statement

- No existing runner/service was modified to invoke organizational modules.
- No config, CLI, or production wiring was changed in M0.
- Result: runtime behavior remains unchanged while package scaffolding is now available for M1-M4 implementation.
