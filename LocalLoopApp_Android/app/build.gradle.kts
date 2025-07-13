plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.localloopapp_android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.localloopapp_android"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.libraries.places:places:3.4.0")

    // Firebase BOM for version management
    implementation(platform("com.google.firebase:firebase-bom:32.1.1"))

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-auth:23.2.1")


    // The view calendar library for Android
    implementation("com.kizitonwose.calendar:view:2.4.0")

    // The compose calendar library for Android
    implementation("com.kizitonwose.calendar:compose:2.4.0")


    // Glide dependency. GIF support
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.firebase:firebase-storage:21.0.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}