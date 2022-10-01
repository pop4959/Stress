plugins {
    id("java-library")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = project.property("pluginGroup")
version = project.property("pluginVersion")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withSourcesJar()
}

repositories {
    jcenter()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    testCompileOnly(group = "junit", name = "junit", version = "4.12")
    compileOnly(group = "com.destroystokyo.paper", name = "paper-api", version = "1.15.2-R0.1-SNAPSHOT")
    compileOnly(group = "net.luckperms", name = "api", version = "5.0")
    implementation(group = "io.papermc", name = "paperlib", version = "1.0.2")
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
    }
    build {
        dependsOn(shadowJar)
    }
}
