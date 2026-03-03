package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfig
import com.gdavidpb.tuindice.base.utils.RemoteConfigDefaultsProfile

internal fun iosDefaultConfigValues(buildVariant: IosBuildVariant): DefaultRemoteConfigValues {
	val profile = when (buildVariant) {
		IosBuildVariant.DEBUG -> RemoteConfigDefaultsProfile.DEBUG
		IosBuildVariant.PRODUCTION -> RemoteConfigDefaultsProfile.PRODUCTION
	}

	return DefaultRemoteConfig.values(profile)
}
