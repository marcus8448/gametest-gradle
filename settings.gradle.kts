pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
            content {
                includeGroup("net.fabricmc")
                includeGroup("fabric-loom")
            }
        }
        gradlePluginPortal()
    }
}

include("injected-mod")
rootProject.name = "gametest-gradle"
