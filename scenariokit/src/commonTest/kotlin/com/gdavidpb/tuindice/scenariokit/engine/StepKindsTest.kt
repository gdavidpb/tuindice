package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenariokit.codec.sampleCatalog
import com.gdavidpb.tuindice.scenariokit.codec.sampleScenario
import com.gdavidpb.tuindice.scenariokit.driver.ElementBounds
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.StepOutcome
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
		replace: Boolean = false
	) = Step.EnterText(field, text, expect, secure, replace)

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
	fun enterText_inASecureField_isJudgedByItsLength() {
		val fake = driver(field to FakeElement(text = ""))
		fake.typing = { "•".repeat(it.length) }

		assertPassed(fake.run(enter("secret", secure = true)))
		assertContains(fake.calls, "readText")
	}

	@Test
	fun enterText_inASecureField_thatLostACharacter_isATypedTextMismatchWithoutTheText() {
		val fake = driver(field to FakeElement(text = ""))
		fake.typing = { "•".repeat(it.length - 1) }

		val failure = assertFailed(fake.run(enter("secret", secure = true)), FailureKind.TYPED_TEXT_MISMATCH, stepIndex = 0)

		assertEquals("6 characters", failure.expected)
		assertEquals("5 characters", failure.actual)
		assertContains(failure.message, "typed 6 characters")
		assertTrue("secret" !in failure.message)
	}

	@Test
	fun enterText_whenTheDriverRefusesAfterPartOfTheText_isATypedTextMismatchWithHowManyWentIn() {
		val fake = driver(field to FakeElement(text = ""))
		fake.typing = { it.take(3) }
		fake.keysAccepted = false

		val failure = assertFailed(fake.run(enter("abcdefg")), FailureKind.TYPED_TEXT_MISMATCH, stepIndex = 0)

		assertContains(failure.message, "3 of 7 characters went in")
		assertEquals("abcdefg", failure.expected)
		assertEquals("abc", failure.actual)
	}

	@Test
	fun enterText_whenTheDriverRefusesAndTheFieldIsEmpty_isAnAssertionNotACorruptedText() {
		val fake = driver(field to FakeElement(text = ""))
		fake.typing = { "" }
		fake.keysAccepted = false

		val failure = assertFailed(fake.run(enter("abc")), FailureKind.ASSERTION, stepIndex = 0)

		assertContains(failure.message, "was refused")
	}

	@Test
	fun enterText_whenTheDriverRefusesASecureFieldPartway_countsTheCharactersThatWentIn() {
		val fake = driver(field to FakeElement(text = ""))
		fake.typing = { "•".repeat(4) }
		fake.keysAccepted = false

		val failure = assertFailed(fake.run(enter("secret", secure = true)), FailureKind.TYPED_TEXT_MISMATCH)

		assertContains(failure.message, "4 of 6 characters went in")
		assertTrue("secret" !in failure.message)
	}

	@Test
	fun enterText_acceptsATextThatTheFieldShowsLate() {
		val fake = driver(field to FakeElement(text = ""))
		fake.screen.getValue(field).scriptedReads = mutableListOf("", "", "abc", "abc")

		assertPassed(fake.run(enter("abc")))
	}

	@Test
	fun enterText_doesNotAcceptATextThatChangesRightAfterItMatched() {
		val fake = driver(field to FakeElement(text = ""))
		fake.screen.getValue(field).scriptedReads = mutableListOf("abc", "abd")

		val failure = assertFailed(fake.run(enter("abc")), FailureKind.TYPED_TEXT_MISMATCH, stepIndex = 0)

		assertEquals("abd", failure.actual)
	}

	@Test
	fun enterText_whenTheTextFlipsBetweenReads_failsSayingItDidNotHold() {
		val fake = driver(field to FakeElement(text = ""))
		// 16 reads fit in the 3 s window at 200 ms; the last one matches, but it is not preceded by a match.
		fake.screen.getValue(field).scriptedReads = MutableList(16) { if (it % 2 == 0) "abd" else "abc" }

		val failure = assertFailed(fake.run(enter("abc")), FailureKind.TYPED_TEXT_MISMATCH)

		assertContains(failure.message, "did not hold as typed")
	}

	@Test
	fun enterText_onAMissingField_timesOut() {
		assertFailed(driver().run(enter("a")), FailureKind.STEP_TIMEOUT, stepIndex = 0)
	}

	@Test
	fun submitTextEntry_failsWhenTheDriverCannotSendTheAction() {
		val fake = driver(field to FakeElement(text = "abc"))

		assertPassed(fake.run(Step.SubmitTextEntry()))

		fake.submitResult = false
		assertFailed(fake.run(Step.SubmitTextEntry()), FailureKind.ASSERTION)
	}

	@Test
	fun aRefusedGestureOrTyping_carriesTheReasonTheDriverGave() {
		val reason = "frame still moving after 20 reads in 2.1 s"
		val far: Query = Query.Tag("far")
		val fake = driver(button to FakeElement(), field to FakeElement(text = ""), far to FakeElement(hiddenUntilSwipes = 3))
		fake.refusal = reason

		listOf(
			Step.Tap(button) to "tap on tag:button was refused",
			Step.TapAt(button, 0.5, 0.5) to "tapAt tag:button was refused",
			Step.DoubleTap(button) to "doubleTap tag:button was refused",
			Step.Swipe(null, 0.5, 0.8, 0.0, -0.4, 300) to "swipe from screen was refused",
			Step.Back() to "back was not handled",
			enter("abc") to "typing into tag:field was refused",
			enter("abc", replace = true) to "clearText tag:field was refused",
			Step.SubmitTextEntry() to "the IME action could not be sent",
			Step.ScrollUntilVisible(far, Scroll.ContentDown, 2_000) to "the driver refused the scroll swipe"
		).forEach { (step, what) ->
			val failure = assertFailed(fake.run(step), FailureKind.ASSERTION)
			assertEquals("$what: $reason", failure.message, step.toString())
		}
	}

	@Test
	fun aRefusalWithoutAReason_keepsTheShortMessage() {
		val fake = driver(button to FakeElement())
		fake.swipeResult = false

		val failure = assertFailed(fake.run(Step.Swipe(null, 0.5, 0.8, 0.0, -0.4, 300)), FailureKind.ASSERTION)

		assertEquals("swipe from screen was refused", failure.message)
	}

	@Test
	fun aDriverWhoseRefusalReasonThrows_stillFailsTheStep() {
		val fake = driver(button to FakeElement())
		val throwing = object : com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver by fake {
			override fun swipe(
				from: Query?,
				vector: com.gdavidpb.tuindice.scenariokit.driver.SwipeVector,
				durationMs: Long
			) = false

			override fun lastRefusal(): String = error("no reason to give")
		}

		val swipe = Step.Swipe(null, 0.5, 0.8, 0.0, -0.4, 300)
		val catalog = CatalogCodec.encode(sampleCatalog(listOf(sampleScenario("a-fail", steps = listOf(swipe)))))

		val outcome = ScenarioRunner.runWith(catalog, "a-fail", throwing, fake.clocks)

		assertFailed(outcome, FailureKind.ASSERTION, stepIndex = 0)
	}

	@Test
	fun mockState_setsTheStateWithTheSameRequestAsTheStartOfTheScenario() {
		val fake = driver(button to FakeElement())

		assertPassed(fake.run(Step.Tap(button), Step.SetMockState("login-token-lifecycle", "Reissued"), Step.Tap(button)))

		assertEquals("Reissued", fake.backend.states["login-token-lifecycle"])
		assertEquals(
			1,
			fake.backend.calls.count { it == "PUT /__admin/scenarios/login-token-lifecycle/state" },
			"the mid-scenario state is a single PUT of the same path prepareBackend uses"
		)
		assertTrue(fake.calls.indexOf("tap") < fake.calls.lastIndexOf("tap"))
	}

	@Test
	fun mockState_aStateThatIsNotAccepted_isBackendUnavailableAtThatStep() {
		val fake = driver(button to FakeElement())
		fake.backend.failingPath = "/__admin/scenarios/login-token-lifecycle/state"

		val failure = assertFailed(
			fake.run(Step.Tap(button), Step.SetMockState("login-token-lifecycle", "Reissued")),
			FailureKind.BACKEND_UNAVAILABLE,
			stepIndex = 1
		)

		assertContains(failure.message, "PUT /__admin/scenarios/login-token-lifecycle/state answered 503")
	}

	@Test
	fun mockState_isNotBlamedOnTheAppWhenTheAppIsInTheBackground() {
		val fake = driver(button to FakeElement())
		fake.backend.failingPath = "/__admin/scenarios/other/state"
		fake.inForeground = false

		assertFailed(fake.run(Step.SetMockState("other", "X")), FailureKind.BACKEND_UNAVAILABLE, stepIndex = 0)
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
	fun scrollUntilVisible_passesWhenTheListBouncesAFewPointsAtItsEnd() {
		val fake = driver(button to FakeElement(bounds = FakeElement.NEAR_BOTTOM))
		fake.onSwipe = { count ->
			val bounce = if (count % 2 == 0) 20 else -20
			val current = fake.screen.getValue(button).bounds
			fake.screen.getValue(button).bounds =
				ElementBounds(current.left, current.top + bounce, current.right, current.bottom + bounce)
		}

		assertPassed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 20_000)))
		assertTrue(fake.swipes in 1..3, "swipes were ${fake.swipes}")
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
					ElementBounds(current.left, current.top - 100, current.right, current.bottom - 100)
			}
		}

		assertPassed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 20_000)))
		assertEquals(3, fake.swipes)
	}

	@Test
	fun scrollUntilVisible_doesNotAcceptAnElementThatKeepsMovingNearTheEdge() {
		val fake = driver(button to FakeElement(bounds = FakeElement.NEAR_BOTTOM))
		fake.onSwipe = { count ->
			val shift = if (count % 2 == 0) 150 else -150
			val current = fake.screen.getValue(button).bounds
			fake.screen.getValue(button).bounds =
				ElementBounds(current.left, current.top + shift, current.right, current.bottom + shift)
		}

		assertFailed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 2_000)), FailureKind.STEP_TIMEOUT)
		assertTrue(fake.swipes in 1..20, "swipes were ${fake.swipes}")
	}

	@Test
	fun scrollUntilVisible_doesNotAcceptAnElementWhosePositionCannotBeRead() {
		val fake = driver(button to FakeElement(unreadableBounds = true))

		val failure =
			assertFailed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 2_000)), FailureKind.STEP_TIMEOUT)

		assertContains(failure.message, "did not scroll into view")
		assertTrue(fake.swipes >= 1, "it kept scrolling instead of passing blind; swipes were ${fake.swipes}")
	}

	@Test
	fun scrollUntilVisible_passesOnceTheElementCanBePlaced() {
		val fake = driver(button to FakeElement(unreadableBounds = true))
		fake.onSwipe = { count -> if (count == 2) fake.screen.getValue(button).unreadableBounds = false }

		assertPassed(fake.run(Step.ScrollUntilVisible(button, Scroll.ContentDown, 20_000)))
		assertEquals(2, fake.swipes)
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
	fun relaunch_withoutArguments_startsTheAppWithTheArgumentsOfTheScenarioStart() {
		val fake = driver()
		val start = LaunchSpec(mapOf("DISABLE_ANIMATIONS" to "true", "NETWORK_AVAILABLE" to "true"))

		assertPassed(fake.run(scenarioOf(Step.Relaunch(emptyMap()), start = start)))
		assertEquals(start.arguments, fake.launches.last().arguments)

		assertPassed(fake.run(scenarioOf(Step.Relaunch(mapOf("OTHER" to "1")), start = start)))
		assertEquals(mapOf("OTHER" to "1"), fake.launches.last().arguments)
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
