package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationWeekKey
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationWeeksItem
import com.gdavidpb.tuindice.base.presentation.mapper.toShortNameText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.model.WeekStripDayItem
import com.gdavidpb.tuindice.base.ui.model.WeekStripItem
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_evaluations_continuous
import tuindice.assistant.generated.resources.assistant_card_b_week

// The weeks of a sheet, laid out as the Evaluations tab lays its own out: the continuous week first, when a row holds
// no week, and then every week of the term. The sheet opens on the week that holds today when that week has rows;
// otherwise on the nearest week after today that has some, and otherwise on the latest week before today that has some.
internal fun evaluationWeeks(rows: List<EvaluationRowItem>, context: EvaluationWeeksContext): EvaluationWeeksItem {
	val weeksWithRows = rows.mapNotNull { row -> row.weekKey }.toSet()
	// The days that get a dot are the dates of the rows that fall in a week, as the Evaluations tab marks them.
	val evaluationDates = rows
		.filter { row -> row.weekKey is EvaluationWeekKey.Academic }
		.mapNotNull { row -> row.date }
		.toSet()

	val strip = buildList {
		if (EvaluationWeekKey.Continuous in weeksWithRows) {
			add(continuousWeek())
		}

		context.calendar.weeks.forEach { weekNumber ->
			add(academicWeek(weekNumber = weekNumber, context = context, evaluationDates = evaluationDates))
		}
	}

	return EvaluationWeeksItem(
		strip = strip,
		initialKey = initialKeyOf(strip = strip, weeksWithRows = weeksWithRows, todayWeek = todayWeekOf(context))
	)
}

// The continuous evaluations hold no week, so their group has no days and is never the week of today.
private fun continuousWeek(): WeekStripItem<EvaluationWeekKey> {
	val key = EvaluationWeekKey.Continuous

	return WeekStripItem(
		key = key,
		tagSuffix = key.tagSuffix,
		labelText = UiText.Resource(Res.string.assistant_card_b_evaluations_continuous),
		days = emptyList(),
		isCurrent = false
	)
}

// One week of the term: the seven days from its Monday, with the day of today selected and the days that hold an
// evaluation marked. The week is the current one when one of its days is today.
private fun academicWeek(
	weekNumber: Int,
	context: EvaluationWeeksContext,
	evaluationDates: Set<LocalDate>
): WeekStripItem<EvaluationWeekKey> {
	val key = EvaluationWeekKey.Academic(weekNumber)
	val monday = context.calendar.weekOneStart.plus(DatePeriod(days = (weekNumber - 1) * DAYS_PER_WEEK))
	val days = (0 until DAYS_PER_WEEK).map { offset ->
		val date = monday.plus(DatePeriod(days = offset))

		WeekStripDayItem(
			weekdayText = UiText.Uppercase(date.dayOfWeek.toShortNameText()),
			dayText = date.day.toString(),
			isSelected = date == context.today,
			hasEvaluations = date in evaluationDates
		)
	}

	return WeekStripItem(
		key = key,
		tagSuffix = key.tagSuffix,
		labelText = UiText.Resource(Res.string.assistant_card_b_week, listOf(weekNumber)),
		days = days,
		isCurrent = days.any { day -> day.isSelected }
	)
}

// The sheet opens on the week that holds today when that week has rows. Otherwise it opens on the nearest week
// after today that has rows, and otherwise on the latest week before today that has some. The continuous week is
// the choice only when no row is dated. A sheet with no row at all opens on the week of today, or on the first week
// of the strip.
private fun initialKeyOf(
	strip: List<WeekStripItem<EvaluationWeekKey>>,
	weeksWithRows: Set<EvaluationWeekKey>,
	todayWeek: Int
): EvaluationWeekKey {
	val datedWeeks = strip.map { week -> week.key }
		.filterIsInstance<EvaluationWeekKey.Academic>()
		.filter { key -> key in weeksWithRows }

	return datedWeeks.firstOrNull { key -> key.weekNumber == todayWeek }
		?: datedWeeks.filter { key -> key.weekNumber > todayWeek }.minByOrNull { key -> key.weekNumber }
		?: datedWeeks.filter { key -> key.weekNumber < todayWeek }.maxByOrNull { key -> key.weekNumber }
		?: EvaluationWeekKey.Continuous.takeIf { key -> key in weeksWithRows }
		?: strip.firstOrNull { week -> week.isCurrent }?.key
		?: strip.first().key
}

// The number the strip gives the week that holds today: below one before the term, and past the last week after it.
private fun todayWeekOf(context: EvaluationWeeksContext): Int {
	val daysIntoTerm = (context.today.toEpochDays() - context.calendar.weekOneStart.toEpochDays()).toInt()

	return daysIntoTerm.floorDiv(DAYS_PER_WEEK) + 1
}

private const val DAYS_PER_WEEK = 7
