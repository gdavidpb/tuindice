package com.gdavidpb.tuindice.scenariokit.contract

import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenariokit.codec.sampleCatalog
import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.driver.SwipeVector
import com.gdavidpb.tuindice.scenariokit.engine.FakeDriver
import com.gdavidpb.tuindice.scenariokit.engine.FakeElement
import com.gdavidpb.tuindice.scenariokit.engine.ScenarioRunner
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DriverContractTest {
	private val catalog = CatalogCodec.encode(sampleCatalog())

	private fun conformant() = FakeDriver().apply {
		screen[Query.Tag("present")] = FakeElement()
		screen[Query.Tag("disabled")] = FakeElement(enabled = false)
		screen[Query.Tag("field")] = FakeElement(text = "")
		screen[Query.Tag("secret")] = FakeElement(text = "")
	}

	private fun contract(driver: ScenarioDriver, fake: FakeDriver) = ScenarioRunner.driverContractWith(
		catalog,
		driver,
		fake.clocks
	)

	@Test
	fun aConformantDriver_passesEveryProbe() {
		val fake = conformant()

		val outcome = contract(fake, fake)

		assertNull(outcome.failure, outcome.report)
		assertEquals(
			listOf(
				"launch", "present-element", "absent-element", "absent-wait-timing", "gone-wait-on-present",
				"gone-wait-on-absent", "refusals-carry-a-reason", "disabled-element", "text-entry", "positive-gestures",
				"long-secure-typing", "keyboard-guard", "submit-text-entry", "foreground", "backend", "back",
				"terminated-app"
			),
			outcome.steps.map { it.primitive }
		)
	}

	@Test
	fun aDriverThatTapsAbsentElements_failsTheNegativeProbe() {
		val fake = conformant()
		val tapper = object : ScenarioDriver by fake {
			override fun tap(q: Query) = true
		}

		val failure = assertNotNull(contract(tapper, fake).failure)

		assertEquals("absent-element", failure.primitive)
		assertContains(failure.message, "tap returned true")
	}

	@Test
	fun aDriverThatTouchesAnAppThatIsGone_failsTheTerminatedAppProbe() {
		val fake = conformant()
		val blind = object : ScenarioDriver by fake {
			override fun tap(q: Query) = fake.terminated || fake.tap(q)
		}

		val failure = assertNotNull(contract(blind, fake).failure)

		assertEquals("terminated-app", failure.primitive)
		assertContains(failure.message, "tap returned true with the app terminated")
	}

	@Test
	fun aDriverThatCallsADeadAppRunning_failsTheTerminatedAppProbe() {
		val fake = conformant()
		val optimistic = object : ScenarioDriver by fake {
			override fun isRunning() = true
		}

		val failure = assertNotNull(contract(optimistic, fake).failure)

		assertEquals("terminated-app", failure.primitive)
		assertContains(failure.message, "isRunning is true with the app terminated")
	}

	@Test
	fun aDriverThatReadsAGoneAppAsGone_failsTheTerminatedAppProbe() {
		val fake = conformant()
		val credulous = object : ScenarioDriver by fake {
			override fun waitGone(q: Query, timeoutMs: Long) = fake.terminated || fake.waitGone(q, timeoutMs)
		}

		val failure = assertNotNull(contract(credulous, fake).failure)

		assertContains(failure.message, "waitGone returned true although nothing can be read from a dead app")
	}

	@Test
	fun aDriverThatIgnoresTimeouts_failsTheTimingProbe() {
		val fake = conformant()
		val impatient = object : ScenarioDriver by fake {
			override fun waitVisible(q: Query, timeoutMs: Long) = fake.isVisible(q)
		}

		val failure = assertNotNull(contract(impatient, fake).failure)

		assertEquals("absent-wait-timing", failure.primitive)
		assertContains(failure.message, "before its 600 ms timeout")
	}

	@Test
	fun aDriverThatCorruptsTypedText_failsTheTextProbe() {
		val fake = conformant().apply { typing = { it.drop(1) } }

		val failure = assertNotNull(contract(fake, fake).failure)

		assertEquals("text-entry", failure.primitive)
	}

	@Test
	fun aDriverThatLetsATouchThroughTheKeyboard_failsTheKeyboardGuardProbe() {
		val fake = conformant()
		val careless = object : ScenarioDriver by fake {
			override fun tapAt(q: Query?, fx: Double, fy: Double) = true
		}

		val failure = assertNotNull(contract(careless, fake).failure)

		assertContains(failure.message, "keyboard-guard: tapAt on the keyboard returned true")
	}

	@Test
	fun aDriverThatSwipesFromTheKeyboard_failsTheKeyboardGuardProbe() {
		val fake = conformant()
		val careless = object : ScenarioDriver by fake {
			override fun swipe(from: Query?, vector: SwipeVector, durationMs: Long) = true
		}

		val failure = assertNotNull(contract(careless, fake).failure)

		assertContains(failure.message, "keyboard-guard: a swipe that starts on the keyboard returned true")
	}

	@Test
	fun aDriverThatRefusesWithoutAReason_failsTheReasonProbe() {
		val fake = conformant()
		val mute = object : ScenarioDriver by fake {
			override fun lastRefusal(): String? = null
		}

		val failure = assertNotNull(contract(mute, fake).failure)

		assertContains(failure.message, "refusals-carry-a-reason: tap refused absent without a reason")
	}

	@Test
	fun aDriverWhoseWaitGoneNeverSeesAnAbsentElement_failsTheGoneProbe() {
		val fake = conformant()
		val blind = object : ScenarioDriver by fake {
			override fun waitGone(q: Query, timeoutMs: Long) = false
		}

		val failures = assertNotNull(contract(blind, fake).failure).message

		assertContains(failures, "gone-wait-on-absent: waitGone returned false for absent")
	}

	@Test
	fun aDriverWhoseGesturesNeverWork_failsThePositiveProbe() {
		val fake = conformant()
		val dead = object : ScenarioDriver by fake {
			override fun tap(q: Query) = false
		}

		val failure = assertNotNull(contract(dead, fake).failure)

		assertContains(failure.message, "positive-gestures: tap returned false for the text field")
	}

	@Test
	fun aDriverThatDropsAKeyOfALongRun_failsTheLongTypingProbe() {
		val fake = conformant().apply { typing = { if (it.length >= 25) it.dropLast(1) else it } }

		val failure = assertNotNull(contract(fake, fake).failure)

		assertContains(failure.message, "long-secure-typing: typeKeys of 25 characters left 24")
	}

	@Test
	fun aDriverWithoutSubmitOrBack_failsThoseProbes() {
		val fake = conformant().apply {
			submitResult = false
			backResult = false
		}

		val failure = assertNotNull(contract(fake, fake).failure)

		assertContains(failure.message, "submit-text-entry: submitTextEntry answered false")
		assertContains(failure.message, "back: pressBack answered false on Android")
	}

	private fun masked(fake: FakeDriver) = object : ScenarioDriver by fake {
		override fun readText(q: Query) = fake.readText(q)?.let {
			if (q == Query.Tag("field") && it.length > 2) it.take(2) + "-" + it.drop(2) else it
		}
	}

	private fun maskedCatalog(expected: String) = CatalogCodec.encode(
		sampleCatalog().let {
			it.copy(contractFixture = it.contractFixture.copy(textSample = "1234567", expectedText = expected))
		}
	)

	@Test
	fun aFieldThatTransformsTheText_passesWhenTheExpectationIsTheTransformedText() {
		val fake = conformant()

		val outcome = ScenarioRunner.driverContractWith(maskedCatalog("12-34567"), masked(fake), fake.clocks)

		assertNull(outcome.failure, outcome.report)
	}

	@Test
	fun aFieldThatTransformsTheText_failsWhenTheExpectationIsTheRawText() {
		val fake = conformant()

		val failure = assertNotNull(
			ScenarioRunner.driverContractWith(maskedCatalog("1234567"), masked(fake), fake.clocks).failure
		)

		assertEquals("text-entry", failure.primitive)
		assertContains(failure.message, "\"12-34567\"")
		assertContains(failure.message, "\"1234567\"")
	}

	@Test
	fun aFailingCheck_doesNotStopTheRest() {
		val fake = conformant().apply { typing = { it.drop(1) } }
		val impatient = object : ScenarioDriver by fake {
			override fun tap(q: Query) = true
		}

		val outcome = contract(impatient, fake)

		assertEquals(17, outcome.steps.size)
		val failed = outcome.steps.filter { it.outcome.wire == "failed" }.map { it.primitive }
		assertEquals(
			listOf("absent-element", "refusals-carry-a-reason", "text-entry", "long-secure-typing", "terminated-app"),
			failed
		)
		assertContains(outcome.report, "5 of 17 checks")
		assertContains(assertNotNull(outcome.failure).message, "text-entry:")
	}

	@Test
	fun aDriverThatThrows_isADriverError() {
		val fake = conformant().apply { throwOn = "isVisible" }

		val failure = assertNotNull(contract(fake, fake).failure)

		assertEquals(FailureKind.DRIVER_ERROR, failure.kind)
	}

	@Test
	fun theDriverContractOfAnUnreadableCatalog_isCrashedNotThrown() {
		val fake = conformant()

		val outcome = ScenarioRunner.driverContract("garbage", fake)

		assertEquals(FailureKind.DRIVER_ERROR, assertNotNull(outcome.failure).kind)
	}
}
