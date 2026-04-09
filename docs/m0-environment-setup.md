# M0 Environment Setup (Build/Test Baseline)

## 1. Detected toolchain requirements

From repository build files:
- `pom.xml` sets `maven.compiler.source=25` and `maven.compiler.target=25`.
- `maven-compiler-plugin` also explicitly compiles with `source=25` and `target=25`.
- Maven wrapper is present (`./mvnw`) and `.mvn/wrapper/maven-wrapper.properties` pins:
  - `apache-maven-3.9.11`

Minimum practical requirements for local build/test:
- Java: **JDK 25** (required by current `pom.xml` compiler settings)
- Maven: **3.9.x+** (wrapper pins 3.9.11; wrapper is preferred)

## 2. Preferred build path

Use repository wrapper first:
- `./mvnw`

Fallback only if wrapper cannot be used:
- `mvn`

## 3. Setup commands (Linux/macOS)

### 3.1 Minimal cross-platform setup using SDKMAN (Linux/macOS)

```bash
# install SDKMAN (if missing)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# install required JDK and Maven
sdk install java 25-tem
sdk install maven 3.9.11

# select active versions
sdk use java 25-tem
sdk use maven 3.9.11
```

### 3.2 macOS (Homebrew alternative)

```bash
brew install openjdk@25 maven
export JAVA_HOME="$(/usr/libexec/java_home -v 25)"
export PATH="/opt/homebrew/opt/openjdk@25/bin:$PATH"
```

### 3.3 Linux (APT-based alternative, if package exists in distro)

```bash
sudo apt-get update
sudo apt-get install -y openjdk-25-jdk maven
export JAVA_HOME="/usr/lib/jvm/java-25-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"
```

## 4. Validation commands

Run from repository root:

```bash
java -version
./mvnw -version
# optional fallback:
mvn -version

# dependency resolution
./mvnw -DskipTests dependency:resolve -Dstyle.color=never

# minimal compile validation (without tests)
./mvnw -DskipTests compile -Dstyle.color=never
```

## 5. Local verification result (this machine)

- `java -version`: `25.0.2` (Oracle JDK)
- `./mvnw -version`: Maven `3.9.11` (wrapper distribution under `~/.m2/wrapper/...`)
- `mvn -version`: Maven `3.9.11` (`/opt/homebrew/bin/mvn`)
- `./mvnw -DskipTests dependency:resolve`: **success**
- `./mvnw -DskipTests compile`: **success**

## 6. Repository-specific caveats

1. Build warns about duplicate plugin declaration in `pom.xml`:
   - duplicate `org.codehaus.mojo:exec-maven-plugin` (Maven model warning).
2. On JDK 25, runtime warnings appear from transitive tooling (`jansi`, `sun.misc.Unsafe` usage from dependencies such as Lombok/Guice).
3. During build, delombok-related output can print import/symbol error text to logs, but current Maven execution still finishes with `BUILD SUCCESS` for `compile` in this repository state.
4. For reproducibility and consistency with repo intent, prefer `./mvnw` over system Maven.

## 7. No extra installation performed in this run

The local machine already had the required Java and Maven tooling available, so no additional packages were installed.
