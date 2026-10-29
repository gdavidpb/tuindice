package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElective
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElectivesComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectListComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectRow
import com.gdavidpb.tuindice.chatcontract.component.SubjectStatsComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_outcome_approved
import tuindice.assistant.generated.resources.assistant_card_b_band_high
import tuindice.assistant.generated.resources.assistant_card_b_link_subject
import tuindice.assistant.generated.resources.assistant_card_b_link_subject_named
import tuindice.assistant.generated.resources.assistant_card_b_see_all_electives
import tuindice.assistant.generated.resources.assistant_card_b_see_all_subjects
import tuindice.assistant.generated.resources.assistant_card_b_stats_average_caption
import tuindice.assistant.generated.resources.assistant_card_b_stats_pass_caption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MateriasComponentItemMappingTest {
	@Test
	fun subjectList_linksEachSubjectToItsOwnScreen_andNamesIt_andShowsItsStatusOnlyWhenItHasOne() {
		val item = SubjectListComponent(
			id = "m1",
			fallbackText = "Materias",
			items = listOf(
				SubjectRow("IS-101", "Programación I", credits = 4, status = "approved"),
				SubjectRow("IS-102", "Física", credits = 4, status = null)
			)
		).toSubjectListItem()

		assertEquals(
			CardLinkItem(
				target = NavTarget.SUBJECTS_DETAIL,
				text = UiText.Resource(Res.string.assistant_card_b_link_subject),
				entityRefs = listOf(EntityRef(EntityKind.SUBJECT, "IS-101")),
				accessibilityText = UiText.Resource(Res.string.assistant_card_b_link_subject_named, listOf("IS-101"))
			),
			item.rows[0].link
		)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_a_outcome_approved), CardChipTone.Approved),
			item.rows[0].statusChip
		)
		assertNull(item.rows[1].statusChip)
		assertEquals(UiText.Resource(Res.string.assistant_card_b_see_all_subjects, listOf(2)), item.seeAllText)
	}

	@Test
	fun subjectStats_isTheAverageWithThePassRateBeside_orThePassRateAloneWithoutAnAverage() {
		val both = SubjectStatsComponent(
			id = "m2",
			fallbackText = "Estadísticas",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			scope = "all",
			average = 14.5,
			median = 15.0,
			passRate = 0.72,
			difficulty = "high",
			histogram = listOf(1, 3)
		).toSubjectStatsItem()
		val passOnly = SubjectStatsComponent(
			id = "m3",
			fallbackText = "Estadísticas",
			subjectCode = "IS-101",
			subjectName = "Programación I",
			scope = "all",
			average = null,
			median = null,
			passRate = 0.72,
			difficulty = null,
			histogram = emptyList()
		).toSubjectStatsItem()

		assertEquals(UiText.Raw("14.50"), both.primaryFigure?.value)
		assertEquals(UiText.Resource(Res.string.assistant_card_b_stats_average_caption), both.primaryFigure?.caption)
		assertEquals(UiText.Resource(Res.string.assistant_card_b_stats_pass_caption), both.secondaryFigure?.caption)
		assertEquals(
			CardChipItem(UiText.Resource(Res.string.assistant_card_b_band_high), CardChipTone.Neutral),
			both.difficultyChip
		)
		assertEquals(listOf(1f / 3f, 1f), both.histogram.map { bar -> bar.fraction })

		assertEquals(UiText.Resource(Res.string.assistant_card_b_stats_pass_caption), passOnly.primaryFigure?.caption)
		assertNull(passOnly.secondaryFigure)
		assertNull(passOnly.medianText)
	}

	@Test
	fun recommendedElectives_keepTheCodeNameAndReasonOfEachElective() {
		val item = RecommendedElectivesComponent(
			id = "m4",
			fallbackText = "Electivas",
			items = listOf(RecommendedElective("IS-210", "Redes", credits = 3, reason = "Complementa tu línea"))
		).toRecommendedElectivesItem()

		assertEquals("IS-210", item.rows.single().code)
		assertEquals("Complementa tu línea", item.rows.single().reason)
		assertNull(item.recordLink)
		assertEquals(UiText.Resource(Res.string.assistant_card_b_see_all_electives, listOf(1)), item.seeAllText)
	}
}
