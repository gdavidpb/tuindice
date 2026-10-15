package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ResumedEffectUiTest {
	@Test
	fun when_theScreenResumes_then_theActionRunsTheFirstTime() = runTuIndiceUiTest {
		val owner = TestLifecycleOwner()
		var runs = 0

		setTuIndiceTestContent {
			ResumedEffect(lifecycleOwner = owner) { runs++ }
		}

		owner.handleEvent(Lifecycle.Event.ON_CREATE)
		owner.handleEvent(Lifecycle.Event.ON_START)
		owner.handleEvent(Lifecycle.Event.ON_RESUME)
		waitForIdle()

		assertEquals(1, runs)
	}

	@Test
	fun when_theScreenIsPausedAndResumesAgain_then_theActionRunsOnceMoreAndNotWhilePaused() = runTuIndiceUiTest {
		val owner = TestLifecycleOwner()
		var runs = 0

		setTuIndiceTestContent {
			ResumedEffect(lifecycleOwner = owner) { runs++ }
		}

		owner.handleEvent(Lifecycle.Event.ON_CREATE)
		owner.handleEvent(Lifecycle.Event.ON_START)
		owner.handleEvent(Lifecycle.Event.ON_RESUME)
		waitForIdle()
		owner.handleEvent(Lifecycle.Event.ON_PAUSE)
		waitForIdle()

		assertEquals(1, runs)

		owner.handleEvent(Lifecycle.Event.ON_RESUME)
		waitForIdle()

		assertEquals(2, runs)
	}

	private class TestLifecycleOwner : LifecycleOwner {
		private val registry = LifecycleRegistry(this)

		override val lifecycle: Lifecycle
			get() = registry

		fun handleEvent(event: Lifecycle.Event) {
			registry.handleLifecycleEvent(event)
		}
	}
}
