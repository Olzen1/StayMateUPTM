    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.compose)
        alias(libs.plugins.google.services)
        alias(libs.plugins.ksp)
    }

    android {
        namespace = "com.staymate.uptm"
        compileSdk = 37

        defaultConfig {
            applicationId = "com.staymate.uptm"
            minSdk = 24
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

        buildFeatures {
            viewBinding = true
            compose = true
        }
    }

    dependencies {
        // Core
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)

        // UI - Views & Material
        implementation(libs.material)
        implementation(libs.androidx.constraintlayout)

        // UI - Compose
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.compose.icons.extended)

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui.test.junit4)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(libs.androidx.junit)

        // Debug
        debugImplementation(libs.androidx.compose.ui.test.manifest)
        debugImplementation(libs.androidx.compose.ui.tooling)

        // Firebase
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.auth)

        // Google Sign-In
        implementation(libs.play.services.auth)

        //firestore
        implementation(libs.kotlinx.coroutines.play.services)
        implementation(libs.kotlinx.coroutines.core)
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
        implementation("com.google.firebase:firebase-firestore-ktx")
        implementation("com.google.firebase:firebase-firestore")
        implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    }