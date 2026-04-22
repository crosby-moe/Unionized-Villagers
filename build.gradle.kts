plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
}

base {
    archivesName = properties["archives_base_name"] as String
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
    implementation("net.fabricmc:fabric-loader:${project.properties["loader_version"] as String}")

    compileOnly(fabricApi.module("fabric-game-rule-api-v1", project.properties["fabric_version"] as String))
    compileOnly(fabricApi.module("fabric-entity-events-v1", project.properties["fabric_version"] as String))
    compileOnly(fabricApi.module("fabric-events-interaction-v0", project.properties["fabric_version"] as String))
    compileOnly(fabricApi.module("fabric-command-api-v2", project.properties["fabric_version"] as String))
    runtimeOnly("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"] as String}")

    implementation(include("xyz.nucleoid:server-translations-api:3.0.3+26.1") {
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

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}
