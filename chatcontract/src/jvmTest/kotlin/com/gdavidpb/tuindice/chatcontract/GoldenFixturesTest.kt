package com.gdavidpb.tuindice.chatcontract

import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.insights.TermInsights
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/** Golden files are shared with the backend: decoding and encoding them again must give the same JSON tree. */
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

	private fun <T> assertGolden(name: String, serializer: KSerializer<T>) {
		val text = checkNotNull(javaClass.getResource("/golden/$name")).readText()
		val original = Json.parseToJsonElement(text)
		val decoded = ChatContract.json.decodeFromJsonElement(serializer, original)

		assertEquals(original, ChatContract.json.encodeToJsonElement(serializer, decoded), name)
	}
}
