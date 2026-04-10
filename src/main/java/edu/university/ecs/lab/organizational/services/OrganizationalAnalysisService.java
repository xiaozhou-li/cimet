package edu.university.ecs.lab.organizational.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Baseline service scaffold for organizational-analysis orchestration.
 *
 * <p>Legacy runtime paths remain unchanged by default. The M1 skeleton pipeline is
 * additive and executes only when explicitly requested.</p>
 */
public class OrganizationalAnalysisService {
    public static final String STAGE_EXTRACT_COMMIT_CONTRIBUTIONS = "extract-commit-contributions";
    public static final String STAGE_NORMALIZE_DEVELOPER_IDENTITIES = "normalize-developer-identities";
    public static final String STAGE_MAP_FILES_TO_MICROSERVICES = "map-files-to-microservices";
    public static final String STAGE_EMIT_TRACEABILITY_EDGES = "emit-traceability-edges";

    public static final List<String> M1_STAGE_ORDER = List.of(
            STAGE_EXTRACT_COMMIT_CONTRIBUTIONS,
            STAGE_NORMALIZE_DEVELOPER_IDENTITIES,
            STAGE_MAP_FILES_TO_MICROSERVICES,
            STAGE_EMIT_TRACEABILITY_EDGES
    );

    private static final Comparator<OrganizationalPipelineResult.CommitContribution> CONTRIBUTION_COMPARATOR =
            Comparator.comparing(OrganizationalPipelineResult.CommitContribution::getCommitId)
                    .thenComparing(OrganizationalPipelineResult.CommitContribution::getDeveloperIdentity)
                    .thenComparing(contribution -> String.join("|", contribution.getTouchedFiles()));

    private static final Comparator<OrganizationalPipelineResult.ServiceTouch> SERVICE_TOUCH_COMPARATOR =
            Comparator.comparing(OrganizationalPipelineResult.ServiceTouch::getCommitId)
                    .thenComparing(OrganizationalPipelineResult.ServiceTouch::getFilePath)
                    .thenComparing(OrganizationalPipelineResult.ServiceTouch::getMicroserviceName);

    private static final Comparator<OrganizationalPipelineResult.TraceabilityEdge> TRACEABILITY_EDGE_COMPARATOR =
            Comparator.comparing(OrganizationalPipelineResult.TraceabilityEdge::getEdgeType)
                    .thenComparing(OrganizationalPipelineResult.TraceabilityEdge::getFromType)
                    .thenComparing(OrganizationalPipelineResult.TraceabilityEdge::getFromId)
                    .thenComparing(OrganizationalPipelineResult.TraceabilityEdge::getToType)
                    .thenComparing(OrganizationalPipelineResult.TraceabilityEdge::getToId);

    private final CommitContributionStage commitContributionStage;
    private final IdentityNormalizationStage identityNormalizationStage;
    private final ServiceMappingStage serviceMappingStage;
    private final TraceabilityEdgeStage traceabilityEdgeStage;

    public OrganizationalAnalysisService() {
        this(
                new EmptyCommitContributionStage(),
                new SimpleIdentityNormalizationStage(),
                new EmptyServiceMappingStage(),
                new SimpleTraceabilityEdgeStage()
        );
    }

    public OrganizationalAnalysisService(
            CommitContributionStage commitContributionStage,
            IdentityNormalizationStage identityNormalizationStage,
            ServiceMappingStage serviceMappingStage,
            TraceabilityEdgeStage traceabilityEdgeStage
    ) {
        this.commitContributionStage = Objects.requireNonNull(commitContributionStage, "commitContributionStage");
        this.identityNormalizationStage = Objects.requireNonNull(identityNormalizationStage, "identityNormalizationStage");
        this.serviceMappingStage = Objects.requireNonNull(serviceMappingStage, "serviceMappingStage");
        this.traceabilityEdgeStage = Objects.requireNonNull(traceabilityEdgeStage, "traceabilityEdgeStage");
    }

    /**
     * Placeholder execution hook for future milestone wiring.
     */
    public void run(OrganizationalIntegrationMode mode) {
        runWithResult(mode);
    }

    /**
     * Executes organizational integration mode and always returns a structured result.
     */
    public OrganizationalPipelineResult runWithResult(OrganizationalIntegrationMode mode) {
        if (mode == null || mode == OrganizationalIntegrationMode.DISABLED || mode == OrganizationalIntegrationMode.STUB) {
            return OrganizationalPipelineResult.empty(M1_STAGE_ORDER);
        }

        if (mode == OrganizationalIntegrationMode.M1_SKELETON) {
            return runM1PipelineSkeleton();
        }

        return OrganizationalPipelineResult.empty(M1_STAGE_ORDER);
    }

    /**
     * Deterministic M1 pipeline skeleton.
     *
     * <p>Stages are additive placeholders and do not yet compute PTC/NOC/AOC/roles.</p>
     */
    public OrganizationalPipelineResult runM1PipelineSkeleton() {
        List<OrganizationalPipelineResult.CommitContribution> extractedContributions =
                sortContributions(commitContributionStage.extractCommitContributions());

        Map<String, String> normalizedIdentities =
                sortIdentityMap(identityNormalizationStage.normalizeDeveloperIdentities(extractedContributions));

        List<OrganizationalPipelineResult.ServiceTouch> serviceTouches =
                sortServiceTouches(serviceMappingStage.mapFilesToMicroservices(extractedContributions, normalizedIdentities));

        List<OrganizationalPipelineResult.TraceabilityEdge> traceabilityEdges =
                sortTraceabilityEdges(traceabilityEdgeStage.emitTraceabilityEdges(
                        extractedContributions,
                        normalizedIdentities,
                        serviceTouches
                ));

        return new OrganizationalPipelineResult(
                M1_STAGE_ORDER,
                extractedContributions,
                normalizedIdentities,
                serviceTouches,
                traceabilityEdges
        );
    }

    private static List<OrganizationalPipelineResult.CommitContribution> sortContributions(
            List<OrganizationalPipelineResult.CommitContribution> contributions
    ) {
        if (contributions == null || contributions.isEmpty()) {
            return List.of();
        }

        List<OrganizationalPipelineResult.CommitContribution> normalized = new ArrayList<>(contributions.size());
        for (OrganizationalPipelineResult.CommitContribution contribution : contributions) {
            if (contribution == null) {
                continue;
            }

            List<String> deduplicatedFiles = contribution.getTouchedFiles() == null
                    ? List.of()
                    : new ArrayList<>(new LinkedHashSet<>(contribution.getTouchedFiles()));
            deduplicatedFiles.sort(String::compareTo);
            normalized.add(new OrganizationalPipelineResult.CommitContribution(
                    contribution.getCommitId(),
                    contribution.getDeveloperIdentity(),
                    deduplicatedFiles
            ));
        }

        normalized.sort(CONTRIBUTION_COMPARATOR);
        return normalized;
    }

    private static Map<String, String> sortIdentityMap(Map<String, String> identities) {
        if (identities == null || identities.isEmpty()) {
            return Map.of();
        }

        Map<String, String> sorted = new TreeMap<>(identities);
        return new LinkedHashMap<>(sorted);
    }

    private static List<OrganizationalPipelineResult.ServiceTouch> sortServiceTouches(
            List<OrganizationalPipelineResult.ServiceTouch> serviceTouches
    ) {
        if (serviceTouches == null || serviceTouches.isEmpty()) {
            return List.of();
        }

        List<OrganizationalPipelineResult.ServiceTouch> normalized = new ArrayList<>(serviceTouches);
        normalized.removeIf(Objects::isNull);
        normalized.sort(SERVICE_TOUCH_COMPARATOR);
        return normalized;
    }

    private static List<OrganizationalPipelineResult.TraceabilityEdge> sortTraceabilityEdges(
            List<OrganizationalPipelineResult.TraceabilityEdge> traceabilityEdges
    ) {
        if (traceabilityEdges == null || traceabilityEdges.isEmpty()) {
            return List.of();
        }

        List<OrganizationalPipelineResult.TraceabilityEdge> normalized = new ArrayList<>(traceabilityEdges);
        normalized.removeIf(Objects::isNull);
        normalized.sort(TRACEABILITY_EDGE_COMPARATOR);
        return normalized;
    }

    /**
     * Stage 1: extract commit-level contribution records.
     */
    public interface CommitContributionStage {
        List<OrganizationalPipelineResult.CommitContribution> extractCommitContributions();
    }

    /**
     * Stage 2: normalize developer identities.
     */
    public interface IdentityNormalizationStage {
        Map<String, String> normalizeDeveloperIdentities(
                List<OrganizationalPipelineResult.CommitContribution> contributions
        );
    }

    /**
     * Stage 3: map touched files to microservices.
     */
    public interface ServiceMappingStage {
        List<OrganizationalPipelineResult.ServiceTouch> mapFilesToMicroservices(
                List<OrganizationalPipelineResult.CommitContribution> contributions,
                Map<String, String> normalizedDeveloperIdentities
        );
    }

    /**
     * Stage 4: emit traceability edges.
     */
    public interface TraceabilityEdgeStage {
        List<OrganizationalPipelineResult.TraceabilityEdge> emitTraceabilityEdges(
                List<OrganizationalPipelineResult.CommitContribution> contributions,
                Map<String, String> normalizedDeveloperIdentities,
                List<OrganizationalPipelineResult.ServiceTouch> serviceTouches
        );
    }

    private static final class EmptyCommitContributionStage implements CommitContributionStage {
        @Override
        public List<OrganizationalPipelineResult.CommitContribution> extractCommitContributions() {
            return List.of();
        }
    }

    private static final class SimpleIdentityNormalizationStage implements IdentityNormalizationStage {
        @Override
        public Map<String, String> normalizeDeveloperIdentities(
                List<OrganizationalPipelineResult.CommitContribution> contributions
        ) {
            if (contributions == null || contributions.isEmpty()) {
                return Map.of();
            }

            Map<String, String> normalized = new LinkedHashMap<>();
            for (OrganizationalPipelineResult.CommitContribution contribution : contributions) {
                String raw = contribution.getDeveloperIdentity();
                if (raw == null || raw.isBlank()) {
                    continue;
                }

                String canonical = raw.trim().toLowerCase();
                normalized.putIfAbsent(raw, canonical);
            }
            return normalized;
        }
    }

    private static final class EmptyServiceMappingStage implements ServiceMappingStage {
        @Override
        public List<OrganizationalPipelineResult.ServiceTouch> mapFilesToMicroservices(
                List<OrganizationalPipelineResult.CommitContribution> contributions,
                Map<String, String> normalizedDeveloperIdentities
        ) {
            return List.of();
        }
    }

    private static final class SimpleTraceabilityEdgeStage implements TraceabilityEdgeStage {
        @Override
        public List<OrganizationalPipelineResult.TraceabilityEdge> emitTraceabilityEdges(
                List<OrganizationalPipelineResult.CommitContribution> contributions,
                Map<String, String> normalizedDeveloperIdentities,
                List<OrganizationalPipelineResult.ServiceTouch> serviceTouches
        ) {
            if (contributions == null || contributions.isEmpty()) {
                return List.of();
            }

            List<OrganizationalPipelineResult.TraceabilityEdge> edges = new ArrayList<>();
            for (OrganizationalPipelineResult.CommitContribution contribution : contributions) {
                String developer = contribution.getDeveloperIdentity();
                String normalizedDeveloper = normalizedDeveloperIdentities.getOrDefault(developer, developer);

                if (!normalizedDeveloper.isBlank() && !contribution.getCommitId().isBlank()) {
                    edges.add(new OrganizationalPipelineResult.TraceabilityEdge(
                            "developer-to-commit",
                            "developer",
                            normalizedDeveloper,
                            "commit",
                            contribution.getCommitId()
                    ));
                }
            }

            return edges;
        }
    }
}
