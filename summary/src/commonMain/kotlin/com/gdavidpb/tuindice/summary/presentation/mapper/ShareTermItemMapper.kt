package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.TermStatistic
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.domain.mapper.selectShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import com.gdavidpb.tuindice.summary.domain.model.ShareTermSource
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArt
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermArtwork
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermTermItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_card_approved_many
import tuindice.summary.generated.resources.share_term_card_approved_one
import tuindice.summary.generated.resources.share_term_card_career
import tuindice.summary.generated.resources.share_term_card_credits_many
import tuindice.summary.generated.resources.share_term_card_credits_one
import tuindice.summary.generated.resources.share_term_card_cumulative_label

private const val INDEX_DECIMALS = 4

/**
 * The card of a closed term, in the words of the card: its figures, its index and cumulative average with four
 * decimals, and the career of the student. Null for a term without an index, which has nothing to put on a card.
 */
internal fun TermStatistic.toShareableTermCardItem(careerName: String): ShareableTermCardItem? {
	val index = index ?: return null
	val cumulative = cumulativeIndex ?: index

	return ShareableTermCardItem(
		term = UiText.Raw("${periodCode.label} $periodYear"),
		index = index.formatGrade(decimals = INDEX_DECIMALS),
		approved = SummaryStatisticsFigureItem(
			value = approvedSubjects.toString(),
			label = countText(
				quantity = approvedSubjects,
				one = Res.string.share_term_card_approved_one,
				many = Res.string.share_term_card_approved_many,
				args = emptyList()
			)
		),
		credits = SummaryStatisticsFigureItem(
			value = approvedCredits.toString(),
			label = countText(
				quantity = approvedCredits,
				one = Res.string.share_term_card_credits_one,
				many = Res.string.share_term_card_credits_many,
				args = emptyList()
			)
		),
		cumulative = SummaryStatisticsFigureItem(
			value = cumulative.formatGrade(decimals = INDEX_DECIMALS),
			label = UiText.Resource(Res.string.share_term_card_cumulative_label)
		),
		career = careerTextOf(careerName = careerName)
	)
}

/**
 * What the share sheet shows of [this] source: the closed terms that have an index, newest first, with one of them
 * selected (the one [current] already selects, else the one [requestedTermKey] asks for, else the newest), and the
 * backgrounds with one selected (the one [current] already selects, else the one the student chose last, else the first
 * of them).
 */
internal fun ShareTermSource.toShareTermContent(
	requestedTermKey: String?,
	current: ShareTerm.State.Content?
): ShareTerm.State.Content {
	val termItems = closedTerms.asReversed().mapNotNull { term ->
		term.toShareableTermCardItem(careerName = careerName)?.let { card ->
			ShareTermTermItem(
				key = term.termKey,
				label = UiText.Raw(term.fullLabel()),
				card = card,
				selected = false
			)
		}
	}
	val chosenTermKey = current?.terms?.firstOrNull { item -> item.selected }?.key ?: requestedTermKey
	val selectedTermKey = termItems.firstOrNull { item -> item.key == chosenTermKey }?.key
		?: termItems.firstOrNull()?.key

	val chosenBackgroundId = current?.backgrounds?.firstOrNull { item -> item.selected }?.artwork?.id
		?: preferredBackgroundId
	val selectedBackgroundId = backgrounds.selectShareBackground(preferredId = chosenBackgroundId)?.id

	return ShareTerm.State.Content(
		terms = termItems.map { item -> item.copy(selected = item.key == selectedTermKey) },
		backgrounds = backgrounds.map { background ->
			ShareTermBackgroundItem(
				artwork = background.toShareTermArtwork(),
				selected = background.id == selectedBackgroundId
			)
		},
		isSharing = current?.isSharing == true
	)
}

// The artwork a background wears on the card and in the picker: a remote one falls back to the one the app ships with,
// credit and all, so the card has one rule for an artwork that cannot be loaded and one credit for each artwork.
internal fun ShareBackground.toShareTermArtwork(): ShareTermArtwork {
	return when (val source = art) {
		ShareBackgroundArt.Bundled -> ShareTermArtwork(
			id = id,
			shortName = shortName,
			title = title,
			author = author,
			art = ShareTermArt.Bundled
		)

		is ShareBackgroundArt.Remote -> ShareTermArtwork(
			id = id,
			shortName = shortName,
			title = title,
			author = author,
			art = ShareTermArt.Remote(cardUrl = source.cardUrl, thumbUrl = source.thumbUrl),
			fallback = ShareBackground.included.toShareTermArtwork()
		)
	}
}

// "Ingeniería de Computación · USB", or the brand alone when the career has no name to give.
private fun careerTextOf(careerName: String): UiText {
	return if (careerName.isBlank()) {
		UiText.Raw(BRAND)
	} else {
		UiText.Resource(Res.string.share_term_card_career, listOf(careerName))
	}
}

private const val BRAND = "USB"
