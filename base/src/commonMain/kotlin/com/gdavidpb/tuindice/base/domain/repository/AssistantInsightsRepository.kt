package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import kotlinx.coroutines.flow.Flow

interface AssistantInsightsRepository {
	/** The term reports the service said about this account, most recent first. */
	fun observeInsightsFlow(): Flow<List<AssistantTermInsight>>

	/** Reads the term reports again from the service. A failure leaves the ones already read as they are. */
	suspend fun refreshInsights()

	/** Sends the student's thumbs on a term report: [useful] is the thumb the student chose. */
	suspend fun rateInsight(insightId: String, useful: Boolean)
}
