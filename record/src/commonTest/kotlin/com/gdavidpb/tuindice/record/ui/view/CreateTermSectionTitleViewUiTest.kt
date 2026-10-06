package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.record.testing.isProgressIndicator
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CreateTermSectionTitleViewUiTest {
	@Test
	fun when_theSectionIsAtRest_then_itsTitleIsShownInBold_withNoSpinner() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSectionTitle(text = "Materias del trimestre")
		}

		onNodeWithText("Materias del trimestre")
			.assertIsDisplayed()
			.assertHasNoClickAction()
		assertEquals(
			expected = FontWeight.Bold,
			actual = onNodeWithText("Materias del trimestre").textLayout().layoutInput.style.fontWeight
		)
		onAllNodes(isProgressIndicator()).assertCountEquals(0)
	}

	@Test
	fun when_theSectionIsRefreshing_then_aSpinnerIsPushedToTheEndOfTheTitlesLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSectionTitle(text = "Sugeridas por tu pensum", isRefreshing = true)
		}

		onNodeWithText("Sugeridas por tu pensum").assertIsDisplayed()
		onNode(isProgressIndicator()).assertIsDisplayed()

		val title = onNodeWithText("Sugeridas por tu pensum").getUnclippedBoundsInRoot()
		val spinner = onNode(isProgressIndicator()).getUnclippedBoundsInRoot()

		// The row spans the width it is given: the title starts it and the spinner ends it.
		assertTrue(spinner.left - title.right > spinner.right - spinner.left, "the spinner is at the far end")
		assertTrue(spinner.top < title.bottom && title.top < spinner.bottom, "both are on the same line")
	}
}
