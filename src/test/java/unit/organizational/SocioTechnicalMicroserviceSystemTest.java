package unit.organizational;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
import edu.university.ecs.lab.organizational.models.OrganizationalSection;
import edu.university.ecs.lab.organizational.models.SocioTechnicalMicroserviceSystem;
import edu.university.ecs.lab.organizational.models.TraceabilitySection;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the M0 socio-technical top-level model scaffold.
 */
public class SocioTechnicalMicroserviceSystemTest {

    private static final String GOLDEN_FIXTURE_PATH =
            "src/test/resources/fixtures/organizational/m0-socio-technical-golden.json";

    @Test
    public void constructionAndDefaults_work() {
        MicroserviceSystem technical = new MicroserviceSystem(
                "sample-system",
                "abcd1234",
                new HashSet<>(),
                new HashSet<>()
        );

        SocioTechnicalMicroserviceSystem socioTechnical =
                new SocioTechnicalMicroserviceSystem("sample-system", technical);

        assertEquals("socio-technical-ir", socioTechnical.getType());
        assertEquals("sample-system", socioTechnical.getName());
        assertEquals("m0", socioTechnical.getSchemaVersion());
        assertNotNull(socioTechnical.getTechnical());
        assertNotNull(socioTechnical.getOrganizational());
        assertNotNull(socioTechnical.getTraceability());
        assertTrue(socioTechnical.getMetadata().isEmpty());
    }

    @Test
    public void toJsonObject_containsRequiredTopLevelKeysInExpectedOrder() {
        SocioTechnicalMicroserviceSystem socioTechnical = new SocioTechnicalMicroserviceSystem(
                "sample-system",
                new MicroserviceSystem("sample-system", "abcd1234", new HashSet<>(), new HashSet<>())
        );

        JsonObject json = socioTechnical.toJsonObject();
        String compact = json.toString();

        assertTrue(compact.startsWith("{\"type\""));
        assertTrue(compact.contains("\"name\""));
        assertTrue(compact.contains("\"technical\""));
        assertTrue(compact.contains("\"organizational\""));
        assertTrue(compact.contains("\"traceability\""));
        assertTrue(compact.contains("\"schemaVersion\""));

        int typeIndex = compact.indexOf("\"type\"");
        int nameIndex = compact.indexOf("\"name\"");
        int technicalIndex = compact.indexOf("\"technical\"");
        int organizationalIndex = compact.indexOf("\"organizational\"");
        int traceabilityIndex = compact.indexOf("\"traceability\"");

        assertTrue(typeIndex < nameIndex);
        assertTrue(nameIndex < technicalIndex);
        assertTrue(technicalIndex < organizationalIndex);
        assertTrue(organizationalIndex < traceabilityIndex);
    }

    @Test
    public void technicalEmbedding_serializesWithoutFailure() {
        MicroserviceSystem technical = new MicroserviceSystem(
                "embedded-tech",
                "commit-001",
                new HashSet<>(),
                new HashSet<>()
        );

        SocioTechnicalMicroserviceSystem socioTechnical =
                new SocioTechnicalMicroserviceSystem("embedded-tech", technical);

        JsonObject json = socioTechnical.toJsonObject();

        assertTrue(json.has("technical"));
        JsonObject technicalJson = json.getAsJsonObject("technical");
        assertEquals("embedded-tech", technicalJson.get("name").getAsString());
        assertEquals("commit-001", technicalJson.get("commitID").getAsString());
        assertTrue(technicalJson.getAsJsonArray("microservices").isEmpty());
        assertTrue(technicalJson.getAsJsonArray("orphans").isEmpty());
    }

    @Test
    public void emptyOrganizationalAndTraceabilitySections_serializeSafely() {
        SocioTechnicalMicroserviceSystem socioTechnical = new SocioTechnicalMicroserviceSystem(
                "safe-empty-sections",
                new MicroserviceSystem("safe-empty-sections", "commit-002", new HashSet<>(), new HashSet<>())
        );

        socioTechnical.setOrganizational(new OrganizationalSection());
        socioTechnical.setTraceability(new TraceabilitySection());

        JsonObject json = socioTechnical.toJsonObject();

        assertTrue(json.has("organizational"));
        assertTrue(json.has("traceability"));
        assertTrue(json.getAsJsonObject("organizational").has("status"));
        assertTrue(json.getAsJsonObject("traceability").has("contractVersion"));
    }

    @Test
    public void serializationMatchesGoldenFixture() throws IOException {
        SocioTechnicalMicroserviceSystem socioTechnical = new SocioTechnicalMicroserviceSystem(
                "sample-system",
                new MicroserviceSystem("sample-system", "abcd1234", new HashSet<>(), new HashSet<>())
        );

        JsonObject actual = socioTechnical.toJsonObject();

        String expectedText = Files.readString(Path.of(GOLDEN_FIXTURE_PATH), StandardCharsets.UTF_8);
        JsonObject expected = JsonParser.parseString(expectedText).getAsJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void writePathViaJsonReadWriteUtils_supportsProjectConvention() throws IOException {
        SocioTechnicalMicroserviceSystem socioTechnical = new SocioTechnicalMicroserviceSystem(
                "write-check",
                new MicroserviceSystem("write-check", "commit-003", new HashSet<>(), new HashSet<>())
        );

        Path outputPath = Files.createTempFile("cimet-socio-technical-", ".json");
        try {
            JsonReadWriteUtils.writeToJSON(outputPath.toString(), socioTechnical.toJsonObject());

            String written = Files.readString(outputPath, StandardCharsets.UTF_8);
            JsonObject parsed = JsonParser.parseString(written).getAsJsonObject();

            assertTrue(parsed.has("type"));
            assertTrue(parsed.has("technical"));
            assertTrue(parsed.has("organizational"));
            assertTrue(parsed.has("traceability"));
        } finally {
            Files.deleteIfExists(outputPath);
        }
    }
}
