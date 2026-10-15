package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Créditos": the credits the pensum has approved out of its total, as one figure with its share and a bar that follows
 * it. When there is nothing to measure yet, [Content.Empty] says what the block lacks.
 */
@Immutable
data class SummaryStatisticsCreditsItem(
	val title: UiText,
	val content: Content
) {
	sealed interface Content {
		data class Empty(
			val text: UiText
		) : Content

		/**
		 * [figure] is the approved credits with what they are out of, and its share at the end. [fraction] fills the
		 * bar, and [description] is what a screen reader hears of it: the same sentence the career card says.
		 */
		data class Data(
			val figure: SummaryStatisticsFigureItem,
			val fraction: Float,
			val description: UiText
		) : Content
	}
}
