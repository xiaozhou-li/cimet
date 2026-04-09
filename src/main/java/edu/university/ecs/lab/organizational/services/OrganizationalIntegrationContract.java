package edu.university.ecs.lab.organizational.services;

/**
 * Contract defining how organizational analysis is integrated into legacy flows.
 *
 * <p>This contract is additive and disabled by default.</p>
 */
public interface OrganizationalIntegrationContract {

    /**
     * The runtime system property used to select integration mode.
     */
    String MODE_PROPERTY = "cimet.organizational.mode";

    /**
     * Resolves the integration mode to use for the current execution.
     *
     * @return integration mode
     */
    OrganizationalIntegrationMode resolveMode();

    /**
     * Indicates whether organizational integration should execute.
     *
     * @return true if mode is non-disabled
     */
    default boolean isEnabled() {
        return resolveMode() != OrganizationalIntegrationMode.DISABLED;
    }
}
