plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.merged.feature.launcher"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:service"))
    implementation(project(":core:storage"))
}
