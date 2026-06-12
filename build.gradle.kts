plugins {
    kotlin("jvm") version "2.3.20"
    id("com.typewritermc.module-plugin") version "2.1.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://maven.typewritermc.com/external")
    mavenLocal()
}

group = "btc.renaud"
version = "0.0.1"

typewriter {
    namespace = "btcrenaud"
    extension {
        name = "InputTrigger"
        shortDescription = "Track and trigger actions via player inputs, hotbar slots and inventory buttons."
        description = "A comprehensive TypeWriter extension for intercepting Minecraft key inputs (F, Q, Shift, Ctrl, T, /) and creating clickable inventory buttons with click-type actions, criteria, cooldowns, and crafting grid safety."
        engineVersion = "0.9.0-beta-173"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA
        paper()
    }
}

kotlin {
    jvmToolchain(25)
}
