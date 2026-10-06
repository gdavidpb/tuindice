package com.gdavidpb.tuindice.testkit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.test.TestResult

enum class TuIndiceTestSizeClass(
	val widthDp: Int,
	val heightDp: Int
) {
	Compact(
		widthDp = 360,
		heightDp = 640
	),
	Medium(
		widthDp = 600,
		heightDp = 960
	),
	Expanded(
		widthDp = 840,
		heightDp = 1280
	)
}

// The v2 harness composes on a StandardTestDispatcher: coroutines are queued and run on the test
// thread when the clock advances, as they would on the UI thread. The v1 harness composed on an
// UnconfinedTestDispatcher, so a LaunchedEffect resumed on whatever thread its withContext finished
// on (a Lottie loaded on IO), and Compose 1.12 rejects observing snapshots from a second thread.
@OptIn(ExperimentalTestApi::class)
fun runTuIndiceUiTest(
	block: suspend ComposeUiTest.() -> Unit
): TestResult {
	return runComposeUiTest(block = block)
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.setTuIndiceTestContent(
	sizeClass: TuIndiceTestSizeClass = TuIndiceTestSizeClass.Compact,
	density: Float = 1f,
	locale: Locale? = null,
	content: @Composable () -> Unit
) {
	setContent {
		val lifecycleOwner = remember { TuIndiceTestLifecycleOwner() }

		DisposableEffect(lifecycleOwner) {
			lifecycleOwner.resume()
			onDispose { lifecycleOwner.destroy() }
		}

		CompositionLocalProvider(
			LocalLifecycleOwner provides lifecycleOwner,
			LocalDensity provides Density(density = density),
			LocalLayoutDirection provides locale.toLayoutDirection()
		) {
			Box(
				modifier = androidx.compose.ui.Modifier
					.requiredSize(
						width = sizeClass.widthDp.dp,
						height = sizeClass.heightDp.dp
					)
			) {
				MaterialTheme {
					content()
				}
			}
		}
	}
}

private class TuIndiceTestLifecycleOwner : LifecycleOwner {
	private val registry = LifecycleRegistry(this)

	override val lifecycle: Lifecycle
		get() = registry

	fun resume() {
		registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
		registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
		registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
	}

	fun destroy() {
		registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
	}
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.advanceAnimationsBy(millis: Long) {
	mainClock.autoAdvance = false
	mainClock.advanceTimeBy(millis)
	waitForIdle()
}

// Generous on purpose: it only bounds a hang, and a CI runner with three cores
// composes far slower than a developer machine.
private const val NODE_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNodeVisible(
	tag: String,
	useUnmergedTree: Boolean = false
) {
	// Wait for the node before asserting on it. Asserting straight away passes only
	// while the machine composes faster than the test reads, which is why these
	// assertions held on an idle laptop and failed on a loaded CI runner. The wait
	// has to read the same tree the assertion will, or a tag that lives only in the
	// unmerged one never satisfies it.
	waitUntil(timeoutMillis = NODE_TIMEOUT_MILLIS) {
		onAllNodesWithTag(
			testTag = tag,
			useUnmergedTree = useUnmergedTree
		).fetchSemanticsNodes().size == 1
	}

	onNodeWithTag(
		testTag = tag,
		useUnmergedTree = useUnmergedTree
	).assertIsDisplayed()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNodeHidden(
	tag: String,
	useUnmergedTree: Boolean = false
) {
	onNodeWithTag(
		testTag = tag,
		useUnmergedTree = useUnmergedTree
	).assertDoesNotExist()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNodeEnabled(
	tag: String,
	useUnmergedTree: Boolean = false
) {
	onNodeWithTag(
		testTag = tag,
		useUnmergedTree = useUnmergedTree
	).assertIsEnabled()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNodeDisabled(
	tag: String,
	useUnmergedTree: Boolean = false
) {
	onNodeWithTag(
		testTag = tag,
		useUnmergedTree = useUnmergedTree
	).assertIsNotEnabled()
}

// One performTextInput per character, as a soft keyboard delivers them. `beforeEach` runs ahead of
// each keystroke with its index, which is where a test lands the view model's lagging answers.
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.performTextInputPerCharacter(
	tag: String,
	text: String,
	beforeEach: (index: Int) -> Unit = {}
) {
	text.forEachIndexed { index, character ->
		beforeEach(index)

		onNodeWithTag(tag).performTextInput(character.toString())
	}
}

private fun Locale?.toLayoutDirection(): LayoutDirection {
	val language = this?.language?.lowercase()
	return if (language in RTL_LANGUAGES) LayoutDirection.Rtl else LayoutDirection.Ltr
}

private val RTL_LANGUAGES = setOf("ar", "fa", "he", "ur")
