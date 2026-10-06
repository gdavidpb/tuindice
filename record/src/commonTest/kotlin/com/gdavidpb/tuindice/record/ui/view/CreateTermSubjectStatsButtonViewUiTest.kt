package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CreateTermSubjectStatsButtonViewUiTest {
	@Test
	fun when_theButtonIsDrawn_then_itIsReadAsOpeningTheStatsOfItsSubject() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSubjectStatsButton(subjectCode = "MA1111", onClick = {})
		}

		// The icon carries no label on screen: the description is all a screen reader has.
		onNodeWithTag(RecordUiTags.createSyntheticTermSubjectStatsButton("MA1111"))
			.assertIsDisplayed()
			.assertIsEnabled()
			.assertContentDescriptionEquals("Ver estadísticas de MA1111")
	}

	@Test
	fun when_twoSubjectsOfferTheirStats_then_eachButtonAnswersToItsOwnSubject() = runTuIndiceUiTest {
		val opened = mutableListOf<String>()

		setTuIndiceTestContent {
			Row {
				CreateTermSubjectStatsButton(subjectCode = "MA1111", onClick = { opened += "MA1111" })
				CreateTermSubjectStatsButton(subjectCode = "FS1111", onClick = { opened += "FS1111" })
			}
		}

		onNodeWithTag(RecordUiTags.createSyntheticTermSubjectStatsButton("FS1111"))
			.assertContentDescriptionEquals("Ver estadísticas de FS1111")
			.performClick()
		onNodeWithTag(RecordUiTags.createSyntheticTermSubjectStatsButton("MA1111")).performClick()

		assertEquals(listOf("FS1111", "MA1111"), opened)
	}
}
