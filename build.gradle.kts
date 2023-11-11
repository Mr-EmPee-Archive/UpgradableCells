import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
  id("org.gradle.java-library")

  id("io.freefair.lombok") version "6.6.3"
  id("com.github.johnrengelman.shadow") version "8.1.0"

  id("xyz.jpenilla.run-paper") version "2.0.1"
  id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
}

group = "ml.empee"

if (project.hasProperty("tag")) {
  version = project.property("tag")!!
} else {
  version = "develop"
}

var basePackage = "ml.empee.upgradableCells"

bukkit {
  load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
  main = "${basePackage}.UpgradableCells"
  apiVersion = "1.13"
  depend = listOf("Vault", "WorldEdit")
  softDepend = listOf("Multiverse-Core")
  authors = listOf("Mr. EmPee")
}

repositories {
  maven("https://jitpack.io")
  maven("https://maven.enginehub.org/repo/")
  maven("https://repo.codemc.io/repository/nms/")
  mavenCentral()
  mavenLocal()
}

dependencies {
  compileOnly("com.destroystokyo.paper:paper:1.13.2-R0.1-SNAPSHOT:sources")

  compileOnly("org.jetbrains:annotations:24.0.1")
  compileOnly("org.xerial:sqlite-jdbc:3.34.0")

  compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9")
  compileOnly("com.github.MilkBowl:VaultAPI:1.7")

  implementation("com.github.Mr-EmPee:LightWire:1.1.0")
  implementation("com.github.Mr-EmPee:SimpleMenu:0.2.3")
  implementation("com.github.Mr-EmPee:ItemBuilder:1.1.3")

  implementation("com.github.cryptomorin:XSeries:9.4.0") { isTransitive = false }

  // Commands
  implementation("me.lucko:commodore:2.2") { exclude("com.mojang", "brigadier") }

  implementation("cloud.commandframework:cloud-paper:1.8.3")
  implementation("cloud.commandframework:cloud-annotations:1.8.3")
}

tasks {

  shadowJar {
    archiveFileName.set("${project.name}.jar")
    isEnableRelocation = project.version != "develop"
    relocationPrefix = "$basePackage.relocations"
  }

  javadoc { options.encoding = Charsets.UTF_8.name() }

  processResources { filteringCharset = Charsets.UTF_8.name() }

  compileJava {
    sourceCompatibility = "11"
    targetCompatibility = "11"

    options.encoding = Charsets.UTF_8.name()
    options.compilerArgs.add("-parameters")
  }

  runServer {
    version.set("1.13.2")
  }
}


java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}