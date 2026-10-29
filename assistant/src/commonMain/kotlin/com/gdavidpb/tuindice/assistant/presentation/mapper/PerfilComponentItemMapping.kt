package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.ProfileSummaryItem
import com.gdavidpb.tuindice.assistant.presentation.model.SyncSourceItem
import com.gdavidpb.tuindice.assistant.presentation.model.SyncStatusItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.ProfileSummaryComponent
import com.gdavidpb.tuindice.chatcontract.component.SyncStatusComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_profile_credits_caption_many
import tuindice.assistant.generated.resources.assistant_card_a_profile_credits_caption_one
import tuindice.assistant.generated.resources.assistant_card_a_profile_credits_description
import tuindice.assistant.generated.resources.assistant_card_a_profile_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_profile_index_description
import tuindice.assistant.generated.resources.assistant_card_a_source_success
import tuindice.assistant.generated.resources.assistant_card_a_sync_problems
import tuindice.assistant.generated.resources.assistant_card_b_fraction
import tuindice.assistant.generated.resources.assistant_card_b_joined

internal fun ProfileSummaryComponent.toProfileSummaryItem(): ProfileSummaryItem {
	val approved = approvedCredits.toString()
	val credits = totalCredits?.let { total ->
		UiText.Resource(Res.string.assistant_card_b_fraction, listOf(approved, total))
	} ?: UiText.Raw(approved)
	// Alone, the figure is the count of credits, and its caption takes the noun of that count. Beside its total, the
	// figure is a fraction, and its caption stays plural, as the total is.
	val creditsCaption = if (totalCredits == null) {
		chatCardNounText(
			count = approvedCredits,
			one = Res.string.assistant_card_a_profile_credits_caption_one,
			many = Res.string.assistant_card_a_profile_credits_caption_many
		)
	} else {
		UiText.Resource(Res.string.assistant_card_a_profile_credits_caption_many)
	}
	val creditsFigure = CardFigureItem(
		value = credits,
		caption = creditsCaption,
		contentDescription = UiText.Resource(Res.string.assistant_card_a_profile_credits_description, listOf(credits))
	)
	val indexFigure = cumulativeIndex?.let { index ->
		val value = UiText.Raw(index.toCardIndexText())

		CardFigureItem(
			value = value,
			caption = UiText.Resource(Res.string.assistant_card_a_profile_index_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_a_profile_index_description, listOf(value))
		)
	}

	return ProfileSummaryItem(
		id = id,
		opensSheet = opensSheet(),
		careerName = careerName,
		pensumLabel = pensumLabel,
		primaryFigure = indexFigure ?: creditsFigure,
		secondaryFigure = if (indexFigure != null) creditsFigure else null,
		currentTermLabel = currentTermLabel
	)
}

internal fun SyncStatusComponent.toSyncStatusItem(nowMillis: Long): SyncStatusItem {
	val hasProblem = sources.any { source -> isSyncProblem(source.status) }

	return SyncStatusItem(
		id = id,
		opensSheet = opensSheet(),
		lastSyncText = lastSyncAtMillis?.toChatCellDateText(nowMillis),
		headlineText = if (sources.isEmpty()) {
			null
		} else {
			UiText.Resource(
				if (hasProblem) Res.string.assistant_card_a_sync_problems else Res.string.assistant_card_a_source_success
			)
		},
		sources = sources.map { source ->
			SyncSourceItem(
				name = source.detail?.let { detail ->
					UiText.Resource(Res.string.assistant_card_b_joined, listOf(source.name, detail))
				} ?: UiText.Raw(source.name),
				stateChip = syncSourceChip(source.status)
			)
		},
		annulmentCause = annulmentCause
	)
}
