package edu.university.ecs.lab.organizational.extract;

/**
 * Baseline contract for extracting organizational contribution signals.
 */
public interface ContributionExtractor {

    /**
     * Executes extraction for the configured repository context.
     */
    void extract();
}
