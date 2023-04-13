package com.gdavidpb.tuindice.transactions.data.offline.source

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface LocalDataSource {
	suspend fun getTransactionsQueue(uid: String): List<Transaction>
	suspend fun applyResolutions(uid: String, resolutions: List<Resolution>)
	suspend fun enqueueTransaction(uid: String, transaction: Transaction): String
}