package com.gdavidpb.tuindice.persistence.utils

import java.util.concurrent.TimeUnit

object WorkerName {
	const val SYNC_WORKER = "syncWorker"
}

object WorkerBackoff {
	val SYNC_WORKER = TimeUnit.HOURS.toMillis(1)
}