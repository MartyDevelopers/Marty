plugins {
    id("dev.architectury.loom-no-remap").version("1.17-SNAPSHOT").apply(false)
    id("architectury-plugin").version("3.5-SNAPSHOT")
    id("com.gradleup.shadow").version("9.4.3").apply(false)
}

fun propertyValue(property: String): String {
    return property(property) as String
}

architectury {
    minecraft = propertyValue("minecraft_version")
}

allprojects {
    group = propertyValue("maven_group")
    version = propertyValue("mod_version")
}

subprojects {
    apply(plugin = "dev.architectury.loom-no-remap")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    extensions.configure<BasePluginExtension> {
        archivesName.set("${rootProject.name}-${project.name}")
    }

    repositories {
    }

    dependencies {
        "minecraft"("net.minecraft:minecraft:${rootProject.property("minecraft_version")}")
    }

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release = 25
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                artifactId = project.extensions.getByType<BasePluginExtension>().archivesName.get()
                from(project.components["java"])
            }
        }

        repositories {

        }
    }
}