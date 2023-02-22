package com.gdavidpb.tuindice.persistence.data.tracker.source

import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionResult

interface RemoteDataSource {
	suspend fun sync(transactions: List<Transaction>): List<TransactionResult>
}