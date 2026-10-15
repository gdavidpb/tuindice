package com.gdavidpb.tuindice.summary.ui.model

import androidx.compose.runtime.Stable
import com.gdavidpb.tuindice.base.ui.view.RichTooltipText

/**
 * The head of a block of the Statistics screen: the [tag] that names the block, its [title], and the [linkText] of the
 * link that leads from it, which [onLinkClick] takes the student through, when the block has a link. [info] is the
 * explanation that the mark beside the title opens, when the block has one.
 */
@Stable
class SummaryStatisticsBlockHead(
	val tag: String,
	val title: String,
	val linkText: String? = null,
	val onLinkClick: () -> Unit = {},
	val info: RichTooltipText? = null
)
