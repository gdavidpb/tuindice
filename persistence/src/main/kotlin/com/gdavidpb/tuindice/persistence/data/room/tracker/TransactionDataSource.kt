package com.gdavidpb.tuindice.persistence.data.room.tracker

import com.gdavidpb.tuindice.persistence.data.room.model.TransactionAction
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionType

interface TransactionDataSource {
	suspend fun trackTransaction(
		reference: String,
		type: TransactionType,
		action: TransactionAction,
		transaction: suspend () -> Unit
	)
}