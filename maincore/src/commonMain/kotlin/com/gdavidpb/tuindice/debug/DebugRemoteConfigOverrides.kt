package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.utils.RemoteConfigKeys
import org.koin.core.Koin

fun Koin.setDebugAppAvailabilityNoticeOverride(
	enabled: Boolean,
	title: String,
	message: String
) {
	val remoteConfig = get<RemoteConfigDataRepository>()
	check(remoteConfig is DebugRemoteConfigDataSource) {
		"App availability notice overrides require DebugRemoteConfigDataSource."
	}

	remoteConfig.setStringOverride(
		key = RemoteConfigKeys.APP_AVAILABILITY_NOTICE_ENABLED,
		value = enabled.toString()
	)
	remoteConfig.setStringOverride(
		key = RemoteConfigKeys.APP_AVAILABILITY_NOTICE_TITLE,
		value = title
	)
	remoteConfig.setStringOverride(
		key = RemoteConfigKeys.APP_AVAILABILITY_NOTICE_MESSAGE,
		value = message
	)
}
