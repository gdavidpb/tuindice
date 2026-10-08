package com.gdavidpb.tuindice.scenariorunner.driver

import android.graphics.Rect
import androidx.test.uiautomator.UiObject2
import com.gdavidpb.tuindice.scenariokit.driver.ElementBounds
import com.gdavidpb.tuindice.scenariokit.driver.ElementProbe
import com.gdavidpb.tuindice.scenariokit.model.Query

/** Read-only probes; every wait is an explicit poll bounded by the step's own timeout. */
internal class ElementProber(private val session: DeviceSession) : ElementProbe {
	override fun waitVisible(q: Query, timeoutMs: Long): Boolean = session.poll(timeoutMs) { isVisible(q) }

	/**
	 * Gone means the app's window was read and the query matched nothing in it, in the same pass. A screen that
	 * cannot be read (no root for the app, an accessibility error) keeps the poll going and, at the deadline,
	 * is a failure: it must never read as "disappeared". Only the changes of what is read reach the log.
	 */
	override fun waitGone(q: Query, timeoutMs: Long): Boolean {
		var last: Presence? = null
		val gone = session.poll(timeoutMs) {
			val reading = session.selectors.presence(q)
			if (reading != last) note(q, reading)
			last = reading
			reading == Presence.ABSENT
		}

		if (!gone) session.log.write("waitGone $q: not gone after $timeoutMs ms; last read $last")

		return gone
	}

	override fun isVisible(q: Query): Boolean = session.selectors.find(q) != null

	override fun isEnabled(q: Query): Boolean = attempt(q) { it.isEnabled } == true

	override fun readText(q: Query): String? = attempt(q) { it.text }

	override fun isChecked(q: Query): Boolean? = attempt(q) { if (it.isCheckable) it.isChecked else null }

	override fun bounds(q: Query?): ElementBounds? {
		val rect = if (q == null) screen() else attempt(q) { it.visibleBounds }

		return rect?.let {
			ElementBounds(it.left.toDouble(), it.top.toDouble(), it.right.toDouble(), it.bottom.toDouble())
		}
	}

	private fun note(q: Query, reading: Presence) {
		if (reading == Presence.UNREADABLE) {
			session.log.write("waitGone $q: the app's accessibility tree cannot be read; it is not taken as gone, still polling")
		}
	}

	private fun screen() = Rect(0, 0, session.device.displayWidth, session.device.displayHeight)

	private fun <T> attempt(q: Query, read: (UiObject2) -> T): T? =
		session.selectors.find(q)?.let { runCatching { read(it) }.getOrNull() }
}
