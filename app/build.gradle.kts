plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.shashwat.stopify"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.shashwat.stopify"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("debugConfig") {
            val localKeystore = file("${rootDir}/debug.keystore")
            val defaultKeystore = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storeFile = if (localKeystore.exists()) localKeystore else defaultKeystore
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            val localKeystore = file("${rootDir}/debug.keystore")
            val defaultKeystore = file("${System.getProperty("user.home")}/.android/debug.keystore")
            if (localKeystore.exists() || defaultKeystore.exists()) {
                signingConfig = signingConfigs.getByName("debugConfig")
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val localKeystore = file("${rootDir}/debug.keystore")
            val defaultKeystore = file("${System.getProperty("user.home")}/.android/debug.keystore")
            signingConfig = if (localKeystore.exists() || defaultKeystore.exists()) {
                signingConfigs.getByName("debugConfig")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-datasource:1.5.1")
    implementation("androidx.media3:media3-session:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("io.github.kyant0:backdrop:2.0.1")
    testImplementation(libs.junit)
    testImplementation("org.json:json:20240303")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register("generateAppIcons") {
    doLast {
        val srcFile = rootProject.file("logo.png")
        if (!srcFile.exists()) return@doLast
        val resDir = file("src/main/res")

        val folders = listOf(
            "drawable",
            "mipmap-mdpi",
            "mipmap-hdpi",
            "mipmap-xhdpi",
            "mipmap-xxhdpi",
            "mipmap-xxxhdpi"
        )

        File(resDir, "drawable/ic_launcher_foreground.xml").delete()
        for (folder in folders) {
            val dir = File(resDir, folder).apply { mkdirs() }
            File(dir, "ic_launcher.webp").delete()
            File(dir, "ic_launcher_round.webp").delete()
            srcFile.copyTo(File(dir, "ic_launcher.png"), overwrite = true)
            srcFile.copyTo(File(dir, "ic_launcher_round.png"), overwrite = true)
        }

        srcFile.copyTo(File(resDir, "drawable/ic_launcher_foreground.png"), overwrite = true)
    }
}

tasks.named("preBuild") {
    dependsOn("generateAppIcons")
}