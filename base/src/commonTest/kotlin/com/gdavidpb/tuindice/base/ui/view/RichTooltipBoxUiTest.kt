package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
class RichTooltipBoxUiTest {
	@Test
	fun when_anotherTargetIsTapped_then_theOpenTooltipDisappears() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SelectionHost()
		}

		onNodeWithTag(targetTag(0)).performTouchInput { click() }
		assertNodeVisible(bodyTag(0), useUnmergedTree = true)

		onNodeWithTag(targetTag(1)).performTouchInput { click() }
		waitUntilAbsent(bodyTag(0))
		assertNodeVisible(bodyTag(1), useUnmergedTree = true)
	}

	@Test
	fun when_targetIsTapped_then_showsTheTitleAndEveryBodyLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column(
				modifier = Modifier.fillMaxSize(),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				RichTooltipBox(
					isVisible = true,
					onDismissRequest = {},
					tooltip = RichTooltipText(
						title = "Trimestre",
						lines = listOf("Índice 4.2", "UC 20"),
						testTag = bodyTag(0)
					)
				) {
					Box(modifier = Modifier.size(48.dp).testTag(targetTag(0)))
				}
			}
		}

		onNodeWithText("Trimestre", useUnmergedTree = true).assertIsDisplayed()
		assertNodeVisible(bodyTag(0), useUnmergedTree = true)
		onNodeWithText("Índice 4.2", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("UC 20", useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_composedAlreadyVisible_then_itStaysOpenAndTheCallerFlagIsKept() = runTuIndiceUiTest {
		val isVisible = mutableStateOf(true)

		setTuIndiceTestContent {
			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				RichTooltipBox(
					isVisible = isVisible.value,
					onDismissRequest = { isVisible.value = false },
					tooltip = RichTooltipText(lines = listOf("Índice 4.2"), testTag = bodyTag(0))
				) {
					Box(modifier = Modifier.size(48.dp).testTag(targetTag(0)))
				}
			}
		}
		waitForIdle()

		assertNodeVisible(bodyTag(0), useUnmergedTree = true)
		assertTrue(isVisible.value)
	}

	@Test
	fun when_callerClearsIsVisible_then_tooltipDisappears() = runTuIndiceUiTest {
		val isVisible = mutableStateOf(false)

		setTuIndiceTestContent {
			RichTooltipBox(
				isVisible = isVisible.value,
				onDismissRequest = { isVisible.value = false },
				tooltip = RichTooltipText(
					lines = listOf("Solo cuerpo"),
					testTag = bodyTag(0)
				)
			) {
				Box(modifier = Modifier.size(48.dp).testTag(targetTag(0)))
			}
		}

		runOnIdle { isVisible.value = true }
		assertNodeVisible(bodyTag(0), useUnmergedTree = true)

		runOnIdle { isVisible.value = false }
		waitUntilAbsent(bodyTag(0))
	}

	// The toolkit closes a tooltip when another one opens. Its caller must learn that, or the next tap on its
	// anchor would close an already closed tooltip instead of opening it.
	@Test
	fun when_anotherTooltipClosesThisOne_then_tappingItsAnchorOpensItAgain() = runTuIndiceUiTest {
		val flags = List(2) { mutableStateOf(false) }

		setTuIndiceTestContent {
			Column(
				modifier = Modifier.fillMaxSize(),
				verticalArrangement = Arrangement.spacedBy(120.dp, Alignment.CenterVertically),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				flags.forEachIndexed { index, isVisible ->
					RichTooltipBox(
						isVisible = isVisible.value,
						onDismissRequest = { isVisible.value = false },
						tooltip = RichTooltipText(
							lines = listOf("Body $index"),
							testTag = bodyTag(index)
						)
					) {
						Box(
							modifier = Modifier
								.size(48.dp)
								.testTag(targetTag(index))
								.clickable(role = Role.Button) { isVisible.value = !isVisible.value }
						)
					}
				}
			}
		}

		onNodeWithTag(targetTag(0)).performTouchInput { click() }
		assertNodeVisible(bodyTag(0), useUnmergedTree = true)

		onNodeWithTag(targetTag(1)).performTouchInput { click() }
		waitUntilAbsent(bodyTag(0))
		assertNodeVisible(bodyTag(1), useUnmergedTree = true)

		onNodeWithTag(targetTag(0)).performTouchInput { click() }
		assertNodeVisible(bodyTag(0), useUnmergedTree = true)
		waitUntilAbsent(bodyTag(1))
	}

	@Test
	fun when_userTapsOutside_then_tooltipDisappears() = runTuIndiceUiTest {
		val isVisible = mutableStateOf(false)

		setTuIndiceTestContent {
			Box(modifier = Modifier.fillMaxSize()) {
				Column(
					modifier = Modifier.fillMaxSize(),
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					RichTooltipBox(
						isVisible = isVisible.value,
						onDismissRequest = { isVisible.value = false },
						tooltip = RichTooltipText(
							lines = listOf("Fuera"),
							testTag = bodyTag(0)
						)
					) {
						Box(
							modifier = Modifier
								.size(48.dp)
								.testTag(targetTag(0))
								.clickable(role = Role.Button) { isVisible.value = true }
						)
					}
				}

				Box(
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.size(48.dp)
						.testTag(OutsideTag)
				)
			}
		}

		onNodeWithTag(targetTag(0)).performTouchInput { click() }
		assertNodeVisible(bodyTag(0), useUnmergedTree = true)

		onNodeWithTag(OutsideTag).performTouchInput { click() }
		waitUntilAbsent(bodyTag(0))
	}

	// Two anchors with one shared selection, as a chart keeps one open tooltip at a time.
	@Composable
	private fun SelectionHost() {
		var selected by remember { mutableStateOf<Int?>(null) }

		Column(
			modifier = Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(120.dp, Alignment.CenterVertically),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			repeat(2) { index ->
				RichTooltipBox(
					isVisible = selected == index,
					onDismissRequest = {
						if (selected == index) selected = null
					},
					tooltip = RichTooltipText(
						lines = listOf("Body $index"),
						testTag = bodyTag(index)
					)
				) {
					Box(
						modifier = Modifier
							.size(48.dp)
							.testTag(targetTag(index))
							.clickable(role = Role.Button) { selected = index }
					)
				}
			}
		}
	}

	private fun ComposeUiTest.waitUntilAbsent(tag: String) {
		waitUntil(timeoutMillis = NODE_TIMEOUT_MILLIS) {
			onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
		}
	}

	private fun targetTag(index: Int): String = "rich_tooltip_target_$index"

	private fun bodyTag(index: Int): String = "rich_tooltip_body_$index"

	private companion object {
		const val NODE_TIMEOUT_MILLIS = 5_000L
		const val OutsideTag = "rich_tooltip_outside"
	}
}
