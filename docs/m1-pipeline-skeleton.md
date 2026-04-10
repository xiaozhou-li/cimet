# M1 Pipeline Skeleton

## Stage order (explicit and deterministic)

`OrganizationalAnalysisService#runM1PipelineSkeleton()` executes these stages in fixed order:

1. `extract-commit-contributions`
2. `normalize-developer-identities`
3. `map-files-to-microservices`
4. `emit-traceability-edges`

The stage order is defined as `OrganizationalAnalysisService.M1_STAGE_ORDER` and is included in the returned result object.

## Key interfaces

`OrganizationalAnalysisService` defines stage interfaces for M1 pipeline composition:

- `CommitContributionStage`
  - output: `List<OrganizationalPipelineResult.CommitContribution>`
- `IdentityNormalizationStage`
  - output: `Map<String, String>` raw identity -> normalized identity
- `ServiceMappingStage`
  - output: `List<OrganizationalPipelineResult.ServiceTouch>`
- `TraceabilityEdgeStage`
  - output: `List<OrganizationalPipelineResult.TraceabilityEdge>`

Structured result contract:

- `OrganizationalPipelineResult`
  - `stageOrder`
  - `contributions`
  - `normalizedDeveloperIdentities`
  - `serviceTouches`
  - `traceabilityEdges`

## Invocation and compatibility

- Legacy default remains unchanged: `cimet.organizational.mode` unset -> `DISABLED`.
- Existing opt-in values (`enabled`, `on`, `true`, `stub`) still map to `STUB` mode.
- M1 skeleton runs only with explicit mode values such as:
  - `m1`
  - `m1-skeleton`
  - `pipeline`

This keeps M1 behavior additive and non-breaking for existing CIMET runtime paths.

## Scope for M1

In scope now:

- deterministic stage orchestration sequence,
- structured pipeline result object returned for explicit runs,
- baseline extraction/normalization/mapping/traceability contracts,
- safe defaults with empty but non-null outputs.

Deferred to M2+:

- PTC/NOC/AOC calculations,
- Jack/Maven/Connector/role detection,
- advanced identity resolution (email aliases, heuristic merges),
- full microservice boundary mapping heuristics,
- rich traceability graph completeness and export integration.
