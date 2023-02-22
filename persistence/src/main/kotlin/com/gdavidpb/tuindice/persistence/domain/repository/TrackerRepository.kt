package com.gdavidpb.tuindice.persistence.domain.repository

import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionAction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionType

interface TrackerRepository {
	suspend fun getPendingTransactions(): List<Transaction>

	suspend fun trackTransaction(
		reference: String,
		type: TransactionType,
		action: TransactionAction,
		block: suspend () -> Unit
	)
}