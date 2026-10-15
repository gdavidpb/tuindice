package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * "Este trimestre": the term in progress as its evaluations tell it. [termLink] is the name of the term, which leads to
 * [termTarget] (Record), and it is null while no term is in progress. The content says what the block has: the rows of
 * the evaluation types, or the invitation to what it lacks.
 */
@Immutable
data class SummaryStatisticsCurrentTermItem(
	val title: UiText,
	val termLink: UiText?,
	val termTarget: SummaryHubTarget?,
	val content: Content
) {
	sealed interface Content {
		/** No term is in progress: the invitation to enroll. */
		data class NoTerm(
			val title: UiText,
			val message: UiText
		) : Content

		/** The evaluations are not downloaded, so nothing is known of them: [action] leads to Evaluations. */
		data class NotSynced(
			val title: UiText,
			val message: UiText,
			val action: UiText,
			val target: SummaryHubTarget
		) : Content

		/** The evaluations are downloaded and the term has none. */
		data class NoEvaluations(
			val text: UiText
		) : Content

		/**
		 * The points the term has evaluated, out of its total: [figure] and the track [fraction] of it. [rows] holds
		 * one row per evaluation type, and [overdue] the evaluations that went by without a grade, if any.
		 */
		data class Data(
			val figure: SummaryStatisticsFigureItem?,
			val fraction: Float?,
			val rows: List<SummaryStatisticsEvaluationTypeItem>,
			val overdue: Overdue?
		) : Content
	}

	/**
	 * The evaluations that went by without a grade: [text] counts them in one line ("2 vencidas sin nota"), and
	 * [target] leads to the week and the evaluation of the oldest, the same line the hub shows for them, or null when
	 * the hub does not have it.
	 */
	data class Overdue(
		val text: UiText,
		val target: SummaryHubTarget.EvaluationsWeek?
	)
}
