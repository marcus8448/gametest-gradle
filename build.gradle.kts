plugins {
    java
    groovy
    `java-gradle-plugin`
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

group = "dev.galacticraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
        content {
            includeGroup("net.fabricmc")
            includeGroup("net.fabricmc.fabric-api")
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("net.fabricmc:fabric-loom:1.0.11")
}

tasks.processResources {
    from(project(":injected-mod").tasks.getByName("remapJar").outputs)
}

gradlePlugin {
    plugins {
        create("gametest") {
            id = "dev.galacticraft.gametest"
            implementationClass = "dev.galacticraft.gametest.GameTestPlugin"
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
