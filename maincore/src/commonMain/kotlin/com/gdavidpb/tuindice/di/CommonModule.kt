package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.repository.ConfigDataRepository
import com.gdavidpb.tuindice.base.data.repository.SessionDataRepository
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.MemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.PreferencesSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.data.source.settings.APP_STORE_NAME
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.data.repository.messaging.MessagingDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.MessagingLocalDataSource
import com.gdavidpb.tuindice.data.repository.messaging.MessagingRemoteDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.MessagingApiDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.MessagingSettingsDataSource
import com.gdavidpb.tuindice.data.repository.credentials.CredentialsDataRepository
import com.gdavidpb.tuindice.data.repository.sync.SyncDataRepository
import com.gdavidpb.tuindice.data.repository.sync.SyncRemoteDataSource
import com.gdavidpb.tuindice.data.repository.sync.SyncSettingsLocalDataSource
import com.gdavidpb.tuindice.data.repository.sync.source.SyncApiDataSource
import com.gdavidpb.tuindice.data.repository.sync.source.SyncSettingsDataSource
import com.gdavidpb.tuindice.data.source.settings.MultiplatformSettingsDataSource
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {
	single {
		createSharedJson()
	}

	single<Settings> {
		get<Settings.Factory>().create(APP_STORE_NAME)
	}

	single<SettingsRepository> {
		MultiplatformSettingsDataSource(get())
	}
	factoryOf(::ConfigDataRepository) { bind<ConfigRepository>() }

	singleOf(::InMemorySessionDataSource) { bind<MemorySessionDataSource>() }
	singleOf(::SecureStoreSessionDataSource) { bind<PreferencesSessionDataSource>() }
	factoryOf(::SessionDataRepository) { bind<SessionRepository>() }

	singleOf(::MessagingApiDataSource) { bind<MessagingRemoteDataSource>() }
	single<MessagingLocalDataSource> {
		MessagingSettingsDataSource(get())
	}
	factoryOf(::MessagingDataRepository) { bind<MessagingRepository>() }

	singleOf(::CredentialsDataRepository) { bind<CredentialsRepository>() }
	singleOf(::SyncSettingsDataSource) { bind<SyncSettingsLocalDataSource>() }
	singleOf(::SyncApiDataSource) { bind<SyncRemoteDataSource>() }
	single<SyncRepository> { SyncDataRepository(settingsDataSource = get(), remoteDataSource = get()) }
}
