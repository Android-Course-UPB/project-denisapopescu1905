plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id ("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.kapt")
}


android {
    namespace = "com.example.blescan"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.example.blescan"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    implementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    val navVersion = "2.7.6"
    val composeVersion = "1.1.0"
    val retrofit_version = "2.9.0"
    val moshi_version = "1.12.0"


    //FireBase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")


    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-graphics:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.material3:material3:$composeVersion")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")
    implementation("com.google.accompanist:accompanist-permissions:0.31.1-alpha")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.influxdb:influxdb-client-kotlin:6.6.0")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    // Feature module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navVersion")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")

    // Jetpack Compose Integration
    implementation("androidx.navigation:navigation-compose:$navVersion")

    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.2")
    implementation("androidx.compose.material3:material3-adaptive:1.0.0-alpha04")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.0.0-alpha02")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("no.nordicsemi.android.kotlin.ble:scanner:1.0.14")

    //Icons
    implementation("androidx.compose.material:material-icons-extended:1.6.3")
    implementation("androidx.compose:compose-bom:2024.03.00")
    //Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.AnyChart:AnyChart-Android:1.1.5")
    implementation("co.yml:ycharts:2.1.0")

    //MapBox
    implementation("com.mapbox.maps:android:11.3.0")
    implementation("com.mapbox.extension:maps-compose:11.3.0")

    // REST API
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofit_version")
    implementation("com.squareup.moshi:moshi:$moshi_version")
    implementation("com.squareup.moshi:moshi-kotlin:$moshi_version")

    implementation("com.android.volley:volley:1.2.0")

    //Tests
    testImplementation("androidx.compose.ui:ui-test-junit4:1.6.7")
    testImplementation("org.mockito:mockito-core:3.12.4")

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")

    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
}

