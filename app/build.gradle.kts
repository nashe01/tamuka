// Swift Ride App Build Configuration
// This file defines the build settings, dependencies, and configuration for the Swift Ride Android application

plugins {
    alias(libs.plugins.android.application)
    // Google services plugin for Firebase integration
    id("com.google.gms.google-services")
}

android {
    namespace = "com.kodelink.glide"
    compileSdk = 36

    // Application configuration
    defaultConfig {
        applicationId = "com.kodelink.glide"
        minSdk = 24        // Minimum Android API level (Android 7.0)
        targetSdk = 35     // Target Android API level (Android 14)
        versionCode = 1    // Internal version number
        versionName = "1.0" // User-visible version name

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Project dependencies
dependencies {
    // AndroidX core libraries
    implementation(libs.appcompat)           // AppCompat for backward compatibility
    implementation(libs.material)            // Material Design components
    implementation(libs.activity)            // Activity library
    implementation(libs.constraintlayout)    // ConstraintLayout for flexible layouts
    
    // Testing dependencies
    testImplementation(libs.junit)           // Unit testing framework
    androidTestImplementation(libs.ext.junit) // Android JUnit extensions
    androidTestImplementation(libs.espresso.core) // UI testing framework
    
    // Firebase dependencies
    implementation(platform(libs.firebase.bom)) // Firebase Bill of Materials for version management
    implementation(libs.firebase.analytics)     // Firebase Analytics
    implementation(libs.firebase.auth)          // Firebase Authentication
    implementation(libs.firebase.database)      // Firebase Realtime Database
    implementation(libs.firebase.firestore)     // Firebase Firestore
    
    // Google Play Services for Maps and Location
    implementation("com.google.android.gms:play-services-maps:18.1.0")        // Google Maps SDK
    implementation("com.google.android.gms:play-services-location:21.0.1")    // Location services
    implementation("com.google.android.libraries.places:places:3.4.0")        // Places API for location search
}