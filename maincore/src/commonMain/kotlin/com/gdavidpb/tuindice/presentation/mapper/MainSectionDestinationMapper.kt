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

fun Destination.toMainSectionOrNull(): MainSection? = when (this) {
	is SummaryDestination.NavGraph -> MainSection.SUMMARY
	is RecordDestination.NavGraph -> MainSection.RECORD
	is PensumDestination.NavGraph -> MainSection.PENSUM
	is EvaluationsDestination.NavGraph -> MainSection.EVALUATIONS
	is AboutDestination.NavGraph -> MainSection.ABOUT
	else -> null
}

fun MainSection.toDestination(): Destination = when (this) {
	MainSection.SUMMARY -> SummaryDestination.NavGraph
	MainSection.RECORD -> RecordDestination.NavGraph
	MainSection.PENSUM -> PensumDestination.NavGraph
	MainSection.EVALUATIONS -> EvaluationsDestination.NavGraph
	MainSection.ABOUT -> AboutDestination.NavGraph
}

fun StartUpTarget.toDestination(): Destination = when (this) {
	StartUpTarget.Auth -> AuthDestination.NavGraph
	is StartUpTarget.Main -> section.toDestination()
}

fun MainSection.toTabRootDestination(): Destination = when (this) {
	MainSection.SUMMARY -> SummaryDestination.Summary
	MainSection.RECORD -> RecordDestination.Record
	MainSection.PENSUM -> PensumDestination.Pensum
	MainSection.EVALUATIONS -> EvaluationsDestination.Evaluations
	MainSection.ABOUT -> AboutDestination.About
}

fun Destination.toTabSectionOrNull(): MainSection? = when (this) {
	is SummaryDestination.Summary, is SummaryDestination.NavGraph -> MainSection.SUMMARY
	is RecordDestination.Record, is RecordDestination.NavGraph -> MainSection.RECORD
	is PensumDestination.Pensum, is PensumDestination.NavGraph -> MainSection.PENSUM
	is EvaluationsDestination.Evaluations, is EvaluationsDestination.NavGraph -> MainSection.EVALUATIONS
	is AboutDestination.About, is AboutDestination.NavGraph -> MainSection.ABOUT
	else -> null
}
