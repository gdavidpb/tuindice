package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_card_approved_many
import tuindice.summary.generated.resources.share_term_card_career
import tuindice.summary.generated.resources.share_term_card_credits_many
import tuindice.summary.generated.resources.share_term_card_cumulative_label

// The card of the term "Enero - Marzo 2026" that the card tests draw, with the index given.
fun shareCardItem(index: String = "3.6875") = ShareableTermCardItem(
	term = UiText.Raw("Enero - Marzo 2026"),
	index = index,
	approved = SummaryStatisticsFigureItem(
		value = "3",
		label = UiText.Resource(Res.string.share_term_card_approved_many)
	),
	credits = SummaryStatisticsFigureItem(
		value = "12",
		label = UiText.Resource(Res.string.share_term_card_credits_many)
	),
	cumulative = SummaryStatisticsFigureItem(
		value = "4.1250",
		label = UiText.Resource(Res.string.share_term_card_cumulative_label)
	),
	career = UiText.Resource(Res.string.share_term_card_career, listOf("Ingeniería de Computación"))
)

// An artwork the app ships with, whose work has no author: its credit is only its title.
fun bibliotecaArtwork() = ShareTermArtwork(
	id = "biblioteca-central",
	shortName = "Biblioteca",
	title = "Biblioteca Central",
	author = null,
	art = ShareTermArt.Bundled
)
