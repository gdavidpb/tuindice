package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationListItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.PointsRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.RequiredGradeItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectPointsItem
import com.gdavidpb.tuindice.assistant.presentation.model.WeekLoadRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.WeeklyLoadItem
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.RequiredGradeComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsComponent
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_goal_feasible
import tuindice.assistant.generated.resources.assistant_card_a_goal_not_feasible
import tuindice.assistant.generated.resources.assistant_card_b_count_evaluation_many
import tuindice.assistant.generated.resources.assistant_card_b_count_evaluation_one
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_no_grade
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_caption
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_description
import tuindice.assistant.generated.resources.assistant_card_b_fraction
import tuindice.assistant.generated.resources.assistant_card_b_joined
import tuindice.assistant.generated.resources.assistant_card_b_load_caption
import tuindice.assistant.generated.resources.assistant_card_b_load_description
import tuindice.assistant.generated.resources.assistant_card_b_required_caption
import tuindice.assistant.generated.resources.assistant_card_b_required_description
import tuindice.assistant.generated.resources.assistant_card_b_required_remaining_caption
import tuindice.assistant.generated.resources.assistant_card_b_required_remaining_description
import tuindice.assistant.generated.resources.assistant_card_b_required_target
import tuindice.assistant.generated.resources.assistant_card_b_week

internal fun EvaluationListComponent.toEvaluationListItem(): EvaluationListItem {
	val count = UiText.Raw(items.size.toString())

	return EvaluationListItem(
		id = id,
		opensSheet = opensSheet(),
		countFigure = CardFigureItem(
			value = count,
			caption = UiText.Resource(Res.string.assistant_card_b_evaluations_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_evaluations_description, listOf(count))
		),
		rows = items.map { row ->
			val date = row.dateMillis?.formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE)
			val maxGrade = row.maxGrade.toCardCompactNumberText()

			EvaluationRowItem(
				code = row.subjectCode,
				nameText = if (date == null) {
					UiText.Raw(row.title)
				} else {
					UiText.Resource(Res.string.assistant_card_b_joined, listOf(row.title, date))
				},
				gradeText = row.grade?.let { value ->
					UiText.Resource(Res.string.assistant_card_b_fraction, listOf(value.toCardCompactNumberText(), maxGrade))
				} ?: UiText.Resource(Res.string.assistant_card_b_evaluation_no_grade, listOf(maxGrade)),
				stateText = evaluationStateText(row.state)
			)
		},
		recordLink = entityRefs.toRecordLinkItem()
	)
}

internal fun SubjectPointsComponent.toSubjectPointsItem(): SubjectPointsItem {
	return SubjectPointsItem(
		id = id,
		opensSheet = opensSheet(),
		rows = rows.map { row ->
			PointsRowItem(
				code = row.subjectCode,
				name = row.subjectName,
				pointsText = UiText.Resource(
					Res.string.assistant_card_b_fraction,
					listOf(row.earned.toCardCompactNumberText(), row.evaluated.toCardCompactNumberText())
				),
				progress = chatCardFraction(row.evaluated, row.total)
			)
		},
		recordLink = entityRefs.toRecordLinkItem()
	)
}

internal fun WeeklyLoadComponent.toWeeklyLoadItem(): WeeklyLoadItem {
	val heavy = UiText.Raw(weeks.count { week -> week.heavy }.toString())

	return WeeklyLoadItem(
		id = id,
		opensSheet = opensSheet(),
		heavyFigure = CardFigureItem(
			value = heavy,
			caption = UiText.Resource(Res.string.assistant_card_b_load_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_load_description, listOf(heavy))
		),
		weeks = weeks.map { week ->
			WeekLoadRowItem(
				weekText = UiText.Resource(Res.string.assistant_card_b_week, listOf(week.week)),
				detailText = UiText.Resource(
					Res.string.assistant_card_b_joined,
					listOf(
						chatCardCountText(
							count = week.count,
							one = Res.string.assistant_card_b_count_evaluation_one,
							many = Res.string.assistant_card_b_count_evaluation_many
						),
						chatCardPointsText(week.points)
					)
				),
				heavy = week.heavy
			)
		},
		recordLink = entityRefs.toRecordLinkItem()
	)
}

internal fun RequiredGradeComponent.toRequiredGradeItem(): RequiredGradeItem {
	val remaining = remainingPoints.toCardCompactNumberText()
	val remainingFigure = CardFigureItem(
		value = UiText.Raw(remaining),
		caption = UiText.Resource(Res.string.assistant_card_b_required_remaining_caption),
		contentDescription = UiText.Resource(Res.string.assistant_card_b_required_remaining_description, listOf(remaining))
	)
	val required = requiredPoints?.toCardCompactNumberText()
	val requiredFigure = required?.let { value ->
		CardFigureItem(
			value = UiText.Raw(value),
			caption = UiText.Resource(Res.string.assistant_card_b_required_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_required_description, listOf(value))
		)
	}

	return RequiredGradeItem(
		id = id,
		opensSheet = opensSheet(),
		primaryFigure = requiredFigure ?: remainingFigure,
		secondaryFigure = if (required != null) remainingFigure else null,
		subjectCode = subjectCode,
		subjectName = subjectName,
		targetText = UiText.Resource(Res.string.assistant_card_b_required_target, listOf(targetGrade)),
		feasibleText = UiText.Resource(
			if (feasible) Res.string.assistant_card_a_goal_feasible else Res.string.assistant_card_a_goal_not_feasible
		),
		recordLink = entityRefs.toRecordLinkItem()
	)
}
