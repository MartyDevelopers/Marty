plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
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
    named("developmentNeoForge") {
        extendsFrom(configurations["common"])
    }

    create("shadowBundle") {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}


repositories {
    maven("https://maven.neoforged.net/releases") {
        name = "NeoForged"
    }
}

dependencies {
    neoForge("net.neoforged:neoforge:${rootProject.property("neoforge_version")}")

    implementation("dev.architectury:architectury-neoforge:${rootProject.property("architectury_api_version")}")

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
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mapOf("version" to version))
            expand(mapOf("description" to rootProject.property("description") as String))
        }
    }

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