## Elide Maven Java Compiler: Sample Project

This project demonstrates use of Elide as a replacement for `javac` within a Maven project. There is some Java source
code to build, and the `pom.xml` is configured to use Elide.

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
