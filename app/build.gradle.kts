plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.linkup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.linkup"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth) // real-time database
    implementation(libs.firebase.database) // authentication
    implementation(libs.firebase.firestore) // cloud storage
    implementation(libs.firebase.storage)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid) // firestore database
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // additional SDK
    implementation("com.intuit.sdp:sdp-android:1.1.1") //sdp - a scalable size unit
    implementation("de.hdodenhof:circleimageview:3.1.0") // Create a CircularImageView
    implementation("com.squareup.picasso:picasso:2.8") // A powerful image downloading and caching library for Android
    implementation("androidx.multidex:multidex:2.0.1") // Enable multidex for apps with over 64K methods
//    //[Start Firebase Google / Facebook Login]
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    // Facebook 登入 SDK
    implementation ("com.facebook.android:facebook-login:latest.release")
//    //[End Firebase Google / Facebook Login]


}