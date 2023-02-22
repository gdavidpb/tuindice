package com.gdavidpb.tuindice.persistence.data.tracker.source

interface SchedulerDataSource {
	suspend fun enqueueSync()
}