# Consistency Review Notes

## Scope reviewed
- `docs/reconnaissance.md`
- `docs/product-requirements.md`
- `docs/user-stories.md`
- `docs/backlog.md`
- `docs/release-plan.md`
- `docs/jira-import.csv`

## Key issues found and corrected

1. **Backlog IDs referenced but not fully defined**
- Issue: `BL-041` to `BL-045` were referenced in risk/release sections but were not explicit backlog entries.
- Correction: Added full backlog items `BL-041..BL-045` under a new section:
  - `Phase 6: Research Spikes and Governance`
  - `Epic L: Research Spikes`
- Result: All referenced backlog IDs are now formally represented with type, priority, estimate, dependencies, DoD, and owner role.

2. **User-story to backlog coverage gap for spike work**
- Issue: User stories stopped at `ORG-040`, while backlog/release planning included additional spike work.
- Correction: Added `ORG-041..ORG-045` in new section:
  - `Epic 9: Research Spikes and Risk Reduction`
- Result: Backlog spike items now have corresponding user stories.

3. **Milestone numbering mismatch across documents**
- Issue: `backlog.md` milestone mapping used `M1..M5`, while release plan used `M0..M5`.
- Correction: Updated `backlog.md` milestone mapping to align with `M0..M5` structure and include spike IDs in milestones.
- Result: Milestone terminology is now consistent across backlog and release plan.

4. **Release plan wording implied non-committed spike IDs**
- Issue: Release plan listed spike IDs as “candidate” even though they are now explicit backlog items.
- Correction: Removed “spike candidate” wording and retained direct backlog IDs.
- Result: Release plan now references committed backlog IDs consistently.

5. **Terminology alignment for coupling metrics**
- Issue: Pairwise coupling terminology was not explicitly aligned everywhere with NOC->AOC framing.
- Correction: Updated `reconnaissance.md` proposed module layout to include:
  - `NOCCalculator` for pairwise normalized coupling
  - `AOCCalculator` as aggregate over pairwise NOC
- Result: Consistent metric terminology across reconnaissance, PRD, backlog, and release plan.

6. **Jira export incompleteness and parsing robustness**
- Issue: `docs/jira-import.csv` initially omitted `BL-041..BL-045` and had malformed tail content due section parsing edge case.
- Correction:
  - Regenerated CSV from updated backlog and release plan.
  - Fixed parser logic to stop section capture at heading boundaries.
  - Ensured epics are emitted first and all child items are included.
- Result: CSV now includes all backlog items and valid rows.

## Current consistency status after corrections

- `ORG` IDs: `ORG-001..ORG-045` present.
- `BL` IDs: `BL-001..BL-045` present.
- All `BL` IDs in backlog are represented in `release-plan.md`.
- `jira-import.csv` includes all backlog child issues plus epics.
- `example_ir.json` compatibility remains explicitly covered in reconnaissance/PRD/backlog/release planning.
- No direct contradictions with current CIMET architecture baseline were introduced; extension remains additive and non-breaking by default.

## Assumptions retained

- `docs/example_ir.json` remains the compatibility target shape (not a strict schema file).
- Issue linking remains optional/heuristic for MVP scope.
- Research spikes are planned backlog scope but may be sequenced in parallel with implementation milestones.


7. **Cross-document terminology baseline added**
- Correction: Added `docs/planning-glossary.md` and linked terminology contracts in planning docs.
- Result: Cohesion vs coupling and technical vs organizational coupling are now explicitly and consistently defined.
