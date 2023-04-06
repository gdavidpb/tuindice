package com.gdavidpb.tuindice.transactions.data.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.transactions.domain.repository.ResolutionRepository

class SyncWorker(
	context: Context,
	workerParameters: WorkerParameters,
	private val authRepository: AuthRepository,
	private val resolutionRepository: ResolutionRepository
) : CoroutineWorker(context, workerParameters) {
	override suspend fun doWork(): Result {
		return runCatching {
			val activeUId = authRepository.getActiveAuth().uid

			resolutionRepository.syncTransactions(uid = activeUId)
		}.fold(
			onSuccess = { Result.success() },
			onFailure = { Result.retry() }
		)
	}
}