pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.google.protobuf" ->
                    useVersion("0.9.4")   // o la versión que necesites
            }
            if (requested.id.id == "com.android.application") {
                useModule("com.android.tools.build:gradle:8.3.1")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
    }
}

rootProject.name = "frontend-movil"
include(":app")
 