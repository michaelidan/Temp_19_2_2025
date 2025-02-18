plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)

    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.sharedfood"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sharedfood"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.22")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.22")
    }
}

dependencies {
    testImplementation ("org.mockito:mockito-core:5.4.0")
    testImplementation ("org.robolectric:robolectric:4.10")
    testImplementation ("junit:junit:4.13.2")

    // Force Kotlin stdlib version
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // Firebase BoM to manage versions of Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:32.1.0")) // Update to the latest BoM version

    // Firebase libraries (no need to specify versions, they will be managed by the BoM)
    implementation("com.google.firebase:firebase-auth") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    }
    implementation("com.google.firebase:firebase-firestore") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    }
    implementation("com.google.firebase:firebase-storage") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    }

    implementation("com.google.android.gms:play-services-location:18.0.0")

    // Other dependencies
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Play services (for authentication)
    implementation("com.google.android.gms:play-services-auth:21.3.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    }


    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.activity)

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.facebook.android:facebook-login:latest.release")
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

        //
    // Activity (libs version)
    implementation(libs.activity)

    // Test dependencies
    //testImplementation("junit:junit:4.13.2")  // כבר יש את השורה הזו
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("org.mockito:mockito-android:4.5.1")

    // ספריות לבדיקה עם Mockito ו-JUnit


    testImplementation("org.mockito:mockito-core:4.5.1")
    testImplementation("org.mockito:mockito-inline:4.5.1")



    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test:core:1.4.0")
    testImplementation("androidx.test:runner:1.5.2")
    testImplementation("androidx.test:rules:1.5.0")

    testImplementation("com.google.firebase:firebase-firestore:24.4.2")

    // Michael - please keep on them to the test of Admin - START
    testImplementation("net.bytebuddy:byte-buddy:1.12.22") // או גרסה חדשה יותר אם זמינה
    testImplementation("org.mockito:mockito-core:5.4.0")
    // Michael - please keep on them to the test of Admin - End
// implementation("androidx.recyclerview:recyclerview:1.4.0")



}
