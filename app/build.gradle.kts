plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.dagger.hilt.android")
    kotlin("kapt")
    id("com.google.gms.google-services")

    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
}

android {
    namespace = "com.kgjr.safecircle"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kgjr.safecircle"
        minSdk = 26
        targetSdk = 35
        versionCode = 17
        versionName = "1.1.6"

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
    buildFeatures {
        compose = true
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    //dagger -  hilt dependency
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation ("androidx.navigation:navigation-compose:2.6.0")

    //Firebase dependency
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation ("com.google.firebase:firebase-database")

    // Google Play Services Location (Fused Location Provider)
    implementation("com.google.android.gms:play-services-location:21.2.0")

    //gson dependency
    implementation ("com.google.code.gson:gson:2.13.1")

    //Google Map dependency
    implementation("com.google.maps.android:maps-compose:6.4.1")
    implementation ("com.google.maps.android:maps-utils-ktx:3.4.0")

    //Async Image Loading dependency
//    implementation("io.coil-kt:coil-compose:2.0.0-rc01")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-gif:2.6.0")

    //lotti animation dependency
    implementation("com.airbnb.android:lottie-compose:6.4.1")


    //gson and okHttp3 dependency
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    //workManager Dependency
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    //Material Dependency
    implementation ("androidx.compose.material:material:1.6.7")


    //Graph Dependency
//    implementation ("co.yml:ycharts:2.1.0")

    //Firebase Cloud Messaging
    implementation ("com.google.firebase:firebase-messaging:24.0.1")

    //In app update dependency
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    //google ad dependency
    implementation("com.google.android.gms:play-services-ads:24.5.0")
}