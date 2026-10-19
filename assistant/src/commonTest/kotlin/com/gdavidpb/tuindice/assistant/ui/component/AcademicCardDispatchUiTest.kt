package com.gdavidpb.tuindice.assistant.ui.component

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.presentation.mapper.toChatComponentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.view.ChatComponentView
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.Citation
import com.gdavidpb.tuindice.chatcontract.component.CitationComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationRow
import com.gdavidpb.tuindice.chatcontract.component.HelpComponent
import com.gdavidpb.tuindice.chatcontract.component.PensumComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PensumProgressComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanEvaluationComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanListComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanOption
import com.gdavidpb.tuindice.chatcontract.component.PlanRow
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElective
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElectivesComponent
import com.gdavidpb.tuindice.chatcontract.component.RequiredGradeComponent
import com.gdavidpb.tuindice.chatcontract.component.RequirementsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectImpactComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectListComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsRow
import com.gdavidpb.tuindice.chatcontract.component.SubjectRef
import com.gdavidpb.tuindice.chatcontract.component.SubjectRow
import com.gdavidpb.tuindice.chatcontract.component.SubjectStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.WeekLoad
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

// Each card of the academic catalogs reaches its own renderer, and no longer the text it falls back to.
@OptIn(ExperimentalTestApi::class)
class AcademicCardDispatchUiTest {
	@Test
	fun when_evaluationListIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			EvaluationListComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				items = listOf(EvaluationRow("eval-1", "IS-101", "Parcial 1", null, "dated", 14.0, 20.0, "graded"))
			),
			AssistantUiTags.Evaluations.list(COMPONENT_ID)
		)
	}

	@Test
	fun when_subjectPointsIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			SubjectPointsComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				rows = listOf(SubjectPointsRow("IS-101", "Programación I", 12.5, 60.0, 100.0))
			),
			AssistantUiTags.Evaluations.points(COMPONENT_ID)
		)
	}

	@Test
	fun when_weeklyLoadIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			WeeklyLoadComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				weeks = listOf(WeekLoad(week = 42, count = 3, points = 60.0, heavy = true))
			),
			AssistantUiTags.Evaluations.load(COMPONENT_ID)
		)
	}

	@Test
	fun when_requiredGradeIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			RequiredGradeComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				subjectCode = "IS-101",
				subjectName = "Programación I",
				targetGrade = 15,
				requiredPoints = 25.0,
				remainingPoints = 40.0,
				feasible = true
			),
			AssistantUiTags.Evaluations.requiredGrade(COMPONENT_ID)
		)
	}

	@Test
	fun when_pensumProgressIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			PensumProgressComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				approvedCredits = 70,
				totalCredits = 150,
				approved = 12,
				inProgress = 3,
				available = 5,
				blocked = 8
			),
			AssistantUiTags.Pensum.progress(COMPONENT_ID)
		)
	}

	@Test
	fun when_requirementsIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			RequirementsComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				subjectCode = "IS-201",
				subjectName = "Estructuras de Datos",
				prerequisites = listOf(SubjectRef("IS-101", "Programación I", "approved")),
				corequisites = emptyList(),
				unlocks = emptyList()
			),
			AssistantUiTags.Pensum.requirements(COMPONENT_ID)
		)
	}

	@Test
	fun when_subjectImpactIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			SubjectImpactComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				subjectCode = "IS-101",
				subjectName = "Programación I",
				chain = listOf(SubjectRef("IS-201", "Estructuras de Datos")),
				lengthInTerms = 1
			),
			AssistantUiTags.Pensum.impact(COMPONENT_ID)
		)
	}

	@Test
	fun when_pensumComparisonIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			PensumComparisonComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				otherCareerName = "Ingeniería Industrial",
				otherPensumLabel = "2016",
				sharedCredits = 96,
				missingSubjects = emptyList()
			),
			AssistantUiTags.Pensum.comparison(COMPONENT_ID)
		)
	}

	@Test
	fun when_subjectListIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			SubjectListComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				items = listOf(SubjectRow("IS-101", "Programación I", credits = 6))
			),
			AssistantUiTags.Subjects.list(COMPONENT_ID)
		)
	}

	@Test
	fun when_subjectStatsIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			SubjectStatsComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				subjectCode = "IS-101",
				subjectName = "Programación I",
				scope = "all",
				average = null,
				median = null,
				passRate = null,
				difficulty = null,
				histogram = emptyList()
			),
			AssistantUiTags.Subjects.stats(COMPONENT_ID)
		)
	}

	@Test
	fun when_recommendedElectivesIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			RecommendedElectivesComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				items = listOf(RecommendedElective("IS-550", "Inteligencia Artificial", 4, "Encaja con tu avance"))
			),
			AssistantUiTags.Subjects.electives(COMPONENT_ID)
		)
	}

	@Test
	fun when_planListIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			PlanListComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				plans = listOf(PlanRow("2026-JAN_MAR", "2026 enero-marzo", listOf("IS-101"), credits = 6))
			),
			AssistantUiTags.Plan.list(COMPONENT_ID)
		)
	}

	@Test
	fun when_planEvaluationIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			PlanEvaluationComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				termLabel = "2026 enero-marzo",
				credits = 6,
				loadBand = "unknown",
				loadIndex = null,
				clashes = 0
			),
			AssistantUiTags.Plan.evaluation(COMPONENT_ID)
		)
	}

	@Test
	fun when_planComparisonIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			PlanComparisonComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				options = listOf(PlanOption("Plan A", 6, "Ligera", clashes = 0))
			),
			AssistantUiTags.Plan.comparison(COMPONENT_ID)
		)
	}

	@Test
	fun when_citationIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			CitationComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				sources = listOf(Citation("Reglamento de evaluación", "Artículo 12", "Las evaluaciones se califican."))
			),
			AssistantUiTags.Knowledge.citation(COMPONENT_ID)
		)
	}

	@Test
	fun when_helpIsShown_then_itsCardRendersNotTheFallback() = runTuIndiceUiTest {
		assertDispatchedTo(
			HelpComponent(
				id = COMPONENT_ID,
				fallbackText = FALLBACK_TEXT,
				title = "Cómo ver tu índice",
				steps = listOf("Abre el informe."),
				target = null
			),
			AssistantUiTags.Knowledge.help(COMPONENT_ID)
		)
	}

	private fun ComposeUiTest.assertDispatchedTo(component: ChatComponent, tag: String) {
		setTuIndiceTestContent {
			ChatComponentView(messageId = MESSAGE_ID, item = component.toChatComponentItem(nowMillis = 0L), onAction = {})
		}

		assertNodeVisible(tag)
		onNodeWithText(FALLBACK_TEXT).assertDoesNotExist()
	}

	private companion object {
		const val COMPONENT_ID = "card-b-1"
		const val MESSAGE_ID = "message-1"
		const val FALLBACK_TEXT = "Respaldo de prueba"
	}
}
