package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.base.presentation.navigation.Destination2
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination

fun Destination2.toDestinationName(): String = when (this) {
	is RecordDestination.NavGraph -> "record"
	is EvaluationsDestination.NavGraph -> "evaluations"
	is AboutDestination.NavGraph -> "about"
	else -> throw IllegalArgumentException()
}

fun String.toDestination(): Destination2 = when (this) {
	"record" -> RecordDestination.NavGraph
	"evaluations" -> EvaluationsDestination.NavGraph
	"about" -> AboutDestination.NavGraph
	else -> throw IllegalArgumentException()
}