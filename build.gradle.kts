plugins {
    // Applies the correct Fabric Loom variant for whichever Minecraft version is active.
    id("dev.kikugie.loom-back-compat")
    id("maven-publish")
}

// DO NOT set group here directly, it is derived from stonecutter.properties.toml below.
version = "${property("mod.version")}+${sc.current.version}"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion =
    if (sc.current.parsed >= "26.1") JavaVersion.VERSION_25 else JavaVersion.VERSION_21

val fabricLoaderVersion = property("deps.fabric_loader") as String
val jgitVersion = property("deps.jgit") as String
val modId = property("mod.id") as String
val fabricApiVersion = sc.properties.get<String>("deps.fabric_api")
val clothConfigVersion = sc.properties.get<String>("deps.cloth_config")
val modMenuVersion = sc.properties.get<String>("deps.mod_menu")
val mcCompat = sc.properties.get<String>("mod.mc_compat")

repositories {
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/") // Mod Menu
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Applies Mojang's official mappings on versions that ship with them,
    // falling back to Yarn where they aren't published (handled by loom-back-compat).
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    testImplementation("net.fabricmc:fabric-loader-junit:$fabricLoaderVersion")

    // Source: https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
    include(implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")!!)

    // Cloth Config
    include(modImplementation("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion")!!)

    // Mod Menu
    modImplementation("com.terraformersmc:modmenu:$modMenuVersion")
}

tasks.test {
    useJUnitPlatform()
}

fabricApi {
    configureTests {
        createSourceSet = true
        modId = "galateahunter-test"
        enableGameTests = false // Default is true
        enableClientGameTests = true // Default is true
        eula = true // By setting this to true, you agree to the Minecraft EULA.
    }
}

loom {
    // Share one `run/` directory between every version instead of duplicating game installs.
    runConfigs.all {
        runDirectory = rootProject.file("run")
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        val props = mapOf(
            "version" to project.version.toString(),
            "loader_version" to fabricLoaderVersion,
            "minecraft_version" to mcCompat
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") { expand(props) }
    }

    jar {
        val archiveBaseName = modId
        from("LICENSE") {
            rename { "${it}_$archiveBaseName" }
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar for the active version and copies it to build/libs/{mc version}/"

        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/${sc.current.version}"))
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = property("mod.id") as String
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}
