import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    java
    id("org.openjfx.javafxplugin") version "0.0.13"

    id("com.github.ben-manes.versions") version "0.44.0"
    idea
}

group = "de.groovybyte.chunky"
version = "1.0"
// https://repo.lemaik.de/se/llbit/chunky-core/maven-metadata.xml
val chunkyVersion = "2.4.4"

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://repo.lemaik.de/")
}

dependencies {
    implementation("se.llbit:chunky-core:$chunkyVersion") {
        isChanging = true
    }
}

javafx {
    version = "17.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks {
    processResources {
        filesMatching("plugin.json") {
            expand(
                "version" to project.version,
                "chunkyVersion" to chunkyVersion,
            )
        }
    }

    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
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
