package com.gdavidpb.tuindice.scenariorunner.driver

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * Resolves scenario queries on the screen. A tag is the resource id, because the app
 * exposes `testTag` as resource name; system queries are not scoped to the app's package.
 */
internal class Selectors(private val device: UiDevice) {
	fun find(q: Query): UiObject2? =
		candidates(q).firstNotNullOfOrNull { selector -> runCatching { device.findObject(selector) }.getOrNull() }

	private fun candidates(q: Query): List<BySelector> = when (q) {
		is Query.Tag -> listOf(By.res(q.value))
		is Query.Text -> listOf(if (q.contains) By.textContains(q.value) else By.text(q.value))
		is Query.System -> listOf(By.res(q.value), By.text(q.value))
	}
}
