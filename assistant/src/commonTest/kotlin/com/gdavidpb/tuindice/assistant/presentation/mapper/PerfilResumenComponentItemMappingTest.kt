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
import tuindice.assistant.generated.resources.assistant_card_a_link_evaluation
import tuindice.assistant.generated.resources.assistant_card_a_link_evaluation_named
import tuindice.assistant.generated.resources.assistant_card_a_profile_credits_caption_many
import tuindice.assistant.generated.resources.assistant_card_a_profile_credits_caption_one
import tuindice.assistant.generated.resources.assistant_card_a_see_all_attention
import tuindice.assistant.generated.resources.assistant_card_a_source_success
import tuindice.assistant.generated.resources.assistant_card_a_sync_problems
import tuindice.assistant.generated.resources.assistant_card_b_fraction
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
		assertEquals(UiText.Resource(Res.string.assistant_card_b_fraction, listOf("80", 180)), item.secondaryFigure?.value)
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
		).toSyncStatusItem(nowMillis = 0L)
		val fine = SyncStatusComponent(
			id = "d2",
			fallbackText = "Sync",
			lastSyncAtMillis = null,
			sources = listOf(SyncSourceRow("Notas", "success")),
			enrollmentStatus = null,
			annulmentCause = null
		).toSyncStatusItem(nowMillis = 0L)

		assertEquals(UiText.Resource(Res.string.assistant_card_a_sync_problems), problem.headlineText)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_source_success), fine.headlineText)
	}

	@Test
	fun profile_theCreditsCaptionTakesTheNounOfTheCount_unlessTheFigureIsAFraction() {
		val one = ProfileSummaryComponent(
			id = "d3",
			fallbackText = "Perfil",
			careerCode = "IS",
			careerName = "Ingeniería de Sistemas",
			pensumLabel = "2016",
			cumulativeIndex = null,
			approvedCredits = 1,
			totalCredits = null,
			currentTermLabel = null
		).toProfileSummaryItem()
		val many = ProfileSummaryComponent(
			id = "d4",
			fallbackText = "Perfil",
			careerCode = "IS",
			careerName = "Ingeniería de Sistemas",
			pensumLabel = "2016",
			cumulativeIndex = null,
			approvedCredits = 80,
			totalCredits = null,
			currentTermLabel = null
		).toProfileSummaryItem()
		val fraction = ProfileSummaryComponent(
			id = "d5",
			fallbackText = "Perfil",
			careerCode = "IS",
			careerName = "Ingeniería de Sistemas",
			pensumLabel = "2016",
			cumulativeIndex = null,
			approvedCredits = 1,
			totalCredits = 180,
			currentTermLabel = null
		).toProfileSummaryItem()

		assertEquals(UiText.Resource(Res.string.assistant_card_a_profile_credits_caption_one), one.primaryFigure.caption)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_profile_credits_caption_many), many.primaryFigure.caption)
		assertEquals(
			UiText.Resource(Res.string.assistant_card_a_profile_credits_caption_many),
			fraction.primaryFigure.caption
		)
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
				entityRefs = listOf(EntityRef(EntityKind.EVALUATION, "e1")),
				accessibilityText = UiText.Resource(Res.string.assistant_card_a_link_evaluation_named, listOf("Parcial 2"))
			),
			withScreen.link
		)
		assertNull(planOnly.link)
		assertNull(noRef.link)
		assertEquals(UiText.Resource(Res.string.assistant_card_a_see_all_attention, listOf(3)), item.seeAllText)
	}
}
