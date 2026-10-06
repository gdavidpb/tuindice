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
				"disabled-element", "text-entry", "foreground", "backend"
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
