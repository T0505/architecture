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

rootProject.name = "MergedRuntime"

include(":app")
include(":core:model")
include(":core:ipc")
include(":core:service")
include(":core:storage")
include(":core:scheduling")
include(":feature:launcher")
include(":feature:agent")
include(":feature:script")
include(":feature:automation")
include(":feature:vision")
include(":feature:plugin")
include(":feature:terminal")
include(":feature:input")
include(":native-bridge")

