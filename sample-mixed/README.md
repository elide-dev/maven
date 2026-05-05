## Elide Maven Kotlin Plugin: Mixed sources sample project

This project demonstrates use of Elide as a Java and Kotlin compiler within a Maven project. To configure it, simply add
the `dev.elide:elide-maven-plugin` and set `extensions` to `true`.

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
