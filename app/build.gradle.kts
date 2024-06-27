plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt") // Hinzufügen des Kapt-Plugins
}

android {
    namespace = "com.example.foodmanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.foodmanager"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Volley library
    implementation("com.android.volley:volley:1.2.1")

    // Room components
    implementation("androidx.room:room-runtime:2.5.2")
    annotationProcessor ("androidx.room:room-compiler:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2") // For Kotlin
    implementation("androidx.room:room-rxjava2:2.5.2")
    implementation("androidx.room:room-rxjava3:2.5.2")
    implementation("androidx.room:room-guava:2.5.2")
    implementation ("androidx.room:room-ktx:2.5.2")
    testImplementation("androidx.room:room-testing:2.5.2")

    // Additional dependencies
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.activity:activity-ktx:1.6.0")
    implementation("com.google.android.material:material:1.8.0")

    // Test helpers
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
