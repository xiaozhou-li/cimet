# M1 Pipeline Skeleton Test (M1-US1)

## Commands run

From repository root:

```bash
# 1) Compile validation
./mvnw -DskipTests clean compile

# 2) Legacy/additive guard inspection
rg -n "invokeOrganizationalIntegrationStub|OrganizationalIntegrationMode\.DISABLED|new OrganizationalAnalysisService\(\)\.run\(mode\)" \
  src/main/java/edu/university/ecs/lab/detection/DetectionService.java \
  src/main/java/edu/university/ecs/lab/organizational/services/SystemPropertyOrganizationalIntegrationContract.java \
  src/main/java/edu/university/ecs/lab/organizational/services/OrganizationalIntegrationContract.java

# 3) Smoke invocation test for M1 skeleton and default-disabled mode
cat > /tmp/M1PipelineSkeletonSmoke.java <<'JAVA'
import edu.university.ecs.lab.organizational.services.OrganizationalAnalysisService;
import edu.university.ecs.lab.organizational.services.OrganizationalIntegrationContract;
import edu.university.ecs.lab.organizational.services.OrganizationalIntegrationMode;
import edu.university.ecs.lab.organizational.services.OrganizationalPipelineResult;
import edu.university.ecs.lab.organizational.services.SystemPropertyOrganizationalIntegrationContract;

public class M1PipelineSkeletonSmoke {
    public static void main(String[] args) {
        System.clearProperty(OrganizationalIntegrationContract.MODE_PROPERTY);

        OrganizationalIntegrationMode resolved =
                new SystemPropertyOrganizationalIntegrationContract().resolveMode();
        if (resolved != OrganizationalIntegrationMode.DISABLED) {
            throw new IllegalStateException("Expected DISABLED default mode but got " + resolved);
        }

        OrganizationalAnalysisService service = new OrganizationalAnalysisService();

        OrganizationalPipelineResult disabledResult = service.runWithResult(resolved);
        if (!disabledResult.getContributions().isEmpty()
                || !disabledResult.getNormalizedDeveloperIdentities().isEmpty()
                || !disabledResult.getServiceTouches().isEmpty()
                || !disabledResult.getTraceabilityEdges().isEmpty()) {
            throw new IllegalStateException("Disabled mode should remain empty/additive");
        }

        OrganizationalPipelineResult skeletonResult =
                service.runWithResult(OrganizationalIntegrationMode.M1_SKELETON);
        if (!skeletonResult.getStageOrder().equals(OrganizationalAnalysisService.M1_STAGE_ORDER)) {
            throw new IllegalStateException("M1 skeleton stage order mismatch");
        }

        System.out.println("resolvedMode=" + resolved);
        System.out.println("m1StageCount=" + skeletonResult.getStageOrder().size());
        System.out.println("disabledResultEmpty=true");
        System.out.println("skeletonInvocationOk=true");
    }
}
JAVA
javac -cp target/classes -d /tmp /tmp/M1PipelineSkeletonSmoke.java
java -cp /tmp:target/classes M1PipelineSkeletonSmoke
```

## Compile/test result

- `./mvnw -DskipTests clean compile`: **BUILD SUCCESS**
- Smoke invocation output:
  - `resolvedMode=DISABLED`
  - `m1StageCount=4`
  - `disabledResultEmpty=true`
  - `skeletonInvocationOk=true`

Status: **PASS**

## Proof M1 pipeline path is additive-only

1. Default mode resolution remains disabled when no property is set:
   - `SystemPropertyOrganizationalIntegrationContract#resolveMode()` uses `OrganizationalIntegrationMode.from(System.getProperty(...))`.
   - Smoke test confirmed `resolvedMode=DISABLED`.
2. Legacy detection flow still short-circuits when mode is disabled:
   - `DetectionService#invokeOrganizationalIntegrationStub()` returns immediately on `mode == DISABLED` before invoking organizational service.
3. M1 path requires explicit opt-in mode values (`m1`, `m1-skeleton`, etc.) and is not the default path.
4. In disabled/default mode, organizational result remains empty placeholders and does not alter technical pipeline outputs.

Conclusion: M1-US1 skeleton compiles and can be invoked in placeholder mode, while default legacy technical behavior remains unchanged.
