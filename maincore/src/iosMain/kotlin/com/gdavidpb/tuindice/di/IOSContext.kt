package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.persistence.di.defaultIosDatabasePath
import org.koin.core.KoinApplication
import org.koin.core.scope.Scope

data class IOSContext(
	val hostCapabilities: IosHostCapabilities,
	val appEnvironment: AppEnvironment,
	val configValues: DefaultRemoteConfigValues,
	val databasePath: String = defaultIosDatabasePath()
)

private const val IOS_CONTEXT_PROPERTY = "ios_context"

fun KoinApplication.iOSContext(context: IOSContext) {
	properties(
		mapOf(IOS_CONTEXT_PROPERTY to context)
	)
}

fun Scope.iOSContext(): IOSContext {
	return getKoin().getProperty(IOS_CONTEXT_PROPERTY)
		?: error("iOSContext is not registered. Configure it before loading iosPlatformModule.")
}
