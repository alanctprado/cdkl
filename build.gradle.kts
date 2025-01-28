plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "com.alanprado"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:5.0.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.alanprado.MainKt")
}
