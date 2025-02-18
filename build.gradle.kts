// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.gms.google.services) apply false

}

buildscript {
//ext.kotlin_version = "1.5.0"
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    repositories {
        mavenCentral()
        maven {
            url = uri("https://company/com/maven2")
        }
        mavenLocal()
        flatDir {
            dirs ("libs")
        }
    }

    dependencies {
        // Google Services classpath (update to latest stable version)
        classpath(libs.google.services)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.gradle)
    }
}

//tasks.register("clean") {
//    delete(rootProject.buildDir)
//}