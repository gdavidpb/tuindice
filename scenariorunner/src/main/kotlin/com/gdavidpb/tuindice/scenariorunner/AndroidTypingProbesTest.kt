package com.gdavidpb.tuindice.scenariorunner

import androidx.test.platform.app.InstrumentationRegistry
import com.gdavidpb.tuindice.scenariorunner.driver.GuardVerdict
import com.gdavidpb.tuindice.scenariorunner.driver.SwipeTrack
import com.gdavidpb.tuindice.scenariorunner.driver.UiAutomatorScenarioDriver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Typing probes of the Android driver (B-2), the verdict of its keyboard guard, which only the typing makes matter
 * (YB-3), and the track of a swipe (E3); run by class on a cleared app, like [AndroidDriverProbesTest].
 */
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
	 * `Clicking on` (the line a touch on a coordinate writes) since the probe began. Red if a click by coordinate returns
	 * to the focus path, by `UiDevice.click` or by `UiObject2.click()` (verified by putting each one in `FieldFocus`).
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

	/** A reading the verdict may ask for: it counts how many times it was asked and answers always the same. */
	private class Reading(private val answer: Boolean?) {
		var asked = 0

		fun ask(): Boolean? {
			asked++
			return answer
		}
	}

	/**
	 * YB-3: every row of the table of the keyboard guard, on its pure verdict, and what each row asks. Red when two
	 * rows are swapped (refusing when the windows cannot be read and letting a keyboard that is shown pass, say): the
	 * row that defends from the extra character is the one a device probe cannot provoke.
	 */
	@Test
	fun theGuardDecidesEveryRowOfItsTableBeforeWaitingAndAsksOnlyWhatTheRowNeeds() {
		var focus = Reading(true)
		var shown = Reading(true)

		fun before(readable: Boolean, listed: Boolean, hasFocus: Boolean?, inputMethod: Boolean?): GuardVerdict {
			focus = Reading(hasFocus)
			shown = Reading(inputMethod)
			return GuardVerdict.beforeWaiting(readable, listed, focus::ask, shown::ask)
		}

		assertEquals("windows unreadable, shown", GuardVerdict.REFUSE, before(false, false, true, true))
		assertEquals(GuardVerdict.PASS_UNREADABLE, before(false, false, true, false))
		assertEquals(GuardVerdict.PASS_UNREADABLE, before(false, false, true, null))
		assertEquals("a keyboard that is listed needs nothing", GuardVerdict.PASS, before(true, true, true, true))
		assertEquals("nothing was asked", 0, focus.asked + shown.asked)
		assertEquals("no field with the focus", GuardVerdict.PASS, before(true, false, false, true))
		assertEquals("the input method is not asked without a field", 0, shown.asked)
		assertEquals(GuardVerdict.PASS_HIDDEN, before(true, false, true, false))
		assertEquals(GuardVerdict.WAIT, before(true, false, true, true))
		assertEquals(GuardVerdict.WAIT, before(true, false, true, null))
		assertEquals("a focus that cannot be read counts as a focus", GuardVerdict.WAIT, before(true, false, null, true))
		assertEquals(GuardVerdict.PASS_HIDDEN, before(true, false, null, false))
		assertEquals(1, shown.asked)
	}

	/** YB-3/YB-4: the rows of the verdict after the wait, the one that says the layout moved (listed) among them. */
	@Test
	fun theGuardDecidesEveryRowOfItsTableAfterWaitingAndAsksOnlyWhatTheRowNeeds() {
		var now = Reading(true)

		fun after(listed: Boolean, before: Boolean?, inputMethod: Boolean?): GuardVerdict {
			now = Reading(inputMethod)
			return GuardVerdict.afterWaiting(listed, before, now::ask)
		}

		assertEquals("the layout moved while it was awaited", GuardVerdict.PASS_AFTER_WAIT, after(true, true, true))
		assertEquals(0, now.asked)
		assertEquals("nothing could be asked", GuardVerdict.PASS_TAKEN_AS_HIDDEN, after(false, null, true))
		assertEquals(0, now.asked)
		assertEquals("hidden while it was awaited", GuardVerdict.PASS_HIDDEN, after(false, true, false))
		assertEquals("shown and never listed", GuardVerdict.REFUSE, after(false, true, true))
		assertEquals("shown and now unreadable", GuardVerdict.REFUSE, after(false, true, null))
	}

	/**
	 * E3: a swipe is a down, [SwipeTrack.MOVES] moves and an up (12 events, not the 80 that `UiDevice.swipe` injected),
	 * and the time of each event is its place of the nominal duration: the velocity the app sees comes from the stamps
	 * and not from how long the host takes to deliver the events. Red if the track goes back to a step per 5 ms, loses
	 * the up, or stamps the events with anything but the plan.
	 */
	@Test
	fun aSwipeIsTwelveEventsStampedAtTheirPlaceOfTheNominalDuration() {
		val track = SwipeTrack.plan(SWIPE_FROM, SWIPE_TO, SWIPE_MS, SWIPE_DOWN_TIME)
		val moves = track.subList(1, track.size - 1)

		assertEquals(SWIPE_EVENTS, track.size)
		assertEquals(SwipeTrack.MOVES + 2, track.size)
		assertEquals(SwipeTrack.Kind.DOWN, track.first().kind)
		assertEquals(SwipeTrack.Kind.UP, track.last().kind)
		assertEquals(List(SwipeTrack.MOVES) { SwipeTrack.Kind.MOVE }, moves.map { it.kind })
		assertEquals("the down is stamped when the swipe begins", SWIPE_DOWN_TIME, track.first().eventTime)
		assertEquals("the up comes when the nominal duration has passed", SWIPE_DOWN_TIME + SWIPE_MS, track.last().eventTime)
		assertEquals(
			"the moves are evenly spaced",
			List(SwipeTrack.MOVES) { SWIPE_DOWN_TIME + SWIPE_MOVE_GAP_MS * (it + 1) },
			moves.map { it.eventTime }
		)
		assertEquals("the track starts where the swipe starts", SWIPE_FROM.second.toFloat(), track.first().y)
		assertEquals("the track ends where the swipe ends", SWIPE_TO.second.toFloat(), track.last().y)
		assertEquals("half the distance at half the time", SWIPE_MIDDLE_Y, track[SwipeTrack.MOVES / 2].y)
	}

	/**
	 * The lines UI Automator writes for a touch since logcat was last cleared (YB-2): `Clicking on` and
	 * `Long-clicking on`, under the tag of the class that made the touch: `UiDevice` for `UiDevice.click`, `UiObject2`
	 * for `UiObject2.click()` (the usual way to put a point read from the tree back into the focus path) and
	 * `UiObject` for the legacy API.
	 */
	private fun clicks(driver: UiAutomatorScenarioDriver): List<String> =
		driver.session.shell("logcat -d -s UiDevice UiObject2 UiObject").lines()
			.filter { it.contains("Clicking on") || it.contains("Long-clicking on") }

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
		const val SWIPE_EVENTS = 12
		const val SWIPE_MS = 400L
		const val SWIPE_MOVE_GAP_MS = 40L
		const val SWIPE_DOWN_TIME = 1_000_000L
		const val SWIPE_MIDDLE_Y = 600f
		val SWIPE_FROM = 100 to 900
		val SWIPE_TO = 100 to 300
	}
}
