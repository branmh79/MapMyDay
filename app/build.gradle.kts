plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Apply Google Services plugin
}

android {
    namespace = "com.SCGIII.mapmyday"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.SCGIII.mapmyday"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.5.1")) // Firebase BOM
    implementation("com.google.firebase:firebase-database-ktx") // Firebase Realtime Database with Kotlin Extensions
    implementation("com.google.firebase:firebase-analytics") // Firebase Analytics

    // AndroidX Annotations
    implementation ("androidx.annotation:annotation:1.1.0")

}
