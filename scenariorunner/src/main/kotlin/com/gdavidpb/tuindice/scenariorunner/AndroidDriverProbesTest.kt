package com.gdavidpb.tuindice.scenariorunner

import android.os.SystemClock
import com.gdavidpb.tuindice.scenariokit.driver.SwipeVector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the Android driver must do under the conditions the audit found, probed on the device. The app is driven
 * from its login screen (the contract fixture), so it must start cleared. The harness never runs this class (it
 * filters on the scenario suite): run it with `am instrument -w -e class <this class>`. Each probe is red without
 * the fix it names. Typing probes are in [AndroidTypingProbesTest].
 */
class AndroidDriverProbesTest {
	private val app = ProbeApp()

	/** B-1: with the app gone the screen cannot be read, and "cannot read" must not be "gone". */
	@Test
	fun waitGoneIsFalseWhenTheAppIsNotThereToBeRead() {
		val driver = app.begin("waitgone")
		val startedAt = SystemClock.uptimeMillis()

		assertTrue("not on screen is gone while the app is readable", driver.waitGone(app.never, LONG_MS))
		assertTrue("and it is quick", SystemClock.uptimeMillis() - startedAt < QUICK_MS)
		assertFalse("on screen is not gone", driver.waitGone(app.screen, SHORT_MS))

		driver.terminate()
		assertFalse("nothing can be read from a dead app, so nothing is gone", driver.waitGone(app.screen, LONG_MS))
		assertFalse(driver.waitGone(app.never, LONG_MS))
		assertFalse(driver.isVisible(app.screen))
		assertTrue(app.log("waitgone").contains("accessibility tree cannot be read"))
	}

	/** B-9: a clear that did not clear is false; a label cannot be emptied, so the old unchecked `true` was wrong. */
	@Test
	fun clearTextIsCheckedAgainstTheField() {
		val driver = app.begin("clear")

		assertTrue(driver.typeKeys(app.usbId, app.fixture.textSample))
		assertTrue(driver.session.poll(LONG_MS) { driver.readText(app.usbId) == app.fixture.expectedText })
		assertTrue(driver.clearText(app.usbId))
		assertTrue(driver.readText(app.usbId).isNullOrEmpty())
		assertFalse("a label keeps its text", driver.clearText(app.label))
		assertFalse("an absent field cannot be cleared", driver.clearText(app.never))
	}

	/** B-10 (a): a touch inside the keyboard window would press a key, so it is refused. */
	@Test
	fun aTouchInsideTheOnScreenKeyboardIsRefused() {
		val driver = app.begin("keyboard")
		assertTrue(driver.tap(app.usbId))
		val shown = driver.session.poll(LONG_MS) { driver.session.keyboard.frame() != null }
		assertTrue("the keyboard window must show", shown)
		val keyboard = checkNotNull(driver.session.keyboard.frame())
		val middle = keyboard.centerY() / driver.session.device.displayHeight.toDouble()

		assertFalse(driver.tapAt(null, MIDDLE, middle))
		assertFalse(driver.swipe(null, SwipeVector(MIDDLE, middle, 0.0, UP_SHORT), SWIPE_MS))
		assertEquals(2, app.log("keyboard").lines().count { it.contains("is inside the on-screen keyboard") })
		assertTrue(driver.readText(app.usbId).isNullOrEmpty())
	}

	/** B-10 (b)/(c): a dead process is not revived by foreground(); a live one brought back logs each request. */
	@Test
	fun foregroundDoesNotReviveADeadAppAndLogsEachRequest() {
		val driver = app.begin("foreground")
		driver.session.device.pressHome()
		assertTrue(driver.foreground())
		assertTrue(app.log("foreground").contains("request 1 to bring the app back"))

		driver.terminate()
		assertFalse(driver.foreground())
		assertTrue(app.log("foreground").contains("the app process is not running; it is not started again"))
		assertFalse("the app stays dead", driver.isForeground())
	}

	/** B-11: a line is on disk as soon as it is written, with no close or flush at the end of the scenario. */
	@Test
	fun driverLogIsWrittenLineByLine() {
		val driver = app.begin("driverlog")

		driver.log("[0] first marker")
		assertTrue(app.log("driverlog").contains("[0] first marker"))
		driver.log("[1] second marker")
		assertEquals(2, app.log("driverlog").lines().count { it.contains("marker") })
	}

	/** Every gesture that aims at an element refuses, and logs why, when the element is not on screen. */
	@Test
	fun everyGestureThatAimsAtAnElementLogsItsRefusal() {
		val driver = app.begin("refusals")

		assertFalse(driver.tap(app.never))
		assertFalse(driver.tapAt(app.never, MIDDLE, MIDDLE))
		assertFalse(driver.doubleTap(app.never))
		assertFalse(driver.swipe(app.never, SwipeVector(MIDDLE, MIDDLE, 0.0, UP_LONG), SWIPE_MS))
		assertFalse(driver.typeKeys(app.never, "a"))
		assertEquals(REFUSING_GESTURES, app.log("refusals").lines().count { it.contains("gesture refused") })
		assertTrue(app.log("refusals").contains("typeKeys"))
	}

	private companion object {
		const val REFUSING_GESTURES = 4
		const val MIDDLE = 0.5
		const val UP_SHORT = -0.1
		const val UP_LONG = -0.2
		const val SHORT_MS = 500L
		const val LONG_MS = 2_000L
		const val QUICK_MS = 1_500L
		const val SWIPE_MS = 300L
	}
}
