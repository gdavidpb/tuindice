package com.gdavidpb.tuindice.transactions.data.workmanager

import androidx.work.*
import com.gdavidpb.tuindice.transactions.data.offline.source.SchedulerDataSource
import java.util.concurrent.TimeUnit

class WorkManagerDataSource(
	private val workManager: WorkManager
) : SchedulerDataSource {
	override suspend fun scheduleSync() {
		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val syncWorker = OneTimeWorkRequestBuilder<SyncWorker>()
			.setConstraints(constraints)
			.setBackoffCriteria(
				backoffPolicy = BackoffPolicy.EXPONENTIAL,
				backoffDelay = com.gdavidpb.tuindice.transactions.utils.WorkerBackoff.SYNC_WORKER,
				timeUnit = TimeUnit.MILLISECONDS
			)
			.build()

		workManager.enqueueUniqueWork(
			com.gdavidpb.tuindice.transactions.utils.WorkerName.SYNC_WORKER,
			ExistingWorkPolicy.KEEP,
			syncWorker
		)
	}
}