package com.gdavidpb.tuindice.transactions.domain.repository

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface ResolutionRepository {
	suspend fun syncTransactions(uid: String)
}