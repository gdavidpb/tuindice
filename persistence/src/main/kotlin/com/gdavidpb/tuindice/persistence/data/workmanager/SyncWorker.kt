package com.gdavidpb.tuindice.persistence.data.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.persistence.domain.repository.TrackerRepository

class SyncWorker(
	context: Context,
	workerParameters: WorkerParameters,
	private val authRepository: AuthRepository,
	private val trackerRepository: TrackerRepository
) : CoroutineWorker(context, workerParameters) {
	override suspend fun doWork(): Result {
		return runCatching {
			val activeUId = authRepository.getActiveAuth().uid

			trackerRepository.syncPendingTransactions(uid = activeUId)
		}.fold(
			onSuccess = { Result.success() },
			onFailure = { Result.retry() }
		)
	}
}