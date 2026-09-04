plugins { id("com.android.application") }

android {
    namespace = "com.chokwangje.equipmentguide"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.chokwangje.equipmentguide"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.core:core:1.15.0")
    implementation("androidx.activity:activity:1.10.0")
}
