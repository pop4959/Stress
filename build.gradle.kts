plugins {
    id("java-library")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "${project.property("pluginGroup")}"
version = "${project.property("pluginVersion")}"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    testCompileOnly(group = "junit", name = "junit", version = "4.13.2")
    compileOnly(group = "io.papermc.paper", name = "paper-api", version = "1.19.2-R0.1-SNAPSHOT")
    compileOnly(group = "net.luckperms", name = "api", version = "5.4")
    implementation(group = "io.papermc", name = "paperlib", version = "1.0.8-SNAPSHOT")
    implementation(group = "org.bstats", name = "bstats-bukkit", version = "3.0.0")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    processResources {
        filesMatching("plugin.yml") {
            expand(
                "version" to project.version,
            )
        }
    }
    jar {
        archiveClassifier.set("noshade")
    }
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("${rootProject.name.capitalize()}-${project.version}.jar")
        relocate("io.papermc.lib", "org.popcraft.stress.paperlib")
        relocate("org.bstats", "org.popcraft.stress.bstats")
    }
    build {
        dependsOn(shadowJar)
    }
}
