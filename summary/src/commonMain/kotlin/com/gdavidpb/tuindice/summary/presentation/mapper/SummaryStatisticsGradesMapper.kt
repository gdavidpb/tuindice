package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.GradeHistogram
import com.gdavidpb.tuindice.academiccore.domain.model.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsBarItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsGradesItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_grades_empty
import tuindice.summary.generated.resources.summary_statistics_grades_info
import tuindice.summary.generated.resources.summary_statistics_grades_share_many
import tuindice.summary.generated.resources.summary_statistics_grades_share_one
import tuindice.summary.generated.resources.summary_statistics_grades_title
import tuindice.summary.generated.resources.summary_statistics_grades_tooltip_many
import tuindice.summary.generated.resources.summary_statistics_grades_tooltip_one
import tuindice.summary.generated.resources.summary_statistics_grades_unreported

private const val LOWEST_GRADE = 1

// "Tus notas": one bar per grade, counting the attempts that carry it, repeated subjects included. The attempts whose
// grade the university never reported are a compact figure of their own, and the info says what the bars count.
internal fun gradesItemOf(histogram: GradeHistogram): SummaryStatisticsGradesItem {
	val title = UiText.Resource(Res.string.summary_statistics_grades_title)
	val graded = histogram.countsByGrade.values.sum()

	if (graded + histogram.unreported == 0) {
		return SummaryStatisticsGradesItem(
			title = title,
			content = SummaryStatisticsGradesItem.Content.Empty(
				text = UiText.Resource(Res.string.summary_statistics_grades_empty)
			)
		)
	}

	return SummaryStatisticsGradesItem(
		title = title,
		content = SummaryStatisticsGradesItem.Content.Data(
			bars = (LOWEST_GRADE..MAX_SUBJECT_GRADE).map { grade ->
				gradeBarOf(grade = grade, count = histogram.countsByGrade[grade] ?: 0, graded = graded)
			},
			facts = factsOf(count = histogram.unreported)
		),
		info = UiText.Resource(Res.string.summary_statistics_grades_info)
	)
}

private fun gradeBarOf(grade: Int, count: Int, graded: Int): SummaryStatisticsBarItem {
	return SummaryStatisticsBarItem(
		axisLabel = UiText.Raw(grade.toString()),
		valueLabel = UiText.Raw(count.toString()),
		value = count.toFloat(),
		tooltip = SummaryStatisticsTooltipItem(
			title = countText(
				quantity = count,
				one = Res.string.summary_statistics_grades_tooltip_one,
				many = Res.string.summary_statistics_grades_tooltip_many,
				args = listOf(grade, count)
			),
			// With no grade at all there is no share to tell, so the title stands alone.
			lines = if (graded > 0) listOf(shareLineOf(count = count, graded = graded)) else emptyList()
		)
	)
}

// "46 % de tus 46 notas", or "100 % de tu nota" when there is a single grade to be out of.
private fun shareLineOf(count: Int, graded: Int): UiText {
	val share = shareOf(part = count, whole = graded)

	return if (graded == 1) {
		UiText.Resource(Res.string.summary_statistics_grades_share_one, listOf(share))
	} else {
		UiText.Resource(Res.string.summary_statistics_grades_share_many, listOf(share, graded))
	}
}

// "3 sin reportar": the attempts whose grade the record never reported, as a compact figure, and none when there are
// none.
private fun factsOf(count: Int): List<SummaryStatisticsFactItem> {
	if (count == 0) return emptyList()

	return listOf(
		SummaryStatisticsFactItem(
			value = count.toString(),
			label = UiText.Resource(Res.string.summary_statistics_grades_unreported)
		)
	)
}
