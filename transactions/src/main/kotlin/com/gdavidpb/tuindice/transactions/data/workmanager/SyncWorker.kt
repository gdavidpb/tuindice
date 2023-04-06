package com.gdavidpb.tuindice.transactions.data.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.transactions.domain.repository.TransactionRepository

class SyncWorker(
	context: Context,
	workerParameters: WorkerParameters,
	private val authRepository: AuthRepository,
	private val transactionRepository: TransactionRepository
) : CoroutineWorker(context, workerParameters) {
	override suspend fun doWork(): Result {
		return runCatching {
			val activeUId = authRepository.getActiveAuth().uid

			val pendingTransactions = transactionRepository.getTransactionsQueue(uid = activeUId)

			// TODO trackerRepository.syncTransactions(uid = activeUId)
		}.fold(
			onSuccess = { Result.success() },
			onFailure = { Result.retry() }
		)
	}
}