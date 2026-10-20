package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.ChatFallbackItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.presentation.model.SupportItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportLine
import com.gdavidpb.tuindice.chatcontract.component.SuspensionComponent
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.component.UsageNoticeComponent
import com.gdavidpb.tuindice.chatcontract.event.UsageSummary
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_suspension_open_ended
import tuindice.assistant.generated.resources.assistant_usage_hour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SistemaComponentItemMappingTest {
	@Test
	fun stillApplies_keepsItsMemoryAndStatement_andTheSheetChoice() {
		val item = StillAppliesComponent(
			id = "c2",
			fallbackText = "¿Sigue aplicando?",
			display = ComponentDisplay.SHEET,
			memoryId = "m1",
			statement = "Prefiere las notas por semana"
		).toStillAppliesItem()

		assertEquals(StillAppliesItem("c2", true, "m1", "Prefiere las notas por semana"), item)
	}

	@Test
	fun support_callsAPhoneThroughItsTelUri_andOpensThePageOfALine() {
		val item = SupportComponent(
			id = "c7",
			fallbackText = "Ayuda",
			lines = listOf(
				SupportLine("Línea de apoyo", phone = "+58 (212) 555-0000"),
				SupportLine("Página", url = "https://example.com/ayuda")
			)
		).toSupportItem()

		val (called, opened) = item.lines
		assertEquals("tel:+582125550000", called.call?.telUri)
		assertNull(called.pageUri)
		assertNull(opened.call)
		assertEquals("https://example.com/ayuda", opened.pageUri)
	}

	@Test
	fun suspension_withoutAnEnd_readsOpenEnded() {
		val item = SuspensionComponent(
			id = "c6",
			fallbackText = "Suspendido",
			reason = AbuseReasonCode.AUTOMATION,
			appealable = false
		).toSuspensionItem(nowMillis = 0L)

		assertEquals(UiText.Resource(Res.string.assistant_suspension_open_ended), item.untilText)
		assertEquals(AbuseReasonCode.AUTOMATION.toReasonText(), item.reasonText)
	}

	@Test
	fun usageNotice_writesEachWindow_andShowsTheResetOnlyWhenKnown() {
		val usage = UsageSummary(remainingHour = 9, remainingDay = 39, remainingMonth = 399)

		val known = UsageNoticeComponent("c3", "Te quedan 9", usage = usage, resetsAtMillis = 1760003600000)
			.toUsageNoticeItem(nowMillis = 0L)
		val unknown = UsageNoticeComponent("c3", "Te quedan 9", usage = usage).toUsageNoticeItem(nowMillis = 0L)

		assertEquals(UiText.Resource(Res.string.assistant_usage_hour, listOf(9)), known.hourText)
		assertIs<UiText.Resource>(known.resetsText)
		assertNull(unknown.resetsText)
	}

	@Test
	fun aPortedComponent_isItsOwnItem_andAnUnknownOneFallsBackToItsText() {
		val ported = StillAppliesComponent("c2", "¿Sigue aplicando?", memoryId = "m1", statement = "x")
		val unknown = UnknownChatComponent(id = "u1", fallbackText = "Texto")

		assertIs<StillAppliesItem>(ported.toChatComponentItem(nowMillis = 0L))
		assertEquals(ChatFallbackItem("u1", false, "Texto"), unknown.toChatComponentItem(nowMillis = 0L))
		assertIs<SupportItem>(SupportComponent("c7", "Ayuda", lines = emptyList()).toChatComponentItem(nowMillis = 0L))
	}
}
