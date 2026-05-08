package com.gdavidpb.tuindice.pensum.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumScreenUiTest {
	@Test
	fun when_stateIsEmpty_then_displaysEmptyViewWithAnimationAndNoRetryAction() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumScreen(
				state = Pensum.State.Empty,
				onRetryClick = {},
				showSelectionSheet = false,
				onSelectionSheetDismiss = {},
				onSubjectStatsClick = {},
				onSelectionApplied = { _, _ -> }
			)
		}

		assertNodeVisible(BaseUiTags.EmptyViewContainer)
		assertNodeVisible(BaseUiTags.EmptyViewTitle)
		assertNodeVisible(BaseUiTags.EmptyViewMessage)
		assertNodeVisible(BaseUiTags.EmptyStateAnimation)
		assertNodeHidden(BaseUiTags.EmptyViewActionButton)
	}

	@Test
	fun when_nodeIsRealSubject_then_statsButtonNavigatesWithSubjectCode() = runTuIndiceUiTest {
		var selectedSubjectCode: String? = null

		setTuIndiceTestContent {
			PensumScreen(
				state = Pensum.State.Content(model = samplePensumModel()),
				onRetryClick = {},
				showSelectionSheet = false,
				onSelectionSheetDismiss = {},
				onSubjectStatsClick = { subjectCode -> selectedSubjectCode = subjectCode },
				onSelectionApplied = { _, _ -> }
			)
		}

		assertNodeVisible(PensumUiTags.nodeSubjectStatsButton("ci4325"))
		onNodeWithTag(PensumUiTags.nodeSubjectStatsButton("ci4325")).performClick()

		assertEquals("CI4325", selectedSubjectCode)
	}

	@Test
	fun when_nodeIsWildcardSlot_then_statsButtonIsHidden() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumScreen(
				state = Pensum.State.Content(model = samplePensumModel()),
				onRetryClick = {},
				showSelectionSheet = false,
				onSelectionSheetDismiss = {},
				onSubjectStatsClick = {},
				onSelectionApplied = { _, _ -> }
			)
		}

		assertNodeHidden(PensumUiTags.nodeSubjectStatsButton("ea1"))
	}
}

private fun samplePensumModel(): PensumScreenModel {
	return PensumScreenModel(
		selection = PensumScreenModel.Selection(
			careerCode = 15,
			year = 2019,
			modalityId = "degree_project"
		),
		pensumOptions = listOf(
			PensumScreenModel.PensumOptionItem(
				id = "computacion-2019",
				careerCode = 15,
				careerName = "Computación",
				year = 2019,
				text = "2019 - Computación"
			)
		),
		modalityOptions = emptyList(),
		progressPercent = 0,
		approvedCredits = 0,
		totalCredits = 8,
		canvas = PensumScreenModel.Canvas(width = 520.0, height = 520.0),
		terms = listOf(PensumScreenModel.Term(id = "T1", label = "T1", x = 0.0, width = 240.0)),
		nodes = listOf(
			PensumScreenModel.Node(
				id = "ci4325",
				displayCode = "CI4325",
				subjectCode = "CI4325",
				name = "Interfaces con el Usuario",
				credits = 5,
				termId = "T1",
				x = 24.0,
				y = 72.0,
				width = 190.0,
				height = 144.0,
				status = PensumNodeStatus.AVAILABLE,
				hasSubjectStatsAction = true
			),
			PensumScreenModel.Node(
				id = "ea1",
				displayCode = "EA1",
				subjectCode = null,
				name = "Electiva de Área I",
				credits = 4,
				termId = "T1",
				x = 24.0,
				y = 240.0,
				width = 190.0,
				height = 144.0,
				status = PensumNodeStatus.AVAILABLE,
				hasSubjectStatsAction = false
			)
		),
		edges = emptyList()
	)
}
