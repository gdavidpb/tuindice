package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.di.ClockReadsScan.findReads
import com.gdavidpb.tuindice.di.ClockReadsScan.millisClockReads
import com.gdavidpb.tuindice.di.ClockReadsScan.readsAny
import com.gdavidpb.tuindice.di.ClockReadsScan.sourcesOf
import com.gdavidpb.tuindice.di.ClockReadsScan.systemClockReads
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
 * 3. Platform code reads no system clock at all (`PlatformClockReadsGuardTest`).
 */
class ClockReadsGuardTest {
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

	private val contentFolders = listOf("/presentation/", "/ui/", "/domain/", "/utils/", "/data/mapper/")

	@Test
	fun theAppReadsTheSystemClockOnlyWhereThisTestSaysSo() {
		val sources = sourcesOf("commonMain")

		assertTrue(sources.isNotEmpty())
		assertEquals(emptyList(), findReads(sources, systemClockReads, allowedReads.keys))
		allowedReads.keys.forEach { path ->
			assertTrue(
				readsAny(sources.getValue(path), systemClockReads),
				"'$path' no longer reads the system clock: drop it"
			)
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
			findReads(sources, systemClockReads, setOf("base/src/commonMain/Allowed.kt"))
		)
	}

	@Test
	fun aSystemClockReachedThroughAnAliasOfTheClockIsFound() {
		val sources = mapOf(
			"evaluations/src/commonMain/A.kt" to "import kotlin.time.Clock as SystemClock\nval c = SystemClock.System",
			"record/src/commonMain/B.kt" to "import kotlinx.datetime.Clock   as   Kx",
			"summary/src/commonMain/Clean.kt" to "import kotlin.time.Clock\nfun f(clock: Clock) = clock.now()"
		)

		assertEquals(
			listOf("evaluations/src/commonMain/A.kt", "record/src/commonMain/B.kt"),
			findReads(sources, systemClockReads, emptySet())
		)
	}

	@Test
	fun theMillisClockIsNotReadWhereTheContentOfAScreenIsDecided() {
		val sources = sourcesOf("commonMain").filterKeys { path -> contentFolders.any { folder -> folder in path } }

		assertTrue(sources.isNotEmpty())
		assertEquals(emptyList(), findReads(sources, millisClockReads, allowedMillisReads.keys))
		allowedMillisReads.keys.forEach { path ->
			assertTrue(
				readsAny(sources.getValue(path), millisClockReads),
				"'$path' no longer reads the millis clock: drop it"
			)
		}
	}

	@Test
	fun aMillisReadInAContentFolderIsFoundAndTheAllowedOnesAreNot() {
		val sources = mapOf(
			"evaluations/src/commonMain/kotlin/x/presentation/utils/Date.kt" to "val now = currentTimeMillis()",
			"evaluations/src/commonMain/kotlin/x/domain/usecase/Get.kt" to "val b = kotlin.run { currentTimeMillis() }",
			"evaluations/src/commonMain/kotlin/x/data/mapper/Mapper.kt" to "val now = currentTimeMillis()",
			"evaluations/src/commonMain/kotlin/x/ui/Reference.kt" to "val read = ::currentTimeMillis",
			"summary/src/commonMain/kotlin/x/ui/View.kt" to "fun f(clock: Clock) = clock.now()",
			"summary/src/commonMain/kotlin/x/utils/Allowed.kt" to "val ok = currentTimeMillis()"
		)
		val inContentFolders = sources.filterKeys { path -> contentFolders.any { folder -> folder in path } }

		assertEquals(sources.keys, inContentFolders.keys)
		assertEquals(
			listOf(
				"evaluations/src/commonMain/kotlin/x/data/mapper/Mapper.kt",
				"evaluations/src/commonMain/kotlin/x/domain/usecase/Get.kt",
				"evaluations/src/commonMain/kotlin/x/presentation/utils/Date.kt",
				"evaluations/src/commonMain/kotlin/x/ui/Reference.kt"
			),
			findReads(inContentFolders, millisClockReads, setOf("summary/src/commonMain/kotlin/x/utils/Allowed.kt"))
		)
	}
}
