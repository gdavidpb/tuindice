package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAcademicCalendar
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationListItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationStatusItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationWeekKey
import com.gdavidpb.tuindice.assistant.presentation.model.PointsRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.RequiredGradeItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectPointsItem
import com.gdavidpb.tuindice.assistant.presentation.model.WeekLoadRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.WeeklyLoadItem
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatCompactGrade
import com.gdavidpb.tuindice.base.utils.toLocalDate
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationRow
import com.gdavidpb.tuindice.chatcontract.component.RequiredGradeComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsComponent
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent
import kotlinx.datetime.LocalDate
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_goal_feasible
import tuindice.assistant.generated.resources.assistant_card_a_goal_not_feasible
import tuindice.assistant.generated.resources.assistant_card_b_chip_heavy
import tuindice.assistant.generated.resources.assistant_card_b_count_evaluation_many
import tuindice.assistant.generated.resources.assistant_card_b_count_evaluation_one
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_no_date
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_no_grade
import tuindice.assistant.generated.resources.assistant_card_b_evaluation_score
import tuindice.assistant.generated.resources.assistant_card_b_fraction
import tuindice.assistant.generated.resources.assistant_card_b_joined
import tuindice.assistant.generated.resources.assistant_card_b_load_caption_many
import tuindice.assistant.generated.resources.assistant_card_b_load_caption_one
import tuindice.assistant.generated.resources.assistant_card_b_load_description
import tuindice.assistant.generated.resources.assistant_card_b_points_description
import tuindice.assistant.generated.resources.assistant_card_b_required_caption_many
import tuindice.assistant.generated.resources.assistant_card_b_required_caption_one
import tuindice.assistant.generated.resources.assistant_card_b_required_description
import tuindice.assistant.generated.resources.assistant_card_b_required_remaining_caption
import tuindice.assistant.generated.resources.assistant_card_b_required_remaining_description
import tuindice.assistant.generated.resources.assistant_card_b_required_target
import tuindice.assistant.generated.resources.assistant_card_b_see_all_evaluations
import tuindice.assistant.generated.resources.assistant_card_b_see_all_subjects
import tuindice.assistant.generated.resources.assistant_card_b_week

internal fun EvaluationListComponent.toEvaluationListItem(weeks: EvaluationWeeksContext? = null): EvaluationListItem {
	val rows = items.map { row -> row.toEvaluationRowItem(calendar = weeks?.calendar) }

	return EvaluationListItem(
		id = id,
		opensSheet = opensSheet(),
		rows = rows,
		recordLink = entityRefs.toRecordLinkItem(),
		seeAllText = UiText.Resource(Res.string.assistant_card_b_see_all_evaluations, listOf(items.size)),
		weeks = weeks?.let { context -> evaluationWeeks(rows = rows, context = context) }
	)
}

// One evaluation of the list, written as the Evaluations tab writes it: its date with the day of the week, or the
// continuous label when it has no date, and its grade with the tab's format.
private fun EvaluationRow.toEvaluationRowItem(calendar: AssistantAcademicCalendar?): EvaluationRowItem {
	val shortDate = dateMillis?.formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE)
	val date = dateMillis?.toLocalDate()
	val maxGradeText = maxGrade.formatCompactGrade()

	return EvaluationRowItem(
		code = subjectCode,
		title = title,
		date = date,
		dateText = dateMillis?.let { millis ->
			UiText.Capitalized(millis.formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE))
		} ?: UiText.Resource(Res.string.assistant_card_b_evaluation_no_date),
		nameText = if (shortDate == null) {
			UiText.Raw(title)
		} else {
			UiText.Resource(Res.string.assistant_card_b_joined, listOf(title, shortDate))
		},
		gradeText = grade?.let { value ->
			UiText.Resource(
				Res.string.assistant_card_b_evaluation_score,
				listOf(value.formatCompactGrade(), maxGradeText)
			)
		} ?: UiText.Resource(Res.string.assistant_card_b_evaluation_no_grade, listOf(maxGradeText)),
		status = evaluationStateChip(state)?.let { chip ->
			EvaluationStatusItem(text = chip.text, tone = evaluationStateTone(state))
		},
		weekKey = weekKeyIn(calendar = calendar, date = date)
	)
}

// The week a row falls in, as the Evaluations tab files it: a continuous evaluation, or one with no date, holds no
// week; any other one holds the week of its date, within the weeks of the term.
private fun EvaluationRow.weekKeyIn(calendar: AssistantAcademicCalendar?, date: LocalDate?): EvaluationWeekKey? {
	return when {
		calendar == null -> null
		scheduleMode == CONTINUOUS_SCHEDULE_MODE || date == null -> EvaluationWeekKey.Continuous
		else -> EvaluationWeekKey.Academic(calendar.weekOf(date))
	}
}

// The serial name the chat carries for a continuous evaluation, as the academic core names its schedule mode.
private const val CONTINUOUS_SCHEDULE_MODE = "continuous"

internal fun SubjectPointsComponent.toSubjectPointsItem(): SubjectPointsItem {
	return SubjectPointsItem(
		id = id,
		opensSheet = opensSheet(),
		rows = rows.map { row ->
			val earned = row.earned.toCardCompactNumberText()
			val evaluated = row.evaluated.toCardCompactNumberText()

			PointsRowItem(
				code = row.subjectCode,
				name = row.subjectName,
				pointsText = UiText.Resource(Res.string.assistant_card_b_fraction, listOf(earned, evaluated)),
				earnedFraction = chatCardFraction(row.earned, row.total),
				evaluatedFraction = chatCardFraction(row.evaluated, row.total),
				description = UiText.Resource(
					Res.string.assistant_card_b_points_description,
					listOf(earned, row.total.toCardCompactNumberText(), evaluated)
				)
			)
		},
		recordLink = entityRefs.toRecordLinkItem(),
		seeAllText = UiText.Resource(Res.string.assistant_card_b_see_all_subjects, listOf(rows.size))
	)
}

internal fun WeeklyLoadComponent.toWeeklyLoadItem(): WeeklyLoadItem {
	val heavyWeeks = weeks.count { week -> week.heavy }
	val heavy = UiText.Raw(heavyWeeks.toString())

	return WeeklyLoadItem(
		id = id,
		opensSheet = opensSheet(),
		heavyFigure = CardFigureItem(
			value = heavy,
			caption = chatCardNounText(
				count = heavyWeeks,
				one = Res.string.assistant_card_b_load_caption_one,
				many = Res.string.assistant_card_b_load_caption_many
			),
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
				chip = if (week.heavy) chatCardChip(Res.string.assistant_card_b_chip_heavy, CardChipTone.Warning) else null
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
	val requiredFigure = requiredPoints?.let { points ->
		val value = UiText.Raw(points.toCardCompactNumberText())

		CardFigureItem(
			value = value,
			caption = chatCardPointsNounText(
				points = points,
				one = Res.string.assistant_card_b_required_caption_one,
				many = Res.string.assistant_card_b_required_caption_many
			),
			contentDescription = UiText.Resource(Res.string.assistant_card_b_required_description, listOf(value))
		)
	}

	return RequiredGradeItem(
		id = id,
		opensSheet = opensSheet(),
		primaryFigure = requiredFigure ?: remainingFigure,
		secondaryFigure = if (requiredPoints != null) remainingFigure else null,
		subjectCode = subjectCode,
		subjectName = subjectName,
		targetText = UiText.Resource(Res.string.assistant_card_b_required_target, listOf(targetGrade)),
		feasibleChip = if (feasible) {
			chatCardChip(Res.string.assistant_card_a_goal_feasible, CardChipTone.Success)
		} else {
			chatCardChip(Res.string.assistant_card_a_goal_not_feasible, CardChipTone.Failed)
		},
		recordLink = entityRefs.toRecordLinkItem()
	)
}
