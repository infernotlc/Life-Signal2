plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.tlh.demo1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tlh.demo1"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1-Beta")
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")
    implementation("androidx.appcompat:appcompat")
    implementation("androidx.health:health-services-client:1.1.0-alpha02")
    implementation ("com.google.guava:guava:31.0.1-jre")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
    implementation("androidx.wear.watchface:watchface:1.2.1")

}