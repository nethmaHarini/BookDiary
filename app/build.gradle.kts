plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "me.nethma.bookdiary"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.nethma.bookdiary"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    // Google Sign-In via CredentialManager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.auth)
    implementation(libs.google.identity.googleid)
    implementation(libs.gms.play.services.auth)
    // Firebase BoM — manages all Firebase library versions
    implementation(platform(libs.firebase.bom))
    // Firebase Authentication (required for Google Sign-In token verification)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.work.runtime)
    // Retrofit + Gson for Open Library API
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    // Glide for cover images
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    // Splash Screen
    implementation(libs.core.splashscreen)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}