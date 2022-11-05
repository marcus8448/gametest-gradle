plugins {
    java
    id("fabric-loom") version("1.0-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

base {
    archivesName.set("gametest-gradle")
}

group = "dev.galacticraft"
version = "0.0.0"

dependencies {
    minecraft("com.mojang:minecraft:1.19.2")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.14.10")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.64.0+1.19.2")
}
