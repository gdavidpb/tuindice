package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ElementBounds
import com.gdavidpb.tuindice.scenariokit.driver.HttpReply
import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.driver.SwipeVector
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Query
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

/**
 * A scripted screen (elements with visibility, enabled state and text), a request journal
 * and a virtual clock that every wait and pause advances, so timeouts cost no real time.
 */
internal class FakeDriver(override val platform: Platform = Platform.Android) : ScenarioDriver {
	val time = TestTimeSource()
	val clocks = Clocks(time) { "2026-01-01T00:00:00Z" }
	val screen = mutableMapOf<Query, FakeElement>()
	val backend = FakeWireMock()
	val calls = mutableListOf<String>()
	val logLines = mutableListOf<String>()
	val captured = mutableListOf<Pair<String, Int>>()
	val launches = mutableListOf<LaunchSpec>()
	val taps = mutableListOf<Query>()
	val onTap = mutableMapOf<Query, () -> Unit>()
	var swipes = 0
	var onSwipe: (Int) -> Unit = {}

	var launchResults = ArrayDeque<Boolean>()
	var foregroundResult = true
	var inForeground = true

	/** When set, `isForeground` answers what it returns on each call instead of [inForeground]. */
	var foregroundScript: (() -> Boolean)? = null
	var dialog: String? = null
	var backResult = true
	var swipeResult = true
	var submitResult = true

	/** When set, every gesture and text call is refused with this reason: it answers false and [lastRefusal] says why. */
	var refusal: String? = null
	var typing: (String) -> String = { it }

	/** False makes `typeKeys` answer false after writing what [typing] makes of the text, like a refused injection. */
	var keysAccepted = true

	/** The reason `lastRefusal` gives when [refusal] is not set: why `typeKeys` stopped, with the keys it typed in. */
	var stopReason: String? = null

	/** What `keysInjected` answers; null means as many as the text has characters (zero when nothing was typed). */
	var injectedOverride: Int? = null
	private var injected = 0
	var terminated = false

	/** Why the last gesture or text call answered false on its own (the element was not on screen). */
	private var reason: String? = null

	/** Set by a successful `typeKeys`: the keyboard is up, and a touch that starts on it is refused. */
	var keyboardOpen = false
	var throwOn: String? = null
	var logThrows = false
	var launchTakesMs = 0L
	var httpTakesMs = 0L

	private fun enter(call: String) {
		calls += call
		if (throwOn == call.substringBefore('(')) error("scripted driver failure in $call")
	}

	private fun element(q: Query): FakeElement? = screen[q]

	private fun shown(q: Query): Boolean {
		val el = element(q)
		return !terminated && el != null && el.visible && swipes >= el.hiddenUntilSwipes
	}

	override fun launch(spec: LaunchSpec): Boolean {
		enter("launch")
		time += launchTakesMs.milliseconds
		launches += spec
		return launchResults.removeFirstOrNull() ?: true
	}

	override fun foreground(): Boolean {
		enter("foreground")
		return foregroundResult && !terminated
	}

	override fun terminate() {
		enter("terminate")
		terminated = true
	}

	override fun isForeground(): Boolean {
		enter("isForeground")
		return (foregroundScript?.invoke() ?: inForeground) && !terminated
	}

	/** When set, `isRunning` answers what it returns on each call instead of "not terminated". */
	var runningScript: (() -> Boolean)? = null

	override fun isRunning(): Boolean {
		enter("isRunning")
		return runningScript?.invoke() ?: !terminated
	}

	override fun waitVisible(q: Query, timeoutMs: Long): Boolean {
		enter("waitVisible")
		val el = element(q)
		val appears = el?.appearsAfterMs
		return when {
			shown(q) -> true
			el != null && appears != null && appears <= timeoutMs -> {
				time += appears.milliseconds
				el.visible = true
				true
			}
			else -> {
				time += timeoutMs.milliseconds
				false
			}
		}
	}

	override fun waitGone(q: Query, timeoutMs: Long): Boolean {
		enter("waitGone")
		val gone = !terminated && !shown(q)
		if (!gone) time += timeoutMs.milliseconds
		return gone
	}

	override fun isVisible(q: Query): Boolean {
		enter("isVisible")
		return shown(q)
	}

	override fun isEnabled(q: Query): Boolean {
		enter("isEnabled")
		val el = element(q)
		el?.let { it.enabledChecks++ }
		val enabledNow = el != null && (el.enabled || (el.enabledAfterChecks > 0 && el.enabledChecks > el.enabledAfterChecks))
		return shown(q) && enabledNow
	}

	override fun readText(q: Query): String? {
		enter("readText")
		val el = element(q)?.takeIf { shown(q) }
		val script = el?.scriptedReads
		return when {
			el == null -> null
			script == null -> el.text
			script.size > 1 -> script.removeAt(0)
			else -> script.firstOrNull()
		}
	}

	override fun isChecked(q: Query): Boolean? {
		enter("isChecked")
		return element(q)?.takeIf { shown(q) }?.checked
	}

	override fun bounds(q: Query?): ElementBounds? {
		enter("bounds")
		return if (q == null) SCREEN else if (shown(q)) element(q)?.takeIf { !it.unreadableBounds }?.let(::drifted) else null
	}

	private fun drifted(el: FakeElement): ElementBounds {
		val current = el.bounds
		el.bounds = ElementBounds(current.left - el.drift, current.top, current.right - el.drift, current.bottom)
		return current
	}

	/** Starts a gesture: forgets the last reason and answers whether [q] can be aimed at. */
	private fun aim(q: Query?, what: String): Boolean {
		reason = null
		val reachable = (q == null || shown(q)) && refusal == null
		if (!reachable) reason = "$what: ${q ?: "the screen"} is not on screen; gesture refused"
		return reachable
	}

	private fun underKeyboard(fy: Double, what: String): Boolean {
		val covered = keyboardOpen && fy >= KEYBOARD_TOP
		if (covered) reason = "$what starts inside the on-screen keyboard; touch refused"
		return covered
	}

	override fun tap(q: Query): Boolean {
		enter("tap")
		taps += q
		val reachable = aim(q, "tap")
		if (reachable) onTap[q]?.invoke()
		return reachable
	}

	override fun tapAt(q: Query?, fx: Double, fy: Double): Boolean {
		enter("tapAt")
		return aim(q, "tapAt") && !underKeyboard(fy, "tapAt")
	}

	override fun doubleTap(q: Query): Boolean {
		enter("doubleTap")
		return aim(q, "doubleTap")
	}

	override fun swipe(from: Query?, vector: SwipeVector, durationMs: Long): Boolean {
		enter("swipe")
		val allowed = aim(from, "swipe") && !underKeyboard(vector.fy, "swipe") && swipeResult
		if (allowed) {
			swipes++
			onSwipe(swipes)
		}
		return allowed
	}

	override fun pressBack(): Boolean {
		enter("pressBack")
		reason = if (platform == Platform.Ios) "pressBack: iOS has no system back action" else null
		return backResult && refusal == null && platform == Platform.Android
	}

	override fun typeKeys(q: Query, text: String): Boolean {
		enter("typeKeys")
		reason = null
		val el = element(q)?.takeIf { refusal == null && !terminated && shown(q) }
		if (el == null) reason = "typeKeys $q: the field is not on screen"
		keyboardOpen = keyboardOpen || el != null
		el?.let { it.text = typing(it.text.orEmpty() + text) }
		injected = if (el == null) 0 else text.length
		return el != null && keysAccepted
	}

	override fun keysInjected(): Int = injectedOverride ?: injected

	override fun clearText(q: Query): Boolean {
		enter("clearText")
		reason = null
		val el = element(q)?.takeIf { refusal == null && shown(q) }
		if (el == null) reason = "clearText $q: the field is not on screen"
		el?.text = ""
		return el != null
	}

	override fun submitTextEntry(): Boolean {
		enter("submitTextEntry")
		return submitResult && refusal == null
	}

	override fun lastRefusal(): String? = refusal ?: stopReason ?: reason

	override fun http(method: String, path: String, body: String?, authorization: String?): HttpReply {
		enter("http($method $path)")
		time += httpTakesMs.milliseconds
		return backend.http(method, path, body, authorization)
	}

	override fun log(line: String) {
		if (logThrows) error("scripted failure of the driver log")
		logLines += line
	}

	override fun pause(ms: Long) {
		time += ms.milliseconds
	}

	override fun captureFailure(scenarioId: String, stepIndex: Int) {
		enter("captureFailure")
		captured += scenarioId to stepIndex
	}

	override fun systemDialogInFront(): String? {
		enter("systemDialogInFront")
		return dialog
	}

	private companion object {
		const val KEYBOARD_TOP = 0.6
		val SCREEN = ElementBounds(0.0, 0.0, 1000.0, 2000.0)
	}
}
