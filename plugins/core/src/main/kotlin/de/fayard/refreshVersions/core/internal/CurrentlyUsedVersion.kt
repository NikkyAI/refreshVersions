package de.fayard.refreshVersions.core.internal

import org.gradle.api.initialization.Settings

@InternalRefreshVersionsApi
fun Settings.currentVersionOfRefreshVersions(): String {
    return buildscript.configurations.flatMap { it.dependencies }.single {
        val useJitpack = "${it.group}".startsWith("com.github.")
            && it.name == "refreshVersions" // See https://gradle.com/s/u6p6roznt4kn6
        it.group == "de.fayard.refreshVersions" || useJitpack
    }.version!!
}
