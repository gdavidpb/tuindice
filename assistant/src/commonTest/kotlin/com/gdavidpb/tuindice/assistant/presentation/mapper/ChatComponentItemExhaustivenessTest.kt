package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.ChatFallbackItem
import com.gdavidpb.tuindice.chatcontract.ChatContract
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.AttentionItem
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.Citation
import com.gdavidpb.tuindice.chatcontract.component.CitationComponent
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay
import com.gdavidpb.tuindice.chatcontract.component.CooldownComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationRow
import com.gdavidpb.tuindice.chatcontract.component.HelpComponent
import com.gdavidpb.tuindice.chatcontract.component.IndexGoalComponent
import com.gdavidpb.tuindice.chatcontract.component.IndexPoint
import com.gdavidpb.tuindice.chatcontract.component.IndexSeriesComponent
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import com.gdavidpb.tuindice.chatcontract.component.OpenScreenComponent
import com.gdavidpb.tuindice.chatcontract.component.PaceComponent
import com.gdavidpb.tuindice.chatcontract.component.PensumComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PensumProgressComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanEvaluationComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanListComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanOption
import com.gdavidpb.tuindice.chatcontract.component.PlanRow
import com.gdavidpb.tuindice.chatcontract.component.ProfileSummaryComponent
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElective
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElectivesComponent
import com.gdavidpb.tuindice.chatcontract.component.RecordStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.RequiredGradeComponent
import com.gdavidpb.tuindice.chatcontract.component.RequirementsComponent
import com.gdavidpb.tuindice.chatcontract.component.ScheduleBlock
import com.gdavidpb.tuindice.chatcontract.component.ScheduleClash
import com.gdavidpb.tuindice.chatcontract.component.ScheduleDay
import com.gdavidpb.tuindice.chatcontract.component.SimulatedGrade
import com.gdavidpb.tuindice.chatcontract.component.SimulationComponent
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectImpactComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectListComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsRow
import com.gdavidpb.tuindice.chatcontract.component.SubjectRef
import com.gdavidpb.tuindice.chatcontract.component.SubjectRow
import com.gdavidpb.tuindice.chatcontract.component.SubjectStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportLine
import com.gdavidpb.tuindice.chatcontract.component.SuspensionComponent
import com.gdavidpb.tuindice.chatcontract.component.SyncSourceRow
import com.gdavidpb.tuindice.chatcontract.component.SyncStatusComponent
import com.gdavidpb.tuindice.chatcontract.component.TermComponent
import com.gdavidpb.tuindice.chatcontract.component.TermCredits
import com.gdavidpb.tuindice.chatcontract.component.TermSubjectRow
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.component.UsageNoticeComponent
import com.gdavidpb.tuindice.chatcontract.component.WarningComponent
import com.gdavidpb.tuindice.chatcontract.component.WeekLoad
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.event.UsageSummary
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

// Every subtype of ChatComponent maps to an item of its own, and only the unknown component falls back to its text.
// The subtypes are read from the sealed descriptor, which lists the whole hierarchy: a subtype added there without a
// sample below, or a sample that falls back, fails here; the mapping chain of ComponentFamilyMapping.kt is the one to
// extend.
class ChatComponentItemExhaustivenessTest {
	@Test
	fun everySubtypeHasASampleAndMapsToItsOwnItem() {
		val samples = knownComponentSamples()

		assertEquals(
			knownWireTypes(),
			samples.map { sample -> wireTypeOf(sample) }.toSet(),
			"a ChatComponent subtype has no sample here, or a sample is of a subtype that is gone"
		)

		samples.forEach { sample ->
			val item = sample.toChatComponentItem(nowMillis = NOW_MILLIS)

			assertFalse(item is ChatFallbackItem, "${sample::class.simpleName} falls back to its text: $item")
		}
	}

	@Test
	fun onlyTheUnknownComponentFallsBackToItsText() {
		val unknown = UnknownChatComponent(
			id = "unknown-1",
			fallbackText = "Texto de respaldo",
			display = ComponentDisplay.INLINE
		)

		assertIs<ChatFallbackItem>(unknown.toChatComponentItem(nowMillis = NOW_MILLIS))
	}

	// The wire types of the sealed hierarchy, as the serializer's descriptor lists them, Unknown aside.
	private fun knownWireTypes(): Set<String> {
		val subclasses = ChatComponent.serializer().descriptor.getElementDescriptor(1).let { hierarchy ->
			(0 until hierarchy.elementsCount).map { index -> hierarchy.getElementName(index) }.toSet()
		}

		return subclasses - UnknownChatComponent.serializer().descriptor.serialName
	}

	private fun wireTypeOf(component: ChatComponent): String {
		return ChatContract.json
			.encodeToJsonElement(ChatComponent.serializer(), component)
			.jsonObject
			.getValue("type")
			.jsonPrimitive
			.content
	}

	private fun knownComponentSamples(): List<ChatComponent> =
		statusComponents() +
			domainComponentsPart1() +
			domainComponentsPart2() +
			domainComponentsPart3() +
			domainComponentsPart4() +
			domainComponentsPart5() +
			domainComponentsPart6() +
			domainComponentsPart7()

	private fun statusComponents(): List<ChatComponent> = listOf(
		StillAppliesComponent("c2", "¿Sigue aplicando?", memoryId = "m1", statement = "Prefiere las notas por semana"),
		UsageNoticeComponent("c3", "Te quedan 9", usage = UsageSummary(9, 39, 399), resetsAtMillis = 1760003600000),
		WarningComponent("c4", "Advertencia", reason = AbuseReasonCode.SPAM),
		CooldownComponent("c5", "Pausa", reason = AbuseReasonCode.HARASSMENT, untilMillis = 1760003600000, appealable = true),
		SuspensionComponent("c6", "Suspendido", reason = AbuseReasonCode.AUTOMATION, appealable = false),
		SupportComponent("c7", "Ayuda", lines = listOf(SupportLine("Línea de apoyo", phone = "0800-0000"))),
		OpenScreenComponent("c8", "Ver evaluaciones", target = NavTarget.EVALUATIONS, label = "Ver evaluaciones")
	)

	private fun domainComponentsPart1(): List<ChatComponent> = listOf(
		ProfileSummaryComponent(
			"d1",
			"Perfil",
			careerCode = "IS",
			careerName = "Ingeniería de Sistemas",
			pensumLabel = "2016",
			cumulativeIndex = 16.4,
			approvedCredits = 80,
			totalCredits = 180,
			currentTermLabel = "2026-JAN_MAR"
		),
		SyncStatusComponent(
			"d2",
			"Sincronización",
			lastSyncAtMillis = 1760000000000,
			sources = listOf(
				SyncSourceRow("Notas", "ok"),
				SyncSourceRow("Pensum", "stale", detail = "Hace 3 días")
			),
			enrollmentStatus = "enrolled",
			annulmentCause = null
		),
		TermComponent(
			"d3",
			"Trimestre 2026-JAN_MAR",
			termId = "2026-JAN_MAR",
			termLabel = "2026 enero-marzo",
			termIndex = 16.2,
			cumulativeIndex = 15.8,
			credits = 14,
			rows = listOf(
				TermSubjectRow("attempt-1", "IS-101", "Programación I", 6, grade = 18, outcome = "approved", badge = "first_try"),
				TermSubjectRow("attempt-2", "IS-102", "Matemática I", 8)
			)
		),
	)

	private fun domainComponentsPart2(): List<ChatComponent> = listOf(
		IndexSeriesComponent(
			"d4",
			"Índice por trimestre",
			points = listOf(
				IndexPoint("2025-SEP_DEC", "2025 septiembre-diciembre", null, 15.1),
				IndexPoint("2026-JAN_MAR", "2026 enero-marzo", 16.2, 15.8)
			),
			bestTermId = "2026-JAN_MAR"
		),
		RecordStatsComponent(
			"d5",
			"Récord académico",
			approved = 30,
			failed = 2,
			retired = 1,
			approvedFirstTry = 25,
			repeated = 3,
			histogram = listOf(0, 1, 2, 10, 15)
		),
		PaceComponent(
			"d6",
			"Ritmo de créditos",
			creditsPerTerm = listOf(TermCredits("2026 enero-marzo", 16)),
			averageCredits = 15.0,
			remainingTermsEstimate = 6
		),
		SimulationComponent(
			"d7",
			"Simulación del trimestre",
			termId = "2026-JAN_MAR",
			termIndex = 17.0,
			cumulativeIndex = 16.0,
			changes = listOf(SimulatedGrade("attempt-1", "IS-101", grade = 20, outcome = "approved"))
		),
	)

	private fun domainComponentsPart3(): List<ChatComponent> = listOf(
		IndexGoalComponent(
			"d8",
			"Meta de índice",
			targetCumulative = 17.0,
			credits = 60,
			requiredTermIndex = 18.2,
			feasible = true
		),
		WeekScheduleComponent(
			"d9",
			"Horario de la semana",
			days = listOf(
				ScheduleDay(
					1,
					listOf(
						ScheduleBlock(
							"IS-101",
							"Programación I",
							480,
							600,
							room = "A-1",
							evaluationTitles = listOf("Parcial 1")
						)
					)
				),
				ScheduleDay(3, listOf(ScheduleBlock("IS-102", "Matemática I", 600, 720)))
			)
		),
		NextClassComponent(
			"d10",
			"Próxima clase",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			dayOfWeek = 2,
			startMinute = 480,
			endMinute = 600,
			room = null,
			inProgress = false,
			dateMillis = 1760100000000
		),
	)

	private fun domainComponentsPart4(): List<ChatComponent> = listOf(
		ClashesComponent(
			"d11",
			"Choques de horario",
			clashes = listOf(ScheduleClash(3, 540, 600, listOf("IS-101", "IS-102")))
		),
		EvaluationListComponent(
			"d12",
			"Evaluaciones",
			items = listOf(
				EvaluationRow(
					"eval-1",
					"IS-101",
					"Parcial 1",
					dateMillis = 1760100000000,
					scheduleMode = "dated",
					grade = 15.5,
					maxGrade = 20.0,
					state = "graded"
				),
				EvaluationRow("eval-2", "IS-102", "Quiz 2", scheduleMode = "undated", maxGrade = 10.0, state = "pending")
			)
		),
		SubjectPointsComponent(
			"d13",
			"Puntos por materia",
			rows = listOf(SubjectPointsRow("IS-101", "Programación I", earned = 12.5, evaluated = 60.0, total = 100.0))
		),
		WeeklyLoadComponent("d14", "Carga semanal", weeks = listOf(WeekLoad(42, 3, 60.0, heavy = true))),
		RequiredGradeComponent(
			"d15",
			"Nota requerida",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			targetGrade = 15,
			requiredPoints = 25.0,
			remainingPoints = 40.0,
			feasible = true
		),
	)

	private fun domainComponentsPart5(): List<ChatComponent> = listOf(
		AttentionComponent(
			"d16",
			"Atención",
			items = listOf(
				AttentionItem("evaluation", "Parcial 2 mañana", "IS-101", EntityRef(EntityKind.EVALUATION, "eval-2")),
				AttentionItem("sync", "Notas desactualizadas", "Sincroniza para verlas")
			)
		),
		PensumProgressComponent(
			"d17",
			"Avance del pensum",
			approvedCredits = 80,
			totalCredits = 180,
			approved = 20,
			inProgress = 4,
			available = 6,
			blocked = 10
		),
		RequirementsComponent(
			"d18",
			"Requisitos de IS-201",
			subjectCode = "IS-201",
			subjectName = "Estructuras de Datos",
			prerequisites = listOf(SubjectRef("IS-101", "Programación I", status = "approved")),
			corequisites = emptyList(),
			unlocks = listOf(SubjectRef("IS-301", "Bases de Datos"))
		),
		SubjectImpactComponent(
			"d19",
			"Impacto de IS-101",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			chain = listOf(SubjectRef("IS-201", "Estructuras de Datos"), SubjectRef("IS-301", "Bases de Datos")),
			lengthInTerms = 2
		),
	)

	private fun domainComponentsPart6(): List<ChatComponent> = listOf(
		PensumComparisonComponent(
			"d20",
			"Comparación de pensum",
			otherCareerName = "Ingeniería Civil",
			otherPensumLabel = "2019",
			sharedCredits = 90,
			missingSubjects = listOf(SubjectRef("IS-401", "Redes")),
			notes = listOf("Dos materias sin equivalente")
		),
		SubjectListComponent(
			"d21",
			"Materias",
			items = listOf(
				SubjectRow("IS-101", "Programación I", 6, status = "approved"),
				SubjectRow("IS-102", "Matemática I", 8)
			)
		),
		SubjectStatsComponent(
			"d22",
			"Estadísticas de IS-101",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			scope = "all",
			average = 14.2,
			median = 14.0,
			passRate = 0.72,
			difficulty = "medium",
			histogram = listOf(1, 2, 5, 9, 7)
		),
		RecommendedElectivesComponent(
			"d23",
			"Electivas recomendadas",
			items = listOf(RecommendedElective("IS-550", "Inteligencia Artificial", 4, "Encaja con tu avance"))
		),
		PlanListComponent(
			"d24",
			"Planes",
			plans = listOf(PlanRow("2026-JAN_MAR", "2026 enero-marzo", listOf("IS-101", "IS-102"), 14))
		),
	)

	private fun domainComponentsPart7(): List<ChatComponent> = listOf(
		PlanEvaluationComponent(
			"d25",
			"Evaluación del plan",
			termLabel = "2026 enero-marzo",
			credits = 14,
			loadBand = "medium",
			loadIndex = 0.6,
			clashes = 0,
			pendingRequirements = listOf("IS-201 requiere IS-101")
		),
		PlanComparisonComponent(
			"d26",
			"Comparación de planes",
			options = listOf(
				PlanOption("Plan A", 14, "medium", unlocks = listOf("IS-201"), clashes = 0),
				PlanOption("Plan B", 18, "high", clashes = 1)
			)
		),
		CitationComponent(
			"d27",
			"Fuentes",
			sources = listOf(
				Citation(
					"Reglamento",
					"Art. 12",
					"Los cupos se asignan por orden de inscripción.",
					url = "https://example.com/r"
				)
			)
		),
		HelpComponent(
			"d28",
			"Ayuda para agregar una evaluación",
			title = "Cómo agregar una evaluación",
			steps = listOf("Abre Evaluaciones", "Toca Agregar"),
			target = NavTarget.EVALUATIONS_ADD
		)
	)

	private companion object {
		const val NOW_MILLIS = 1760000000000L
	}
}
