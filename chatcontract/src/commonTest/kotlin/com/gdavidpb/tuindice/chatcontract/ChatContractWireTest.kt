package com.gdavidpb.tuindice.chatcontract

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.AttentionItem
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.Citation
import com.gdavidpb.tuindice.chatcontract.component.CitationComponent
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
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
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.ComponentEmitted
import com.gdavidpb.tuindice.chatcontract.event.IntentProposed
import com.gdavidpb.tuindice.chatcontract.event.IntentResolved
import com.gdavidpb.tuindice.chatcontract.event.MessageCompleted
import com.gdavidpb.tuindice.chatcontract.event.Progress
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.event.RunStarted
import com.gdavidpb.tuindice.chatcontract.event.UsageSummary
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionLimits
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionRequest
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.CreatePlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditPlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.EvaluationDraft
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentBinding
import com.gdavidpb.tuindice.chatcontract.intent.IntentEffectLine
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.IntentRisk
import com.gdavidpb.tuindice.chatcontract.intent.IntentStatus
import com.gdavidpb.tuindice.chatcontract.intent.MutatingIntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.ProjectionArgs
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemoveEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemovePlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.RequestEnrollmentProofArgs
import com.gdavidpb.tuindice.chatcontract.intent.UnknownIntentArgs
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.SuggestedReply
import com.gdavidpb.tuindice.chatcontract.message.Suggestions
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChatContractWireTest {

	private val draft = EvaluationDraft(
		"mut-1", "Parcial 1", "attempt-1", "IS-101", "2026-JAN_MAR", "QUIZ", "dated", 1761000000000, 15.5, 20.0
	)

	private val intentArgs: List<IntentArgs> = listOf(
		AddEvaluationsArgs(listOf(draft)),
		EditEvaluationArgs("mut-2", "eval-1", grade = 18.0, evaluationType = "QUIZ"),
		RemoveEvaluationArgs("mut-3", "eval-1"),
		CreatePlanArgs("mut-4", 2026, "JAN_MAR", listOf("IS-101")),
		EditPlanArgs("mut-5", "2026-JAN_MAR", listOf("IS-101", "IS-102")),
		RemovePlanArgs("mut-6", "2026-JAN_MAR"),
		ProjectionArgs("mut-7", "attempt-1", grade = 14, outcome = "approved"),
		RequestEnrollmentProofArgs,
		RememberArgs("Prefiere las notas por semana", "preference", "por semana")
	)

	private val intent = Intent(
		id = "intent-1",
		args = intentArgs.first(),
		title = "Agregar 1 evaluación",
		effect = listOf(IntentEffectLine("Nueva evaluación", "Parcial 1 de IS-101")),
		risk = IntentRisk.NORMAL,
		editable = listOf("grade"),
		binding = IntentBinding(listOf("mut-1"), 12, "hash-1"),
		expiresAtMillis = 1760000600000,
		confirmLabel = "Agregar",
		cancelLabel = "Cancelar"
	)

	private val components: List<ChatComponent> = listOf(
		StillAppliesComponent("c2", "¿Sigue aplicando?", memoryId = "m1", statement = "Prefiere las notas por semana"),
		UsageNoticeComponent("c3", "Te quedan 9", usage = UsageSummary(9, 39, 399), resetsAtMillis = 1760003600000),
		WarningComponent("c4", "Advertencia", reason = AbuseReasonCode.SPAM),
		CooldownComponent("c5", "Pausa", reason = AbuseReasonCode.HARASSMENT, untilMillis = 1760003600000, appealable = true),
		SuspensionComponent("c6", "Suspendido", reason = AbuseReasonCode.AUTOMATION, appealable = false),
		SupportComponent("c7", "Ayuda", lines = listOf(SupportLine("Línea de apoyo", phone = "0800-0000"))),
		OpenScreenComponent("c8", "Ver evaluaciones", target = NavTarget.EVALUATIONS, label = "Ver evaluaciones")
	)

	private val domainComponents: List<ChatComponent> = listOf(
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
								startMinute = 480,
								endMinute = 600,
								startBlock = 1,
								endBlock = 2,
								room = "A-1",
							evaluationTitles = listOf("Parcial 1")
						)
					)
				),
				ScheduleDay(
					3,
					listOf(
						ScheduleBlock(
							"IS-102",
							"Matemática I",
							startMinute = 600,
							endMinute = 720,
							startBlock = 3,
							endBlock = 4
						)
					)
				)
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
			startBlock = 1,
			endBlock = 2,
			room = null,
			dateMillis = 1760100000000
		),
		ClashesComponent(
			"d11",
			"Choques de horario",
			clashes = listOf(
				ScheduleClash(
					dayOfWeek = 3,
					startMinute = 510,
					endMinute = 570,
					startBlock = 2,
					endBlock = 2,
					subjectCodes = listOf("IS-101", "IS-102")
				)
			)
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

	private val message = AssistantMessage(
		"msg-1",
		listOf(
			Text("Listo"),
			Component(components.last()),
			IntentPart(intent),
			Suggestions(listOf(SuggestedReply("s1", "Ver promedio", "¿Cuál es mi promedio?")))
		),
		1760000000000
	)

	@Test
	fun everyAssistantEventRoundTripsUnderItsWireType() = assertWire(
		AssistantEvent.serializer(),
		listOf(
			RunStarted(1, "turn-1"),
			Progress(2, "turn-1", "search", "Buscando tus notas"),
			MessageCompleted(3, "turn-1", message),
			ComponentEmitted(4, "turn-1", "msg-1", components.first()),
			IntentProposed(5, "turn-1", "msg-1", intent),
			IntentResolved(6, "turn-1", "intent-1", OutcomeKind.CONFIRMED),
			RunFinished(7, "turn-1", UsageSummary(9, 39, 399)),
			RunError(8, "turn-1", RunErrorCode.QUOTA_EXHAUSTED, retryable = false, retryAfterSeconds = 60)
		),
		listOf(
			"run_started",
			"progress",
			"message_completed",
			"component_emitted",
			"intent_proposed",
			"intent_resolved",
			"run_finished",
			"run_error"
		)
	)

	@Test
	fun everyMessagePartRoundTripsUnderItsWireType() = assertWire(
		MessagePart.serializer(),
		listOf(Text("Listo"), Component(components.last()), IntentPart(intent), Suggestions(emptyList())),
		listOf("text", "component", "intent", "suggestions")
	)

	@Test
	fun everyComponentRoundTripsUnderItsCatalogName() {
		val names = listOf(
			"sistema.sigue_aplicando/1",
			"sistema.aviso_uso/1",
			"sistema.advertencia/1",
			"sistema.enfriamiento/1",
			"sistema.suspension/1",
			"sistema.apoyo/1",
			"nav.abrir/1",
			"perfil.resumen/1",
			"perfil.estado_sync/1",
			"informe.trimestre/1",
			"informe.serie/1",
			"informe.estadisticas/1",
			"informe.ritmo/1",
			"informe.simulacion/1",
			"informe.meta/1",
			"horario.semana/1",
			"horario.proxima/1",
			"horario.choques/1",
			"evaluaciones.lista/1",
			"evaluaciones.puntos/1",
			"evaluaciones.carga/1",
			"evaluaciones.meta/1",
			"resumen.atencion/1",
			"pensum.avance/1",
			"pensum.requisitos/1",
			"pensum.impacto/1",
			"pensum.comparacion/1",
			"materias.lista/1",
			"materias.estadisticas/1",
			"materias.recomendadas/1",
			"plan.lista/1",
			"plan.evaluacion/1",
			"plan.comparacion/1",
			"conocimiento.cita/1",
			"conocimiento.ayuda/1"
		)
		assertWire(ChatComponent.serializer(), components + domainComponents, names)

		// Every concrete component is sampled above: the sealed descriptor lists them all, Unknown aside.
		val subclasses = ChatComponent.serializer().descriptor.getElementDescriptor(1).elementNames
		assertEquals(names.toSet(), subclasses.toSet() - UnknownChatComponent.serializer().descriptor.serialName)
	}

	@Test
	fun everyIntentArgsRoundTripsAndEverySubclassIsSampled() {
		val names = listOf(
			"evaluaciones.agregar/1", "evaluaciones.editar/1", "evaluaciones.borrar/1", "plan.crear/1",
			"plan.editar/1", "plan.borrar/1", "informe.proyeccion/1", "comprobante.pedir/1", "memoria.recordar/1"
		)
		assertWire(IntentArgs.serializer(), intentArgs, names)

		// MutatingIntentArgs does not extend IntentArgs, so the sealed descriptor lists only the wire subclasses.
		val subclasses = IntentArgs.serializer().descriptor.getElementDescriptor(1)
		assertEquals(names.toSet(), subclasses.elementNames.toSet() - UnknownIntentArgs.serializer().descriptor.serialName)
	}

	@Test
	fun aMemoryCarriesItsAnchorAndAnOlderOneWithoutItReadsAsUnanchored() {
		val anchored = RememberArgs(
			statement = "Quiere aprobar MA2115 este trimestre",
			category = "SUBJECT",
			quote = "aprobar Mate IV",
			anchorKind = "ATTEMPT",
			anchorRef = "attempt-1",
			anchorDate = "2026-12-11"
		)
		assertWire(IntentArgs.serializer(), listOf(anchored), listOf("memoria.recordar/1"))

		val older = """{"type":"memoria.recordar/1","statement":"Prefiere","category":"PREFERENCE","quote":"prefiero"}"""
		val read = assertIs<RememberArgs>(ChatContract.json.decodeFromString(IntentArgs.serializer(), older))
		assertEquals(listOf(null, null, null), listOf(read.anchorKind, read.anchorRef, read.anchorDate))
	}

	@Test
	fun aMemoryCarriesTheIdOfTheOneItReplacesAndAnOlderOneWithoutItReplacesNone() {
		val correction = RememberArgs(
			statement = "Prefiere las notas por trimestre",
			category = "PREFERENCE",
			quote = "mejor por trimestre",
			replacesMemoryId = "memory-1"
		)
		assertWire(IntentArgs.serializer(), listOf(correction), listOf("memoria.recordar/1"))

		val older = """{"type":"memoria.recordar/1","statement":"Prefiere","category":"PREFERENCE","quote":"prefiero"}"""
		val read = assertIs<RememberArgs>(ChatContract.json.decodeFromString(IntentArgs.serializer(), older))
		assertEquals(null, read.replacesMemoryId)
		val wire = ChatContract.json.encodeToJsonElement(IntentArgs.serializer(), read).jsonObject
		assertFalse("replacesMemoryId" in wire)
	}

	@Test
	fun aReportCarriesTheOutcomeOfEachMutationAndOneWithoutThemKeepsItsOlderShape() {
		val partial = IntentOutcomeReport(
			intentId = "intent-1",
			outcome = OutcomeKind.CONFIRMED,
			mutationIds = listOf("mut-1", "mut-2"),
			itemOutcomes = listOf(
				MutationOutcome("mut-1", OutcomeKind.CONFIRMED, newRevision = 3),
				MutationOutcome("mut-2", OutcomeKind.FAILED, detailCode = "unhandled")
			)
		)
		assertEquals(partial, roundTrip(IntentOutcomeReport.serializer(), partial))

		val whole = IntentOutcomeReport(intentId = "intent-1", outcome = OutcomeKind.CONFIRMED, mutationIds = listOf("mut-1"))
		val wire = ChatContract.json.encodeToJsonElement(IntentOutcomeReport.serializer(), whole).jsonObject
		assertFalse("itemOutcomes" in wire)

		val older = """{"intentId":"intent-1","outcome":"queued","mutationIds":["mut-1"]}"""
		assertEquals(emptyList(), ChatContract.json.decodeFromString(IntentOutcomeReport.serializer(), older).itemOutcomes)
	}

	@Test
	fun aResolvedIntentCarriesItsFinalStatusAndAnOlderEventWithoutItReadsAsNone() {
		val superseded = IntentResolved(6, "turn-1", "intent-1", OutcomeKind.CANCELLED, status = IntentStatus.SUPERSEDED)
		assertWire(AssistantEvent.serializer(), listOf(superseded), listOf("intent_resolved"))

		val older = """{"type":"intent_resolved","seq":6,"turnId":"turn-1","intentId":"intent-1","outcome":"confirmed"}"""
		val read = assertIs<IntentResolved>(ChatContract.json.decodeFromString(AssistantEvent.serializer(), older))
		assertEquals(null, read.status)
	}

	@Test
	fun anExtractionAndItsProposalRoundTrip() {
		val request = ExtractionRequest(clientRequestId = "req-1", mediaType = "image/jpeg", imageBase64 = "AAAA")
		val found = ExtractionResult(message = "Encontré 1 evaluación.", intent = intent)
		val none = ExtractionResult(message = "No encontré evaluaciones en la foto.")

		assertEquals(request, roundTrip(ExtractionRequest.serializer(), request))
		assertEquals(found, roundTrip(ExtractionResult.serializer(), found))
		assertEquals(none, roundTrip(ExtractionResult.serializer(), none))
		assertTrue("image/jpeg" in ExtractionLimits.MEDIA_TYPES)
	}

	private fun <T> roundTrip(serializer: KSerializer<T>, value: T): T =
		ChatContract.json.decodeFromString(serializer, ChatContract.json.encodeToString(serializer, value))

	@Test
	fun mutatingArgsImplementTheMarkerAndSendNoMutationIds() {
		intentArgs.filter { it !is RequestEnrollmentProofArgs && it !is RememberArgs }.forEach { args ->
			assertIs<MutatingIntentArgs>(args)
			val wire = ChatContract.json.encodeToJsonElement(IntentArgs.serializer(), args).jsonObject
			assertFalse("mutationIds" in wire, args.toString())
		}
	}

	private fun <T> assertWire(serializer: KSerializer<T>, values: List<T>, wireTypes: List<String>) {
		val json = ChatContract.json
		val types = values.map { json.encodeToJsonElement(serializer, it).jsonObject.getValue("type").jsonPrimitive }
		assertEquals(wireTypes, types.map { it.content })
		values.forEach { assertEquals(it, json.decodeFromString(serializer, json.encodeToString(serializer, it))) }
	}
}
