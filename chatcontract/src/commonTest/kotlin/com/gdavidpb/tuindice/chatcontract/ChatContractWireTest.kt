package com.gdavidpb.tuindice.chatcontract

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.ConsentComponent
import com.gdavidpb.tuindice.chatcontract.component.CooldownComponent
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.OpenScreenComponent
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportLine
import com.gdavidpb.tuindice.chatcontract.component.SuspensionComponent
import com.gdavidpb.tuindice.chatcontract.component.UsageNoticeComponent
import com.gdavidpb.tuindice.chatcontract.component.WarningComponent
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
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.CreatePlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditPlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.EvaluationDraft
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentBinding
import com.gdavidpb.tuindice.chatcontract.intent.IntentEffectLine
import com.gdavidpb.tuindice.chatcontract.intent.IntentRisk
import com.gdavidpb.tuindice.chatcontract.intent.MutatingIntentArgs
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
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

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
		ConsentComponent(
			"c1",
			"Términos",
			version = 2,
			termsUrl = "https://example.com/t",
			privacyUrl = "https://example.com/p"
		),
		StillAppliesComponent("c2", "¿Sigue aplicando?", memoryId = "m1", statement = "Prefiere las notas por semana"),
		UsageNoticeComponent("c3", "Te quedan 9", usage = UsageSummary(9, 39, 399), resetsAtMillis = 1760003600000),
		WarningComponent("c4", "Advertencia", reason = AbuseReasonCode.SPAM),
		CooldownComponent("c5", "Pausa", reason = AbuseReasonCode.HARASSMENT, untilMillis = 1760003600000, appealable = true),
		SuspensionComponent("c6", "Suspendido", reason = AbuseReasonCode.AUTOMATION, appealable = false),
		SupportComponent("c7", "Ayuda", lines = listOf(SupportLine("Línea de apoyo", phone = "0800-0000"))),
		OpenScreenComponent("c8", "Ver evaluaciones", target = NavTarget.EVALUATIONS, label = "Ver evaluaciones")
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
	fun everyComponentRoundTripsUnderItsCatalogName() = assertWire(
		ChatComponent.serializer(),
		components,
		listOf(
			"sistema.consentimiento/1",
			"sistema.sigue_aplicando/1",
			"sistema.aviso_uso/1",
			"sistema.advertencia/1",
			"sistema.enfriamiento/1",
			"sistema.suspension/1",
			"sistema.apoyo/1",
			"nav.abrir/1"
		)
	)

	@Test
	fun everyIntentArgsRoundTripsAndEverySubclassIsSampled() {
		val names = listOf(
			"evaluaciones.agregar/1", "evaluaciones.editar/1", "evaluaciones.borrar/1", "plan.crear/1",
			"plan.editar/1", "plan.borrar/1", "informe.proyeccion/1", "comprobante.pedir/1", "memoria.recordar/1"
		)
		assertWire(IntentArgs.serializer(), intentArgs, names)

		// MutatingIntentArgs is a non-sealed interface: the sealed descriptor lists it as a polymorphic entry,
		// which is not a wire subclass, so only the concrete entries are compared.
		val subclasses = IntentArgs.serializer().descriptor.getElementDescriptor(1)
		val wireSubclasses = subclasses.elementNames.filter { name ->
			subclasses.getElementDescriptor(subclasses.getElementIndex(name)).kind !is PolymorphicKind
		}
		assertEquals(names.toSet(), wireSubclasses.toSet() - UnknownIntentArgs.serializer().descriptor.serialName)
	}

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
