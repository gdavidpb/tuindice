package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.record.testing.fitsWhole
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CreateTermSubjectCodeChipViewUiTest {
	@Test
	fun when_aSubjectCodeIsGiven_then_theChipShowsItWhole_inTheColorOfThatCode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSubjectCodeChip(subjectCode = "MA1111")
		}

		onNodeWithText("MA1111")
			.assertIsDisplayed()
			// The chip names the subject; adding it is the job of the button next to it.
			.assertHasNoClickAction()

		val layout = onNodeWithText("MA1111").textLayout()

		assertEquals(CourseCodeColorGenerator.fromCode("MA1111").color, layout.layoutInput.style.color)
		assertTrue(layout.fitsWhole, "a code is drawn whole on its one line")
	}

	@Test
	fun when_twoSubjectsAreListed_then_eachChipTakesTheColorOfItsOwnCode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				CreateTermSubjectCodeChip(subjectCode = "MA1111")
				CreateTermSubjectCodeChip(subjectCode = "FS1111")
			}
		}

		// The same code is the same colour wherever it is drawn: here, in the record and in the pensum.
		listOf("MA1111", "FS1111").forEach { code ->
			assertEquals(
				expected = CourseCodeColorGenerator.fromCode(code).color,
				actual = onNodeWithText(code).assertIsDisplayed().textLayout().layoutInput.style.color,
				message = "the colour of $code"
			)
		}
	}
}
