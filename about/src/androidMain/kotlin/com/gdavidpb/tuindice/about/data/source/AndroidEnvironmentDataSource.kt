package com.gdavidpb.tuindice.about.data.source

import android.content.Context
import android.content.pm.ApplicationInfo
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataRepository

class AndroidEnvironmentDataSource(
	private val context: Context
) : EnvironmentDataRepository {
	override fun isDebugEnvironment(): Boolean {
		return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
	}
}
