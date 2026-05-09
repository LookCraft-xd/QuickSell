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

    compileOnly(files("../libs/PrisonUtils_v1.7.jar"))

    implementation(libs.dev.rollczi.litecommands.bukkit)
    implementation(libs.com.github.thebusybiscuit.cs.corelib2)
}

group = "me.mrCookieSlime"
version = "2.3.5"
description = "quicksell"

tasks {

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
            //modrinth("worldguard", "7.0.15")
            //modrinth("worldedit", "JUWRHdru")
        }
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