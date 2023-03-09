package com.gdavidpb.tuindice.persistence.domain.repository

import com.gdavidpb.tuindice.persistence.domain.model.Transaction

interface TrackerRepository {
	suspend fun syncPendingTransactions()

	suspend fun trackTransaction(
		transaction: Transaction,
		block: suspend () -> Unit
	)
}