plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "sg.edu.np.mad.TicketFinderSG"
    compileSdk = 34

    defaultConfig {
        applicationId = "sg.edu.np.mad.TicketFinderSG"
        minSdk = 33
        targetSdk = 34
        versionCode = 2
        versionName = "2"

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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.glide)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.appcheck.playintegrity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.paypal.sdk:paypal-android-sdk:2.16.0")
    implementation("com.stripe:stripe-java:25.0.0")
    implementation("com.stripe:stripe-android:20.44.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
}