import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    java
    id("org.openjfx.javafxplugin") version "0.0.13"

    id("com.github.ben-manes.versions") version "0.44.0"
    idea
}

group = "de.groovybyte.chunky"
version = "1.0-ALPHA"

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://repo.lemaik.de/")
}

dependencies {
    implementation("se.llbit:chunky-core:2.5.0-SNAPSHOT") {
        isChanging = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

javafx {
    version = "11.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks {
    processResources {
        filesMatching("plugin.json") {
            expand(
                "version" to project.version,
                "chunkyVersion" to "2.5.0",
            )
        }
    }

    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

//    withType<Jar> {
//        archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
//        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//        configurations["compileClasspath"].apply {
//            files { dep ->
//                when {
//                    dep.name.startsWith("chunky") -> false
//                    else -> true
//                }
//            }.forEach { file ->
//                from(zipTree(file.absoluteFile))
//            }
//        }
//    }

    withType<DependencyUpdatesTask> {
        val unstable = Regex("^.*?(?:alpha|beta|unstable|rc|ea).*\$", RegexOption.IGNORE_CASE)
        rejectVersionIf {
            candidate.version.matches(unstable)
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
