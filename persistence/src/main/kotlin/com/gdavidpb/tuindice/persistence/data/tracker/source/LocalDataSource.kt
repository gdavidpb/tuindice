package com.gdavidpb.tuindice.persistence.data.tracker.source

import com.gdavidpb.tuindice.persistence.domain.model.Transaction
import com.gdavidpb.tuindice.persistence.domain.model.TransactionData
import com.gdavidpb.tuindice.persistence.domain.model.TransactionStatus

interface LocalDataSource {
	suspend fun getPendingTransactions(): List<Transaction<TransactionData>>
	suspend fun createTransaction(transaction: Transaction<TransactionData>)
	suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus)
	suspend fun updateTransactionsStatus(from: TransactionStatus, to: TransactionStatus)
}