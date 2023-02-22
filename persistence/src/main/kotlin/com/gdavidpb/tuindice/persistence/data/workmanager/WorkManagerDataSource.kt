package com.gdavidpb.tuindice.persistence.data.workmanager

import androidx.work.*
import com.gdavidpb.tuindice.persistence.data.tracker.source.SchedulerDataSource
import com.gdavidpb.tuindice.persistence.utils.WorkerName

class WorkManagerDataSource(
	private val workManager: WorkManager
) : SchedulerDataSource {
	override suspend fun enqueueSync() {
		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val syncWorker = OneTimeWorkRequestBuilder<SyncWorker>()
			.setConstraints(constraints)
			.build()

		workManager.enqueueUniqueWork(WorkerName.SYNC_WORKER, ExistingWorkPolicy.KEEP, syncWorker)
	}
}