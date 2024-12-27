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

    compileOnly("net.luckperms:api:5.4")

    /* database */
    implementation("gg.ingot:iron:f77a1ccb6a")
    ksp("gg.ingot.iron:processor:f77a1ccb6a")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    compileOnly("org.flywaydb:flyway-core:10.16.0")

    /* used for translations w/ strings */
    compileOnly("com.moandjiezana.toml:toml4j:0.7.2")
    /* used for config due to object mapping */
    implementation("cc.ekblad:4koma:1.2.0")

    implementation(kotlin("reflect"))

    compileOnly("net.dv8tion:JDA:5.1.0") {
        exclude("opus-java")
    }
}

tasks.register("createMigration") {
    doLast {

        val timestamp = System.currentTimeMillis() / 1000
        val path = "${timestamp}_.sql"

        val migrationDir = File(project.projectDir.toString(), "src/main/resources/db/migration")
        if (!migrationDir.exists()) {
            migrationDir.mkdirs()
        }

        val migrationFile = File(migrationDir, path)
        migrationFile.writeText(
            """
         |-- Create your migration here
         |
         |CREATE TABLE IF NOT EXISTS example (
         |   id INTEGER PRIMARY KEY,
         |   name TEXT
         |);
         |
         |-- Seeding
         |INSERT INTO example (id, name) VALUES (1, 'Hello');
         """.trimMargin()
        )
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveFileName.set("Maple.jar")
}