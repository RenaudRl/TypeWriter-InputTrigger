plugins {
    kotlin("jvm") version "2.2.10"
    id("com.typewritermc.module-plugin") version "2.2.0"
}

repositories {
    mavenCentral()
    maven("https://maven.typewritermc.com/beta/")
    maven("https://maven.typewritermc.com/external")
}

group = "btc.renaud"
version = "0.0.4"

base {
    archivesName.set("InputTriggerExtension")
}

typewriter {
    namespace = "btcrenaud"
    extension {
        name = "InputTrigger"
        shortDescription = "Track and trigger actions via player inputs, hotbar slots and inventory buttons."
        description = "A comprehensive TypeWriter extension for intercepting Minecraft key inputs (F, Q, Shift, Ctrl, T, /) and creating clickable inventory buttons with click-type actions, criteria, cooldowns, and crafting grid safety."
        engineVersion = "0.9.0-beta-176"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA
        paper()
    }
}

    

kotlin {
    jvmToolchain(21)
}

