## Elide Maven Kotlin Plugin: Mixed sources sample project

This project demonstrates use of Elide as a Java and Kotlin compiler within a Maven project. This is a drop-in
replacement for the Kotlin plugin, so configure your Kotlin project like normal, but use
`dev.elide:elide-kotlin-maven-plugin` instead of `org.jetbrains.kotlin:kotlin-maven-plugin`, add 
`elide-plexus-compilers` as a dependency to the `maven-compiler-plugin` and configure `compilerId` to `elide`.

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
