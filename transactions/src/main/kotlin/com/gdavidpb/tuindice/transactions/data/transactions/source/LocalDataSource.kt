package com.gdavidpb.tuindice.transactions.data.transactions.source

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface LocalDataSource {
	suspend fun getTransactionsQueue(uid: String): List<Transaction<*>>
	suspend fun applyResolutions(resolutions: List<Resolution>)
	suspend fun enqueueTransaction(transaction: Transaction<*>): String
}