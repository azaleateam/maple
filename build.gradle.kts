import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "team.azalea"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    implementation("com.github.honkling.commando:spigot:b0ff9a152d")
    implementation("com.github.honkling:Pocket:ea9e90511b")
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
}

tasks.withType<ShadowJar> {
    dependencies {
        exclude { it.moduleGroup == "org.jetbrains.kotlin" }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveFileName.set("Maple.jar")
}