// ============================================================
// SPRING BOOT REST API - BUILD CONFIGURATION
// Kotlin + Spring Boot 4.x + JPA + Security
// ============================================================

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.asciidoctor.jvm.convert") version "4.0.5"
}

// ------------------------------------------------------------
// PROJECT METADATA
// ------------------------------------------------------------
group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Spring Boot REST API with Clean Architecture"

// ------------------------------------------------------------
// JAVA TOOLCHAIN
// ------------------------------------------------------------
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// ------------------------------------------------------------
// REPOSITORIES
// ------------------------------------------------------------
repositories {
    mavenCentral()
}

// ------------------------------------------------------------
// CONFIGURATIONS
// ------------------------------------------------------------
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

// ------------------------------------------------------------
// DEPENDENCY VERSIONS
// ------------------------------------------------------------
val jjwtVersion = "0.12.3"
val modelMapperVersion = "3.2.1"

// ------------------------------------------------------------
// DEPENDENCIES
// ------------------------------------------------------------
dependencies {
    // === Spring Boot Starters ===
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // === Thymeleaf Security Integration ===
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // === Kotlin Support ===
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // === Database ===
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // === JWT Authentication ===
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // === Apache Kafka ===
    implementation("org.springframework.kafka:spring-kafka")

    // === Stripe Payment ===
    implementation("com.stripe:stripe-java:24.0.0")

    // === Utilities ===
    implementation("org.modelmapper:modelmapper:$modelMapperVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // === Testing ===
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-restdocs")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Swagger/OpenAPI Documentation - Use 2.8.4 for Spring Boot 4.x compatibility
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
}

// ------------------------------------------------------------
// KOTLIN COMPILER OPTIONS
// ------------------------------------------------------------
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property"
        )
    }
}

// ------------------------------------------------------------
// JPA ENTITY CONFIGURATION
// Makes JPA entities open for proxying (required by Hibernate)
// ------------------------------------------------------------
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

// ------------------------------------------------------------
// TEST CONFIGURATION
// ------------------------------------------------------------
extra["snippetsDir"] = file("build/generated-snippets")

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    outputs.dir(project.extra["snippetsDir"]!!)
}

// ------------------------------------------------------------
// DOCUMENTATION (AsciiDoctor)
// ------------------------------------------------------------
tasks.asciidoctor {
    inputs.dir(project.extra["snippetsDir"]!!)
    dependsOn(tasks.test)
}
