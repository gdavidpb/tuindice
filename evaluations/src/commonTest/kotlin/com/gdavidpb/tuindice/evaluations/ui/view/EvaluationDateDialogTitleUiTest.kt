package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EvaluationDateDialogTitleUiTest {
	@Test
	fun when_aSubjectIsChosen_then_itsChipSitsBesideTheTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationDateDialogTitle(subjectCode = "CI5311")
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogTitle)
		onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogTitle).assertTextEquals("Fecha")
		onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogSubjectChip).assertTextEquals("CI5311")
	}

	@Test
	fun when_noSubjectIsChosen_then_onlyTheTitleShows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationDateDialogTitle(subjectCode = null)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogTitle)
		onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogSubjectChip).assertDoesNotExist()
	}
}
