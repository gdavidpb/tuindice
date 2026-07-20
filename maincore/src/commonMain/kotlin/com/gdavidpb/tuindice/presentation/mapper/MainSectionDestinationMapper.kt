package com.gdavidpb.tuindice.presentation.mapper

import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.domain.model.StartUpTarget
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination

fun MainSection.toTabRootDestination(): Destination = when (this) {
	MainSection.SUMMARY -> SummaryDestination.Summary
	MainSection.RECORD -> RecordDestination.Record
	MainSection.PENSUM -> PensumDestination.Pensum
	MainSection.EVALUATIONS -> EvaluationsDestination.Evaluations
	MainSection.ABOUT -> AboutDestination.About
}

fun Destination.toTabSectionOrNull(): MainSection? = when (this) {
	is SummaryDestination.Summary -> MainSection.SUMMARY
	is RecordDestination.Record -> MainSection.RECORD
	is PensumDestination.Pensum -> MainSection.PENSUM
	is EvaluationsDestination.Evaluations -> MainSection.EVALUATIONS
	is AboutDestination.About -> MainSection.ABOUT
	else -> null
}

fun StartUpTarget.toDestination(): Destination = when (this) {
	StartUpTarget.Auth -> AuthDestination.SignIn
	is StartUpTarget.Main -> section.toTabRootDestination()
}
