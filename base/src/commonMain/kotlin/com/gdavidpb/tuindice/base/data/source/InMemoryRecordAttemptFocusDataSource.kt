package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.model.RecordAttemptFocusRequest
import com.gdavidpb.tuindice.base.domain.repository.RecordAttemptFocusRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryRecordAttemptFocusDataSource : RecordAttemptFocusRepository, SessionMemory {
	private val requests = MutableStateFlow<RecordAttemptFocusRequest?>(null)

	override val pending: StateFlow<RecordAttemptFocusRequest?> = requests.asStateFlow()

	override fun request(attemptId: String) {
		requests.value = RecordAttemptFocusRequest(attemptId = attemptId)
	}

	override fun consume(request: RecordAttemptFocusRequest): Boolean {
		return requests.compareAndSet(expect = request, update = null)
	}

	// A request made in one session must not open the next account's record on that attempt.
	override suspend fun clearSessionMemory() {
		requests.value = null
	}
}
