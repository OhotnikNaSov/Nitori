plugins {
  `java-library`

  id("com.github.johnrengelman.shadow")
  id("io.papermc.paperweight.userdev")
  id("com.diffplug.spotless")
  id("maven-publish")
}

// Expose version catalog
val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

java {
  javaTarget(17)
  withSourcesJar()
}

repositories {
  mavenCentral()
  maven("https://oss.sonatype.org/content/groups/public/")
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://repo.spongepowered.org/maven/")
}

dependencies {
  compileOnlyApi(libs.jetbrains.annotations)
}

tasks {
  jar {
    archiveClassifier.set("dev")
  }

  reobfJar {
    remapperArgs.add("--mixin")
  }

  build {
    dependsOn(reobfJar)
  }
}

spotless {
  format("misc") {
    target(project.files("*.gradle.kts", "gradle.properties", "settings.gradle.kts", "gradle/libs.versions.toml"))

    trimTrailingWhitespace()
    indentWithSpaces(4)
    endWithNewline()
  }

  java {
    licenseHeaderFile("LICENSE_header.txt")
  }
}

var jarFile = file("build/libs/%s-%s.jar".format(project.name, project.version))
var jarArtifact = artifacts.add("default", jarFile) {
  type = "jar"
  builtBy("jar")
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifact(jarArtifact)
      group = "plugins"
    }
  }

  repositories {
    maven {
      name = "gensorepo"
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
      }
      // url to the releases maven repository
      url = uri("https://repo.gensokyoreimagined.net/")
    }
  }
}

tasks.named("publishMavenJavaPublicationToGensorepoRepository") {
  dependsOn("reobfJar")
}