package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Tus notas": one bar per grade from 1 to 5, counting the attempts that carry it. [notes] are the lines under the
 * bars, and [unreported] counts the attempts whose grade the university never reported.
 */
@Immutable
data class SummaryStatisticsGradesItem(
	val title: UiText,
	val content: Content
) {
	sealed interface Content {
		data class Empty(
			val text: UiText
		) : Content

		data class Data(
			val bars: List<SummaryStatisticsBarItem>,
			val notes: List<UiText>,
			val unreported: UiText?
		) : Content
	}
}
