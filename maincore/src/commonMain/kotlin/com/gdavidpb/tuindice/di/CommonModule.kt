package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.ConfigDataSource
import com.gdavidpb.tuindice.base.data.source.SessionInvalidationDataSource
import com.gdavidpb.tuindice.base.data.source.SessionDataSource
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.contract.MemorySessionDataSource
import com.gdavidpb.tuindice.base.data.contract.PreferencesSessionDataSource
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
import com.gdavidpb.tuindice.data.source.messaging.MessagingDataSource
import com.gdavidpb.tuindice.data.contract.messaging.MessagingLocalDataSource
import com.gdavidpb.tuindice.data.contract.messaging.MessagingRemoteDataSource
import com.gdavidpb.tuindice.data.source.messaging.MessagingApiDataSource
import com.gdavidpb.tuindice.data.source.messaging.MessagingSettingsDataSource
import com.gdavidpb.tuindice.data.source.credentials.CredentialsDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncDataSource
import com.gdavidpb.tuindice.data.contract.sync.SyncRemoteDataSource
import com.gdavidpb.tuindice.data.contract.sync.SyncSettingsLocalDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncStatusDataSource
import com.gdavidpb.tuindice.data.contract.sync.SyncStatusLocalDataSource
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
	singleOf(::ConfigDataSource) { bind<ConfigRepository>() }

	singleOf(::InMemorySessionDataSource) { bind<MemorySessionDataSource>() }
	singleOf(::SecureStoreSessionDataSource) { bind<PreferencesSessionDataSource>() }
	singleOf(::SessionDataSource) { bind<SessionRepository>() }
	singleOf(::SessionInvalidationDataSource) { bind<SessionInvalidationRepository>() }

	singleOf(::MessagingApiDataSource) { bind<MessagingRemoteDataSource>() }
	singleOf(::MessagingSettingsDataSource) { bind<MessagingLocalDataSource>() }
	singleOf(::MessagingDataSource) { bind<MessagingRepository>() }

	singleOf(::CredentialsDataSource) { bind<CredentialsRepository>() }
	singleOf(::SyncSettingsDataSource) { bind<SyncSettingsLocalDataSource>() }
	singleOf(::SyncStatusSettingsDataSource) { bind<SyncStatusLocalDataSource>() }
	singleOf(::SyncStatusDataSource) { bind<SyncStatusRepository>() }
	singleOf(::SyncApiDataSource) { bind<SyncRemoteDataSource>() }
	single<SyncRepository> {
		SyncDataSource(
			settingsDataSource = get(),
			syncStatusRepository = get(),
			remoteDataSource = get()
		)
	}
}
