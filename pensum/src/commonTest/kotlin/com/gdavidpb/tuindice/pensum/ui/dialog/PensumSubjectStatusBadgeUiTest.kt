package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.testing.samplePensumStatusDisplay
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumSubjectStatusBadgeUiTest {
	@Test
	fun when_statusIsApproved_then_badgeSaysApproved() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectStatusBadge(status = samplePensumStatusDisplay(PensumNodeStatusType.APPROVED))
		}

		onNodeWithTag(PensumUiTags.SubjectDetailStatus).assertTextEquals("Aprobada")
	}

	@Test
	fun when_statusChanges_then_badgeFollowsWithTheLegendLabel() = runTuIndiceUiTest {
		val statusTypeState = mutableStateOf(PensumNodeStatusType.CURRENT)

		setTuIndiceTestContent {
			PensumSubjectStatusBadge(status = samplePensumStatusDisplay(statusTypeState.value))
		}

		onNodeWithTag(PensumUiTags.SubjectDetailStatus).assertTextEquals("En curso")
		runOnIdle { statusTypeState.value = PensumNodeStatusType.AVAILABLE }
		waitForIdle()
		onNodeWithTag(PensumUiTags.SubjectDetailStatus).assertTextEquals("Disponible")
		runOnIdle { statusTypeState.value = PensumNodeStatusType.BLOCKED }
		waitForIdle()
		onNodeWithTag(PensumUiTags.SubjectDetailStatus).assertTextEquals("Bloqueada")
	}
}
