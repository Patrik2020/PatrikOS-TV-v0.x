plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "hu.patrikos.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "hu.patrikos.tv"
        minSdk = 21
        targetSdk = 34
        versionCode = 4
        versionName = "0.3.1"
    }

    signingConfigs {
        create("development") {
            storeFile = rootProject.file("keystore/patrikos-dev.keystore")
            storePassword = "patrikos-dev"
            keyAlias = "patrikos-dev"
            keyPassword = "patrikos-dev"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("development")
        }
        release {
            isMinifyEnabled = true
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

    lint {
        checkReleaseBuilds = true
        abortOnError = true
    }
}

dependencies {
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
