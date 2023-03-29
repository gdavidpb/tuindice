package com.gdavidpb.tuindice.persistence.domain.repository

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface TrackerRepository {
	suspend fun syncPendingTransactions(uid: String)

	suspend fun trackTransaction(
		transaction: Transaction<*>,
		block: suspend () -> Unit
	)
}