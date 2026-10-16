package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * What a "Mi trimestre" card says about one closed term: the [term], its [index] (the term's own average), the subjects
 * and the credits approved in it, the [cumulative] average and the [career] the student is in. It never carries the
 * student's name or code.
 */
@Immutable
data class ShareableTermCardItem(
	val term: UiText,
	val index: String,
	val approved: SummaryStatisticsFigureItem,
	val credits: SummaryStatisticsFigureItem,
	val cumulative: SummaryStatisticsFigureItem,
	val career: UiText
)
