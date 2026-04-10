# M1 Environment Setup (Build/Test Baseline)

## Detected toolchain requirements

From repository build files:
- `pom.xml` sets `maven.compiler.source=25` and `maven.compiler.target=25`.
- `maven-compiler-plugin` also explicitly uses `<source>25</source>` and `<target>25</target>`.
- Maven wrapper is present (`./mvnw`) and `.mvn/wrapper/maven-wrapper.properties` pins Maven `3.9.11`.

Minimum requirements to build/test this repo:
- Java: **JDK 25**
- Maven: **3.9.11** (via wrapper, preferred)

## Preferred build path

Use the project wrapper first:
```bash
./mvnw
```

System Maven is optional fallback:
```bash
mvn
```

## Setup commands (Linux/macOS)

### Option A: SDKMAN (recommended for both Linux and macOS)

```bash
# install SDKMAN (if missing)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# install required JDK
sdk install java 25-tem

# optional: install Maven (wrapper already pins Maven, so this is not required)
sdk install maven 3.9.11

# activate for current shell
sdk use java 25-tem
sdk use maven 3.9.11
```

### Option B: macOS (Homebrew)

```bash
brew update
brew install openjdk@25 maven

echo 'export JAVA_HOME="$(/usr/libexec/java_home -v 25)"' >> ~/.zshrc
echo 'export PATH="/opt/homebrew/opt/openjdk@25/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### Option C: Linux (APT-based, if your distro provides JDK 25)

```bash
sudo apt-get update
sudo apt-get install -y openjdk-25-jdk maven

export JAVA_HOME="/usr/lib/jvm/java-25-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"
```

## Validation commands

Run from repository root:

```bash
java -version
./mvnw -version
# optional fallback check
mvn -version

# dependency resolution
./mvnw -DskipTests dependency:resolve -Dstyle.color=never

# minimal compile validation (without tests)
./mvnw -DskipTests clean compile
```

## Validation results on this machine

- `java -version`: `25.0.2` (Oracle JDK)
- `./mvnw -version`: Maven `3.9.11`
- `mvn -version`: Maven `3.9.11`
- `./mvnw -DskipTests dependency:resolve`: **BUILD SUCCESS**
- `./mvnw -DskipTests clean compile`: **BUILD SUCCESS**

## Repository-specific caveats

- `README.md` currently says Java 11+/Maven 3.6+, but active `pom.xml` now requires Java 25 source/target.
- Maven warns that `org.codehaus.mojo:exec-maven-plugin` is declared twice in `pom.xml`.
- During `delombok-main`, logs include many import/symbol error lines from `src/main/java`; however, Maven still completes with `BUILD SUCCESS` and compiles generated sources under `target/generated-sources/delombok`.
- On JDK 25, runtime warnings appear from transitive tooling (`jansi`, `sun.misc.Unsafe` usage).

## Installation changes made in this run

No additional tools were installed, because required Java and Maven tooling were already available locally.
