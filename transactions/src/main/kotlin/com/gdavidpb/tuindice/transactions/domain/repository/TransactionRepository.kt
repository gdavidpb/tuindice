package com.gdavidpb.tuindice.transactions.domain.repository

import com.gdavidpb.tuindice.transactions.domain.model.Transaction

interface TransactionRepository {
	suspend fun enqueueTransaction(uid: String, transaction: Transaction)
}