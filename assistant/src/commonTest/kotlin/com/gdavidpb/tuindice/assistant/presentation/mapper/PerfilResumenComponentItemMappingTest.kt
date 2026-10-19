package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.AttentionItem
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.ProfileSummaryComponent
import com.gdavidpb.tuindice.chatcontract.component.SyncSourceRow
import com.gdavidpb.tuindice.chatcontract.component.SyncStatusComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_fraction
import tuindice.assistant.generated.resources.assistant_card_a_link_evaluation
import tuindice.assistant.generated.resources.assistant_card_a_sync_ok
import tuindice.assistant.generated.resources.assistant_card_a_sync_problems
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PerfilResumenComponentItemMappingTest {
	@Test
	fun profile_withoutAnIndex_leavesTheCreditsAsTheOnlyFigure() {
		val item = ProfileSummaryComponent(
			id = "d1",
			fallbackText = "Perfil",
			careerCode = "IS",
			careerName = "Ingeniería de Sistemas",
			pensumLabel = "2016",
			cumulativeIndex = null,
			approvedCredits = 80,
			totalCredits = null,
			currentTermLabel = null
		).toProfileSummaryItem()

		assertEquals(UiText.Raw("80"), item.primaryFigure.value)
		assertNull(item.secondaryFigure)
	}

	@Test
	fun profile_withAnIndex_showsTheIndexFirst_andTheCreditsBeside() {
		val item = ProfileSummaryComponent(
			id = "d1",
			fallbackText = "Perfil",
			careerCode = "IS",
			careerName = "Ingeniería de Sistemas",
			pensumLabel = "2016",
			cumulativeIndex = 16.4,
			approvedCredits = 80,
			totalCredits = 180,
			currentTermLabel = "2026-JAN_MAR"
		).toProfileSummaryItem()

		assertEquals(UiText.Raw(16.4.toCardIndexText()), item.primaryFigure.value)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_fraction, listOf("80", 180)), item.secondaryFigure?.value)
	}

	@Test
	fun sync_headlineIsAProblemOnlyWhenASourceIsUnavailable() {
		val problem = SyncStatusComponent(
			id = "d2",
			fallbackText = "Sync",
			lastSyncAtMillis = null,
			sources = listOf(SyncSourceRow("Notas", "unavailable")),
			enrollmentStatus = null,
			annulmentCause = null
		).toSyncStatusItem()
		val fine = SyncStatusComponent(
			id = "d2",
			fallbackText = "Sync",
			lastSyncAtMillis = null,
			sources = listOf(SyncSourceRow("Notas", "success")),
			enrollmentStatus = null,
			annulmentCause = null
		).toSyncStatusItem()

		assertEquals(UiText.Resource(Res.string.assistant_card_a_sync_problems), problem.headlineText)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_sync_ok), fine.headlineText)
	}

	@Test
	fun attention_linksOnlyTheKindsThatHaveAScreen() {
		val item = AttentionComponent(
			id = "r1",
			fallbackText = "Atención",
			items = listOf(
				AttentionItem("evaluacion", "Parcial 2", "Sin nota", EntityRef(EntityKind.EVALUATION, "e1")),
				AttentionItem("plan", "Plan", "Por revisar", EntityRef(EntityKind.PLAN, "p1")),
				AttentionItem("sin_ref", "Otro", "Sin referencia")
			)
		).toAttentionCardItem()

		val (withScreen, planOnly, noRef) = item.rows
		assertEquals(
			CardLinkItem(
				target = NavTarget.EVALUATIONS_DETAIL,
				text = UiText.Resource(Res.string.assistant_card_a_link_evaluation),
				entityRefs = listOf(EntityRef(EntityKind.EVALUATION, "e1"))
			),
			withScreen.link
		)
		assertNull(planOnly.link)
		assertNull(noRef.link)
	}
}
