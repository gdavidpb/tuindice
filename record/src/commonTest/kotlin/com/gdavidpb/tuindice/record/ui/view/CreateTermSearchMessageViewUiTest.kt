package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.testing.isProgressIndicator
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CreateTermSearchMessageViewUiTest {
	@Test
	fun when_onlyATitleIsGiven_then_itStandsAlone_withNoSpinner() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSearchMessage(
				modifier = Modifier.testTag(MessageTag),
				title = "3 resultados"
			)
		}

		onNodeWithText("3 resultados").assertIsDisplayed()
		onAllNodes(isProgressIndicator()).assertCountEquals(0)
		// The title is the only text the message holds.
		onAllNodes(hasAnyAncestor(hasTestTag(MessageTag))).assertCountEquals(1)
	}

	@Test
	fun when_aDescriptionIsGiven_then_itExplainsTheTitle_underIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSearchMessage(
				title = "No encontramos materias",
				description = "No hay resultados para \"xyz\". Prueba con otro código o nombre."
			)
		}

		val title = onNodeWithText("No encontramos materias").assertIsDisplayed().getUnclippedBoundsInRoot()
		val description = onNodeWithText("No hay resultados para \"xyz\". Prueba con otro código o nombre.")
			.assertIsDisplayed()
			.getUnclippedBoundsInRoot()

		assertTrue(title.bottom <= description.top, "the description goes under the title")
	}

	@Test
	fun when_theDescriptionIsBlank_then_noEmptyLineIsLeftUnderTheTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSearchMessage(
				modifier = Modifier.testTag(MessageTag),
				title = "3 resultados",
				description = "   "
			)
		}

		onNodeWithText("3 resultados").assertIsDisplayed()
		onAllNodes(hasAnyAncestor(hasTestTag(MessageTag))).assertCountEquals(1)
	}

	@Test
	fun when_theSearchIsRefreshing_then_aSpinnerSharesTheLineWithTheTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSearchMessage(title = "3 resultados", isRefreshing = true)
		}

		onNodeWithText("3 resultados").assertIsDisplayed()
		onNode(isProgressIndicator()).assertIsDisplayed()

		val title = onNodeWithText("3 resultados").getUnclippedBoundsInRoot()
		val spinner = onNode(isProgressIndicator()).getUnclippedBoundsInRoot()

		assertTrue(title.right <= spinner.left, "the spinner sits at the end of the title's line")
		assertTrue(spinner.top < title.bottom && title.top < spinner.bottom, "both are on the same line")
	}

	private companion object {
		const val MessageTag = "search_message"
	}
}
