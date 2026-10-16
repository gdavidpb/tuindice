package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Tus notas": one bar per grade from 1 to 5, counting the attempts that carry it. [info] explains what the bars count,
 * and it sits beside the title when the block has data. [facts] hold the attempts whose grade the university never
 * reported, as a compact figure, when there are any.
 */
@Immutable
data class SummaryStatisticsGradesItem(
	val title: UiText,
	val content: Content,
	val info: UiText? = null
) {
	sealed interface Content {
		data class Empty(
			val text: UiText
		) : Content

		data class Data(
			val bars: List<SummaryStatisticsBarItem>,
			val facts: List<SummaryStatisticsFactItem>
		) : Content
	}
}
