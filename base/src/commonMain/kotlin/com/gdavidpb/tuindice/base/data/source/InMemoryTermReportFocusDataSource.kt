package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.repository.TermReportFocusRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryTermReportFocusDataSource : TermReportFocusRepository, SessionMemory {
	private val requests = MutableStateFlow<String?>(null)

	override fun observeRequest(): Flow<String?> = requests.asStateFlow()

	override fun request(insightId: String) {
		requests.value = insightId
	}

	override fun consume(insightId: String) {
		requests.compareAndSet(expect = insightId, update = null)
	}

	// A request made in one session must not open the next account's report.
	override suspend fun clearSessionMemory() {
		requests.value = null
	}
}
