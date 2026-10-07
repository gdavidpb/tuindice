package com.gdavidpb.tuindice.ui.activity

import android.app.Application
import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class AnimatorScaleObserverTest {
	private val resolver = RuntimeEnvironment.getApplication().contentResolver
	private val scaleUri = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)

	private fun notifyScaleChanged() {
		resolver.notifyChange(scaleUri, null)
		shadowOf(android.os.Looper.getMainLooper()).idle()
	}

	@Test
	fun whileStarted_aChangeOfTheScaleIsReported() {
		var changes = 0
		val observer = AnimatorScaleObserver(resolver) { changes++ }

		observer.start()
		notifyScaleChanged()

		assertEquals(1, changes)
	}

	@Test
	fun afterStop_aChangeOfTheScaleIsNotReported() {
		var changes = 0
		val observer = AnimatorScaleObserver(resolver) { changes++ }

		observer.start()
		observer.stop()
		notifyScaleChanged()

		assertEquals(0, changes)
	}

	@Test
	fun beforeStart_aChangeOfTheScaleIsNotReported() {
		var changes = 0
		AnimatorScaleObserver(resolver) { changes++ }

		notifyScaleChanged()

		assertEquals(0, changes)
	}
}
