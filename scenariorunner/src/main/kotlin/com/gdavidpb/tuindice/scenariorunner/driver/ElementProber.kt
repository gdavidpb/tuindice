package com.gdavidpb.tuindice.scenariorunner.driver

import android.graphics.Rect
import androidx.test.uiautomator.UiObject2
import com.gdavidpb.tuindice.scenariokit.driver.ElementBounds
import com.gdavidpb.tuindice.scenariokit.driver.ElementProbe
import com.gdavidpb.tuindice.scenariokit.model.Query

/** Read-only probes; every wait is an explicit poll bounded by the step's own timeout. */
internal class ElementProber(private val session: DeviceSession) : ElementProbe {
	override fun waitVisible(q: Query, timeoutMs: Long): Boolean = session.poll(timeoutMs) { isVisible(q) }

	override fun waitGone(q: Query, timeoutMs: Long): Boolean = session.poll(timeoutMs) { !isVisible(q) }

	override fun isVisible(q: Query): Boolean = session.selectors.find(q) != null

	override fun isEnabled(q: Query): Boolean = attempt(q) { it.isEnabled } == true

	override fun readText(q: Query): String? = attempt(q) { it.text }

	override fun bounds(q: Query?): ElementBounds? {
		val rect = if (q == null) screen() else attempt(q) { it.visibleBounds }

		return rect?.let {
			ElementBounds(it.left.toDouble(), it.top.toDouble(), it.right.toDouble(), it.bottom.toDouble())
		}
	}

	private fun screen() = Rect(0, 0, session.device.displayWidth, session.device.displayHeight)

	private fun <T> attempt(q: Query, read: (UiObject2) -> T): T? =
		session.selectors.find(q)?.let { runCatching { read(it) }.getOrNull() }
}
