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
	fun anActionTowardAScreenThisBuildDoesNotKnowIsLeftOutAndTheReportArrives() {
		val text = """
			{"items":[{"id":"term-1:3","termKey":"2026-APR_JUL","kind":"term_end","title":"Cerraste","body":"Aprobaste.",
			"nextActions":[
				{"target":"record.projection","label":"Proyectar tu índice","entityRefs":[]},
				{"target":"plan.create","label":"Planear el trimestre","entityRefs":[]},
				{"target":"share_term","label":"Compartir tu trimestre","entityRefs":[{"kind":"term","id":"term-1"}]}
			]}]}
		""".trimIndent()

		val report = ChatContract.json.decodeFromString(TermInsights.serializer(), text).items.single()

		assertEquals(
			listOf(
				InsightAction(NavTarget.RECORD_PROJECTION, "Proyectar tu índice"),
				InsightAction(NavTarget.SHARE_TERM, "Compartir tu trimestre", listOf(term))
			),
			report.nextActions
		)
	}

	@Test
	fun anActionWithAnEntityKindThisBuildDoesNotKnowIsLeftOutToo() {
		val text = """
			{"id":"term-1:3","termKey":"2026-APR_JUL","kind":"term_end","title":"Cerraste","body":"Aprobaste.",
			"nextActions":[
				{"target":"share_term","label":"Compartir","entityRefs":[{"kind":"course","id":"c-1"}]},
				{"target":"record","label":"Ver tu récord"}
			]}
		""".trimIndent()

		val decoded = ChatContract.json.decodeFromString(TermInsight.serializer(), text)

		assertEquals(listOf(InsightAction(NavTarget.RECORD, "Ver tu récord")), decoded.nextActions)
	}

	// A report whose every action this build cannot read still arrives: it keeps its text and shows no buttons.
	@Test
	fun aReportWhoseActionsAreAllUnknownArrivesWithoutThem() {
		val text = """
			{"items":[{"id":"term-1:4","termKey":"2026-APR_JUL","kind":"term_end","title":"Cerraste","body":"Aprobaste.",
			"nextActions":[{"target":"plan.create","label":"Planear el trimestre","entityRefs":[]}]}]}
		""".trimIndent()

		val report = ChatContract.json.decodeFromString(TermInsights.serializer(), text).items.single()

		assertEquals("Cerraste", report.title)
		assertEquals(emptyList(), report.nextActions)
	}

	// Actions written as null leave the report without buttons, and the report still arrives with its text.
	@Test
	fun aReportWhoseActionsAreNullArrivesWithoutThem() {
		val text = """
			{"items":[{"id":"term-1:5","termKey":"2026-APR_JUL","kind":"term_end","title":"Cerraste","body":"Aprobaste.",
			"nextActions":null}]}
		""".trimIndent()

		val report = ChatContract.json.decodeFromString(TermInsights.serializer(), text).items.single()

		assertEquals("Cerraste", report.title)
		assertEquals(emptyList(), report.nextActions)
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
