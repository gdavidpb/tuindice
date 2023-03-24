package com.gdavidpb.tuindice.persistence.data.tracker.source

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution

interface LocalDataSource {
	suspend fun applyResolutions(resolutions: List<Resolution>)
	suspend fun getTransactionsQueue(): List<Transaction<*>>
	suspend fun enqueueTransaction(transaction: Transaction<*>): String
}