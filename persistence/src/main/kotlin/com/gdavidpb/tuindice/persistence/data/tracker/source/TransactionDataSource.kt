package com.gdavidpb.tuindice.persistence.data.tracker.source

import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionStatus

interface TransactionDataSource {
	suspend fun getPendingTransactions(): List<Transaction>
	suspend fun createTransaction(transaction: Transaction)
	suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus)
}