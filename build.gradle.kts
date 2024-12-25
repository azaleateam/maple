import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.google.devtools.ksp") version "2.0.10-1.0.24"
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
    implementation("com.github.honkling:commonlib:616d753")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.20.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.20.0")

    /* database */
    implementation("gg.ingot:iron:2.0.0-RC1")
    ksp("gg.ingot.iron:processor:2.0.0-RC1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    /* used for translations w/ strings */
    compileOnly("com.moandjiezana.toml:toml4j:0.7.2")
    /* used for config due to object mapping */
    implementation("cc.ekblad:4koma:1.2.0")

    implementation(kotlin("reflect"))
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