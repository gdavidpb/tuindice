package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PensumCanvasLegendUiTest {
	@Test
	fun when_noFilterIsActive_then_showsFourUnselectedStatusesWithoutClearAction() = runTuIndiceUiTest {
		setLegendContent(recorder = LegendRecorder(), activeStatusFilters = emptySet())

		assertNodeVisible(PensumUiTags.CanvasLegend)
		STATUS_LABELS.forEach { (statusType, label) ->
			onNodeWithTag(PensumUiTags.statusFilter(statusType))
				.assertTextEquals(label)
				.assertIsNotSelected()
				.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
		}
		assertNodeHidden(PensumUiTags.StatusFilterClear)
	}

	@Test
	fun when_filtersAreActive_then_onlyThoseAreSelectedAndClearActionAppears() = runTuIndiceUiTest {
		setLegendContent(
			recorder = LegendRecorder(),
			activeStatusFilters = setOf(PensumNodeStatusType.CURRENT, PensumNodeStatusType.BLOCKED)
		)

		onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.CURRENT)).assertIsSelected()
		onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.BLOCKED)).assertIsSelected()
		onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.APPROVED)).assertIsNotSelected()
		onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.AVAILABLE)).assertIsNotSelected()
		onNodeWithTag(PensumUiTags.StatusFilterClear)
			.assertContentDescriptionEquals("Limpiar filtros")
	}

	@Test
	fun when_statusIsTapped_then_reportsThatStatusTypeWithoutClearing() = runTuIndiceUiTest {
		val recorder = LegendRecorder()
		setLegendContent(recorder = recorder, activeStatusFilters = emptySet())

		onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.CURRENT)).performClick()
		onNodeWithTag(PensumUiTags.statusFilter(PensumNodeStatusType.APPROVED)).performClick()

		runOnIdle {
			assertEquals(
				listOf(PensumNodeStatusType.CURRENT, PensumNodeStatusType.APPROVED),
				recorder.toggledStatusTypes
			)
			assertEquals(0, recorder.clearCount)
		}
	}

	@Test
	fun when_clearIsTapped_then_requestsClearWithoutTogglingAnyStatus() = runTuIndiceUiTest {
		val recorder = LegendRecorder()
		setLegendContent(
			recorder = recorder,
			activeStatusFilters = setOf(PensumNodeStatusType.APPROVED)
		)

		onNodeWithTag(PensumUiTags.StatusFilterClear).performClick()

		runOnIdle {
			assertEquals(1, recorder.clearCount)
			assertEquals(emptyList(), recorder.toggledStatusTypes)
		}
	}
}

private class LegendRecorder {
	val toggledStatusTypes = mutableListOf<PensumNodeStatusType>()
	var clearCount = 0
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setLegendContent(
	recorder: LegendRecorder,
	activeStatusFilters: Set<PensumNodeStatusType>
) {
	setTuIndiceTestContent {
		PensumCanvasLegend(
			activeStatusFilters = activeStatusFilters,
			onStatusFilterToggle = { statusType -> recorder.toggledStatusTypes += statusType },
			onClearStatusFilters = { recorder.clearCount += 1 }
		)
	}
}

private val STATUS_LABELS = mapOf(
	PensumNodeStatusType.APPROVED to "Aprobada",
	PensumNodeStatusType.CURRENT to "En curso",
	PensumNodeStatusType.AVAILABLE to "Disponible",
	PensumNodeStatusType.BLOCKED to "Bloqueada"
)
