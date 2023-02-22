package com.gdavidpb.tuindice.persistence.data.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gdavidpb.tuindice.persistence.domain.repository.TrackerRepository

class SyncWorker(
	context: Context,
	workerParameters: WorkerParameters,
	private val trackerRepository: TrackerRepository
) : CoroutineWorker(context, workerParameters) {
	override suspend fun doWork(): Result {
		return runCatching {
			val pendingTransactions = trackerRepository.getPendingTransactions()

			// TODO call sync api
		}.fold(
			onSuccess = { Result.success() },
			onFailure = { Result.retry() }
		)
	}
}