package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.data.repository.sync.FakeAcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.data.repository.sync.FakePensumRevalidationRepository
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncRemoteDataSource
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncSettingsLocalDataSource
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncStatusRepository
import com.gdavidpb.tuindice.data.repository.sync.FakeUserLocalDataRepository
import com.gdavidpb.tuindice.data.repository.sync.SyncRemoteDataRepository
import com.gdavidpb.tuindice.data.repository.sync.SyncResultLocalDataRepository
import com.gdavidpb.tuindice.data.repository.sync.SyncSettingsLocalDataRepository
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRevalidationRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordOutboxDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.gdavidpb.tuindice.testing.NoOpRecordOutboxDataRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import org.koin.dsl.module
import kotlin.test.Test

class CommonModuleKoinSmokeTest {
	// Resolves the real SyncRepository definition; only its leaves are faked. The post-sync
	// collaborators come from commonModule itself (SyncResultLocalDataRepository) and from
	// pensumModule in the app (PensumRevalidationRepository).
	@Test
	fun resolvesSyncRepositoryWithItsPostSyncCollaborators() = withKoinSmokeTest(
		commonModule,
		module {
			single<SyncSettingsLocalDataRepository> { FakeSyncSettingsLocalDataSource(onCooldown = false) }
			single<SyncStatusRepository> { FakeSyncStatusRepository() }
			single<SyncRemoteDataRepository> { FakeSyncRemoteDataSource() }
			single<AcademicRecordLocalDataRepository> { FakeAcademicRecordLocalDataRepository() }
			single<AcademicRecordOutboxDataRepository> { NoOpRecordOutboxDataRepository }
			single<LocalDataRepository> { FakeUserLocalDataRepository() }
			single<PensumRevalidationRepository> { FakePensumRevalidationRepository() }
		}
	) {
		assertResolves(
			SyncRepository::class,
			SyncResultLocalDataRepository::class
		)
	}
}
