package com.gdavidpb.tuindice.persistence.domain.repository

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface TrackerRepository {
	suspend fun syncPendingTransactions()

	suspend fun trackTransaction(
		transaction: Transaction,
		block: suspend () -> Unit
	)
}