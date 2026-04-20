import java.awt.Desktop
import java.util.Properties
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.kover)
}

val versionProps = Properties().apply {
    load(rootProject.file("gradle/version.properties").inputStream())
}

val releaseKeystorePropertiesFile = rootProject.file("keystore.properties")
val releaseKeystoreProperties = Properties().apply {
    if (releaseKeystorePropertiesFile.exists()) {
        load(releaseKeystorePropertiesFile.inputStream())
    }
}

val hasCompleteReleaseSigningConfig = listOf(
    "storeFile",
    "storePassword",
    "keyAlias",
    "keyPassword"
).all { requiredKey ->
    !releaseKeystoreProperties.getProperty(requiredKey).isNullOrBlank()
}

android {
    namespace = "dev.harrisonsoftware.stitchCounter"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.harrisonsoftware.stitchCounter"
        minSdk = 24
        targetSdk = 36
        versionCode = versionProps["VERSION_CODE"].toString().toInt()
        versionName = versionProps["VERSION_NAME"].toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            if (hasCompleteReleaseSigningConfig) {
                storeFile = file(releaseKeystoreProperties.getProperty("storeFile"))
                storePassword = releaseKeystoreProperties.getProperty("storePassword")
                keyAlias = releaseKeystoreProperties.getProperty("keyAlias")
                keyPassword = releaseKeystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Compose Destinations
    implementation(libs.compose.destinations.core)
    implementation(libs.compose.destinations.animations.core)
    ksp(libs.compose.destinations.ksp)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Coil for image loading
    implementation(libs.coil.compose)
    
    // Kotlinx Serialization for JSON
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.R",
                    "*.R$*",
                    "*.BuildConfig",
                    "*.Manifest*",
                    "*_Factory*",
                    "*_Provide*Factory*",
                    "*_MembersInjector*",
                    "*_HiltModules*",
                    "*_HiltComponents*",
                    "*_Impl*",
                    "*ComposableSingletons*",
                    "*\$*",
                    "*Hilt_*",
                    "*MainActivity*",
                    "*Destination*",
                    "*ScreenKt",
                    "*LayoutKt",
                    "*ComponentsKt",
                    "*BottomSheetKt",
                    "*TopBarKt",
                    "*TopBarsKt",
                    "*ProjectRowKt",
                    "*ExpandableSectionKt",
                    "*ThemeComponentsKt",
                    "*BackupComponentsKt",
                    "*SupportLegalComponentsKt",
                    "*NavGraphsKt",
                    "dev.harrisonsoftware.stitchCounter.StitchCounterAppKt",
                    "dev.harrisonsoftware.stitchCounter.ui.theme.*",
                    "dev.harrisonsoftware.stitchCounter.feature.*.*ScreenKt",
                    "dev.harrisonsoftware.stitchCounter.feature.*.*LayoutKt",
                    "dev.harrisonsoftware.stitchCounter.feature.sharedComposables.*"
                )
            }
        }
        variant("debug") {
            xml {
                onCheck = true
            }
            html {
                onCheck = true
            }
        }
    }
}

tasks.configureEach {
    if (name == "assembleRelease" || name == "bundleRelease") {
        dependsOn("testReleaseUnitTest")
        doFirst {
            if (!hasCompleteReleaseSigningConfig) {
                throw GradleException(
                    "Release signing is not configured. Create keystore.properties with storeFile, storePassword, keyAlias, and keyPassword."
                )
            }
        }
    }
}

tasks.register("buildPlayReleaseAab") {
    group = "release"
    description =
        "Runs release unit tests, builds the signed Play Store AAB, and opens the output folder when a graphical desktop is available."
    dependsOn("bundleRelease")
    doFirst {
        logger.lifecycle(
            "[SCBuildReleaseAab] Starting buildPlayReleaseAab for versionName=${versionProps["VERSION_NAME"]}, versionCode=${versionProps["VERSION_CODE"]}"
        )
    }
    doLast {
        val releaseBundleDir = file("$projectDir/build/outputs/bundle/release")
        val absolutePath = releaseBundleDir.absolutePath
        logger.lifecycle("[SCBuildReleaseAab] Output folder: $absolutePath")

        if (!Desktop.isDesktopSupported()) {
            return@doLast
        }
        val desktop = Desktop.getDesktop()
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            return@doLast
        }
        runCatching {
            desktop.open(releaseBundleDir)
        }.onFailure { throwable ->
            logger.lifecycle(
                "[SCBuildReleaseAab] Could not open folder in a file manager: ${throwable.message}"
            )
        }
    }
}