plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.merged.core.ipc"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
    }
}

dependencies {
    implementation(project(":core:model"))
}

