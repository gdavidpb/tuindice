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
	var dialog: String? = null
	var backResult = true
	var swipeResult = true
	var submitResult = true

	/** When set, every gesture and text call is refused with this reason: it answers false and [lastRefusal] says why. */
	var refusal: String? = null
	var typing: (String) -> String = { it }

	/** False makes `typeKeys` answer false after writing what [typing] makes of the text, like a refused injection. */
	var keysAccepted = true
	var terminated = false
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
		return inForeground && !terminated
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

	override fun bounds(q: Query?): ElementBounds? {
		enter("bounds")
		return if (q == null) SCREEN else if (shown(q)) element(q)?.takeIf { !it.unreadableBounds }?.let(::drifted) else null
	}

	private fun drifted(el: FakeElement): ElementBounds {
		val current = el.bounds
		el.bounds = ElementBounds(current.left - el.drift, current.top, current.right - el.drift, current.bottom)
		return current
	}

	override fun tap(q: Query): Boolean {
		enter("tap")
		taps += q
		val reachable = shown(q) && refusal == null
		if (reachable) onTap[q]?.invoke()
		return reachable
	}

	override fun tapAt(q: Query?, fx: Double, fy: Double): Boolean {
		enter("tapAt")
		return (q == null || shown(q)) && refusal == null
	}

	override fun doubleTap(q: Query): Boolean {
		enter("doubleTap")
		return shown(q) && refusal == null
	}

	override fun swipe(from: Query?, vector: SwipeVector, durationMs: Long): Boolean {
		enter("swipe")
		val startsOffScreen = from != null && !shown(from)
		if (!swipeResult || refusal != null || startsOffScreen) return false
		swipes++
		onSwipe(swipes)
		return true
	}

	override fun pressBack(): Boolean {
		enter("pressBack")
		return backResult && refusal == null
	}

	override fun typeKeys(q: Query, text: String): Boolean {
		enter("typeKeys")
		val el = element(q)?.takeIf { refusal == null && !terminated }
		el?.let { it.text = typing(it.text.orEmpty() + text) }
		return el != null && keysAccepted
	}

	override fun clearText(q: Query): Boolean {
		enter("clearText")
		val el = element(q)?.takeIf { refusal == null }
		el?.text = ""
		return el != null
	}

	override fun submitTextEntry(): Boolean {
		enter("submitTextEntry")
		return submitResult && refusal == null
	}

	override fun lastRefusal(): String? = refusal

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
		val SCREEN = ElementBounds(0.0, 0.0, 1000.0, 2000.0)
	}
}
