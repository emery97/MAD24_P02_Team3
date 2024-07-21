plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "sg.edu.np.mad.TicketFinder"
    compileSdk = 34

    defaultConfig {
        applicationId = "sg.edu.np.mad.TicketFinder"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
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
    packaging {
        resources {
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/DEPENDENCIES")
        }
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
    implementation(libs.car.ui.lib)
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
    implementation("com.google.firebase:firebase-messaging") {
        exclude(group = "com.google.firebase", module = "firebase-iid")
    }
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    // Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.libraries.places:places:2.5.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // For Google Auth and Calendar integrations
    implementation("com.google.android.gms:play-services-auth:20.3.0")
    implementation("org.apache.commons:commons-text:1.9")

    // For easy permissions
    implementation("pub.devrel:easypermissions:3.0.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha04")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Chat bot
    implementation("com.google.firebase:firebase-ml-natural-language:22.0.0") {
        exclude(group = "com.google.firebase", module = "firebase-iid")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
        exclude(group = "com.google.type", module = "PostalAddressProto")
        exclude(group = "com.google.type", module = "Quaternion")
        exclude(group = "com.google.type", module = "TimeOfDay")
        exclude(group = "com.google.type", module = "TimeZone")
    }
    implementation("com.google.firebase:firebase-ml-natural-language-smart-reply-model:20.0.7") {
        exclude(group = "com.google.firebase", module = "firebase-iid")
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
        exclude(group = "com.google.type", module = "PostalAddressProto")
        exclude(group = "com.google.type", module = "Quaternion")
        exclude(group = "com.google.type", module = "TimeOfDay")
        exclude(group = "com.google.type", module = "TimeZone")
    }
    implementation("com.airbnb.android:lottie:5.0.3")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("com.google.cloud:google-cloud-translate:1.95.1") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
        exclude(group = "com.google.type", module = "PostalAddressProto")
        exclude(group = "com.google.type", module = "Quaternion")
        exclude(group = "com.google.type", module = "TimeOfDay")
        exclude(group = "com.google.type", module = "TimeZone")
    }

    // for unfriend activity
    implementation ("com.google.code.gson:gson:2.8.6")

}
