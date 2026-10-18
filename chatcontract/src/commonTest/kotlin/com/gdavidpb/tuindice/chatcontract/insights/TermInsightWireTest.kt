package com.gdavidpb.tuindice.chatcontract.insights

import com.gdavidpb.tuindice.chatcontract.ChatContract
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.context.ScreenContext
import com.gdavidpb.tuindice.chatcontract.context.ScreenId
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TermInsightWireTest {

	private val term = EntityRef(EntityKind.TERM, "term-1")

	private val termEnd = TermInsight(
		id = "term-1:2",
		termKey = "2026-APR_JUL",
		kind = TermInsightTrigger.TERM_END,
		title = "Cerraste con 4,18",
		body = "Aprobaste las 3 materias.\n\nTu ritmo\n\nEste trimestre aprobaste 11 créditos.",
		entityRefs = listOf(term),
		sections = listOf(
			InsightSection("Cerraste con 4,18", "Aprobaste las 3 materias."),
			InsightSection("Tu ritmo", "Este trimestre aprobaste 11 créditos.")
		),
		nextActions = listOf(
			InsightAction(NavTarget.RECORD_PROJECTION, "Proyectar tu índice para Jul - Ago"),
			InsightAction(NavTarget.SHARE_TERM, "Compartir tu trimestre", listOf(term))
		),
		createdAtMillis = 1_761_000_000_000L
	)

	@Test
	fun insightRoundTripsOnTheWire() {
		val encoded = ChatContract.json.encodeToString(TermInsight.serializer(), termEnd)
		val decoded = ChatContract.json.decodeFromString(TermInsight.serializer(), encoded)

		assertEquals(termEnd, decoded)
	}

	@Test
	fun actionsCarryTheWireNameOfTheirTarget() {
		val json = ChatContract.json.encodeToJsonElement(TermInsight.serializer(), termEnd).jsonObject
		val actions = json.getValue("nextActions").jsonArray

		assertEquals("record.projection", actions[0].jsonObject.getValue("target").jsonPrimitive.content)
		assertEquals("share_term", actions[1].jsonObject.getValue("target").jsonPrimitive.content)
	}

	@Test
	fun insightWrittenBeforeTheNewFieldsDecodesWithTheirDefaults() {
		val legacy = """{"id":"term-1:1","termKey":"2026-JAN_MAR","kind":"new_grades","title":"T","body":"B"}"""

		val decoded = ChatContract.json.decodeFromString(TermInsight.serializer(), legacy)

		assertEquals(emptyList(), decoded.entityRefs)
		assertEquals(emptyList(), decoded.sections)
		assertEquals(emptyList(), decoded.nextActions)
		assertEquals(0L, decoded.createdAtMillis)
	}

	@Test
	fun feedbackRequestIsTheUsefulFlag() {
		val request = InsightFeedbackRequest(useful = true)
		val encoded = ChatContract.json.encodeToString(InsightFeedbackRequest.serializer(), request)

		assertEquals("""{"useful":true}""", encoded)
	}

	@Test
	fun termReportScreenIsSentAsTermReport() {
		val context = ScreenContext(screen = ScreenId.TERM_REPORT, entities = listOf(term))

		val encoded = ChatContract.json.encodeToString(ScreenContext.serializer(), context)

		assertTrue(encoded.contains("\"screen\":\"term_report\""), encoded)
	}

	@Test
	fun triggersAreTheWireValuesOfTheKind() {
		assertEquals("term_end", TermInsightTrigger.TERM_END)
		assertEquals("new_grades", TermInsightTrigger.NEW_GRADES)
	}
}
