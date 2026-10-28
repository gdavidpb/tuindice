package com.gdavidpb.tuindice.chatcontract

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.OpenScreenComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportComponent
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.ComponentEmitted
import com.gdavidpb.tuindice.chatcontract.event.IntentResolved
import com.gdavidpb.tuindice.chatcontract.event.Progress
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.UnknownAssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.UnknownIntentArgs
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.Text
import com.gdavidpb.tuindice.chatcontract.message.UnknownMessagePart
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ChatContractUnknownTypesTest {

	private fun <T> decode(serializer: KSerializer<T>, text: String): T =
		ChatContract.json.decodeFromString(serializer, text)

	@Test
	fun unknownComponentKeepsItsFallbackText() {
		val text = """{"type":"x.y/9","id":"c9","fallbackText":"Texto de respaldo"}"""

		assertEquals(UnknownChatComponent("c9", "Texto de respaldo"), decode(ChatComponent.serializer(), text))
	}

	@Test
	fun unknownIntentArgsEventAndMessagePartDecodeWithoutFailing() {
		assertIs<UnknownIntentArgs>(decode(IntentArgs.serializer(), """{"type":"x.y/9","foo":1}"""))
		assertEquals(
			UnknownAssistantEvent(5, "t1"),
			decode(AssistantEvent.serializer(), """{"type":"x.y/9","seq":5,"turnId":"t1"}""")
		)
		assertIs<UnknownMessagePart>(decode(MessagePart.serializer(), """{"type":"x.y/9"}"""))
	}

	@Test
	fun extraFieldsOfAKnownTypeAreIgnored() {
		val support = """{"type":"sistema.apoyo/1","id":"c1","fallbackText":"Ayuda","lines":[],"extra":{"a":1}}"""
		val progress = """{"type":"progress","seq":1,"turnId":"t1","tool":"a","label":"b","extra":true}"""

		assertEquals(SupportComponent("c1", "Ayuda", lines = emptyList()), decode(ChatComponent.serializer(), support))
		assertEquals(Progress(1, "t1", "a", "b"), decode(AssistantEvent.serializer(), progress))
	}

	@Test
	fun unknownEnumCodeOfAKnownEventFallsBackToTheDefault() {
		val text = """{"type":"run_error","seq":1,"turnId":"t1","code":"nuevo_codigo","retryable":true}"""

		assertEquals(RunError(1, "t1", RunErrorCode.OTHER, retryable = true), decode(AssistantEvent.serializer(), text))
	}

	// A value this build does not know inside a component: the component shows the fallback text it carries, and the
	// rest of the message keeps its parts. The same message with a target this build knows keeps the component itself.
	@Test
	fun unknownEnumValueInsideAComponentKeepsItsFallbackTextAndTheRestOfTheMessage() {
		val text = """
			{"id":"m1","createdAtMillis":1,"parts":[
				{"type":"text","text":"Hola"},
				{"type":"component","component":{"type":"nav.abrir/1","id":"c1","fallbackText":"Abre el resumen","target":"destino_nuevo","label":"Abrir"}},
				{"type":"text","text":"Fin"}
			]}
		"""
		val known = decode(AssistantMessage.serializer(), text.replace("destino_nuevo", "summary")).parts

		assertIs<OpenScreenComponent>(assertIs<Component>(known[1]).component)
		assertEquals(
			listOf(Text("Hola"), Component(UnknownChatComponent("c1", "Abre el resumen")), Text("Fin")),
			decode(AssistantMessage.serializer(), text).parts
		)
	}

	// A value this build does not know inside an intent: the intent is left out as an unknown part, and the rest of the
	// message stays. The same message with a risk this build knows keeps the intent itself.
	@Test
	fun unknownRiskOfAnIntentIsAnUnknownPartAndTheRestOfTheMessageStays() {
		val text = """
			{"id":"m1","createdAtMillis":1,"parts":[
				{"type":"text","text":"Hola"},
				{"type":"intent","intent":{"id":"intent-1","args":{"type":"evaluaciones.agregar/1","items":[],"source":"manual"},"title":"Agregar","effect":[],"risk":"riesgo_nuevo","editable":[],"binding":{"mutationIds":[],"expectedRevision":12,"argsHash":"hash-1"},"expiresAtMillis":1760000600000,"status":"proposed","confirmLabel":"Agregar","cancelLabel":"Cancelar"}},
				{"type":"text","text":"Fin"}
			]}
		"""

		assertIs<IntentPart>(decode(AssistantMessage.serializer(), text.replace("riesgo_nuevo", "normal")).parts[1])
		assertEquals(listOf(Text("Hola"), UnknownMessagePart, Text("Fin")), decode(AssistantMessage.serializer(), text).parts)
	}

	// An outcome this build does not know, or none at all, is not an intent resolution: the app reads the intent from the
	// envelope of the event instead, and the card closes without an outcome label.
	@Test
	fun unknownOrMissingOutcomeOfAnIntentResolvedDoesNotDecode() {
		val unknown = """{"type":"intent_resolved","seq":4,"turnId":"t1","intentId":"i1","outcome":"resultado_nuevo"}"""
		val missing = """{"type":"intent_resolved","seq":4,"turnId":"t1","intentId":"i1"}"""

		assertFailsWith<SerializationException> { decode(AssistantEvent.serializer(), unknown) }
		assertFailsWith<SerializationException> { decode(AssistantEvent.serializer(), missing) }
	}

	// A status this build does not know is no status at all, and the outcome it carries stays.
	@Test
	fun unknownStatusOfAnIntentResolvedIsNoneAndTheOutcomeStays() {
		val text = """
			{"type":"intent_resolved","seq":4,"turnId":"t1","intentId":"i1",
			"outcome":"confirmed","status":"nuevo"}
		""".trimIndent()

		assertEquals(IntentResolved(4, "t1", "i1", OutcomeKind.CONFIRMED), decode(AssistantEvent.serializer(), text))
	}

	// A component inside an event follows the rule of a component inside a message: a value this build does not know
	// leaves it with its fallback text, and a value it knows keeps the component itself.
	@Test
	fun unknownValueInsideAComponentEmittedKeepsItsFallbackText() {
		val text = """
			{"type":"component_emitted","seq":2,"turnId":"t1","messageId":"m1","component":
			{"type":"nav.abrir/1","id":"c1","fallbackText":"Abre el resumen","target":"destino_nuevo","label":"Abrir"}}
		""".trimIndent()
		val known = text.replace("destino_nuevo", "summary")

		assertIs<OpenScreenComponent>(assertIs<ComponentEmitted>(decode(AssistantEvent.serializer(), known)).component)
		assertEquals(
			ComponentEmitted(2, "t1", "m1", UnknownChatComponent("c1", "Abre el resumen")),
			decode(AssistantEvent.serializer(), text)
		)
	}

	// A standing level this build does not know restricts nothing on this side: it decodes as NONE, and the server still
	// refuses what it restricts.
	@Test
	fun unknownStandingLevelDecodesAsNoRestriction() {
		assertEquals(AbuseStanding(StandingLevel.NONE), decode(AbuseStanding.serializer(), """{"level":"nivel_nuevo"}"""))
	}
}
