package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination

fun Destination.toDestinationName(): String = when (this) {
	is SummaryDestination.NavGraph -> "summary"
	is RecordDestination.NavGraph -> "record"
	is EvaluationsDestination.NavGraph -> "evaluations"
	is AboutDestination.NavGraph -> "about"
	else -> throw IllegalArgumentException()
}

fun String.toDestination(): Destination = when (this) {
	"summary" -> SummaryDestination.NavGraph
	"record" -> RecordDestination.NavGraph
	"evaluations" -> EvaluationsDestination.NavGraph
	"about" -> AboutDestination.NavGraph
	else -> throw IllegalArgumentException()
}