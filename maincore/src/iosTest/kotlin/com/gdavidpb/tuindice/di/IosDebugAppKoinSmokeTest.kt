package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.debug.OverridableClock
import com.gdavidpb.tuindice.debug.freezeDebugClock
import com.gdavidpb.tuindice.domain.model.IosBuildVariant
import com.gdavidpb.tuindice.testkit.koin.withStartedKoin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The graph of the debug build, which is the one the E2E flows run against: what [IosAppKoinSmokeTest]
 * loads plus the debug variant module on top, as `startIosKoin` does for a debug host.
 */
@OptIn(ExperimentalTime::class)
class IosDebugAppKoinSmokeTest {
	// The clock the app reads for the dates it shows is the one the E2E launch argument freezes, and
	// whoever asks Koin for it gets that same instance.
	@Test
	fun theClockTheLaunchArgumentFreezesIsTheOneTheAppReads() = withStartedKoin(
		start = {
			startAppKoin(
				AppKoinBootstrapRequest(
					platformBootstrap = IosKoinBootstrap(
						iOSContext = iosSmokeTestContext(),
						platformVariantModules = iosVariantModules(IosBuildVariant.DEBUG)
					)
				)
			)
		}
	) {
		assertTrue(get<Clock>() is OverridableClock)

		freezeDebugClock(Instant.parse("2026-10-15T12:00:00Z"))

		assertEquals(Instant.parse("2026-10-15T12:00:00Z"), get<Clock>().now())
	}

	@Test
	fun theProductionGraphReadsTheSystemClock() = withStartedKoin(
		start = {
			startAppKoin(
				AppKoinBootstrapRequest(
					platformBootstrap = IosKoinBootstrap(
						iOSContext = iosSmokeTestContext(),
						platformVariantModules = iosVariantModules(IosBuildVariant.PRODUCTION)
					)
				)
			)
		}
	) {
		assertEquals(Clock.System, get<Clock>())
	}
}
