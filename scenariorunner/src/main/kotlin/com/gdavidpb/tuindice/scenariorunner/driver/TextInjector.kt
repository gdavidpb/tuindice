package com.gdavidpb.tuindice.scenariorunner.driver

import android.view.KeyCharacterMap
import com.gdavidpb.tuindice.scenariokit.driver.TextEntry
import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * `setText` assigns in one accessibility action; `typeKeys` clicks the field and injects
 * key events, so the app sees the same input a keyboard would produce.
 */
internal class TextInjector(private val session: DeviceSession) : TextEntry {
	override fun typeKeys(q: Query, text: String): Boolean {
		val field = session.selectors.find(q)
		val events = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD).getEvents(text.toCharArray())

		return field != null && events != null && runCatching {
			// Clicks where the field is once it has stopped moving: right after a tap that opens the keyboard
			// the form is still sliding up, and the position read a moment ago is a key of the keyboard.
			val place = session.settledBounds(q) ?: return@runCatching false
			session.device.click(place.centerX(), place.centerY())
			events.all { session.instrumentation.uiAutomation.injectInputEvent(it, true) }
		}.getOrDefault(false)
	}

	override fun setText(q: Query, text: String): Boolean =
		session.selectors.find(q)?.let { runCatching { it.text = text }.isSuccess } ?: false

	override fun clearText(q: Query): Boolean = setText(q, "")

	override fun finishTextEntry(): Boolean {
		if (!keyboardShown()) return true

		session.device.pressBack()

		return session.poll(HIDE_TIMEOUT_MS) { !keyboardShown() }
	}

	private fun keyboardShown(): Boolean = session.shell("dumpsys input_method").contains("mInputShown=true")

	private companion object {
		const val HIDE_TIMEOUT_MS = 2_000L
	}
}
