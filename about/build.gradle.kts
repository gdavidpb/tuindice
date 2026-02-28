plugins {
	kotlin("multiplatform")
	id("com.android.kotlin.multiplatform.library")
	alias(libs.plugins.compose.multiplatform)

	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	android {
		namespace = "com.gdavidpb.tuindice.about"
		compileSdk = 36
		minSdk = 24

		androidResources {
			enable = true
		}
	}
	iosX64()
	iosArm64()
	iosSimulatorArm64()

	sourceSets {
		val commonMain by getting {
			kotlin.srcDir("build/generated/aboutDependencyTexts/commonMain/kotlin")

			dependencies {
				implementation(project(":base"))
				implementation(libs.navigation.compose)
				implementation(libs.koin.compose)
				implementation(libs.components.resources)
				implementation(libs.material.icons.extended)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
				implementation(project(":testkit"))
			}
		}

		val androidMain by getting {
			dependencies {
				implementation(project(":base"))
			}
		}
	}
}

abstract class GenerateAboutDependencyTextsTask : DefaultTask() {
	@get:InputFile
	abstract val versionCatalogFile: RegularFileProperty

	@get:OutputDirectory
	abstract val outputDir: DirectoryProperty

	@TaskAction
	fun generate() {
		val versions = readVersions(versionCatalogFile.get().asFile)
		val packageDir = outputDir.get()
			.dir("com/gdavidpb/tuindice/about/generated")
			.asFile
		packageDir.mkdirs()

		packageDir.resolve("AboutDependencyTexts.kt").writeText(
			"""
			package com.gdavidpb.tuindice.about.generated

			object AboutDependencyTexts {
				const val kotlinDescription = "Kotlin ${versions.getValue("kotlin")}\nConcise. Cross-platform. Fun.\n© 2012 JetBrains"
				const val composeDescription = "JetPack Compose ${versions.getValue("compose-bom")}\nBuild better apps faster with Jetpack Compose.\n© 2019 JetBrains & Google"
				const val firebaseDescription = "Firebase ${versions.getValue("firebase-bom")}\nQuickly develop high-quality apps and grow your business.\n© 2014 Google"
				const val koinDescription = "Koin ${versions.getValue("koin")}\nA pragmatic lightweight dependency injection framework for Kotlin.\n© 2018 Arnaud Giuliani"
				const val ktorDescription = "Ktor ${versions.getValue("ktor")}\nCreate asynchronous client and server applications. Open Source, free, and fun!.\n© 2017 JetBrains"
			}
			""".trimIndent()
		)
	}

	private fun readVersions(file: File): Map<String, String> {
		val versions = mutableMapOf<String, String>()
		var inVersionsSection = false

		file.forEachLine { line ->
			val trimmedLine = line.substringBefore("#").trim()

			when {
				trimmedLine == "[versions]" -> inVersionsSection = true
				trimmedLine.startsWith("[") && trimmedLine != "[versions]" -> inVersionsSection = false
				inVersionsSection && "=" in trimmedLine -> {
					val (key, value) = trimmedLine.split("=", limit = 2)
					versions[key.trim()] = value.trim().removeSurrounding("\"")
				}
			}
		}

		return versions
	}
}

val generateAboutDependencyTexts = tasks.register<GenerateAboutDependencyTextsTask>(
	"generateAboutDependencyTexts"
) {
	versionCatalogFile.set(rootProject.layout.projectDirectory.file("gradle/libs.versions.toml"))
	outputDir.set(layout.buildDirectory.dir("generated/aboutDependencyTexts/commonMain/kotlin"))
}

tasks.matching {
	it.name.startsWith("compileKotlin") ||
			it.name.startsWith("compileTestKotlin") ||
			it.name == "compileAndroidMain"
}.configureEach {
	dependsOn(generateAboutDependencyTexts)
}
