package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.data.repository.AppInfoDataRepository
import platform.Foundation.NSBundle

class IosAppInfoDataSource : AppInfoDataRepository {
	override fun appVersionName(): String {
		return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString")
			?.toString()
			.orEmpty()
	}

	override fun appVersionCode(): Long {
		return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion")
			?.toString()
			?.toLongOrNull()
			?: 0L
	}
}
