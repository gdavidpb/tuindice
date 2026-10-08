package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FailureKindsTest {
	private val button: Query = Query.Tag("button")
	private val field: Query = Query.Tag("field")

	@Test
	fun disabledTap_isAnAssertion() {
		val fake = FakeDriver().apply { screen[button] = FakeElement(enabled = false) }

		assertFailed(fake.run(Step.Tap(button)), FailureKind.ASSERTION)
	}

	@Test
	fun waitForAMissingElement_isAStepTimeout() {
		assertFailed(FakeDriver().run(Step.WaitVisible(button, 1_000)), FailureKind.STEP_TIMEOUT)
	}

	@Test
	fun rereadingOtherText_isATypedTextMismatch() {
		val fake = FakeDriver().apply {
			screen[field] = FakeElement(text = "")
			typing = { it.dropLast(1) }
		}

		assertFailed(
			fake.run(Step.EnterText(field, "abc", null, false, false)),
			FailureKind.TYPED_TEXT_MISMATCH
		)
	}

	@Test
	fun aDriverThatThrows_isADriverErrorAtThatStep() {
		val fake = FakeDriver().apply {
			screen[button] = FakeElement()
			throwOn = "tap"
		}

		val failure = assertFailed(fake.run(Step.Back(), Step.Tap(button)), FailureKind.DRIVER_ERROR, stepIndex = 1)

		assertContains(failure.message, "scripted driver failure in tap")
	}

	@Test
	fun aFailedStepWhileTheAppIsNotInTheForeground_isAppNotRunning() {
		val fake = FakeDriver().apply { inForeground = false }

		assertFailed(fake.run(Step.Tap(button)), FailureKind.APP_NOT_RUNNING)
	}

	@Test
	fun backendAndSystemSteps_neverBecomAppNotRunning() {
		val fake = FakeDriver().apply { inForeground = false }
		val system = Query.System("Cancel")

		assertFailed(fake.run(Step.ExpectRequest("POST", "/x", null, 200)), FailureKind.STEP_TIMEOUT)
		assertFailed(fake.run(Step.WaitVisible(system, 200)), FailureKind.STEP_TIMEOUT)
	}

	@Test
	fun aFailedStepWithASystemDialogInFront_isASystemDialog() {
		val fake = FakeDriver().apply { dialog = "App isn't responding" }

		val failure = assertFailed(fake.run(Step.Tap(button)), FailureKind.SYSTEM_DIALOG)

		assertContains(failure.message, "App isn't responding")
	}

	@Test
	fun aSystemDialogWinsOverTheAppLeavingTheForeground() {
		val fake = FakeDriver().apply {
			dialog = "ANR"
			inForeground = false
		}

		assertFailed(fake.run(Step.Tap(button)), FailureKind.SYSTEM_DIALOG)
	}

	@Test
	fun aTypedTextMismatch_isNotHiddenByAnEnvironmentKind() {
		val fake = FakeDriver().apply {
			screen[field] = FakeElement(text = "")
			typing = { "" }
			inForeground = false
		}

		assertFailed(
			fake.run(Step.EnterText(field, "abc", null, false, false)),
			FailureKind.TYPED_TEXT_MISMATCH
		)
	}

	@Test
	fun aFailedRun_capturesEvidenceAtTheFailingStepOnly() {
		val fake = FakeDriver().apply { screen[button] = FakeElement() }

		assertPassed(fake.run(Step.Tap(button)))
		assertTrue(fake.captured.isEmpty())

		fake.run(Step.Tap(button), Step.WaitVisible(Query.Tag("missing"), 100))
		assertEquals(listOf("test-scenario" to 1), fake.captured)
	}
}
