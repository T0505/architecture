plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.merged.core.model"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
    }
}