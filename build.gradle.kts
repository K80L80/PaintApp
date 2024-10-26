// Top-level build file where you can add configuration options common to all sub-projects/modules.
//Project level gradle file
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false //Kotlin Version
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false //KSP version
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}
