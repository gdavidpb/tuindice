package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.ProfileSummaryItem
import com.gdavidpb.tuindice.assistant.presentation.model.SyncSourceItem
import com.gdavidpb.tuindice.assistant.presentation.model.SyncStatusItem
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.ProfileSummaryComponent
import com.gdavidpb.tuindice.chatcontract.component.SyncStatusComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_fraction
import tuindice.assistant.generated.resources.assistant_card_a_joined
import tuindice.assistant.generated.resources.assistant_card_a_profile_credits_caption
import tuindice.assistant.generated.resources.assistant_card_a_profile_credits_description
import tuindice.assistant.generated.resources.assistant_card_a_profile_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_profile_index_description
import tuindice.assistant.generated.resources.assistant_card_a_sync_label
import tuindice.assistant.generated.resources.assistant_card_a_sync_last
import tuindice.assistant.generated.resources.assistant_card_a_sync_ok
import tuindice.assistant.generated.resources.assistant_card_a_sync_problems

internal fun ProfileSummaryComponent.toProfileSummaryItem(): ProfileSummaryItem {
	val approved = approvedCredits.toString()
	val credits = totalCredits?.let { total ->
		UiText.Resource(Res.string.assistant_card_a_fraction, listOf(approved, total))
	} ?: UiText.Raw(approved)
	val creditsFigure = CardFigureItem(
		value = credits,
		caption = UiText.Resource(Res.string.assistant_card_a_profile_credits_caption),
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
		careerCode = careerCode,
		careerName = careerName,
		pensumLabel = pensumLabel,
		primaryFigure = indexFigure ?: creditsFigure,
		secondaryFigure = if (indexFigure != null) creditsFigure else null,
		currentTermLabel = currentTermLabel
	)
}

internal fun SyncStatusComponent.toSyncStatusItem(): SyncStatusItem {
	val lastSync = lastSyncAtMillis?.let { millis ->
		UiText.Resource(
			Res.string.assistant_card_a_sync_last,
			listOf(millis.formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE))
		)
	}
	val hasProblem = sources.any { source -> isSyncProblem(source.status) }

	return SyncStatusItem(
		id = id,
		opensSheet = opensSheet(),
		labelText = lastSync ?: UiText.Resource(Res.string.assistant_card_a_sync_label),
		headlineText = if (sources.isEmpty()) {
			null
		} else {
			UiText.Resource(
				if (hasProblem) Res.string.assistant_card_a_sync_problems else Res.string.assistant_card_a_sync_ok
			)
		},
		sources = sources.map { source ->
			SyncSourceItem(
				name = source.detail?.let { detail ->
					UiText.Resource(Res.string.assistant_card_a_joined, listOf(source.name, detail))
				} ?: UiText.Raw(source.name),
				stateText = syncSourceText(source.status)
			)
		},
		annulmentCause = annulmentCause
	)
}
