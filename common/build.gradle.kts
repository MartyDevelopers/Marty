architectury {
    common((rootProject.property("enabled_platforms") as String).split(","))
}

dependencies {
    implementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")

    implementation("dev.architectury:architectury:${rootProject.property("architectury_api_version")}")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
