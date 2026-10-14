package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * The section "Estadísticas" of the hub, under the career, while the record is ready (it is absent otherwise). With no
 * term in progress and no term over, [Empty] says what the statistics will show and has no link. With a term in
 * progress and none over, [Invite] invites to read them when that term is over, and leads there. With terms over,
 * [Figures] shows what they have and leads to the Statistics screen.
 */
sealed interface SummaryStatisticsSectionItem {
	/** No term is in progress and none is over: the statistics have nothing to read yet, and the card says so. */
	data object Empty : SummaryStatisticsSectionItem

	/** A term is in progress and none is over: the invitation to read the statistics once it is over. */
	data object Invite : SummaryStatisticsSectionItem

	/**
	 * The terms over. [figure] is the cumulative average at the last of them that has an average, null when none has one,
	 * and [line] those averages in order, which is empty with no figure. [phrase] says how many subjects are approved and
	 * names the best regular term when there is one.
	 */
	@Immutable
	data class Figures(
		val figure: String?,
		val line: List<Float>,
		val phrase: UiText
	) : SummaryStatisticsSectionItem
}
