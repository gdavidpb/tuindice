package com.gdavidpb.tuindice.about.data.source

import android.content.Context
import android.content.pm.ApplicationInfo
import com.gdavidpb.tuindice.about.data.source.EnvironmentDataSource

class AndroidEnvironmentDataSource(
	private val context: Context
) : EnvironmentDataSource {
	override fun isDebugEnvironment(): Boolean {
		return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
	}
}
