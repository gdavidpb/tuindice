package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationGradeWheelPickerUiTest {
	@Test
	fun when_rendered_then_displaysPickerAndSelectionFrame() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationGradeWheelPicker(
				grade = 15.0,
				onGradeChange = {}
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationGradeWheelPicker)
		assertNodeVisible(EvaluationsUiTags.EvaluationGradeWheelSelectedFrame)
	}

	@Test
	fun when_wheelScrolled_then_dispatchesGradeChange() = runTuIndiceUiTest {
		var changedGrade: Double? = null

		setTuIndiceTestContent {
			EvaluationGradeWheelPicker(
				grade = 15.0,
				onGradeChange = { grade ->
					changedGrade = grade
				}
			)
		}

		onAllNodesWithTag(BaseUiTags.WheelPickerList).assertCountEquals(2)
		onAllNodesWithTag(BaseUiTags.WheelPickerList)[0]
			.performTouchInput { swipeUp() }

		waitUntil(timeoutMillis = 2_000) {
			changedGrade != null
		}

		assertNotNull(changedGrade)
	}

	@Test
	fun when_wheelScrolledWithLimitedRange_then_dispatchedGradeStaysInsideRange() = runTuIndiceUiTest {
		var changedGrade: Double? = null

		setTuIndiceTestContent {
			EvaluationGradeWheelPicker(
				grade = 10.0,
				gradeRange = 0.0..20.0,
				onGradeChange = { grade ->
					changedGrade = grade
				}
			)
		}

		onAllNodesWithTag(BaseUiTags.WheelPickerList)[1]
			.performTouchInput { swipeUp() }

		waitUntil(timeoutMillis = 2_000) {
			changedGrade != null
		}

		assertNotNull(changedGrade)
		assertTrue(changedGrade in 0.0..20.0)
	}
}
