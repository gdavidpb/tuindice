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
	var finishResult = true
	var typing: (String) -> String = { it }
	var throwOn: String? = null

	private fun enter(call: String) {
		calls += call
		if (throwOn == call.substringBefore('(')) error("scripted driver failure in $call")
	}

	private fun element(q: Query): FakeElement? = screen[q]

	private fun shown(q: Query): Boolean {
		val el = element(q)
		return el != null && el.visible && swipes >= el.hiddenUntilSwipes
	}

	override fun launch(spec: LaunchSpec): Boolean {
		enter("launch")
		launches += spec
		return launchResults.removeFirstOrNull() ?: true
	}

	override fun foreground(): Boolean {
		enter("foreground")
		return foregroundResult
	}

	override fun terminate() = enter("terminate")

	override fun isForeground(): Boolean {
		enter("isForeground")
		return inForeground
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
		if (!shown(q)) return true
		time += timeoutMs.milliseconds
		return false
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
		return if (shown(q)) element(q)?.text else null
	}

	override fun bounds(q: Query?): ElementBounds? {
		enter("bounds")
		return if (q == null) SCREEN else if (shown(q)) element(q)?.let(::drifted) else null
	}

	private fun drifted(el: FakeElement): ElementBounds {
		val current = el.bounds
		el.bounds = ElementBounds(current.left - el.drift, current.top, current.right - el.drift, current.bottom)
		return current
	}

	override fun tap(q: Query): Boolean {
		enter("tap")
		taps += q
		val reachable = shown(q)
		if (reachable) onTap[q]?.invoke()
		return reachable
	}

	override fun tapAt(q: Query?, fx: Double, fy: Double): Boolean {
		enter("tapAt")
		return q == null || shown(q)
	}

	override fun doubleTap(q: Query): Boolean {
		enter("doubleTap")
		return shown(q)
	}

	override fun swipe(from: Query?, vector: SwipeVector, durationMs: Long): Boolean {
		enter("swipe")
		if (!swipeResult) return false
		swipes++
		onSwipe(swipes)
		return true
	}

	override fun pressBack(): Boolean {
		enter("pressBack")
		return backResult
	}

	override fun typeKeys(q: Query, text: String): Boolean {
		enter("typeKeys")
		val el = element(q) ?: return false
		el.text = typing(el.text.orEmpty() + text)
		return true
	}

	override fun setText(q: Query, text: String): Boolean {
		enter("setText")
		val el = element(q) ?: return false
		el.text = text
		return true
	}

	override fun clearText(q: Query): Boolean {
		enter("clearText")
		val el = element(q) ?: return false
		el.text = ""
		return true
	}

	override fun finishTextEntry(): Boolean {
		enter("finishTextEntry")
		return finishResult
	}

	override fun http(method: String, path: String, body: String?, authorization: String?): HttpReply {
		enter("http($method $path)")
		return backend.http(method, path, body, authorization)
	}

	override fun log(line: String) {
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
