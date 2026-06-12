package $PACKAGE.di

import $PACKAGE.domain.repository.$REPOSITORY_INTERFACE_NAME
import $PACKAGE.presentation.viewmodel.$VIEWMODEL_NAME
import $PACKAGE.testing.$RECORDING_REPOSITORY_NAME
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import org.koin.dsl.module

class $SMOKE_TEST_CLASS_NAME {
	@Test
	fun resolves$VIEWMODEL_NAME() = withKoinSmokeTest(
		$MODULE_VAR_NAME,
		module {
			single<$REPOSITORY_INTERFACE_NAME> { $RECORDING_REPOSITORY_NAME() }
			single<NetworkRepository> { FakeNetworkRepository(isAvailable = true) }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<EventPublisher> { NoOpEventPublisher }
			single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
		}
	) {
		assertResolves($VIEWMODEL_NAME::class)
	}
}
