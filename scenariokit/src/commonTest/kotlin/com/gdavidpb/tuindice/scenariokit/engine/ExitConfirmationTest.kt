package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Site
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/**
 * A scenario cannot end green having left the app without a step waiting for it: the interpreter confirms the
 * foreground when the last step passes and before a `Foreground` or a `Relaunch`, unless a `WaitBackgrounded`
 * announced the exit.
 */
class ExitConfirmationTest {
	private val link: Query = Query.Tag("link")
	private val title: Query = Query.Tag("title")
	private val site = Site("About.kt", 42)

	/** A screen whose [link] takes the app out when it is tapped, while every lookup keeps finding [title]. */
	private fun driver() = FakeDriver().apply {
		screen[link] = FakeElement()
		screen[title] = FakeElement()
		onTap[link] = { inForeground = false }
	}

	private fun confirmations(fake: FakeDriver) = fake.calls.count { it == "confirmForeground" }

	@Test
	fun anExitBySurprise_failsTheScenarioAtTheEndEvenIfTheLookupsAfterItPass() {
		val fake = driver()

		val outcome = fake.run(Step.Tap(link), Step.WaitVisible(title, 1_000, site))

		val failure = assertFailed(outcome, FailureKind.APP_NOT_RUNNING, stepIndex = 1)
		assertContains(failure.message, "left the foreground")
		assertContains(failure.message, "no step waited for it")
		assertContains(failure.message, "WaitVisible")
		assertEquals(site, failure.site)
		assertEquals(listOf("test-scenario" to 1), fake.captured)
	}

	@Test
	fun anExitBySurprise_failsBeforeARelaunchWithoutRunningIt() {
		val fake = driver()

		val outcome = fake.run(Step.Tap(link), Step.WaitVisible(title, 1_000), Step.Relaunch(emptyMap(), site))

		val failure = assertFailed(outcome, FailureKind.APP_NOT_RUNNING, stepIndex = 2)
		assertContains(failure.message, "no step waited for it")
		assertEquals(site, failure.site)
		assertEquals(1, fake.launches.size, "only the launch of the scenario: the Relaunch did not run")
	}

	@Test
	fun anExitBySurprise_failsBeforeAForegroundWithoutRunningIt() {
		val fake = driver()

		val outcome = fake.run(Step.Tap(link), Step.Foreground(site))

		assertFailed(outcome, FailureKind.APP_NOT_RUNNING, stepIndex = 1)
		assertEquals(0, fake.calls.count { it == "foreground" })
	}

	@Test
	fun anExitThatAWaitBackgroundedAnnounced_passesWhenTheAppComesBackWithForeground() {
		val fake = driver()

		val outcome = fake.run(
			Step.Tap(link),
			Step.WaitBackgrounded(2_000),
			Step.Foreground(),
			Step.WaitVisible(title, 1_000)
		)

		assertPassed(outcome)
		assertEquals(1, confirmations(fake), "only the end: the Foreground was announced")
	}

	@Test
	fun anExitThatAWaitBackgroundedAnnounced_passesWhenTheAppComesBackWithARelaunch() {
		val fake = driver().apply { launchBringsFront = true }

		assertPassed(fake.run(Step.Tap(link), Step.WaitBackgrounded(2_000), Step.Relaunch(emptyMap())))
		assertEquals(1, confirmations(fake))
	}

	@Test
	fun aScenarioThatEndsOutsideAfterAWaitBackgrounded_passesWithoutAsking() {
		val fake = driver()

		assertPassed(fake.run(Step.Tap(link), Step.WaitBackgrounded(2_000)))
		assertEquals(0, confirmations(fake))
	}

	@Test
	fun aForegroundClosesTheAnnouncement_soASecondExitBySurpriseFails() {
		val fake = driver()

		val outcome = fake.run(
			Step.Tap(link),
			Step.WaitBackgrounded(2_000),
			Step.Foreground(),
			Step.Tap(link),
			Step.WaitVisible(title, 1_000)
		)

		assertFailed(outcome, FailureKind.APP_NOT_RUNNING, stepIndex = 4)
	}

	@Test
	fun aScenarioThatNeverLeaves_asksOnceAndPaysTheWindowOnlyOnce() {
		val fake = driver().apply { confirmTakesMs = 3_500 }
		fake.onTap.clear()
		val before = fake.time.markNow()

		assertPassed(fake.run(Step.Tap(link), Step.WaitVisible(title, 1_000)))

		assertEquals(1, confirmations(fake))
		assertEquals(3_500.milliseconds, before.elapsedNow())
	}

	@Test
	fun anExitThatComesLongAfterTheLastInput_butBeforeTheWindowEnds_isStillSeen() {
		val fake = driver().apply {
			confirmTakesMs = 3_500
			leavesAtMs = 2_000
			onTap.clear()
		}

		val outcome = fake.run(Step.Tap(link), Step.WaitVisible(title, 1_000))

		assertFailed(outcome, FailureKind.APP_NOT_RUNNING, stepIndex = 1)
	}

	@Test
	fun aScenarioThatAnnouncedItsExit_paysNothingForTheConfirmation() {
		val fake = driver().apply { confirmTakesMs = 3_500 }
		val before = fake.time.markNow()

		assertPassed(fake.run(Step.Tap(link), Step.WaitBackgrounded(2_000)))

		assertEquals(0, confirmations(fake))
		assertTrue(before.elapsedNow() < 3_500.milliseconds, "the window was paid: ${before.elapsedNow()}")
	}

	@Test
	fun aScenarioThatAlreadyFailed_keepsItsOwnFailureAndDoesNotAsk() {
		val fake = driver()

		val outcome = fake.run(Step.Tap(link), Step.WaitVisible(Query.Tag("missing"), 1_000), Step.Foreground())

		val failure = assertNotNull(outcome.failure)
		assertEquals(1, failure.stepIndex)
		assertEquals(0, confirmations(fake))
	}

	@Test
	fun theConfirmationOnlyCountsTheTopLevelEnd_notTheEndOfAContainer() {
		val fake = driver()

		val outcome = fake.run(Step.Group("leave", listOf(Step.Tap(link))), Step.WaitBackgrounded(2_000))

		assertPassed(outcome)
		assertEquals(0, confirmations(fake))
	}

	@Test
	fun aDriverThatThrowsWhileConfirming_isADriverErrorNotACrash() {
		val fake = driver().apply { throwOn = "confirmForeground" }

		assertFailed(fake.run(Step.WaitVisible(title, 1_000)), FailureKind.DRIVER_ERROR)
	}

	@Test
	fun anAppThatIsNotInFrontAtTheEnd_failsEvenWithoutAnInput() {
		val fake = driver().apply { confirmScript = { false } }

		assertFailed(fake.run(Step.WaitVisible(title, 1_000)), FailureKind.APP_NOT_RUNNING, stepIndex = 0)
	}
}
