package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.mapper.toAssistantTermInsight
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AssistantInsightsDataSource(
	private val api: KtorAssistantApiDataSource
) : AssistantInsightsRepository, SessionMemory {

	private val insights = MutableStateFlow<List<AssistantTermInsight>>(emptyList())

	override fun observeInsightsFlow(): Flow<List<AssistantTermInsight>> = insights.asStateFlow()

	override suspend fun refreshInsights() {
		insights.value = api.getInsights().items.map { item -> item.toAssistantTermInsight() }
	}

	override suspend fun rateInsight(insightId: String, useful: Boolean) {
		api.postInsightFeedback(insightId = insightId, useful = useful)
	}

	// The insights are what the server said about this account, so they go with the session.
	override suspend fun clearSessionMemory() {
		insights.value = emptyList()
	}
}
