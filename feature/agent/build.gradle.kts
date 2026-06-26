plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.merged.feature.agent"
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
