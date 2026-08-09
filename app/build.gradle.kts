import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
	id("com.android.application")
	id("com.google.gms.google-services")
	id("com.google.firebase.crashlytics")
	id("com.google.firebase.firebase-perf")

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

val releaseKeyAlias = providers.environmentVariable("TU_INDICE_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("TU_INDICE_KEY_PASSWORD").orNull
val releaseKeyStorePath = providers.environmentVariable("TU_INDICE_KEY_STORE_PATH").orNull
val releaseKeyStorePassword = providers.environmentVariable("TU_INDICE_KEY_STORE_PASSWORD").orNull
val hasReleaseSigningConfig = listOf(
	releaseKeyAlias,
	releaseKeyPassword,
	releaseKeyStorePath,
	releaseKeyStorePassword
).all { !it.isNullOrBlank() }
val appVersionPropertiesFile = rootProject.file("gradle/app-version.properties")
val appVersionProperties = Properties().apply {
	check(appVersionPropertiesFile.isFile) {
		"Missing app version properties file: ${appVersionPropertiesFile.absolutePath}"
	}
	appVersionPropertiesFile.inputStream().use(::load)
}
fun appVersionProperty(name: String): String =
	checkNotNull(appVersionProperties.getProperty(name)?.trim()?.takeIf { it.isNotEmpty() }) {
		"Missing app version property '$name' in ${appVersionPropertiesFile.absolutePath}"
	}
val appVersionName = appVersionProperty("versionName")
val androidVersionCode = appVersionProperty("androidVersionCode").toInt()
val debugApiBaseUrl = providers.gradleProperty("tuindice.apiBaseUrl")
	.orElse(providers.environmentVariable("TUINDICE_API_BASE_URL"))
	.orElse("http://10.0.2.2:8080/")
val debugPrivacyPolicyUrl = providers.gradleProperty("tuindice.privacyPolicyUrl")
	.orElse(providers.environmentVariable("TUINDICE_PRIVACY_POLICY_URL"))
	.orElse("https://tuindice.app/privacy")
val debugTermsAndConditionsUrl = providers.gradleProperty("tuindice.termsAndConditionsUrl")
	.orElse(providers.environmentVariable("TUINDICE_TERMS_AND_CONDITIONS_URL"))
	.orElse("https://tuindice.app/terms")
val debugSupportUrl = providers.gradleProperty("tuindice.supportUrl")
	.orElse(providers.environmentVariable("TUINDICE_SUPPORT_URL"))
	.orElse("https://tuindice.app/support")

fun String.toBuildConfigString(): String =
	"\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

extensions.configure<ApplicationExtension> {
	compileSdk = 37

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}

	buildFeatures {
		compose = true
		buildConfig = true
		resValues = false
	}

	defaultConfig {
		applicationId = "com.gdavidpb.tuindice"
		minSdk = 24
		targetSdk = 36
		versionCode = androidVersionCode
		versionName = appVersionName
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		buildConfigField("long", "PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER", "375954751920L")
	}

	signingConfigs {
		create("release") {
			if (hasReleaseSigningConfig) {
				keyAlias = releaseKeyAlias
				keyPassword = releaseKeyPassword
				storeFile = file(releaseKeyStorePath!!)
				storePassword = releaseKeyStorePassword
			}
		}
	}

	packaging {
		resources {
			excludes += setOf("DebugProbesKt.bin")
		}
	}

	buildTypes {
		getByName("debug") {
			isDebuggable = true
			isMinifyEnabled = false
			applicationIdSuffix = ".debug"

			buildConfigField("String", "URL_API", debugApiBaseUrl.get().toBuildConfigString())
			buildConfigField(
				"String",
				"URL_PRIVACY_POLICY",
				debugPrivacyPolicyUrl.get().toBuildConfigString()
			)
			buildConfigField(
				"String",
				"URL_TERMS_AND_CONDITIONS",
				debugTermsAndConditionsUrl.get().toBuildConfigString()
			)
			buildConfigField(
				"String",
				"URL_SUPPORT",
				debugSupportUrl.get().toBuildConfigString()
			)
		}
		getByName("release") {
			isDebuggable = false
			isMinifyEnabled = true
			isShrinkResources = true

			buildConfigField("String", "URL_API", "\"https://api.tuindice.app/\"")
			buildConfigField(
				"String",
				"URL_PRIVACY_POLICY",
				"\"https://tuindice.app/privacy\""
			)
			buildConfigField(
				"String",
				"URL_TERMS_AND_CONDITIONS",
				"\"https://tuindice.app/terms\""
			)
			buildConfigField(
				"String",
				"URL_SUPPORT",
				"\"https://tuindice.app/support\""
			)

			if (hasReleaseSigningConfig) {
				signingConfig = signingConfigs.getByName("release")
			}

			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}

	namespace = defaultConfig.applicationId
}

dependencies {
	implementation(platform(libs.compose.bom))
	implementation(libs.bundles.compose)
	implementation(libs.core.ktx)
	implementation(libs.bundles.ktor)
	implementation(libs.ktor.client.okhttp)
	implementation(libs.bundles.google)
	implementation(platform(libs.firebase.bom))
	implementation(libs.bundles.firebase)
	implementation(libs.koin.android)
	implementation(libs.koin.core)
	implementation(libs.kotlinx.coroutines.android)
	implementation(libs.kotlinx.coroutines.play.services)
	implementation(libs.filekit.dialogs)
	implementation(libs.tink.android)
	testImplementation(libs.bundles.testing)
	testImplementation(libs.ktor.client.mock)
	testImplementation(libs.kotlinx.coroutines.test)
	androidTestImplementation(libs.bundles.testing.android)
	androidTestImplementation(libs.test.ext.junit)

	implementation(project(":base"))
	implementation(project(":maincore"))
	implementation(project(":persistence"))
	implementation(project(":security"))
	implementation(project(":auth"))
	implementation(project(":about"))
	implementation(project(":summary"))
	implementation(project(":record"))
	implementation(project(":enrollmentproof"))
	implementation(project(":evaluations"))
	implementation(project(":subjects"))
	implementation(project(":wizard"))
	implementation(project(":pensum"))
}

tasks.configureEach {
	val isDebugGoogleServicesTask = name.contains("GoogleServices") && name.contains("Debug")
	val isDebugCrashlyticsTask = name.contains("Crashlytics") && name.contains("Debug")
	val isReleaseCrashlyticsMappingUploadTask = name == "uploadCrashlyticsMappingFileRelease"
	val uploadReleaseCrashlyticsMapping =
		providers.environmentVariable("TUINDICE_UPLOAD_CRASHLYTICS_MAPPING").orNull == "1"

	if (
		isDebugGoogleServicesTask ||
		isDebugCrashlyticsTask ||
		(isReleaseCrashlyticsMappingUploadTask && !uploadReleaseCrashlyticsMapping)
	) {
		enabled = false
	}
}
