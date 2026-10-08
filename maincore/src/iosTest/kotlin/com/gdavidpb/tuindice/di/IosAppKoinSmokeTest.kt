package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withStartedKoin
import kotlin.test.Test

class IosAppKoinSmokeTest {
	@Test
	fun resolvesIosAppEntryPoints() = withStartedKoin(
		start = {
			startAppKoin(
				AppKoinBootstrapRequest(
					platformBootstrap = IosKoinBootstrap(
						iOSContext = iosSmokeTestContext(),
						platformVariantModules = emptyList()
					)
				)
			)
		}
		) {
			assertResolves(
				SignInViewModel::class,
				MainViewModel::class,
				BrowserViewModel::class,
				RecordViewModel::class
			)
		}
}
