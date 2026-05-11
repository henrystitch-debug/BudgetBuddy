import java.util.Properties


val localProperties = Properties()
val localPropertiesFile: File? = rootProject.file("local.properties")
if (localPropertiesFile?.exists() == true) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.github.budgetbuddy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.budgetbuddy"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "ANTHROPIC_API_KEY",
            "\"${localProperties.getProperty("ANTHROPIC_API_KEY", "")}\""
        )

        buildConfigField(
            "String",
            "GOOGLE_API_KEY",
            "\"${localProperties.getProperty("GOOGLE_API_KEY", "")}\""
        )
    }

    buildFeatures {
        buildConfig = true
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
    implementation(libs.room)
    implementation(libs.recyclerview)
    implementation(libs.workmanager)
    implementation(libs.okhttp)
    implementation(libs.mpandroidchart)
    implementation(libs.viewpager2)
    implementation(libs.gemini.ai)


    // testing
    // ── Unit Testing ──────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.room.compiler)

    // Optional but highly recommended: mock objects
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline) // mock final classes

    // ── Instrumented Testing ──────────────────────────────────
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    // If you're using Room (very likely for an expense tracker):
    androidTestImplementation(libs.room.testing)
}
