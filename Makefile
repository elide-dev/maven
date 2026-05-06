all: clean install samples

clean:
	./mvnw clean

install: java kotlin plugin

java:
	./mvnw install -pl java-compiler

kotlin:
	./mvnw install -pl kotlin-plugin

plugin:
	./mvnw install -pl elide-plugin

samples: sample-java sample-kotlin sample-plugin

sample-java:
	./mvnw clean package exec:java -f sample-java

sample-kotlin:
	./mvnw clean package exec:java -f sample-kotlin

sample-plugin:
	./mvnw clean package exec:java -f sample-mixed