# Elide Maven Plugin

This plugin can be consumed in a Maven project to use [Elide](https://elide.dev).

> [!WARNING]
> This plugin is currently under development.

## Features

- [x] Swap out `javac ...` for `elide javac -- ...`
- [x] Supports explicit path to `elide`
- [ ] Resolve `elide` via the `PATH`
- [ ] Swap out `kotlinc ...` for `elide kotlinc -- ...`
- [ ] Usability of Elide as a Maven toolchain

## Usage

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

Properties needed:
```xml
    <properties>
        <maven.compiler.executable>/path/to/elide</maven.compiler.executable>
        <maven.compiler.source>24</maven.compiler.source>
        <maven.compiler.target>24</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
```

> [!TIP]
> See the [sample project](./sample) for a usage example. Elide also provides a [Gradle plugin](https://github.com/elide-dev/gradle).
