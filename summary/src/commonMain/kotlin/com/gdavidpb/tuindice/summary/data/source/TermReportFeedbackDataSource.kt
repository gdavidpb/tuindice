package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.summary.domain.repository.TermReportFeedbackRepository
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// The votes on the term reports the service confirmed, kept by insight id. Each one is stored as `<insightId>=1`
// (useful) or `<insightId>=0`, between commas: the value follows the last `=`. The votes go with the session.
class TermReportFeedbackDataSource(
	private val settings: Settings
) : TermReportFeedbackRepository, SessionMemory {
	private val termReportFeedbacks = MutableStateFlow(readTermReportFeedbacks())

	override fun observeTermReportFeedbacks(): Flow<Map<String, Boolean>> {
		return termReportFeedbacks
	}

	override suspend fun saveTermReportFeedback(insightId: String, useful: Boolean) {
		val feedbacks = termReportFeedbacks.value + (insightId to useful)

		settings.putString(PreferencesKeys.TERM_REPORT_FEEDBACKS, feedbacks.toStoredFeedbacks())
		termReportFeedbacks.value = feedbacks
	}

	// The mirror goes back to what is stored, which the wipe has just emptied.
	override suspend fun clearSessionMemory() {
		termReportFeedbacks.value = readTermReportFeedbacks()
	}

	private fun readTermReportFeedbacks(): Map<String, Boolean> {
		return settings.getStringOrNull(PreferencesKeys.TERM_REPORT_FEEDBACKS)
			?.split(ENTRIES_SEPARATOR)
			?.filter { entry -> entry.contains(VALUE_SEPARATOR) }
			?.associate { entry ->
				val insightId = entry.substringBeforeLast(VALUE_SEPARATOR)
				val useful = entry.substringAfterLast(VALUE_SEPARATOR) == USEFUL

				insightId to useful
			}
			.orEmpty()
	}

	private fun Map<String, Boolean>.toStoredFeedbacks(): String {
		return entries.joinToString(separator = ENTRIES_SEPARATOR) { (insightId, useful) ->
			"$insightId$VALUE_SEPARATOR${if (useful) USEFUL else NOT_USEFUL}"
		}
	}

	private companion object {
		// Insight ids are `<termId>:<sequence>`: a comma never appears in one.
		const val ENTRIES_SEPARATOR = ","
		const val VALUE_SEPARATOR = "="
		const val USEFUL = "1"
		const val NOT_USEFUL = "0"
	}
}
