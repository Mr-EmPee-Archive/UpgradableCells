import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
  id("org.gradle.java-library")
  id("org.gradle.checkstyle")

  id("io.freefair.lombok") version "6.6.3"
  id("com.github.johnrengelman.shadow") version "8.1.0"

  id("io.papermc.paperweight.userdev") version "1.5.2"
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
  maven("https://gitlab.com/api/v4/projects/49162563/packages/maven")

  mavenCentral()
}

dependencies {
  paperweight.paperDevBundle("1.19.3-R0.1-SNAPSHOT")
  compileOnly("org.xerial:sqlite-jdbc:3.34.0")

  compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9")
  compileOnly("com.github.MilkBowl:VaultAPI:1.7")

  implementation("mr.empee:LightWire:1.2")
  implementation("com.github.Mr-EmPee:SimpleMenu:0.0.5")
  implementation("com.github.Mr-EmPee:ItemBuilder:1.1.1")

  implementation("com.github.cryptomorin:XSeries:9.4.0") { isTransitive = false }

  //Commands
  implementation("me.lucko:commodore:2.2") {
    exclude("com.mojang", "brigadier")
  }

  implementation("cloud.commandframework:cloud-paper:1.8.3")
  implementation("cloud.commandframework:cloud-annotations:1.8.3")
}

tasks {
  checkstyle {
    toolVersion = "10.10.0"
    configFile = file("$projectDir/checkstyle.xml")
  }

  shadowJar {
    archiveFileName.set("${project.name}.jar")
    isEnableRelocation = project.version != "develop"
    relocationPrefix = "$basePackage.relocations"
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }

  processResources {
    filteringCharset = Charsets.UTF_8.name()
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.compilerArgs.add("-parameters")
  }
}

java {
  toolchain.languageVersion.set(
    JavaLanguageVersion.of(17)
  )
}
