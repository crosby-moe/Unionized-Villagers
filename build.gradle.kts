plugins {
    id("fabric-loom") version "1.15-SNAPSHOT"
}

base {
    version = properties["mod_version"] as String
    group = properties["maven_group"] as String
}

repositories {
    maven {
        name = "Nucleoid"
        url = uri("https://maven.nucleoid.xyz")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"] as String}")
    mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"] as String}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.properties["loader_version"] as String}")

    modImplementation(include(fabricApi.module("fabric-api-base", project.properties["fabric_version"] as String))!!)
    modImplementation(include(fabricApi.module("fabric-game-rule-api-v1", project.properties["fabric_version"] as String))!!)
    modImplementation(include(fabricApi.module("fabric-entity-events-v1", project.properties["fabric_version"] as String))!!)
    modImplementation(include(fabricApi.module("fabric-events-interaction-v0", project.properties["fabric_version"] as String))!!)
    modImplementation(include(fabricApi.module("fabric-resource-loader-v0", project.properties["fabric_version"] as String))!!)
    modImplementation(include(fabricApi.module("fabric-convention-tags-v2", project.properties["fabric_version"] as String))!!)
    modImplementation(include(fabricApi.module("fabric-command-api-v2", project.properties["fabric_version"] as String))!!)
    modImplementation(include(fabricApi.module("fabric-registry-sync-v0", project.properties["fabric_version"] as String))!!)
    modImplementation(include("xyz.nucleoid:server-translations-api:2.5.2+1.21.9-pre3") {
        exclude("net.fabricmc.fabric-api", "fabric-api")
    })
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

        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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
