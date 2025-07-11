# Elide Maven Plugin

This plugin can be consumed in a Maven project to use [Elide](https://elide.dev) for compiling Java and Kotlin sources.

> [!WARNING]
> This plugin is currently under development.

## Features

- [x] Swap out `javac ...` for `elide javac -- ...`
- [x] Supports explicit path to `elide`
- [x] Resolve `elide` via the `PATH`
- [x] Swap out `kotlinc ...` for `elide kotlinc -- ...`
- [ ] Usability of Elide as a Maven toolchain

## Usage

### Java

Configuring Elide as your `javac` compiler:

**`pom.xml`**
```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <dependencies>
                <dependency>
                    <groupId>dev.elide</groupId>
                    <artifactId>elide-plexus-compilers</artifactId>
                    <version>1.0.0-SNAPSHOT</version>
                </dependency>
            </dependencies>
            <configuration>
                <compilerId>elide</compilerId>
            </configuration>
        </plugin>
    </plugins>
</build>
```

> [!TIP]
> See the [Java sample project](java-sample) for a usage example. Elide also provides
> a [Gradle plugin](https://github.com/elide-dev/gradle).

### Kotlin

Configuring the Elide Kotlin plugin is done the exact same way as configuring the Kotlin Maven plugin, just replacing 
the `groupId` and `artifactId`:

**`pom.xml`**
```xml
<build>
    <sourceDirectory>src/main/kotlin</sourceDirectory>
    <testSourceDirectory>src/test/kotlin</testSourceDirectory>
    <plugins>
        <plugin>
            <groupId>dev.elide</groupId>
            <artifactId>elide-kotlin-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <id>compile</id>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
                <execution>
                    <id>test-compile</id>
                    <goals>
                        <goal>test-compile</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>

<dependencies>
    <dependency>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-stdlib</artifactId>
        <version>2.2.0</version>
    </dependency>
</dependencies>
```

> [!TIP]
> See the [Kotlin sample project](kotlin-sample) for a usage example. Elide also provides
> a [Gradle plugin](https://github.com/elide-dev/gradle).