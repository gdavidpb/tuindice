package com.gdavidpb.tuindice.transactions.data.repository.transactions

import com.gdavidpb.tuindice.transactions.domain.model.Resolution
import com.gdavidpb.tuindice.transactions.domain.model.Transaction

interface RemoteDataSource {
	suspend fun sync(transactions: List<Transaction>): List<Resolution>
}