package com.gdavidpb.tuindice.enrollmentproof.di

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.EnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeFileRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import org.koin.dsl.module

class EnrollmentProofModuleKoinSmokeTest {
	@Test
	fun resolvesEnrollmentProofViewModel() = withKoinSmokeTest(
		enrollmentProofModule,
		module {
			single<EnrollmentProofRepository> { FakeEnrollmentProofRepository() }
			single<FileRepository> { FakeFileRepository(canOpen = true) }
			single<NetworkRepository> { FakeNetworkRepository(isAvailable = true) }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<EnrollmentProofTextProvider> { FakeEnrollmentProofTextProvider() }
			single<EventPublisher> { NoOpEventPublisher }
			single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
		}
	) {
		assertResolves(EnrollmentProofViewModel::class)
	}
}
