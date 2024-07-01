buildscript {
    repositories {
        google()
        mavenCentral()
        maven ( url ="https://jitpack.io" )
    }
    dependencies {
        classpath(libs.google.services)
        classpath("com.google.gms:google-services:4.4.1")
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}
