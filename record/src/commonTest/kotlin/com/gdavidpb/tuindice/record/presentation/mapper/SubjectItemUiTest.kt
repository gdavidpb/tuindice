package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.record.testing.recordMapperTexts
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectItemUiTest {
	@Test
	fun when_subjectIsRetired_then_mapsRetiredStatusAndDashGrade() = runTuIndiceUiTest {
		val subject = Subject(
			id = "subject-1",
			quarterId = "quarter-1",
			code = "FS1113",
			name = "FISICA III",
			credits = 3,
			grade = 0
		)

		setTuIndiceTestContent {
			val mapped = subject.toSubjectItem(
				isReadOnly = false,
				texts = recordMapperTexts()
			)

			Column {
				Text(text = mapped.codeAndStatusText.text)
				Text(text = mapped.gradeText)
				Text(text = mapped.creditsText)
			}
		}

		onNodeWithText("FS1113 (Retirada)").assertIsDisplayed()
		onNodeWithText("—").assertIsDisplayed()
		onNodeWithText("3 UC").assertIsDisplayed()
	}

	@Test
	fun when_subjectIsActive_then_mapsCodeWithoutStatusAndNumericGrade() = runTuIndiceUiTest {
		val subject = Subject(
			id = "subject-2",
			quarterId = "quarter-1",
			code = "MA1112",
			name = "MATEMATICAS II",
			credits = 4,
			grade = 5
		)

		setTuIndiceTestContent {
			val mapped = subject.toSubjectItem(
				isReadOnly = true,
				texts = recordMapperTexts()
			)

			Column {
				Text(text = mapped.codeAndStatusText.text)
				Text(text = mapped.gradeText)
				Text(text = mapped.creditsText)
			}
		}

		onNodeWithText("MA1112").assertIsDisplayed()
		onNodeWithText("5 / 5").assertIsDisplayed()
		onNodeWithText("4 UC").assertIsDisplayed()
	}
}
