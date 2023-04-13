package com.gdavidpb.tuindice.transactions.data.offline.source

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface RemoteDataSource {
	suspend fun sync(transactions: List<Transaction>): List<Resolution>
}