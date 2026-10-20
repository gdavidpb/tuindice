package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.HistogramBarItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_count_subject_many
import tuindice.assistant.generated.resources.assistant_card_b_count_subject_one
import tuindice.assistant.generated.resources.assistant_card_b_histogram_grade

// The bars of a grade histogram, from the first grade up, given the number of subjects of each grade. Every bar fills
// the part of the longest one that its count is, the one fraction rule of the cards, and its sentence for a screen
// reader names the grade and its subjects («Nota 4: 16 materias»).
internal fun List<Int>.toHistogramBarItems(): List<HistogramBarItem> {
	val largest = maxOrNull()?.toDouble() ?: 0.0

	return mapIndexed { index, count ->
		val grade = (index + 1).toString()
		val subjects = chatCardCountText(
			count = count,
			one = Res.string.assistant_card_b_count_subject_one,
			many = Res.string.assistant_card_b_count_subject_many
		)

		HistogramBarItem(
			grade = grade,
			count = count.toString(),
			fraction = chatCardFraction(count.toDouble(), largest),
			description = UiText.Resource(Res.string.assistant_card_b_histogram_grade, listOf(grade, subjects))
		)
	}
}
