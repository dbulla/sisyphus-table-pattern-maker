/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.1.1/userguide/building_java_projects.html
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    //    id("org.jetbrains.kotlin.jvm") version "1.4.31"
    id("org.jetbrains.kotlin.jvm") version "1.5.30-M1"

    // Apply the application plugin to add support for building a CLI application in Java.
    application

    id("com.github.ben-manes.versions") version "0.39.0"
    id("com.dorongold.task-tree") version "2.1.0"

}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("commons-io:commons-io:2.11.0")

    testImplementation("io.kotest:kotest-runner-junit5:4.6.1")
}

application {
    // Define the main class for the application.
    mainClass.set("com.nurflugel.sisyphus.sunbursts.ClockworkWigglerGenerator")
    group = "com.nurflugel"
    version = "0.0.1-SNAPSHOT"
}

tasks.withType<Test> {
    useJUnitPlatform()
}