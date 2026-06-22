package com.gdavidpb.tuindice.wizard.presentation.mapper

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumCanvasItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSelection
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.RecordRouteViewState
import com.gdavidpb.tuindice.record.presentation.model.RecordTopBarViewModeState
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.presentation.model.contextualCoachmarks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachmarkSurfaceMapperTest {
	@Test
	fun toCoachmarkSurface_mapsRecordRouteContentStateToRecordCoachmarks() {
		val surface = RecordRouteViewState(
			topBarViewModeState = RecordTopBarViewModeState(
				selectedMode = RecordViewMode.Projection
			)
		).toCoachmarkSurface(visitKey = "record:1")

		assertEquals("record:1", surface.visitKey)
		assertEquals(
			listOf(CoachmarkId.Record, CoachmarkId.RecordControls),
			surface.eligibleCoachmarkIds
		)
	}

	@Test
	fun toCoachmarkSurface_ignoresRecordRouteStateWithoutContentControls() {
		val surface = RecordRouteViewState().toCoachmarkSurface(visitKey = "record:1")

		assertTrue(surface.eligibleCoachmarkIds.isEmpty())
	}

	@Test
	fun toCoachmarkSurface_mapsContentStatesToContextualCoachmarks() {
		val cases: List<Pair<ViewState, List<CoachmarkId>>> = listOf(
			Pensum.State.Content(model = pensumScreenModel()) to
				listOf(CoachmarkId.Pensum, CoachmarkId.PensumTools),
			Evaluations.State.Content(
				weekItem = EvaluationsWeekItem(
					labelText = "Semana 1",
					days = emptyList()
				),
				evaluationGroups = emptyList()
			) to listOf(CoachmarkId.Evaluations, CoachmarkId.EvaluationsTools),
			Evaluation.State.Content() to listOf(CoachmarkId.EvaluationEditor),
			SubjectSearch.State() to listOf(CoachmarkId.SubjectSearch),
			SubjectDetail.State.Content(detail = subjectDetailItem()) to listOf(CoachmarkId.SubjectDetail),
			CreateSyntheticTerm.State() to listOf(CoachmarkId.SyntheticTerm),
			About.State.Content(versionText = "1.0.0") to listOf(CoachmarkId.About, CoachmarkId.AboutActions)
		)

		cases.forEachIndexed { index, (state, expectedCoachmarkIds) ->
			val surface = state.toCoachmarkSurface(visitKey = "case:$index")

			assertEquals("case:$index", surface.visitKey)
			assertEquals(expectedCoachmarkIds, surface.eligibleCoachmarkIds)
		}
	}

	@Test
	fun contextualCoachmarks_definesEveryCoachmarkId() {
		val ids = contextualCoachmarks().map { coachmark -> coachmark.id }

		assertEquals(enumValues<CoachmarkId>().toSet(), ids.toSet())
		assertEquals(ids.size, ids.toSet().size)
	}

	private fun pensumScreenModel(): PensumScreenModel {
		return PensumScreenModel(
			careerName = "Ingenieria",
			selection = PensumScreenSelection(
				year = 2020,
				modalityId = "default"
			),
			pensumOptions = emptyList(),
			modalityOptions = emptyList(),
			progressPercent = 0,
			approvedCredits = 0,
			totalCredits = 0,
			isCurrentFocusVisible = false,
			canvas = PensumCanvasItem(
				width = 0.0,
				height = 0.0
			),
			terms = emptyList(),
			nodes = emptyList(),
			edges = emptyList()
		)
	}

	private fun subjectDetailItem(): SubjectDetailItem {
		return SubjectDetailItem(
			id = "CI1010",
			name = "Algoritmos",
			creditsText = "4 creditos",
			gradingModeText = "Numerica",
			generatedAtText = "Actualizado",
			selectedTab = SubjectSegmentTab.CAREER,
			hasSegmentTabs = false,
			chartMode = SubjectDetailItem.ChartMode.NUMERIC_GRADES,
			careerSegment = null,
			globalSegment = null
		)
	}
}
