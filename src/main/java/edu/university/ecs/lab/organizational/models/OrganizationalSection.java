package edu.university.ecs.lab.organizational.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal organizational section scaffold for socio-technical artifacts.
 *
 * <p>Detailed organizational entities (developers, ownership, cohesion/coupling,
 * roles, windows) are intentionally deferred to M1+ milestones.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationalSection implements JsonSerializable {
    /**
     * Section status marker to indicate M0 scaffold state.
     */
    private String status = "stub";

    /**
     * Human-readable note about scaffold completeness.
     */
    private String note = "Organizational analysis is not populated in M0.";

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("note", note);
        return jsonObject;
    }
}

