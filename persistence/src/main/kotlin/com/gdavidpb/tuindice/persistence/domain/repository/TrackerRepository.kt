package com.gdavidpb.tuindice.persistence.domain.repository

import com.gdavidpb.tuindice.persistence.domain.model.TransactionAction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionData
import com.gdavidpb.tuindice.persistence.domain.model.TransactionType

interface TrackerRepository {
	suspend fun syncPendingTransactions()

	suspend fun trackTransaction(
		reference: String,
		type: TransactionType,
		action: TransactionAction,
		data: TransactionData? = null,
		remote: suspend () -> Unit
	)
}