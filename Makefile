all: clean all-java all-kotlin all-plugin

clean:
	./mvnw clean

all-java: build-java test-java install-java sample-java

all-kotlin: build-kotlin test-kotlin install-kotlin sample-kotlin

all-plugin: build-plugin test-plugin install-plugin sample-plugin

build-java:
	./mvnw compile -pl java-compiler

build-kotlin:
	./mvnw compile -pl kotlin-plugin

build-plugin:
	./mvnw compile -pl elide-plugin

test-java:
	./mvnw test -pl java-compiler

test-kotlin:
	./mvnw test -pl kotlin-plugin

test-plugin:
	./mvnw test -pl elide-plugin

install-java:
	./mvnw install -pl java-compiler

install-kotlin:
	./mvnw install -pl kotlin-plugin

install-plugin:
	./mvnw install -pl elide-plugin

sample-java:
	./mvnw clean package exec:java -f sample-java

sample-kotlin:
	./mvnw clean package exec:java -f sample-kotlin

sample-plugin:
	./mvnw clean package exec:java -f sample-mixed