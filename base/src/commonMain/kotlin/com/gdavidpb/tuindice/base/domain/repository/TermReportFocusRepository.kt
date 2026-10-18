package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * A request, kept as state, to open a term report: a tap on its push writes it, and the host opens the report once the
 * student is in the app and takes the request. It is state so that a tap which arrives before the app shows its content
 * is not lost.
 */
interface TermReportFocusRepository {
	/** The insight of the report asked for and not taken yet, or null when no request is pending. */
	fun observeRequest(): Flow<String?>

	/** Asks for the report of [insightId]; a request not taken yet is replaced. */
	fun request(insightId: String)

	/** Takes the request for [insightId] when it is still the pending one. */
	fun consume(insightId: String)
}
