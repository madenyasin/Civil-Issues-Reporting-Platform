plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.daggerHilt)
    alias(libs.plugins.googleGmsGoogleServices)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.dawinder.btnjc"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.dawinder.btnjc"
        minSdk = 23
        //noinspection OldTargetApi
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}


dependencies {
    // Maps SDK for Android
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Jetpack Compose Platform
    implementation(platform(libs.compose.bom))
    implementation(libs.firebase.auth)
    androidTestImplementation(platform(libs.compose.bom))

    // Jetpack Compose Libraries with BOM version
    implementation(libs.bundles.compose)

    // Other Jetpack Compose Libraries
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)

    // Navigation Compose
    implementation(libs.navigation.compose)

    // Core Libraries
    implementation(libs.core.ktx)

    // Dagger Hilt for Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ui.test.junit4)

    // Debugging Tools
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
}