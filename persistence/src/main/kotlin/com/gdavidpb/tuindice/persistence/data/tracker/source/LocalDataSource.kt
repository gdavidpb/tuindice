package com.gdavidpb.tuindice.persistence.data.tracker.source

import com.gdavidpb.tuindice.persistence.domain.model.Resolution
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionStatus

interface LocalDataSource {
	suspend fun applyResolutions(resolutions: List<Resolution>)
	suspend fun getPendingTransactions(): List<Transaction>
	suspend fun createTransaction(transaction: Transaction): String
	suspend fun updateTransactionStatus(transactionId: String, status: TransactionStatus)
	suspend fun updateTransactionsStatus(from: TransactionStatus, to: TransactionStatus)
}