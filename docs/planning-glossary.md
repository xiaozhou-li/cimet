# Planning Glossary (Organizational Analysis)

This glossary is the canonical terminology baseline for planning artifacts in `docs/`.

## Core distinction: technical vs organizational signals

- **Technical coupling**: dependency strength among services inferred from source/runtime-facing structures (e.g., API calls, dependency graphs, architecture edges).
- **Organizational coupling**: coordination/interdependence among services inferred from contributor overlap and cross-service switching behavior.
- **Raw organizational coupling (OC)**: pre-normalized pairwise coupling signal.
- **Normalized organizational coupling (NOC)**: normalized pairwise coupling score in `[0,1]` for each service pair.
- **Average organizational coupling (AOC)**: aggregate (mean) of pairwise NOC over active service pairs in a window.

## Core distinction: cohesion vs coupling

- **Cohesion**: within-entity coherence.
  - In this extension, **PTC** measures within-service organizational cohesion (focused and balanced contributor participation).
- **Coupling**: between-entity interdependence.
  - In this extension, OC/NOC/AOC measure between-service organizational coupling.

## Role semantics

- **Jack**: breadth-oriented contributor profile (works across many services and/or frequent cross-service transitions).
- **Maven**: depth-oriented contributor profile (specialized, high concentration and influence in fewer services).
- **Connector**: brokerage-oriented profile (bridges service boundaries and coupled service pairs).
- **RSI (Role Stacking Index)**: degree of co-occurrence/intensity across Jack/Maven/Connector dimensions.

## Compatibility terms

- `organizationalCoupling` is retained as compatibility field for `docs/example_ir.json`.
- `noc` is the explicit pairwise normalized organizational coupling field in the enriched model.

## Practical writing rules for planning docs

1. Always qualify whether coupling is **technical** or **organizational**.
2. Use **PTC** only for organizational cohesion in this extension context.
3. Use **NOC** for pairwise normalized values and **AOC** for aggregate window-level values.
4. Keep role definitions (Jack/Maven/Connector/RSI) aligned with this glossary.
