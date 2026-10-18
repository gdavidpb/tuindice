package com.gdavidpb.tuindice.summary.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * The term reports the student has opened, by insight id, kept on the device: a report opened once stops offering its line
 * in the summary. The ids are the account's, so they go with the session like the other things kept on the device.
 */
interface TermReportSeenRepository {
	fun observeSeenTermReportIds(): Flow<Set<String>>

	suspend fun markTermReportSeen(insightId: String)
}
