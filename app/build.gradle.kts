plugins {
    alias(libs.plugins.android.application)



}
dependencies {
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1") // Use annotationProcessor instead of KSP

    // SQLCipher for encryption
    implementation("net.zetetic:android-database-sqlcipher:4.5.3")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    implementation ("com.itextpdf:itext7-core:7.1.15")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")
    implementation ("androidx.work:work-runtime:2.9.0")
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")



}






android {
    namespace = "com.example.adhd_monitor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.adhd_monitor"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

        defaultConfig {
           
            vectorDrawables.useSupportLibrary = true
        }


}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    //implementation(libs.mpandroidchart)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)



}

