package edu.university.ecs.lab.organizational.models;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal traceability section scaffold for socio-technical artifacts.
 *
 * <p>Commit-level provenance and lineage details are intentionally deferred to M1+.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceabilitySection implements JsonSerializable {
    /**
     * Contract version for traceability section shape.
     */
    private String contractVersion = "m0";

    /**
     * Human-readable note about traceability scaffold completeness.
     */
    private String note = "Traceability details are not populated in M0.";

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("contractVersion", contractVersion);
        jsonObject.addProperty("note", note);
        return jsonObject;
    }
}

