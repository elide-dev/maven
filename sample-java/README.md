## Elide Maven Java Compiler: Sample project

This project demonstrates use of Elide as a replacement for `javac` within a Maven project. Explicitly add the
`maven-compiler-plugin`, add `elide-plexus-compilers` as a dependency, and configure `compilerId` to `elide`.

**`pom.xml`**
```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.14.0</version>
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
