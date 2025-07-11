## Elide Maven Kotlin Plugin: Sample Project

This project demonstrates use of Elide as a Kotlin compiler within a Maven project. This is a drop-in replacement to the
Kotlin plugin, so configure your Kotlin project like normal, but use `dev.elide:elide-kotlin-maven-plugin` instead of
`org.jetbrains.kotlin:kotlin-maven-plugin`. Use of `<extensions>` is not supported yet.

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
