package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveTermReportUseCaseError

/**
 * What the term report screen can read at one moment: the report, the word that there is none, or the cause that keeps
 * it from being read. A cause that goes away lets the next reading deliver the report, so the observation stays alive.
 */
sealed interface TermReportObservation {
	data class Found(val report: SummaryTermReport) : TermReportObservation

	data object Missing : TermReportObservation

	data class Unavailable(val error: ObserveTermReportUseCaseError) : TermReportObservation
}
