package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.recordMapperTexts
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class QuarterItemUiTest {
	@Test
	fun when_quartersMapped_then_createsQuarterItemsWithTextsAndSubjects() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val items = listOf(DEFAULT_RECORD_QUARTER).toQuarterItemList(
				texts = recordMapperTexts(),
				highlightColor = Color(0xFFB8860B)
			)
			val item = items.first()

			Column {
				Text(text = item.nameText)
				Text(text = item.gradeText.text)
				Text(text = item.gradeSumText.text)
				Text(text = item.creditsText.text)
				Text(text = item.subjects.first().nameText)
			}
		}

		onNodeWithText("2026-1").assertIsDisplayed()
		onNodeWithText("Δx 70.0").assertIsDisplayed()
		onNodeWithText("∑x 70.0").assertIsDisplayed()
		onNodeWithText("⦿ 6 UC").assertIsDisplayed()
		onNodeWithText("Programacion").assertIsDisplayed()
	}

	@Test
	fun when_quarterValueAnnotated_then_preservesTextContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Text(
				text = "Δx 4.2104"
					.annotatedQuarterValue(highlightColor = Color(0xFFB8860B))
					.text
			)
		}

		onNodeWithText("Δx 4.2104").assertIsDisplayed()
	}
}
