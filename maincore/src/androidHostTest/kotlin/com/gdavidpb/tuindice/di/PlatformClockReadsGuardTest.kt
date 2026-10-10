package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.di.ClockReadsScan.appSources
import com.gdavidpb.tuindice.di.ClockReadsScan.findReads
import com.gdavidpb.tuindice.di.ClockReadsScan.platformClockReads
import com.gdavidpb.tuindice.di.ClockReadsScan.readsAny
import com.gdavidpb.tuindice.di.ClockReadsScan.sourcesOf
import com.gdavidpb.tuindice.di.ClockReadsScan.swiftClockReads
import com.gdavidpb.tuindice.di.ClockReadsScan.swiftSources
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Platform code (`androidMain`, `iosMain`, `app/` main and debug) reads no system clock at all, and the Swift of
 * the iOS host reads `Date` only in the files this test lists. See [ClockReadsGuardTest] for why.
 */
class PlatformClockReadsGuardTest {
	// `Date()` and friends in the Swift of the iOS host: the age of a build, never a date a screen shows.
	private val allowedSwiftReads = mapOf(
		"iosApp/Sources/TuIndiceHost/TuIndicePlatformBridge.swift" to
			"isReleaseDateOlderThan: whether the build is older than the staleness window, never shown"
	)

	@Test
	fun platformCodeReadsNoSystemClock() {
		val sources = sourcesOf("androidMain") + sourcesOf("iosMain") + appSources()

		assertTrue(sources.isNotEmpty())
		assertEquals(emptyList(), findReads(sources, platformClockReads, emptySet()))
	}

	@Test
	fun theSwiftOfTheHostReadsTheClockOnlyWhereThisTestSaysSo() {
		val sources = swiftSources()

		assertTrue(sources.isNotEmpty())
		assertEquals(emptyList(), findReads(sources, swiftClockReads, allowedSwiftReads.keys))
		allowedSwiftReads.keys.forEach { path ->
			assertTrue(
				readsAny(sources.getValue(path), swiftClockReads),
				"'$path' no longer reads the clock: drop it"
			)
		}
	}

	@Test
	fun aSwiftClockReadIsFoundForEachWayOfReadingIt() {
		val sources = mapOf(
			"iosApp/Sources/A.swift" to "let elapsed = Date().timeIntervalSince(start)",
			"iosApp/Sources/B.swift" to "let now = Date.now",
			"iosApp/Sources/C.swift" to "let now = NSDate()",
			"iosApp/Sources/D.swift" to "let t = CFAbsoluteTimeGetCurrent()",
			"iosApp/Sources/E.swift" to "let now = Date.init()",
			"iosApp/Sources/F.swift" to "let t = ProcessInfo.processInfo.systemUptime",
			"iosApp/Sources/Clean.swift" to "let parsed = Date(timeIntervalSince1970: 0)\nlet d = releaseDate()"
		)

		assertEquals(
			listOf(
				"iosApp/Sources/A.swift",
				"iosApp/Sources/B.swift",
				"iosApp/Sources/C.swift",
				"iosApp/Sources/D.swift",
				"iosApp/Sources/E.swift"
			),
			findReads(sources, swiftClockReads, emptySet())
		)
	}

	@Test
	fun aPlatformClockReadIsFoundForEachWayOfReadingIt() {
		val sources = mapOf(
			"base/src/androidMain/A.kt" to "val t = System.currentTimeMillis()",
			"base/src/iosMain/B.kt" to "val d = NSDate()",
			"app/src/main/C.kt" to "val d = LocalDate.now()",
			"app/src/debug/D.kt" to "import kotlin.time.Clock.System",
			"base/src/iosMain/E.kt" to "val t = currentTimeMillis()",
			"base/src/iosMain/F.kt" to "val t = Instant.now()",
			"base/src/iosMain/G.kt" to "val t = LocalDateTime.now()",
			"base/src/androidMain/H.kt" to "val t = ZonedDateTime.now()",
			"base/src/androidMain/I.kt" to "val t = Calendar.getInstance()",
			"base/src/androidMain/J.kt" to "val t = Date()",
			"base/src/iosMain/K.kt" to "val t = CFAbsoluteTimeGetCurrent()",
			"base/src/iosMain/L.kt" to "val t = getTimeMillis()",
			"base/src/iosMain/M.kt" to "val t = ::currentTimeMillis",
			"base/src/androidMain/Clean.kt" to "fun f(clock: Clock) = clock.now()\nval d = parseDate()\nval x = Date(0L)"
		)

		assertEquals(
			listOf(
				"app/src/debug/D.kt",
				"app/src/main/C.kt",
				"base/src/androidMain/A.kt",
				"base/src/androidMain/H.kt",
				"base/src/androidMain/I.kt",
				"base/src/androidMain/J.kt",
				"base/src/iosMain/B.kt",
				"base/src/iosMain/E.kt",
				"base/src/iosMain/F.kt",
				"base/src/iosMain/G.kt",
				"base/src/iosMain/K.kt",
				"base/src/iosMain/L.kt",
				"base/src/iosMain/M.kt"
			),
			findReads(sources, platformClockReads, emptySet())
		)
	}
}
