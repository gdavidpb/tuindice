package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.domain.model.StartUpTarget
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination

fun Destination.toMainSectionOrNull(): MainSection? = when (this) {
	is SummaryDestination.NavGraph -> MainSection.SUMMARY
	is RecordDestination.NavGraph -> MainSection.RECORD
	is EvaluationsDestination.NavGraph -> MainSection.EVALUATIONS
	is AboutDestination.NavGraph -> MainSection.ABOUT
	else -> null
}

fun MainSection.toDestination(): Destination = when (this) {
	MainSection.SUMMARY -> SummaryDestination.NavGraph
	MainSection.RECORD -> RecordDestination.NavGraph
	MainSection.EVALUATIONS -> EvaluationsDestination.NavGraph
	MainSection.ABOUT -> AboutDestination.NavGraph
}

fun StartUpTarget.toDestination(): Destination = when (this) {
	StartUpTarget.Auth -> AuthDestination.NavGraph
	is StartUpTarget.Main -> section.toDestination()
}
