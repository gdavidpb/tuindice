package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.EvaluationsWeekFocusRequest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

/**
 * A request, kept as state, to show a week of the Evaluations tab and, when it names one, to bring an
 * evaluation of that week into view: whoever asks (the host, when a screen sends the user there)
 * writes it, and the Evaluations machine applies it once its content is ready and consumes it. It is
 * state and not an event so it survives the tab not being composed yet, or its content not having
 * loaded, at the moment of the request.
 */
interface EvaluationsWeekFocusRepository {
	/** The request not yet consumed, or null when nothing is pending. */
	val pending: StateFlow<EvaluationsWeekFocusRequest?>

	/**
	 * Asks for the week that [weekStart] (a Monday) opens and, when given, for [evaluationId] inside it;
	 * a request not yet consumed is replaced.
	 */
	fun request(weekStart: LocalDate, evaluationId: String? = null)

	/**
	 * Takes the pending request when it is still [request]. True for the caller that took it, so a
	 * request is applied once however many times it is seen; false when it was already taken or a
	 * newer one replaced it.
	 */
	fun consume(request: EvaluationsWeekFocusRequest): Boolean
}
