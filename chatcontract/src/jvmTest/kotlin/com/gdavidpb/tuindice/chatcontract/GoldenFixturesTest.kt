package com.gdavidpb.tuindice.chatcontract

import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult
import com.gdavidpb.tuindice.chatcontract.insights.TermInsights
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Golden files are written by hand from the contract, never with its encoder. Decoding them and encoding them again
 * must give the same JSON tree, so a renamed field, a changed default or a changed discriminator fails here.
 * They cover the types the backend stores as JSON and reads back: intent args, events, extraction results and the
 * components inside them. The backend does not read these files; it shares the contract, not the goldens.
 */
class GoldenFixturesTest {

	@Test
	fun turnRequestIsStableOnTheWire() = assertGolden("turn_request.json", TurnRequest.serializer())

	@Test
	fun messageCompletedEventIsStableOnTheWire() =
		assertGolden("event_message_completed.json", AssistantEvent.serializer())

	@Test
	fun intentProposedEventIsStableOnTheWire() =
		assertGolden("event_intent_proposed_add_evaluations.json", AssistantEvent.serializer())

	@Test
	fun termComponentIsStableOnTheWire() = assertGolden("component_term.json", ChatComponent.serializer())

	@Test
	fun intentOutcomeReportIsStableOnTheWire() =
		assertGolden("intent_outcome_report.json", IntentOutcomeReport.serializer())

	@Test
	fun intentOutcomeReportWithItemOutcomesIsStableOnTheWire() =
		assertGolden("intent_outcome_report_items.json", IntentOutcomeReport.serializer())

	@Test
	fun termInsightsAreStableOnTheWire() = assertGolden("term_insights.json", TermInsights.serializer())

	@Test
	fun intentArgsAreStableOnTheWire() = assertGolden("intent_args.json", ListSerializer(IntentArgs.serializer()))

	@Test
	fun assistantEventsAreStableOnTheWire() =
		assertGolden("assistant_events.json", ListSerializer(AssistantEvent.serializer()))

	@Test
	fun extractionResultsAreStableOnTheWire() =
		assertGolden("extraction_result.json", ListSerializer(ExtractionResult.serializer()))

	@Test
	fun componentsAreStableOnTheWire() = assertGolden("components.json", ListSerializer(ChatComponent.serializer()))

	private fun <T> assertGolden(name: String, serializer: KSerializer<T>) {
		val text = checkNotNull(javaClass.getResource("/golden/$name")).readText()
		val original = Json.parseToJsonElement(text)
		val decoded = ChatContract.json.decodeFromJsonElement(serializer, original)

		assertEquals(original, ChatContract.json.encodeToJsonElement(serializer, decoded), name)
	}
}
