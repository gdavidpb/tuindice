package com.gdavidpb.tuindice.transactions.data.repository.transactions

import com.gdavidpb.tuindice.transactions.domain.model.Resolution
import com.gdavidpb.tuindice.transactions.domain.model.Transaction

interface LocalDataSource {
	suspend fun getTransactionsQueue(uid: String): List<Transaction>
	suspend fun enqueueTransaction(uid: String, transaction: Transaction): String

	suspend fun applyResolutions(uid: String, resolutions: List<Resolution>)
}