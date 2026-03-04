package com.gdavidpb.tuindice.platform.android

import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfig
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.base.utils.RemoteConfigDefaultsProfile

fun androidDefaultConfigValues(isDebug: Boolean): DefaultRemoteConfigValues {
	val profile = if (isDebug) {
		RemoteConfigDefaultsProfile.DEBUG
	} else {
		RemoteConfigDefaultsProfile.PRODUCTION
	}

	return DefaultRemoteConfig.values(profile)
}
