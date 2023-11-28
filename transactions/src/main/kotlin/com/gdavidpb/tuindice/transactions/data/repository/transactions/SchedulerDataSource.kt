package com.gdavidpb.tuindice.transactions.data.repository.transactions

interface SchedulerDataSource {
	suspend fun scheduleSync()
}