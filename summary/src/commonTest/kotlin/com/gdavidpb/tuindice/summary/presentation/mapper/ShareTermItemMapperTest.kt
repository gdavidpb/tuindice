package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareTermSource
import com.gdavidpb.tuindice.summary.testing.otherShareBackground
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_card_career
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShareTermItemMapperTest {
	@Test
	fun when_aTermHasAnIndex_then_itsCardCarriesItsFiguresWithFourDecimals() {
		val term = statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR, index = 4.25, approvedCredits = 15)

		val card = assertNotNull(term.toShareableTermCardItem(careerName = CAREER))

		assertEquals(UiText.Raw("Enero - Marzo 2024"), card.term)
		assertEquals("4.2500", card.index)
		assertEquals("4.2500", card.cumulative.value)
		assertEquals("15", card.credits.value)
		assertEquals(term.approvedSubjects.toString(), card.approved.value)
		assertEquals(UiText.Resource(Res.string.share_term_card_career, listOf(CAREER)), card.career)
	}

	@Test
	fun when_aTermHasNoIndex_then_thereIsNoCardForIt() {
		assertNull(statisticsTerm(year = 2024, index = null).toShareableTermCardItem(careerName = CAREER))
	}

	@Test
	fun when_theCareerHasNoName_then_TheCardCarriesTheBrandAlone() {
		val card = assertNotNull(statisticsTerm(year = 2024).toShareableTermCardItem(careerName = "  "))

		assertEquals(UiText.Raw("USB"), card.career)
	}

	@Test
	fun when_theTermsAreShown_then_theyAreNewestFirst_andTheIndexlessOnesAreLeftOut() {
		val oldest = statisticsTerm(year = 2023, index = 3.0)
		val withoutIndex = statisticsTerm(year = 2024, index = null)
		val newest = statisticsTerm(year = 2025, index = 4.0)

		val content = source(closedTerms = listOf(oldest, withoutIndex, newest)).toShareTermContent(
			requestedTermKey = null,
			current = null
		)

		assertEquals(listOf(newest.termKey, oldest.termKey), content.terms.map { item -> item.key })
		assertEquals(listOf(true, false), content.terms.map { item -> item.selected })
	}

	@Test
	fun when_aTermIsRequested_then_itIsSelected() {
		val oldest = statisticsTerm(year = 2023, index = 3.0)
		val newest = statisticsTerm(year = 2025, index = 4.0)

		val content = source(closedTerms = listOf(oldest, newest)).toShareTermContent(
			requestedTermKey = oldest.termKey,
			current = null
		)

		assertEquals(oldest.termKey, content.terms.single { item -> item.selected }.key)
	}

	@Test
	fun when_theChosenTermIsRefreshed_then_itStaysSelected_andSharingStaysAsItWas() {
		val oldest = statisticsTerm(year = 2023, index = 3.0)
		val newest = statisticsTerm(year = 2025, index = 4.0)
		val source = source(closedTerms = listOf(oldest, newest))
		val current = source.toShareTermContent(requestedTermKey = oldest.termKey, current = null)
			.copy(isSharing = true)

		val refreshed = source.toShareTermContent(requestedTermKey = null, current = current)

		assertEquals(oldest.termKey, refreshed.terms.single { item -> item.selected }.key)
		assertTrue(refreshed.isSharing)
	}

	@Test
	fun when_aTermIsNotThere_then_theNewestOneIsSelected() {
		val newest = statisticsTerm(year = 2025, index = 4.0)

		val content = source(closedTerms = listOf(newest)).toShareTermContent(
			requestedTermKey = "a-term-that-is-not-there",
			current = null
		)

		assertEquals(newest.termKey, content.terms.single { item -> item.selected }.key)
	}

	@Test
	fun when_aBackgroundWasChosenLast_then_itIsTheSelectedOne() {
		val content = source(
			backgrounds = listOf(ShareBackground.included, otherShareBackground),
			preferredBackgroundId = otherShareBackground.id
		).toShareTermContent(requestedTermKey = null, current = null)

		assertEquals(otherShareBackground.id, content.backgrounds.single { item -> item.selected }.background.id)
	}

	@Test
	fun when_noBackgroundWasChosen_then_theFirstOneIsSelected_andAChoiceIsKeptOnRefresh() {
		val source = source(
			backgrounds = listOf(ShareBackground.included, otherShareBackground),
			preferredBackgroundId = null
		)
		val first = source.toShareTermContent(requestedTermKey = null, current = null)
		assertEquals(ShareBackground.included.id, first.backgrounds.single { item -> item.selected }.background.id)

		val chosen = first.copy(
			backgrounds = first.backgrounds.map { item -> item.copy(selected = item.background.id == otherShareBackground.id) }
		)
		val refreshed = source.toShareTermContent(requestedTermKey = null, current = chosen)

		assertEquals(otherShareBackground.id, refreshed.backgrounds.single { item -> item.selected }.background.id)
	}

	private fun source(
		closedTerms: List<com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic> = emptyList(),
		backgrounds: List<ShareBackground> = listOf(ShareBackground.included),
		preferredBackgroundId: String? = null
	) = ShareTermSource(
		closedTerms = closedTerms,
		careerName = CAREER,
		backgrounds = backgrounds,
		preferredBackgroundId = preferredBackgroundId
	)

	private companion object {
		const val CAREER = "Ingeniería de Computación"
	}
}
