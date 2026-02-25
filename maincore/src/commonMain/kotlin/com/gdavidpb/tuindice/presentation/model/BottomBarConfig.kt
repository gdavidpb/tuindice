package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination

sealed class BottomBarConfig(
	val destination: Destination
) {
	data object Summary : BottomBarConfig(
		destination = SummaryDestination.NavGraph
	)

	data object Record : BottomBarConfig(
		destination = RecordDestination.NavGraph
	)

	data object Evaluations : BottomBarConfig(
		destination = EvaluationsDestination.NavGraph
	)

	data object About : BottomBarConfig(
		destination = AboutDestination.NavGraph
	)
}
