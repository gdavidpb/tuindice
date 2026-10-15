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

	/**
	 * The height of every progress bar of the app, the one TuIndiceProgressBar draws. It is 6 dp because the bar of the
	 * career and the week track of the term already had that height, the two bars of the summary that agreed on one; the
	 * stacked bar of the statistics was drawn at 24 dp, the thickest of them.
	 */
	val ProgressBarHeight = 6.dp
}
