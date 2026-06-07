package com.gdavidpb.tuindice.pensum.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumCanvasItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusDisplay
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusIcon
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeVisualStyle
import com.gdavidpb.tuindice.pensum.presentation.model.PensumOptionItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSelection
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectDetailItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTermItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_service_unavailable

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
	fun when_stateIsFailed_then_displaysPersistentFailureCauseAndRetryAction() = runTuIndiceUiTest {
		var retryCount = 0

		setTuIndiceTestContent {
			PensumScreen(
				state = Pensum.State.Failed(
					message = UiText.Resource(Res.string.pensum_failed_service_unavailable)
				),
				onRetryClick = { retryCount += 1 },
				showSelectionSheet = false,
				onSelectionSheetDismiss = {},
				onSubjectStatsClick = {},
				onSelectionApplied = { _, _ -> }
			)
		}

		assertNodeVisible(BaseUiTags.ErrorViewContainer)
		onNodeWithText("No pudimos cargar el pensum").assertExists()
		onNodeWithText("El servicio no está disponible. Intenta de nuevo en unos minutos.").assertExists()
		onNodeWithTag(BaseUiTags.ErrorViewRetryButton)
			.assertHasClickAction()
			.performClick()
		assertEquals(1, retryCount)
	}

	@Test
	fun when_contentIsDisplayed_then_summaryShowsOnlyCareerInContextCard() = runTuIndiceUiTest {
		var contextClickCount = 0

		setTuIndiceTestContent {
			PensumScreen(
				state = Pensum.State.Content(model = samplePensumModelWithSelectableYears()),
				onRetryClick = {},
				showSelectionSheet = false,
				onSelectionSheetDismiss = {},
				onSubjectStatsClick = {},
				onSelectionApplied = { _, _ -> },
				onPensumContextClick = { contextClickCount += 1 }
			)
		}

		onNodeWithText("Ingenieria de Computacion").assertExists()
		onAllNodesWithText("Pensum 2019").assertCountEquals(0)
		onAllNodesWithText("Proyecto de Grado").assertCountEquals(0)
		onNodeWithTag(PensumUiTags.PensumContextSummary)
			.assertHasClickAction()
			.performClick()
		assertEquals(1, contextClickCount)
	}

	@Test
	fun when_nodeIsRealSubject_then_detailSheetStatsButtonNavigatesWithSubjectCode() = runTuIndiceUiTest {
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

		onNodeWithTag(PensumUiTags.node("ci4325")).performClick()
		onNodeWithTag(PensumUiTags.focusedNode("ci4325"), useUnmergedTree = true)
			.assertExists()
		onNodeWithTag(PensumUiTags.node("ci4325")).performClick()
		assertNodeHidden(PensumUiTags.focusedNode("ci4325"), useUnmergedTree = true)
		onNodeWithTag(PensumUiTags.node("ci4325")).performClick()
		onNodeWithTag(PensumUiTags.focusedNode("ci4325"), useUnmergedTree = true)
			.assertExists()
		assertNodeHidden(PensumUiTags.SubjectDetailSheet)
		assertNodeHidden(PensumUiTags.nodeSubjectStatsButton("ci4325"))
		onNodeWithTag(PensumUiTags.nodeDetailButton("ci4325"))
			.assertHasClickAction()
			.performClick()
		assertNodeVisible(PensumUiTags.SubjectDetailSheet)
		onNodeWithText("Detalle de materia").assertExists()
		onNodeWithTag(PensumUiTags.SubjectDetailCode)
			.assertTextEquals("CI4325")
		assertNodeVisible(PensumUiTags.SubjectDetailName)
		assertNodeVisible(PensumUiTags.SubjectDetailStatus)
		onNodeWithTag(PensumUiTags.SubjectDetailTermValue)
			.assertTextEquals("1° trimestre")
		onNodeWithText("Ver estadísticas").assertExists()
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()
		assertNodeHidden(PensumUiTags.SubjectDetailSheet)
		onNodeWithTag(PensumUiTags.focusedNode("ci4325"), useUnmergedTree = true)
			.assertExists()
		onNodeWithTag(PensumUiTags.nodeDetailButton("ci4325")).performClick()
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		assertEquals("CI4325", selectedSubjectCode)
	}

	@Test
	fun when_contentIsDisplayed_then_canvasLegendAndViewportControlsAreAvailable() = runTuIndiceUiTest {
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

		assertNodeVisible(PensumUiTags.CanvasLegend)
		onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.APPROVED)).assertHasClickAction()
		onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.CURRENT)).assertHasClickAction()
		onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.AVAILABLE)).assertHasClickAction()
		onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.BLOCKED)).assertHasClickAction()
		onNodeWithText("1° trimestre").assertExists()
		onNodeWithTag(PensumUiTags.FocusProgress).assertHasClickAction().performClick()
		assertNodeVisible(PensumUiTags.StickyTerms)
		onNodeWithTag(PensumUiTags.FitToScreen).assertHasClickAction().performClick()
		assertNodeHidden(PensumUiTags.MinimapToggle)
		assertNodeHidden(PensumUiTags.StickyTerms)
		assertNodeHidden(PensumUiTags.FitToScreen)
		onNodeWithTag(PensumUiTags.ZoomIn).assertHasClickAction().performClick()
		assertNodeVisible(PensumUiTags.StickyTerms)
		assertNodeVisible(PensumUiTags.FitToScreen)
		assertNodeVisible(PensumUiTags.MinimapToggle)
		onNodeWithTag(PensumUiTags.ZoomOut).assertHasClickAction()
		assertNodeHidden(PensumUiTags.Minimap)
		onNodeWithTag(PensumUiTags.MinimapToggle).assertHasClickAction().performClick()
		assertNodeVisible(PensumUiTags.Minimap)
		onNodeWithTag(PensumUiTags.FitToScreen).performClick()
		assertNodeHidden(PensumUiTags.MinimapToggle)
		assertNodeHidden(PensumUiTags.Minimap)
		assertNodeHidden(PensumUiTags.StickyTerms)
		assertNodeHidden(PensumUiTags.FitToScreen)
	}

	@Test
	fun when_statusLegendFiltersAreToggled_then_selectionIsMultiSelectAndResettable() = runTuIndiceUiTest {
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

		val availableFilter = PensumUiTags.statusFilter(PensumNodeStatusType.AVAILABLE)
		val currentFilter = PensumUiTags.statusFilter(PensumNodeStatusType.CURRENT)

		assertNodeHidden(PensumUiTags.StatusFilterClear)
		onNodeWithTag(availableFilter)
			.assertIsNotSelected()
			.performClick()
			.assertIsSelected()
		assertNodeVisible(PensumUiTags.StatusFilterClear)
		onNodeWithTag(currentFilter)
			.assertIsNotSelected()
			.performClick()
			.assertIsSelected()
		onNodeWithTag(PensumUiTags.node("approved-ee1111")).assertExists()
		onNodeWithTag(PensumUiTags.node("ci4325")).assertExists()
		onNodeWithTag(PensumUiTags.node("ea1")).assertExists()
		onNodeWithTag(PensumUiTags.node("blocked-ci9999")).assertExists()
		onNodeWithTag(PensumUiTags.StatusFilterClear)
			.assertHasClickAction()
			.performClick()
		onNodeWithTag(availableFilter).assertIsNotSelected()
		onNodeWithTag(currentFilter).assertIsNotSelected()
		assertNodeHidden(PensumUiTags.StatusFilterClear)
	}

	@Test
	fun when_contentHasNoCurrentFocus_then_hidesCurrentFocusButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumScreen(
				state = Pensum.State.Content(
					model = samplePensumModel().copy(isCurrentFocusVisible = false)
				),
				onRetryClick = {},
				showSelectionSheet = false,
				onSelectionSheetDismiss = {},
				onSubjectStatsClick = {},
				onSelectionApplied = { _, _ -> }
			)
		}

		assertNodeHidden(PensumUiTags.FocusProgress)
		onNodeWithTag(PensumUiTags.ZoomIn).assertHasClickAction()
		onNodeWithTag(PensumUiTags.ZoomOut).assertHasClickAction()
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
		onNodeWithTag(PensumUiTags.node("ea1")).performClick()
		assertNodeHidden(PensumUiTags.SubjectDetailSheet)
		onNodeWithTag(PensumUiTags.nodeDetailButton("ea1")).performClick()
		assertNodeVisible(PensumUiTags.SubjectDetailSheet)
		assertNodeVisible(PensumUiTags.SubjectDetailName)
		assertNodeVisible(PensumUiTags.SubjectDetailStatsUnavailable)
		onAllNodesWithText("Ver estadísticas").assertCountEquals(0)
	}

	@Test
	fun when_equivalentCodesAreSeparateNodes_then_displaysSeparateCards() = runTuIndiceUiTest {
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

		onNodeWithText("MA1111").assertExists()
		onNodeWithText("MA1121").assertExists()
		onAllNodesWithText("Matemáticas I").assertCountEquals(2)
	}

	@Test
	fun when_selectionSheetOpens_then_marksSelectedYearWithoutBasicCycle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumScreen(
				state = Pensum.State.Content(model = samplePensumModelWithSelectableYears()),
				onRetryClick = {},
				showSelectionSheet = true,
				onSelectionSheetDismiss = {},
				onSubjectStatsClick = {},
				onSelectionApplied = { _, _ -> }
			)
		}

		onAllNodesWithText("Ciclo Básico").assertCountEquals(0)
		onNodeWithText("Pensum actual").assertExists()
		onAllNodesWithText("Ingenieria de Computacion").assertCountEquals(2)
		onNodeWithText("0% avance · 0 / 8 UC aprobadas").assertExists()
		onNodeWithTag(PensumUiTags.versionOption(year = 2019)).assertIsSelected()
		onNodeWithTag(PensumUiTags.versionOption(year = 2018)).assertIsNotSelected()
		onNodeWithTag(PensumUiTags.modalityOption("degree_project")).assertIsSelected()
		onNodeWithTag(PensumUiTags.modalityOption("long_internship")).assertIsNotSelected()
	}
}

private fun samplePensumModel(): PensumScreenModel {
	return PensumScreenModel(
		careerName = "Ingenieria de Computacion",
		selection = PensumScreenSelection(
			year = 2019,
			modalityId = "degree_project"
		),
		pensumOptions = listOf(
			PensumOptionItem(
				id = "computacion-2019",
				year = 2019,
				modalityOptions = emptyList(),
				text = "2019"
			)
		),
		modalityOptions = emptyList(),
		progressPercent = 0,
		approvedCredits = 0,
		totalCredits = 8,
		isCurrentFocusVisible = true,
		canvas = PensumCanvasItem(width = 520.0, height = 700.0),
		terms = listOf(
			PensumTermItem(id = "T1", label = "Primer trimestre", x = 0.0, width = 240.0),
			PensumTermItem(id = "T2", label = "Segundo trimestre", x = 240.0, width = 240.0)
		),
		nodes = listOf(
			sampleNode(
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
				subjectStatsCode = "CI4325",
				status = currentNodeStatus(),
				visualStyle = currentNodeVisualStyle()
			),
			sampleNode(
				id = "approved-ee1111",
				displayCode = "EE1111",
				subjectCode = "EE1111",
				name = "Electiva General",
				credits = 3,
				termId = "T2",
				x = 270.0,
				y = 72.0,
				width = 190.0,
				height = 120.0,
				status = approvedNodeStatus(),
				visualStyle = approvedNodeVisualStyle()
			),
			sampleNode(
				id = "ea1",
				displayCode = "EA1",
				subjectCode = null,
				name = "Electiva de Área I",
				credits = 4,
				termId = "T1",
				x = 24.0,
				y = 240.0,
				width = 190.0,
				height = 144.0
			),
			sampleNode(
				id = "blocked-ci9999",
				displayCode = "CI9999",
				subjectCode = "CI9999",
				name = "Proyecto Integrador",
				credits = 4,
				termId = "T2",
				x = 270.0,
				y = 240.0,
				width = 190.0,
				height = 120.0,
				status = blockedNodeStatus(),
				visualStyle = blockedNodeVisualStyle()
			),
			sampleNode(
				id = "math1-ma1111",
				displayCode = "MA1111",
				subjectCode = "MA1111",
				name = "Matemáticas I",
				credits = 4,
				termId = "T1",
				x = 24.0,
				y = 408.0,
				width = 190.0,
				height = 120.0
			),
			sampleNode(
				id = "math1-ma1121",
				displayCode = "MA1121",
				subjectCode = "MA1121",
				name = "Matemáticas I",
				credits = 4,
				termId = "T1",
				x = 24.0,
				y = 552.0,
				width = 190.0,
				height = 120.0
			)
		),
		edges = emptyList()
	)
}

private fun samplePensumModelWithSelectableYears(): PensumScreenModel {
	val modalities = listOf(
		PensumModalityItem(
			id = "degree_project",
			name = "Proyecto de Grado",
			isDefault = true,
			text = "Proyecto de Grado"
		),
		PensumModalityItem(
			id = "long_internship",
			name = "Pasantía Larga",
			isDefault = false,
			text = "Pasantía Larga"
		)
	)

	return samplePensumModel().copy(
		selection = PensumScreenSelection(
			year = 2019,
			modalityId = "degree_project"
		),
		pensumOptions = listOf(
			PensumOptionItem(
				id = "2018",
				year = 2018,
				modalityOptions = listOf(
					PensumModalityItem(
						id = "degree_project",
						name = "Proyecto de Grado",
						isDefault = true,
						text = "Proyecto de Grado"
					)
				),
				text = "2018"
			),
			PensumOptionItem(
				id = "2019",
				year = 2019,
				modalityOptions = modalities,
				text = "2019"
			)
		),
		modalityOptions = modalities
	)
}

private fun sampleNode(
	id: String,
	displayCode: String,
	subjectCode: String?,
	name: String,
	credits: Int,
	termId: String,
	x: Double,
	y: Double,
	width: Double,
	height: Double,
	subjectStatsCode: String? = null,
	status: PensumNodeStatusDisplay = availableNodeStatus(),
	visualStyle: PensumNodeVisualStyle = availableNodeVisualStyle()
): PensumNodeItem {
	val creditsText = "$credits UC"

	return PensumNodeItem(
		id = id,
		displayCode = displayCode,
		subjectCode = subjectCode,
		name = name,
		displayName = name,
		credits = credits,
		creditsText = creditsText,
		termId = termId,
		x = x,
		y = y,
		width = width,
		height = height,
		visualStyle = visualStyle,
		status = status,
		subjectStatsCode = subjectStatsCode,
		fulfilledSubject = null,
		detail = PensumSubjectDetailItem(
			code = displayCode,
			name = name,
			status = status,
			termLabel = "1° trimestre",
			creditsText = creditsText,
			statsCode = subjectStatsCode,
			fulfilledSubject = null
		)
	)
}

private fun approvedNodeStatus(): PensumNodeStatusDisplay {
	return PensumNodeStatusDisplay(
		type = PensumNodeStatusType.APPROVED,
		icon = PensumNodeStatusIcon.CHECK,
		colorArgb = 0xFF8FE38C
	)
}

private fun currentNodeStatus(): PensumNodeStatusDisplay {
	return PensumNodeStatusDisplay(
		type = PensumNodeStatusType.CURRENT,
		icon = PensumNodeStatusIcon.CURRENT_ROUTE,
		colorArgb = 0xFFFFC400
	)
}

private fun availableNodeStatus(): PensumNodeStatusDisplay {
	return PensumNodeStatusDisplay(
		type = PensumNodeStatusType.AVAILABLE,
		icon = PensumNodeStatusIcon.ADD,
		colorArgb = 0xFF8A8F94
	)
}

private fun blockedNodeStatus(): PensumNodeStatusDisplay {
	return PensumNodeStatusDisplay(
		type = PensumNodeStatusType.BLOCKED,
		icon = PensumNodeStatusIcon.LOCK,
		colorArgb = 0xFF686B70
	)
}

private fun approvedNodeVisualStyle(): PensumNodeVisualStyle {
	return PensumNodeVisualStyle(
		containerArgb = 0xFF171819,
		borderArgb = 0xFF8FE38C,
		chipArgb = 0xFFB8F4A8,
		chipTextArgb = 0xFF1D5B25,
		textArgb = 0xFFF7F7F7,
		secondaryTextArgb = 0xFF9C9EA3
	)
}

private fun currentNodeVisualStyle(): PensumNodeVisualStyle {
	return PensumNodeVisualStyle(
		containerArgb = 0xFF171819,
		borderArgb = 0xFFFFC400,
		chipArgb = 0xFFF7E6A6,
		chipTextArgb = 0xFF5A4A00,
		textArgb = 0xFFF7F7F7,
		secondaryTextArgb = 0xFF9C9EA3
	)
}

private fun blockedNodeVisualStyle(): PensumNodeVisualStyle {
	return PensumNodeVisualStyle(
		containerArgb = 0xFF171819,
		borderArgb = 0xFF686B70,
		chipArgb = 0xFFB7B8BA,
		chipTextArgb = 0xFF383A3D,
		textArgb = 0xFFF7F7F7,
		secondaryTextArgb = 0xFF9C9EA3
	)
}

private fun availableNodeVisualStyle(): PensumNodeVisualStyle {
	return PensumNodeVisualStyle(
		containerArgb = 0xFF171819,
		borderArgb = 0xFF8A8F94,
		chipArgb = 0xFFEBDDA3,
		chipTextArgb = 0xFF534500,
		textArgb = 0xFFF7F7F7,
		secondaryTextArgb = 0xFF9C9EA3
	)
}
