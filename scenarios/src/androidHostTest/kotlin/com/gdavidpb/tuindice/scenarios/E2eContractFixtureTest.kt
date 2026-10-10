package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.catalog.E2eContractFixture
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** What `DriverContractFixture` promises about its samples, fixed where the values live. */
class E2eContractFixtureTest {
	private val fixture = E2eContractFixture.fixture

	/** The placeholder the USB-ID field shows when it is empty. */
	private val placeholder = "12-34567"

	@Test
	fun secureSampleIsLongEnoughToCatchADriverThatDropsOrDoublesAKeyOfALongRun() {
		val length = fixture.secureSample.length

		assertTrue(length >= MIN_SECURE_SAMPLE, "the secure sample has $length characters")
	}

	@Test
	fun textSampleIsNotThePlaceholderOfTheField() {
		assertNotEquals(placeholder, fixture.textSample)
		assertNotEquals(placeholder, fixture.expectedText)
	}

	private companion object {
		const val MIN_SECURE_SAMPLE = 25
	}
}
