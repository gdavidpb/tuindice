package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.repository.ConfigDataRepository
import com.gdavidpb.tuindice.base.data.repository.SessionInvalidationDataRepository
import com.gdavidpb.tuindice.base.data.repository.SessionDataRepository
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.MemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.PreferencesSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.data.source.settings.APP_STORE_NAME
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.data.repository.messaging.MessagingDataRepository
import com.gdavidpb.tuindice.data.source.messaging.MessagingLocalDataSource
import com.gdavidpb.tuindice.data.source.messaging.MessagingRemoteDataSource
import com.gdavidpb.tuindice.data.source.messaging.MessagingApiDataSource
import com.gdavidpb.tuindice.data.source.messaging.MessagingSettingsDataSource
import com.gdavidpb.tuindice.data.repository.credentials.CredentialsDataRepository
import com.gdavidpb.tuindice.data.repository.sync.SyncDataRepository
import com.gdavidpb.tuindice.data.source.sync.SyncRemoteDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncSettingsLocalDataSource
import com.gdavidpb.tuindice.data.repository.sync.SyncStatusDataRepository
import com.gdavidpb.tuindice.data.source.sync.SyncStatusLocalDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncApiDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncSettingsDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncStatusSettingsDataSource
import com.gdavidpb.tuindice.data.source.settings.MultiplatformSettingsDataSource
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {
	single {
		createSharedJson()
	}

	single<Settings> {
		get<Settings.Factory>().create(APP_STORE_NAME)
	}

	singleOf(::MultiplatformSettingsDataSource) { bind<SettingsRepository>() }
	singleOf(::ConfigDataRepository) { bind<ConfigRepository>() }

	singleOf(::InMemorySessionDataSource) { bind<MemorySessionDataSource>() }
	singleOf(::SecureStoreSessionDataSource) { bind<PreferencesSessionDataSource>() }
	singleOf(::SessionDataRepository) { bind<SessionRepository>() }
	singleOf(::SessionInvalidationDataRepository) { bind<SessionInvalidationRepository>() }

	singleOf(::MessagingApiDataSource) { bind<MessagingRemoteDataSource>() }
	singleOf(::MessagingSettingsDataSource) { bind<MessagingLocalDataSource>() }
	singleOf(::MessagingDataRepository) { bind<MessagingRepository>() }

	singleOf(::CredentialsDataRepository) { bind<CredentialsRepository>() }
	singleOf(::SyncSettingsDataSource) { bind<SyncSettingsLocalDataSource>() }
	singleOf(::SyncStatusSettingsDataSource) { bind<SyncStatusLocalDataSource>() }
	singleOf(::SyncStatusDataRepository) { bind<SyncStatusRepository>() }
	singleOf(::SyncApiDataSource) { bind<SyncRemoteDataSource>() }
	single<SyncRepository> {
		SyncDataRepository(
			settingsDataSource = get(),
			syncStatusRepository = get(),
			remoteDataSource = get()
		)
	}
}
