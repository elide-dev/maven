import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.2.0"
    `java-library`
    `maven-publish`
}

group = "dev.elide"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(24)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.kotlin.plugin)
    implementation(libs.maven.plugin.api)
    implementation(libs.maven.core)
    implementation(libs.javax.inject)
    implementation(project(":plexus-compilers"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.elide"
            artifactId = "elide-kotlin-maven-plugin"
            version = version

            from(components["kotlin"])
        }
    }
}