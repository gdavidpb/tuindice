package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.StepOutcome
import com.gdavidpb.tuindice.scenariokit.model.TextEntryMode
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContainerStepsTest {
	private val button: Query = Query.Tag("button")
	private val banner: Query = Query.Tag("banner")
	private val tapButton = Step.Tap(button)

	private fun driver(vararg elements: Pair<Query, FakeElement>, platform: Platform = Platform.Android) =
		FakeDriver(platform).apply { screen.putAll(elements) }

	@Test
	fun ifVisible_neverFailsWhenTheElementIsAbsent() {
		val fake = driver()

		val outcome = fake.run(Step.IfVisible(banner, 1_500, listOf(tapButton)))

		assertPassed(outcome)
		assertEquals(StepOutcome.Skipped, outcome.steps.single().outcome)
		assertTrue(fake.taps.isEmpty())
	}

	@Test
	fun ifVisible_runsItsStepsWhenTheElementShowsUp() {
		val fake = driver(banner to FakeElement(), button to FakeElement())

		assertPassed(fake.run(Step.IfVisible(banner, 1_500, listOf(tapButton))))
		assertEquals(listOf(button), fake.taps)
	}

	@Test
	fun ifVisible_whenItsStepsFail_failsAtTheInnerStep() {
		val fake = driver(banner to FakeElement())

		val failure = assertFailed(fake.run(Step.IfVisible(banner, 1_500, listOf(tapButton))), FailureKind.STEP_TIMEOUT)

		assertEquals(1, failure.stepIndex)
		assertEquals("Tap", failure.primitive)
	}

	@Test
	fun ifGone_neverFailsAndRunsOnlyWhenTheElementIsGone() {
		val gone = driver(button to FakeElement())
		val staying = driver(banner to FakeElement(), button to FakeElement())

		assertPassed(gone.run(Step.IfGone(banner, 1_500, listOf(tapButton))))
		assertEquals(listOf(button), gone.taps)
		assertPassed(staying.run(Step.IfGone(banner, 1_500, listOf(tapButton))))
		assertTrue(staying.taps.isEmpty())
	}

	@Test
	fun onPlatform_runsOnlyOnTheMatchingPlatform() {
		val android = driver(button to FakeElement())
		val ios = driver(button to FakeElement(), platform = Platform.Ios)

		assertPassed(android.run(Step.OnPlatform(Platform.Ios, listOf(tapButton))))
		assertTrue(android.taps.isEmpty())
		assertPassed(ios.run(Step.OnPlatform(Platform.Ios, listOf(tapButton))))
		assertEquals(listOf(button), ios.taps)
	}

	@Test
	fun group_runsItsStepsAndAppearsInTheRecords() {
		val fake = driver(button to FakeElement())

		val outcome = fake.run(Step.Group("sign in", listOf(tapButton)))

		assertPassed(outcome)
		assertEquals(listOf("Group", "Tap"), outcome.steps.map { it.primitive })
		assertEquals("sign in", outcome.steps.first().target)
	}

	@Test
	fun retry_isBoundedByTheCapEvenWhenAskedForMore() {
		val fake = driver(button to FakeElement(enabled = false))

		val outcome = fake.run(Step.Retry(maxAttempts = 99, reason = "flaky", steps = listOf(tapButton)))

		assertFailed(outcome, FailureKind.ASSERTION)
		assertEquals(Step.Retry.MAX_ATTEMPTS, outcome.steps.count { it.primitive == "Tap" })
	}

	@Test
	fun retry_stopsAtTheFirstSuccess() {
		// Disabled through the whole first tap budget (one check plus one per poll), enabled for the second attempt.
		val firstAttemptChecks = (Timeouts.Action / Timeouts.PollInterval).toInt() + 1
		val fake = driver(button to FakeElement(enabled = false, enabledAfterChecks = firstAttemptChecks))

		val outcome = fake.run(Step.Retry(3, "flaky", listOf(tapButton)))

		assertPassed(outcome)
		val tapOutcomes = outcome.steps.filter { it.primitive == "Tap" }.map { it.outcome }
		assertEquals(listOf(StepOutcome.Failed, StepOutcome.Passed), tapOutcomes)
		assertEquals(listOf(button), fake.taps)
	}

	@Test
	fun retry_doesNotRepeatATypedTextMismatch() {
		val field = Query.Tag("field")
		val fake = driver(field to FakeElement(text = ""))
		fake.typing = { it.drop(1) }
		val enter = Step.EnterText(field, "abc", null, false, true, TextEntryMode.Keys)

		val outcome = fake.run(Step.Retry(3, "flaky", listOf(enter)))

		assertFailed(outcome, FailureKind.TYPED_TEXT_MISMATCH)
		assertEquals(1, fake.calls.count { it == "typeKeys" })
	}
}
