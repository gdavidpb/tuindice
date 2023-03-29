package com.gdavidpb.tuindice.persistence.data.tracker.source

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution

interface LocalDataSource {
	suspend fun getTransactionsQueue(uid: String): List<Transaction<*>>
	suspend fun applyResolutions(resolutions: List<Resolution>)
	suspend fun enqueueTransaction(transaction: Transaction<*>): String
}