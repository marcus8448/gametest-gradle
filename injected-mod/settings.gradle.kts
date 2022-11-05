pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
            content {
                includeGroup("fabric-loom")
                includeGroup("net.fabricmc")
                includeGroup("net.fabricmc.fabric-api")
            }
        }
        gradlePluginPortal()
    }
}

rootProject.name = "injected-mod"
