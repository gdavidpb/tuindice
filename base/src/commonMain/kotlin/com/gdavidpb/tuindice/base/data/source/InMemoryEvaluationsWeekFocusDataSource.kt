package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.repository.EvaluationsWeekFocusRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

class InMemoryEvaluationsWeekFocusDataSource : EvaluationsWeekFocusRepository, SessionMemory {
	private val pending = MutableStateFlow<LocalDate?>(null)

	override val pendingWeekStart: StateFlow<LocalDate?> = pending.asStateFlow()

	override fun request(weekStart: LocalDate) {
		pending.value = weekStart
	}

	override fun consume(weekStart: LocalDate): Boolean {
		return pending.compareAndSet(expect = weekStart, update = null)
	}

	// A request made in one session must not move the next account's tab.
	override suspend fun clearSessionMemory() {
		pending.value = null
	}
}
