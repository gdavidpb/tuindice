package com.gdavidpb.tuindice.testkit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

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

@OptIn(ExperimentalTestApi::class)
fun runTuIndiceUiTest(
	block: ComposeUiTest.() -> Unit
) {
	runComposeUiTest(block = block)
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.setTuIndiceTestContent(
	sizeClass: TuIndiceTestSizeClass = TuIndiceTestSizeClass.Compact,
	density: Float = 1f,
	locale: Locale? = null,
	content: @Composable () -> Unit
) {
	setContent {
		CompositionLocalProvider(
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

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.advanceAnimationsBy(millis: Long) {
	mainClock.autoAdvance = false
	mainClock.advanceTimeBy(millis)
	waitForIdle()
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertNodeVisible(
	tag: String,
	useUnmergedTree: Boolean = false
) {
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

private fun Locale?.toLayoutDirection(): LayoutDirection {
	val language = this?.language?.lowercase()
	return if (language in RTL_LANGUAGES) LayoutDirection.Rtl else LayoutDirection.Ltr
}

private val RTL_LANGUAGES = setOf("ar", "fa", "he", "ur")
