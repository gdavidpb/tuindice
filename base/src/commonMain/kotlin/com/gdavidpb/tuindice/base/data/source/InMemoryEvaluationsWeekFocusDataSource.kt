package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.model.EvaluationsWeekFocusRequest
import com.gdavidpb.tuindice.base.domain.repository.EvaluationsWeekFocusRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

class InMemoryEvaluationsWeekFocusDataSource : EvaluationsWeekFocusRepository, SessionMemory {
	private val requests = MutableStateFlow<EvaluationsWeekFocusRequest?>(null)

	override val pending: StateFlow<EvaluationsWeekFocusRequest?> = requests.asStateFlow()

	override fun request(weekStart: LocalDate, evaluationId: String?) {
		requests.value = EvaluationsWeekFocusRequest(weekStart = weekStart, evaluationId = evaluationId)
	}

	override fun consume(request: EvaluationsWeekFocusRequest): Boolean {
		return requests.compareAndSet(expect = request, update = null)
	}

	// A request made in one session must not move the next account's tab.
	override suspend fun clearSessionMemory() {
		requests.value = null
	}
}
