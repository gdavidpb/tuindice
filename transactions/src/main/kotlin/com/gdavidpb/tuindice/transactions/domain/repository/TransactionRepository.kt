package com.gdavidpb.tuindice.transactions.domain.repository

import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction

interface TransactionRepository {
	suspend fun enqueueTransaction(transaction: Transaction<*>)
	suspend fun getTransactionsQueue(uid: String): List<Transaction<*>>
}