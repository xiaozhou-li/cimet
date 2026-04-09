package edu.university.ecs.lab.organizational.services;

import java.util.Locale;

/**
 * Supported integration modes for organizational analysis.
 *
 * <p>M0 supports only a backward-compatible disabled default and a stub mode.</p>
 */
public enum OrganizationalIntegrationMode {
    DISABLED,
    STUB;

    /**
     * Parses a textual mode value into a known integration mode.
     * Unknown values are treated as {@link #DISABLED}.
     *
     * @param rawMode mode string from configuration source
     * @return parsed integration mode
     */
    public static OrganizationalIntegrationMode from(String rawMode) {
        if (rawMode == null) {
            return DISABLED;
        }

        String normalized = rawMode.trim().toLowerCase(Locale.ROOT);
        if ("stub".equals(normalized) || "enabled".equals(normalized) || "on".equals(normalized) || "true".equals(normalized)) {
            return STUB;
        }

        return DISABLED;
    }
}
