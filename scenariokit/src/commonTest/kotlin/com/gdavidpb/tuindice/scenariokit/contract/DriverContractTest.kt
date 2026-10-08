package com.gdavidpb.tuindice.scenariokit.contract

import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenariokit.codec.sampleCatalog
import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
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
				"disabled-element", "text-entry", "foreground", "backend", "terminated-app"
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

	private fun masked(fake: FakeDriver) = object : ScenarioDriver by fake {
		override fun readText(q: Query) = fake.readText(q)?.let { if (it.length > 2) it.take(2) + "-" + it.drop(2) else it }
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

		assertEquals(10, outcome.steps.size)
		val failed = outcome.steps.filter { it.outcome.wire == "failed" }.map { it.primitive }
		assertEquals(listOf("absent-element", "text-entry", "terminated-app"), failed)
		assertContains(outcome.report, "3 of 10 checks")
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
