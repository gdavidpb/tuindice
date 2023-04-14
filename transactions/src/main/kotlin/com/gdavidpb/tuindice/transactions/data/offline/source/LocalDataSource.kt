package com.gdavidpb.tuindice.transactions.data.offline.source

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface LocalDataSource {
	suspend fun getTransactionsQueue(uid: String): List<Transaction>
	suspend fun enqueueTransaction(uid: String, transaction: Transaction): String

	suspend fun applyResolutions(resolutions: List<Resolution>)
}