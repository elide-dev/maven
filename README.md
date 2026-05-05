# Elide Maven Plugin

This plugin can be consumed in a Maven project to use [Elide](https://elide.dev) for compiling Java and Kotlin sources.

> [!WARNING]
> This plugin is currently under development.

## Features

- [x] Swap out `javac ...` for `elide javac -- ...`
- [x] Supports explicit path to `elide`
- [x] Resolve `elide` via the `PATH`
- [x] Swap out `kotlinc ...` for `elide kotlinc -- ...`
- [x] Usability of Elide as a Maven toolchain

## Usage

### Elide Plugin

The easiest way to start using Elide in your project is the `elide-maven-plugin`. Enable it and set `extensions` to 
`true` and all of your Java and Kotlin sources will be compiled with Elide `javac` and `kotlinc`.

**`pom.xml`**
```xml
<build>
    <plugins>
        <plugin>
            <groupId>dev.elide</groupId>
            <artifactId>elide-maven-plugin</artifactId>
            <version>1.0.0</version>
            <extensions>true</extensions>
        </plugin>
    </plugins>
</build>
```

> [!TIP]
> See the [Mixed sources sample project](sample-mixed) for a usage example.

### Kotlin drop-in replacement

If you already have a project that uses the `kotlin-maven-plugin`, you can use the `elide-kotlin-maven-plugin` as a 
drop-in replacement. It supports all configuration you would expect from the Kotlin Maven plugin.

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
    </plugins>
</build>
```

> [!TIP]
> See the [Kotlin sample project](sample-kotlin) for a usage example.

### Java Compiler

If you already have a complex project and just want the Elide Java compiler, you can set the `maven-compiler-plugin`'s
`compilerId` to `elide` to use Elide just as a Java compiler without using any plugins.

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