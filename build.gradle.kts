plugins { java }

subprojects {

    apply(plugin = "java-library")

    repositories {
        mavenLocal()
        maven("https://jitpack.io")
        maven("https://repo.citizensnpcs.co")
        maven("https://repo.aikar.co/content/groups/aikar")
        maven("https://repo.destroystokyo.com/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    group = "me.mrCookieSlime"
    version = "2.3.5"
    description = "quicksell"

}