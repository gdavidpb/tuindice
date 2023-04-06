package com.gdavidpb.tuindice.transactions.data.transactions.source

interface SchedulerDataSource {
	suspend fun scheduleSync()
}