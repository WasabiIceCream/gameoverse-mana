plugins {
    id("fabric-loom") version "1.17.20"
    `java-library`
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    // Compile-only against the installed jars (reference-jars/, not committed); provided at runtime.
    compileOnly(files("reference-jars/mana-attributes-4.0.0.jar"))
    compileOnly(files("reference-jars/spell_engine-fabric-1.10.9+26.1.2.jar"))
    compileOnly(files("reference-jars/spell_power-fabric-1.6.2+26.1.2.jar"))
    compileOnly(files("reference-jars/dynamic_resource_bars-fabric-0.9.6+26.1.2.jar"))
    compileOnly(files("reference-jars/fzzy_config-0.7.6+26.1.jar"))
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}
