import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    id("com.gradleup.shadow") version "8.3.3"
    id("io.freefair.lombok") version "8.12.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "com.mcdragonmasters"
version = "1.0.0"
repositories {
    mavenCentral()
    mavenLocal()
    // Paper
    maven("https://repo.papermc.io/repository/maven-public/")
    // ConfigUpdater
    maven("https://oss.sonatype.org/content/groups/public")
    // PotatoEssentials
    maven("https://jitpack.io")
}
dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    // CommandAPI
    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:10.0.1")
    // Vault
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
}
tasks {
    shadowJar {
        archiveClassifier.set(null as String?)
        relocate("dev.jorel.commandapi", "com.mcdragonmasters.guiManager.libs.commandapi")
    }
    build {
        dependsOn(shadowJar)
    }
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.4")
    }
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-source", "21", "-target", "21"))
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}
tasks.processResources {
    filter<ReplaceTokens>("tokens" to mapOf(
        "version" to project.version.toString()))
}