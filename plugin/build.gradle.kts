import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.0.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

dependencies {
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.com.github.milkbowl.vaultapi)

    implementation(libs.dev.dejvokep.boostedyaml)
    implementation(libs.dev.triumphteam.triumphgui)
    implementation(libs.dev.rollczi.litecommands.bukkit)
    implementation(libs.com.github.thebusybiscuit.cs.corelib2)

    // Dependencias transitivas weonas.
    configurations.all {
        exclude(group = "com.google.code.gson", module = "gson")
        exclude(group = "org.jetbrains", module = "annotations")
        exclude(group = "org.intellij.lang", module = "annotations")
    }
}

group = "me.mrCookieSlime"
version = "2.3.5"
description = "quicksell"


tasks {

    build { dependsOn(shadowJar) }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    runServer {
        minecraftVersion("1.21.11")

        downloadPlugins {
            //hangar("PlaceholderAPI", "2.12.2")
            //modrinth("worldguard", "7.0.15")
            //modrinth("worldedit", "JUWRHdru")
        }
    }

    shadowJar {
        relocate ("net.kyori", "me.mrCookieSlime.QuickSell.libs.kyori")
        relocate ("dev.triumphteam", "me.mrCookieSlime.QuickSell.libs.gui")
        relocate ("dev.rollczi", "me.mrCookieSlime.QuickSell.libs.commands")
        relocate ("dev.dejvokep", "me.mrCookieSlime.QuickSell.libs.boostedyaml")

        exclude("org/jetbrains/annotations/**")
        exclude("org/intellij/lang/annotations/**")
        exclude("META-INF/maven/**")
        exclude("META-INF/versions/**")

        archiveClassifier.set("")
    }

    bukkit {
        name = "QuickSell"
        description = "Plugin that allow the player to quickly sell its inventory items."
        prefix = name
        version = project.version.toString()
        main = project.group.toString() + ".QuickSell.QuickSell"
        //me.mrCookieSlime.QuickSell.QuickSell
        apiVersion = "1.21"
        authors = listOf("mrCookieSlime", "Shay Punter")
        contributors = listOf("Sliide_")
        defaultPermission = BukkitPluginDescription.Permission.Default.FALSE
        depend = listOf(
            "Vault"
        )
        softDepend = listOf(
            "mcMMO",
            "PrisonGems",
            "PlaceholderAPI"
        )

        permissions {
            // Admin permission
            register("QuickSell.sign.create") {
                default = BukkitPluginDescription.Permission.Default.OP
                description = "Allows you to create SELL Signs"
            }
            register("QuickSell.booster") {
                default = BukkitPluginDescription.Permission.Default.OP
                description = "Allows you to create a Booster"
            }
            register("QuickSell.manage") {
                default = BukkitPluginDescription.Permission.Default.OP
                description = "Allows you to manage Shops"
            }
            register("QuickSell.prices") {
                default = BukkitPluginDescription.Permission.Default.OP
                description = "Allows you to do /prices"
            }
        }
    }
}