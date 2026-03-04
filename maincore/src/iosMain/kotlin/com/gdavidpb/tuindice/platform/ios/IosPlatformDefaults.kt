package com.gdavidpb.tuindice.platform.ios

import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfig
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.base.utils.RemoteConfigDefaultsProfile
import com.gdavidpb.tuindice.domain.model.IosBuildVariant

fun iosDefaultConfigValues(buildVariant: IosBuildVariant): DefaultRemoteConfigValues {
	val profile = when (buildVariant) {
		IosBuildVariant.DEBUG -> RemoteConfigDefaultsProfile.DEBUG
		IosBuildVariant.PRODUCTION -> RemoteConfigDefaultsProfile.PRODUCTION
	}

	return DefaultRemoteConfig.values(profile)
}
