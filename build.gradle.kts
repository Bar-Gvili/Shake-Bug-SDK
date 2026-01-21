plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    id("com.google.gms.google-services") version "4.4.4" apply false

    // Kotlin – לנעול ל־1.9.24
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false

    // KSP – תואם Kotlin 1.9.24
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}
