plugins { id("com.android.application") }
android {
    namespace = "com.chokwangje.equipmentguide"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.chokwangje.equipmentguide"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }
}
dependencies {
    implementation("androidx.activity:activity:1.10.0")
    implementation("androidx.core:core:1.15.0")
}
