import java.util.*

plugins {
    id("java-library")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    compileOnly(group = "io.papermc.paper", name = "paper-api", version = "1.20.4-R0.1-SNAPSHOT")
    compileOnly(group = "net.luckperms", name = "api", version = "5.4")
    implementation(group = "io.papermc", name = "paperlib", version = "1.0.8")
    implementation(group = "org.bstats", name = "bstats-bukkit", version = "3.0.2")
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
        archiveFileName.set("${rootProject.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}-${project.version}.jar")
        relocate("io.papermc.lib", "org.popcraft.stress.paperlib")
        relocate("org.bstats", "org.popcraft.stress.bstats")
    }
    build {
        dependsOn(shadowJar)
    }
}
