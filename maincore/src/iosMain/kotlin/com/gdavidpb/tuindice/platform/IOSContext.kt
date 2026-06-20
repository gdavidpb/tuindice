package com.gdavidpb.tuindice.platform

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.domain.model.IosHostCapabilities
import com.gdavidpb.tuindice.persistence.di.defaultIosDatabasePath

data class IOSContext(
	val hostCapabilities: IosHostCapabilities,
	val appEnvironment: AppEnvironment,
	val appStoreUrl: String,
	val configValues: DefaultRemoteConfigValues,
	val databasePath: String = defaultIosDatabasePath()
)
