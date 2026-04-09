package edu.university.ecs.lab.organizational.services;

/**
 * Default contract implementation backed by a JVM system property.
 *
 * <p>By default no property is set, which resolves to disabled mode and preserves
 * legacy runtime behavior.</p>
 */
public class SystemPropertyOrganizationalIntegrationContract implements OrganizationalIntegrationContract {

    @Override
    public OrganizationalIntegrationMode resolveMode() {
        return OrganizationalIntegrationMode.from(System.getProperty(MODE_PROPERTY));
    }
}
