package com.gdavidpb.tuindice.di

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the screens show by date is told by the clock Koin binds (`LocalTuIndiceClock` for the
 * composables), so debug builds can freeze it. Reading the system clock directly would bring back a
 * "today" the E2E launch argument cannot reach. Three rules keep it so:
 * 1. `Clock.System` in `commonMain` only where this test says so.
 * 2. `currentTimeMillis(` is not read where the content of a screen is decided (`presentation`, `ui`,
 *    `domain` and `utils` of `commonMain`) except in the files this test lists.
 * 3. Platform code (`androidMain`, `iosMain`, `app/` main and debug) reads no system clock at all.
 */
class ClockReadsGuardTest {
	private val repoRoot = File("..")

	private val allowedReads = mapOf(
		"maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/di/CommonModule.kt" to "the Koin binding of the clock",
		"maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/debug/OverridableClock.kt" to
			"the debug clock delegates to the system one until a launch argument freezes it",
		"base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/ui/style/LocalTuIndiceClock.kt" to
			"the value for trees no host builds (previews, tests)",
		"base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/utils/Time.kt" to
			"currentTimeMillis: token expiry, cooldowns, retry windows, timestamps and telemetry"
	)

	// currentTimeMillis( in the folders that decide content: elapsed time and stamps, never a date a screen shows.
	private val allowedMillisReads = mapOf(
		"base/src/commonMain/kotlin/com/gdavidpb/tuindice/base/utils/Time.kt" to
			"the definition of currentTimeMillis",
		"persistence/src/commonMain/kotlin/com/gdavidpb/tuindice/persistence/domain/mutation/StoreBackedMutationEngine.kt" to
			"stamps of the outbox rows",
		"record/src/commonMain/kotlin/com/gdavidpb/tuindice/record/presentation/transition/RecordAnyStateTransitions.kt" to
			"stamps a selection in flight; it is never shown"
	)

	// The harness and its catalog are not the app.
	private val notTheApp = setOf("scenariokit", "scenariorunner", "scenarios", "testkit")

	private val contentFolders = listOf("/presentation/", "/ui/", "/domain/", "/utils/")

	@Test
	fun theAppReadsTheSystemClockOnlyWhereThisTestSaysSo() {
		val sources = sourcesOf("commonMain")

		assertTrue(sources.isNotEmpty())
		assertEquals(emptyList(), findReads(sources, listOf(SYSTEM_CLOCK), allowedReads.keys))
		allowedReads.keys.forEach { path ->
			assertTrue(sources.getValue(path).contains(SYSTEM_CLOCK), "'$path' no longer reads the system clock: drop it")
		}
	}

	@Test
	fun aReadOutsideTheAllowedFilesIsFound() {
		val sources = mapOf(
			"evaluations/src/commonMain/A.kt" to "val today = Clock.System.now()",
			"record/src/commonMain/B.kt" to "val today = kotlin.time.Clock.System\n\t.now()",
			"summary/src/commonMain/C.kt" to "class C(private val clock: Clock = Clock.System)",
			"base/src/commonMain/Allowed.kt" to "val ok = Clock.System.now()",
			"base/src/commonMain/Clean.kt" to "fun f(clock: Clock) = clock.now()"
		)

		assertEquals(
			listOf("evaluations/src/commonMain/A.kt", "record/src/commonMain/B.kt", "summary/src/commonMain/C.kt"),
			findReads(sources, listOf(SYSTEM_CLOCK), setOf("base/src/commonMain/Allowed.kt"))
		)
	}

	@Test
	fun theMillisClockIsNotReadWhereTheContentOfAScreenIsDecided() {
		val sources = sourcesOf("commonMain").filterKeys { path -> contentFolders.any { folder -> folder in path } }

		assertTrue(sources.isNotEmpty())
		assertEquals(emptyList(), findReads(sources, listOf(MILLIS_CLOCK), allowedMillisReads.keys))
		allowedMillisReads.keys.forEach { path ->
			assertTrue(sources.getValue(path).contains(MILLIS_CLOCK), "'$path' no longer reads the millis clock: drop it")
		}
	}

	@Test
	fun aMillisReadInAContentFolderIsFoundAndTheAllowedOnesAreNot() {
		val sources = mapOf(
			"evaluations/src/commonMain/kotlin/x/presentation/utils/Date.kt" to "val now = currentTimeMillis()",
			"evaluations/src/commonMain/kotlin/x/domain/usecase/Get.kt" to "val b = kotlin.run { currentTimeMillis() }",
			"summary/src/commonMain/kotlin/x/ui/View.kt" to "fun f(clock: Clock) = clock.now()",
			"summary/src/commonMain/kotlin/x/utils/Allowed.kt" to "val ok = currentTimeMillis()"
		)
		val inContentFolders = sources.filterKeys { path -> contentFolders.any { folder -> folder in path } }

		assertEquals(sources.keys, inContentFolders.keys)
		assertEquals(
			listOf(
				"evaluations/src/commonMain/kotlin/x/domain/usecase/Get.kt",
				"evaluations/src/commonMain/kotlin/x/presentation/utils/Date.kt"
			),
			findReads(inContentFolders, listOf(MILLIS_CLOCK), setOf("summary/src/commonMain/kotlin/x/utils/Allowed.kt"))
		)
	}

	@Test
	fun platformCodeReadsNoSystemClock() {
		val sources = sourcesOf("androidMain") + sourcesOf("iosMain") + appSources()

		assertTrue(sources.isNotEmpty())
		assertEquals(emptyList(), findReads(sources, platformClockReads, emptySet()))
	}

	@Test
	fun aPlatformClockReadIsFoundForEachWayOfReadingIt() {
		val sources = mapOf(
			"base/src/androidMain/A.kt" to "val t = System.currentTimeMillis()",
			"base/src/iosMain/B.kt" to "val d = NSDate()",
			"app/src/main/C.kt" to "val d = LocalDate.now()",
			"app/src/debug/D.kt" to "import kotlin.time.Clock.System",
			"base/src/iosMain/E.kt" to "val t = currentTimeMillis()",
			"base/src/androidMain/Clean.kt" to "fun f(clock: Clock) = clock.now()"
		)

		assertEquals(
			listOf(
				"app/src/debug/D.kt",
				"app/src/main/C.kt",
				"base/src/androidMain/A.kt",
				"base/src/iosMain/B.kt",
				"base/src/iosMain/E.kt"
			),
			findReads(sources, platformClockReads, emptySet())
		)
	}

	private fun sourcesOf(sourceSet: String): Map<String, String> =
		repoRoot.listFiles { file -> file.isDirectory && file.name !in notTheApp }.orEmpty()
			.map { module -> File(module, "src/$sourceSet") }
			.let(::readKotlinFiles)

	// The host's production and debug source sets; its tests may read what they like.
	private fun appSources(): Map<String, String> =
		listOf("main", "debug").map { variant -> File(repoRoot, "app/src/$variant") }.let(::readKotlinFiles)

	private fun readKotlinFiles(roots: List<File>): Map<String, String> =
		roots.filter { it.isDirectory }
			.flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }
			.associate { file -> file.relativeTo(repoRoot).path to file.readText() }

	private fun findReads(sources: Map<String, String>, reads: List<String>, allowed: Set<String>): List<String> =
		sources.filter { (path, text) -> path !in allowed && reads.any { read -> text.contains(read) } }.keys.sorted()

	private companion object {
		const val SYSTEM_CLOCK = "Clock.System"
		const val MILLIS_CLOCK = "currentTimeMillis("
		val platformClockReads = listOf("System.currentTimeMillis", "NSDate", "LocalDate.now", SYSTEM_CLOCK, MILLIS_CLOCK)
	}
}
