package com.gdavidpb.tuindice.scenariorunner.driver

import androidx.test.uiautomator.By

/** The two OS dialogs that appear on emulators and cover the app. */
internal class SystemDialogs(private val session: DeviceSession) {
	/** Clicks away the dialogs we know are harmless; called when a scenario starts. */
	fun dismissKnown() {
		runCatching {
			session.device.findObject(By.res(ANR_WAIT_BUTTON))?.let {
				session.log.write("dismissKnown: the \"app not responding\" dialog was dismissed with its wait button")
				it.click()
			}
			if (session.device.hasObject(By.textContains(STYLUS_TEXT))) {
				session.log.write("dismissKnown: the stylus handwriting notice was dismissed")
				session.device.findObject(By.text(STYLUS_CANCEL))?.click()
			}
		}
	}

	/** Text of the dialog covering the app, or null when nothing is in front. */
	fun inFront(): String? = runCatching {
		ANR_TEXTS.firstNotNullOfOrNull { text -> session.device.findObject(By.textContains(text))?.text }
			?: session.device.findObject(By.textContains(STYLUS_TEXT))?.text
	}.getOrNull()

	private companion object {
		const val ANR_WAIT_BUTTON = "android:id/aerr_wait"
		const val STYLUS_TEXT = "Try out your stylus"
		const val STYLUS_CANCEL = "Cancel"
		val ANR_TEXTS = listOf("isn't responding", "isn’t responding")
	}
}
