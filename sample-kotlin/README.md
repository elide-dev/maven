## Elide Maven Kotlin Plugin: Sample project

This project demonstrates use of Elide as a Kotlin compiler within a Maven project. This is a drop-in replacement for
the Kotlin plugin, so configure your Kotlin project like normal, but use `dev.elide:elide-kotlin-maven-plugin` instead
of `org.jetbrains.kotlin:kotlin-maven-plugin`.

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
