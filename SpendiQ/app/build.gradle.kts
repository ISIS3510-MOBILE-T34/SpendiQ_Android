plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
}

android {
    namespace = "com.isis3510.spendiq"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.isis3510.spendiq"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.maps.android:maps-compose:2.4.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.4.0")

    // AndroidX and Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.security.crypto)

    // Navigation and ViewModel
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-rc01")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.espresso.core)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Retrofit and OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit library
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Gson converter for Retrofit
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // OkHttp for networking

    // Graphs
    implementation("io.github.ehsannarmani:compose-charts:0.0.14")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.work:work-runtime-ktx:2.8.0")
}