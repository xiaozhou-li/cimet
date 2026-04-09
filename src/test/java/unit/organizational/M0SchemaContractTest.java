package unit.organizational;

import com.google.gson.JsonObject;
import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
import edu.university.ecs.lab.organizational.models.SocioTechnicalMicroserviceSystem;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract checks for M0 schema scaffold and compatibility mapping.
 */
public class M0SchemaContractTest {

    private static final Path GLOSSARY_PATH = Path.of("docs/planning-glossary.md");
    private static final Path CONTRACT_PATH = Path.of("docs/m0-schema-contract.md");
    private static final Path EXAMPLE_PATH = Path.of("docs/example_ir.json");

    @Test
    public void scaffoldContainsRequiredTopLevelKeys() {
        SocioTechnicalMicroserviceSystem scaffold = new SocioTechnicalMicroserviceSystem(
                "sample-system",
                new MicroserviceSystem("sample-system", "m0commit", new HashSet<>(), new HashSet<>())
        );

        JsonObject json = scaffold.toJsonObject();

        assertTrue(json.has("type"));
        assertTrue(json.has("name"));
        assertTrue(json.has("technical"));
        assertTrue(json.has("organizational"));
        assertTrue(json.has("traceability"));
    }

    @Test
    public void contractTerminologyMatchesPlanningGlossary() throws IOException {
        String glossary = Files.readString(GLOSSARY_PATH, StandardCharsets.UTF_8).toLowerCase();
        String contract = Files.readString(CONTRACT_PATH, StandardCharsets.UTF_8).toLowerCase();

        List<String> requiredTerms = List.of(
                "technical coupling",
                "organizational coupling",
                "cohesion",
                "ptc",
                "noc",
                "aoc",
                "organizationalcoupling",
                "noc"
        );

        for (String term : requiredTerms) {
            assertTrue(glossary.contains(term), "Glossary missing required term: " + term);
            assertTrue(contract.contains(term), "Contract missing glossary-aligned term: " + term);
        }
    }

    @Test
    public void scaffoldAndExampleAlignAtKeyPathLevelWithDeferredMapping() throws IOException {
        String example = Files.readString(EXAMPLE_PATH, StandardCharsets.UTF_8);
        String contract = Files.readString(CONTRACT_PATH, StandardCharsets.UTF_8);

        // Required shared key paths for M0 compatibility.
        List<String> sharedTopLevelPaths = List.of(
                "\"type\"",
                "\"name\"",
                "\"technical\"",
                "\"organizational\"",
                "\"traceability\""
        );

        for (String keyPath : sharedTopLevelPaths) {
            assertTrue(example.contains(keyPath), "example_ir.json missing required top-level key: " + keyPath);
        }

        // Paths present in example_ir.json but intentionally deferred by M0 contract.
        List<String> deferredExamplePaths = List.of(
                "organizational.developers",
                "organizational.commits",
                "organizational.issues",
                "organizational.serviceOwnership",
                "organizational.servicePairRelations",
                "traceability.developerToCommit",
                "traceability.commitToIssue",
                "traceability.commitToFile",
                "traceability.developerToMicroservice"
        );

        for (String deferredPath : deferredExamplePaths) {
            assertTrue(contract.contains(deferredPath),
                    "Deferred compatibility path not explicitly documented: " + deferredPath);
        }

        // Type compatibility alias must be explicit.
        assertTrue(contract.contains("SocioTechnicalMicroserviceSystem"));
        assertTrue(contract.contains("socio-technical-ir"));
    }
}
