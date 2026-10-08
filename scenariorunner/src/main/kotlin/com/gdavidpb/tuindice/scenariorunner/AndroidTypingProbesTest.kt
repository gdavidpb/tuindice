package com.gdavidpb.tuindice.scenariorunner

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Typing probes of the Android driver (B-2); run by class on a cleared app, like [AndroidDriverProbesTest]. */
class AndroidTypingProbesTest {
	private val app = ProbeApp()

	/** A 30-character run (longer than any fixture password) enters completely and says how many keys went in. */
	@Test
	fun aThirtyCharacterTextIsTypedCompletely() {
		val driver = app.begin("long-typing")

		assertTrue(driver.typeKeys(app.password, ProbeApp.PASSWORD_SAMPLE))
		assertTrue(
			app.log("long-typing")
				.contains("${ProbeApp.SAMPLE_EVENTS} of ${ProbeApp.SAMPLE_EVENTS} key events injected for 30 characters")
		)
	}

	/**
	 * With `-e typingSeries N`, types 30 characters into the password field N times and counts how many enter
	 * completely (the field reads back 30 bullets). The result is the last line of the probe's `driver.log`.
	 */
	@Test
	fun typingSeries() {
		val runs = InstrumentationRegistry.getArguments().getString("typingSeries")?.toIntOrNull()
		assumeTrue("no -e typingSeries N given", runs != null)
		val driver = app.begin("typing-series")
		var complete = 0

		repeat(checkNotNull(runs)) {
			driver.clearText(app.password)
			val typed = driver.typeKeys(app.password, ProbeApp.PASSWORD_SAMPLE)
			val length = driver.session.poll(READ_MS) { driver.readText(app.password)?.length == ProbeApp.SAMPLE_LENGTH }
			if (typed && length) complete++ else driver.log("series: run $it typed=$typed lengthOk=$length")
		}

		driver.log("series: $complete of $runs complete")
		assertEquals(runs, complete)
	}

	private companion object {
		const val READ_MS = 2_000L
	}
}
