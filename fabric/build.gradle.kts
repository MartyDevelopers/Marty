plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

configurations {
    create("common") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    named("compileClasspath") {
        extendsFrom(configurations["common"])
    }
    named("runtimeClasspath") {
        extendsFrom(configurations["common"])
    }
    named("developmentFabric") {
        extendsFrom(configurations["common"])
    }

    create("shadowBundle") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

dependencies {
    implementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")

    implementation("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_api_version")}")

    implementation("dev.architectury:architectury-fabric:${rootProject.property("architectury_api_version")}")

    add("common", project(":common")) {
        isTransitive = false
    }

    add(
            "shadowBundle",
            project(
                    path = ":common",
                    configuration = "transformProductionFabric"
            )
    )
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to version))
        }
    }

    // Minecraft is not obfuscated on this version, so there is no remapJar task.
    // The shaded jar is the mod jar that ships.
    jar {
        archiveClassifier = "dev"
    }

    shadowJar {
        configurations = listOf(project.configurations["shadowBundle"])
        archiveClassifier = null
    }

    assemble {
        dependsOn(shadowJar)
    }
}

configurations {
    named("apiElements") {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.shadowJar)
    }

    named("runtimeElements") {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.shadowJar)
    }
}