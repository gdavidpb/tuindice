package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.base.data.source.SessionInvalidationDataSource
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository

// The real signal, counted: how many times it was raised, not how many collectors saw it.
internal class CountingSessionInvalidationRepository(
	private val delegate: SessionInvalidationRepository = SessionInvalidationDataSource()
) : SessionInvalidationRepository by delegate {
	var raised = 0
		private set

	override fun notifySessionInvalidated(sessionId: String?): Boolean {
		val result = delegate.notifySessionInvalidated(sessionId)

		if (result) raised++

		return result
	}
}
