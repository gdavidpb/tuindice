package com.gdavidpb.tuindice.data.source.android

import android.app.ActivityManager
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository

class AndroidApplicationDataSource(
	private val activityManager: ActivityManager
) : ApplicationRepository {
	override suspend fun clearData() {
		activityManager.clearApplicationUserData()
	}
}