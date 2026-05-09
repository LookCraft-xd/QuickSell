plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    compileOnly(libs.com.github.thebusybiscuit.cs.corelib2)
    compileOnly(libs.co.aikar.acf.paper)
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.com.github.milkbowl.vaultapi)
    compileOnly(libs.net.citizensnpcs.citizens.main)

    compileOnly(files("../libs/PrisonUtils_v1.7.jar"))
}

group = "me.mrCookieSlime"
version = "2.3.5"
description = "quicksell"

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks {

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    compileJava {
        options.encoding = "UTF-8"
    }
}