package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import org.jetbrains.compose.resources.getString
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.outdated_app_action
import tuindice.base.generated.resources.outdated_app_message
import tuindice.base.generated.resources.outdated_app_title
import kotlin.test.Test
import kotlin.test.assertEquals

// Resolves resources through getString, so it runs on the iOS host only (androidHostTestExcludedPatterns).
@OptIn(ExperimentalTestApi::class)
class OutdatedAppScreenUiTest {
	@Test
	fun when_noTextsAreProvided_then_showsTheBundledCopyAndTheAnimation() = runTuIndiceUiTest {
		val title = getString(Res.string.outdated_app_title)
		val message = getString(Res.string.outdated_app_message)
		val actionLabel = getString(Res.string.outdated_app_action)

		setTuIndiceTestContent {
			OutdatedAppScreen(onUpdateClick = {})
		}

		waitForText(title)
		waitForText(message)
		waitForText(actionLabel)

		onNodeWithTag(BaseUiTags.OutdatedAppTitle).assertTextEquals(title)
		onNodeWithTag(BaseUiTags.OutdatedAppMessage).assertTextEquals(message)
		onNodeWithTag(BaseUiTags.OutdatedAppUpdateButton)
			.assertTextEquals(actionLabel)
			.assertHasClickAction()
			.assertIsEnabled()
		assertNodeVisible(BaseUiTags.OutdatedAppAnimation)
	}

	@Test
	fun when_updateButtonIsTapped_then_invokesTheUpdateCallbackOncePerTap() = runTuIndiceUiTest {
		var updateClicks = 0

		setTuIndiceTestContent {
			OutdatedAppScreen(
				title = "Actualiza para continuar",
				message = "Esta versión ya no es compatible.",
				actionLabel = "Ir a la tienda",
				onUpdateClick = { updateClicks++ }
			)
		}

		assertNodeVisible(BaseUiTags.OutdatedAppUpdateButton)
		onNodeWithTag(BaseUiTags.OutdatedAppUpdateButton).performClick()
		assertEquals(1, updateClicks)

		onNodeWithTag(BaseUiTags.OutdatedAppUpdateButton).performClick()
		assertEquals(2, updateClicks)
	}

	@Test
	fun when_customTextsAreProvided_then_rendersThemInsteadOfTheBundledCopy() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			OutdatedAppScreen(
				title = "Actualiza para continuar",
				message = "Esta versión ya no es compatible.",
				actionLabel = "Ir a la tienda",
				onUpdateClick = {}
			)
		}

		assertNodeVisible(BaseUiTags.OutdatedAppTitle)
		onNodeWithTag(BaseUiTags.OutdatedAppTitle).assertTextEquals("Actualiza para continuar")
		onNodeWithTag(BaseUiTags.OutdatedAppMessage).assertTextEquals("Esta versión ya no es compatible.")
		onNodeWithTag(BaseUiTags.OutdatedAppUpdateButton).assertTextEquals("Ir a la tienda")
	}

	@Test
	fun when_rendered_then_screenFillsTheWholeViewport() = runTuIndiceUiTest {
		setTuIndiceTestContent(sizeClass = TuIndiceTestSizeClass.Medium) {
			OutdatedAppScreen(
				title = "Actualiza para continuar",
				message = "Esta versión ya no es compatible.",
				actionLabel = "Ir a la tienda",
				onUpdateClick = {}
			)
		}

		assertNodeVisible(BaseUiTags.OutdatedAppScreen)
		onNodeWithTag(BaseUiTags.OutdatedAppScreen)
			.assertWidthIsEqualTo(TuIndiceTestSizeClass.Medium.widthDp.dp)
			.assertHeightIsEqualTo(TuIndiceTestSizeClass.Medium.heightDp.dp)
	}

	// stringResource resolves asynchronously off Android: wait for the text instead of reading the first frame.
	private fun ComposeUiTest.waitForText(text: String) {
		waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
			onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L
	}
}
