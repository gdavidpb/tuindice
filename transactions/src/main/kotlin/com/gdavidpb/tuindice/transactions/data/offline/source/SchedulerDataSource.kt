package com.gdavidpb.tuindice.transactions.data.offline.source

interface SchedulerDataSource {
	suspend fun scheduleSync()
}