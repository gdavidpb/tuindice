package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.ConfigDataSource
import com.gdavidpb.tuindice.base.data.source.SessionInvalidationDataSource
import com.gdavidpb.tuindice.base.data.source.SessionDataSource
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.repository.MemorySessionDataRepository
import com.gdavidpb.tuindice.base.data.repository.PreferencesSessionDataRepository
import com.gdavidpb.tuindice.base.data.source.SecureStoreSessionDataSource
import com.gdavidpb.tuindice.base.data.source.usage.UsageDataConsentSettingsDataSource
import com.gdavidpb.tuindice.base.data.source.event.BufferedEventPublisher
import com.gdavidpb.tuindice.base.data.source.event.CompositeEventSubscriber
import com.gdavidpb.tuindice.base.data.source.settings.APP_STORE_NAME
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.data.source.cache.CoreCacheStateDataSource
import com.gdavidpb.tuindice.data.source.prerequisite.RecordDataPrerequisiteDataSource
import com.gdavidpb.tuindice.data.source.messaging.MessagingDataSource
import com.gdavidpb.tuindice.data.repository.messaging.MessagingLocalDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.MessagingRemoteDataRepository
import com.gdavidpb.tuindice.data.source.messaging.MessagingApiDataSource
import com.gdavidpb.tuindice.data.source.messaging.MessagingSettingsDataSource
import com.gdavidpb.tuindice.data.source.credentials.CredentialsDataSource
import com.gdavidpb.tuindice.data.source.pending.PendingChangesDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncDataSource
import com.gdavidpb.tuindice.data.repository.sync.SyncRemoteDataRepository
import com.gdavidpb.tuindice.data.repository.sync.SyncSettingsLocalDataRepository
import com.gdavidpb.tuindice.data.source.sync.SyncApiDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncSettingsDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncStatusSettingsDataSource
import com.gdavidpb.tuindice.data.source.settings.MultiplatformSettingsDataSource
import com.gdavidpb.tuindice.domain.repository.CoreCacheStateRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {
	singleOf(::createSharedJson)
	singleOf(::createAppSettings)

	singleOf(::MultiplatformSettingsDataSource) { bind<SettingsRepository>() }
	singleOf(::UsageDataConsentSettingsDataSource) { bind<UsageDataConsentRepository>() }
	singleOf(::ConfigDataSource) { bind<ConfigRepository>() }
	single<EventPublisher> {
		BufferedEventPublisher(
			usageDataConsentRepository = get(),
			eventSubscriber = CompositeEventSubscriber(subscribers = getAll<EventSubscriber>())
		)
	}

	singleOf(::InMemorySessionDataSource) { bind<MemorySessionDataRepository>() }
	singleOf(::SecureStoreSessionDataSource) { bind<PreferencesSessionDataRepository>() }
	singleOf(::SessionDataSource) { bind<SessionRepository>() }
	singleOf(::SessionInvalidationDataSource) { bind<SessionInvalidationRepository>() }

	singleOf(::MessagingApiDataSource) { bind<MessagingRemoteDataRepository>() }
	singleOf(::MessagingSettingsDataSource) { bind<MessagingLocalDataRepository>() }
	singleOf(::MessagingDataSource) { bind<MessagingRepository>() }

	singleOf(::CredentialsDataSource) { bind<CredentialsRepository>() }
	singleOf(::PendingChangesDataSource) { bind<PendingChangesRepository>() }
	singleOf(::SyncSettingsDataSource) { bind<SyncSettingsLocalDataRepository>() }
	singleOf(::SyncStatusSettingsDataSource) { bind<SyncStatusRepository>() }
	singleOf(::SyncApiDataSource) { bind<SyncRemoteDataRepository>() }
	singleOf(::CoreCacheStateDataSource) { bind<CoreCacheStateRepository>() }
	singleOf(::RecordDataPrerequisiteDataSource) { bind<RecordDataPrerequisiteRepository>() }
	singleOf(::createSyncRepository)
}

private fun createAppSettings(settingsFactory: Settings.Factory): Settings {
	return settingsFactory.create(APP_STORE_NAME)
}

private fun createSyncRepository(
	settingsDataSource: SyncSettingsLocalDataRepository,
	syncStatusRepository: SyncStatusRepository,
	remoteDataSource: SyncRemoteDataRepository,
	recordLocalDataSource: AcademicRecordLocalDataRepository,
	userLocalDataSource: LocalDataRepository
): SyncRepository {
	return SyncDataSource(
		settingsDataSource = settingsDataSource,
		syncStatusRepository = syncStatusRepository,
		remoteDataSource = remoteDataSource,
		recordLocalDataSource = recordLocalDataSource,
		userLocalDataSource = userLocalDataSource
	)
}
