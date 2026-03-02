package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.gdavidpb.tuindice.base.data.source.SecureStoreDataSource
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.persistence.di.defaultIosDatabasePath
import org.koin.core.KoinApplication
import org.koin.core.scope.Scope

internal data class IOSContext(
	val hostCapabilities: IosHostCapabilities,
	val appEnvironment: AppEnvironment,
	val configValues: IosConfigValues,
	val secureStore: SecureStoreDataSource? = null,
	val dataStore: DataStore<Preferences> = createIosDataStore(),
	val databasePath: String = defaultIosDatabasePath()
)

private const val IOS_CONTEXT_PROPERTY = "ios_context"

internal fun KoinApplication.iOSContext(context: IOSContext) {
	properties(
		mapOf(IOS_CONTEXT_PROPERTY to context)
	)
}

internal fun Scope.iOSContext(): IOSContext {
	return getKoin().getProperty(IOS_CONTEXT_PROPERTY)
		?: error("iOSContext is not registered. Configure it before loading iosPlatformModule.")
}
