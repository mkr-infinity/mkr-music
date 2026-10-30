import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.net.URL
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

// Canonical version resolution from root version.properties
val versionPropertiesFile = rootProject.file("version.properties")
if (!versionPropertiesFile.exists()) {
    throw GradleException("Canonical version file 'version.properties' not found at project root (${versionPropertiesFile.absolutePath}).")
}

val versionProperties = Properties().apply {
    versionPropertiesFile.inputStream().use { load(it) }
}

val canonicalAppVersion = versionProperties.getProperty("APP_VERSION")?.trim()
    ?: throw GradleException("Missing 'APP_VERSION' key in version.properties.")

val semverRegex = Regex("""^(\d+)\.(\d+)\.(\d+)$""")
val semverMatch = semverRegex.matchEntire(canonicalAppVersion)
    ?: throw GradleException("Invalid APP_VERSION '$canonicalAppVersion' in version.properties. Must strictly follow semantic versioning MAJOR.MINOR.PATCH (e.g., 1.0.0).")

val (majorStr, minorStr, patchStr) = semverMatch.destructured
val versionMajor = majorStr.toIntOrNull() ?: throw GradleException("Invalid MAJOR component in '$canonicalAppVersion'")
val versionMinor = minorStr.toIntOrNull() ?: throw GradleException("Invalid MINOR component in '$canonicalAppVersion'")
val versionPatch = patchStr.toIntOrNull() ?: throw GradleException("Invalid PATCH component in '$canonicalAppVersion'")

if (versionMinor !in 0..99) {
    throw GradleException("MINOR version '$versionMinor' out of supported range (0..99).")
}
if (versionPatch !in 0..99) {
    throw GradleException("PATCH version '$versionPatch' out of supported range (0..99).")
}
if (versionMajor < 0 || versionMajor > 214748) {
    throw GradleException("MAJOR version '$versionMajor' out of supported range (0..214748).")
}

val computedVersionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
if (computedVersionCode <= 77) {
    throw GradleException("Computed versionCode ($computedVersionCode) must be strictly greater than historical version code 77 to permit Android upgrades.")
}
plugins {
    id("com.android.application")
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobufPlugin)
}

android {
    namespace = "com.music.mkr"
    compileSdk = 37
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = "com.kaif.music"
        minSdk = 26
        targetSdk = 37
        versionCode = computedVersionCode
        versionName = canonicalAppVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // LastFM API keys from local.properties or environment with fallback
        val lastFmKey = localProperties.getProperty("LASTFM_API_KEY")
            ?: System.getenv("LASTFM_API_KEY")
            ?: "694cbaa17c78202a133eac4656dff651"
        val lastFmSecret = localProperties.getProperty("LASTFM_SECRET")
            ?: System.getenv("LASTFM_SECRET")
            ?: "a0fdaf6060f19128c4a84f297c71e627"

        buildConfigField("String", "LASTFM_API_KEY", "\"$lastFmKey\"")
        buildConfigField("String", "LASTFM_SECRET", "\"$lastFmSecret\"")



//add nightly build label support
        val isNightly = project.hasProperty("nightly") && project.property("nightly") == "true"
        buildConfigField("Boolean", "IS_NIGHTLY", isNightly.toString())
        buildConfigField("Boolean", "CAST_AVAILABLE", "true")
    }

    flavorDimensions += listOf("abi")
    productFlavors {
        create("universal") {
            dimension = "abi"
            isDefault = true
            buildConfigField("String", "ARCHITECTURE", "\"universal\"")
        }
        create("arm64") {
            dimension = "abi"
            buildConfigField("String", "ARCHITECTURE", "\"arm64\"")
            ndk {
                abiFilters.add("arm64-v8a")
            }
        }
        create("armeabi") {
            dimension = "abi"
            buildConfigField("String", "ARCHITECTURE", "\"armeabi\"")
            ndk {
                abiFilters.add("armeabi-v7a")
            }
        }
        create("x86") {
            dimension = "abi"
            buildConfigField("String", "ARCHITECTURE", "\"x86\"")
            ndk {
                abiFilters.add("x86")
            }
        }
        create("x86_64") {
            dimension = "abi"
            buildConfigField("String", "ARCHITECTURE", "\"x86_64\"")
            ndk {
                abiFilters.add("x86_64")
            }
        }
    }

   signingConfigs {
    create("release") {
        val keystoreFile = file("keystore/release.keystore")
        if (keystoreFile.exists()) {
            storeFile = keystoreFile
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    getByName("debug") {
        keyAlias = "androiddebugkey"
        keyPassword = "android"
        storePassword = "android"
        storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
    }
}

buildTypes {
    release {
        signingConfig = if (file("keystore/release.keystore").exists())
            signingConfigs.getByName("release")
        else
            signingConfigs.getByName("debug")
        isMinifyEnabled = true
        isShrinkResources = true
        isCrunchPngs = false
        isDebuggable = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        buildConfigField("String", "ARCHITECTURE", "\"release\"")
    }
    debug {
        applicationIdSuffix = ".debug"
        isDebuggable = true
        signingConfig = signingConfigs.getByName("debug")
        buildConfigField("String", "ARCHITECTURE", "\"debug\"")
    }
}

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
        compilerOptions {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
            freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    lint {
        lintConfig = file("lint.xml")
        warningsAsErrors = false
        abortOnError = false
        checkDependencies = false
    }

    androidResources {
        generateLocaleConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
            keepDebugSymbols += listOf(
                "**/libandroidx.graphics.path.so",
                "**/libdatastore_shared_counter.so"
            )
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/CONTRIBUTORS.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn"
        )
        suppressWarnings.set(false)
    }
}

dependencies {
    implementation(libs.guava)
    implementation(libs.coroutines.guava)
    implementation(libs.concurrent.futures)

    implementation(libs.activity)
    implementation(libs.hilt.navigation)
    implementation(libs.datastore)

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.animation)
    implementation(libs.compose.reorderable)

    implementation(libs.viewmodel)
    implementation(libs.viewmodel.compose)

    implementation(libs.material3)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(libs.material3.adaptive.navigation.suite)
    implementation(libs.palette)
    implementation(libs.materialKolor)
    implementation(libs.vico.compose.m3)

    implementation(libs.appcompat)

    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)

    implementation(libs.ucrop)

    implementation(libs.shimmer)

    implementation(libs.media3)
    implementation(libs.media3.session)
    implementation(libs.media3.hls)
    implementation(libs.media3.ui)
    implementation(libs.media3.okhttp)

    // Google Cast
    implementation(libs.media3.cast)
    implementation(libs.mediarouter)
    implementation(libs.cast.framework)

    implementation(libs.room.runtime)
    implementation(libs.kuromoji.ipadic)
    implementation(libs.tinypinyin)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.apache.lang3)

    implementation(libs.hilt)
    implementation(libs.jsoup)
    ksp(libs.hilt.compiler)

    implementation(project(":innertube"))
    implementation(project(":kizzy"))
    implementation(project(":lastfm"))
    implementation(project(":spotify"))
    implementation(project(":lyricsProvider"))

    implementation(libs.innertubex)
    implementation(libs.expressivelab)




    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)

    // Protobuf for message serialization (lite version for Android)
    implementation(libs.protobuf.javalite)
    implementation(libs.protobuf.kotlin.lite)

    coreLibraryDesugaring(libs.desugaring)
    implementation(libs.timber)
    implementation(libs.smoothCorner)
    implementation(libs.lottie.compose)
    implementation(libs.haze)
    implementation(libs.work.runtime.ktx)
    implementation(libs.androidx.core.splashscreen)
}

abstract class PrintAppVersionTask : DefaultTask() {
    @get:Input
    abstract val versionName: Property<String>

    @get:Input
    abstract val versionCode: Property<Int>

    @TaskAction
    fun printVersion() {
        println("CANONICAL_VERSION_NAME=${versionName.get()}")
        println("COMPUTED_VERSION_CODE=${versionCode.get()}")
    }
}

tasks.register<PrintAppVersionTask>("printAppVersion") {
    description = "Prints the canonical app version name and computed version code for CI validation"
    versionName.set(canonicalAppVersion)
    versionCode.set(computedVersionCode)
}

