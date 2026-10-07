package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ElementBounds
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.StepOutcome
import com.gdavidpb.tuindice.scenariokit.model.TextEntryMode
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class StepKindsTest {
	private val button: Query = Query.Tag("button")
	private val field: Query = Query.Tag("field")

	private fun driver(vararg elements: Pair<Query, FakeElement>) = FakeDriver().apply { screen.putAll(elements) }

	private fun enter(
		text: String,
		expect: String? = null,
		secure: Boolean = false,
		replace: Boolean = false,
		mode: TextEntryMode = TextEntryMode.Keys
	) = Step.EnterText(field, text, expect, secure, replace, mode)

	@Test
	fun tap_onAnEnabledElement_passesAndTapsIt() {
		val fake = driver(button to FakeElement())

		val outcome = fake.run(Step.Tap(button))

		assertPassed(outcome)
		assertEquals(listOf(button), fake.taps)
		assertEquals(StepOutcome.Passed, outcome.steps.single().outcome)
	}

	@Test
	fun tap_onAnElementThatNeverShowsUp_timesOut() {
		val outcome = driver().run(Step.Tap(button))

		assertFailed(outcome, FailureKind.STEP_TIMEOUT, stepIndex = 0)
	}

	@Test
	fun tap_onADisabledElement_failsWithoutTappingIt() {
		val fake = driver(button to FakeElement(enabled = false))

		val failure = assertFailed(fake.run(Step.Tap(button)), FailureKind.ASSERTION)

		assertEquals("disabled", failure.actual)
		assertTrue(fake.taps.isEmpty())
	}

	@Test
	fun tap_onAnElementThatEnablesMidBudget_waitsAndTapsItOnce() {
		val fake = driver(button to FakeElement(enabled = false, enabledAfterChecks = 5))
		val start = fake.time.markNow()

		assertPassed(fake.run(Step.Tap(button)))

		assertEquals(listOf(button), fake.taps)
		assertTrue(start.elapsedNow() < Timeouts.Action.milliseconds)
	}

	@Test
	fun tap_onAnElementDisabledForTheWholeBudget_failsAfterWaitingIt() {
		val fake = driver(button to FakeElement(enabled = false))
		val start = fake.time.markNow()

		val failure = assertFailed(fake.run(Step.Tap(button)), FailureKind.ASSERTION)

		assertEquals("enabled", failure.expected)
		assertEquals("disabled", failure.actual)
		assertContains(failure.message, "${Timeouts.Action} ms")
		assertTrue(start.elapsedNow() >= Timeouts.Action.milliseconds)
		assertTrue(fake.taps.isEmpty())
	}

	@Test
	fun tap_withoutRequireEnabled_tapsADisabledElement() {
		val fake = driver(button to FakeElement(enabled = false))

		assertPassed(fake.run(Step.Tap(button, requireEnabled = false)))
		assertEquals(listOf(button), fake.taps)
	}

	@Test
	fun tapAtAndDoubleTap_passOnAVisibleElementAndTimeOutOnAMissingOne() {
		val fake = driver(button to FakeElement())

		assertPassed(fake.run(Step.TapAt(button, 0.5, 0.5), Step.TapAt(null, 0.5, 0.5), Step.DoubleTap(button)))
		assertFailed(driver().run(Step.TapAt(button, 0.5, 0.5)), FailureKind.STEP_TIMEOUT)
		assertFailed(driver().run(Step.DoubleTap(button)), FailureKind.STEP_TIMEOUT)
	}

	@Test
	fun back_failsWhenTheDriverDoesNotHandleIt() {
		assertPassed(driver().run(Step.Back()))

		val fake = driver().apply { backResult = false }
		assertFailed(fake.run(Step.Back()), FailureKind.ASSERTION)
	}

	@Test
	fun enterText_typesWithKeysAndPassesWhenTheFieldShowsIt() {
		val fake = driver(field to FakeElement(text = ""))

		assertPassed(fake.run(enter("12-34567")))
		assertContains(fake.calls, "typeKeys")
	}

	@Test
	fun enterText_withReplace_clearsBeforeTyping() {
		val fake = driver(field to FakeElement(text = "old"))

		assertPassed(fake.run(enter("new", replace = true)))

		assertTrue(fake.calls.indexOf("clearText") < fake.calls.indexOf("typeKeys"))
		assertEquals("new", fake.screen.getValue(field).text)
	}

	@Test
	fun enterText_inSetMode_assignsAtomically() {
		val fake = driver(field to FakeElement(text = ""))

		assertPassed(fake.run(enter("value", mode = TextEntryMode.Set)))

		assertContains(fake.calls, "setText")
		assertTrue("typeKeys" !in fake.calls)
	}

	@Test
	fun enterText_comparesTheFieldWithExpectWhenGiven() {
		val fake = driver(field to FakeElement(text = ""))
		fake.typing = { digits -> if (digits.length > 2) digits.take(2) + "-" + digits.drop(2) else digits }

		assertPassed(fake.run(enter("1234567", expect = "12-34567")))
	}

	@Test
	fun enterText_whenTheFieldShowsOtherText_failsWithBothStrings() {
		val fake = driver(field to FakeElement(text = ""))
		fake.typing = { it.replace("b", "") }

		val failure = assertFailed(fake.run(enter("abc")), FailureKind.TYPED_TEXT_MISMATCH, stepIndex = 0)

		assertEquals("abc", failure.expected)
		assertEquals("ac", failure.actual)
		assertContains(failure.message, "\"abc\"")
		assertContains(failure.message, "\"ac\"")
	}

	@Test
	fun enterText_inASecureField_isNotReadBack() {
		val fake = driver(field to FakeElement(text = ""))
		fake.typing = { "•".repeat(it.length) }

		assertPassed(fake.run(enter("secret", secure = true)))
		assertTrue("readText" !in fake.calls)
	}

	@Test
	fun enterText_onAMissingField_timesOut() {
		assertFailed(driver().run(enter("a")), FailureKind.STEP_TIMEOUT, stepIndex = 0)
	}

	@Test
	fun clearTextAndFinishTextEntry_failWhenRefused() {
		val fake = driver(field to FakeElement(text = "abc"))

		assertPassed(fake.run(Step.ClearText(field), Step.FinishTextEntry()))
		assertEquals("", fake.screen.getValue(field).text)

		fake.finishResult = false
		assertFailed(fake.run(Step.FinishTextEntry()), FailureKind.ASSERTION)
		assertFailed(driver().run(Step.ClearText(field)), FailureKind.STEP_TIMEOUT)
	}

	@Test
	fun waitVisible_passesWhenTheElementAppearsInTimeAndFailsOtherwise() {
		val late = driver(button to FakeElement(visible = false, appearsAfterMs = 500))

		assertPassed(late.run(Step.WaitVisible(button, 1_000)))
		assertFailed(
			driver(button to FakeElement(visible = false, appearsAfterMs = 5_000)).run(Step.WaitVisible(button, 1_000)),
			FailureKind.STEP_TIMEOUT
		)
	}

	@Test
	fun waitGone_passesForAbsentElementsAndFailsForOnesThatStay() {
		assertPassed(driver().run(Step.WaitGone(button, 1_000)))
		assertFailed(driver(button to FakeElement()).run(Step.WaitGone(button, 1_000)), FailureKind.STEP_TIMEOUT)
	}

	@Test
	fun waitAnyVisible_passesWithAnyOfTheQueriesAndFailsWithNone() {
		val other = Query.Tag("other")

		assertPassed(driver(other to FakeElement()).run(Step.WaitAnyVisible(listOf(button, other), 1_000)))
		assertFailed(driver().run(Step.WaitAnyVisible(listOf(button, other), 1_000)), FailureKind.STEP_TIMEOUT)
	}

	@Test
	fun assertEnabled_checksTheEnabledStateInBothDirections() {
		val enabled = driver(button to FakeElement(enabled = true))
		val disabled = driver(button to FakeElement(enabled = false))

		assertPassed(enabled.run(Step.AssertEnabled(button, true, 1_000)))
		assertPassed(disabled.run(Step.AssertEnabled(button, false, 1_000)))
		assertFailed(disabled.run(Step.AssertEnabled(button, true, 1_000)), FailureKind.STEP_TIMEOUT)
		assertFailed(driver().run(Step.AssertEnabled(button, false, 1_000)), FailureKind.STEP_TIMEOUT)
	}

	@Test
	fun swipe_failsWhenTheDriverRefusesIt() {
		val fake = driver()
		assertPassed(fake.run(Step.Swipe(null, 0.5, 0.8, 0.0, -0.4, 300)))

		fake.swipeResult = false
		assertFailed(fake.run(Step.Swipe(null, 0.5, 0.8, 0.0, -0.4, 300)), FailureKind.ASSERTION)
	}

	@Test
	fun scrollUntilVisible_swipesUntilTheElementShowsUp() {
		val fake = driver(button to FakeElement(hiddenUntilSwipes = 3))

		assertPassed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 20_000)))
		assertEquals(3, fake.swipes)
	}

	@Test
	fun scrollUntilVisible_keepsScrollingWhileTheElementIsOutsideTheComfortZone() {
		val fake = driver(button to FakeElement(bounds = FakeElement.NEAR_BOTTOM))
		fake.onSwipe = { fake.screen.getValue(button).bounds = FakeElement.CENTERED }

		assertPassed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 20_000)))
		assertEquals(1, fake.swipes)
	}

	@Test
	fun scrollUntilVisible_passesWhenTheElementIsTheLastOneAndFurtherScrollingDoesNotMoveIt() {
		val fake = driver(button to FakeElement(bounds = FakeElement.NEAR_BOTTOM))

		assertPassed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 20_000)))
		assertTrue(fake.swipes in 1..2, "swipes were ${fake.swipes}")
	}

	@Test
	fun scrollUntilVisible_passesForAFixedElementJustOutsideTheComfortZone() {
		val fixed = ElementBounds(450.0, 1560.0, 550.0, 1670.0)
		val fake = driver(button to FakeElement(bounds = fixed))

		assertPassed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 20_000)))
		assertTrue(fake.swipes in 1..2, "swipes were ${fake.swipes}")
	}

	@Test
	fun scrollUntilVisible_keepsScrollingWhileTheElementStillMovesAndStopsAtTheEnd() {
		val fake = driver(button to FakeElement(bounds = FakeElement.NEAR_BOTTOM))
		fake.onSwipe = { count ->
			if (count <= 2) {
				val current = fake.screen.getValue(button).bounds
				fake.screen.getValue(button).bounds =
					ElementBounds(current.left, current.top - 40, current.right, current.bottom - 40)
			}
		}

		assertPassed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 20_000)))
		assertEquals(3, fake.swipes)
	}

	@Test
	fun scrollUntilVisible_doesNotAcceptAnElementThatKeepsMovingNearTheEdge() {
		val fake = driver(button to FakeElement(bounds = FakeElement.NEAR_BOTTOM, drift = 3.0))

		assertFailed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 2_000)), FailureKind.STEP_TIMEOUT)
		assertTrue(fake.swipes in 1..20, "swipes were ${fake.swipes}")
	}

	@Test
	fun scrollUntilVisible_terminatesWhenTheElementNeverShowsUp() {
		val fake = driver()

		val failure =
			assertFailed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 2_000)), FailureKind.STEP_TIMEOUT)

		assertContains(failure.message, "did not scroll into view")
		assertTrue(fake.swipes in 1..20, "swipes were ${fake.swipes}")
	}

	@Test
	fun scrollUntilVisible_failsWhenTheDriverRefusesTheSwipe() {
		val fake = driver().apply { swipeResult = false }

		assertFailed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 2_000)), FailureKind.ASSERTION)
	}

	@Test
	fun settle_passesOnAStillElementAndTimesOutOnAMovingOne() {
		assertPassed(driver(button to FakeElement()).run(Step.Settle(button, 2_000)))
		assertPassed(driver().run(Step.Settle(null, 2_000)))
		assertFailed(driver(button to FakeElement(drift = 3.0)).run(Step.Settle(button, 2_000)), FailureKind.STEP_TIMEOUT)
	}

	@Test
	fun relaunchAndForeground_keepStateAndFailWhenTheAppDoesNotComeBack() {
		val fake = driver()

		assertPassed(fake.run(Step.Relaunch(mapOf("KEY" to "value")), Step.Foreground()))
		assertEquals(mapOf("KEY" to "value"), fake.launches.last().arguments)
		assertTrue(fake.launches.last().mockStates.isEmpty())

		fake.launchResults = ArrayDeque(listOf(true, false))
		assertFailed(fake.run(Step.Relaunch(emptyMap())), FailureKind.APP_NOT_RUNNING, stepIndex = 0)

		fake.foregroundResult = false
		assertFailed(fake.run(Step.Foreground()), FailureKind.APP_NOT_RUNNING, stepIndex = 0)
	}
}
