package com.gdavidpb.tuindice.persistence.data.tracker.source

import com.gdavidpb.tuindice.persistence.domain.model.Resolution
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface RemoteDataSource {
	suspend fun sync(transactions: List<Transaction>): List<Resolution>
}