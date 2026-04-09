package edu.university.ecs.lab.organizational.models;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.models.serialization.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Top-level socio-technical artifact model scaffold.
 *
 * <p>This model embeds the existing technical IR while adding placeholder sections
 * for organizational and traceability content.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocioTechnicalMicroserviceSystem implements JsonSerializable {
    /**
     * Artifact type discriminator.
     */
    private String type = "socio-technical-ir";

    /**
     * Artifact/system name.
     */
    private String name = "";

    /**
     * Optional schema version marker.
     */
    private String schemaVersion = "m0";

    /**
     * Optional metadata for artifact-level annotations.
     */
    private Map<String, String> metadata = new LinkedHashMap<>();

    /**
     * Embedded existing technical IR.
     */
    private MicroserviceSystem technical;

    /**
     * Placeholder organizational section.
     */
    private OrganizationalSection organizational = new OrganizationalSection();

    /**
     * Placeholder traceability section.
     */
    private TraceabilitySection traceability = new TraceabilitySection();

    /**
     * Convenience constructor for wrapping a technical IR artifact.
     *
     * @param name system/artifact name
     * @param technical technical IR model to embed
     */
    public SocioTechnicalMicroserviceSystem(String name, MicroserviceSystem technical) {
        this.name = name;
        this.technical = technical;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        // Keep top-level ordering explicit for deterministic scaffold serialization.
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("name", name);
        jsonObject.add("technical", technical == null ? JsonNull.INSTANCE : technical.toJsonObject());
        jsonObject.add("organizational", organizational == null ? JsonNull.INSTANCE : organizational.toJsonObject());
        jsonObject.add("traceability", traceability == null ? JsonNull.INSTANCE : traceability.toJsonObject());

        if (schemaVersion != null && !schemaVersion.isBlank()) {
            jsonObject.addProperty("schemaVersion", schemaVersion);
        }

        if (metadata != null && !metadata.isEmpty()) {
            JsonObject metadataObject = new JsonObject();
            for (Map.Entry<String, String> entry : new TreeMap<>(metadata).entrySet()) {
                metadataObject.addProperty(entry.getKey(), entry.getValue());
            }
            jsonObject.add("metadata", metadataObject);
        }

        return jsonObject;
    }
}

