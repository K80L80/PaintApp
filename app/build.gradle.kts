plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.paintapp"
    compileSdk = 34
    buildFeatures {
        viewBinding = true
        dataBinding = true

        compose = true //to use jetpack compose
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2" //to use jetpack compose
    }
    defaultConfig {
        applicationId = "com.example.paintapp"
        minSdk = 34
//        targetSdk = 34
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-testing:2.8.5")

// Color wheel library
    implementation("com.github.yukuku:ambilwarna:2.0.1")

// Fragment and activity libraries
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    val fragment_version = "1.8.3"
    debugImplementation("androidx.fragment:fragment-testing:$fragment_version")

// Jetpack Compose libraries
    implementation("androidx.compose.ui:ui:1.7.1")
    implementation("androidx.compose.material:material:1.7.1")
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.1")
    implementation("androidx.compose.ui:ui-graphics:1.5.1")
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))

// Jetpack Navigation libraries
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.3")

// Testing libraries
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

// Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

// Room and KSP
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

// Gson for JSON handling
    implementation("com.google.code.gson:gson:2.8.8")

}






