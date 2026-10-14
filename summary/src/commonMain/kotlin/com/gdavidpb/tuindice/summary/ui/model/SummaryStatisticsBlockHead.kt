package com.gdavidpb.tuindice.summary.ui.model

import androidx.compose.runtime.Stable

/**
 * The head of a block of the Statistics screen: the [tag] that names the block, its [title], and the [linkText] of the
 * link that leads from it, which [onLinkClick] takes the student through, when the block has a link.
 */
@Stable
class SummaryStatisticsBlockHead(
	val tag: String,
	val title: String,
	val linkText: String? = null,
	val onLinkClick: () -> Unit = {}
)
