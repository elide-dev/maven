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
            <version>3.15.0</version>
            <dependencies>
                <dependency>
                    <groupId>dev.elide</groupId>
                    <artifactId>elide-plexus-compilers</artifactId>
                    <version>1.0.0</version>
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
> See the [Java sample project](sample-java) for a usage example. Elide also provides
> a [Gradle plugin](https://github.com/elide-dev/gradle).

### Kotlin

Configuring the Elide Kotlin plugin is done the exact same way as configuring the Kotlin Maven plugin, just replacing 
the plugin coordinates:

**`pom.xml`**
```xml
<build>
    <sourceDirectory>src/main/kotlin</sourceDirectory>
    <testSourceDirectory>src/test/kotlin</testSourceDirectory>
    <plugins>
        <plugin>
            <groupId>dev.elide</groupId>
            <artifactId>elide-kotlin-maven-plugin</artifactId>
            <version>1.0.0</version>
            <extensions>true</extensions>
        </plugin>
    </plugins>
</build>
```

> [!TIP]
> See the [Kotlin sample project](sample-kotlin) for a usage example.

### Mixed Java and Kotlin

By combining the Kotlin configuration and Java compiler replacement, mixed Java and Kotlin sources can be compiled with
Elide:

**`pom.xml`**

```xml
<build>
    <plugins>
        <plugin>
            <groupId>dev.elide</groupId>
            <artifactId>elide-kotlin-maven-plugin</artifactId>
            <version>1.0.0</version>
            <extensions>true</extensions>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.15.0</version>
            <dependencies>
                <dependency>
                    <groupId>dev.elide</groupId>
                    <artifactId>elide-plexus-compilers</artifactId>
                    <version>1.0.0</version>
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
> See the [Mixed sources sample project](sample-mixed) for a usage example.