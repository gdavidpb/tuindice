package com.gdavidpb.tuindice.transactions.domain.repository

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface TransactionRepository {
	suspend fun enqueueTransaction(uid: String, transaction: Transaction)
}