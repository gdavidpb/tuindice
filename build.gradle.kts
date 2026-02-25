plugins {
	alias(libs.plugins.compose.compiler) apply false
}

buildscript {
	repositories {
		google()
		mavenCentral()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	}

	dependencies {
		classpath(libs.google.services)
		classpath(libs.android.gradle.plugin)
		classpath(libs.kotlin.gradle.plugin)
		classpath(libs.firebase.crashlytics.gradle)
	}
}

allprojects {
	repositories {
		google()
		mavenCentral()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	}
}

tasks.register("clean", Delete::class) {
	delete(layout.buildDirectory)
}

tasks.register("checkCommonMainPlatformLeaks") {
	group = "verification"
	description = "Fails when commonMain source sets use platform-specific android/java packages."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val commonMainSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/*.kt")
		}

		val forbiddenImports = listOf(
			Regex("""^\s*import\s+android\."""),
			Regex("""^\s*import\s+java\."""),
			Regex(
				"""^\s*import\s+androidx\.(?!compose\.|lifecycle\.|datastore\.|room\.|navigation\.).*"""
			)
		)

		val violations = mutableListOf<String>()

		commonMainSources.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					if (forbiddenImports.any { pattern -> pattern.containsMatchIn(line) }) {
						violations += "${file.relativeTo(rootDir)}:${index + 1}: $line"
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Platform leakage detected in commonMain:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkAllowedExpectUsageInCommonMain") {
	group = "verification"
	description = "Fails when commonMain introduces expect declarations outside approved Room KMP constructor."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val commonMainSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/*.kt")
		}

		val allowedExpectFile = "persistence/src/commonMain/kotlin/com/gdavidpb/tuindice/persistence/data/room/TuIndiceDatabase.kt"
		val expectPattern = Regex("""^\s*expect\s+""")
		val allowedPattern = Regex("""^\s*expect\s+object\s+TuIndiceDatabaseConstructor\b""")
		val violations = mutableListOf<String>()

		commonMainSources.files
			.sortedBy { it.path }
			.forEach { file ->
				val relativePath = file.relativeTo(rootDir).invariantSeparatorsPath
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					if (!expectPattern.containsMatchIn(line)) return@forEachIndexed

					val isAllowed = relativePath == allowedExpectFile &&
						allowedPattern.containsMatchIn(line)

					if (!isAllowed) {
						violations += "$relativePath:${index + 1}: $line"
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Unexpected expect usage detected in commonMain:")
					appendLine("Only Room constructor expect is allowed: $allowedExpectFile")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkNoBuildConfigInCommonMain") {
	group = "verification"
	description = "Fails when commonMain sources reference platform BuildConfig."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val commonMainSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/*.kt")
		}

		val forbiddenPattern = Regex("""\bBuildConfig\b""")
		val violations = mutableListOf<String>()

		commonMainSources.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					if (forbiddenPattern.containsMatchIn(line)) {
						violations += "${file.relativeTo(rootDir)}:${index + 1}: $line"
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("BuildConfig references are not allowed in commonMain:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkNoJvmStreamsInCommonMain") {
	group = "verification"
	description = "Fails when commonMain sources use JVM stream/reader/writer types."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val commonMainSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/*.kt")
		}

		val forbiddenPatterns = listOf(
			Regex("""\bInputStream\b"""),
			Regex("""\bOutputStream\b"""),
			Regex("""\bReader\b"""),
			Regex("""\bWriter\b""")
		)
		val violations = mutableListOf<String>()

		commonMainSources.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					if (forbiddenPatterns.any { pattern -> pattern.containsMatchIn(line) }) {
						violations += "${file.relativeTo(rootDir)}:${index + 1}: $line"
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("JVM stream/reader/writer types are not allowed in commonMain:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkNoHardcodedTopBarTitlesInCommonMain") {
	group = "verification"
	description = "Fails when commonMain contracts declare hardcoded topBarTitle string defaults."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val contractSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/presentation/contract/**/*.kt")
		}

		val forbiddenPattern = Regex("""topBarTitle\s*:\s*String\s*=\s*"[^"]+"""")
		val violations = mutableListOf<String>()

		contractSources.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					if (forbiddenPattern.containsMatchIn(line)) {
						violations += "${file.relativeTo(rootDir)}:${index + 1}: $line"
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Hardcoded topBarTitle defaults are not allowed in commonMain contracts:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkNoHardcodedComposeMultiplatformPluginVersion") {
	group = "verification"
	description = "Fails when Gradle scripts hardcode the org.jetbrains.compose plugin version."
	notCompatibleWithConfigurationCache("Scans Gradle scripts directly.")

	doLast {
		val gradleScripts = fileTree(rootDir) {
			include("**/build.gradle.kts")
			exclude("build/**", ".gradle/**")
		}

		val forbiddenPattern = Regex(
			"""^\s*id\("org\.jetbrains\.compose"\)\s+version\s+["'][^"']+["']"""
		)
		val violations = mutableListOf<String>()

		gradleScripts.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					if (forbiddenPattern.containsMatchIn(line)) {
						violations += "${file.relativeTo(rootDir)}:${index + 1}: $line"
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Hardcoded Compose Multiplatform plugin versions are not allowed:")
					appendLine("Use version catalog alias libs.plugins.compose.multiplatform instead.")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkKmpComposePluginCoverage") {
	group = "verification"
	description = "Fails when KMP modules use compose compiler without Compose Multiplatform plugin alias."
	notCompatibleWithConfigurationCache("Scans module Gradle scripts directly.")

	doLast {
		val moduleGradleScripts = fileTree(rootDir) {
			include("*/build.gradle.kts")
			exclude("app/build.gradle.kts")
		}

		val violations = mutableListOf<String>()

		moduleGradleScripts.files
			.sortedBy { it.path }
			.forEach { file ->
				val content = file.readText()
				val hasKmpPlugin = content.contains("id(\"com.android.kotlin.multiplatform.library\")")
				val hasComposeCompiler = content.contains("alias(libs.plugins.compose.compiler)")
				val hasComposeMultiplatform = content.contains("alias(libs.plugins.compose.multiplatform)")

				if (hasKmpPlugin && hasComposeCompiler && !hasComposeMultiplatform) {
					violations += file.relativeTo(rootDir).invariantSeparatorsPath
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Compose Multiplatform plugin alias is required for KMP modules using compose compiler:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkNoLegacyAndroidStringResourcesInComposeResourceModules") {
	group = "verification"
	description = "Fails when composeResources-migrated Android modules still use R.string or androidx stringResource."
	notCompatibleWithConfigurationCache("Scans Android source sets directly.")

	doLast {
		val composeResourceModules = listOf(
			"about",
			"login",
			"summary",
			"enrollmentproof",
			"record",
			"evaluations"
		)
		val forbiddenPatterns = listOf(
			Regex("""\bR\.string\."""),
			Regex("""\bandroidx\.compose\.ui\.res\.stringResource\b""")
		)
		val violations = mutableListOf<String>()

		composeResourceModules.forEach { moduleName ->
			val androidMainDir = file("$rootDir/$moduleName/src/androidMain")
			if (!androidMainDir.exists())
				return@forEach

			fileTree(androidMainDir) {
				include("**/*.kt")
			}.files
				.sortedBy { it.path }
				.forEach { file ->
					val lines = file.readLines()

					lines.forEachIndexed { index, line ->
						if (forbiddenPatterns.any { pattern -> pattern.containsMatchIn(line) }) {
							violations += "${file.relativeTo(rootDir).invariantSeparatorsPath}:${index + 1}: ${line.trim()}"
						}
					}
				}
		}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Legacy Android string resource APIs are not allowed in composeResources-migrated modules.")
					appendLine("Use org.jetbrains.compose.resources.stringResource with Res.string instead.")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkNoDestructiveRoomFallback") {
	group = "verification"
	description = "Fails when Room destructive fallback is used in production sources."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val kotlinSources = fileTree(rootDir) {
			include("**/src/**/*.kt")
		}

		val forbiddenPattern = Regex(
			"""\bfallbackToDestructiveMigration(?:OnDowngrade|From)?\s*\("""
		)
		val violations = mutableListOf<String>()

		kotlinSources.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					if (forbiddenPattern.containsMatchIn(line)) {
						violations += "${file.relativeTo(rootDir)}:${index + 1}: $line"
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Destructive Room fallback usage detected:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkNavigationBuildersInCommonMain") {
	group = "verification"
	description = "Fails when NavGraphBuilder navigation builders are defined in androidMain source sets."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val androidMainSources = fileTree(rootDir) {
			include("**/src/androidMain/kotlin/**/*.kt")
		}

		val forbiddenPattern = Regex("""^\s*fun\s+NavGraphBuilder\.[A-Za-z0-9_]+Navigation\s*\(""")
		val violations = mutableListOf<String>()

		androidMainSources.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					if (forbiddenPattern.containsMatchIn(line)) {
						violations += "${file.relativeTo(rootDir)}:${index + 1}: $line"
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Navigation builders must live in commonMain:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkNoKoinViewModelInCommonMain") {
	group = "verification"
	description = "Fails when commonMain uses koinViewModel/koinNavViewModel APIs."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val commonMainSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/*.kt")
		}

		val forbiddenPatterns = listOf(
			Regex("""\bkoinViewModel\s*<"""),
			Regex("""\bkoinNavViewModel\s*<""")
		)
		val violations = mutableListOf<String>()

		commonMainSources.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					if (forbiddenPatterns.any { pattern -> pattern.containsMatchIn(line) }) {
						violations += "${file.relativeTo(rootDir)}:${index + 1}: $line"
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("koinViewModel/koinNavViewModel are not allowed in commonMain:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkMaincoreHostFeatureEncapsulation") {
	group = "verification"
	description = "Fails when maincore hosts wire low-level feature UI components directly instead of feature content wrappers."
	notCompatibleWithConfigurationCache("Scans specific host source files directly.")

	doLast {
		val hostFiles = listOf(
			file("maincore/src/androidMain/kotlin/com/gdavidpb/tuindice/ui/screen/TuIndiceScreen.kt"),
			file("maincore/src/iosMain/kotlin/com/gdavidpb/tuindice/ui/TuIndiceRootControllerFactory.kt")
		)
		val forbiddenSymbols = listOf(
			"SignInScreen(",
			"SignInIdleView(",
			"SignInLoggingInView(",
			"SignOutDialog(",
			"UpdatePasswordDialog(",
			"SummaryScreen(",
			"SummaryContentView(",
			"SummaryLoadingView(",
			"SummaryFailedView(",
			"ProfilePictureView(",
			"ProfilePictureSettingsDialog(",
			"RemoveProfilePictureConfirmationDialog(",
			"RecordScreen(",
			"RecordContentView(",
			"RecordLoadingView(",
			"RecordFailedView(",
			"RecordEmptyView(",
			"EvaluationsScreen(",
			"EvaluationScreen(",
			"EvaluationsContentView(",
			"EvaluationsLoadingView(",
			"EvaluationsFailedView(",
			"EvaluationsNoSubjectsView(",
			"EvaluationsEmptyView(",
			"EvaluationContentView(",
			"EvaluationLoadingView(",
			"EvaluationFailedView(",
			"GradePickerDialog(",
			"EnrollmentProofFetchDialog(",
			"EnrollmentProofFetchingSheet("
		)

		val violations = mutableListOf<String>()

		hostFiles.forEach { hostFile ->
			check(hostFile.exists()) {
				"Missing host source file: ${hostFile.absolutePath}"
			}

			val hostSource = hostFile.readText()
			violations += forbiddenSymbols
				.filter { symbol -> hostSource.contains(symbol) }
				.map { symbol -> "${hostFile.relativeTo(rootDir)} -> contains forbidden symbol '$symbol'" }
		}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Maincore host feature encapsulation violated:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkMaincoreAndroidHostNoModuleResourceImports") {
	group = "verification"
	description = "Fails when maincore Android host imports module R classes or direct Compose resource APIs."
	notCompatibleWithConfigurationCache("Scans specific host source file directly.")

	doLast {
		val hostFile = file("maincore/src/androidMain/kotlin/com/gdavidpb/tuindice/ui/screen/TuIndiceScreen.kt")

		check(hostFile.exists()) {
			"Missing host screen file: ${hostFile.absolutePath}"
		}

		val lines = hostFile.readLines()
		val forbiddenPatterns = listOf(
			Regex("""^\s*import\s+com\.gdavidpb\.tuindice\..*\.R(?:\s+as\s+\w+)?\s*$"""),
			Regex("""^\s*import\s+androidx\.compose\.ui\.res\.(stringResource|dimensionResource|painterResource|vectorResource)\s*$""")
		)

		val violations = lines.mapIndexedNotNull { index, line ->
			if (forbiddenPatterns.any { pattern -> pattern.containsMatchIn(line) }) {
				"${hostFile.relativeTo(rootDir)}:${index + 1}: $line"
			} else {
				null
			}
		}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Maincore Android host resource encapsulation violated:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkHostUiTextProviderBindings") {
	group = "verification"
	description = "Fails when HostUiTextProvider bindings are missing on Android or iOS host modules."
	notCompatibleWithConfigurationCache("Scans specific host DI source files directly.")

	doLast {
		val androidModuleFile = file("app/src/main/kotlin/com/gdavidpb/tuindice/di/AppModule.kt")
		val iosModuleFile = file("maincore/src/iosMain/kotlin/com/gdavidpb/tuindice/di/IosPlatformModule.kt")

		check(androidModuleFile.exists()) {
			"Missing Android DI module file: ${androidModuleFile.absolutePath}"
		}
		check(iosModuleFile.exists()) {
			"Missing iOS DI module file: ${iosModuleFile.absolutePath}"
		}

		val androidSource = androidModuleFile.readText()
		val iosSource = iosModuleFile.readText()

		val androidHasBinding =
			androidSource.contains("bind<HostUiTextProvider>()")
		val iosHasBinding =
			iosSource.contains("single<HostUiTextProvider>")

		if (!androidHasBinding || !iosHasBinding) {
			error(
				buildString {
					appendLine("HostUiTextProvider binding validation failed:")
					if (!androidHasBinding) {
						appendLine("- Missing Android binding in ${androidModuleFile.relativeTo(rootDir)}")
					}
					if (!iosHasBinding) {
						appendLine("- Missing iOS binding in ${iosModuleFile.relativeTo(rootDir)}")
					}
				}
			)
		}
	}
}

tasks.register("checkNoAndroidViewModelDslInKmpModules") {
	group = "verification"
	description = "Fails when KMP feature androidMain source sets use Android-only viewModelOf DSL."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val kmpModules = listOf(
			"about",
			"enrollmentproof",
			"evaluations",
			"login",
			"maincore",
			"record",
			"summary"
		)

		val androidMainSources = fileTree(rootDir) {
			kmpModules.forEach { module ->
				include("$module/src/androidMain/kotlin/**/*.kt")
			}
		}

		val forbiddenPatterns = listOf(
			Regex("""\bviewModelOf\s*\("""),
			Regex("""\bviewModel\s*\(""")
		)
		val violations = mutableListOf<String>()

		androidMainSources.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					if (forbiddenPatterns.any { pattern -> pattern.containsMatchIn(line) }) {
						violations += "${file.relativeTo(rootDir)}:${index + 1}: $line"
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("KMP feature androidMain cannot define viewModel DSL bindings:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkSharedViewModelBindings") {
	group = "verification"
	description = "Fails when required shared ViewModel bindings are missing from commonMain Koin modules."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val requiredBindings = mapOf(
			"maincore" to listOf("MainViewModel", "BrowserViewModel"),
			"login" to listOf("SignInViewModel", "SignOutViewModel", "UpdatePasswordViewModel"),
			"about" to listOf("AboutViewModel"),
			"summary" to listOf("SummaryViewModel"),
			"record" to listOf("RecordViewModel"),
			"evaluations" to listOf("EvaluationsViewModel", "EvaluationViewModel"),
			"enrollmentproof" to listOf("EnrollmentProofViewModel")
		)

		val missingBindings = mutableListOf<String>()

		requiredBindings.forEach { (module, viewModels) ->
			val moduleSources = fileTree(rootDir) {
				include("$module/src/commonMain/kotlin/**/*.kt")
			}

			val moduleText = moduleSources.files
				.sortedBy { it.path }
				.joinToString(separator = "\n") { file -> file.readText() }

			viewModels.forEach { viewModel ->
				val bindingPattern = Regex("""\bfactoryOf\s*\(\s*::\s*$viewModel\s*\)""")
				if (!bindingPattern.containsMatchIn(moduleText)) {
					missingBindings += "$module -> missing factoryOf(::$viewModel)"
				}
			}
		}

		if (missingBindings.isNotEmpty()) {
			error(
				buildString {
					appendLine("Missing shared ViewModel bindings in commonMain modules:")
					missingBindings.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkSharedPresentationCoverage") {
	group = "verification"
	description = "Fails when shared presentation ActionProcessors/ViewModels lack commonTest references in the same module."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val excludedSymbols = setOf(
			"ActionProcessor",
			"BaseViewModel"
		)

		val presentationSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/presentation/action/*ActionProcessor.kt")
			include("**/src/commonMain/kotlin/**/presentation/viewmodel/*ViewModel.kt")
		}

		val missingCoverage = mutableListOf<String>()

		presentationSources.files
			.sortedBy { it.path }
			.forEach { sourceFile ->
				val relativePath = sourceFile.relativeTo(rootDir).invariantSeparatorsPath
				val module = relativePath.substringBefore("/")
				val symbol = sourceFile.nameWithoutExtension

				if (symbol in excludedSymbols) return@forEach

				val moduleCommonTests = fileTree(rootDir) {
					include("$module/src/commonTest/kotlin/**/*.kt")
				}.files

				if (moduleCommonTests.isEmpty()) {
					missingCoverage += "$relativePath -> module has no commonTest sources."
					return@forEach
				}

				val symbolPattern = Regex("""\b$symbol\b""")
				val isReferencedInTests = moduleCommonTests.any { testFile ->
					symbolPattern.containsMatchIn(testFile.readText())
				}

				if (!isReferencedInTests) {
					missingCoverage += "$relativePath -> symbol '$symbol' not referenced in $module commonTest."
				}
			}

		if (missingCoverage.isNotEmpty()) {
			error(
				buildString {
					appendLine("Missing shared presentation coverage references:")
					missingCoverage.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkSharedUseCaseCoverage") {
	group = "verification"
	description = "Fails when shared domain UseCases in commonMain lack commonTest references in the same module."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val useCaseSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/domain/usecase/*UseCase.kt")
		}

		val missingCoverage = mutableListOf<String>()

		useCaseSources.files
			.sortedBy { it.path }
			.forEach { sourceFile ->
				val relativePath = sourceFile.relativeTo(rootDir).invariantSeparatorsPath
				val module = relativePath.substringBefore("/")
				val symbol = sourceFile.nameWithoutExtension

				val moduleCommonTests = fileTree(rootDir) {
					include("$module/src/commonTest/kotlin/**/*.kt")
				}.files

				if (moduleCommonTests.isEmpty()) {
					missingCoverage += "$relativePath -> module has no commonTest sources."
					return@forEach
				}

				val symbolPattern = Regex("""\b$symbol\b""")
				val isReferencedInTests = moduleCommonTests.any { testFile ->
					symbolPattern.containsMatchIn(testFile.readText())
				}

				if (!isReferencedInTests) {
					missingCoverage += "$relativePath -> symbol '$symbol' not referenced in $module commonTest."
				}
			}

		if (missingCoverage.isNotEmpty()) {
			error(
				buildString {
					appendLine("Missing shared domain UseCase coverage references:")
					missingCoverage.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkSharedExceptionHandlerCoverage") {
	group = "verification"
	description = "Fails when shared domain ExceptionHandlers in commonMain lack commonTest references in the same module."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val exceptionHandlerSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/domain/usecase/exceptionhandler/*ExceptionHandler.kt")
		}

		val missingCoverage = mutableListOf<String>()

		exceptionHandlerSources.files
			.sortedBy { it.path }
			.forEach { sourceFile ->
				val relativePath = sourceFile.relativeTo(rootDir).invariantSeparatorsPath
				val module = relativePath.substringBefore("/")
				val symbol = sourceFile.nameWithoutExtension

				val moduleCommonTests = fileTree(rootDir) {
					include("$module/src/commonTest/kotlin/**/*.kt")
				}.files

				if (moduleCommonTests.isEmpty()) {
					missingCoverage += "$relativePath -> module has no commonTest sources."
					return@forEach
				}

				val symbolPattern = Regex("""\b$symbol\b""")
				val isReferencedInTests = moduleCommonTests.any { testFile ->
					symbolPattern.containsMatchIn(testFile.readText())
				}

				if (!isReferencedInTests) {
					missingCoverage += "$relativePath -> symbol '$symbol' not referenced in $module commonTest."
				}
			}

		if (missingCoverage.isNotEmpty()) {
			error(
				buildString {
					appendLine("Missing shared domain ExceptionHandler coverage references:")
					missingCoverage.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkSharedValidatorCoverage") {
	group = "verification"
	description = "Fails when shared domain ParamsValidators in commonMain lack commonTest references in the same module."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val validatorSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/domain/usecase/validator/*Validator.kt")
		}

		val missingCoverage = mutableListOf<String>()

		validatorSources.files
			.sortedBy { it.path }
			.forEach { sourceFile ->
				val relativePath = sourceFile.relativeTo(rootDir).invariantSeparatorsPath
				val module = relativePath.substringBefore("/")
				val symbol = sourceFile.nameWithoutExtension

				val moduleCommonTests = fileTree(rootDir) {
					include("$module/src/commonTest/kotlin/**/*.kt")
				}.files

				if (moduleCommonTests.isEmpty()) {
					missingCoverage += "$relativePath -> module has no commonTest sources."
					return@forEach
				}

				val symbolPattern = Regex("""\b$symbol\b""")
				val isReferencedInTests = moduleCommonTests.any { testFile ->
					symbolPattern.containsMatchIn(testFile.readText())
				}

				if (!isReferencedInTests) {
					missingCoverage += "$relativePath -> symbol '$symbol' not referenced in $module commonTest."
				}
			}

		if (missingCoverage.isNotEmpty()) {
			error(
				buildString {
					appendLine("Missing shared domain ParamsValidator coverage references:")
					missingCoverage.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("reportCommonMainUiStringLiterals") {
	group = "verification"
	description = "Generates a report with hardcoded string literals found in commonMain UI/contracts/resources sources."
	notCompatibleWithConfigurationCache("Scans the workspace source tree directly.")

	doLast {
		val targetSources = fileTree(rootDir) {
			include("**/src/commonMain/kotlin/**/ui/**/*.kt")
			include("**/src/commonMain/kotlin/**/presentation/contract/**/*.kt")
			include("**/src/commonMain/kotlin/**/presentation/resource/**/*.kt")
		}

		val literalPattern = Regex(""""([^"\\]|\\.)+"""")
		val reportEntries = mutableListOf<String>()
		val candidateEntries = mutableListOf<String>()

		fun isLocalizationCandidate(
			literal: String,
			relativePath: String
		): Boolean {
			val value = literal.removePrefix("\"").removeSuffix("\"")
			val isSharedFallbackTextProvider = relativePath.contains("/presentation/resource/") &&
				relativePath.substringAfterLast('/').startsWith("Default") &&
				relativePath.endsWith("TextProvider.kt")
			val isSharedScreenTitleProvider = relativePath.contains("/presentation/resource/") &&
				(
					relativePath.endsWith("ScreenTitle.kt") ||
						relativePath.endsWith("ScreenTitles.kt")
				)

			if (value.isBlank()) return false
			if (isSharedFallbackTextProvider || isSharedScreenTitleProvider) return false
			if (value.contains("://")) return false
			if (value.contains('$')) return false
			if (value.endsWith("AnimatedContent")) return false
			if (value.endsWith("Crossfade")) return false
			if (value == "SealedCrossfade") return false
			if (value.startsWith("\\")) return false
			if (!value.any { char -> char.isLetter() }) return false
			if (value.contains('_') && !value.contains(' ')) return false
			if (value.matches(Regex("""^[A-Za-z][A-Za-z0-9]*$""")) && value.length > 20) return false

			return true
		}

		targetSources.files
			.sortedBy { it.path }
			.forEach { file ->
				val relativePath = file.relativeTo(rootDir).invariantSeparatorsPath
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					val trimmed = line.trim()

					if (trimmed.startsWith("package ") || trimmed.startsWith("import ")) {
						return@forEachIndexed
					}

					literalPattern.findAll(line).forEach { match ->
						if (match.value == "\"\"") return@forEach

						val entry = "| `$relativePath:${index + 1}` | `${match.value}` |"
						reportEntries += entry

						if (isLocalizationCandidate(match.value, relativePath)) {
							candidateEntries += entry
						}
					}
				}
			}

		val reportFile = layout.buildDirectory
			.file("reports/kmp/commonMain-ui-string-literals.md")
			.get()
			.asFile

		reportFile.parentFile.mkdirs()
		reportFile.writeText(
			buildString {
				appendLine("# commonMain UI String Literals")
				appendLine()
				appendLine("Generated entries: ${reportEntries.size}")
				appendLine("Localization candidates: ${candidateEntries.size}")
				appendLine()
				appendLine("## Localization Candidates")
				appendLine()
				appendLine("| Location | Literal |")
				appendLine("|---|---|")

				if (candidateEntries.isEmpty()) {
					appendLine("| _none_ | _none_ |")
				} else {
					candidateEntries.forEach { entry ->
						appendLine(entry)
					}
				}
				appendLine()
				appendLine("## All Literals")
				appendLine()
				appendLine("| Location | Literal |")
				appendLine("|---|---|")

				if (reportEntries.isEmpty()) {
					appendLine("| _none_ | _none_ |")
				} else {
					reportEntries.forEach { entry ->
						appendLine(entry)
					}
				}
			}
		)

		println("Generated UI string literal report at: ${reportFile.absolutePath}")
	}
}

tasks.register("checkCommonMainUiStringCandidateBudget") {
	group = "verification"
	description = "Fails when commonMain localization-candidate string literals exceed the configured budget."
	notCompatibleWithConfigurationCache("Reads generated report from reportCommonMainUiStringLiterals.")

	dependsOn("reportCommonMainUiStringLiterals")

	doLast {
		val maxCandidates = providers
			.gradleProperty("tuindice.commonMainUiStringCandidatesMax")
			.orNull
			?.toIntOrNull()
			?: 0

		val reportFile = layout.buildDirectory
			.file("reports/kmp/commonMain-ui-string-literals.md")
			.get()
			.asFile

		check(reportFile.exists()) {
			"UI literal report not found at ${reportFile.absolutePath}. Run reportCommonMainUiStringLiterals first."
		}

		val line = reportFile
			.readLines()
			.firstOrNull { reportLine -> reportLine.startsWith("Localization candidates:") }
			?: error("Could not find 'Localization candidates' line in ${reportFile.absolutePath}.")

		val candidateCount = line
			.substringAfter(':')
			.trim()
			.toIntOrNull()
			?: error("Unable to parse localization candidate count from line: $line")

		if (candidateCount > maxCandidates) {
			error(
				"commonMain localization-candidate literals budget exceeded: $candidateCount > $maxCandidates. " +
					"Review ${reportFile.absolutePath} and migrate new strings to shared resources/provider."
			)
		}
	}
}

tasks.register("checkIosNoSyntheticAllowFlags") {
	group = "verification"
	description = "Fails when iOS xcconfig files still define TUINDICE_ALLOW_* runtime synthetic flags."
	notCompatibleWithConfigurationCache("Reads iOS xcconfig files from workspace.")

	doLast {
		val configFiles = listOf(
			file("iosApp/Config/Debug.xcconfig"),
			file("iosApp/Config/Release.xcconfig")
		)

		configFiles.forEach { configFile ->
			check(configFile.exists()) {
				"Missing iOS config file: ${configFile.absolutePath}"
			}
		}

		val forbiddenPattern = Regex("""^\s*TUINDICE_ALLOW_[A-Z0-9_]+\s*=""")
		val violations = mutableListOf<String>()

		configFiles.forEach { configFile ->
			configFile.readLines().forEachIndexed { index, line ->
				if (forbiddenPattern.containsMatchIn(line)) {
					violations += "${configFile.relativeTo(rootDir)}:${index + 1}: $line"
				}
			}
		}

		check(violations.isEmpty()) {
			"Found forbidden TUINDICE_ALLOW_* runtime flags in iOS configs:\n" +
				violations.joinToString(separator = "\n")
		}
	}
}

tasks.register("checkNoLegacyDataMigrationCodepaths") {
	group = "verification"
	description = "Fails when legacy local-data migration codepaths are present in runtime sources."
	notCompatibleWithConfigurationCache("Scans selected workspace sources directly.")

	doLast {
		val sources = fileTree(rootDir) {
			include("app/src/**/*.kt")
			include("maincore/src/**/*.kt")
			include("iosApp/Sources/**/*.swift")
			include("app/build.gradle.kts")
			include("gradle/libs.versions.toml")
		}

		val forbiddenPatterns = listOf(
			Regex("""\bMigrationManager\b""") to "Legacy migration manager is not allowed.",
			Regex("""^\s*package\s+com\.gdavidpb\.tuindice\.migration\b""") to
				"Legacy migration package is not allowed.",
			Regex("""\bSharedPreferencesMigration\b""") to
				"SharedPreferences migration path is not allowed for greenfield release.",
			Regex("""\bLegacyEncryptedPreferencesMigrator\b""") to
				"Legacy encrypted-preferences migrator is not allowed.",
			Regex("""\bEncryptedSharedPreferences\b""") to
				"EncryptedSharedPreferences dependency usage is not allowed.",
			Regex("""\bMasterKey\b""") to
				"AndroidX security crypto MasterKey usage is not allowed.",
			Regex("""androidx\.security\.crypto""") to
				"AndroidX security crypto artifacts are not allowed."
		)

		val violations = mutableListOf<String>()

		sources.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					forbiddenPatterns.forEach { (pattern, reason) ->
						if (pattern.containsMatchIn(line)) {
							violations += "${file.relativeTo(rootDir)}:${index + 1}: $reason -> $line"
						}
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("Legacy local-data migration codepaths detected:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("checkIosSmokeRequiredChecksCoverage") {
	group = "verification"
	description = "Fails when iOS host smoke script weakens required runtime checks."
	notCompatibleWithConfigurationCache("Reads iOS smoke script from workspace.")

	doLast {
		val smokeScript = file("iosApp/scripts/ci-smoke-ios-host.sh")

		check(smokeScript.exists()) {
			"Missing iOS smoke script: ${smokeScript.absolutePath}"
		}

		val scriptContent = smokeScript.readText()
		val requiredChecks = listOf(
			"appenv:ok",
			"network:",
			"device:ok",
			"file-gateway:ok",
			"secure-store:ok",
			"push:ok",
			"attestation:",
			"startup:",
			"destination:ok",
			"review:",
			"update:",
			"config:ok",
			"reporting:ok",
			"browser-flow:ok",
			"signin-flow:ok",
			"about-flow:ok",
			"summary-flow:ok",
			"record-flow:ok",
			"enrollment-flow:ok",
			"evaluations-flow:ok",
			"evaluation-flow:ok",
			"feature-usecases:loading",
			"viewmodels:ok"
		)

		val missingChecks = requiredChecks.filterNot { token -> scriptContent.contains(token) }
		if (missingChecks.isNotEmpty()) {
			error(
				"iOS smoke required checks were weakened. Missing tokens in " +
					"${smokeScript.relativeTo(rootDir)}: ${missingChecks.joinToString()}"
			)
		}

		check(scriptContent.contains("TUINDICE_IOS_SMOKE_RUN_ID")) {
			"iOS smoke script must keep run-id marker isolation (TUINDICE_IOS_SMOKE_RUN_ID)."
		}
		check(scriptContent.contains("TUINDICE_SMOKE_USBID")) {
			"iOS smoke script must support optional authenticated smoke via TUINDICE_SMOKE_USBID."
		}
		check(scriptContent.contains("TUINDICE_SMOKE_PASSWORD")) {
			"iOS smoke script must support optional authenticated smoke via TUINDICE_SMOKE_PASSWORD."
		}
		check(scriptContent.contains("signin-auth:ok")) {
			"iOS smoke script must support authenticated smoke checks (signin-auth:ok)."
		}
		check(scriptContent.contains("signout-auth:ok")) {
			"iOS smoke script must support authenticated smoke checks (signout-auth:ok)."
		}
		check(scriptContent.contains("\$SMOKE_MARKER_PREFIX:\$SMOKE_RUN_ID:PASS:")) {
			"iOS smoke script must validate PASS marker with run-id isolation."
		}
		check(scriptContent.contains("\$SMOKE_MARKER_PREFIX:\$SMOKE_RUN_ID:FAIL:")) {
			"iOS smoke script must validate FAIL marker with run-id isolation."
		}
	}
}

tasks.register("checkIosSmokeVerifierChecksCoverage") {
	group = "verification"
	description = "Fails when iOS smoke verifier tokens drift from required checks declared in smoke script."
	notCompatibleWithConfigurationCache("Reads iOS smoke script and smoke verifier source from workspace.")

	doLast {
		val smokeScript = file("iosApp/scripts/ci-smoke-ios-host.sh")
		val smokeVerifier = file("maincore/src/iosSimulatorMain/kotlin/com/gdavidpb/tuindice/ui/TuIndiceIosSmokeVerifier.kt")

		check(smokeScript.exists()) {
			"Missing iOS smoke script: ${smokeScript.absolutePath}"
		}
		check(smokeVerifier.exists()) {
			"Missing iOS smoke verifier source: ${smokeVerifier.absolutePath}"
		}

		val scriptContent = smokeScript.readText()
		val verifierContent = smokeVerifier.readText()

		val checksPrefix = "SMOKE_REQUIRED_CHECKS=\"\${SMOKE_REQUIRED_CHECKS:-"
		val declaredChecks = scriptContent
			.substringAfter(checksPrefix, missingDelimiterValue = "")
			.substringBefore("}\"", missingDelimiterValue = "")

		check(declaredChecks.isNotBlank()) {
			"Could not parse default SMOKE_REQUIRED_CHECKS declaration from ${smokeScript.relativeTo(rootDir)}."
		}

		val requiredTokens = declaredChecks
			.split(",")
			.map { token -> token.trim() }
			.filter { token -> token.isNotEmpty() }

		check(requiredTokens.contains("attestation:APP_ATTEST")) {
			"iOS smoke script must require attestation:APP_ATTEST in SMOKE_REQUIRED_CHECKS " +
				"(${smokeScript.relativeTo(rootDir)})."
		}
		check(requiredTokens.contains("attestation-key-id:ok")) {
			"iOS smoke script must require attestation-key-id:ok in SMOKE_REQUIRED_CHECKS " +
				"(${smokeScript.relativeTo(rootDir)})."
		}
		check(!requiredTokens.contains("attestation:")) {
			"iOS smoke script must not use generic attestation: token in SMOKE_REQUIRED_CHECKS; " +
				"use attestation:APP_ATTEST (${smokeScript.relativeTo(rootDir)})."
		}

		val missingTokens = requiredTokens.filterNot { token ->
			verifierContent.contains(token)
		}

		if (missingTokens.isNotEmpty()) {
			error(
				"iOS smoke verifier is missing required check tokens from script: " +
					missingTokens.joinToString()
			)
		}
	}
}

tasks.register("checkIosDeviceE2ERequiredChecksCoverage") {
	group = "verification"
	description = "Fails when iOS host device E2E script weakens required production runtime checks."
	notCompatibleWithConfigurationCache("Reads iOS device E2E script and smoke verifier from workspace.")

	doLast {
		val e2eScript = file("iosApp/scripts/ci-e2e-ios-host-device.sh")
		val smokeVerifier = file("maincore/src/iosArm64Main/kotlin/com/gdavidpb/tuindice/ui/TuIndiceIosSmokeVerifier.kt")

		check(e2eScript.exists()) {
			"Missing iOS device E2E script: ${e2eScript.absolutePath}"
		}
		check(smokeVerifier.exists()) {
			"Missing iOS smoke verifier source: ${smokeVerifier.absolutePath}"
		}

		val scriptContent = e2eScript.readText()
		val verifierContent = smokeVerifier.readText()

		val checksPrefix = "SMOKE_REQUIRED_CHECKS=\"\${SMOKE_REQUIRED_CHECKS:-"
		val declaredChecks = scriptContent
			.substringAfter(checksPrefix, missingDelimiterValue = "")
			.substringBefore("}\"", missingDelimiterValue = "")

		check(declaredChecks.isNotBlank()) {
			"Could not parse default SMOKE_REQUIRED_CHECKS declaration from ${e2eScript.relativeTo(rootDir)}."
		}

		val requiredChecks = listOf(
			"smoke-runtime:disabled-on-device"
		)

		val missingChecks = requiredChecks.filterNot { token -> declaredChecks.contains(token) }
		if (missingChecks.isNotEmpty()) {
			error(
				"iOS device E2E required checks were weakened. Missing tokens in " +
					"${e2eScript.relativeTo(rootDir)}: ${missingChecks.joinToString()}"
			)
		}

		check(scriptContent.contains("TUINDICE_IOS_SMOKE_RUN_ID")) {
			"iOS device E2E script must keep run-id marker isolation (TUINDICE_IOS_SMOKE_RUN_ID)."
		}
		check(scriptContent.contains("\$SMOKE_MARKER_PREFIX:\$SMOKE_RUN_ID:PASS:")) {
			"iOS device E2E script must validate PASS marker with run-id isolation."
		}
		check(scriptContent.contains("\$SMOKE_MARKER_PREFIX:\$SMOKE_RUN_ID:FAIL:")) {
			"iOS device E2E script must validate FAIL marker with run-id isolation."
		}

		val missingVerifierTokens = requiredChecks.filterNot { token ->
			verifierContent.contains(token)
		}
		if (missingVerifierTokens.isNotEmpty()) {
			error(
				"iOS smoke verifier is missing required tokens from device E2E script: " +
					missingVerifierTokens.joinToString()
			)
		}
	}
}

tasks.register("checkAttestationContractConsistency") {
	group = "verification"
	description = "Fails when attestation contract doc drifts from shared enum/headers."
	notCompatibleWithConfigurationCache("Reads contract and source files from workspace.")

	doLast {
		val contractFile = file("KMP_BACKEND_ATTESTATION_CONTRACT.md")
		val providerEnumFile = file(
			"base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/domain/model/AttestationProvider.kt"
		)
		val authRepositoryFile = file(
			"login/src/commonMain/kotlin/com/gdavidpb/tuindice/login/data/repository/KtorAuthApiApiDataRepository.kt"
		)

		check(contractFile.exists()) {
			"Missing attestation contract doc: ${contractFile.absolutePath}"
		}
		check(providerEnumFile.exists()) {
			"Missing attestation provider enum: ${providerEnumFile.absolutePath}"
		}
		check(authRepositoryFile.exists()) {
			"Missing auth repository source: ${authRepositoryFile.absolutePath}"
		}

		val contractText = contractFile.readText()
		val providerEnumText = providerEnumFile.readText()
		val authRepositoryText = authRepositoryFile.readText()

		check(!contractText.contains("APPLE_DEVICE_CHECK")) {
			"Legacy APPLE_DEVICE_CHECK reference detected in ${contractFile.relativeTo(rootDir)}."
		}

		val enumBody = providerEnumText
			.substringAfter("enum class AttestationProvider")
			.substringAfter("{")
			.substringBefore("}")

		val providers = enumBody
			.lines()
			.map { line ->
				line.substringBefore("//")
					.trim()
					.trimEnd(',')
			}
			.filter { token -> token.matches(Regex("[A-Z_]+")) }

		check(providers.isNotEmpty()) {
			"Could not parse providers from ${providerEnumFile.relativeTo(rootDir)}."
		}

		val missingProvidersInContract = providers.filterNot { provider ->
			contractText.contains("`$provider`") || contractText.contains(provider)
		}

		val requiredHeaderTokens = listOf(
			"Attestation-Id",
			"Attestation",
			"Attestation-Provider",
			"Attestation-Key-Id",
			"X-Forwarded-Authorization"
		)
		val requiredEndpointTokens = listOf(
			"auth/token",
			"auth/token/refresh"
		)
		val requiredErrorCodes = listOf(
			"ATTESTATION_MISSING",
			"ATTESTATION_INVALID",
			"ATTESTATION_EXPIRED",
			"ATTESTATION_REPLAY",
			"ATTESTATION_PROVIDER_UNSUPPORTED"
		)
		val requiredTelemetryMetrics = listOf(
			"attestation_requests_total{provider}",
			"attestation_reject_total{provider,code}",
			"attestation_accept_total{provider}"
		)
		val requiredAppAttestKeyIdEnforcementTokens = listOf(
			"attestation.provider == AttestationProvider.APP_ATTEST",
			"require(!keyId.isNullOrBlank())",
			"AuthHeaders.ATTESTATION_KEY_ID"
		)

		val missingHeadersInContract = requiredHeaderTokens.filterNot { token ->
			contractText.contains("`$token`") || contractText.contains(token)
		}
		val missingHeadersInShared = requiredHeaderTokens.filterNot { token ->
			authRepositoryText.contains("\"$token\"")
		}
		val missingEndpointsInContract = requiredEndpointTokens.filterNot { token ->
			contractText.contains("`$token`") || contractText.contains(token)
		}
		val missingEndpointsInShared = requiredEndpointTokens.filterNot { token ->
			authRepositoryText.contains("\"$token\"")
		}
		val missingErrorCodesInContract = requiredErrorCodes.filterNot { token ->
			contractText.contains("`$token`") || contractText.contains(token)
		}
		val missingTelemetryMetricsInContract = requiredTelemetryMetrics.filterNot { token ->
			contractText.contains("`$token`") || contractText.contains(token)
		}
		val missingAppAttestKeyIdEnforcementInShared = requiredAppAttestKeyIdEnforcementTokens.filterNot { token ->
			authRepositoryText.contains(token)
		}

		if (missingProvidersInContract.isNotEmpty() ||
			missingHeadersInContract.isNotEmpty() ||
			missingHeadersInShared.isNotEmpty() ||
			missingEndpointsInContract.isNotEmpty() ||
			missingEndpointsInShared.isNotEmpty() ||
			missingErrorCodesInContract.isNotEmpty() ||
			missingTelemetryMetricsInContract.isNotEmpty() ||
			missingAppAttestKeyIdEnforcementInShared.isNotEmpty()
		) {
			error(
				buildString {
					appendLine("Attestation contract drift detected:")
					if (missingProvidersInContract.isNotEmpty()) {
						appendLine(
							"- Missing providers in contract ${contractFile.relativeTo(rootDir)}: " +
								missingProvidersInContract.joinToString()
						)
					}
					if (missingHeadersInContract.isNotEmpty()) {
						appendLine(
							"- Missing headers in contract ${contractFile.relativeTo(rootDir)}: " +
								missingHeadersInContract.joinToString()
						)
					}
					if (missingHeadersInShared.isNotEmpty()) {
						appendLine(
							"- Missing header literals in shared auth source " +
								"${authRepositoryFile.relativeTo(rootDir)}: " +
								missingHeadersInShared.joinToString()
						)
					}
					if (missingEndpointsInContract.isNotEmpty()) {
						appendLine(
							"- Missing endpoint tokens in contract ${contractFile.relativeTo(rootDir)}: " +
								missingEndpointsInContract.joinToString()
						)
					}
					if (missingEndpointsInShared.isNotEmpty()) {
						appendLine(
							"- Missing endpoint literals in shared auth source " +
								"${authRepositoryFile.relativeTo(rootDir)}: " +
								missingEndpointsInShared.joinToString()
						)
					}
					if (missingErrorCodesInContract.isNotEmpty()) {
						appendLine(
							"- Missing recommended error codes in contract " +
								"${contractFile.relativeTo(rootDir)}: " +
								missingErrorCodesInContract.joinToString()
						)
					}
					if (missingTelemetryMetricsInContract.isNotEmpty()) {
						appendLine(
							"- Missing telemetry metrics in contract " +
								"${contractFile.relativeTo(rootDir)}: " +
								missingTelemetryMetricsInContract.joinToString()
						)
					}
					if (missingAppAttestKeyIdEnforcementInShared.isNotEmpty()) {
						appendLine(
							"- Missing APP_ATTEST keyId enforcement tokens in shared auth source " +
								"${authRepositoryFile.relativeTo(rootDir)}: " +
								missingAppAttestKeyIdEnforcementInShared.joinToString()
						)
					}
				}
			)
		}
	}
}

tasks.register("checkIosStructuredUserAgent") {
	group = "verification"
	description = "Fails when iOS shared client user-agent drifts from structured backend contract."
	notCompatibleWithConfigurationCache("Reads iOS platform module source from workspace.")

	doLast {
		val iosPlatformModule = file(
			"maincore/src/iosMain/kotlin/com/gdavidpb/tuindice/di/IosPlatformModule.kt"
		)
		val userAgentContract = file(
			"maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/di/UserAgentContract.kt"
		)
		val iosSmokeScript = file("iosApp/scripts/ci-smoke-ios-host.sh")

		check(iosPlatformModule.exists()) {
			"Missing iOS platform module: ${iosPlatformModule.absolutePath}"
		}
		check(userAgentContract.exists()) {
			"Missing shared user-agent contract: ${userAgentContract.absolutePath}"
		}
		check(iosSmokeScript.exists()) {
			"Missing iOS smoke script: ${iosSmokeScript.absolutePath}"
		}

		val content = iosPlatformModule.readText()
		val userAgentContractContent = userAgentContract.readText()
		val smokeScriptContent = iosSmokeScript.readText()

		check(content.contains("userAgentValue = createIosUserAgent(")) {
			"iOS shared client must use createIosUserAgent(...) in ${iosPlatformModule.relativeTo(rootDir)}."
		}
		check(content.contains("internal fun createIosUserAgent")) {
			"Missing createIosUserAgent function in ${iosPlatformModule.relativeTo(rootDir)}."
		}
		check(!content.contains("userAgentValue = \"TuIndice-iOS\"")) {
			"Legacy flat iOS user-agent detected in ${iosPlatformModule.relativeTo(rootDir)}."
		}

		val requiredSharedContractTokens = listOf(
			"\"TuIndice\"",
			"joinToString(\";\")"
		)
		val requiredIosPlatformTokens = listOf(
			"\"iOS\"",
			"\"Apple\""
		)

		val missingSharedTokens = requiredSharedContractTokens.filterNot(userAgentContractContent::contains)
		if (missingSharedTokens.isNotEmpty()) {
			error(
				"Shared structured user-agent contract is missing required tokens in " +
					"${userAgentContract.relativeTo(rootDir)}: ${missingSharedTokens.joinToString()}"
			)
		}

		val missingIosTokens = requiredIosPlatformTokens.filterNot(content::contains)
		if (missingIosTokens.isNotEmpty()) {
			error(
				"iOS structured user-agent mapping is missing required tokens in " +
					"${iosPlatformModule.relativeTo(rootDir)}: ${missingIosTokens.joinToString()}"
			)
		}

		check(smokeScriptContent.contains("user-agent:ok")) {
			"iOS smoke script must require user-agent:ok in SMOKE_REQUIRED_CHECKS " +
				"(${iosSmokeScript.relativeTo(rootDir)})."
		}
	}
}

tasks.register("checkIosAppAttestOnlySurface") {
	group = "verification"
	description = "Fails when iOS attestation surface regresses away from App Attest-only flow."
	notCompatibleWithConfigurationCache("Reads iOS attestation bridge and shared sources from workspace.")

	doLast {
		val providerEnumFile = file(
			"base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/domain/model/AttestationProvider.kt"
		)
		val iosBridgeFile = file("iosApp/Sources/TuIndiceHost/TuIndicePlatformBridge.swift")
		val iosPlatformModule = file(
			"maincore/src/iosMain/kotlin/com/gdavidpb/tuindice/di/IosPlatformModule.kt"
		)
		val smokeVerifier = file(
			"maincore/src/iosSimulatorMain/kotlin/com/gdavidpb/tuindice/ui/TuIndiceIosSmokeVerifier.kt"
		)

		check(providerEnumFile.exists()) {
			"Missing attestation provider enum: ${providerEnumFile.absolutePath}"
		}
		check(iosBridgeFile.exists()) {
			"Missing iOS platform bridge: ${iosBridgeFile.absolutePath}"
		}
		check(iosPlatformModule.exists()) {
			"Missing iOS platform module: ${iosPlatformModule.absolutePath}"
		}
		check(smokeVerifier.exists()) {
			"Missing iOS smoke verifier: ${smokeVerifier.absolutePath}"
		}

		val enumText = providerEnumFile.readText()
		val bridgeText = iosBridgeFile.readText()
		val platformModuleText = iosPlatformModule.readText()
		val smokeVerifierText = smokeVerifier.readText()

		check(!enumText.contains("APPLE_DEVICE_CHECK")) {
			"Legacy APPLE_DEVICE_CHECK provider found in ${providerEnumFile.relativeTo(rootDir)}."
		}
		check(bridgeText.contains("DCAppAttestService.shared")) {
			"iOS bridge must use DCAppAttestService.shared in ${iosBridgeFile.relativeTo(rootDir)}."
		}
		check(platformModuleText.contains("providerAttestation.provider == AttestationProvider.APP_ATTEST")) {
			"iOS shared attestation repository must enforce APP_ATTEST provider in " +
				"${iosPlatformModule.relativeTo(rootDir)}."
		}
		check(smokeVerifierText.contains("attestation:APP_ATTEST")) {
			"iOS smoke verifier must report attestation:APP_ATTEST in " +
				"${smokeVerifier.relativeTo(rootDir)}."
		}
		check(smokeVerifierText.contains("attestation-key-id:ok")) {
			"iOS smoke verifier must report attestation-key-id:ok in " +
				"${smokeVerifier.relativeTo(rootDir)}."
		}

		val forbiddenBridgeTokens = listOf(
			"BaseAttestationProvider.appleDeviceCheck",
			"BaseAttestationProvider.playIntegrity",
			"DCDevice.current",
			"generateToken("
		)
		val presentForbiddenTokens = forbiddenBridgeTokens.filter(bridgeText::contains)
		if (presentForbiddenTokens.isNotEmpty()) {
			error(
				"iOS bridge contains forbidden legacy attestation tokens in " +
					"${iosBridgeFile.relativeTo(rootDir)}: ${presentForbiddenTokens.joinToString()}"
			)
		}
	}
}

tasks.register("checkIosDefaultBridgeNoSecureStoreFallback") {
	group = "verification"
	description = "Fails when DefaultIosPlatformBridge reintroduces insecure secure-store fallback."
	notCompatibleWithConfigurationCache("Reads iOS platform module source from workspace.")

	doLast {
		val iosPlatformModule = file(
			"maincore/src/iosMain/kotlin/com/gdavidpb/tuindice/di/IosPlatformModule.kt"
		)

		check(iosPlatformModule.exists()) {
			"Missing iOS platform module: ${iosPlatformModule.absolutePath}"
		}

		val content = iosPlatformModule.readText()

		check(content.contains("failMissingBridge(api = \"secureStoreContains\")")) {
			"DefaultIosPlatformBridge must fail explicitly for secureStoreContains in " +
				"${iosPlatformModule.relativeTo(rootDir)}."
		}
		check(content.contains("failMissingBridge(api = \"secureStoreGetString\")")) {
			"DefaultIosPlatformBridge must fail explicitly for secureStoreGetString in " +
				"${iosPlatformModule.relativeTo(rootDir)}."
		}
		check(content.contains("failMissingBridge(api = \"secureStorePutString\")")) {
			"DefaultIosPlatformBridge must fail explicitly for secureStorePutString in " +
				"${iosPlatformModule.relativeTo(rootDir)}."
		}
		check(content.contains("failMissingBridge(api = \"secureStoreClear\")")) {
			"DefaultIosPlatformBridge must fail explicitly for secureStoreClear in " +
				"${iosPlatformModule.relativeTo(rootDir)}."
		}

		val forbiddenTokens = listOf(
			"secureStoreDefaults",
			"NSUserDefaults.standardUserDefaults",
			"dictionaryRepresentation().keys"
		)
		val presentForbiddenTokens = forbiddenTokens.filter(content::contains)
		if (presentForbiddenTokens.isNotEmpty()) {
			error(
				"DefaultIosPlatformBridge contains forbidden secure-store fallback tokens in " +
					"${iosPlatformModule.relativeTo(rootDir)}: ${presentForbiddenTokens.joinToString()}"
			)
		}
	}
}

tasks.register("checkIosBridgeNoPlaceholders") {
	group = "verification"
	description = "Fails when iOS host bridge files contain placeholder or crash-only markers."
	notCompatibleWithConfigurationCache("Scans selected iOS bridge sources directly.")

	doLast {
		val sources = fileTree(rootDir) {
			include("iosApp/Sources/TuIndiceHost/*.swift")
			include("maincore/src/iosMain/kotlin/com/gdavidpb/tuindice/di/*.kt")
		}

		val forbiddenPatterns = listOf(
			Regex("""\bTODO\b""") to "TODO markers are not allowed in iOS bridge sources.",
			Regex("""\bFIXME\b""") to "FIXME markers are not allowed in iOS bridge sources.",
			Regex("""\bfatalError\s*\(""") to "fatalError is not allowed in iOS bridge sources.",
			Regex("""not implemented""", RegexOption.IGNORE_CASE) to
				"'not implemented' placeholders are not allowed in iOS bridge sources.",
			Regex("""\bstub\b""", RegexOption.IGNORE_CASE) to
				"'stub' placeholders are not allowed in iOS bridge sources."
		)

		val violations = mutableListOf<String>()

		sources.files
			.sortedBy { it.path }
			.forEach { file ->
				val lines = file.readLines()

				lines.forEachIndexed { index, line ->
					forbiddenPatterns.forEach { (pattern, reason) ->
						if (pattern.containsMatchIn(line)) {
							violations += "${file.relativeTo(rootDir)}:${index + 1}: $reason -> $line"
						}
					}
				}
			}

		if (violations.isNotEmpty()) {
			error(
				buildString {
					appendLine("iOS bridge placeholder markers detected:")
					violations.forEach { appendLine(it) }
				}
			)
		}
	}
}

tasks.register("verifyKmpTargets") {
	group = "verification"
	description = "Compiles KMP shared modules for Android and iOS simulator targets."

	dependsOn(
		":about:compileAndroidMain",
		":about:compileKotlinIosSimulatorArm64",
		":base:compileAndroidMain",
		":base:compileKotlinIosSimulatorArm64",
		":enrollmentproof:compileAndroidMain",
		":enrollmentproof:compileKotlinIosSimulatorArm64",
		":evaluations:compileAndroidMain",
		":evaluations:compileKotlinIosSimulatorArm64",
		":login:compileAndroidMain",
		":login:compileKotlinIosSimulatorArm64",
		":maincore:compileAndroidMain",
		":maincore:compileKotlinIosSimulatorArm64",
		":persistence:compileAndroidMain",
		":persistence:compileKotlinIosSimulatorArm64",
		":record:compileAndroidMain",
		":record:compileKotlinIosSimulatorArm64",
		":summary:compileAndroidMain",
		":summary:compileKotlinIosSimulatorArm64"
	)
}

tasks.register("verifyKmpSharedTests") {
	group = "verification"
	description = "Runs shared KMP tests on iOS x64 where available in CI/dev machines."

	dependsOn(
		":about:iosX64Test",
		":base:iosX64Test",
		":enrollmentproof:iosX64Test",
		":evaluations:iosX64Test",
		":login:iosX64Test",
		":record:iosX64Test",
		":summary:iosX64Test",
		":maincore:iosX64Test"
	)
}

tasks.register<Exec>("verifyIosHostTypecheck") {
	group = "verification"
	description = "Type-checks iOS host Swift sources against linked maincore.framework."

	val isMacHost = System.getProperty("os.name")
		.contains("Mac", ignoreCase = true)

	if (isMacHost) {
		dependsOn(":maincore:linkDebugFrameworkIosSimulatorArm64")
	}

	onlyIf {
		isMacHost
	}

	environment("SKIP_FRAMEWORK_BUILD", "1")
	commandLine("bash", "${rootDir}/iosApp/scripts/ci-typecheck-ios-host.sh")
}

tasks.register<Exec>("verifyIosHostBuildDebug") {
	group = "verification"
	description = "Builds iOS host app in Debug configuration (simulator)."

	val isMacHost = System.getProperty("os.name")
		.contains("Mac", ignoreCase = true)
	val runHostBuild = System.getenv("TUINDICE_IOS_HOST_BUILD") == "1"

	if (isMacHost && runHostBuild) {
		dependsOn(":maincore:linkDebugFrameworkIosSimulatorArm64")
	}

	onlyIf {
		isMacHost && runHostBuild
	}

	environment("CONFIGURATION", "Debug")
	environment("DERIVED_DATA_PATH", "${rootDir}/iosApp/.build/ios-host-debug")
	if (System.getenv("TUINDICE_IOS_HOST_SMOKE_REQUIRE_SIMULATOR") == "1") {
		environment("REQUIRE_SIMULATOR", "1")
	}
	commandLine("bash", "${rootDir}/iosApp/scripts/ci-build-ios-host.sh")
}

tasks.register<Exec>("verifyIosHostBuildRelease") {
	group = "verification"
	description = "Builds iOS host app in Release configuration (simulator)."

	val isMacHost = System.getProperty("os.name")
		.contains("Mac", ignoreCase = true)
	val runHostBuild = System.getenv("TUINDICE_IOS_HOST_BUILD") == "1"

	if (isMacHost && runHostBuild) {
		dependsOn(":maincore:linkReleaseFrameworkIosSimulatorArm64")
	}

	onlyIf {
		isMacHost && runHostBuild
	}

	environment("CONFIGURATION", "Release")
	environment("DERIVED_DATA_PATH", "${rootDir}/iosApp/.build/ios-host-release")
	if (System.getenv("TUINDICE_IOS_HOST_SMOKE_REQUIRE_SIMULATOR") == "1") {
		environment("REQUIRE_SIMULATOR", "1")
	}
	commandLine("bash", "${rootDir}/iosApp/scripts/ci-build-ios-host.sh")
}

tasks.register<Exec>("verifyIosHostLaunchSmoke") {
	group = "verification"
	description = "Installs and launches iOS host app on simulator (Debug smoke + functional marker validation)."

	val isMacHost = System.getProperty("os.name")
		.contains("Mac", ignoreCase = true)
	val runHostSmoke = System.getenv("TUINDICE_IOS_HOST_SMOKE") == "1"
	val runHostBuild = System.getenv("TUINDICE_IOS_HOST_BUILD") == "1"

	if (isMacHost && runHostSmoke && runHostBuild) {
		dependsOn("verifyIosHostBuildDebug")
	}

	onlyIf {
		isMacHost && runHostSmoke
	}

	environment("CONFIGURATION", "Debug")
	environment("DERIVED_DATA_PATH", "${rootDir}/iosApp/.build/ios-host-debug")
	if (runHostBuild) {
		environment("SKIP_HOST_BUILD", "1")
	}
	if (System.getenv("TUINDICE_IOS_HOST_SMOKE_REQUIRE_SIMULATOR") == "1") {
		environment("REQUIRE_SIMULATOR", "1")
	}
	commandLine("bash", "${rootDir}/iosApp/scripts/ci-smoke-ios-host.sh")
}

tasks.register<Exec>("verifyIosHostBuildDeviceRelease") {
	group = "verification"
	description = "Builds iOS host app in Release configuration (physical device)."

	val isMacHost = System.getProperty("os.name")
		.contains("Mac", ignoreCase = true)
	val runHostBuild = System.getenv("TUINDICE_IOS_HOST_BUILD") == "1"
	val runHostDeviceE2E = System.getenv("TUINDICE_IOS_HOST_E2E") == "1"

	if (isMacHost && runHostBuild && runHostDeviceE2E) {
		dependsOn(":maincore:linkReleaseFrameworkIosArm64")
	}

	onlyIf {
		isMacHost && runHostBuild && runHostDeviceE2E
	}

	environment("CONFIGURATION", "Release")
	environment("IOS_PLATFORM", "device")
	environment("DERIVED_DATA_PATH", "${rootDir}/iosApp/.build/ios-host-device-release")
	environment(
		"CODE_SIGNING_ALLOWED",
		System.getenv("TUINDICE_IOS_HOST_DEVICE_CODE_SIGNING_ALLOWED") ?: "YES"
	)
	commandLine("bash", "${rootDir}/iosApp/scripts/ci-build-ios-host.sh")
}

tasks.register<Exec>("verifyIosHostDeviceE2E") {
	group = "verification"
	description = "Installs and launches iOS host app on a physical device (Release E2E smoke)."

	val isMacHost = System.getProperty("os.name")
		.contains("Mac", ignoreCase = true)
	val runHostDeviceE2E = System.getenv("TUINDICE_IOS_HOST_E2E") == "1"
	val runHostBuild = System.getenv("TUINDICE_IOS_HOST_BUILD") == "1"

	if (isMacHost && runHostDeviceE2E && runHostBuild) {
		dependsOn("verifyIosHostBuildDeviceRelease")
	}

	onlyIf {
		isMacHost && runHostDeviceE2E
	}

	environment("CONFIGURATION", "Release")
	environment("DERIVED_DATA_PATH", "${rootDir}/iosApp/.build/ios-host-device-release")
	environment("IOS_DEVICE_IDENTIFIER", System.getenv("TUINDICE_IOS_DEVICE_IDENTIFIER") ?: "")
	if (runHostBuild) {
		environment("SKIP_HOST_BUILD", "1")
	}
	if (System.getenv("TUINDICE_IOS_HOST_E2E_REQUIRE_DEVICE") == "1") {
		environment("REQUIRE_DEVICE", "1")
	}
	System.getenv("TUINDICE_IOS_HOST_E2E_TIMEOUT_SECONDS")
		?.takeIf { it.isNotBlank() }
		?.let { timeoutSeconds ->
			environment("E2E_LAUNCH_TIMEOUT_SECONDS", timeoutSeconds)
		}

	commandLine("bash", "${rootDir}/iosApp/scripts/ci-e2e-ios-host-device.sh")
}

tasks.register("verifyIosHostSmoke") {
	group = "verification"
	description = "Runs iOS host smoke builds and optional launch/device E2E lanes."

	dependsOn(
		"verifyIosHostBuildDebug",
		"verifyIosHostBuildRelease",
		"verifyIosHostLaunchSmoke",
		"verifyIosHostDeviceE2E"
	)
}

tasks.register<Exec>("reportWorktreeHealth") {
	group = "help"
	description = "Generates a worktree health report with suggested atomic commit batches."
	commandLine("bash", "${rootDir}/scripts/kmp/report-worktree-health.sh")
}

tasks.register("verifyKmpMigration") {
	group = "verification"
	description = "Runs migration guardrails and multiplatform compilation checks."

	dependsOn(
		"checkCommonMainPlatformLeaks",
		"checkAllowedExpectUsageInCommonMain",
		"checkNoBuildConfigInCommonMain",
		"checkNoJvmStreamsInCommonMain",
		"checkNoHardcodedTopBarTitlesInCommonMain",
		"checkNoHardcodedComposeMultiplatformPluginVersion",
		"checkKmpComposePluginCoverage",
		"checkNoLegacyAndroidStringResourcesInComposeResourceModules",
		"checkNoDestructiveRoomFallback",
		"checkNavigationBuildersInCommonMain",
		"checkNoKoinViewModelInCommonMain",
		"checkMaincoreHostFeatureEncapsulation",
		"checkMaincoreAndroidHostNoModuleResourceImports",
		"checkHostUiTextProviderBindings",
		"checkNoAndroidViewModelDslInKmpModules",
		"checkSharedViewModelBindings",
		"checkSharedPresentationCoverage",
		"checkSharedUseCaseCoverage",
		"checkSharedExceptionHandlerCoverage",
		"checkSharedValidatorCoverage",
		"reportCommonMainUiStringLiterals",
		"checkCommonMainUiStringCandidateBudget",
		"checkIosNoSyntheticAllowFlags",
		"checkNoLegacyDataMigrationCodepaths",
		"checkIosSmokeRequiredChecksCoverage",
		"checkIosSmokeVerifierChecksCoverage",
		"checkIosDeviceE2ERequiredChecksCoverage",
		"checkAttestationContractConsistency",
		"checkIosStructuredUserAgent",
		"checkIosAppAttestOnlySurface",
		"checkIosDefaultBridgeNoSecureStoreFallback",
		"checkIosBridgeNoPlaceholders",
		"verifyKmpTargets",
		"verifyKmpSharedTests",
		"verifyIosHostTypecheck",
		"verifyIosHostSmoke",
		":app:compileDebugAndroidTestSources",
		":app:compileDebugSources"
	)
}
