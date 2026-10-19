package com.gdavidpb.tuindice.summary.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * The thumbs the student's votes on the term reports were confirmed with by the service, by insight id, kept on the
 * device like the reports opened: a report reopened shows the vote it had. They go with the session.
 */
interface TermReportFeedbackRepository {
	fun observeTermReportFeedbacks(): Flow<Map<String, Boolean>>

	suspend fun saveTermReportFeedback(insightId: String, useful: Boolean)
}
