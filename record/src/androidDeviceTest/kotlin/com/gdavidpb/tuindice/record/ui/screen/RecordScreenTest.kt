package com.gdavidpb.tuindice.record.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.record.presentation.contract.Record
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordScreenTest {
	@get:Rule
	val composeRule = createAndroidComposeRule<ComponentActivity>()

	@Test
	fun failedState_whenRetryIsClicked_invokesRetryCallback() {
		var retryClicks = 0
		val retryLabel =
			composeRule.activity.getString(com.gdavidpb.tuindice.base.R.string.view_error_retry)

		composeRule.setContent {
			MaterialTheme {
				RecordScreen(
					state = Record.State.Failed,
					onRetryClick = { retryClicks++ },
					onSubjectGradeChange = { _, _, _, _ -> }
				)
			}
		}

		composeRule.onNodeWithText(retryLabel)
			.performClick()

		assertEquals(1, retryClicks)
	}

	@Test
	fun contentState_whenSliderChanges_invokesSubjectGradeCallbackWithExpectedParams() {
		val calls = mutableListOf<GradeChangeCall>()

		composeRule.setContent {
			MaterialTheme {
				RecordScreen(
					state = Record.State.Content(
						quarters = listOf(
							createQuarter(
								quarterId = "q1",
								subjectId = "s1",
								grade = 3,
								isReadOnly = false
							)
						)
					),
					onRetryClick = {},
					onSubjectGradeChange = { quarterId, subjectId, grade, isSelected ->
						calls += GradeChangeCall(
							quarterId = quarterId,
							subjectId = subjectId,
							grade = grade,
							isSelected = isSelected
						)
					}
				)
			}
		}

		composeRule.onNodeWithTag("subject_grade_slider_s1")
			.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
				setProgress(4f)
			}

		composeRule.waitUntil(timeoutMillis = 5_000L) {
			calls.any {
				it.quarterId == "q1" &&
						it.subjectId == "s1" &&
						it.grade == 4 &&
						!it.isSelected
			}
		}
	}

	@Test
	fun contentState_whenSubjectIsReadOnly_sliderIsNotShown() {
		composeRule.setContent {
			MaterialTheme {
				RecordScreen(
					state = Record.State.Content(
						quarters = listOf(
							createQuarter(
								quarterId = "q1",
								subjectId = "s1",
								grade = 3,
								isReadOnly = true
							)
						)
					),
					onRetryClick = {},
					onSubjectGradeChange = { _, _, _, _ -> }
				)
			}
		}

		val sliders = composeRule.onAllNodesWithTag("subject_grade_slider_s1")
			.fetchSemanticsNodes()

		assertEquals(0, sliders.size)
	}

	private fun createQuarter(
		quarterId: String,
		subjectId: String,
		grade: Int,
		isReadOnly: Boolean
	) = Quarter(
		id = quarterId,
		name = "Enero - Marzo 2024",
		startDate = 1704067200000L,
		endDate = 1711843200000L,
		grade = 4.5,
		gradeSum = 4.2,
		credits = 6,
		creditsSum = 12,
		isCurrent = true,
		isReadOnly = isReadOnly,
		subjects = listOf(
			Subject(
				id = subjectId,
				quarterId = quarterId,
				code = "MA1111",
				name = "MATEMATICAS I",
				credits = 4,
				grade = grade
			)
		)
	)
}

private data class GradeChangeCall(
	val quarterId: String,
	val subjectId: String,
	val grade: Int,
	val isSelected: Boolean
)
