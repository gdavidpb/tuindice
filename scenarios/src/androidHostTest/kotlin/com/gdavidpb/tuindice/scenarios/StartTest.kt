package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.scenariokit.model.MockState
import com.gdavidpb.tuindice.scenarios.fixture.Coachmarks
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** The ways a scenario can begin: what each `Start` hands the app, read back the way the app reads it. */
@OptIn(ExperimentalTime::class)
class StartTest {
	private val seedKeys = listOf(
		DebugLaunchArguments.SEED_SESSION_ID,
		DebugLaunchArguments.SEED_ACCESS_TOKEN,
		DebugLaunchArguments.SEED_REFRESH_TOKEN,
		DebugLaunchArguments.SEED_USB_ID,
		DebugLaunchArguments.SEED_PASSWORD,
		DebugLaunchArguments.SEED_COACHMARKS,
		DebugLaunchArguments.MAIN_SECTION
	)

	@Test
	fun cleanStartsWithoutSeedAndWithAnimationsOffAndTheNetworkOn() {
		val spec = Start.Clean().toLaunchSpec()
		val parsed = DebugLaunchArguments.parse(spec.arguments)

		assertEquals(
			mapOf(
				DebugLaunchArguments.DISABLE_ANIMATIONS to "true",
				DebugLaunchArguments.NOW to E2eFixtures.Now,
				DebugLaunchArguments.NETWORK_AVAILABLE to "true"
			),
			spec.arguments
		)
		assertTrue(spec.mockStates.isEmpty())
		assertNull(parsed.sessionSeed)
		assertNull(parsed.availabilityNotice)
		assertEquals(true, parsed.animationsDisabled)
		assertEquals(true, parsed.networkAvailable)
	}

	@Test
	fun cleanCanTakeTheNetworkDown() {
		val parsed = DebugLaunchArguments.parse(Start.Clean(network = false).toLaunchSpec().arguments)

		assertEquals(false, parsed.networkAvailable)
	}

	@Test
	fun cleanCanShowTheAvailabilityNotice() {
		val notice = DebugLaunchArguments.AvailabilityNotice(true, Copy.NoticeTitle, Copy.NoticeMessage)
		val spec = Start.Clean(notice = notice).toLaunchSpec()
		val parsed = DebugLaunchArguments.parse(spec.arguments)

		assertEquals(notice, parsed.availabilityNotice)
		assertNull(parsed.sessionSeed)
		seedKeys.forEach { assertTrue(it !in spec.arguments, it) }
	}

	@Test
	fun seededCarriesTheSessionOfTheAccountAndItsMockState() {
		val spec = Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
		val seed = assertNotNull(DebugLaunchArguments.parse(spec.arguments).sessionSeed)
		val session = checkNotNull(E2eAccounts.Canonical.session)

		assertEquals(session.sessionId, seed.sessionId)
		assertEquals(session.accessToken, seed.accessToken)
		assertEquals(session.refreshToken, seed.refreshToken)
		assertEquals("11-11111", seed.usbId)
		assertEquals("123456", seed.password)
		assertEquals(listOf(MockState("login-token-lifecycle", "TokensIssued")), spec.mockStates)
	}

	@Test
	fun seededDefaultsToTheSummaryWithTheCoachmarksSeen() {
		val seed = assertNotNull(
			DebugLaunchArguments.parse(Start.Seeded(E2eAccounts.Canonical).toLaunchSpec().arguments).sessionSeed
		)

		assertEquals(MainSection.SUMMARY, seed.mainSection)
		assertEquals(true, seed.coachmarksSeen)
	}

	@Test
	fun seededCanStartOnAnySectionWithTheCoachmarksPending() {
		MainSection.entries.forEach { section ->
			val spec = Start.Seeded(E2eAccounts.Canonical, section, Coachmarks.Pending).toLaunchSpec()
			val seed = assertNotNull(DebugLaunchArguments.parse(spec.arguments).sessionSeed)

			assertEquals(section, seed.mainSection)
			assertEquals(false, seed.coachmarksSeen)
		}
	}

	@Test
	fun seededCanTakeTheNetworkDown() {
		val spec = Start.Seeded(E2eAccounts.Canonical, network = false).toLaunchSpec()

		assertEquals(false, DebugLaunchArguments.parse(spec.arguments).networkAvailable)
	}

	@Test
	fun everyStartUsesOnlyDeclaredKeys() {
		val specs = listOf(Start.Clean().toLaunchSpec()) +
			E2eAccounts.all.filter { it.session != null }.map { Start.Seeded(it).toLaunchSpec() }

		specs.forEach { spec ->
			assertTrue(spec.arguments.keys.all { it in DebugLaunchArguments.keys })
		}
	}

	@Test
	fun everyStartFreezesTheClockAtTheSameNamedInstant() {
		val specs = listOf(
			Start.Clean(),
			Start.Clean(network = false),
			Start.Seeded(E2eAccounts.Canonical)
		).map { it.toLaunchSpec() }

		specs.forEach { spec ->
			assertEquals(E2eFixtures.Now, spec.arguments[DebugLaunchArguments.NOW])
			assertEquals(Instant.parse(E2eFixtures.Now), DebugLaunchArguments.parse(spec.arguments).fixedNow)
		}
	}
}
