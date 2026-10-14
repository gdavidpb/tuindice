package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Ritmo": the approved credits of each closed term as a bar, with the average of the regular ones as a reference line.
 * The July - August term is drawn but leaves the average out. [Projection] says how many regular terms the pensum still
 * takes at that pace, or why it cannot say yet.
 */
@Immutable
data class SummaryStatisticsPaceItem(
	val title: UiText,
	val content: Content
) {
	sealed interface Content {
		data class Empty(
			val text: UiText
		) : Content

		data class Data(
			val figure: SummaryStatisticsFigureItem,
			val bars: List<SummaryStatisticsBarItem>,
			val reference: SummaryStatisticsReferenceItem,
			val projection: Projection
		) : Content
	}

	sealed interface Projection {
		/** The projection itself, marked with [tag] as a projection. */
		data class Remaining(
			val text: UiText,
			val tag: UiText
		) : Projection

		/** Too few regular terms have closed for the pace to be projected yet. */
		data class Pending(
			val text: UiText
		) : Projection

		/** The pensum is complete: there is no term left to project. */
		data class Complete(
			val text: UiText
		) : Projection

		/** No pensum is cached, so the pace cannot be projected: [title] and [message] invite to open it. */
		data class NoPensum(
			val title: UiText,
			val message: UiText
		) : Projection

		/** Nothing to say: the pace cannot be projected from the credits approved so far. */
		data object None : Projection
	}
}
