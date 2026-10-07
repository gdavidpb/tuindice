package com.gdavidpb.tuindice.di

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the screens show by date is told by the clock Koin binds (`LocalTuIndiceClock` for the
 * composables), so debug builds can freeze it. Reading the system clock directly in `commonMain` would
 * bring back a "today" the E2E launch argument cannot reach; the only reads left are these.
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

	// The harness and its catalog are not the app.
	private val notTheApp = setOf("scenariokit", "scenariorunner", "scenarios", "testkit")

	@Test
	fun theAppReadsTheSystemClockOnlyWhereThisTestSaysSo() {
		val sources = repoRoot.listFiles { file -> file.isDirectory && file.name !in notTheApp }.orEmpty()
			.map { module -> File(module, "src/commonMain") }
			.filter { it.isDirectory }
			.flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }
			.associate { file -> file.relativeTo(repoRoot).path to file.readText() }

		assertTrue(sources.isNotEmpty())
		assertEquals(emptyList(), findSystemClockReads(sources, allowedReads.keys))
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
			findSystemClockReads(sources, setOf("base/src/commonMain/Allowed.kt"))
		)
	}

	private fun findSystemClockReads(sources: Map<String, String>, allowed: Set<String>): List<String> =
		sources.filter { (path, text) -> path !in allowed && text.contains(SYSTEM_CLOCK) }.keys.sorted()

	private companion object {
		const val SYSTEM_CLOCK = "Clock.System"
	}
}
