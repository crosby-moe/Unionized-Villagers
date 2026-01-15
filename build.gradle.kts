plugins {
    id("fabric-loom") version "1.14-SNAPSHOT"
}

base {
    version = properties["mod_version"] as String
    group = properties["maven_group"] as String
}

repositories {

}

dependencies {
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"] as String}")
    mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"] as String}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.properties["loader_version"] as String}")

    modImplementation(include(fabricApi.module("fabric-game-rule-api-v1", project.properties["fabric_version"] as String))!!)
    modImplementation(include(fabricApi.module("fabric-resource-loader-v0", project.properties["fabric_version"] as String))!!)
}

tasks {
    processResources {
        val properties = mapOf("version" to project.version)

        inputs.properties(properties)

        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    java {
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    jar {
        inputs.property("archivesName", project.base.archivesName.get())

        from("LICENSE") {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
