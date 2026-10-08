package com.gdavidpb.tuindice.scenariorunner

import androidx.test.platform.app.InstrumentationRegistry
import com.gdavidpb.tuindice.scenariorunner.driver.UiAutomatorScenarioDriver
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
			"the field holds all 30 characters",
			driver.session.poll(READ_MS) { driver.readText(app.password)?.length == ProbeApp.SAMPLE_LENGTH }
		)
		assertTrue(
			app.log("long-typing")
				.contains("${ProbeApp.SAMPLE_EVENTS} of ${ProbeApp.SAMPLE_EVENTS} key events injected for 30 characters")
		)
	}

	/**
	 * ZB-3, the regression probe of the extra character ("v" in the password field): `typeKeys` never touches the screen.
	 * With the field without the focus, `typeKeys` asks for it with the accessibility click action and logcat has no
	 * `Clicking on` (the line `UiDevice.click` writes for a touch on a coordinate) since the probe began. Red if a click
	 * by coordinate returns to the focus path (verified by putting one in `FieldFocus`).
	 */
	@Test
	fun typeKeysOnAFieldWithoutFocusDoesNotClickOnTheScreen() {
		val driver = app.begin("no-click-without-focus")
		driver.session.shell("logcat -c")

		assertTrue(driver.typeKeys(app.password, ProbeApp.SHORT_SAMPLE))

		assertTrue(app.log("no-click-without-focus").contains("does not have the focus"))
		assertEquals("typeKeys put a click on the screen: ${clicks(driver)}", 0, clicks(driver).size)
	}

	/**
	 * ZB-3, the other half: after a `tap` that gave the field the focus and listed the keyboard, `typeKeys` finds the
	 * focus already there and leaves the screen alone, so the only `Clicking on` since the probe began is the tap's.
	 */
	@Test
	fun typeKeysAfterATapAddsNoClickToTheOneOfTheTap() {
		val driver = app.begin("no-click-after-tap")
		driver.session.shell("logcat -c")

		assertTrue(driver.tap(app.password))
		assertTrue("the keyboard must show", driver.session.poll(READ_MS) { driver.session.keyboard.frame() != null })
		assertTrue(driver.typeKeys(app.password, ProbeApp.SHORT_SAMPLE))

		assertTrue(app.log("no-click-after-tap").contains("already has the focus"))
		assertEquals("the tap clicked once and typeKeys never: ${clicks(driver)}", 1, clicks(driver).size)
	}

	/** The `Clicking on` lines of `UiDevice` since logcat was last cleared. */
	private fun clicks(driver: UiAutomatorScenarioDriver): List<String> =
		driver.session.shell("logcat -d -s UiDevice").lines().filter { it.contains("Clicking on") }

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
