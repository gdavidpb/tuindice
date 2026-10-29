package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.mapper.toAssistantTermInsight
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

class AssistantInsightsDataSource(
	private val api: KtorAssistantApiDataSource
) : AssistantInsightsRepository, SessionMemory {

	private val insights = MutableStateFlow<List<AssistantTermInsight>>(emptyList())

	// Held ONLY while the answer of a refresh is written, never during the request, and taken by clearSessionMemory to
	// change the epoch: a refresh answered after the session ended finds the new epoch and writes nothing.
	private val writeGate = Mutex()

	// Bumped by clearSessionMemory. A refresh remembers the one it started in.
	@Volatile
	private var sessionEpoch: Int = 0

	override fun observeInsightsFlow(): Flow<List<AssistantTermInsight>> = insights.asStateFlow()

	override suspend fun refreshInsights() {
		val epoch = sessionEpoch
		val fetched = api.getInsights().items.map { item -> item.toAssistantTermInsight() }

		writeGate.withLock {
			if (epoch == sessionEpoch) insights.value = fetched
		}
	}

	override suspend fun rateInsight(insightId: String, useful: Boolean) {
		api.postInsightFeedback(insightId = insightId, useful = useful)
	}

	// The insights are what the server said about this account, so they go with the session.
	override suspend fun clearSessionMemory() {
		writeGate.withLock {
			sessionEpoch += 1
			insights.value = emptyList()
		}
	}
}
