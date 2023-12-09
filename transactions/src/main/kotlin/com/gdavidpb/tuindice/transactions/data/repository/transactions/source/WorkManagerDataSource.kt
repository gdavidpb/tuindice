package com.gdavidpb.tuindice.transactions.data.repository.transactions.source

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gdavidpb.tuindice.transactions.data.repository.transactions.SchedulerDataSource
import com.gdavidpb.tuindice.transactions.data.repository.transactions.SyncWorker
import com.gdavidpb.tuindice.transactions.utils.WorkerBackoff
import com.gdavidpb.tuindice.transactions.utils.WorkerName
import java.util.concurrent.TimeUnit

class WorkManagerDataSource(
	private val workManager: WorkManager
) : SchedulerDataSource {
	override fun scheduleSync() {
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

		workManager.enqueueUniqueWork(
			WorkerName.SYNC_WORKER,
			ExistingWorkPolicy.KEEP,
			syncWorker
		)
	}
}