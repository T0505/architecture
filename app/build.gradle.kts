plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.merged.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.merged.runtime"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ipc"))
    implementation(project(":core:service"))
    implementation(project(":core:storage"))
    implementation(project(":core:scheduling"))
    implementation(project(":feature:launcher"))
    implementation(project(":feature:agent"))
    implementation(project(":feature:script"))
    implementation(project(":feature:automation"))
    implementation(project(":feature:vision"))
    implementation(project(":feature:plugin"))
    implementation(project(":feature:terminal"))
    implementation(project(":feature:input"))
    implementation(project(":native-bridge"))
}

