package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.testing.samplePensumStatusDisplay
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumSubjectRelationStatusBadgeUiTest {
	@Test
	fun when_statusChanges_then_badgeShowsTheLegendLabelOfEachStatus() = runTuIndiceUiTest {
		val statusTypeState = mutableStateOf(PensumNodeStatusType.APPROVED)

		setTuIndiceTestContent {
			PensumSubjectRelationStatusBadge(
				status = samplePensumStatusDisplay(statusTypeState.value)
			)
		}

		STATUS_LABELS.forEach { (statusType, label) ->
			runOnIdle { statusTypeState.value = statusType }
			waitForIdle()

			onNodeWithText(label).assertExists()
			STATUS_LABELS.values.filter { otherLabel -> otherLabel != label }.forEach { otherLabel ->
				onAllNodesWithText(otherLabel).assertCountEquals(0)
			}
		}
	}

	@Test
	fun when_modifierCarriesATag_then_labelIsRenderedInsideTheTaggedBadge() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectRelationStatusBadge(
				modifier = Modifier.testTag(BADGE_TAG),
				status = samplePensumStatusDisplay(PensumNodeStatusType.CURRENT)
			)
		}

		onNode(hasText("En curso") and hasParent(hasTestTag(BADGE_TAG))).assertExists()
	}
}

private const val BADGE_TAG = "pensum_subject_relation_status_badge"

private val STATUS_LABELS = mapOf(
	PensumNodeStatusType.APPROVED to "Aprobada",
	PensumNodeStatusType.CURRENT to "En curso",
	PensumNodeStatusType.AVAILABLE to "Disponible",
	PensumNodeStatusType.BLOCKED to "Bloqueada"
)
