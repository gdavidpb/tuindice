package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_APPROVED_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_CURRENT_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.sampleAvailablePensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleCurrentPensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleCurrentPensumNodeWithRelations
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationDirection
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationTarget
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumSubjectDetailBottomSheetUiTest {
	@Test
	fun when_nodeHasStatsCode_then_statsButtonReportsItAndKeepsTheSheetOpen() = runTuIndiceUiTest {
		val recorder = SubjectDetailSheetRecorder()
		setSubjectDetailSheetContent(recorder = recorder, node = sampleCurrentPensumNode())

		onNodeWithText("Detalle de materia").assertExists()
		onNodeWithTag(PensumUiTags.SubjectDetailCode).assertTextEquals("CI4325")
		assertNodeHidden(PensumUiTags.SubjectDetailStatsUnavailable)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton)
			.assertTextEquals("Ver estadísticas")
			.performClick()
		waitForIdle()

		assertEquals(listOf("CI4325"), recorder.statsCodes)
		assertEquals(0, recorder.dismissCount)
		assertNodeVisible(PensumUiTags.SubjectDetailSheet)
	}

	@Test
	fun when_nodeHasNoStatsCode_then_explainsItAndOffersNoStatsButton() = runTuIndiceUiTest {
		setSubjectDetailSheetContent(
			recorder = SubjectDetailSheetRecorder(),
			node = sampleAvailablePensumNode()
		)

		onNodeWithTag(PensumUiTags.SubjectDetailStatsUnavailable)
			.assertTextEquals("Las estadísticas no están disponibles para esta materia.")
		assertNodeHidden(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).assertTextEquals("Cerrar")
	}

	@Test
	fun when_nodeHasNoRelations_then_moreDetailIsNotOffered() = runTuIndiceUiTest {
		setSubjectDetailSheetContent(
			recorder = SubjectDetailSheetRecorder(),
			node = sampleCurrentPensumNode(),
			shouldStartExpanded = true
		)

		assertNodeVisible(PensumUiTags.SubjectDetailSheet)
		assertNodeHidden(PensumUiTags.SubjectDetailMoreButton)
		assertNodeHidden(PensumUiTags.SubjectDetailRouteContext)
	}

	@Test
	fun when_moreDetailIsTapped_then_routeAndCorequisitesReplaceTheButton() = runTuIndiceUiTest {
		setSubjectDetailSheetContent(
			recorder = SubjectDetailSheetRecorder(),
			node = sampleCurrentPensumNodeWithRelations()
		)

		assertNodeVisible(PensumUiTags.SubjectDetailMoreButton)
		assertNodeHidden(PensumUiTags.SubjectDetailRouteContext)
		onNodeWithTag(PensumUiTags.SubjectDetailMoreButton).performClick()
		waitForIdle()

		assertNodeVisible(PensumUiTags.SubjectDetailRouteContext)
		assertNodeVisible(PensumUiTags.SubjectDetailCorequisites)
		assertNodeHidden(PensumUiTags.SubjectDetailMoreButton)
	}

	@Test
	fun when_sheetStartsExpanded_then_relatedSubjectTapReportsItsNavigationTarget() =
		runTuIndiceUiTest {
			val recorder = SubjectDetailSheetRecorder()
			setSubjectDetailSheetContent(
				recorder = recorder,
				node = sampleCurrentPensumNodeWithRelations(),
				shouldStartExpanded = true
			)

			assertNodeHidden(PensumUiTags.SubjectDetailMoreButton)
			assertNodeVisible(PensumUiTags.SubjectDetailRouteContext)
			onNodeWithTag(PensumUiTags.subjectDetailRequirement(SAMPLE_APPROVED_NODE_ID)).performClick()
			waitForIdle()

			assertEquals(
				listOf(
					PensumSubjectDetailNavigationTarget(
						nodeId = SAMPLE_APPROVED_NODE_ID,
						originNodeId = SAMPLE_CURRENT_NODE_ID,
						direction = PensumSubjectDetailNavigationDirection.Backward
					)
				),
				recorder.navigationTargets
			)
		}

	@Test
	fun when_closeIsTapped_then_requestsDismissWithoutOpeningStats() = runTuIndiceUiTest {
		val recorder = SubjectDetailSheetRecorder()
		setSubjectDetailSheetContent(recorder = recorder, node = sampleCurrentPensumNode())

		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()
		waitUntil(timeoutMillis = TIMEOUT_MILLIS) { recorder.dismissCount == 1 }

		assertEquals(emptyList(), recorder.statsCodes)
	}
}

private class SubjectDetailSheetRecorder {
	val statsCodes = mutableListOf<String>()
	val navigationTargets = mutableListOf<PensumSubjectDetailNavigationTarget>()
	var dismissCount = 0
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setSubjectDetailSheetContent(
	recorder: SubjectDetailSheetRecorder,
	node: PensumNodeItem,
	shouldStartExpanded: Boolean = false
) {
	setTuIndiceTestContent {
		PensumSubjectDetailBottomSheet(
			node = node,
			shouldStartExpanded = shouldStartExpanded,
			navigationOriginNodeId = null,
			navigationDirection = null,
			onSubjectStatsClick = { subjectCode -> recorder.statsCodes += subjectCode },
			onRelatedSubjectClick = { target -> recorder.navigationTargets += target },
			onDismissRequest = { recorder.dismissCount += 1 }
		)
	}
}

private const val TIMEOUT_MILLIS = 5_000L
