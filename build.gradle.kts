
plugins {
    id("java")
}

group = "mythic.prison"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/") // Add PaperMC repository
    maven("https://maven.enginehub.org/repo/") // Add EngineHub repository for WorldEdit
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Adventure components (needed for text formatting)
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")

    // SLF4J implementation to fix the logging warnings
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // Minestom
    implementation("com.github.Minestom:Minestom:master-SNAPSHOT")

    // MongoDB
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation("org.mongodb:bson:4.11.1")

    // Redis (Lettuce)
    implementation("io.lettuce:lettuce-core:6.3.0.RELEASE")

    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")

    // Async/Threading
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // WorldEdit dependencies
    implementation("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.8.4")
    implementation("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.8.4") // If using Bukkit API

    // Alternative: If you want to use pure WorldEdit instead of FastAsyncWorldEdit
    // implementation("com.sk89q.worldedit:worldedit-core:7.2.15")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}