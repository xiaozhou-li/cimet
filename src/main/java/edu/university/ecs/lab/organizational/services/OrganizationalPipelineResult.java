package edu.university.ecs.lab.organizational.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Structured result for the M1 organizational pipeline skeleton.
 *
 * <p>The model is intentionally minimal and traceability-first. It captures
 * stage outputs even when stages are placeholders, guaranteeing a stable shape
 * for downstream callers.</p>
 */
public final class OrganizationalPipelineResult {
    private final List<String> stageOrder;
    private final List<CommitContribution> contributions;
    private final Map<String, String> normalizedDeveloperIdentities;
    private final List<ServiceTouch> serviceTouches;
    private final List<TraceabilityEdge> traceabilityEdges;

    public OrganizationalPipelineResult(
            List<String> stageOrder,
            List<CommitContribution> contributions,
            Map<String, String> normalizedDeveloperIdentities,
            List<ServiceTouch> serviceTouches,
            List<TraceabilityEdge> traceabilityEdges
    ) {
        this.stageOrder = immutableList(stageOrder);
        this.contributions = immutableList(contributions);
        this.normalizedDeveloperIdentities = immutableMap(normalizedDeveloperIdentities);
        this.serviceTouches = immutableList(serviceTouches);
        this.traceabilityEdges = immutableList(traceabilityEdges);
    }

    public static OrganizationalPipelineResult empty(List<String> stageOrder) {
        return new OrganizationalPipelineResult(stageOrder, List.of(), Map.of(), List.of(), List.of());
    }

    public List<String> getStageOrder() {
        return stageOrder;
    }

    public List<CommitContribution> getContributions() {
        return contributions;
    }

    public Map<String, String> getNormalizedDeveloperIdentities() {
        return normalizedDeveloperIdentities;
    }

    public List<ServiceTouch> getServiceTouches() {
        return serviceTouches;
    }

    public List<TraceabilityEdge> getTraceabilityEdges() {
        return traceabilityEdges;
    }

    private static <T> List<T> immutableList(List<T> input) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }

        return Collections.unmodifiableList(new ArrayList<>(input));
    }

    private static Map<String, String> immutableMap(Map<String, String> input) {
        if (input == null || input.isEmpty()) {
            return Map.of();
        }

        Map<String, String> sorted = new TreeMap<>(input);
        return Collections.unmodifiableMap(new LinkedHashMap<>(sorted));
    }

    /**
     * Commit-level contribution record used by extraction and traceability stages.
     */
    public static final class CommitContribution {
        private final String commitId;
        private final String developerIdentity;
        private final List<String> touchedFiles;

        public CommitContribution(String commitId, String developerIdentity, List<String> touchedFiles) {
            this.commitId = defaultString(commitId);
            this.developerIdentity = defaultString(developerIdentity);
            this.touchedFiles = immutableList(touchedFiles);
        }

        public String getCommitId() {
            return commitId;
        }

        public String getDeveloperIdentity() {
            return developerIdentity;
        }

        public List<String> getTouchedFiles() {
            return touchedFiles;
        }
    }

    /**
     * Commit/file to service mapping output.
     */
    public static final class ServiceTouch {
        private final String commitId;
        private final String filePath;
        private final String microserviceName;

        public ServiceTouch(String commitId, String filePath, String microserviceName) {
            this.commitId = defaultString(commitId);
            this.filePath = defaultString(filePath);
            this.microserviceName = defaultString(microserviceName);
        }

        public String getCommitId() {
            return commitId;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getMicroserviceName() {
            return microserviceName;
        }
    }

    /**
     * Minimal traceability edge produced by the M1 skeleton.
     */
    public static final class TraceabilityEdge {
        private final String edgeType;
        private final String fromType;
        private final String fromId;
        private final String toType;
        private final String toId;

        public TraceabilityEdge(String edgeType, String fromType, String fromId, String toType, String toId) {
            this.edgeType = defaultString(edgeType);
            this.fromType = defaultString(fromType);
            this.fromId = defaultString(fromId);
            this.toType = defaultString(toType);
            this.toId = defaultString(toId);
        }

        public String getEdgeType() {
            return edgeType;
        }

        public String getFromType() {
            return fromType;
        }

        public String getFromId() {
            return fromId;
        }

        public String getToType() {
            return toType;
        }

        public String getToId() {
            return toId;
        }
    }

    private static String defaultString(String value) {
        return Objects.requireNonNullElse(value, "");
    }
}
