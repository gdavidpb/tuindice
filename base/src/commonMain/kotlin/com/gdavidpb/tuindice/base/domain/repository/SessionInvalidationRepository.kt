package com.gdavidpb.tuindice.base.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionInvalidationRepository {
	fun observeSessionInvalidation(): Flow<Unit>
	fun notifySessionInvalidated()
}
