//Module level gradle build file
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") //Kotlin
    id("com.google.devtools.ksp") //KSP
    id("org.jetbrains.kotlin.plugin.serialization") //to serialize kotlin class
    id("com.google.gms.google-services")
}
val ktor_version = "2.3.0"

android {
    namespace = "com.example.paintapp"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    defaultConfig {
        applicationId = "com.example.paintapp"
        minSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core libraries
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // Lifecycle, ViewModel, and LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.7.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.4")
    implementation("androidx.compose.material:material:1.7.4")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.4")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation ("androidx.compose.material:material-icons-extended:1.7.4") //for icons


    // Navigation
    implementation("androidx.navigation:navigation-ui-ktx:2.8.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.2")
    implementation("androidx.navigation:navigation-compose:2.8.2")

    // Fragment and Activity KTX
    implementation("androidx.fragment:fragment-ktx:1.8.4")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-testing:2.8.6")
    implementation("androidx.test.espresso:espresso-core:3.6.1")

    // Room for persistence
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Color wheel
    implementation("com.github.yukuku:ambilwarna:2.0.1")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.fragment:fragment-ktx:1.8.4")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.03"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.fragment:fragment-testing:1.8.4")// Or the latest version

    debugImplementation("androidx.fragment:fragment-testing-manifest:1.8.4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.3")

    val nav_version = "2.8.2"

    // Jetpack Compose integration
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Feature module support for Fragments
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    //tracing library
    implementation ("androidx.tracing:tracing:1.2.0")

    implementation ("com.google.android.material:material:1.12.0")

    //Client Side (android studio) Ktor stuff (Network requests and serialization)
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version") //This enables Ktor to handle content negotiation and serialization (like JSON) on the client-side.
    implementation("io.ktor:ktor-client-android:$ktor_version") //Use Android-Specific Ktor
    implementation("io.ktor:ktor-client-core:$ktor_version") //Ktor client core
    implementation("org.slf4j:slf4j-simple:2.0.7") // Adjust version as needed
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version") // //Integrates Kotlinx Serialization into Ktor.
    implementation("io.ktor:ktor-client-plugins:$ktor_version") ////Ktor plugins
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // // Core Kotlinx Serialization library– provides the actual logic to serialize/deserialize Kotlin objects to and from JSON.

    //adding support for firebase (handles the authentication)
    implementation ("com.google.firebase:firebase-auth:23.1.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
}


