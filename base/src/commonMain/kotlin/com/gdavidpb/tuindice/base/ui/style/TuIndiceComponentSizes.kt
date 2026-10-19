package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.ui.unit.dp

object TuIndiceComponentSizes {
	val IconXSmall = 12.dp
	val IconSmall = 16.dp
	val IconMedium = 20.dp
	val Icon = 24.dp
	val IconButton = 48.dp
	val TopBarHeight = 56.dp
	val BottomBarHeight = 56.dp

	// The round avatar of the student: the small one sits in the 48 dp touch target of the top bar, the large one heads
	// the Settings card.
	val AvatarSmall = 32.dp
	val AvatarLarge = 64.dp

	/**
	 * The height of the proportion bar that TuIndiceProgressBar draws, which the summary and its statistics use.
	 * It is 6 dp because the bar of the career and the week track of the term already had that height, the two
	 * bars of the summary that agreed on one; the stacked bar of the statistics was drawn at 24 dp, the thickest of them.
	 */
	val ProgressBarHeight = 6.dp
}
