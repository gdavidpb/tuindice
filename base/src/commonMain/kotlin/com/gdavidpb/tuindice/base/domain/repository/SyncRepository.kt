package com.gdavidpb.tuindice.base.domain.repository

interface SyncRepository {
	fun scheduleSync(password: String)
}
