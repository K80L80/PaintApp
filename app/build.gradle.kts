plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs")
    //To use safe args jetpack navigation feature
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

android {
    namespace = "com.example.paintapp"
    compileSdk = 34
    buildFeatures{
        viewBinding = true
        dataBinding = true

        compose = true //to use jetpack compose
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0" //to use jetpack compose
    }
    defaultConfig {
        applicationId = "com.example.paintapp"
        minSdk = 34
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-testing:2.8.5")
    testImplementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //for the color wheel
    implementation ("com.github.yukuku:ambilwarna:2.0.1")

    //to get livedata + viewmodel stuff
    implementation("androidx.activity:activity-ktx:1.7.2")
   // testImplementation("androidx.activity:activity-ktx:1.7.2")
    //Fragment stuff
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    testImplementation("androidx.fragment:fragment-ktx:1.6.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    val fragment_version = "1.8.3"

    //for testing
    debugImplementation("androidx.fragment:fragment-testing-manifest:$fragment_version")
    androidTestImplementation("androidx.fragment:fragment-testing:$fragment_version")

    //To use Jetpack Compose
    implementation ("androidx.compose.ui:ui:1.7.1")
    implementation ("androidx.compose.material:material:1.7.1")

    //jetpack preview annotations (provides developer a UI) to see components of user UI
    implementation ("androidx.compose.ui:ui-tooling-preview:1.7.1")

    //To use jetpack navigation
    implementation ("androidx.navigation:navigation-ui-ktx:2.8.0")

    //Jetpack Navigation w/ Views - need to make navigation folder + navigation xml file
    implementation ("androidx.activity:activity-compose:1.9.2")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.8.0")

    //Jetpack Navigation w/ Jetpack compose - Navigation is done in kotlin file (no seperate xml file)
    implementation ("androidx.navigation:navigation-compose: 2.5.3")

}




