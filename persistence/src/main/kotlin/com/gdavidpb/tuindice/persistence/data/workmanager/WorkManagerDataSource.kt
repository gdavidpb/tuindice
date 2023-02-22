package com.gdavidpb.tuindice.persistence.data.workmanager

import androidx.work.*
import com.gdavidpb.tuindice.persistence.data.tracker.source.SchedulerDataSource
import com.gdavidpb.tuindice.persistence.utils.WorkerBackoff
import com.gdavidpb.tuindice.persistence.utils.WorkerName
import java.util.concurrent.TimeUnit

class WorkManagerDataSource(
	private val workManager: WorkManager
) : SchedulerDataSource {
	override suspend fun enqueueSync() {
		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val syncWorker = OneTimeWorkRequestBuilder<SyncWorker>()
			.setConstraints(constraints)
			.setBackoffCriteria(
				backoffPolicy = BackoffPolicy.EXPONENTIAL,
				backoffDelay = WorkerBackoff.SYNC_WORKER,
				timeUnit = TimeUnit.MILLISECONDS
			)
			.build()

		workManager.enqueueUniqueWork(WorkerName.SYNC_WORKER, ExistingWorkPolicy.KEEP, syncWorker)
	}
}