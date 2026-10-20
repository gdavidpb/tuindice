package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.ElectiveRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.RecommendedElectivesItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectListItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectStatsItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElectivesComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectListComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectStatsComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_credits
import tuindice.assistant.generated.resources.assistant_card_b_joined
import tuindice.assistant.generated.resources.assistant_card_b_stats_average_caption
import tuindice.assistant.generated.resources.assistant_card_b_stats_average_description
import tuindice.assistant.generated.resources.assistant_card_b_stats_pass_caption
import tuindice.assistant.generated.resources.assistant_card_b_stats_pass_description
import tuindice.assistant.generated.resources.assistant_card_b_subjects_caption
import tuindice.assistant.generated.resources.assistant_card_b_subjects_description

internal fun SubjectListComponent.toSubjectListItem(): SubjectListItem {
	val count = UiText.Raw(items.size.toString())

	return SubjectListItem(
		id = id,
		opensSheet = opensSheet(),
		countFigure = CardFigureItem(
			value = count,
			caption = UiText.Resource(Res.string.assistant_card_b_subjects_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_subjects_description, listOf(count))
		),
		rows = items.map { subject ->
			val credits = UiText.Resource(Res.string.assistant_card_b_credits, listOf(subject.credits))

			SubjectRowItem(
				code = subject.code,
				nameText = UiText.Resource(Res.string.assistant_card_b_joined, listOf(subject.name, credits)),
				statusChip = subjectStatusChip(subject.status),
				link = chatSubjectLinkItem(subject.code)
			)
		},
		recordLink = entityRefs.toRecordLinkItem()
	)
}

internal fun SubjectStatsComponent.toSubjectStatsItem(): SubjectStatsItem {
	val averageFigure = average?.toCardCompactNumberText()?.let { text ->
		val value = UiText.Raw(text)

		CardFigureItem(
			value = value,
			caption = UiText.Resource(Res.string.assistant_card_b_stats_average_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_stats_average_description, listOf(value))
		)
	}
	val passFigure = passRate?.toCardPercent()?.let { percent ->
		val rate = chatCardPercentText(percent)

		CardFigureItem(
			value = rate,
			caption = UiText.Resource(Res.string.assistant_card_b_stats_pass_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_stats_pass_description, listOf(rate))
		)
	}

	return SubjectStatsItem(
		id = id,
		opensSheet = opensSheet(),
		subjectName = subjectName,
		primaryFigure = averageFigure ?: passFigure,
		secondaryFigure = if (averageFigure != null) passFigure else null,
		difficultyChip = bandChip(difficulty),
		medianText = median?.toCardCompactNumberText(),
		histogram = histogram.toHistogramBarItems(),
		recordLink = entityRefs.toRecordLinkItem()
	)
}

internal fun RecommendedElectivesComponent.toRecommendedElectivesItem(): RecommendedElectivesItem {
	return RecommendedElectivesItem(
		id = id,
		opensSheet = opensSheet(),
		rows = items.map { elective ->
			ElectiveRowItem(code = elective.code, name = elective.name, reason = elective.reason)
		},
		recordLink = entityRefs.toRecordLinkItem()
	)
}
