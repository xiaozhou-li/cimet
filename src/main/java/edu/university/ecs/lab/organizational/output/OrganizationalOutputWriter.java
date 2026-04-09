package edu.university.ecs.lab.organizational.output;

/**
 * Baseline contract for writing organizational-analysis outputs.
 */
public interface OrganizationalOutputWriter {

    /**
     * Writes organizational output to a target sink.
     */
    void write();
}
