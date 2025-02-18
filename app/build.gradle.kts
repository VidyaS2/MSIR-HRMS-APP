

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    id ("org.jetbrains.kotlin.android".toString())

}

android {
    namespace = "com.attendance.clockme"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.attendance.clockme"
        minSdk = 26
        targetSdk = 34
        versionCode = 7
        versionName = "1.6"
        vectorDrawables.useSupportLibrary = true

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        pickFirst ("META-INF/NOTICE.md".toString())
        pickFirst ("META-INF/LICENSE.md".toString())
        // Use pickFirst to pick one of the duplicate files
    }
}

dependencies {
    // AndroidX and Material Design Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase Libraries
    implementation(platform(libs.firebase.bom)) // Import the Firebase BoM
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation (libs.firebase.messaging.v2312)
    implementation (libs.firebase.analytics.v2110)

    // Lifecycle Libraries
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Google Sign-In Dependency
    implementation(libs.play.services.auth)

    // Navigation Components
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // RecyclerView
    implementation(libs.recyclerview)

    // Mail-related dependencies
    implementation(libs.android.activation)
    implementation(libs.android.mail)

    // CardView
    implementation(libs.cardview)

    // Google Maps and Location Services
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.gridlayout)
    implementation(libs.firebase.messaging)

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.v115) // Ensure this is the correct version
    androidTestImplementation(libs.espresso.core.v351) // Ensure this is the correct version
    implementation(kotlin("script-runtime"))

    implementation (libs.play.services.maps.v1810) // Latest version as of now
    implementation (libs.play.services.location.v1901) // Optional: For location services

    implementation (libs.glide)

    implementation (libs.shortcutbadger)

    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.dexter)

    implementation (libs.material.v190)
    implementation (libs.shortcutbadger)
    implementation (libs.firebase.messaging.v2300)


}

