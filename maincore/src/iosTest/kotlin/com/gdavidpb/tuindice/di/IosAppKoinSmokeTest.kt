package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.session.SessionResidue
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.summary.data.source.IosProfilePictureInputDataSource
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withStartedKoin
import kotlin.test.Test
import kotlin.test.assertTrue

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

	// The sign-out wipe and the start without a session reach the normalized profile pictures only
	// through these two bindings, which live in the iOS platform module.
	@Test
	fun bindsTheProfilePictureInputAsSessionMemoryAndResidue() = withStartedKoin(
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
		assertTrue(getAll<SessionMemory>().any { holder -> holder is IosProfilePictureInputDataSource })
		assertTrue(getAll<SessionResidue>().any { holder -> holder is IosProfilePictureInputDataSource })
	}
}
