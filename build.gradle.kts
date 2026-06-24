buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.intellij.platform:intellij-platform-gradle-plugin:2.16.0")
    }
}

plugins {
    java
}

apply(plugin = "org.jetbrains.intellij.platform")

group = "com.paprikaapps"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    intellijPlatform {
        webstorm("2024.2")
        bundledPlugin("JavaScript")
        instrumentationTools()
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        version = project.version.toString()

        ideaVersion {
            sinceBuild = "242"
        }

        changeNotes.set(
            """
            <ul>
              <li>Initial release.</li>
              <li>Go to Declaration from i18n keys to JSON locale definitions.</li>
              <li>Support for namespaces, configurable path templates, and simple template strings.</li>
            </ul>
            """.trimIndent()
        )
    }

    publishing {
        token = providers.environmentVariable("JETBRAINS_MARKETPLACE_TOKEN")
    }
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}
