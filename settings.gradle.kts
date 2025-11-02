pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TravelFoodie"
include(":app")
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":core:sync")
include(":core:sensors")
include(":feature:trip")
include(":feature:attraction")
include(":feature:restaurant")
include(":feature:voice")
include(":feature:widget")
include(":feature:board")
