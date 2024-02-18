plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
    kotlin("plugin.serialization")
}

android {
    namespace = "at.co.schwaerzler.maximilian.androidmailer"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    packaging {
        resources {
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/NOTICE.md"
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["java"])
                groupId = "at.co.schwaerzler.maximilian"
                artifactId = "androidmailer"
                version = "0.0.1"
            }
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Angus mail for Android
    // https://eclipse-ee4j.github.io/angus-mail/Android
    implementation("org.eclipse.angus:jakarta.mail:2.0.2")
    implementation("org.eclipse.angus:angus-activation:2.0.1")
    implementation("jakarta.activation:jakarta.activation-api:2.1.2")


    // WorkManager
    val workVersion = "2.9.0"

    // (Java only)
    implementation("androidx.work:work-runtime:$workVersion")

    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // optional - RxJava2 support
    implementation("androidx.work:work-rxjava2:$workVersion")

    // optional - GCMNetworkManager support
    implementation("androidx.work:work-gcm:$workVersion")

    // optional - Test helpers
    androidTestImplementation("androidx.work:work-testing:$workVersion")

    // optional - Multiprocess support
    implementation("androidx.work:work-multiprocess:$workVersion")


    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}